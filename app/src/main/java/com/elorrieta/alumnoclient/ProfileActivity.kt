package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.ProfileSocket
import com.elorrieta.alumnoclient.socketIO.model.MessageChangePassword
import com.elorrieta.alumnoclient.utils.AESUtil
import java.util.Locale
import kotlin.reflect.KMutableProperty0

@Suppress("DEPRECATION", "UNUSED_EXPRESSION")
class ProfileActivity : BaseActivity() {

    private lateinit var iconLanguage: ImageView
    private var socketClient: ProfileSocket? = null

    private lateinit var editOldPassword: EditText
    private lateinit var editNewPassword: EditText
    private lateinit var editRepeatPassword: EditText
    private lateinit var buttonSend: Button
    private lateinit var buttonTheme: ImageView

    private lateinit var toggleOldPassword: ImageView
    private lateinit var toggleNewPassword: ImageView
    private lateinit var toggleRepeatPassword: ImageView

    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isRepeatPasswordVisible = false

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"  // Español por defecto
        setLocale(language)  // Aplicar el idioma guardado

        super.onCreate(savedInstanceState)
       /* setContentView(R.layout.activity_profile) */
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_profile, null)
        findViewById<FrameLayout>(R.id.content_frame)?.addView(contentView)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            editOldPassword = findViewById(R.id.editOldPassword)
            editNewPassword = findViewById(R.id.editNewPassword)
            editRepeatPassword = findViewById(R.id.editRepeatPassword)
            buttonSend = findViewById(R.id.buttonSend)
            buttonTheme = findViewById(R.id.buttonTheme)
            toggleOldPassword = findViewById(R.id.toggleOldPassword)
            toggleNewPassword = findViewById(R.id.toggleNewPassword)
            toggleRepeatPassword = findViewById(R.id.toggleRepeatPassword)
            iconLanguage = findViewById(R.id.iconLanguage)
        } catch (e: Exception) {
            Log.e("ProfileActivity", getString(R.string.error_initializing_views) + ": ${e.message}")
            return
        }

        socketClient = try {
            ProfileSocket(this)
        } catch (e: Exception) {
            Log.e("ProfileActivity", getString(R.string.error_initializing_socket) + ":  ${e.message}")
            null
        }

        setupPasswordVisibility()
        setupLanguageSelector()
        setupThemeToggle()

        try {
            AESUtil.loadKey(this)
        } catch (e: Exception) {
            Log.e("ProfileActivity", getString(R.string.error_loading_encryption_key) + ":  ${e.message}")
            null
        }

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
                toggleButton.setImageResource(R.drawable.ic_eye_open)
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.ic_eye_closed)
            }
            editText.setSelection(editText.text.length)
        }
    }

    private fun setupLanguageSelector() {
        Log.d("ProfileActivity", "setupLanguageSelector inicializado")

        val languageButton = findViewById<ImageView>(R.id.iconLanguage) // Usa el ID correcto

        languageButton.setOnClickListener { view ->
            Log.d("ProfileActivity", "spinnerLanguage clicado")
            showLanguagePopupMenu(view)
        }
    }

    private fun showLanguagePopupMenu(view: View) {
        Log.d("ProfileActivity", getString(R.string.showing_language_menu))
        Toast.makeText(this,  getString(R.string.opening_language_menu), Toast.LENGTH_SHORT).show() // <-- Depuración visual

        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.language_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            Log.d("ProfileActivity", "Opción de idioma seleccionada: ${item.title}")
            val languageCode = when (item.itemId) {
                R.id.lang_es -> "es"
                R.id.lang_en -> "en"
                R.id.lang_eus -> "eu"
                else -> return@setOnMenuItemClickListener false
            }
            setLocale(languageCode)
            true
        }
        popupMenu.show()
    }


    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics) // ✅ Compatible con todas las versiones

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", language).apply()
    }


    private fun changePassword() {
        val oldPassword = editOldPassword.text.toString().trim()
        val newPassword = editNewPassword.text.toString().trim()
        val repeatPassword = editRepeatPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_complete_fields) , Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != repeatPassword) {
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        val user = LoggedUser.user
        if (user == null) {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val changePasswordMsg = MessageChangePassword(user.email ?: "", oldPassword, newPassword)
        socketClient?.doChangePassword(changePasswordMsg)

        Toast.makeText(this, getString(R.string.password_reset_requested), Toast.LENGTH_SHORT).show()
    }

    private fun setupThemeToggle() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        updateThemeIcon(isDarkMode)

        buttonTheme.setOnClickListener {
            val newMode = !isDarkMode
            prefs.edit().putBoolean("dark_mode", newMode).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (newMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            updateThemeIcon(newMode)
            recreate()
        }
    }

    private fun updateThemeIcon(isDarkMode: Boolean) {
        buttonTheme.setImageResource(if (isDarkMode) R.drawable.luna else R.drawable.sol)
    }
}
