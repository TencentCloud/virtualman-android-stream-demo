package com.tencent.virtualman_demo_app.view.shadow

import android.graphics.drawable.Drawable
import android.view.View.OnLayoutChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import androidx.annotation.RequiresApi
import android.os.Build
import android.view.View
import com.bumptech.glide.request.transition.Transition
import com.tencent.virtualman_demo_app.view.shadow.GlideRoundTransform

object GlideRoundUtils {

    fun setRoundCorner(view: View, resourceId: Drawable?, cornerDipValue: Float) {
        if (cornerDipValue == 0f) {
            if (view.measuredWidth == 0 && view.measuredHeight == 0) {
                view.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
                    Glide.with(view)
                        .asDrawable()
                        .load(resourceId)
                        .transform(CenterCrop())
                        .override(view.measuredWidth, view.measuredHeight)
                        .into(object : CustomTarget<Drawable?>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable?>?
                            ) {
                                view.background = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            } else {
                Glide.with(view)
                    .asDrawable()
                    .load(resourceId)
                    .transform(CenterCrop())
                    .override(view.measuredWidth, view.measuredHeight)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            view.background = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        } else {
            if (view.measuredWidth == 0 && view.measuredHeight == 0) {
                view.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
                    Glide.with(view)
                        .load(resourceId)
                        .transform(CenterCrop(), RoundedCorners(cornerDipValue.toInt()))
                        .override(view.measuredWidth, view.measuredHeight)
                        .into(object : CustomTarget<Drawable?>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable?>?
                            ) {
                                view.background = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            } else {
                Glide.with(view)
                    .load(resourceId)
                    .transform(CenterCrop(), RoundedCorners(cornerDipValue.toInt()))
                    .override(view.measuredWidth, view.measuredHeight)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            view.background = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }

    fun setCorners(
        view: View,
        resourceId: Drawable?,
        leftTop_corner: Float,
        leftBottom_corner: Float,
        rightTop_corner: Float,
        rightBottom_corner: Float
    ) {
        if (leftTop_corner == 0f && leftBottom_corner == 0f && rightTop_corner == 0f && rightBottom_corner == 0f) {
            if (view.measuredWidth == 0 && view.measuredHeight == 0) {
                view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    Glide.with(view)
                        .load(resourceId)
                        .override(view.measuredWidth, view.measuredHeight)
                        .into(object : CustomTarget<Drawable?>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable?>?
                            ) {
                                view.background = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            } else {
                Glide.with(view)
                    .load(resourceId)
                    .override(view.measuredWidth, view.measuredHeight)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            view.background = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        } else {
            if (view.measuredWidth == 0 && view.measuredHeight == 0) {
                view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    val transform = GlideRoundTransform(
                        view.context,
                        leftTop_corner,
                        leftBottom_corner,
                        rightTop_corner,
                        rightBottom_corner
                    )
                    Glide.with(view)
                        .load(resourceId)
                        .transform(transform)
                        .override(view.measuredWidth, view.measuredHeight)
                        .into(object : CustomTarget<Drawable?>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable?>?
                            ) {
                                view.background = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            } else {
                val transform = GlideRoundTransform(
                    view.context,
                    leftTop_corner,
                    leftBottom_corner,
                    rightTop_corner,
                    rightBottom_corner
                )
                Glide.with(view)
                    .load(resourceId)
                    .transform(transform)
                    .override(view.measuredWidth, view.measuredHeight)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            view.background = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }
}