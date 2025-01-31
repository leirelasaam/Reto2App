package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.socketIO.DocumentsSocket
import com.elorrieta.alumnoclient.socketIO.HomeTeacherSocket

class DocumentsActivity : BaseActivity() {
    private var socketClient: DocumentsSocket? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_documents, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)
        enableEdgeToEdge()

        socketClient = DocumentsSocket(this)
        socketClient!!.doGetDocumentList()
    }
}