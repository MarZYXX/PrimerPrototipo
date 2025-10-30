package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario

class AdminActivity : AppCompatActivity() {

    private lateinit var btnManageAccounts: Button
    private lateinit var txtBusView: TextView
    private lateinit var txtBusCount: TextView
    private lateinit var usuarioActual: Usuario
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        usuarioActual = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }
        btnManageAccounts = findViewById(R.id.manageAcc)
        txtBusView = findViewById(R.id.busView)
        txtBusCount = findViewById(R.id.textView2)
        btnCerrarSesion = findViewById(R.id.admincerrar)

        txtBusCount.text = "5"

        btnManageAccounts.setOnClickListener {
            val intent = Intent(this, GestionCuentasActivity::class.java)
            intent.putExtra("USUARIO_ACTUAL", usuarioActual)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}