package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager.disconnect
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //Esto es para quitar el nombre de la aplicación del toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val menu = navigationView.menu

        // Añadir el nombre del usuario
        val headerView = navigationView.getHeaderView(0)
        val headerText: TextView = headerView.findViewById(R.id.headerText)
        headerText.text = "Welcome, " + (LoggedUser.user?.name ?: "") + " " + (LoggedUser.user?.lastname ?: "")


        if (LoggedUser.user?.role?.role == "profesor") {
            menu.findItem(R.id.nav_home_teacher)?.isVisible = true
            menu.findItem(R.id.nav_profile)?.isVisible = true
            menu.findItem(R.id.nav_meetings)?.isVisible = true
            menu.findItem(R.id.nav_meetings_box)?.isVisible = true
            menu.findItem(R.id.nav_logout)?.isVisible = true

            menu.findItem(R.id.nav_home_student)?.isVisible = false
            menu.findItem(R.id.nav_document)?.isVisible = false
            menu.findItem(R.id.nav_course)?.isVisible = false
        } else {
            menu.findItem(R.id.nav_home_student)?.isVisible = true
            menu.findItem(R.id.nav_profile)?.isVisible = true
            menu.findItem(R.id.nav_document)?.isVisible = true
            menu.findItem(R.id.nav_course)?.isVisible = true
            menu.findItem(R.id.nav_logout)?.isVisible = true

            menu.findItem(R.id.nav_meetings_box)?.isVisible = false
            menu.findItem(R.id.nav_home_teacher)?.isVisible = false
            menu.findItem(R.id.nav_meetings)?.isVisible = false
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home_student -> {
                    startActivity(Intent(this, StudentScheduleActivity::class.java))
                    finish()
                }
                R.id.nav_meetings -> {
                    startActivity(Intent(this, MeetingsActivity::class.java))
                    finish()
                }
                R.id.nav_meetings_box -> {
                    startActivity(Intent(this, MeetingBoxActivity::class.java))
                }
                R.id.nav_home_teacher -> {
                    startActivity(Intent(this, TeacherScheduleActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                R.id.nav_document -> {
                    startActivity(Intent(this, DocumentsActivity::class.java))
                }
                R.id.nav_course -> {
                    startActivity(Intent(this, CourseListActivity::class.java))
                    finish()
                }
                R.id.nav_logout -> {
                    LoggedUser.user = null
                    disconnect()

                    val intent = Intent(this, IndexActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}
