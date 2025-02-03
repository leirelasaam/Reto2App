package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.entity.Document
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.socketIO.DocumentsSocket
import com.elorrieta.alumnoclient.socketIO.HomeTeacherSocket
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DocumentsActivity : BaseActivity() {
    private var socketClient: DocumentsSocket? = null
    private lateinit var mListView: ListView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_documents, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)
        enableEdgeToEdge()

        mListView = findViewById<ListView>(R.id.listDoc)
        socketClient = DocumentsSocket(this)
        socketClient!!.doGetDocumentList()
    }

    fun loadAdapter(documents: MutableList<Document>){

        val formattedDocuments = documents.map { document ->
            "${document.name} (${document.module?.code})"
        }

        val arrayAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            formattedDocuments
        )
        mListView.adapter = arrayAdapter

        mListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDocument = documents[position]
            downloadFile(selectedDocument)
        }
    }

    fun showEmpty() {
        val arrayAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            listOf("No hay documentos.")
        )
        mListView.adapter = arrayAdapter
    }

    private fun downloadFile(document: Document) {
        val fileData = document.file
        if (fileData == null) {
            Toast.makeText(this, "File data is missing", Toast.LENGTH_SHORT).show()
        } else {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            try {
                val file = File(path, document.name + ".pdf")
                val os = FileOutputStream(file)

                os.write(fileData)
                os.close()

                Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}