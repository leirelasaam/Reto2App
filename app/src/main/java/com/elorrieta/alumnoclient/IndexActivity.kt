package com.elorrieta.alumnoclient

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager

class IndexActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var imageView: ImageView

    //Esto lo usamos para la reconexión y cada cuaanto se intenta
    @Suppress("DEPRECATION")
    private val handler = Handler()
    private val retryInterval: Long = 50000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        button = findViewById(R.id.btn_Activity)
        imageView = findViewById(R.id.img_Cuervo)

        imageView.setBackgroundResource(R.drawable.transition)
        val animationDrawable = imageView.background as AnimationDrawable
        animationDrawable.start()
        checkConnection()

        val socket = SocketConnectionManager.getSocket()

        button.setOnClickListener {
            if (isConnected()) {
                // Para que salga algo porque sino igual es que no se puede conectar al servidor
                try {
                    SocketConnectionManager.connect()
                    // Usamos un hilo en segundo plano para evitar bloquear la UI
                    Thread {
                        try {
                            Thread.sleep(2500)
                            if (SocketConnectionManager.isConnected()) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Conexión establecida con el servidor",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "No se ha podido establecer la conexión",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }.start()

                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error al establecer la conexión con el servidor",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.d("index", "$e")
                }

            } else {
                Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun isConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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