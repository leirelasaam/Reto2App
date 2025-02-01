package com.elorrieta.alumnoclient

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.socketIO.RegisterSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageRegisterUpdate
import com.google.android.material.chip.Chip
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import android.Manifest
import android.content.Context
import android.util.Log
import com.elorrieta.alumnoclient.room.model.UserRoom
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageRegister


class RegistrationActivity : AppCompatActivity() {

    private var socketClient: RegisterSocket? = null
    private var imageUri: Uri? = null  // Uri para la imagen capturada
    private val REQUEST_CAMERA_PERMISSION = 1001  // Número único para la solicitud de cámara
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
        socketClient = RegisterSocket(this)

        //Obtener cada elemento de la vista
        val foto: ImageView = findViewById(R.id.textViewFoto)
        val titulo: TextView = findViewById(R.id.textViewTitulo)

        //val userEditText: EditText = findViewById(R.id.editTextUser)
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

        //Obtenemos el usuario logueado
        val user = LoggedUser.user;

        //Mostramos los datos del usuario
        if (user != null) {
            nombreEditText.setText(user.name)
            apellidosEditText.setText(user.lastname)
            dniEditText.setText(user.pin)
            correoEditText.setText(user.email)
            direccionEditText.setText(user.address)
            telefonoEditText1.setText(user.phone1)
            telefonoEditText2.setText(user.phone2)
            chipDualIntensiva.isChecked = user.intensive

            //Mostrar ciclo formativo
            cicloFormativoEditText.setText(user.modules.joinToString { module -> module.course.toString()})
            if (user.modules.isEmpty()) {
                Log.d("DEBUG", "El conjunto de módulos está vacío")
            } else {
                Log.d("DEBUG", "El conjunto de módulos tiene elementos: ${user.modules}")
            }

            //Mostrar curso
            cursoEditText.setText(user.modules.map {it.course }.toSet().joinToString())

            //Mostrar foto
            if (user.photo != null) {
                val bitmap = user.photo?.let { BitmapFactory.decodeByteArray(user.photo, 0, it.size) }
                foto.setImageBitmap(bitmap)
            }
        }

        //Obtenemos la contraseña antigua
        val passAntiguo = user?.password ?: ""

        //Ocultar campos si es profesor
        val rolUsuarioLogeado = user?.role?.role

        //Ocultar campos para profesor
        if (user?.role?.role.isNullOrEmpty()) {
            Log.d("DEBUG", "El rol del usuario está vacío")
        } else {
            Log.d("DEBUG", "El usuario tiene rol: ${rolUsuarioLogeado}")
        }
        if(rolUsuarioLogeado == "profesor"){
            gestionarCamposInvisiblesProfesor()
        }

        Toast.makeText(this, "Attempt of sign up", Toast.LENGTH_SHORT).show()
        var email = "laurine.mitchell@elorrieta-errekamari.com"

        //Lanzamos un evento al servidor
        val registerMsg = MessageRegister(email)

        socketClient!!.doSignUp(registerMsg)

        Toast.makeText(this, email, Toast.LENGTH_SHORT).show()

