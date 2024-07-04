package com.tencent.virtualman_demo_app

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MyWsListener: WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        // WebSocket 连接开启
        println("websocket-onOpen")
        println(response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        // 收到服务端发送来的 String 类型消息
        println("websocket-onMessage")
        println(text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        // 收到服务端发来的 CLOSE 帧消息，准备关闭连接
        println("websocket-onClosing")
        println(code)
        println(reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        // WebSocket 连接关闭
        println("websocket-onClosed")
        println(code)
        println(reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        // 出错了
        println("websocket-onFailure")
        println(t)
        println(response)
    }
}