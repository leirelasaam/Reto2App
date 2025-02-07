package com.elorrieta.alumnoclient
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.elorrieta.alumnoclient.entity.Course
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Date


@Suppress("DEPRECATION")
class CourseActivity : BaseActivity(), OnMapReadyCallback {


    private lateinit var mapView: MapView
    private lateinit var course: Course

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_course, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)


        // Recibir el objeto Course que es pasado  en el Intent desde el adapter
        course = intent.getSerializableExtra("course") as? Course
            ?: throw IllegalArgumentException("Course no encontrado en el Intent")

        findViewById<TextView>(R.id.course_name).text = course.name
        findViewById<TextView>(R.id.course_date).text = formatDate(course.date)
        findViewById<TextView>(R.id.course_contact).text = course.contact
        findViewById<TextView>(R.id.course_description).text = course.description
        findViewById<TextView>(R.id.course_schedule).text = course.schedule

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        findViewById<Button>(R.id.buttonReturn)
            .setOnClickListener {
                startActivity(Intent(this, CourseListActivity::class.java))
                finish()
            }
    }


    @SuppressLint("SimpleDateFormat")
    fun formatDate(date: Date?): String {
        if (date == null) {
            return "Fecha no disponible"
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(date)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val latLng = course.latitude?.let { lat ->
            course.longitude?.let { lon ->
                LatLng(lat.toDouble(), lon.toDouble())
            }
        }
        latLng?.let {
            googleMap.addMarker(MarkerOptions().position(it).title(course.name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
        } ?: run {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }


    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


}