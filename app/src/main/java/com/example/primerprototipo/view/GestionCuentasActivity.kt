package com.example.primerprototipo.view

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.GestionarCuentasViewModel

class GestionCuentasActivity : AppCompatActivity() {

    private lateinit var viewModel: GestionarCuentasViewModel
    private lateinit var etCorreoBusqueda: EditText
    private lateinit var etNombre: EditText
    private lateinit var etApellidoPaterno: EditText
    private lateinit var etApellidoMaterno: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var spinnerRol: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var btnCrear: Button
    private lateinit var btnActualizar: Button
    private lateinit var btnEliminar: Button
    private lateinit var tvMensaje: TextView
    private lateinit var btnFinalizar: Button

    private lateinit var usuarioActual: Usuario
    private var usuarioEditando: Usuario? = null

    // --- Vistas para Chofer ---
    private lateinit var layoutDatosChofer: LinearLayout
    private lateinit var etNumeroLicencia: EditText
    private lateinit var etTelefonoEmergencia: EditText
    private lateinit var layoutAsignacionBus: LinearLayout
    private lateinit var etBusId: EditText
    private lateinit var btnAsignarBus: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar)

        usuarioActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }

        initViews()
        initViewModel()

        val esSuperAdmin = usuarioActual.rol == Role.SuperAdmin
        viewModel.cargarRolesDisponibles(esSuperAdmin)

        setupListeners()
    }

    private fun initViews() {
        etCorreoBusqueda = findViewById(R.id.editTextCorreo)
        etNombre = findViewById(R.id.etNombre)
        etApellidoPaterno = findViewById(R.id.etApellidoPaterno)
        etApellidoMaterno = findViewById(R.id.etApellidoMaterno)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        spinnerRol = findViewById(R.id.spinnerRol)
        btnBuscar = findViewById(R.id.mostrarCuenta)
        btnCrear = findViewById(R.id.btnCrear)
        btnActualizar = findViewById(R.id.btnActualizar)
        btnEliminar = findViewById(R.id.btnEliminar)
        tvMensaje = findViewById(R.id.tvMensaje)
        btnFinalizar = findViewById(R.id.btnFinalizar)

        // Vistas de Chofer
        layoutDatosChofer = findViewById(R.id.layoutDatosChofer)
        etNumeroLicencia = findViewById(R.id.etNumeroLicencia)
        etTelefonoEmergencia = findViewById(R.id.etTelefonoEmergencia)
        layoutAsignacionBus = findViewById(R.id.layoutAsignacionBus)
        etBusId = findViewById(R.id.etBusId)
        btnAsignarBus = findViewById(R.id.btnAsignarBus)

        btnActualizar.isEnabled = false
        btnEliminar.isEnabled = false
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[GestionarCuentasViewModel::class.java]

        viewModel.rolesDisponibles.observe(this) { roles ->
            setupSpinnerRoles(roles)
        }

        viewModel.usuarioEncontrado.observe(this) { usuario ->
            usuarioEditando = usuario
            if (usuario != null) {
                cargarDatosUsuario(usuario)
                btnCrear.isEnabled = false
                btnActualizar.isEnabled = true
                btnEliminar.isEnabled = true
            } else {
                limpiarFormulario()
                btnCrear.isEnabled = true
                btnActualizar.isEnabled = false
                btnEliminar.isEnabled = false
            }
            actualizarVisibilidadChofer(spinnerRol.selectedItem as? Role)
        }

        viewModel.mensaje.observe(this) { mensaje ->
            tvMensaje.text = mensaje
        }

        viewModel.operacionExitosa.observe(this) { exitoso ->
            if (exitoso) {
                Toast.makeText(this, "Operación completada", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.busAsignado.observe(this){busId ->
            etBusId.setText(busId ?: "")
        }
    }

    private fun setupListeners() {
        btnBuscar.setOnClickListener {
            val correo = etCorreoBusqueda.text.toString().trim()
            if (correo.isNotEmpty()) viewModel.buscarUsuario(correo)
        }
        btnCrear.setOnClickListener { crearUsuario() }
        btnActualizar.setOnClickListener { actualizarUsuario() }
        btnEliminar.setOnClickListener { eliminarUsuario() }
        btnFinalizar.setOnClickListener { finish() }

        // CONECTAMOS EL BOTÓN DE ASIGNAR BUS
        btnAsignarBus.setOnClickListener {
            val busId = etBusId.text.toString().trim()
            if (usuarioEditando != null && busId.isNotEmpty()) {
                viewModel.asignarAutobus(usuarioEditando!!.id, busId)
            } else {
                Toast.makeText(this, "Primero busca a un chofer y después especifica un Bus ID.", Toast.LENGTH_LONG).show()
            }
        }

        spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val rolSeleccionado = parent?.getItemAtPosition(position) as? Role
                actualizarVisibilidadChofer(rolSeleccionado)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun actualizarVisibilidadChofer(rol: Role?) {
        val esChofer = rol == Role.Chofer
        layoutDatosChofer.visibility = if (esChofer) View.VISIBLE else View.GONE
        layoutAsignacionBus.visibility = if (esChofer && usuarioEditando != null) View.VISIBLE else View.GONE

        if (esChofer && usuarioEditando != null) {
            viewModel.obtenerAsignacion(usuarioEditando!!.id)
        }
    }

    private fun setupSpinnerRoles(roles: List<Role>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapter

        if (usuarioEditando == null) {
            val defaultRole = if (roles.contains(Role.Usuario)) Role.Usuario else roles.first()
            val position = roles.indexOf(defaultRole)
            if (position >= 0) spinnerRol.setSelection(position)
        }
    }

    private fun cargarDatosUsuario(usuario: Usuario) {
        etNombre.setText(usuario.nombre)
        etApellidoPaterno.setText(usuario.apellidoPaterno)
        etApellidoMaterno.setText(usuario.apellidoMaterno)
        etCorreo.setText(usuario.correo)
        etContrasena.setText("")

        val rolActual = usuario.rol
        val adapter = spinnerRol.adapter as ArrayAdapter<Role>
        val position = adapter.getPosition(rolActual)
        if (position >= 0) spinnerRol.setSelection(position)

        etCorreo.isEnabled = false
        etContrasena.isEnabled = false
        etContrasena.hint = "No se puede editar"
    }

    private fun limpiarFormulario() {
        etNombre.setText("")
        etApellidoPaterno.setText("")
        etApellidoMaterno.setText("")
        etCorreo.setText("")
        etContrasena.setText("")
        etNumeroLicencia.setText("")
        etTelefonoEmergencia.setText("")
        usuarioEditando = null

        etCorreo.isEnabled = true
        etContrasena.isEnabled = true
        etContrasena.hint = "Contraseña"
        btnCrear.isEnabled = true
        btnActualizar.isEnabled = false
        btnEliminar.isEnabled = false

        actualizarVisibilidadChofer(spinnerRol.selectedItem as? Role)
    }

    private fun crearUsuario() {
        val nombre = etNombre.text.toString().trim()
        val apellidoPaterno = etApellidoPaterno.text.toString().trim()
        val apellidoMaterno = etApellidoMaterno.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val rol = spinnerRol.selectedItem as Role

        val numeroLicencia = etNumeroLicencia.text.toString().trim()
        val telefonoEmergencia = etTelefonoEmergencia.text.toString().trim()

        viewModel.crearUsuario(
            nombre, apellidoPaterno, apellidoMaterno, correo,
            contrasena, rol, usuarioActual,
            numeroLicencia.takeIf { rol == Role.Chofer },
            telefonoEmergencia.takeIf { rol == Role.Chofer }
        )
    }

    private fun actualizarUsuario() {
        val usuario = usuarioEditando ?: return
        val nombre = etNombre.text.toString().trim()
        val apellidoPaterno = etApellidoPaterno.text.toString().trim()
        val apellidoMaterno = etApellidoMaterno.text.toString().trim()
        val rol = spinnerRol.selectedItem as Role

        // --- RECOGEMOS LOS DATOS DEL CHOFER ---
        val numeroLicencia = etNumeroLicencia.text.toString().trim()
        val telefonoEmergencia = etTelefonoEmergencia.text.toString().trim()

        viewModel.actualizarUsuario(
            usuarioOriginal = usuario,
            nuevoNombre = nombre,
            nuevoApellidoPaterno = apellidoPaterno,
            nuevoApellidoMaterno = apellidoMaterno,
            nuevoRol = rol,
            actualizador = usuarioActual,
            // --- Y LOS PASAMOS AL VIEWMODEL ---
            numeroLicencia = numeroLicencia.takeIf { rol == Role.Chofer },
            telefonoEmergencia = telefonoEmergencia.takeIf { rol == Role.Chofer }
        )
    }

    private fun eliminarUsuario() {
        val usuario = usuarioEditando ?: return
        viewModel.eliminarUsuario(usuario, usuarioActual)
    }
}