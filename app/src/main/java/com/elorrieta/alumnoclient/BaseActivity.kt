package com.elorrieta.alumnoclient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)  // Layout común con DrawerLayout

        // Configura el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Configura la Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configura la navegación
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home_student -> {
                    // Navega a Home
                    startActivity(Intent(this, HomeStudentActivity::class.java))
                }
                R.id.nav_profile_student -> {
                    // Navega a Profile
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_meetings -> {
                    // Navega a Settings
                    startActivity(Intent(this, MeetingsActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Configura el listener para abrir el Drawer cuando se presiona el icono en la Toolbar
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}
