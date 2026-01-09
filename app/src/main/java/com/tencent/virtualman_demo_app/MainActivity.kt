package com.tencent.virtualman_demo_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCAudioFrame
import com.tencent.trtc.TRTCCloudListener
import com.tencent.trtc.TRTCStatistics
import com.tencent.virtualman.TRTCListener
import com.tencent.virtualman.Virtualman
import com.tencent.virtualman.VirtualmanParams
import com.tencent.virtualman.data.ResponseData
import com.tencent.virtualman.data.SendAudioParams
import com.tencent.virtualman.data.AssetVirtualmanParams
import com.tencent.virtualman.data.ExtraInfo
import com.tencent.virtualman.data.ProtocolOption
import com.tencent.virtualman.data.SendStreamTextParams
import com.tencent.virtualman.data.SendTextParams
import com.tencent.virtualman.data.VirtualmanProjectParams
import com.tencent.virtualman.net.WsListener
import com.tencent.virtualman.utils.CommonUtils.genTrtcPrivateMapKey
import com.tencent.virtualman.utils.CommonUtils.genTrtcUserSig
import okhttp3.Response
import okhttp3.WebSocket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val TAG = "demo_index"
    private lateinit var mVirtualman: Virtualman
    private var mVirtualmanParams = VirtualmanParams()

    // UI
    private lateinit var btnRecord: Button
    private lateinit var btnAudioFile: Button
    private lateinit var btnSend: Button
    private lateinit var btnStreamSend: Button
    private lateinit var btnInterrupt: Button
    private lateinit var btnProjectStream: Button
    private lateinit var btnAssetStream: Button
    private lateinit var btnCloseStream: Button
    private lateinit var btnToggleSize: Button
    private lateinit var etInput: EditText
    private lateinit var tvStatus: TextView
    private lateinit var tvMode: TextView
    private lateinit var etLoopCount: EditText
    private lateinit var tvLoopStatus: TextView
    private lateinit var containerBig: FrameLayout
    private lateinit var containerSmall: FrameLayout
    
    // 循环测试相关
    private var isLoopTesting = false
    private var loopTotalCount = 1
    private var loopCurrentCount = 0
    private var loopTestText = ""
    private var loopTestType = LoopTestType.TEXT  // 循环测试类型
    
    // 循环测试类型枚举
    enum class LoopTestType {
        TEXT,           // 文本发送
        STREAM_TEXT,    // 流式文本发送
        AUDIO_FILE      // 音频文件驱动
    }

    // 是否已建流
    private var isStreamInitialized = false
    
    // 是否在小窗模式
    private var isSmallMode = false
    
    // 是否已收到 WaitingTextStart（用于打断逻辑判断）
    private var hasReceivedWaitingTextStart = false

    // TRTC 音频驱动相关
    private var isRecording = false
    private var audioRecordReqId = ""
    private var audioRecordSeq = 1
    private val audioBuffer = mutableListOf<ByteArray>()  // 音频缓冲区，累积 8 帧（160ms）再发送

    private var trtcCloud: TRTCCloud? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initImmersionBar()
        
        // 检查权限
        if (allPermissionsGranted()) {
            init()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        trtcCloud = TRTCCloud.sharedInstance(applicationContext)
        // 通过 addListener 添加 SEI 监听（演示外部直接使用 TRTC 实例）
         trtcCloud?.addListener(object : TRTCCloudListener() {
             override fun onRecvSEIMsg(userId: String?, data: ByteArray?) {
                 if (data != null && data.size > 16) {
                     val jsonData = String(data, 16, data.size - 16, Charsets.UTF_8)
                     Log.i(TAG, "[addListener] 收到SEI消息: userId=$userId, data=$jsonData")
                 }
             }
         })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        if (isStreamInitialized) {
            mVirtualman.close()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                init()
            } else {
                Toast.makeText(this, "需要录音权限", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        // 初始化UI
        btnRecord = findViewById(R.id.btn_record)
        btnAudioFile = findViewById(R.id.btn_audio_file)
        btnSend = findViewById(R.id.btn_send)
        btnStreamSend = findViewById(R.id.btn_stream_send)
        btnInterrupt = findViewById(R.id.btn_interrupt)
        btnProjectStream = findViewById(R.id.btn_project_stream)
        btnAssetStream = findViewById(R.id.btn_asset_stream)
        btnCloseStream = findViewById(R.id.btn_close_stream)
        btnToggleSize = findViewById(R.id.btn_toggle_size)
        etInput = findViewById(R.id.et_input)
        tvStatus = findViewById(R.id.tv_status)
        tvMode = findViewById(R.id.tv_mode)
        etLoopCount = findViewById(R.id.et_loop_count)
        tvLoopStatus = findViewById(R.id.tv_loop_status)
        containerBig = findViewById(R.id.container_big)
        containerSmall = findViewById(R.id.container_small)

        // 动态创建 Virtualman 并添加到大容器
        mVirtualman = Virtualman(this)
        containerBig.addView(mVirtualman, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // 项目建流按钮
        btnProjectStream.setOnClickListener {
            initVirtualman(useAssetMode = false)
        }

        // 资产建流按钮
        btnAssetStream.setOnClickListener {
            initVirtualman(useAssetMode = true)
        }

        // 关流按钮
        btnCloseStream.setOnClickListener {
            mVirtualman.close()
            isStreamInitialized = false
            updateStatus("已关流")
            tvMode.text = "未建流"
        }

        // 切换大小按钮
        btnToggleSize.setOnClickListener {
            toggleViewSize()
        }

        // 录音按钮 - 使用 TRTC 本地音频
        btnRecord.setOnClickListener {
            if (!isStreamInitialized) {
                updateStatus("请先建流")
                return@setOnClickListener
            }
            if (!isRecording) {
                startTrtcRecording()
                btnRecord.text = "停止录音"
            } else {
                stopTrtcRecording()
                btnRecord.text = "语音驱动"
            }
        }

        // 音频文件驱动按钮
        btnAudioFile.setOnClickListener {
            if (!isStreamInitialized) {
                updateStatus("请先建流")
                return@setOnClickListener
            }
            startLoopTest(LoopTestType.AUDIO_FILE, "")
        }

        // 发送文本按钮
        btnSend.setOnClickListener {
            if (!isStreamInitialized) {
                updateStatus("请先建流")
                return@setOnClickListener
            }
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) {
                updateStatus("请输入文本")
                return@setOnClickListener
            }
            startLoopTest(LoopTestType.TEXT, text)
        }

        // 流式文本发送按钮
        btnStreamSend.setOnClickListener {
            if (!isStreamInitialized) {
                updateStatus("请先建流")
                return@setOnClickListener
            }
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) {
                updateStatus("请输入文本")
                return@setOnClickListener
            }
            startLoopTest(LoopTestType.STREAM_TEXT, text)
        }

        // 打断按钮
        btnInterrupt.setOnClickListener {
            if (!isStreamInitialized) {
                updateStatus("请先建流")
                return@setOnClickListener
            }
            // 停止循环测试
            if (isLoopTesting) {
                val wasAudioLoop = loopTestType == LoopTestType.AUDIO_FILE
                stopLoopTest()
                if (wasAudioLoop) {
                    // 音频驱动无法通过 stop() 打断，只停止循环
                    updateStatus("已停止循环")
                    return@setOnClickListener
                }
            }
            val result = mVirtualman.stop()
            if (result == true) {
                updateStatus("已打断")
            } else {
                updateStatus("打断失败")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initVirtualman(useAssetMode: Boolean) {
        // 初始化数智人
        mVirtualmanParams = VirtualmanParams()
        mVirtualmanParams.appkey = Config.APP_KEY
        mVirtualmanParams.accesstoken = Config.ACCESS_TOKEN
        mVirtualmanParams.infoVisible = true

        if (useAssetMode) {
            // 方式1: 使用 AssetVirtualmanKey 建流
            mVirtualmanParams.assetVirtualmanParams = AssetVirtualmanParams().apply {
                assetVirtualmanKey = Config.ASSET_VIRTUALMAN_KEY
                extraInfo = ExtraInfo().apply {
                    alphaChannelEnable = true
                }
                userId = "test001"
            }
            tvMode.text = "模式: 资产建流"
        } else {
            // 方式2: 使用 VirtualmanProjectId 建流
            mVirtualmanParams.virtualmanProjectParams = VirtualmanProjectParams().apply {
                virtualmanProjectId = Config.VIRTUALMAN_PROJECT_ID
                userId = "test001"
                extraInfo = ExtraInfo().apply {
                    alphaChannelEnable = true
                }
            }
            tvMode.text = "模式: 项目建流"
        }

        updateStatus("正在初始化...")
        
        mVirtualman.init(mVirtualmanParams, { result ->
            Log.i(TAG, "init result: $result")
            runOnUiThread {
                if (result.toString().startsWith("success")) {
                    isStreamInitialized = true
                    updateStatus("初始化成功，可以开始对话")
                } else {
                    isStreamInitialized = false
                    updateStatus("初始化失败: $result")
                }
            }
        }, StreamWebSocketListener())

        // 设置TRTC监听器，接收SEI消息和网络状态
        mVirtualman.setTRTCListener(object : TRTCListener {
            override fun onRecvSEIMsg(userId: String?, data: ByteArray?) {
                Log.i(TAG, "收到SEI消息: ${data?.size}")
                // SEI数据：前16字节为uuid，第17字节开始为JSON格式的包体数据
                if (data != null && data.size > 16) {
                    val jsonData = String(data, 16, data.size - 16, Charsets.UTF_8)
                    Log.i(TAG, "收到SEI消息: userId=$userId, data=$jsonData")
                }
            }
            override fun onFirstVideoFrame(userId: String?, streamType: Int, width: Int, height: Int) {
                Log.i(TAG, "收到首帧视频: userId=$userId, streamType=$streamType, width=$width, height=$height")
            }
            override fun onError(errCode: Int, errMsg: String?, extraInfo: Bundle?) {
                Log.e(TAG, "TRTC错误: code=$errCode, msg=$errMsg")
            }
            override fun onConnectionLost() {
                Log.w(TAG, "TRTC连接断开")
                runOnUiThread {
                    updateStatus("网络连接断开，正在重连...")
                }
            }
            override fun onTryToReconnect() {
                Log.i(TAG, "TRTC正在重连...")
            }
            override fun onConnectionRecovery() {
                Log.i(TAG, "TRTC连接已恢复")
                runOnUiThread {
                    updateStatus("网络连接已恢复")
                }
            }
            override fun onNetworkQuality(localQuality: TRTCCloudDef.TRTCQuality, remoteQuality: ArrayList<TRTCCloudDef.TRTCQuality>?) {
                // 网络质量: 0-未知, 1-极好, 2-好, 3-一般, 4-差, 5-极差, 6-断开
                val qualityDesc = when (localQuality.quality) {
                    0 -> "未知"
                    1 -> "极好"
                    2 -> "好"
                    3 -> "一般"
                    4 -> "差"
                    5 -> "极差"
                    6 -> "断开"
                    else -> "未知(${localQuality.quality})"
                }
//                Log.d(TAG, "网络质量: 本地=$qualityDesc, 远端数量=${remoteQuality?.size ?: 0}")
            }
            override fun onStatistics(statistics: TRTCStatistics?) {
                statistics?.let {
//                    Log.d(TAG, "统计: 上行丢包=${it.upLoss}%, 下行丢包=${it.downLoss}%, RTT=${it.rtt}ms")
                }
            }
        })
    }

    private fun updateStatus(status: String) {
        tvStatus.text = status
    }
    
    // 开始循环测试
    private fun startLoopTest(type: LoopTestType, text: String) {
        val loopCount = etLoopCount.text.toString().toIntOrNull() ?: 1
        
        isLoopTesting = true
        loopTotalCount = loopCount  // 0 表示无限循环
        loopCurrentCount = 0
        loopTestText = text
        loopTestType = type
        
        // 执行第一次
        executeLoopOnce()
    }
    
    // 执行一次循环
    private fun executeLoopOnce() {
        loopCurrentCount++
        hasReceivedWaitingTextStart = false
        updateLoopStatus()
        
        val typeName = when (loopTestType) {
            LoopTestType.TEXT -> "文本"
            LoopTestType.STREAM_TEXT -> "流式文本"
            LoopTestType.AUDIO_FILE -> "音频文件"
        }
        val progressText = if (loopTotalCount == 0) "$loopCurrentCount/∞" else "$loopCurrentCount/$loopTotalCount"
        
        when (loopTestType) {
            LoopTestType.TEXT -> {
                val textParams = SendTextParams(
                    loopTestText,
                    videoSeiInfo = "{\"TextSEI\":\"Hello~ apple\"}"
                )
                val result = mVirtualman.sendText(textParams)
                if (result == true) {
                    updateStatus("[$typeName] 循环 $progressText 发送成功，等待响应...")
                } else {
                    updateStatus("[$typeName] 循环 $progressText 发送失败")
                    stopLoopTest()
                }
            }
            LoopTestType.STREAM_TEXT -> {
                sendTextInChunksForLoop()
            }
            LoopTestType.AUDIO_FILE -> {
                sendAudioFileForLoop()
            }
        }
    }
    
    // 流式文本发送（循环版本）
    private fun sendTextInChunksForLoop() {
        val typeName = "流式文本"
        val progressText = if (loopTotalCount == 0) "$loopCurrentCount/∞" else "$loopCurrentCount/$loopTotalCount"
        val chunkSize = 10
        val reqId = UUID.randomUUID().toString().replace("-", "")
        val chunks = loopTestText.chunked(chunkSize)
        
        Thread {
            var allSuccess = true
            chunks.forEachIndexed { index, chunk ->
                val isLast = index == chunks.size - 1
                val seq = index + 1
                val params = SendStreamTextParams(
                    reqId = reqId,
                    text = chunk,
                    seq = seq,
                    isFinal = isLast,
                    videoSeiInfo = if (seq == 1) "{\"StreamTextSEI\":\"Hello~ apple\"}" else null
                )
                val result = mVirtualman.sendStreamText(params)
                if (result != true) {
                    allSuccess = false
                    Log.w(TAG, "流式发送失败 seq=${index + 1}")
                }
                if (!isLast) {
                    Thread.sleep(100)
                }
            }
            runOnUiThread {
                if (allSuccess) {
                    updateStatus("[$typeName] 循环 $progressText 发送成功，等待响应...")
                } else {
                    updateStatus("[$typeName] 循环 $progressText 发送失败")
                    stopLoopTest()
                }
            }
        }.start()
    }
    
    // 音频文件发送（循环版本）
    private fun sendAudioFileForLoop() {
        val typeName = "音频文件"
        val progressText = if (loopTotalCount == 0) "$loopCurrentCount/∞" else "$loopCurrentCount/$loopTotalCount"
        Thread {
            try {
                val inputStream = assets.open("audio_test_01.pcm")
                val pcmData = inputStream.readBytes()
                inputStream.close()

                val chunkSize = 5120
                val reqId = UUID.randomUUID().toString().replace("-", "")
                val totalChunks = (pcmData.size + chunkSize - 1) / chunkSize
                var seq = 1

                runOnUiThread {
                    updateStatus("[$typeName] 循环 $progressText 驱动中... 共${totalChunks}个分片")
                }

                var offset = 0
                while (offset < pcmData.size) {
                    val remaining = pcmData.size - offset
                    val currentChunkSize = minOf(chunkSize, remaining)
                    val chunk = pcmData.copyOfRange(offset, offset + currentChunkSize)
                    val isLast = offset + currentChunkSize >= pcmData.size

                    val base64String = Base64.encodeToString(chunk, Base64.NO_WRAP)
                    val currentSeq = seq
                    val result = mVirtualman.sendAudio(SendAudioParams(
                        reqId = reqId,
                        audio = base64String,
                        seq = currentSeq,
                        isFinal = isLast,
                        videoSeiInfo = if (currentSeq == 1) "{\"AudioFileSEI\":\"Hello~ apple\"}" else null
                    ))

                    if (result != true) {
                        Log.w(TAG, "音频文件发送失败 seq=$currentSeq")
                    } else {
                        Log.d(TAG, "音频文件发送成功 seq=$currentSeq, size=$currentChunkSize, isLast=$isLast")
                    }

                    seq++
                    offset += currentChunkSize

                    if (!isLast) {
                        Thread.sleep(140)
                    }
                }

                runOnUiThread {
                    updateStatus("[$typeName] 循环 $progressText 发送完成，等待响应...")
                }
            } catch (e: Exception) {
                Log.e(TAG, "音频文件驱动失败", e)
                runOnUiThread {
                    updateStatus("[$typeName] 循环 $progressText 驱动失败: ${e.message}")
                    stopLoopTest()
                }
            }
        }.start()
    }
    
    // 更新循环状态显示
    private fun updateLoopStatus() {
        if (isLoopTesting) {
            val typeName = when (loopTestType) {
                LoopTestType.TEXT -> "文本"
                LoopTestType.STREAM_TEXT -> "流式"
                LoopTestType.AUDIO_FILE -> "音频"
            }
            val progressText = if (loopTotalCount == 0) "$loopCurrentCount/∞" else "$loopCurrentCount/$loopTotalCount"
            tvLoopStatus.text = "[$typeName] $progressText"
        } else {
            tvLoopStatus.text = ""
        }
    }
    
    // 停止循环测试
    private fun stopLoopTest() {
        isLoopTesting = false
        loopCurrentCount = 0
        updateLoopStatus()
    }

    // 切换 View 大小：在大容器和小容器之间移动
    private fun toggleViewSize() {
        if (!isStreamInitialized) {
            updateStatus("请先建流")
            return
        }

        // 从当前容器移除
        (mVirtualman.parent as? ViewGroup)?.removeView(mVirtualman)

        if (isSmallMode) {
            // 切换到大窗
            containerSmall.visibility = View.GONE
            containerBig.addView(mVirtualman, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            btnToggleSize.text = "切换小窗"
            updateStatus("已切换到大窗模式")
        } else {
            // 切换到小窗
            containerSmall.visibility = View.VISIBLE
            containerSmall.addView(mVirtualman, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            btnToggleSize.text = "切换大窗"
            updateStatus("已切换到小窗模式")
        }
        isSmallMode = !isSmallMode
    }

    // 开始 TRTC 录音
    private fun startTrtcRecording() {
        val cloud = trtcCloud ?: return
        
        isRecording = true
        audioRecordReqId = UUID.randomUUID().toString().replace("-", "")
        audioRecordSeq = 1
        audioBuffer.clear()  // 清空缓冲区
        updateStatus("正在录音...")
        
        // 设置音频帧回调格式：16kHz, 单声道, 20ms一帧(320采样点)
        val format = TRTCCloudDef.TRTCAudioFrameCallbackFormat()
        format.sampleRate = 16000
        format.channel = 1
        format.samplesPerCall = 320  // 20ms 一帧
        cloud.setLocalProcessedAudioFrameCallbackFormat(format)
        
        // 设置音频帧回调
        cloud.setAudioFrameListener(object : TRTCCloudListener.TRTCAudioFrameListener {
            override fun onCapturedAudioFrame(frame: TRTCAudioFrame?) {
                // 不处理原始采集帧
            }

            override fun onLocalProcessedAudioFrame(frame: TRTCAudioFrame?) {
                // 处理本地处理后的音频帧
                if (frame?.data != null && isRecording) {
                    val pcmData = frame.data.copyOf()  // 拷贝数据，避免被覆盖
                    
                    synchronized(audioBuffer) {
                        audioBuffer.add(pcmData)
                        
                        // 累积 8 帧（160ms）再发送
                        if (audioBuffer.size >= 8) {
                            // 合并 8 帧数据
                            val totalSize = audioBuffer.sumOf { it.size }
                            val mergedData = ByteArray(totalSize)
                            var offset = 0
                            for (chunk in audioBuffer) {
                                System.arraycopy(chunk, 0, mergedData, offset, chunk.size)
                                offset += chunk.size
                            }
                            audioBuffer.clear()
                            
                            val seq = audioRecordSeq++
                            val reqId = audioRecordReqId
                            // 异步发送，避免阻塞音频回调
                            Thread {
                                val base64String = Base64.encodeToString(mergedData, Base64.NO_WRAP)
                                val result = mVirtualman.sendAudio(SendAudioParams(
                                    reqId = reqId,
                                    audio = base64String,
                                    seq = seq,
                                    isFinal = false,
                                    // 只在第一个分片发送 SEI
                                    videoSeiInfo = if (seq == 1) "{\"AudioSEI\":\"Hello~ apple\"}" else null
                                ))
                                if (result != true) {
                                    Log.w(TAG, "音频发送失败 seq=$seq")
                                }
                            }.start()
                        }
                    }
                }
            }

            override fun onRemoteUserAudioFrame(frame: TRTCAudioFrame?, userId: String?) {
                // 不处理远端用户音频帧
            }

            override fun onMixedPlayAudioFrame(frame: TRTCAudioFrame?) {
                // 不处理混音播放帧
            }

            override fun onMixedAllAudioFrame(frame: TRTCAudioFrame?) {
                // 不处理所有混音帧
            }

            override fun onVoiceEarMonitorAudioFrame(frame: TRTCAudioFrame?) {
                // 不处理耳返帧
            }
        })
        
        // 开启本地音频采集
        cloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH)
    }

    // 停止 TRTC 录音
    private fun stopTrtcRecording() {
        val cloud = trtcCloud ?: return
        
        isRecording = false
        
        // 发送缓冲区剩余的数据（如果有）
        synchronized(audioBuffer) {
            if (audioBuffer.isNotEmpty()) {
                val totalSize = audioBuffer.sumOf { it.size }
                val mergedData = ByteArray(totalSize)
                var offset = 0
                for (chunk in audioBuffer) {
                    System.arraycopy(chunk, 0, mergedData, offset, chunk.size)
                    offset += chunk.size
                }
                audioBuffer.clear()
                
                val seq = audioRecordSeq++
                val base64String = Base64.encodeToString(mergedData, Base64.NO_WRAP)
                mVirtualman.sendAudio(SendAudioParams(
                    reqId = audioRecordReqId,
                    audio = base64String,
                    seq = seq,
                    isFinal = false
                ))
            }
        }
        
        // 发送结束标记
        val result = mVirtualman.sendAudio(SendAudioParams(
            reqId = audioRecordReqId,
            audio = "",
            seq = audioRecordSeq,
            isFinal = true
        ))
        if (result == true) {
            updateStatus("录音结束，等待响应...")
        } else {
            updateStatus("录音结束，发送结束标记失败")
        }
        
        // 停止本地音频采集
        cloud.stopLocalAudio()
        
        // 移除音频帧回调
        cloud.setAudioFrameListener(null)
    }

    // WebSocket 监听
    inner class StreamWebSocketListener : WsListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
//            Log.i(TAG, "WebSocket onMessage: $text")
            val rdata = Gson().fromJson(text, ResponseData::class.java)
            
            // 优先检查错误码
            val errorCode = rdata.Payload?.ErrorCode
            val errorMsg = rdata.Payload?.ErrorMsg ?: rdata.Payload?.ErrorMessage
            if (errorCode != null && errorCode != 0) {
                Log.e(TAG, "业务错误: code=$errorCode, msg=$errorMsg, raw=$text")
                runOnUiThread {
                    updateStatus("错误: [$errorCode] $errorMsg")
                }
                return
            }
            
            when (rdata.Payload?.Type) {
                // Type 3: 播报状态
                3 -> {
                    val speakStatus = rdata.Payload?.SpeakStatus
                    Log.d(TAG, "speakStatus: $speakStatus")
                    runOnUiThread {
                        when (speakStatus) {
                            "WaitingTextStart" -> {
                                hasReceivedWaitingTextStart = true
                                updateStatus("等待播报开始...")
                            }
                            "TextOver", "AudioOver" -> {
                                hasReceivedWaitingTextStart = false
                                // 检查是否在循环测试中（loopTotalCount == 0 表示无限循环）
                                val isInfiniteLoop = loopTotalCount == 0
                                if (isLoopTesting && (isInfiniteLoop || loopCurrentCount < loopTotalCount)) {
                                    val typeName = when (loopTestType) {
                                        LoopTestType.TEXT -> "文本"
                                        LoopTestType.STREAM_TEXT -> "流式文本"
                                        LoopTestType.AUDIO_FILE -> "音频文件"
                                    }
                                    val progressText = if (isInfiniteLoop) "$loopCurrentCount/∞" else "$loopCurrentCount/$loopTotalCount"
                                    updateStatus("[$typeName] 循环 $progressText 完成，开始下一次...")
                                    // 延迟一点再发送下一次，避免太快
                                    btnSend.postDelayed({
                                        executeLoopOnce()
                                    }, 500)
                                } else if (isLoopTesting) {
                                    // 循环测试完成
                                    val typeName = when (loopTestType) {
                                        LoopTestType.TEXT -> "文本"
                                        LoopTestType.STREAM_TEXT -> "流式文本"
                                        LoopTestType.AUDIO_FILE -> "音频文件"
                                    }
                                    updateStatus("[$typeName] 循环测试完成！共 $loopTotalCount 次")
                                    stopLoopTest()
                                } else {
                                    updateStatus("播报完成，可以继续对话")
                                }
                            }
                            "Error" -> {
                                hasReceivedWaitingTextStart = false
                                updateStatus("播报出错")
                            }
                            else -> updateStatus("播报状态: $speakStatus")
                        }
                    }
                }
                // Type 4: 大模型返回内容
                4 -> {
                    val messageText = rdata.Payload?.Text
                    if (!messageText.isNullOrEmpty()) {
                        Log.i(TAG, "大模型回复: $messageText")
                        runOnUiThread {
                            updateStatus("大模型回复中...")
                        }
                    }
                }
                // Type 5: 大模型思考过程内容
                5 -> {
                    val thinkingText = rdata.Payload?.Text
                    if (!thinkingText.isNullOrEmpty()) {
                        Log.i(TAG, "大模型思考: $thinkingText")
                        runOnUiThread {
                            updateStatus("大模型思考中...")
                        }
                    }
                }
                // Type 9: 驱动失败
                9 -> {
                    Log.e(TAG, "驱动失败: $text")
                    runOnUiThread {
                        updateStatus("驱动失败")
                    }
                }
                else -> {
                    Log.d(TAG, "其他消息类型: ${rdata.Payload?.Type}, data: $text")
                }
            }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.i(TAG, "WebSocket connected")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            val errorMsg = t.message ?: t.javaClass.simpleName
            Log.e(TAG, "WebSocket error: $errorMsg, type: ${t.javaClass.simpleName}, response: ${response?.code}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.i(TAG, "WebSocket closed")
        }
    }

    private fun initImmersionBar() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }
}
