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
import kotlinx.coroutines.*

class IndexActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var imageView: ImageView

    @Suppress("DEPRECATION")
    private val handler = Handler()
    private val retryInterval: Long = 50000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        button = findViewById(R.id.btn_Activity)
        imageView = findViewById(R.id.img_Cuervo)

        imageView.setBackgroundResource(R.drawable.transition_logo)
        val animationDrawable = imageView.background as AnimationDrawable
        animationDrawable.start()

        checkConnection()

        button.setOnClickListener {
            if (isConnected()) {
                connectToServer()
            } else {
                Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show()
            }
        }

        startConnectionChecker()
    }

    // Verificar si la conexión está activa
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


    private fun connectToServer() {
        val socket = SocketConnectionManager.getSocket()

        try {
            SocketConnectionManager.connect()
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
                            // Abrir la actividad de Login
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
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
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startConnectionChecker() {
        GlobalScope.launch(Dispatchers.IO) {
            var myConnection = true
            while (true) {
                delay(300)

                val isConenected = isConnected()

                withContext(Dispatchers.Main) {
                    if (!isConenected) {
                        if (myConnection) {
                            Toast.makeText(this@IndexActivity, "NO HAY CONECTIVIDAD", Toast.LENGTH_LONG).show()

                            /*
                            LoggedUser.user = null
                            disconnect()

                            val intent = Intent(this@IndexActivity, IndexActivity::class.java)
                            startActivity(intent)
                            finish()*/


                            myConnection = false
                        }
                        button.isEnabled = false
                    } else {
                        if (!myConnection) {
                            Toast.makeText(this@IndexActivity, "SE HA VUELTO A CONECTAR", Toast.LENGTH_SHORT).show()
                            myConnection = true
                        }
                        button.isEnabled = true
                    }
                }
            }
        }
    }
}
