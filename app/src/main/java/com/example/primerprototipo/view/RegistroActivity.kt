package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.UserRepository

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvVolverLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreoRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        etConfirmar = findViewById(R.id.etConfirmarContrasena)
        btnRegistrar = findViewById(R.id.buttonRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        tvVolverLogin.setOnClickListener {
            irALogin()
        }
    }

    private fun registrarUsuario() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val pass = etContrasena.text.toString().trim()
        val confirmar = etConfirmar.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmar) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (UserRepository.findUserByEmail(correo) != null) {
            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Registro exitoso
        val nuevoUsuario = Usuario(nombre, correo, pass)
        UserRepository.addUser(nuevoUsuario)
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
        irALogin()
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
