package com.elorrieta.alumnoclient

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.socketIO.ProfileSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageChangePassword
import com.elorrieta.alumnoclient.utils.AESUtil
import java.util.Locale
import kotlin.reflect.KMutableProperty0

class ProfileActivity : BaseActivity() {

    private lateinit var spinnerLanguage: Spinner
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
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_profile, null)
        findViewById<FrameLayout>(R.id.content_frame)?.addView(contentView)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar componentes
        try {
            editOldPassword = findViewById(R.id.editOldPassword)
            editNewPassword = findViewById(R.id.editNewPassword)
            editRepeatPassword = findViewById(R.id.editRepeatPassword)
            buttonSend = findViewById(R.id.buttonSend)
            buttonLanguage = findViewById(R.id.buttonLanguage)
            buttonTheme = findViewById(R.id.buttonTheme)
            toggleOldPassword = findViewById(R.id.toggleOldPassword)
            toggleNewPassword = findViewById(R.id.toggleNewPassword)
            toggleRepeatPassword = findViewById(R.id.toggleRepeatPassword)
            spinnerLanguage = findViewById(R.id.spinnerLanguage)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error inicializando vistas: ${e.message}")
            return
        }

        socketClient = try {
            ProfileSocket(this)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error inicializando socket: ${e.message}")
            null
        }

        setupPasswordVisibility()

        val key = try {
            AESUtil.loadKey(this)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error al cargar la clave de encriptación: ${e.message}")
            null
        }

        buttonSend.setOnClickListener {
            changePassword()
        }

        // Configurar Spinner de idioma
        val languages = listOf("Español", "English", "Euskera")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val language = when (position) {
                    0 -> "es"
                    1 -> "en"
                    2 -> "eus"
                    else -> "es"
                }
                setLocale(language)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                toggleButton.setImageResource(R.drawable.ic_eye_open)
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.ic_eye_closed)
            }
            editText.setSelection(editText.text.length)
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
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val changePasswordMsg = MessageChangePassword(user.email ?: "", oldPassword, newPassword)
        socketClient?.doChangePassword(changePasswordMsg)

        Toast.makeText(this, "Solicitud de cambio de contraseña enviada", Toast.LENGTH_SHORT).show()
    }

    private fun setLocale(language: String) {
        try {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration(resources.configuration)
            config.setLocale(locale)

            // Crear un nuevo contexto con la configuración del idioma
            val newContext = createConfigurationContext(config)
            applyOverrideConfiguration(config)

            // Guardar el idioma en SharedPreferences
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putString("language", language).apply()

            // Reiniciar la actividad para aplicar los cambios
            finish()
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error cambiando idioma: ${e.message}")
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val newContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(newContext)
    }
}
