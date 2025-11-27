package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegistrarse: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Inicializar vistas
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.buttonLogin)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        // Observar el estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
        }

        // Observar el resultado del login
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginViewModel.LoginResult.Success -> {
                    tvError.visibility = View.GONE
                    Toast.makeText(this, "¡Bienvenido ${result.usuario.nombre}!", Toast.LENGTH_SHORT).show()
                    navigateToRoleActivity(result.usuario)
                }
                is LoginViewModel.LoginResult.Error -> {
                    mostrarError(result.mensaje)
                }
            }
        }

        // Evento login
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()
            viewModel.login(correo, contrasena)
        }

        // Ir a registro
        tvRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToRoleActivity(usuario: Usuario) {
        val intent = when (usuario.rol) {
            Role.Usuario -> Intent(this, MapaActivity::class.java)
            Role.Chofer -> Intent(this, ChoferActivity::class.java)
            Role.Admin -> Intent(this, AdminActivity::class.java)
            Role.SuperAdmin -> Intent(this, SuperAdminActivity::class.java)
        }
        intent.putExtra("USUARIO_ACTUAL", usuario) // Pasar el usuario a la siguiente actividad
        startActivity(intent)
        finish()
    }

    private fun mostrarError(mensaje: String) {
        tvError.text = mensaje
        tvError.visibility = View.VISIBLE
    }
}
