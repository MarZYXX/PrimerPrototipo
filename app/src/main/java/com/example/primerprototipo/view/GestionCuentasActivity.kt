package com.example.primerprototipo.view

import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.primerprototipo.R
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.viewmodel.GestionarCuentasViewModel

class GestionCuentasActivity : AppCompatActivity() {

    private lateinit var viewModel: GestionarCuentasViewModel

    // Views
    private lateinit var etCorreoBusqueda: EditText
    private lateinit var etNombre: EditText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar)

         usuarioActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USUARIO_ACTUAL", Usuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("USUARIO_ACTUAL") as Usuario
        }

        initViewModel()

        etCorreoBusqueda = findViewById(R.id.editTextCorreo)
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        spinnerRol = findViewById(R.id.spinnerRol)
        btnBuscar = findViewById(R.id.mostrarCuenta)
        btnCrear = findViewById(R.id.btnCrear)
        btnActualizar = findViewById(R.id.btnActualizar)
        btnEliminar = findViewById(R.id.btnEliminar)
        tvMensaje = findViewById(R.id.tvMensaje)
        btnFinalizar = findViewById(R.id.btnFinalizar)

        val esSuperAdmin = usuarioActual.rol == Role.SuperAdmin

        viewModel.cargarRolesDisponibles(esSuperAdmin)

        btnBuscar.setOnClickListener {
            val correo = etCorreoBusqueda.text.toString().trim()
            viewModel.buscarUsuario(correo)
        }

        btnCrear.setOnClickListener {
            crearUsuario()
        }

        btnActualizar.setOnClickListener {
            actualizarUsuario()
        }

        btnEliminar.setOnClickListener {
            eliminarUsuario()
        }

        btnFinalizar.setOnClickListener {
            finish()
        }

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
        }

        viewModel.mensaje.observe(this) { mensaje ->
            tvMensaje.text = mensaje
        }

        viewModel.operacionExitosa.observe(this) { exitoso ->
            if (exitoso) {
                Toast.makeText(this, "Operación completada", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupSpinnerRoles(roles: List<Role>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapter

        if (usuarioEditando == null) {
            val defaultRole = if (roles.contains(Role.Usuario)) Role.Usuario else roles.first()
            val position = roles.indexOf(defaultRole)
            if (position >= 0) {
                spinnerRol.setSelection(position)
            }
        }
    }

    private fun cargarDatosUsuario(usuario: Usuario) {
        etNombre.setText(usuario.nombre)
        etCorreo.setText(usuario.correo)
        etContrasena.setText("")

        val rolActual = usuario.rol
        val adapter = spinnerRol.adapter as ArrayAdapter<Role>
        val position = adapter.getPosition(rolActual)
        if (position >= 0) {
            spinnerRol.setSelection(position)
        }
    }

    private fun limpiarFormulario() {
        etNombre.setText("")
        etCorreo.setText("")
        etContrasena.setText("")
        usuarioEditando = null
    }

    private fun crearUsuario() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val rol = spinnerRol.selectedItem as Role

        viewModel.crearUsuario(nombre, correo, contrasena, rol, usuarioActual)
    }

    private fun actualizarUsuario() {
        val usuario = usuarioEditando ?: return
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val rol = spinnerRol.selectedItem as Role

        viewModel.actualizarUsuario(
            usuario,
            nombre,
            correo,
            contrasena.ifEmpty { null },
            rol,
            usuarioActual
        )
    }

    private fun eliminarUsuario() {
        val usuario = usuarioEditando ?: return
        viewModel.eliminarUsuario(usuario, usuarioActual)
    }
}
