package com.example.primerprototipo.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.primerprototipo.R
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.widget.ProgressBar
import android.widget.TextView
import com.example.primerprototipo.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etContraseña = findViewById<EditText>(R.id.etContraseña)
        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        val tvError = findViewById<TextView>(R.id.tvError)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvRegistrarse = findViewById<TextView>(R.id.tvRegistrarse)

        // Acción del botón Login
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString()
            val contraseña = etContraseña.text.toString()
            viewModel.login(correo, contraseña)
        }

        // Navegar a registro
        tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // Observa el estado del login
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, MapaActivity::class.java))
                finish()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) ProgressBar.VISIBLE else ProgressBar.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            tvError.text = message
            tvError.visibility = if (message.isNotEmpty()) TextView.VISIBLE else TextView.GONE
        }
    }
}