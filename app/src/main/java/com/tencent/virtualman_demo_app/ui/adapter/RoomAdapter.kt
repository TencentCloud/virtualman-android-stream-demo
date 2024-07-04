package com.tencent.virtualman_demo_app.ui.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tencent.virtualman_demo_app.R
import com.tencent.virtualman_demo_app.bean.RoomBean
import com.tencent.virtualman_demo_app.utils.ArmUtils
import com.tencent.virtualman_demo_app.utils.ArmUtils.getScreenHeight
import com.tencent.virtualman_demo_app.utils.ArmUtils.getScreenWidth
import com.tencent.virtualman_demo_app.utils.GlideLoader
import com.tencent.virtualman_demo_app.utils.VideoUtils
import com.tencent.virtualman_demo_app.view.video.JzvdStdCustom

class RoomAdapter : BaseMultiItemQuickAdapter<RoomBean, BaseViewHolder>(ArrayList()) {

    init {
        addItemType(0, R.layout.item_room_voice_tran_text)
        addItemType(1, R.layout.item_room_mind_text)
        addItemType(2, R.layout.item_room_mind_image)
        addItemType(3, R.layout.item_room_mind_video)
    }

    override fun convert(helper: BaseViewHolder, item: RoomBean) {
        val tvRoomItemText = helper.getView<TextView>(R.id.tv_room_item_text)
        val ivRoomItemImage = helper.getView<ImageView>(R.id.iv_room_item_image)
        val jscRoomItemVideo = helper.getView<JzvdStdCustom>(R.id.jsc_room_item_video)

        if (item.content != null && item.content.isNotEmpty()){
            tvRoomItemText.visibility = View.VISIBLE
            tvRoomItemText.text = item.content
        }else {
            tvRoomItemText.visibility = View.GONE
        }
        when(item.type){
            0 ->{

            }
            1 -> {

            }
            2 -> {
                val wh = ArmUtils.dip2px(mContext, 416f)

                Glide.with(mContext).asBitmap().load(item.url).into(object : SimpleTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                    ) {
                        val width = resource.width
                        val height = resource.height

                        ivRoomItemImage.layoutParams.width = width*wh/height
                        ivRoomItemImage.layoutParams.height = wh

                        ivRoomItemImage.setImageBitmap(resource)
                    }
                })

                helper.addOnClickListener(R.id.iv_room_item_image)
            }
            3 -> {
                val wh = VideoUtils.getPlayInfo(item.url)

                val w = getScreenWidth(mContext) - ArmUtils.dip2px(mContext, 200f)
                val h = getScreenHeight(mContext)/3
                var relW: Int
                var relH: Int
                if (wh[0] != null && wh[1] != null) {
                    val mW = wh[0]!!.toInt()
                    val mH = wh[1]!!.toInt()
                    //判断宽高比
                    if (mW >= mH) {
                        //宽大于高--直接充满宽，高按照比例放大
                        relW = w
                        relH = mW * w / mH
                    } else {
                        if (h.toFloat() / w < mH.toFloat() / mW) {
                            //可以填满高，但没有填满宽
                            relW = mW * h / mH
                            relH = h
                        } else {
                            //填满高的同时并不能完全显示宽--使用满宽
                            relW = w
                            relH = mH * w / mW
                        }
                    }
                } else {
                    relW = w
                    relH = h
                }
                if (relW > w) {
                    relW = w
                }
                if (relH > h) {
                    relH = h
                }
                jscRoomItemVideo?.layoutParams?.height = relH
                jscRoomItemVideo?.layoutParams?.width = relW

                val source = JZDataSource(item.url, "")
                source.looping = false
                jscRoomItemVideo?.setUp(source, Jzvd.SCREEN_NORMAL)
//                jscRoomItemVideo?.startVideo()
                GlideLoader.loaderCenterCrop(mContext, jscRoomItemVideo.posterImageView, item.url)

                helper.addOnClickListener(R.id.v_room_item_video)
            }
        }
        helper.addOnClickListener(R.id.tv_room_item_text)
    }
}