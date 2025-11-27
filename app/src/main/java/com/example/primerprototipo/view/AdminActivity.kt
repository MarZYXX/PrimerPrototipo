package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario

class AdminActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var btnGestionarCuenta: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var tvAutobusesRuta: TextView
    private lateinit var tvCantidadAutobuses: TextView

    private lateinit var usuarioActual: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()
        leerDatosIntent()
        setupListeners()
        cargarDatosAdmin()
    }

    private fun initViews() {
        tvTitulo = findViewById(R.id.busView)
        btnGestionarCuenta = findViewById(R.id.manageAcc)
        btnCerrarSesion = findViewById(R.id.admincerrar)
        tvAutobusesRuta = findViewById(R.id.busView)
        tvCantidadAutobuses = findViewById(R.id.textView2)
    }

    private fun leerDatosIntent() {
        usuarioActual = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }

        tvTitulo.text = "Panel Admin: ${usuarioActual.nombre}"
    }

    private fun cargarDatosAdmin() {
        // Datos de ejemplo para el dashboard del admin
        tvCantidadAutobuses.text = "8" // 8 autobuses en ruta
    }

    private fun setupListeners() {
        btnGestionarCuenta.setOnClickListener {
            irAGestionCuentas()
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun irAGestionCuentas() {
        val intent = Intent(this, GestionCuentasActivity::class.java)
        intent.putExtra("USUARIO_ACTUAL", usuarioActual)
        startActivity(intent)
    }

    private fun cerrarSesion() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}