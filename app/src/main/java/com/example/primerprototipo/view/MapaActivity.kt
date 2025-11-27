package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R
import com.example.primerprototipo.model.RutasMisantla
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
    private lateinit var imageViewMapa: ImageView

    private var autobusesMarkers = mutableMapOf<String, Marker>()
    private var rutaSeleccionada = ""

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
        imageViewMapa = findViewById(R.id.imageView3)
        imageViewMapa.visibility = android.view.View.VISIBLE

         spinner()

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

        LocationRepository.iniciarEscuchaUbicaciones()

        // Observar cambios en ubicaciones
        LocationRepository.ubicaciones.observe(this) { ubicaciones ->
            actualizarMarkersEnMapa(ubicaciones)
            actualizarInfoBusMasCercano()
        }
    }

    private fun spinner() {
        val rutas = arrayOf(
            "Misantla - Martinez de la Torre",
            "Martinez de la Torre - Misantla"
        )
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
            0 -> tvInfoBus.text = "Bus más cercano: Arroyo Hondo"
            1 -> tvInfoBus.text = "Bus más cercano: Santa Clara"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarMapa()
    }

    private fun configurarMapa() {
        // Configuración inicial del mapa
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Centrar en Misantla por defecto
        val misantla = LatLng(19.9319, -96.8461)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(misantla, 12f))
    }

    private fun actualizarMarkersEnMapa(ubicaciones: Map<String, UbicacionAutobus>) {
        // Remover markers antiguos
        autobusesMarkers.values.forEach { it.remove() }
        autobusesMarkers.clear()

        // Agregar nuevos markers
        ubicaciones.values.forEach { ubicacion ->
            if (ubicacion.ruta == rutaSeleccionada) {
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(ubicacion.latitud, ubicacion.longitud))
                        .title("Autobús ${ubicacion.autobusId}")
                        .snippet("Ruta: ${ubicacion.ruta}\nPasajeros: ${ubicacion.pasajerosAbordo}")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker))
                )
                marker?.let { autobusesMarkers[ubicacion.autobusId] = it }
            }
        }
    }

    private fun actualizarInfoBusMasCercano() {
        if (rutaSeleccionada.isNotEmpty()) {
            // Usar ubicación del usuario (por ahora usar Misantla como ejemplo)
            val userLat = 19.9319
            val userLng = -96.8461

            val autobusCercano = LocationRepository.obtenerAutobusMasCercano(
                userLat, userLng, rutaSeleccionada
            )

            autobusCercano?.let { bus ->
                val distancia = LocationRepository.calcularDistancia(
                    userLat, userLng, bus.latitud, bus.longitud
                )

                tvInfoBus.text = "Bus más cercano: A ${"%.1f".format(distancia)} km - ${bus.proximaParada}"
            } ?: run {
                tvInfoBus.text = "Bus más cercano: No disponible"
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
        rutaSeleccionada = when (position) {
            0 -> "Misantla - Martinez de la Torre"
            1 -> "Martinez de la Torre - Misantla"
            else -> ""
        }

        // Filtrar por ruta seleccionada
        LocationRepository.iniciarEscuchaUbicaciones(rutaSeleccionada)
        actualizarInfoBusMasCercano()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationRepository.detenerEscucha()
    }

    private fun mapFrag(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
}