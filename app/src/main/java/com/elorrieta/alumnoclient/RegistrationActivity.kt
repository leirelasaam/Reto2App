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
import com.elorrieta.alumnoclient.socketIO.RegisterSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageRegisterUpdate
import com.google.android.material.chip.Chip
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.model.MessageRegister

class RegistrationActivity : AppCompatActivity() {

    private var socketClient: RegisterSocket? = null
    private var imageUri: Uri? = null  // Uri para la imagen capturada
    private val REQUEST_CAMERA_PERMISSION = 1001  // Número único para la solicitud de cámara
    private lateinit var photoByteArray: ByteArray  // Aquí almacenarás la foto en formato byte array

    @SuppressLint("MissingInflatedId")
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
        val idUser = user?.id

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

            findViewById<EditText>(R.id.editTextCicloFormativo).visibility = View.VISIBLE
            findViewById<EditText>(R.id.editTextCurso).visibility = View.VISIBLE

            //Mostrar foto
            if (user.photo != null) {
                val bitmap = user.photo?.let { BitmapFactory.decodeByteArray(user.photo, 0, it.size) }
                foto.setImageBitmap(bitmap)
            }
        }
        else{
            Toast.makeText(this, getString(R.string.register_toast_datos_incompletos), Toast.LENGTH_SHORT).show()
        }

        //Obtenemos la contraseña antigua
        val passAntiguo = user?.password ?: ""

        //Ocultar campos si es profesor
        val rolUsuarioLogeado = user?.role?.role

        //Ocultar campos para profesor
        if (rolUsuarioLogeado.equals("profesor")) {
            Log.d("DEBUG", "El usuario es profesor")
            gestionarCamposInvisiblesProfesor()
        } else {
            Log.d("DEBUG", "El usuario es alumno")
        }

        Toast.makeText(this, getString(R.string.register_toast_datos_incompletos), Toast.LENGTH_SHORT).show()
        var email = "laurine.mitchell@elorrieta-errekamari.com"

        //Lanzamos un evento al servidor
        val registerMsg = MessageRegister(email)

        //Lanza evento pero creo que ya no es necesario
        //socketClient!!.doSignUp(registerMsg)

        Toast.makeText(this, email, Toast.LENGTH_SHORT).show()

        val botonVolver: Button = findViewById(R.id.buttonVolver)
        botonVolver.setOnClickListener {

            /*
            var newActivity = LoginActivity::class.java
            val intent = Intent(activity, newActivity)
            activity.startActivity(intent)
            activity.finish()
            */
        }

        val botonTomarFoto: Button = findViewById(R.id.btn_takephoto)
        botonTomarFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                openCamera() // Llama a tu función para abrir la cámara
            }
        }

        //No necesita mandar; curso, ciclo, rol, ni intensivo ya que van a estar desactivados
        //Por lo tanto, podré mandar el User guardando solo los datos básicos
        val botonRegistro: Button = findViewById(R.id.buttonRegistro)
        botonRegistro.setOnClickListener {
            // La foto tiene que estar disponible antes de registrar
            if (user != null) {
                if (comprobarContraseña(this, clave1EditText.text.toString(), clave2EditText.text.toString(), passAntiguo)) {
                    //Comprobamos si los campos del usuario son nulos.
                    if (!tieneCamposNulos(
                            nombreEditText.text.toString(),
                            apellidosEditText.text.toString(),
                            dniEditText.text.toString(),
                            correoEditText.text.toString(),
                            direccionEditText.text.toString(),
                            telefonoEditText1.text.toString()
                        )
                    ) {
                        //Comprobamos si el usuario ha tomado una foto
                        if ((::photoByteArray.isInitialized)) {
                            val registerMsg = MessageRegisterUpdate(
                                id = idUser.toString(),
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
                            val tag = "Registro"
                            Log.d(tag, "Se mandan los datos actualizados al servidor.")

                        } else (
                                Toast.makeText(this, getString(R.string.register_toast_foto_requerida), Toast.LENGTH_SHORT)
                                    .show()
                                )
                    } else {
                        Toast.makeText(this, getString(R.string.register_toast_campos_incompletos), Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }
        }
    }


    //Métodos

    private fun openCamera() {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val storageDir = getExternalFilesDir(null)
        val photoFile = File(storageDir, fileName)
        imageUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, REQUEST_CAMERA_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAMERA_PERMISSION && resultCode == RESULT_OK) {
            try {
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    // Redimensionar la imagen antes de comprimir
                    val resizedBitmap = resizeBitmap(bitmap, 800, 800)

                    // Comprimir la imagen a un tamaño aproximado de 40 KB
                    val compressedBytes = compressToTargetSize(resizedBitmap, 40 * 1024)  // 40 KB
                    this.photoByteArray = compressedBytes

                    // Mostrar la imagen en el ImageView
                    val imageView: ImageView = findViewById(R.id.textViewFoto)
                    imageView.setImageBitmap(resizedBitmap)

                    Toast.makeText(this, "${getString(R.string.register_toast_imagen_comprimida)}+ (${compressedBytes.size} bytes)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.register_toast_error_imagen), Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.register_Error_Al_Procesar_Imagen), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Redimensiona la imagen para reducir su tamaño.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scaleFactor = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

        return Bitmap.createScaledBitmap(bitmap, (width * scaleFactor).toInt(), (height * scaleFactor).toInt(), true)
    }

    /**
     * Comprime la imagen iterativamente hasta que su tamaño sea cercano a targetSizeBytes.
     */
    private fun compressToTargetSize(bitmap: Bitmap, targetSizeBytes: Int): ByteArray {
        var quality = 90  // Calidad inicial
        var byteArray: ByteArray

        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            byteArray = stream.toByteArray()
            quality -= 10  // Reducir calidad en pasos de 10

        } while (byteArray.size > targetSizeBytes && quality > 10)  // Evitar calidad menor a 10

        return byteArray
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

    //Métodos para comprobar datos - Devuelve true si algun campo es nulo
    fun tieneCamposNulos(name: String, lastname: String, pin: String, email: String, address: String, phone1: String): Boolean {
        return (name.isNullOrBlank() ||
                lastname.isNullOrBlank() ||
                pin.isNullOrBlank() ||
                email.isNullOrBlank() ||
                address.isNullOrBlank() ||
                phone1.isNullOrBlank())
    }

    fun comprobarContraseña(context: Context, passNuevo1: String, passNuevo2: String, passAntiguo: String): Boolean {
        if (passNuevo1 != passNuevo2) {
            Toast.makeText(this, getString(R.string.register_Error_Pass_Deben_Ser_Iguales), Toast.LENGTH_SHORT).show()
            return false
        }
        if (passNuevo1.length < 6) {
            Toast.makeText(this, getString(R.string.register_Error_Requisitos_Pass), Toast.LENGTH_SHORT).show()
            return false
        }
        if (passAntiguo == passNuevo1) {
            Toast.makeText(this, getString(R.string.register_Error_Pass_Igual_Anterior), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /*
    override fun onDestroy() {
        super.onDestroy()
        socketClient?.disconnect()
    }

    override fun onStop() {
        super.onStop()
        socketClient?.disconnect()
    }
    */

}
