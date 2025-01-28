package com.elorrieta.alumnoclient

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.entity.Course

class CourseListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_course_list, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCursos)

        val courses = listOf(
            Course()
        )

        val adapter = CourseAdapter(courses) { course ->
            val intent = Intent(this, CourseActivity::class.java).apply {
                putExtra("name", course.name)
                putExtra("date", course.date)
                putExtra("schedule", course.schedule)
                putExtra("contact", course.contact)
                putExtra("description", course.description)
                putExtra("latitude", course.latitude)
                putExtra("longitude", course.longitude)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}