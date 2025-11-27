package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Terminal
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.ChoferViewModel

class ChoferActivity : AppCompatActivity() {

    // ============================================
    // VARIABLES DE VISTA (Modularidad)
    // ============================================
    private lateinit var tvTituloChofer: TextView
    private lateinit var tvLabelTerminal: TextView
    private lateinit var spinnerTerminal: Spinner
    private lateinit var tvNombreRuta: TextView
    private lateinit var tvTerminalDestino: TextView
    private lateinit var tvLabelSalida: TextView
    private lateinit var spinnerHorario: Spinner
    private lateinit var tvLabelPasajeros: TextView
    private lateinit var tvNumPasajeros: TextView
    private lateinit var tvLabelProximaParada: TextView
    private lateinit var tvProximaParada: TextView
    private lateinit var tvCapacidadDisponible: TextView
    private lateinit var btnAgregarPasajero: Button
    private lateinit var btnQuitarPasajero: Button
    private lateinit var btnSiguienteParada: Button
    private lateinit var btnParadaAnterior: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var layoutControles: View

    // ViewModel y Usuario
    private lateinit var viewModel: ChoferViewModel
    private lateinit var usuarioActual: Usuario
    private var rutaConfigurada = false

    // ============================================
    // LIFECYCLE
    // ============================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chofer)

        initViewModel()
        initViews()
        leerDatosIntent()
        setupObservers()
        inicializarDatos()
        configurarSpinnerTerminal()
        configurarSpinnerHorario()
        setupListeners()
    }

    // ============================================
    // INICIALIZACIÓN
    // ============================================
    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ChoferViewModel::class.java]
    }

    private fun initViews() {
        tvTituloChofer = findViewById(R.id.textChofer)
        tvLabelTerminal = findViewById(R.id.tvLabelTerminal)
        spinnerTerminal = findViewById(R.id.spinnerTerminal)
        tvNombreRuta = findViewById(R.id.tvNombreRuta)
        tvTerminalDestino = findViewById(R.id.tvTerminalDestino)
        tvLabelSalida = findViewById(R.id.choferSalida)
        spinnerHorario = findViewById(R.id.spinnerHorario)
        tvLabelPasajeros = findViewById(R.id.pasajerosAbordo)
        tvNumPasajeros = findViewById(R.id.noPasajeros)
        tvLabelProximaParada = findViewById(R.id.nextStop)
        tvProximaParada = findViewById(R.id.nextParada)
        tvCapacidadDisponible = findViewById(R.id.tvCapacidadDisponible)
        btnAgregarPasajero = findViewById(R.id.btnAgregarPasajero)
        btnQuitarPasajero = findViewById(R.id.btnQuitarPasajero)
        btnSiguienteParada = findViewById(R.id.btnSiguienteParada)
        btnParadaAnterior = findViewById(R.id.btnParadaAnterior)
        btnCerrarSesion = findViewById(R.id.sesionChofer)
        layoutControles = findViewById(R.id.layoutControles)

        // Ocultar controles hasta que se seleccione terminal
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
        // Crear lista con opción por defecto
        val terminalesOpciones = mutableListOf<String>()
        terminalesOpciones.add("Selecciona terminal de salida")
        terminalesOpciones.addAll(Terminal.values().map { it.nombreCompleto })

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            terminalesOpciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTerminal.adapter = adapter

        // Listener para cambios de selección
        spinnerTerminal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ignorar la primera opción (placeholder)
                if (position == 0) return

                if (!rutaConfigurada) {
                    // position - 1 porque agregamos un elemento al inicio
                    val terminalSeleccionada = Terminal.values()[position - 1]
                    viewModel.configurarTerminalSalida(terminalSeleccionada)
                    rutaConfigurada = true
                    mostrarControles(true)
                    spinnerTerminal.isEnabled = false // Bloquear después de seleccionar
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun configurarSpinnerHorario() {
        // Obtener horarios del ViewModel
        val horarios = viewModel.obtenerHorariosDisponibles()

        // Crear lista con opción por defecto
        val horariosOpciones = mutableListOf<String>()
        horariosOpciones.add("Selecciona horario")
        horariosOpciones.addAll(horarios)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            horariosOpciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHorario.adapter = adapter

        // Listener para cambios de selección
        spinnerHorario.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ignorar la primera opción (placeholder)
                if (position == 0) return

                val horarioSeleccionado = horarios[position - 1]
                viewModel.establecerTiempoSalida(horarioSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun mostrarControles(mostrar: Boolean) {
        layoutControles.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    // ============================================
    // OBSERVERS (LiveData)
    // ============================================
    private fun setupObservers() {
        viewModel.pasajerosAbordo.observe(this) { pasajeros ->
            tvNumPasajeros.text = pasajeros.toString()
        }

        viewModel.proximaParada.observe(this) { parada ->
            tvProximaParada.text = parada
        }

        viewModel.capacidadDisponible.observe(this) { capacidad ->
            tvCapacidadDisponible.text = capacidad
        }

        viewModel.nombreRuta.observe(this) { nombreRuta ->
            tvNombreRuta.text = nombreRuta
        }

        viewModel.terminalDestino.observe(this) { destino ->
            tvTerminalDestino.text = destino
        }

        viewModel.mensaje.observe(this) { mensaje ->
            if (mensaje.isNotEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============================================
    // EVENTOS
    // ============================================
    private fun setupListeners() {
        btnAgregarPasajero.setOnClickListener {
            viewModel.agregarPasajero()
        }

        btnQuitarPasajero.setOnClickListener {
            viewModel.quitarPasajero()
        }

        btnSiguienteParada.setOnClickListener {
            viewModel.avanzarSiguienteParada()
        }

        btnParadaAnterior.setOnClickListener {
            viewModel.retrocederParada()
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    // ============================================
    // NAVEGACIÓN
    // ============================================
    private fun cerrarSesion() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}