package com.elorrieta.alumnoclient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

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

        if (LoggedUser.user?.role?.role == "profesor") {
            menu.findItem(R.id.nav_home_teacher)?.isVisible = true
            menu.findItem(R.id.nav_profile_teacher)?.isVisible = true
            menu.findItem(R.id.nav_meetings)?.isVisible = true
            menu.findItem(R.id.nav_meetings_status)?.isVisible = true

            menu.findItem(R.id.nav_home_student)?.isVisible = false
            menu.findItem(R.id.nav_profile_student)?.isVisible = false
            menu.findItem(R.id.nav_document)?.isVisible = false
            menu.findItem(R.id.nav_course)?.isVisible = false
        } else {
            menu.findItem(R.id.nav_home_student)?.isVisible = true
            menu.findItem(R.id.nav_profile_student)?.isVisible = true
            menu.findItem(R.id.nav_document)?.isVisible = true
            menu.findItem(R.id.nav_course)?.isVisible = true

            menu.findItem(R.id.nav_home_teacher)?.isVisible = false
            menu.findItem(R.id.nav_profile_teacher)?.isVisible = false
            menu.findItem(R.id.nav_meetings)?.isVisible = false
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home_student -> {
                    startActivity(Intent(this, HomeStudentActivity::class.java))
                }
                R.id.nav_profile_student -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_meetings -> {
                    startActivity(Intent(this, MeetingsActivity::class.java))
                }
                R.id.nav_meetings_status -> {
                    startActivity(Intent(this, MeetingBoxActivity::class.java))
                }
                R.id.nav_home_teacher -> {
                    startActivity(Intent(this, HomeTeacherActivity::class.java))
                }
                R.id.nav_profile_teacher -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_document -> {
                    startActivity(Intent(this, DocumentsActivity::class.java))
                }
                R.id.nav_course -> {
                    startActivity(Intent(this, CourseListActivity::class.java))
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
