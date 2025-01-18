package com.elorrieta.alumnoclient

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.socketIO.LoginSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput

class LoginActivity : AppCompatActivity() {
    private var socketClient: LoginSocket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        socketClient = LoginSocket(this)
        socketClient!!.connect()
        val loginTxt = findViewById<EditText>(R.id.editLogin)
        val passwordTxt = findViewById<EditText>(R.id.editPass)

        findViewById<Button>(R.id.btnLogin)
            .setOnClickListener {
                val login = loginTxt.text.toString()
                val password = passwordTxt.text.toString()

                val loginMsg = MessageLogin(login, password)

                if (login.isNotEmpty() && password.isNotEmpty()) {
                    socketClient!!.doLogin(loginMsg)
                } else {
                    Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show()
                }
            }

        findViewById<Button>(R.id.btnReset)
            .setOnClickListener {
                val login = loginTxt.text.toString()
                val msg = MessageOutput(login)
                if (login.isNotEmpty()) {
                    socketClient!!.doSendPassEmail(msg)
                } else {
                    Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show()
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient!!.disconnect()
    }

    override fun onStop() {
        super.onStop()
        socketClient!!.disconnect()
    }
}