package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R

class TempMove : AppCompatActivity() {

    private lateinit var btnUsuario: Button
    private lateinit var btnChofer: Button
    private lateinit var btnAdmin: Button
    private lateinit var btnSuperAdmin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)

         btnUsuario = findViewById(R.id.usuario)
        btnChofer = findViewById(R.id.chofer)
        btnAdmin = findViewById(R.id.admin)
        btnSuperAdmin = findViewById(R.id.superadmin)

         btnUsuario.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
        }

        btnChofer.setOnClickListener {
            val intent = Intent(this, ChoferActivity::class.java)
            startActivity(intent)
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        btnSuperAdmin.setOnClickListener {
            val intent = Intent(this, SuperAdminActivity::class.java)
            startActivity(intent)
        }
    }
}
