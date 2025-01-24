package com.elorrieta.alumnoclient

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.entity.LoggedUser.user
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.socketIO.LoginSocket
import com.elorrieta.alumnoclient.socketIO.RegisterSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageRegister
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private var socketClient: RegisterSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Obtener Rol del cliente


        //Ocultar campos si es profesor
        gestionarCamposInvisiblesProfesor()

        socketClient = RegisterSocket(this)
        socketClient!!.connect()

        //Obtener cada elemento de la vista
        val foto: TextView = findViewById(R.id.textViewFoto)
        val titulo: TextView = findViewById(R.id.textViewTitulo)

        val userEditText: EditText = findViewById(R.id.editTextUser)
        val nombreEditText: EditText = findViewById(R.id.editTextNombre)
        val apellidosEditText: EditText = findViewById(R.id.editTextApellidos)
        val dniEditText: EditText = findViewById(R.id.editTextDNI)
        val correoEditText: EditText = findViewById(R.id.editTextEmail)
        val direccionEditText: EditText = findViewById(R.id.editTextDireccion)

        val telefonoEditText1: EditText = findViewById(R.id.editTextTelefono1)
        val telefonoEditText2: EditText = findViewById(R.id.editTextTelefono2)
        val cicloFormativoEditText: EditText = findViewById(R.id.editTextCicloFormativo) //falta de userDTO
        val cursoEditText: EditText = findViewById(R.id.editTextCurso)  //falta de userDTO
        val clave1EditText: EditText = findViewById(R.id.editTextTextClave1)
        val clave2EditText: EditText = findViewById(R.id.editTextTextClave2)
        val chipDualIntensiva: Chip = findViewById(R.id.chipDualIntesiva)

        //Obtengo el email del user y se lo paso al evento para pedir los datos del usuario
        user?.let { socketClient!!.doSignUp(it.email) }


        //Precargar los datos del usuario
        fun initializeSocket() {
            socketClient = RegisterSocket(this).apply {
                connect()
                socket.on("onRegisterAnswer") { args ->
                    runOnUiThread {
                        if (args.isNotEmpty()) {
                            val response = args[0] as String
                            gestionarRespuestaRegistroServidor(response)
                        }
                    }
                }
            }
        }

        //No necesita mandar ni curso ni ciclo ya que van a estar desactivados
        //Por lo tanto, podré mandar el userDTO que tengo aqui
        //En cambio tendré que obtener los datos uno a uno en el servidor
        //Y uno a uno ir actualizando el usuario en la bbdd
        val botonRegistro: Button = findViewById(R.id.buttonRegistro)
        botonRegistro.setOnClickListener {
            val registerMsg = MessageRegister(
                name = nombreEditText.text.toString(),
                lastname = apellidosEditText.text.toString(),
                pin = dniEditText.text.toString(),
                email = correoEditText.text.toString(),
                password = clave1EditText.text.toString(),
                address = direccionEditText.text.toString(),
                phone1 = telefonoEditText1.text.toString(),
                phone2 = telefonoEditText2.text.toString(),
                registered = true,  //se hace true cuando el usuario recibe 200 de ON_REGISTER_UPDATE_ANSWER
                intensive = chipDualIntensiva.isChecked,
                photo = TODO()
            )
            socketClient?.doRegisterUpdate(registerMsg)
        }
    }

    //Funciones
    fun gestionarCamposInvisiblesProfesor() {
        val esProfesor = intent.getBooleanExtra("ES_PROFESOR", false)
        if (esProfesor) {
            findViewById<EditText>(R.id.editTextCicloFormativo).visibility = View.GONE
            findViewById<EditText>(R.id.editTextCurso).visibility = View.GONE
            findViewById<Chip>(R.id.chipDualIntesiva).visibility = View.GONE
        }
    }
    private fun gestionarRespuestaRegistroServidor(response: String) {
        println("Respuesta del servidor: $response")
    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient?.disconnect()
    }

    override fun onStop() {
        super.onStop()
        socketClient?.disconnect()
    }
}
