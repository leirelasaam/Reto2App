package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.elorrieta.alumnoclient.socketIO.HomeTeacherSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeetingsActivity : AppCompatActivity() {
    private var socketClient: HomeTeacherSocket? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meetings)
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

        socketClient = HomeTeacherSocket(this)
        obtenerYRellenarMultiselectorProfesores()

        val loginTxt = findViewById<AutoCompleteTextView>(R.id.editLogin)
        val passwordTxt = findViewById<EditText>(R.id.editPass)
        val errorLogin = findViewById<TextView>(R.id.errorLogin)
        val errorPass = findViewById<TextView>(R.id.errorPass)
        val spinnerDay = findViewById<Spinner>(R.id.spinnerDay)
        val spinnerTime = findViewById<Spinner>(R.id.spinnerTime)

        // Configuración de Spinner para día
        val days = listOf("Selecciona el día", "1", "2", "3", "4", "5")
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = dayAdapter

        // Configuración de Spinner para hora
        val hour = listOf("Selecciona la hora", "1", "2", "3", "4", "5", "6")
        val hourAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hour)
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTime.adapter = hourAdapter

        // Listeners para los spinners
        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    Toast.makeText(
                        this@MeetingsActivity,
                        "Por favor, selecciona un día",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    Toast.makeText(
                        this@MeetingsActivity,
                        "Por favor, selecciona una hora",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Botón para guardar reunión
        findViewById<Button>(R.id.buttonSaveMeeting).setOnClickListener {
            onSaveMeetingClicked()
        }
    }

    private fun obtenerYRellenarMultiselectorProfesores() {
        // Lista inicial vacía
        val teacherNames = mutableListOf<String>()

        // Inicialización del adaptador
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teacherNames)

        // Configuración del MultiAutoCompleteTextView
        val multiAutoCompleteTeachers: MultiAutoCompleteTextView =
            findViewById(R.id.multiAutoCompleteTeachers)
        multiAutoCompleteTeachers.setAdapter(adapter)
        multiAutoCompleteTeachers.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTeachers.threshold = 0 // Mostrar opciones inmediatamente

        // Mostrar el desplegable cuando el usuario hace clic en el campo
        multiAutoCompleteTeachers.setOnClickListener {
            if (!multiAutoCompleteTeachers.isPopupShowing) {
                multiAutoCompleteTeachers.showDropDown() // Mostrar el menú desplegable
            }
        }

        // Simula carga de datos dinámicamente
        CoroutineScope(Dispatchers.IO).launch {
            val roleId = 1 // ID del rol que quieres buscar
            var profesoresCargados = mutableListOf<String>() // Lista mutable para almacenar nombres y apellidos
            socketClient!!.getUsersByRole(roleId) { users ->
                if (users != null) {
                    users.forEach { user ->
                    val nombreCompleto = "${user.name} ${user.lastname}" // Asegúrate de que el modelo tenga el campo surname
                    profesoresCargados.add(nombreCompleto)
                    }
                }
            } // Simula datos cargados
            teacherNames.clear() // Limpia datos previos
            teacherNames.addAll(profesoresCargados) // Añade nuevos nombres
            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged() // Notifica cambios al adaptador
            }
        }
    }

    private fun onSaveMeetingClicked() {
        // Capturar valores de los campos
        val title = findViewById<EditText>(R.id.editTitle).text.toString()
        val subject = findViewById<EditText>(R.id.editSubject).text.toString()
        val day = findViewById<Spinner>(R.id.spinnerDay).selectedItem.toString()
        val time = findViewById<Spinner>(R.id.spinnerTime).selectedItem.toString()
        val classroom = findViewById<EditText>(R.id.editClassroom).text.toString()
        val teachers =
            findViewById<MultiAutoCompleteTextView>(R.id.multiAutoCompleteTeachers).text.toString()

        // Validación
        if (title.isEmpty() || subject.isEmpty() || classroom.isEmpty() || teachers.isEmpty()) {
            findViewById<TextView>(R.id.textErrorMessage).visibility = View.VISIBLE
        } else {
            findViewById<TextView>(R.id.textErrorMessage).visibility = View.GONE
            Toast.makeText(this, "Reunión generada exitosamente", Toast.LENGTH_SHORT).show()
            // Aquí puedes añadir la lógica para guardar la reunión en una base de datos o servidor
        }
    }

}