        //Precargar los datos del usuario
        /*
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
*/
        val botonTomarFoto: Button = findViewById(R.id.btn_takephoto)
        botonTomarFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                openCamera() // Llama a tu función para abrir la cámara
            }
        }


        //Esto tendrá que ir dentro del boton registrar
        if (comprobarContraseña(this, clave1EditText.toString(), clave2EditText.toString(), passAntiguo)) {
            Toast.makeText(this, "Contraseña cambiada con éxito", Toast.LENGTH_SHORT).show()
        }


        //No necesita mandar; curso, ciclo, rol, ni intensivo ya que van a estar desactivados
        //Por lo tanto, podré mandar el User guardando solo los datos básicos
        val botonRegistro: Button = findViewById(R.id.buttonRegistro)
        botonRegistro.setOnClickListener {
            // La foto tiene que estar disponible antes de registrar
            if (user != null) {
                //Comprobamos si los campos del usuario son nulos.
                if (tieneCamposNulos(nombreEditText.toString(), apellidosEditText.toString(), dniEditText.toString(), correoEditText.toString(), direccionEditText.toString(), telefonoEditText1.toString(), telefonoEditText2.toString()) ) {
                    if((::photoByteArray.isInitialized)){
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
                            photo = photoByteArray // Asignamos el byteArray de la foto
                        )
                    socketClient?.doRegisterUpdate(registerMsg) // Enviar al servidor
                    }
                    else(
                            Toast.makeText(this, "Debes tomar una foto.", Toast.LENGTH_SHORT).show()
                    )
                } else {
                    // Mostrar mensaje si no se ha tomado una foto
                    Toast.makeText(this, "Debes rellenar todos los campos.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

        //Método para comprobar que los datos son correctos o que se han modificado hay que poner validateInputs() && en el if de botonRegistro.setOnClick...
    /*fun validateInputs(): Boolean {
        return nombreEditText.text.isNotEmpty() &&
                apellidosEditText.text.isNotEmpty() &&
                dniEditText.text.isNotEmpty() &&
                correoEditText.text.isNotEmpty() &&
                clave1EditText.text.toString() == clave2EditText.text.toString() &&
                ::photoByteArray.isInitialized
    }*/

    //Métodos

    // Método para abrir la cámara
    private fun openCamera() {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val storageDir = getExternalFilesDir(null)
        val photoFile = File(storageDir, fileName)
        imageUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, REQUEST_CAMERA_PERMISSION)
    }

    // Gestiona la foto tomada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAMERA_PERMISSION && resultCode == RESULT_OK) {
            try {
                // Usamos el ContentResolver para obtener el inputStream de la imagen
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val photoByteArray = inputStream?.readBytes()
                inputStream?.close()

                if (photoByteArray != null) {
                    // La imagen fue leída correctamente en un ByteArray
                    Toast.makeText(this, "Imagen leída correctamente en bytes", Toast.LENGTH_SHORT).show()

                    // Puedes asignar el ByteArray a la variable photoByteArray para usarla posteriormente
                    this.photoByteArray = photoByteArray

                    // Mostrar la imagen en el ImageView
                    val imageView: ImageView = findViewById(R.id.textViewFoto)
                    imageView.setImageURI(imageUri)
                } else {
                    Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
                }

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
            findViewById<EditText>(R.id.editTextCicloFormativo).visibility = View.GONE
            findViewById<EditText>(R.id.editTextCurso).visibility = View.GONE
            findViewById<Chip>(R.id.chipDualIntesiva).visibility = View.GONE
    }

    private fun gestionarRespuestaRegistroServidor(response: String) {
        println("Respuesta del servidor: $response")
    /*
        // Asumiendo que la respuesta del servidor es un JSON que contiene los datos del usuario
        val userFromServer = parseResponseToUser(response)  // Aquí debes convertir la respuesta a tu objeto 'User'

        // Ahora asignamos los valores a los campos
        nombreEditText.setText(userFromServer.name)
        apellidosEditText.setText(userFromServer.lastname)
        dniEditText.setText(userFromServer.pin)
        correoEditText.setText(userFromServer.email)
        direccionEditText.setText(userFromServer.address)
        telefonoEditText1.setText(userFromServer.phone1)
        telefonoEditText2.setText(userFromServer.phone2)
        cicloFormativoEditText.setText(userFromServer.cycle)
        cursoEditText.setText(userFromServer.course)
        chipDualIntensiva.isChecked = userFromServer.intensive

        // Si hay una foto en el servidor, cargarla en la vista de la imagen
        if (userFromServer.photo != null) {
            val bitmap = BitmapFactory.decodeByteArray(userFromServer.photo, 0, userFromServer.photo.size)
            foto.setImageBitmap(bitmap)
        }
    */

    }
    //Métodos para comprobar datos - Devuelve true si algun campo es nulo
    fun tieneCamposNulos(name: String, lastname: String, pin: String, email: String, address: String, phone1: String, phone2: String): Boolean {
        return (name.isNullOrBlank() &&
                lastname.isNullOrBlank() &&
                pin.isNullOrBlank() &&
                email.isNullOrBlank() &&
                address.isNullOrBlank() &&
                phone1.isNullOrBlank() &&
                phone2.isNullOrBlank())
    }

    fun comprobarContraseña(context: Context, passNuevo1: String, passNuevo2: String, passAntiguo: String): Boolean {
        if (passNuevo1 != passNuevo2) {
            Toast.makeText(this, "Las contraseñas deben ser iguales", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passNuevo1.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passAntiguo == passNuevo1) {
            Toast.makeText(this, "La nueva contraseña no puede ser igual a la predefinida", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
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
