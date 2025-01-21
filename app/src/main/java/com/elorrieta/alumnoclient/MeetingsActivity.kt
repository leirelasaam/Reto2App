package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.socketIO.LoginSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeetingsActivity : AppCompatActivity() {
    private var socketClient: LoginSocket? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meetings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        socketClient = LoginSocket(this)
        socketClient!!.connect()
        val loginTxt = findViewById<AutoCompleteTextView>(R.id.editLogin)
        val passwordTxt = findViewById<EditText>(R.id.editPass)
        val errorLogin = findViewById<TextView>(R.id.errorLogin)
        val errorPass = findViewById<TextView>(R.id.errorPass)

        // Configuración de MultiAutoCompleteTextView
        val teacherNames = arrayOf("Teacher 1", "Teacher 2", "Teacher 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teacherNames)

        val multiAutoCompleteTeachers: MultiAutoCompleteTextView = findViewById(R.id.multiAutoCompleteTeachers)
        multiAutoCompleteTeachers.setAdapter(adapter)
        multiAutoCompleteTeachers.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        // Configuración de Spinner para día PONERLO COMO EN LA BASES DE DATOS!!!!
        val days = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerDay).adapter = dayAdapter

        // Configuración de Spinner para hora
        val timeSlots = arrayOf("10:00", "11:00", "12:00", "13:00")
        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerTime).adapter = timeAdapter

        // Botón para guardar reunión
        findViewById<Button>(R.id.buttonSaveMeeting).setOnClickListener {
            onSaveMeetingClicked()
    }
}

    private fun onSaveMeetingClicked() {
        // Capturar valores de los campos
        val title = findViewById<EditText>(R.id.editTitle).text.toString()
        val subject = findViewById<EditText>(R.id.editSubject).text.toString()
        val day = findViewById<Spinner>(R.id.spinnerDay).selectedItem.toString()
        val time = findViewById<Spinner>(R.id.spinnerTime).selectedItem.toString()
        val classroom = findViewById<EditText>(R.id.editClassroom).text.toString()
        val teachers = findViewById<MultiAutoCompleteTextView>(R.id.multiAutoCompleteTeachers).text.toString()

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
