package com.elorrieta.alumnoclient.singletons

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket

object SocketConnectionManager {
    private var socket: Socket? = null
    private var tag = "socket.io"

    fun getSocket(): Socket {
        if (socket == null || !socket!!.connected()) {
            val ipPort = "http://10.5.104.25:3000"
            //val ipPort = "http://192.168.1.142:3000"
            //val ipPort = "http://10.5.104.52:3000"

            socket = IO.socket(ipPort).apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d(tag, "Connected")
                }
                on(Socket.EVENT_DISCONNECT) {
                    Log.d(tag, "Disconnected")
                }
            }
            socket?.connect()
        }
        return socket!!
    }

    fun connect() {
        if (!socket!!.connected()) {
            socket?.connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun isConnected(): Boolean {
        return socket!!.connected()
    }
}
