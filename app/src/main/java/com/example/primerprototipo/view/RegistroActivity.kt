package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.viewmodel.RegistroViewModel

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellidoPaterno: EditText
    private lateinit var etApellidoMaterno: EditText

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvVolverLogin: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: RegistroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        viewModel = ViewModelProvider(this)[RegistroViewModel::class.java]

        etNombre = findViewById(R.id.etNombre)
        etApellidoPaterno = findViewById(R.id.editTextAPP)
        etApellidoMaterno = findViewById(R.id.editTextAPM)
        etCorreo = findViewById(R.id.etCorreoRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        etConfirmar = findViewById(R.id.etConfirmarContrasena)
        btnRegistrar = findViewById(R.id.buttonRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)
        progressBar = findViewById(R.id.progressBarRegistro) // Asegúrate de que este ID exista en tu XML

        setupObservers()

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellidoPaterno = etApellidoPaterno.text.toString().trim()
            val apellidoMaterno = etApellidoMaterno.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pass = etContrasena.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            viewModel.registro(nombre, apellidoPaterno, apellidoMaterno, correo, pass, confirmar)
        }

        tvVolverLogin.setOnClickListener {
            irALogin()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegistrar.isEnabled = !isLoading
        }

        viewModel.registroResult.observe(this) { result ->
            when (result) {
                is RegistroViewModel.RegistroResult.Success -> {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    irALogin()
                }
                is RegistroViewModel.RegistroResult.Error -> {
                    Toast.makeText(this, result.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
