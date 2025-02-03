package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.Document
import com.elorrieta.alumnoclient.entity.TeacherSchedule
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.alumnoclient.socketIO.config.Events
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DocumentsSocket(private val activity: Activity) {
    private var tag = "socket.io"
    private var key = PrivateKeyManager.getKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_STUDENT_DOCUMENTS_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                val mListView = activity.findViewById<ListView>(R.id.listDoc)
                if (mi.code == 200) {
                    val documentsJson = JSONObject(mi.message as String)
                    val documentsArray = documentsJson.getJSONArray("documents")
                    val documents = mutableListOf<Document>()

                    for (i in 0 until documentsArray.length()) {
                        val document = JSONUtil.fromJson<Document>(
                            documentsArray.getJSONObject(i).toString()
                        )
                        documents.add(document)
                    }

                    activity.runOnUiThread {
                        val formattedDocuments = documents.map { document ->
                            "${document.name} (${document.module?.code})"
                        }

                        val arrayAdapter = ArrayAdapter<String>(
                            activity,
                            android.R.layout.simple_list_item_1,
                            formattedDocuments
                        )
                        mListView.adapter = arrayAdapter

                        mListView.setOnItemClickListener { _, _, position, _ ->
                            val selectedDocument = documents[position]
                            downloadFile(selectedDocument)
                        }
                    }
                } else {
                    val arrayAdapter = ArrayAdapter<String>(
                        activity,
                        android.R.layout.simple_list_item_1,
                        listOf("No hay documentos.")
                    )
                    mListView.adapter = arrayAdapter
                }
            }
        }
    }

    // Custom events
    fun doGetDocumentList() {
        val message = MessageOutput(LoggedUser.user?.id.toString())
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_STUDENT_DOCUMENTS.value, encryptedMsg)

        Log.d(tag, "Attempt of get documents - $message")
    }

    private fun downloadFile(document: Document) {
        val fileData = document.file
        if (fileData == null) {
            Toast.makeText(activity, "File data is missing", Toast.LENGTH_SHORT).show()
        } else {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            try {
                val file = File(path, document.name + ".pdf")
                val os = FileOutputStream(file)

                os.write(fileData)
                os.close()

                Toast.makeText(activity, "File saved to Downloads", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(activity, "Error saving file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}