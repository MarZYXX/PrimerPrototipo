package com.example.primerprototipo.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.model.LocationForegroundService
import com.example.primerprototipo.model.Terminal
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.ChoferViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class ChoferActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var tvTituloChofer: TextView
    private lateinit var spinnerTerminal: Spinner
    private lateinit var tvNombreRuta: TextView
    private lateinit var tvTerminalDestino: TextView
    private lateinit var spinnerHorario: Spinner
    private lateinit var tvNumPasajeros: TextView
    private lateinit var tvProximaParada: TextView
    private lateinit var tvCapacidadDisponible: TextView
    private lateinit var btnAgregarPasajero: Button
    private lateinit var btnQuitarPasajero: Button
    private lateinit var btnSiguienteParada: Button
    private lateinit var btnParadaAnterior: Button
    private lateinit var btnFinalizarRuta: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var layoutControles: View

    private lateinit var viewModel: ChoferViewModel
    private lateinit var usuarioActual: Usuario
    private var rutaConfigurada = false

    private lateinit var mMap: GoogleMap
    private var busMarker: Marker? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chofer)

        initViewModel()
        initViews()
        initMap()
        leerDatosIntent()
        setupObservers()
        inicializarDatos()
        configurarSpinnerTerminal()
        configurarSpinnerHorario()
        setupListeners()
        solicitarPermisosDeUbicacion()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapChofer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Configuración inicial del mapa
        val misantla = LatLng(19.9319, -96.8461)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(misantla, 12f))
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ChoferViewModel::class.java]
    }

    private fun initViews() {
        tvTituloChofer = findViewById(R.id.textChofer)
        spinnerTerminal = findViewById(R.id.spinnerTerminal)
        tvNombreRuta = findViewById(R.id.tvNombreRuta)
        tvTerminalDestino = findViewById(R.id.tvTerminalDestino)
        spinnerHorario = findViewById(R.id.spinnerHorario)
        tvNumPasajeros = findViewById(R.id.noPasajeros)
        tvProximaParada = findViewById(R.id.nextParada)
        tvCapacidadDisponible = findViewById(R.id.tvCapacidadDisponible)
        btnAgregarPasajero = findViewById(R.id.btnAgregarPasajero)
        btnQuitarPasajero = findViewById(R.id.btnQuitarPasajero)
        btnSiguienteParada = findViewById(R.id.btnSiguienteParada)
        btnParadaAnterior = findViewById(R.id.btnParadaAnterior)
        btnFinalizarRuta = findViewById(R.id.btnFinalizarRuta)
        btnCerrarSesion = findViewById(R.id.sesionChofer)
        layoutControles = findViewById(R.id.layoutControles)
        mostrarControles(false)
    }

    private fun leerDatosIntent() {
        usuarioActual = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }
    }

    private fun inicializarDatos() {
        viewModel.inicializarAutobus(usuarioActual.nombre)
        tvTituloChofer.text = "Bienvenido Chofer: ${usuarioActual.nombre}"
    }

    private fun configurarSpinnerTerminal() {
        val terminalesOpciones = mutableListOf("Selecciona terminal de salida")
        terminalesOpciones.addAll(Terminal.values().map { it.nombreCompleto })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terminalesOpciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTerminal.adapter = adapter

        spinnerTerminal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) return
                if (!rutaConfigurada) {
                    val terminalSeleccionada = Terminal.values()[position - 1]
                    viewModel.configurarTerminalSalida(terminalSeleccionada)
                    rutaConfigurada = true
                    mostrarControles(true)
                    spinnerTerminal.isEnabled = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarSpinnerHorario() {
        val horarios = viewModel.obtenerHorariosDisponibles()
        val horariosOpciones = mutableListOf("Selecciona horario")
        horariosOpciones.addAll(horarios)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horariosOpciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHorario.adapter = adapter

        spinnerHorario.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) return
                val horarioSeleccionado = horarios[position - 1]
                viewModel.establecerTiempoSalida(horarioSeleccionado)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun mostrarControles(mostrar: Boolean) {
        layoutControles.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    private fun setupObservers() {
        viewModel.pasajerosAbordo.observe(this) { tvNumPasajeros.text = it.toString() }
        viewModel.proximaParada.observe(this) { tvProximaParada.text = it }
        viewModel.capacidadDisponible.observe(this) { tvCapacidadDisponible.text = it }
        viewModel.nombreRuta.observe(this) { tvNombreRuta.text = it }
        viewModel.terminalDestino.observe(this) { tvTerminalDestino.text = it }
        viewModel.mensaje.observe(this) { if (it.isNotEmpty()) Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }

        viewModel.autobus.observe(this) { autobus ->
            if (::mMap.isInitialized) { // Asegurarse de que el mapa esté listo
                actualizarUbicacionEnMapa(autobus.latitud, autobus.longitud)
            }
        }

        viewModel.accionServicio.observe(this) { accion ->
            when (accion) {
                is ChoferViewModel.AccionServicio.INICIAR -> iniciarServicioDeUbicacion()
                is ChoferViewModel.AccionServicio.DETENER -> detenerServicioDeUbicacion()
            }
        }
    }

    private fun setupListeners() {
        btnAgregarPasajero.setOnClickListener { viewModel.agregarPasajero() }
        btnQuitarPasajero.setOnClickListener { viewModel.quitarPasajero() }
        btnSiguienteParada.setOnClickListener { viewModel.avanzarSiguienteParada() }
        btnParadaAnterior.setOnClickListener { viewModel.retrocederParada() }
        btnFinalizarRuta.setOnClickListener { viewModel.finalizarRuta() }
        btnCerrarSesion.setOnClickListener { cerrarSesion() }
    }

    private fun cerrarSesion() {
        viewModel.finalizarRuta() // También finaliza la ruta al cerrar sesión
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun solicitarPermisosDeUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun iniciarServicioDeUbicacion() {
        val intent = Intent(this, LocationForegroundService::class.java).apply {
            putExtra("CHOFER_ID", usuarioActual.id)
            putExtra("AUTOBUS_ID", viewModel.autobus.value?.numeroUnidad)
            putExtra("RUTA_ID", viewModel.autobus.value?.ruta)
        }
        startService(intent)
    }

    private fun detenerServicioDeUbicacion() {
        stopService(Intent(this, LocationForegroundService::class.java))
    }

    private fun actualizarUbicacionEnMapa(latitud: Double, longitud: Double) {
        val nuevaPosicion = LatLng(latitud, longitud)
        if (busMarker == null) {
            busMarker = mMap.addMarker(MarkerOptions().position(nuevaPosicion).title("Mi Autobús"))
        } else {
            busMarker?.position = nuevaPosicion
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nuevaPosicion, 15f))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido. La lógica para iniciar el servicio ya está en el observer.
            } else {
                Toast.makeText(this, "El permiso de ubicación es necesario para el rastreo.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
