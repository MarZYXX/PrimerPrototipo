package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.viewmodel.RegistroViewModel

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvVolverLogin: TextView

    private lateinit var viewModel: RegistroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[RegistroViewModel::class.java]

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreoRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        etConfirmar = findViewById(R.id.etConfirmarContrasena)
        btnRegistrar = findViewById(R.id.buttonRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)

        // Observar el resultado del registro
        viewModel.registroExitoso.observe(this) { exitoso ->
            if (exitoso) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                irALogin()
            }
        }

        // Observar mensajes de error
        viewModel.errorMensaje.observe(this) { mensaje ->
            if (mensaje.isNotEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
        }

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pass = etContrasena.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            viewModel.registrarUsuario(nombre, correo, pass, confirmar)
        }

        tvVolverLogin.setOnClickListener {
            irALogin()
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}