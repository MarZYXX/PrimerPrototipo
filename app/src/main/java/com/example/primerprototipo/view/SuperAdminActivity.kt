package com.example.primerprototipo.view

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R

class SuperAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_superadmin)

        val btnGestionarCuenta: Button = findViewById(R.id.gestionarCuenta)

        btnGestionarCuenta.setOnClickListener {
            Toast.makeText(this, "bajo mantenimiento", Toast.LENGTH_SHORT).show()
        }
    }
}
