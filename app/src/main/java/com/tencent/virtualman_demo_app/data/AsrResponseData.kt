package com.tencent.virtualman_demo_app.data

import com.google.gson.annotations.SerializedName


data class AsrResponseData (

    @SerializedName("code"  ) var code  : Int? = null,
    @SerializedName("message" ) var message : String? = null,

)