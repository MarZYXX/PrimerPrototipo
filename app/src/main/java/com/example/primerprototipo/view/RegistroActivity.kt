package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.viewmodel.RegistroViewModel

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContraseña: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvVolver: TextView

    private val viewModel: RegistroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        inicializarComponentes()
        configurarEventos()
        observarCambios()
    }

    private fun inicializarComponentes() {
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreoRegistro)
        etContraseña = findViewById(R.id.etContraseñaRegistro)
        etConfirmar = findViewById(R.id.etConfirmarContraseña)
        btnRegistrar = findViewById(R.id.buttonRegistrar)
        tvVolver = findViewById(R.id.tvVolverLogin)
    }

    private fun configurarEventos() {
        btnRegistrar.setOnClickListener {
            viewModel.registrarUsuario(
                etNombre.text.toString(),
                etCorreo.text.toString(),
                etContraseña.text.toString(),
                etConfirmar.text.toString()
            )
        }

        tvVolver.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observarCambios() {
        viewModel.registroExitoso.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        viewModel.errorMensaje.observe(this) { error ->
            if (error.isNotEmpty())
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}
