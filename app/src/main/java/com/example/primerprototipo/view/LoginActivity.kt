package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.UserRepository

class LoginActivity : AppCompatActivity() {

    // Variables globales
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegistrarse: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialización
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.buttonLogin)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        // Evento login
        btnLogin.setOnClickListener {
            realizarLogin()
        }

        // Ir a registro
        tvRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun realizarLogin() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, llena todos los campos")
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE

        val usuario = UserRepository.findUserByEmail(correo)

        if (usuario != null && usuario.contrasena == contrasena) {
            progressBar.visibility = ProgressBar.GONE
            tvError.visibility = TextView.GONE
            irAMapa()
        } else {
            progressBar.visibility = ProgressBar.GONE
            mostrarError("Correo o contraseña incorrectos")
        }
    }

    private fun irAMapa() {
        val intent = Intent(this, TempMove::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarError(mensaje: String) {
        tvError.text = mensaje
        tvError.visibility = TextView.VISIBLE
    }
}
