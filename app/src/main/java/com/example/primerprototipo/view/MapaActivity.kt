package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Usuario
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var btnRastrear: Button
    private lateinit var tvInfoBus: TextView
    private lateinit var usuarioActual: Usuario
    private lateinit var closeMapSesion: Button
    private lateinit var spinnerRuta: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_ruta)

        usuarioActual = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }
        btnRastrear = findViewById(R.id.btnRastrear)
        tvInfoBus = findViewById(R.id.tvInfoBus)
        closeMapSesion = findViewById(R.id.closeMapSesion)
        spinnerRuta = findViewById(R.id.spinnerRuta)

        // Configurar el spinner
        setupSpinner()

        mapFrag()

        btnRastrear.setOnClickListener {
            rastrearBus()
        }

        closeMapSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupSpinner() {
        val rutas = arrayOf("Misantla-Martínez", "Martínez-Misantla")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rutas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRuta.adapter = adapter
        spinnerRuta.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
        when (position) {
            0 -> tvInfoBus.text = "Bus más cercano: En ruta hacia Martínez"
            1 -> tvInfoBus.text = "Bus más cercano: En ruta hacia Misantla"
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }

    private fun rastrearBus() {
        val rutaSeleccionada = spinnerRuta.selectedItemPosition
        when (rutaSeleccionada) {
            0 -> tvInfoBus.text = "Bus más cercano: Arroyo Hondo - En ruta hacia Martínez"
            1 -> tvInfoBus.text = "Bus más cercano: Santa Clara - En ruta hacia Misantla"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val misantla = LatLng(19.93, -96.85)
        mMap.addMarker(MarkerOptions().position(misantla).title("Autobús en Misantla"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(misantla, 13f))
    }

    private fun mapFrag(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
}