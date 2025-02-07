package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.adapters.DocumentAdapter
import com.elorrieta.alumnoclient.adapters.MeetingBoxAdapter
import com.elorrieta.alumnoclient.entity.Document
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.socketIO.DocumentsSocket
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DocumentsActivity : BaseActivity() {
    private var socketClient: DocumentsSocket? = null
    private lateinit var recycler: RecyclerView
    @SuppressLint("MissingInflatedId", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_documents, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)
        enableEdgeToEdge()

        recycler = findViewById(R.id.recyclerDocs)
        socketClient = DocumentsSocket(this)
        socketClient!!.doGetDocumentList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadAdapter(documents: MutableList<Document>){
        val adapter = DocumentAdapter(documents, this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    fun showEmpty(){
        recycler.visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
    }

    fun downloadFile(document: Document) {
        val fileData = document.file
        if (fileData == null) {
            Toast.makeText(this, getString(R.string.doc_file_error), Toast.LENGTH_SHORT).show()
        } else {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            try {
                val file = File(path, document.name + ".pdf")
                val os = FileOutputStream(file)

                os.write(fileData)
                os.close()

                Toast.makeText(this, getString(R.string.doc_file_saved), Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.doc_file_saved_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}