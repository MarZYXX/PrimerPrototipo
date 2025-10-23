package com.example.primerprototipo.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnRastrear: Button
    private lateinit var tvInfoBus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_ruta)

        btnRastrear = findViewById(R.id.btnRastrear)
        tvInfoBus = findViewById(R.id.tvInfoBus)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnRastrear.setOnClickListener {
            tvInfoBus.text = "Bus más cercano: En ruta hacia Martínez"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val misantla = LatLng(19.93, -96.85)
        mMap.addMarker(MarkerOptions().position(misantla).title("Autobús en Misantla"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(misantla, 13f))
    }
}
