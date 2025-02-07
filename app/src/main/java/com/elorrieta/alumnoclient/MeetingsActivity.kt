package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.elorrieta.alumnoclient.socketIO.HomeTeacherSocket

class MeetingsActivity : BaseActivity() {
    private var socketClient: HomeTeacherSocket? = null
    private var teacherNames: MutableList<Pair<String, Long>>? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_meetings, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

        //socketClient = HomeTeacherSocket(this)
        //obtenerYRellenarMultiselectorProfesores()

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
            //onSaveMeetingClicked()
        }
    }

    /*
    private fun obtenerYRellenarMultiselectorProfesores() {
        // Lista de profesores, usando un Map donde la clave es el nombre completo y el valor es el id
        val teacherNames = mutableListOf<Pair<String, Long>>()

        // Inicialización del adaptador
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teacherNames.map { it.first })

        // Configuración del MultiAutoCompleteTextView
        val multiAutoCompleteTeachers: MultiAutoCompleteTextView =
            findViewById(R.id.multiAutoCompleteTeachers)
        multiAutoCompleteTeachers.setAdapter(adapter)
        multiAutoCompleteTeachers.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTeachers.threshold = 0 // Mostrar opciones inmediatamente

        // Mostrar el desplegable cuando el usuario hace clic en el campo
        multiAutoCompleteTeachers.setOnClickListener {
            if (!multiAutoCompleteTeachers.isPopupShowing) {
                multiAutoCompleteTeachers.showDropDown()
            }
        }

        // Simula carga de datos dinámicamente
        CoroutineScope(Dispatchers.IO).launch {
            val roleId = 1 // ID del rol que quieres buscar
            // Limpia datos previos y recarga los profesores de manera segura
            val loadedTeachers = mutableListOf<Pair<String, Long>>() // Usamos una lista local para cargar los datos

            // Llamada suspendida a la función de red, asumiendo que 'getUsersByRole' es una función suspendida
            socketClient!!.getUsersByRole(roleId) { users ->
                if (users != null) {
                    users.forEach { user ->
                        val nombreCompleto = "${user.name} ${user.lastname}" // Asegúrate de que el modelo tenga el campo surname
                        loadedTeachers.add(Pair(nombreCompleto, user.id)) // Añadimos el nombre completo y el id
                    }
                }

                // Una vez cargados los datos, actualizamos el adaptador en el hilo principal
                runOnUiThread {
                    teacherNames.clear()
                    teacherNames.addAll(loadedTeachers) // Actualizamos con los nuevos datos

                    // Actualizamos el adaptador con los nombres
                    adapter.clear()
                    adapter.addAll(teacherNames.map { it.first }) // Añadimos solo los nombres completos
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // Obtener el ID del profesor seleccionado al guardar!!
        multiAutoCompleteTeachers.setOnItemClickListener { parent, view, position, id ->
            val selectedProfesor = teacherNames[position]
            val profesorId = selectedProfesor.second // Aquí obtenemos el id asociado

        }
    }


    private fun onSaveMeetingClicked() {
        // Capturar valores de los campos
        val title = findViewById<EditText>(R.id.editTitle).text.toString()
        val subject = findViewById<EditText>(R.id.editSubject).text.toString()
        val day = findViewById<Spinner>(R.id.spinnerDay).selectedItem.toString().toByteOrNull() ?: 0
        val time = findViewById<Spinner>(R.id.spinnerTime).selectedItem.toString().toByteOrNull() ?: 0
        val classroom = findViewById<EditText>(R.id.editClassroom).text.toString().toByteOrNull() ?: 0
        val teachersInput = findViewById<MultiAutoCompleteTextView>(R.id.multiAutoCompleteTeachers).text.toString()

        // Validación
        if (title.isEmpty() || subject.isEmpty() || teachersInput.isEmpty()) {
            findViewById<TextView>(R.id.textErrorMessage).apply {
                visibility = View.VISIBLE
                text = "Por favor, complete todos los campos obligatorios."
            }
        } else {
            findViewById<TextView>(R.id.textErrorMessage).visibility = View.GONE

            // Obtener los identificadores de los profesores seleccionados
            val teacherIds = teachersInput.split(",").mapNotNull { teacherName ->
                teacherNames!!.find { it.first.equals(teacherName.trim(), ignoreCase = true) }?.second
            }

            // Validación de los profesores seleccionados
            if (teacherIds.isEmpty()) {
                findViewById<TextView>(R.id.textErrorMessage).apply {
                    visibility = View.VISIBLE
                    text = "Por favor, seleccione al menos un profesor válido."
                }
            } else {
                // Crear un objeto `Meeting`
                //val meeting = Meeting(
                //    id = null, // Asignado por la base de datos o backend
                //    user = null, // Aquí puedes asignar un usuario actual si aplica
                //    day = day,
                //    time = time,
                //    week = 0, // Puedes agregar lógica para determinar la semana si es necesario
                //    status = "Pending", // Estado inicial
                //    title = title,
                //    room = classroom.toByte(),
                //    subject = subject,
                //    createdAt = Timestamp(System.currentTimeMillis()),
                //    updatedAt = Timestamp(System.currentTimeMillis()),
                //    participants = teacherIds.map { Participant(it) }.toSet() // Guardar solo los IDs
                //)

                // Guardar la reunión en la base de datos local o enviar al servidor
                //saveMeetingToDatabase(meeting)

                Toast.makeText(this, "Reunión generada exitosamente", Toast.LENGTH_SHORT).show()
            }
        }

    }
    */


}