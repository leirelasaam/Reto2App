package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import com.elorrieta.alumnoclient.socketIO.config.SocketConnectionManager
import com.elorrieta.alumnoclient.utils.AESUtil

/**
 * The client
 */
class CourseSocket (private val activity: Activity) {
    private var tag = "socket.io"
    private var key = AESUtil.loadKey(activity)
    private val socket = SocketConnectionManager.getSocket()
}