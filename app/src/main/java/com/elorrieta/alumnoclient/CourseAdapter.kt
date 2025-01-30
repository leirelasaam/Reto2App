package com.elorrieta.alumnoclient


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.entity.Course


class CourseAdapter(private val context: Context?, private var courses: List<Course>) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.course_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCourses(newCourses: List<Course>) {
        this.courses = newCourses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = courses.size

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.name.text = course.name

        holder.name.setOnClickListener {
            // Intent pasando el curso completo
            val intent = Intent(context, CourseActivity::class.java)
            intent.putExtra("course", course)
            context?.startActivity(intent)
            Log.d("adapter", "Clickado: $course")
        }
    }

}
