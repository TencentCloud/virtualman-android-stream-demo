package com.tencent.virtualman_demo_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.Jzvd
import com.google.gson.Gson
import com.tencent.aai.AAIClient
import com.tencent.aai.audio.data.AudioRecordDataSource
import com.tencent.aai.auth.LocalCredentialProvider
import com.tencent.aai.exception.ClientException
import com.tencent.aai.exception.ServerException
import com.tencent.aai.listener.AudioRecognizeResultListener
import com.tencent.aai.listener.AudioRecognizeStateListener
import com.tencent.aai.model.AudioRecognizeConfiguration
import com.tencent.aai.model.AudioRecognizeRequest
import com.tencent.aai.model.AudioRecognizeResult
import com.tencent.virtualman.Virtualman
import com.tencent.virtualman.VirtualmanParams
import com.tencent.virtualman.data.ResponseData
import com.tencent.virtualman.net.WsListener
import com.tencent.virtualman_demo_app.bean.RoomBean
import com.tencent.virtualman_demo_app.data.AsrResponseData
import com.tencent.virtualman_demo_app.ui.adapter.RoomAdapter
import com.tencent.virtualman_demo_app.view.LineWaveVoiceView
import com.tencent.virtualman_demo_app.view.PagerSnapScrollView
import com.tencent.virtualman_demo_app.view.dialog.PreviewImageDialog
import com.tencent.virtualman_demo_app.view.dialog.PreviewPopupDialog
import com.tencent.virtualman_demo_app.view.dialog.PreviewVideoDialog
import com.tencent.virtualman_demo_app.view.dialog.SingleChoiceDialog
import okhttp3.Response
import okhttp3.WebSocket
import org.json.JSONObject
import java.util.ArrayList
import java.util.Locale

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "demoMain"
    private lateinit var mVirtualman: Virtualman
    private var mVirtualmanParams = VirtualmanParams()
    private var aaiClient: AAIClient? = null

    private var isUninterrupt = false
    private var uninterruptId = ""
    private var speakStatus = 0
    private var isPauseRecord = false

    private lateinit var start_talk_btn: LinearLayout
    private lateinit var start_talk_btn_mask: LinearLayout
    private lateinit var tv_audio_record_content: TextView
    private lateinit var wv_audio_record: LineWaveVoiceView
    private lateinit var cl_room_audio: ConstraintLayout
    private lateinit var rvRoom: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var pssv_room_ask: PagerSnapScrollView
    private lateinit var tv_room_item_text: TextView

    private var singleChoiceDialog: SingleChoiceDialog?= null
    private var previewImageDialog: PreviewImageDialog?= null
    private var previewVideoDialog: PreviewVideoDialog?= null
    private var previewPopupDialog: PreviewPopupDialog?= null

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initImmersionBar()

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork

        if (currentNetwork == null) {
            val dialog = NetworkErrorDialog()
            val fragmentManager = supportFragmentManager
            dialog.show(supportFragmentManager, "NoticeDialogFragment")
        } else {
            if (allPermissionsGranted()) {
                init()
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network : Network) {
                val dialog = NetworkErrorDialog()
                val fragmentManager = supportFragmentManager
                dialog.show(supportFragmentManager, "NoticeDialogFragment")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        // 退出时调用数智人关流
        mVirtualman.close()
        Jzvd.releaseAllVideos()
        //取消语音识别
        releaseAudioRecognize()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                init()
            } else {
                showToast("Permissions not granted by the user.")
                finish()
            }
        }
    }

    private fun showToast(msg: String, context: Context = this, type: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, msg, type).show()
    }

    class NetworkErrorDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage("网络不可用，请连接网络后重试")
                    .setPositiveButton("退出",
                        DialogInterface.OnClickListener { dialog, id ->
                            activity?.finishAffinity();
                        })
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        // 初始化ui相关
        start_talk_btn = findViewById(R.id.start_talk_btn)
        start_talk_btn_mask = findViewById(R.id.start_talk_btn_mask)
        tv_audio_record_content = findViewById(R.id.tv_audio_record_content)
        wv_audio_record = findViewById(R.id.wv_audio_record)
        cl_room_audio = findViewById(R.id.cl_room_audio)
        rvRoom = findViewById(R.id.rv_room)
        pssv_room_ask = findViewById(R.id.pssv_room_ask)
        tv_room_item_text = findViewById(R.id.tv_room_item_text)

        val layoutManager: LinearLayoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        rvRoom.layoutManager = layoutManager
        roomAdapter = RoomAdapter()
        rvRoom.adapter = roomAdapter

        // 气泡点击事件
        roomAdapter.setOnItemChildClickListener { _, view, position ->
            val roomBean = roomAdapter.data[position]
            if (roomBean.type == 2) {
                // 点击图片类气泡
                isPauseRecord = true
                stopAudioRecognize()
                previewImageDialog = PreviewImageDialog(roomBean.url)
                previewImageDialog?.show(supportFragmentManager, PreviewImageDialog::class.java.simpleName)
                previewImageDialog!!.setOnDismissListener {
                    isPauseRecord = false
                    startAudioRecognize()
                }
            } else if (roomBean.type == 3) {
                // 点击视频类气泡
                isPauseRecord = true
                stopAudioRecognize()
                previewVideoDialog = PreviewVideoDialog(roomBean.url)
                previewVideoDialog?.show(supportFragmentManager, PreviewVideoDialog::class.java.simpleName)
                previewVideoDialog!!.setOnDismissListener {
                    isPauseRecord = false
                    startAudioRecognize()
                }
            } else {
                // 点击文本类气泡
                pssv_room_ask.visibility = View.GONE
                rvRoom.visibility = View.INVISIBLE
                stopAudioRecognize()
            }
        }

        // 初始化数智人
        mVirtualman = findViewById(R.id.virtualman)
        mVirtualmanParams.appkey = Config.APP_KEY
        mVirtualmanParams.accesstoken = Config.ACCESS_TOKEN
        mVirtualmanParams.virtualmanProjectId = Config.VIRTUALMAN_PROJECT_ID
        mVirtualman.init(mVirtualmanParams, { result ->
            Log.i(TAG, "---------------------------------------------------")
            Log.i(TAG, result.toString())
            if (result.toString().startsWith("success")) {
                // 显示开始对话按钮
                start_talk_btn.postDelayed({
                    start_talk_btn.visibility = View.VISIBLE
                    start_talk_btn_mask.visibility = View.VISIBLE
                }, 5000)
            }
        }, StreamWebSocketListener())

        // 开始对话按钮事件
        start_talk_btn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.start_talk_btn -> {
                if (aaiClient == null){
                    initAudioRecognize()
                }else {
                    startAudioRecognize()
                }
            }
        }
    }

    // 初始化ASR client对象
    private fun initAudioRecognize() {
        Thread {
            aaiClient = AAIClient(
                this,
                Config.ASR_APP_ID,
                0,
                Config.ASR_SECRET_ID,
                Config.ASR_SECRET_KEY,
                LocalCredentialProvider(Config.ASR_SECRET_KEY)
            )
            startAudioRecognize()
        }.start()
    }

    // 开始ASR语音识别
    private fun startAudioRecognize(){
        if (aaiClient != null){
            Thread {
                aaiClient?.startAudioRecognize(
                    audioRecognizeRequest,
                    audioRecognizeResultlistener,
                    audioRecognizeStateListener,
                    audioRecognizeConfiguration
                )
            }.start()
            speakStatus = 1
        }
    }

    // 停止ASR语音识别
    private fun stopAudioRecognize(){
        if (aaiClient != null){
            Thread {
                //停止语音识别，等待最终识别结果
                aaiClient?.stopAudioRecognize()
            }.start()
            speakStatus = 0
        }
    }

    // 取消ASR语音识别
    private fun releaseAudioRecognize(){
        if (aaiClient != null){
            Thread {
                //取消语音识别，丢弃当前任务，丢弃最终结果
                aaiClient?.cancelAudioRecognize()
                aaiClient?.release()
                aaiClient = null
            }.start()
            speakStatus = 0
        }
    }

    // 初始化ASR语音识别请求
    private val audioRecognizeRequest: AudioRecognizeRequest =
        AudioRecognizeRequest.Builder() //设置数据源，数据源要求实现PcmAudioDataSource接口，您可以自己实现此接口来定制您的自定义数据源，例如从第三方推流中获
            .pcmAudioDataSource(AudioRecordDataSource(false)) // 使用SDK内置录音器作为数据源,false:不保存音频?
            .setEngineModelType("16k_zh") // 设置引擎参数("16k_zh" 通用引擎，支持中文普通话+英文)
            .setFilterDirty(0) // 0 ：默认状态 不过滤脏话 1：过滤脏话
            .setFilterModal(0) // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
            .setFilterPunc(0) // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
            .setConvert_num_mode(1) //1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
            .setNeedvad(1) //0：关闭 vad，1：默认状态 开启 vad。语音时长超过一分钟需要开启,如果对实时性要求较高,并且时间较短的输入,建议关闭
            .setReinforceHotword(1)
            .build()


    // 初始化ASR语音识别结果监听器
    private val audioRecognizeResultlistener: AudioRecognizeResultListener =
        object : AudioRecognizeResultListener {
            // ASR返回分片的识别结果，此为中间态结果，会被持续修正
            override fun onSliceSuccess(
                request: AudioRecognizeRequest,
                result: AudioRecognizeResult,
                seq: Int
            ) {
                if (isDestroyed) return
                Log.i(TAG, "onSliceSuccess seq=$seq result=${result.text} startTime=${result.startTime} endTime=${result.endTime}")
                if (result.text.isNotEmpty()) {

                    if (!isUninterrupt) {
                        runOnUiThread {
                            if (cl_room_audio.visibility != View.VISIBLE) {
                                cl_room_audio.visibility = View.VISIBLE
                            }
                            tv_audio_record_content.setText(result.text)
                            rvRoom.visibility = View.INVISIBLE
                            pssv_room_ask.visibility = View.GONE

                            previewImageDialog?.dismiss()
                            previewVideoDialog?.dismiss()
                            singleChoiceDialog?.dismiss()
                            if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.state == Jzvd.STATE_PLAYING) {
                                Jzvd.goOnPlayOnPause()
                            }
                        }
                    }
                }
            }

            // ASR返回语音流的识别结果，此为稳定态结果，可做为识别结果用与业务
            override fun onSegmentSuccess(
                request: AudioRecognizeRequest,
                result: AudioRecognizeResult,
                seq: Int
            ) {
                Log.i(TAG, "onSegmentSuccess seq=$seq result=${result.text} startTime=${result.startTime} endTime=${result.endTime}")

                if (result.text.isNotEmpty()) {
                    if (!isUninterrupt) {
                        val isSuccess = mVirtualman.sendText(result.text)

                        tv_audio_record_content.post {
                            tv_audio_record_content.text = ""
                        }

                        if (isSuccess == true) {
                            runOnUiThread {
                                roomAdapter.setNewData(ArrayList<RoomBean>())
                                tv_room_item_text.text = result.text
                                pssv_room_ask.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                runOnUiThread {
                    cl_room_audio.visibility = View.GONE
                }
            }

            //识别结束回调，返回所有的识别结果
            override fun onSuccess(request: AudioRecognizeRequest, result: String) {
                Log.i(TAG, "onSuccess result=${result} vad_silence_time=${request.vad_silence_time}")
            }

            // 识别失败
            override fun onFailure(
                request: AudioRecognizeRequest?,
                clientException: ClientException?,
                serverException: ServerException?,
                response: String?
            ) {
                Log.e(TAG, "ASR error onFailure $response")
                Log.e(TAG, "ASR error clientException ${clientException.toString()}")
                Log.e(TAG, "ASR error serverException ${serverException.toString()}")
                val rdata = Gson().fromJson(response, AsrResponseData::class.java)
                val message = rdata?.message
                if (message != null && message.isNotEmpty()) {
                    Thread.sleep(500)
                    singleChoiceDialog?.dismiss()
                    previewImageDialog?.dismiss()
                    previewVideoDialog?.dismiss()
                    previewPopupDialog?.dismiss()
                    previewPopupDialog = PreviewPopupDialog("ASR错误","$message", "确认")
                    previewPopupDialog?.show(supportFragmentManager, PreviewImageDialog::class.java.simpleName)
                }
            }
        }

    // 初始化ASR语音识别状态监听器
    private val audioRecognizeStateListener = object : AudioRecognizeStateListener {
        override fun onStartRecord(request: AudioRecognizeRequest?) {
            // 开始录音
            Log.i(TAG, "onStartRecord")
            speakStatus = 1
            runOnUiThread {
                tv_audio_record_content.text = ""
                start_talk_btn.visibility = View.GONE
                start_talk_btn_mask.visibility = View.GONE
            }
        }

        override fun onStopRecord(request: AudioRecognizeRequest?) {
            // 结束录音
            Log.e(TAG, "onStopRecord")
            speakStatus = 0
            if (!isPauseRecord){
                start_talk_btn.post {
                    start_talk_btn.visibility = View.VISIBLE
                    start_talk_btn_mask.visibility = View.VISIBLE
                }
            }
        }

        private var volumeSize: Int = 0
        private var volumeResultSize: Int = 0
        private var volumeMaxResultSize: Int = 0
        override fun onVoiceVolume(request: AudioRecognizeRequest?, volume: Int) {
            // 音量回调
            wv_audio_record.post {
                wv_audio_record.addWaveList(volume)
            }
            if (volume == 0) {
                volumeSize++
                volumeMaxResultSize = 0
                if (volumeSize > 15){
                    runOnUiThread {
                        cl_room_audio.visibility = View.GONE
                    }
                }
            } else {
                runOnUiThread {
                    if (volume < 5) {
                        volumeResultSize++
                        volumeMaxResultSize = 0
                        if (volumeResultSize > 35){
                            volumeResultSize = 0
                            cl_room_audio.visibility = View.GONE
                        }
                    }else {
                        volumeResultSize = 0

                        if (volume > 15){
                            volumeMaxResultSize++
                            if (!isUninterrupt && volumeMaxResultSize > 5) {
                                if (cl_room_audio.visibility != View.VISIBLE) {
                                    cl_room_audio.visibility = View.VISIBLE
                                }
                            }
                        }else {
                            volumeMaxResultSize = 0
                        }
                    }
                }
                volumeSize = 0
            }
        }

        /**
         * 返回音频流，
         * 用于返回宿主层做录音缓存业务。
         * 由于方法跑在sdk线程上，这里多用于文件操作，宿主需要新开一条线程专门用于实现业务逻辑
         * new AudioRecordDataSource(true) 有效，否则不会回调该函数
         * @param audioDatas
         */
        override fun onNextAudioData(audioDatas: ShortArray?, readBufferLength: Int) {
            Log.i(TAG, "onNextAudioData $readBufferLength")
        }

        /**
         * 静音检测超时回调
         * 注意：此时任务还未中止，仍然会等待最终识别结果
         */
        override fun onSilentDetectTimeOut() {
            //触发了静音检测事件
            Log.i(TAG, "onSilentDetectTimeOut")
        }
    }

    // 自定义ASR识别配置
    private val audioRecognizeConfiguration =
        AudioRecognizeConfiguration.Builder()
            .setSilentDetectTimeOut(false) // 静音检测超时停止录音可设置>2000ms，setSilentDetectTimeOut为true有效，超过指定时间没有说话将关闭识别；需要大于等于sliceTime，实际时间为sliceTime的倍数，如果小于sliceTime，则按sliceTime的时间为准
            .setSilentDetectTimeOutAutoStop(true)
            .audioFlowSilenceTimeOut(2000) // 音量回调时间，需要大于等于sliceTime，实际时间为sliceTime的倍数，如果小于sliceTime，则按sliceTime的时间为准
            .minVolumeCallbackTime(80)
            .build()

    // 数智人socket监听事件
    internal inner class StreamWebSocketListener: WsListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.i(TAG, "-----websocket-onMessage")
            Log.i(TAG, text)

            val rdata = Gson().fromJson(text, ResponseData::class.java)
            // 打断状态逻辑处理
            if (rdata.Payload?.Type == 2) {
                isUninterrupt = rdata.Payload?.Uninterrupt == true

                if (isUninterrupt) {
                    uninterruptId = rdata.Payload?.ReqId!!
                }
                if (isUninterrupt && cl_room_audio.visibility == View.VISIBLE){
                    runOnUiThread {
                        cl_room_audio.visibility = View.GONE
                    }
                }
            } else if (rdata.Payload?.Type == 3) {
                if ((rdata.Payload?.SpeakStatus == "TextOver" || rdata.Payload?.SpeakStatus == "Error")&& rdata.Payload?.ReqId == uninterruptId) {
                    isUninterrupt = false
                }
            }

            // 内容消息处理
            val interactionContent = rdata.Payload?.InteractionContent
            if (interactionContent != null && interactionContent.isNotEmpty()){
                val jsonObject = JSONObject(interactionContent)
                val interactionType = rdata?.Payload?.InteractionType?.lowercase(
                    Locale.ROOT
                )
                when (interactionType){
                    "video" -> {
                        // 视频
                        val videoUrl = jsonObject.getString("url")
                        if (videoUrl.isNotEmpty()){
                            runOnUiThread {
                                val roomBean = RoomBean(3, rdata.Payload?.Text, videoUrl)
                                val list = ArrayList<RoomBean>()
                                list.add(roomBean)
                                roomAdapter.setNewData(list)
                                rvRoom.visibility = View.VISIBLE
                            }
                        }
                    }
                    "image" -> {
                        // 图片
                        val imageUrl = jsonObject.getString("url")
                        if (imageUrl.isNotEmpty()){
                            runOnUiThread {
                                val roomBean = RoomBean(2, rdata.Payload?.Text, imageUrl)
                                val list = ArrayList<RoomBean>()
                                list.add(roomBean)
                                roomAdapter.setNewData(list)
                                rvRoom.visibility = View.VISIBLE
                            }
                        }
                    }
                    "optioninfo" -> {
                        // 选择题
                        val areas = jsonObject.getJSONArray("options")
                        val style = jsonObject.getString("style")

                        val options = ArrayList<String>()
                        for (index in 0 until areas.length()) {
                            if (areas.getString(index) != null) {
                                options.add(areas.getString(index))
                            }
                        }
                        if (options.size > 0) {
                            playSelectDialog(options, rdata.Payload?.Text, style)
                        }else {
                            runOnUiThread {
                                val roomBean = RoomBean(1, "选择题没有选项/n" + rdata.Payload?.Text + "/n" + areas, "")
                                val list = ArrayList<RoomBean>()
                                list.add(roomBean)
                                roomAdapter.setNewData(list)
                                rvRoom.visibility = View.VISIBLE
                            }
                        }
                    }
                    "imageoption" -> {
                        // 图片和选择题
                        val imageUrl = jsonObject.getString("url")
                        val areas = jsonObject.getJSONArray("options")
                        val style = jsonObject.getString("style")
                        val options = ArrayList<String>()
                        for (index in 0 until areas.length()) {
                            if (areas.getString(index) != null) {
                                options.add(areas.getString(index))
                            }
                        }
                        if (options.size > 0) {
                            playSelectDialog(options, rdata.Payload?.Text, style, imageUrl)
                        }
                    }
                    "popup" -> {
                        // 弹窗
                        val title = jsonObject.getString("title")
                        val content = jsonObject.getString("content")
                        val button = jsonObject.getString("button")
                        isPauseRecord = true
                        stopAudioRecognize()
                        previewPopupDialog = PreviewPopupDialog(title, content, button)
                        previewPopupDialog?.show(supportFragmentManager, PreviewImageDialog::class.java.simpleName)
                        previewPopupDialog!!.setOnDismissListener {
                            isPauseRecord = false
                            startAudioRecognize()
                        }
                    }
                }
            } else {
                // 文本消息
                if (rdata.Payload?.Type == 2) {
                    var messageText = rdata.Payload?.Text
                    if (messageText != null && messageText.isNotEmpty()) {
                        runOnUiThread {
                            val roomBean = RoomBean(1, messageText, "")
                            val list = ArrayList<RoomBean>()
                            list.add(roomBean)
                            roomAdapter.setNewData(list)
                            rvRoom.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.i(TAG, "-----websocket-onOpen")
            Log.i(TAG, response.toString())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.i(TAG, "-----websocket-onClosing")
            Log.i(TAG, code.toString())
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.i(TAG, "-----websocket-onClosed")
            Log.i(TAG, code.toString())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.i(TAG, "-----websocket-onFailure")
            Log.i(TAG, t.toString())
        }
    }

    // 打开选择题弹窗或图片选择题
    private fun playSelectDialog(areas: List<String?>, content: String?, style: String? = null, imageUrl: String? = null){
        runOnUiThread {
            singleChoiceDialog = SingleChoiceDialog(this, areas, content, style, imageUrl)
            singleChoiceDialog?.onClickSingleChoiceListener = object : SingleChoiceDialog.OnClickSingleChoiceListener{
                override fun confirmSingle(choice: String?) {
                    choice?.let { mVirtualman.sendText(it) }
                }
            }
            singleChoiceDialog?.show()
            rvRoom.visibility = View.INVISIBLE
            pssv_room_ask.visibility = View.GONE
        }
    }

    private fun initImmersionBar(){
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN  // 隐藏状态栏
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  // 隐藏导航栏

                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  // 允许视图内容延伸到状态栏区域
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // 允许视图延伸到导航栏区域
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.READ_PHONE_STATE
            ).toTypedArray()
    }
}
