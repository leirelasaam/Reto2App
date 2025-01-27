package com.elorrieta.alumnoclient

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CourseActivity : BaseActivity() , OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var txtCourse: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtSchedule: TextView
    private lateinit var txtContact: TextView
    private lateinit var txtDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_course, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        // Inicializa las vistas de texto
        txtCourse = findViewById(R.id.txtCourse)
        txtDate = findViewById(R.id.txtDate)
        txtSchedule = findViewById(R.id.txtSchedule)
        txtContact = findViewById(R.id.txtContact)
        txtDescription = findViewById(R.id.txtDescription)

        // Obtén los datos del Intent
        val name = intent.getStringExtra("name")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val date = intent.getStringExtra("date")
        val schedule = intent.getStringExtra("schedule")
        val contact = intent.getStringExtra("contact")
        val description = intent.getStringExtra("description")

        // Asigna los datos a las vistas
        txtCourse.text = name
        txtDate.text = date
        txtSchedule.text = schedule
        txtContact.text = contact
        txtDescription.text = description

        // Configura el mapa dinámicamente
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapContainer) as SupportMapFragment?

        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, newMapFragment)
                .commit()
            newMapFragment.getMapAsync(this)
        } else {
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val location = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
        map.addMarker(MarkerOptions().position(location).title("Course: ${intent.getStringExtra("name")}"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}