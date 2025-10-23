package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    // 🔹 Declaración modular de vistas (afuera de onCreate)
    private lateinit var etCorreo: EditText
    private lateinit var etContraseña: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegistrarse: TextView
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    // 🔹 ViewModel
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inicializarComponentes()
        configurarEventos()
        observarCambios()
    }

    // 🔹 Inicializa vistas solo una vez
    private fun inicializarComponentes() {
        etCorreo = findViewById(R.id.etCorreo)
        etContraseña = findViewById(R.id.etContraseña)
        btnLogin = findViewById(R.id.buttonLogin)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)
    }

    // 🔹 Configura listeners y acciones de botones
    private fun configurarEventos() {
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString()
            val contraseña = etContraseña.text.toString()
            viewModel.login(correo, contraseña)
        }

        tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    // 🔹 Observa los cambios del ViewModel
    private fun observarCambios() {
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, MapaActivity::class.java))
                finish()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) ProgressBar.VISIBLE else ProgressBar.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            tvError.text = error
            tvError.visibility = if (error.isNotEmpty()) TextView.VISIBLE else TextView.GONE
        }
    }
}
