package com.tencent.virtualman_demo_app.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        initImmersionBar()

        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        initView()
        initListener()
        initData()
    }

    protected abstract fun getLayoutId(): Int

    open fun initView(){}
    open fun initListener(){}
    open fun initData(){}

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
}