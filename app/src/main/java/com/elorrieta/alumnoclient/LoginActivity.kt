package com.elorrieta.alumnoclient

import android.os.Bundle

import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.room.model.UserRoom
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.socketIO.LoginSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KMutableProperty0

class LoginActivity : AppCompatActivity() {

    //private val tagLoginActivity = "tagLoginActivity"
    //Log.d(tagLoginActivity, "huyghuyhgu8")

    private var socketClient: LoginSocket? = null
    private var isPassVisible: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // POR SI FUERA NECESARIO BORRAR LA DATABASE
        /*
        val context = applicationContext
        val dbName = "usersDatabase"
        context.deleteDatabase(dbName)
        */

        socketClient = LoginSocket(this)
        val loginTxt = findViewById<AutoCompleteTextView>(R.id.editLogin)
        val passwordTxt = findViewById<EditText>(R.id.editPass)
        val errorLogin = findViewById<TextView>(R.id.errorLogin)
        val errorPass = findViewById<TextView>(R.id.errorPass)

        val db = UsersRoomDatabase(this)

        // Obtener el último logueado
        GlobalScope.launch(Dispatchers.IO) {
            val user = db.usersDao().getLastLoggedUser()
            if (user != null) {
                loginTxt.setText(user.email)
                passwordTxt.setText(user.password)
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            val users = db.usersDao().getAll()
            // Crear lista con email y DNI, que son las opciones válidas en nuestro login
            val loginOptions = users.flatMap { listOf(it.email, it.pin) }

            // Poblar el adapter del login
            withContext(Dispatchers.Main) {
                if (loginOptions.isNotEmpty()) {
                    val adapter = ArrayAdapter(this@LoginActivity, android.R.layout.simple_dropdown_item_1line, loginOptions)
                    loginTxt.setAdapter(adapter)
                }
            }

            // Autocompletar el password
            loginTxt.setOnItemClickListener { parent, _, position, _ ->
                // Opción seleccionada en el autocomplete del login
                val selectedLogin = parent.getItemAtPosition(position).toString()
                // Buscar el usuario (objeto que contiene la pass)
                val selectedUser = users.find { it.email == selectedLogin || it.pin == selectedLogin }

                // Completar el pass
                selectedUser?.let {
                    passwordTxt.setText(it.password)
                }
            }
        }

        findViewById<Button>(R.id.btnLogin)
            .setOnClickListener {
                val login = loginTxt.text.toString()
                val password = passwordTxt.text.toString()

                errorLogin.text = ""
                errorPass.text = ""

                val loginMsg = MessageLogin(login, password)

                if (login.isNotEmpty() && password.isNotEmpty()) {
                    socketClient!!.doLogin(loginMsg)
                } else {
                    if (login.isEmpty()){
                        errorLogin.text = "Campo obligatorio"
                    }
                    if (password.isEmpty()){
                        errorPass.text = "Campo obligatorio"
                    }
                }
            }

        findViewById<TextView>(R.id.txtReset)
            .setOnClickListener {
                val login = loginTxt.text.toString()
                val msg = MessageOutput(login)

                errorLogin.text = ""
                errorPass.text = ""

                if (login.isNotEmpty()) {
                    socketClient!!.doSendPassEmail(msg)
                } else {
                    errorLogin.text = "Campo obligatorio"
                }
            }

        val toggleButton = findViewById<ImageView>(R.id.togglePass)
        toggleButton.setOnClickListener {
            isPassVisible = !isPassVisible
            if (isPassVisible) {
                passwordTxt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.setImageResource(R.drawable.eye_hidden)
            } else {
                passwordTxt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.eye)
            }
            passwordTxt.setSelection(passwordTxt.text.length)
        }
    }
/*
    override fun onDestroy() {
        super.onDestroy()
        socketClient!!.disconnect()
    }

    override fun onStop() {
        super.onStop()
        socketClient!!.disconnect()
    }
*/
}