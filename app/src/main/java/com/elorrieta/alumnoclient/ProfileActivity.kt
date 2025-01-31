package com.elorrieta.alumnoclient

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.ProfileSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageChangePassword
import com.elorrieta.alumnoclient.utils.AESUtil
import javax.crypto.SecretKey
import kotlin.reflect.KMutableProperty0

class ProfileActivity : BaseActivity() {

    private var socketClient: ProfileSocket? = null

    private lateinit var editOldPassword: EditText
    private lateinit var editNewPassword: EditText
    private lateinit var editRepeatPassword: EditText
    private lateinit var buttonSend: Button
    private lateinit var buttonLanguage: Button
    private lateinit var buttonTheme: Button

    private lateinit var toggleOldPassword: ImageView
    private lateinit var toggleNewPassword: ImageView
    private lateinit var toggleRepeatPassword: ImageView

    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isRepeatPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_profile, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        /*
        // Inicializar componentes
        editOldPassword = findViewById(R.id.editOldPassword)
        editNewPassword = findViewById(R.id.editNewPassword)
        editRepeatPassword = findViewById(R.id.editRepeatPassword)
        buttonSend = findViewById(R.id.buttonSend)
        buttonLanguage = findViewById(R.id.buttonLanguage)
        buttonTheme = findViewById(R.id.buttonTheme)

        // Inicializar iconos de ojo
        toggleOldPassword = findViewById(R.id.toggleOldPassword)
        toggleNewPassword = findViewById(R.id.toggleNewPassword)
        toggleRepeatPassword = findViewById(R.id.toggleRepeatPassword)
        */

        socketClient = ProfileSocket(this)

        // Configurar visibilidad de contraseñas
        setupPasswordVisibility()

        val key = try {
            AESUtil.loadKey(this) // Cargar la clave de encriptación
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error al cargar la clave de encriptación: ${e.message}")
            null
        }

        // Configurar botón de cambio de contraseña
        buttonSend.setOnClickListener {
            changePassword()
        }
    }

    private fun setupPasswordVisibility() {
        setPasswordVisibility(editOldPassword, toggleOldPassword, ::isOldPasswordVisible)
        setPasswordVisibility(editNewPassword, toggleNewPassword, ::isNewPasswordVisible)
        setPasswordVisibility(editRepeatPassword, toggleRepeatPassword, ::isRepeatPasswordVisible)
    }

    private fun setPasswordVisibility(editText: EditText, toggleButton: ImageView, visibilityFlag: KMutableProperty0<Boolean>) {
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        toggleButton.setOnClickListener {
            visibilityFlag.set(!visibilityFlag.get())
            if (visibilityFlag.get()) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.setImageResource(R.drawable.ic_eye_open) // Icono de ojo abierto
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.ic_eye_closed) // Icono de ojo cerrado
            }
            editText.setSelection(editText.text.length) // Mantiene el cursor en su posición
        }
    }

    private fun changePassword() {
        val oldPassword = editOldPassword.text.toString().trim()
        val newPassword = editNewPassword.text.toString().trim()
        val repeatPassword = editRepeatPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != repeatPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val user = LoggedUser.user
        /*if (user == null || user.password != oldPassword) {
            Toast.makeText(this, "La contraseña actual no es correcta", Toast.LENGTH_SHORT).show()
            return
        }*/

        val changePasswordMsg = MessageChangePassword(user!!.email!!, oldPassword, newPassword)
        socketClient?.doChangePassword(changePasswordMsg)

        Toast.makeText(this, "Solicitud de cambio de contraseña enviada", Toast.LENGTH_SHORT).show()
    }
}