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
import android.widget.MultiAutoCompleteTextView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.entity.Participant
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.MeetingSocket
import com.elorrieta.alumnoclient.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Timestamp

class MeetingsActivity : BaseActivity() {
    private var socketClient: MeetingSocket? = null
    private var teacherNames: MutableList<Pair<String, Long>>? = null
    private var currentWeek = Util.getCurrentWeek()

    @SuppressLint("MissingInflatedId", "InflateParams")
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

        socketClient = MeetingSocket(this)
        obtenerYRellenarMultiselectorProfesores()

        findViewById<AutoCompleteTextView>(R.id.editLogin)
        findViewById<EditText>(R.id.editPass)
        findViewById<TextView>(R.id.errorLogin)
        findViewById<TextView>(R.id.errorPass)
        val spinnerDay = findViewById<Spinner>(R.id.spinnerDay)
        val spinnerTime = findViewById<Spinner>(R.id.spinnerTime)

        // Configuración de Spinner para día
        val days = listOf(getString(R.string.select_day), "1", "2", "3", "4", "5")
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = dayAdapter

        // Configuración de Spinner para hora
        val hour = listOf(getString(R.string.select_time), "1", "2", "3", "4", "5", "6")
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
                        getString(R.string.error_select_day),
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
                        getString(R.string.error_select_time),
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

        val selectedTeachers = mutableSetOf<Long>()

        this.teacherNames = mutableListOf()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, this.teacherNames!!.map { it.first })
        val multiAutoCompleteTeachers: MultiAutoCompleteTextView = findViewById(R.id.multiAutoCompleteTeachers)
        multiAutoCompleteTeachers.setAdapter(adapter)
        multiAutoCompleteTeachers.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTeachers.threshold = 0

        // ID del usuario logueado que no debe ser seleccionado
        val loggerUserId = LoggedUser.user!!.id // Asumiendo que LoggedUser es tu usuario logueado

        // Mostrar el popup de selección si no está visible
        multiAutoCompleteTeachers.setOnClickListener {
            if (!multiAutoCompleteTeachers.isPopupShowing) {
                multiAutoCompleteTeachers.showDropDown()
            }
        }

        // Manejo de clic en el item y prevenir duplicados o selección del usuario logueado
        multiAutoCompleteTeachers.setOnItemClickListener { parent, _, position, _ ->
            val selectedTeacherName = parent.getItemAtPosition(position) as String
            val selectedTeacher = this.teacherNames!!.find { it.first == selectedTeacherName }

            if (selectedTeacher != null) {
                val teacherId = selectedTeacher.second

                // Verificar si es el mismo ID que el usuario logueado
                if (teacherId == loggerUserId) {
                    Toast.makeText(this, "You cannot select yourself!", Toast.LENGTH_SHORT).show()
                } else if (!selectedTeachers.contains(teacherId)) {
                    selectedTeachers.add(teacherId) // Añadir al set de seleccionados usando el ID
                    println("Profesor seleccionado: $selectedTeacherName - ID: $teacherId")

                    // Eliminar al profesor seleccionado de la lista de posibles seleccionados
                    this.teacherNames?.removeAll { it.second == teacherId }

                    // Recargar la lista de profesores excluyendo al seleccionado
                    val remainingTeachers = this.teacherNames?.filterNot { selectedTeachers.contains(it.second) } ?: listOf()

                    // Actualizar el adaptador con la lista filtrada
                    adapter.clear()
                    adapter.addAll(remainingTeachers.map { it.first })
                    adapter.notifyDataSetChanged()

                    // Agregar el nombre del profesor al campo de texto
                    val selectedNames = multiAutoCompleteTeachers.text.toString().split(",").toMutableList()
                    if (!selectedNames.contains(selectedTeacherName)) {
                        selectedNames.add(selectedTeacherName)  // Agregar el nuevo nombre si no está ya en el texto
                    }

                    // Actualizar el texto del multiAutoCompleteTeachers para reflejar la selección
                    multiAutoCompleteTeachers.setText(selectedNames.joinToString(","))
                } else {
                    Toast.makeText(this, "This teacher has already been selected!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val roleId = 1
            val loadedTeachers = mutableListOf<Pair<String, Long>>()

            socketClient!!.getUsersByRole(roleId) { users ->
                if (users != null) {
                    users.forEach { user ->
                        if(user.id != LoggedUser.user!!.id){
                            val nombreCompleto = "${user.name} ${user.lastname}"
                            loadedTeachers.add(Pair(nombreCompleto, user.id))
                        }
                    }
                }

                runOnUiThread {
                    this@MeetingsActivity.teacherNames?.clear()
                    this@MeetingsActivity.teacherNames?.addAll(loadedTeachers)

                    adapter.clear()
                    adapter.addAll(this@MeetingsActivity.teacherNames!!.map { it.first })
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // Obtener el ID del profesor seleccionado al hacer clic en un nombre
        multiAutoCompleteTeachers.setOnItemClickListener { parent, view, position, id ->
            val selectedTeacherName = parent.getItemAtPosition(position) as String
            val selectedTeacher = this.teacherNames!!.find { it.first == selectedTeacherName }

            if (selectedTeacher != null) {
                val teacherId = selectedTeacher.second
                println("Profesor seleccionado: $selectedTeacherName - ID: $teacherId")
            }
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
        if (title.isEmpty() || subject.isEmpty() || teachersInput.isEmpty())  {
            findViewById<TextView>(R.id.textErrorMessage).apply {
                visibility = View.VISIBLE
                text = getString(R.string.error_complete_fields)
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
                    text = getString(R.string.error_select_professor)
                }
            } else {
                // Crear un objeto `Meeting`
                val meeting = Meeting(
                    id = null, // Asignado por la base de datos o backend
                    user = LoggedUser.user,
                    day = day,
                    time = time,
                    week = currentWeek.toByte(), // Puedes agregar lógica para determinar la semana si es necesario !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
                    status = "pendiente",
                    title = title,
                    room = classroom.toByte() ?: 0,
                    subject = subject,
                    createdAt = Timestamp(System.currentTimeMillis()),
                    updatedAt = Timestamp(System.currentTimeMillis()),
                    participants = teacherIds.map { idUser -> createParticipant(idUser) }.toSet()
                )

                // Guardar la reunión en la base de datos local o enviar al servidor
                socketClient!!.saveMeeting(meeting)

                Toast.makeText(this, getString(R.string.meeting_saved_successfully), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun createParticipant(idUser: Long): Participant {
        return Participant(
            id = 0, // ID de Participant
            createdAt = null,
            updatedAt = null,
            meeting = null,
            user = User(id = idUser), // Solo inicializamos el ID del usuario
            status = ""
        )
    }

}