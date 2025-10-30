package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario

class SuperAdminActivity : AppCompatActivity() {

    private lateinit var usuarioActual: Usuario
    private lateinit var btnCerrar: Button
    private lateinit var btnGestionarCuenta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_superadmin)

        usuarioActual = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }
        btnGestionarCuenta = findViewById(R.id.gestionarCuenta)
        btnCerrar = findViewById(R.id.closeSuperAdmin)

        btnGestionarCuenta.setOnClickListener {
            val intent = Intent(this, GestionCuentasActivity::class.java)
            intent.putExtra("USUARIO_ACTUAL", usuarioActual)
            startActivity(intent)
        }

        btnCerrar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}