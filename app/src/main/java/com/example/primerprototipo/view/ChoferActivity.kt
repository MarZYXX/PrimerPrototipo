package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario

class ChoferActivity : AppCompatActivity() {
    private lateinit var editTiempoSalida: EditText
    private lateinit var numPasajeros: TextView
    private lateinit var parada: TextView
    private lateinit var usuarioActual: Usuario
    private lateinit var sesionChofer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chofer)

        usuarioActual = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }
        editTiempoSalida = findViewById(R.id.tiempoSalida)
        numPasajeros = findViewById(R.id.noPasajeros)
        parada = findViewById(R.id.nextParada)
        sesionChofer = findViewById(R.id.sesionChofer)

        sesionChofer.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}