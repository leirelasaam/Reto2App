package com.elorrieta.alumnoclient

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class IndexActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var imageView: ImageView
    //Esto lo usamos para la reconexión y cada cuaanto se intenta
    private val handler = Handler()
    private val retryInterval: Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        button = findViewById(R.id.btn_Activity)
        imageView = findViewById(R.id.img_Cuervo)

        imageView.setBackgroundResource(R.drawable.transition)
        val animationDrawable = imageView.background as AnimationDrawable
        animationDrawable.start()
        checkConnection()

        button.setOnClickListener {
            if (isConnected()) {
                val intent = Intent(this, MeetingsActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkConnection() {
        if (isConnected()) {
            button.text = getString(R.string.btn_connected)
            Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
        } else {
            button.text = getString(R.string.btn_no_connection)
            Toast.makeText(this, "Trying to connect ...", Toast.LENGTH_LONG).show()
            handler.postDelayed({
                checkConnection()
            }, retryInterval)
        }
    }
}