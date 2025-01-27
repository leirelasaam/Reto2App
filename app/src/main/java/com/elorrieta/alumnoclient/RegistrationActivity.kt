package com.elorrieta.alumnoclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.entity.LoggedUser.user
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.socketIO.LoginSocket
import com.elorrieta.alumnoclient.socketIO.RegisterSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageRegister
import com.elorrieta.alumnoclient.socketIO.model.MessageRegisterUpdate
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class RegistrationActivity : AppCompatActivity() {

    private var socketClient: RegisterSocket? = null
    private var imageUri: Uri? = null  // Uri para la imagen capturada
    private val IMAGE_CAPTURE_CODE = 1001
    private lateinit var photoByteArray: ByteArray  // Aquí almacenarás la foto en formato byte array


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
        val foto: ImageView = findViewById(R.id.textViewFoto)
        val titulo: TextView = findViewById(R.id.textViewTitulo)

        val userEditText: EditText = findViewById(R.id.editTextUser)
        val nombreEditText: EditText = findViewById(R.id.editTextNombre)
        val apellidosEditText: EditText = findViewById(R.id.editTextApellidos)
        val dniEditText: EditText = findViewById(R.id.editTextDNI)
        val correoEditText: EditText = findViewById(R.id.editTextEmail)
        val direccionEditText: EditText = findViewById(R.id.editTextDireccion)

        val telefonoEditText1: EditText = findViewById(R.id.editTextTelefono1)
        val telefonoEditText2: EditText = findViewById(R.id.editTextTelefono2)
        val cicloFormativoEditText: EditText =
            findViewById(R.id.editTextCicloFormativo) //falta de userDTO
        val cursoEditText: EditText = findViewById(R.id.editTextCurso)  //falta de userDTO
        val clave1EditText: EditText = findViewById(R.id.editTextTextClave1)
        val clave2EditText: EditText = findViewById(R.id.editTextTextClave2)
        val chipDualIntensiva: Chip = findViewById(R.id.chipDualIntesiva)

        //Obtengo el email del user y se lo paso al evento para pedir los datos del usuario
        user?.let { it.email?.let { it1 -> socketClient!!.doSignUp(it1) } }




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

        val botonTomarFoto: Button = findViewById(R.id.btn_takephoto)
        botonTomarFoto.setOnClickListener {
            openCamera()
        }

        //No necesita mandar ni curso ni ciclo ya que van a estar desactivados
        //Por lo tanto, podré mandar el userDTO que tengo aqui
        //En cambio tendré que obtener los datos uno a uno en el servidor
        //Y uno a uno ir actualizando el usuario en la bbdd
        val botonRegistro: Button = findViewById(R.id.buttonRegistro)
        botonRegistro.setOnClickListener {
            // La foto tiene que estar disponible antes de registrar
            if (::photoByteArray.isInitialized) {
                val registerMsg = MessageRegisterUpdate(
                    name = nombreEditText.text.toString(),
                    lastname = apellidosEditText.text.toString(),
                    pin = dniEditText.text.toString(),
                    email = correoEditText.text.toString(),
                    password = clave1EditText.text.toString(),
                    address = direccionEditText.text.toString(),
                    phone1 = telefonoEditText1.text.toString(),
                    phone2 = telefonoEditText2.text.toString(),
                    registered = true,
                    intensive = chipDualIntensiva.isChecked,
                    photo = photoByteArray // Asignamos el byteArray de la foto
                )
                socketClient?.doRegisterUpdate(registerMsg) // Enviar al servidor
            } else {
                // Mostrar mensaje si no se ha tomado una foto
                Toast.makeText(this, "Debe tomar una foto", Toast.LENGTH_SHORT).show()
            }
        }



    }

    //Métodos

    // Método para abrir la cámara
    private fun openCamera() {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val storageDir = getExternalFilesDir(null)
        val photoFile = File(storageDir, fileName)
        imageUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    // Gestiona la foto tomada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            try {
                // Convierte la imagen capturada a un ByteArray
                val photoFile = File(imageUri?.path)
                photoByteArray = convertFileToByteArray(photoFile)

                // Una vez capturada la imagen se muestra para que la pueda ver el usuario
                val imageUri = Uri.fromFile(photoFile)
                val imageView: ImageView = findViewById(R.id.textViewFoto)
                imageView.setImageURI(imageUri)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Convierte la imagen a en un ByteArray
    private fun convertFileToByteArray(file: File): ByteArray {
        val inputStream = FileInputStream(file)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

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
