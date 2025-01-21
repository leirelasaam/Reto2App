package com.elorrieta.alumnoclient

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.AnimationDrawable

class IndexActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var imageView: ImageView
    private val handler = Handler()
    private val retryInterval: Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        button = findViewById(R.id.btn_Activity)
        imageView = findViewById(R.id.img_Cuervo)

        imageView.setBackgroundResource(R.drawable.transition)
        val animationDrawable = imageView.background as AnimationDrawable
        animationDrawable.start() // Inicia la animación de la imagen

        checkConnection()

        // Acción del botón al hacer clic
        button.setOnClickListener {
            if (isNetworkConnected()) {
                // Si hay conexión, navegar al Login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Finaliza la MainActivity
            } else {
                // Si no hay conexión, mostrar un mensaje de error
                Toast.makeText(this, "No hay conexión", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Metodo para verificar la conectividad
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Metodo para comprobar la conectividad y actualizar la UI
    private fun checkConnection() {
        if (isNetworkConnected()) {
            button.text = getString(R.string.btn_connected) // Texto para cuando haya conexión
            Toast.makeText(this, "Hay conexión", Toast.LENGTH_LONG).show()
        } else {
            // Si no hay conexión, actualiza el botón y reintenta después de un intervalo
            button.text = getString(R.string.btn_no_connection) // Texto para cuando no haya conexión
            Toast.makeText(this, "Reintentando conexión...", Toast.LENGTH_LONG).show()

            // Reintentar la conexión después del intervalo
            handler.postDelayed({
                checkConnection() // Llamada recursiva para verificar nuevamente
            }, retryInterval)
        }
    }
}