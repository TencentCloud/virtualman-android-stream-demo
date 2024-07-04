package com.tencent.virtualman_demo_app.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object GlideLoader {

    fun loader(context: Context, iv: ImageView?, url: String?) {
        val options = RequestOptions()
        glideImage(context, iv, url, options)
    }

    fun loaderCenterCrop(context: Context, iv: ImageView?, url: String?) {
        val options = RequestOptions().centerCrop()
        glideImage(context, iv, url, options)
    }

    private fun glideImage(context: Context, iv: ImageView?, url: String?, option: RequestOptions) {
        if (iv != null) {
            Glide.with(context).load(url).apply(option).into(iv)
        }
    }
}