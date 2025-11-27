package com.example.primerprototipo.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Terminal
import com.example.primerprototipo.model.UbicacionAutobus
import com.example.primerprototipo.viewmodel.MapaViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var spinnerRuta: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCerrarSesion: Button

    private lateinit var viewModel: MapaViewModel

    private var busMarkers = mutableMapOf<String, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_ruta)

        viewModel = ViewModelProvider(this)[MapaViewModel::class.java]

        spinnerRuta = findViewById(R.id.spinnerRuta)
        progressBar = findViewById(R.id.progressBarMapa)
        btnCerrarSesion = findViewById(R.id.closeMapSesion)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupSpinner()
        setupObservers()

        btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val misantla = LatLng(19.9319, -96.8461)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(misantla, 10f))
    }

    private fun setupSpinner() {
        val rutas = Terminal.values().map { it.nombreCompleto }.toMutableList()
        rutas.add(0, "Selecciona una ruta para ver")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rutas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRuta.adapter = adapter

        spinnerRuta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) return
                progressBar.visibility = View.VISIBLE
                val terminalSeleccionada = Terminal.values()[position - 1]
                viewModel.seleccionarRuta(terminalSeleccionada)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        viewModel.rutaDibujable.observe(this) { ruta ->
            progressBar.visibility = View.GONE
            if (ruta != null && ::mMap.isInitialized) {
                dibujarRutaYParadas(ruta, viewModel.paradasDeLaRuta.value ?: emptyList())
            }
        }

        viewModel.autobusesEnRuta.observe(this) { autobuses ->
            if (::mMap.isInitialized) {
                actualizarMarcadoresDeAutobuses(autobuses)
            }
        }

        viewModel.mensajeError.observe(this) { error ->
            progressBar.visibility = View.GONE
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun dibujarRutaYParadas(ruta: List<LatLng>, paradas: List<com.example.primerprototipo.model.Parada>) {
        mMap.clear()
        val polylineOptions = PolylineOptions().color(Color.BLUE).width(12f).addAll(ruta)
        mMap.addPolyline(polylineOptions)

        paradas.forEach {
            mMap.addMarker(MarkerOptions().position(LatLng(it.latitud, it.longitud)).title(it.nombre))
        }

        val boundsBuilder = LatLngBounds.Builder()
        ruta.forEach { boundsBuilder.include(it) }
        spinnerRuta.post {
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150))
            } catch (e: IllegalStateException) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ruta.first(), 12f))
            }
        }
    }

    private fun actualizarMarcadoresDeAutobuses(autobuses: List<UbicacionAutobus>) {
        val autobusIdsActuales = autobuses.map { it.autobusId }

        val marcadoresAEliminar = busMarkers.keys.filterNot { autobusIdsActuales.contains(it) }
        marcadoresAEliminar.forEach {
            busMarkers.remove(it)?.remove()
        }

        autobuses.forEach { autobus ->
            val pos = LatLng(autobus.latitud, autobus.longitud)
            if (busMarkers.containsKey(autobus.autobusId)) {
                busMarkers[autobus.autobusId]?.position = pos
            } else {
                val nuevoMarcador = mMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("Autobús #${autobus.autobusId}")
                        .snippet("Próxima parada: ${autobus.proximaParada}")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker))
                )
                if (nuevoMarcador != null) {
                    busMarkers[autobus.autobusId] = nuevoMarcador
                }
            }
        }
    }
}
