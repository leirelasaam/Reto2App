package com.elorrieta.alumnoclient

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.room.model.Course

class CourseListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_course_list, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCursos)

        val courses = listOf(
            Course("DAM", "2025-01-01 - 2025-06-01", "9:00 - 13:00", "info@elorrieta.com", "Descripción DAM", 43.28394, -2.96481),
            Course("DAW", "2025-01-01 - 2025-06-01", "9:00 - 13:00", "info@elorrieta.com", "Descripción DAW", 43.28394, -2.96481),
            Course("ADE", "2025-01-01 - 2025-06-01", "9:00 - 13:00", "info@errekamari.com", "Descripción ADE", 43.27155, -2.94476),
            Course("JDJAJD", "2025-01-01 - 2025-06-01", "9:00 - 13:00", "info@errekamari.com", "Descripción JDJAJD", 43.27155, -2.94476)
        )

        val adapter = CourseAdapter(courses) { course ->
            val intent = Intent(this, CourseActivity::class.java).apply {
                putExtra("name", course.name)
                putExtra("latitude", course.latitude)
                putExtra("longitude", course.longitude)
                putExtra("date", course.date)
                putExtra("schedule", course.schedule)
                putExtra("contact", course.contact)
                putExtra("description", course.description)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}