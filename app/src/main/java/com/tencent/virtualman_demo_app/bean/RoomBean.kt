package com.tencent.virtualman_demo_app.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

data class RoomBean(val type: Int, val content: String?, val url: String) : MultiItemEntity, Serializable {

    override fun getItemType(): Int {
        return type
    }
}