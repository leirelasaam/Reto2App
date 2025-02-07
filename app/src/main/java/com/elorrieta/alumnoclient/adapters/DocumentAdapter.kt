package com.elorrieta.alumnoclient.adapters


import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.DocumentsActivity
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.Document


class DocumentAdapter(private var documents: List<Document>, private var activity: DocumentsActivity) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>()
{
    class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val doc: TextView = view.findViewById(R.id.docTitleTxt)
        val module: TextView = view.findViewById(R.id.moduleTxt)
        val download: ImageView = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDocuments(newDocuments: List<Document>) {
        this.documents = newDocuments
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = documents.size

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val docu = documents[position]
        Log.d("adapter", "$docu")
        holder.doc.text = docu.name
        holder.module.text = docu.module?.name ?: ""

        holder.download.setOnClickListener {
            activity.downloadFile(docu)
        }
    }

}
