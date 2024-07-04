package com.tencent.virtualman_demo_app.utils

import android.media.MediaMetadataRetriever
import android.util.Log

object VideoUtils {

    fun getPlayInfo(mUri: String?): Array<String?> {
        val mmr = MediaMetadataRetriever()
        val videoWH = arrayOfNulls<String>(2)
        try {
            if (mUri != null) {
                var headers: HashMap<String?, String?>? = null
                if (headers == null) {
                    headers = HashMap()
                    headers["User-Agent"] =
                        "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1"
                }
                mmr.setDataSource(mUri, headers)
            }
//            val duration =
//                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) //时长(毫秒)
            val width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) //宽
            val height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) //高

            videoWH[0] = width
            videoWH[1] = height
        } catch (ex: Exception) {
            Log.e("VideoUtils", "MediaMetadataRetriever exception $ex")
        } finally {
            mmr.release()
        }
        return videoWH
    }
}