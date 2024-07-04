package com.tencent.virtualman_demo_app.view.dialog

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import com.tencent.virtualman_demo_app.R
import com.tencent.virtualman_demo_app.view.LineWaveVoiceView

class AudioRecordDialog(context: Context) : BaseDialog(context) {

    private var wvAudioRecord: LineWaveVoiceView? = null
    private var tvAudioRecordContent: TextView? = null
    var onClickBackListener: OnClickBackListener? = null

    override fun onStart() {
        super.onStart()

        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    override fun getLayoutId(): Int {
        return R.layout.dialog_audio_record
    }

    override fun initView() {
        super.initView()
        wvAudioRecord = findViewById(R.id.wv_audio_record)
        tvAudioRecordContent = findViewById(R.id.tv_audio_record_content)
    }

    fun setTvAudioRecordContent(content: String?) {
        if (tvAudioRecordContent != null) {
            tvAudioRecordContent!!.text = content
        }
    }

    fun addData(volume: Int){
        if (wvAudioRecord != null){
            wvAudioRecord!!.addWaveList(volume)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (onClickBackListener != null){
                onClickBackListener?.onBack()
            }
        }
        return false
    }

    interface OnClickBackListener{
        fun onBack();
    }

    private var openTime: Long = 0

    override fun show() {
        super.show()

        setTvAudioRecordContent("")
        openTime = System.currentTimeMillis()
        cancelTimer()
    }

    private var timer: java.util.Timer?= null
    override fun dismiss() {
        if (!isShowing) return
        if (openTime > System.currentTimeMillis() - 3000){
            cancelTimer()
            timer = java.util.Timer()
            timer?.schedule(object: java.util.TimerTask() {
                override fun run() {
                    Log.e("DIALOG_DISMISS", "超时关闭")
                    openTime = 0
                    this@AudioRecordDialog.dismiss()
                }
            },1000)
        }else {
            Log.e("DIALOG_DISMISS", "正常关闭")
            super.dismiss()
        }
    }

    private fun cancelTimer(){
        if (timer != null){
            timer!!.cancel()
            timer = null
        }
    }
}