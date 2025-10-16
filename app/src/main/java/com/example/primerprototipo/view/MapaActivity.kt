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

class MapaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_ruta)

    }
}