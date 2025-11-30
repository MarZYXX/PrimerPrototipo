package com.example.primerprototipo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.primerprototipo.model.AsignacionChofer
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GestionarCuentasViewModel(application: Application) : AndroidViewModel(application) {

    private val _usuarioEncontrado = MutableLiveData<Usuario?>()
    val usuarioEncontrado: LiveData<Usuario?> = _usuarioEncontrado

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _operacionExitosa = MutableLiveData<Boolean>()
    val operacionExitosa: LiveData<Boolean> = _operacionExitosa

    private val _rolesDisponibles = MutableLiveData<List<Role>>()
    val rolesDisponibles: LiveData<List<Role>> = _rolesDisponibles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val db = FirebaseFirestore.getInstance()

    private val asignacionBus = MutableLiveData<String?>()
    val busAsignado: LiveData<String?> = asignacionBus


    fun buscarUsuario(correo: String) {
        _isLoading.value = true

        db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                _isLoading.value = false
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val rolString = doc.getString("rol") ?: "Usuario"
                    val rolEnum = try { Role.valueOf(rolString) } catch (e: Exception) { Role.Usuario }

                    val usuario = Usuario(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        apellidoPaterno = doc.getString("apellidoPaterno") ?: "",
                        apellidoMaterno = doc.getString("apellidoMaterno") ?: "",
                        correo = doc.getString("correo") ?: "",
                        rol = rolEnum
                    )
                    _usuarioEncontrado.value = usuario
                    _mensaje.value = "Usuario encontrado"
                } else {
                    _usuarioEncontrado.value = null
                    _mensaje.value = "Usuario no encontrado en la base de datos"
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
                _mensaje.value = "Error al buscar: ${it.message}"
            }
    }

    fun cargarRolesDisponibles(esSuperAdmin: Boolean) {
        val roles = if (esSuperAdmin) {
            listOf(Role.Usuario, Role.Chofer, Role.Admin, Role.SuperAdmin)
        } else {
            listOf(Role.Usuario, Role.Chofer, Role.Admin)
        }
        _rolesDisponibles.value = roles
    }

    fun crearUsuario(nombre: String, correo: String, apellidoPaterno: String, apellidoMaterno: String, contrasena: String, rol: Role, creador: Usuario) {
        if (!puedeCrear(creador, rol)) {
            _mensaje.value = "No tienes permiso para crear este tipo de usuario"
            return
        }

        _isLoading.value = true
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(FirebaseApp.getInstance().options.apiKey)
            .setApplicationId(FirebaseApp.getInstance().options.applicationId)
            .setProjectId(FirebaseApp.getInstance().options.projectId)
            .build()

        val appName = "SecondaryAppForCreation"
        val secondaryApp = try {
            FirebaseApp.getInstance(appName)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(getApplication(), firebaseOptions, appName)
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        // Crear en Auth
        secondaryAuth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                //  datos para Firestore DB
                val userMap = hashMapOf(
                    "id" to uid,
                    "nombre" to nombre,
                    "apellidoPaterno" to apellidoPaterno,
                    "apellidoMaterno" to apellidoMaterno,
                    "correo" to correo,
                    "rol" to rol.name
                )

                // Guardar en DB
                db.collection("usuarios").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _mensaje.value = "Usuario creado con éxito"
                        _operacionExitosa.value = true
                        secondaryAuth.signOut()
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _mensaje.value = "Creado en Auth pero falló en DB: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _mensaje.value = "Error creando cuenta: ${e.message}"
            }
    }

    fun actualizarUsuario(
        usuarioOriginal: Usuario,
        nuevoNombre: String,
        nuevoApellidoPaterno: String,
        nuevoApellidoMaterno: String,
        nuevoRol: Role,
        actualizador: Usuario
    ) {
        if (!puedeEditar(actualizador, usuarioOriginal, nuevoRol)) {
            _mensaje.value = "No tienes permiso para modificar este usuario"
            return
        }

        _isLoading.value = true

        val updates = mapOf(
            "nombre" to nuevoNombre,
            "apellidoPaterno" to nuevoApellidoPaterno,
            "apellidoMaterno" to nuevoApellidoMaterno,
            "rol" to nuevoRol.name
        )

        db.collection("usuarios").document(usuarioOriginal.id)
            .update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _mensaje.value = "Usuario actualizado con éxito"
                _operacionExitosa.value = true
                // Refrescar objeto local
                _usuarioEncontrado.value = usuarioOriginal.copy(nombre = nuevoNombre, apellidoPaterno = nuevoApellidoPaterno, apellidoMaterno = nuevoApellidoMaterno, rol = nuevoRol)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _mensaje.value = "Error al actualizar: ${e.message}"
            }
    }

    fun eliminarUsuario(usuario: Usuario, eliminador: Usuario) {
        if (!puedeEliminar(eliminador, usuario)) {
            _mensaje.value = "No tienes permiso para eliminar este usuario"
            return
        }

        _isLoading.value = true

        db.collection("usuarios").document(usuario.id)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                _mensaje.value = "Registro eliminado de la base de datos"
                _operacionExitosa.value = true
                _usuarioEncontrado.value = null
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _mensaje.value = "Error al eliminar: ${e.message}"
            }
    }

    private fun puedeCrear(creador: Usuario, rolNuevo: Role): Boolean {
        return when (creador.rol) {
            Role.SuperAdmin -> true
            Role.Admin -> rolNuevo != Role.SuperAdmin // Admin no puede crear SuperAdmin
            else -> false
        }
    }

    private fun puedeEditar(actualizador: Usuario, usuario: Usuario, rolNuevo: Role): Boolean {
        return when (actualizador.rol) {
            Role.SuperAdmin -> true
            Role.Admin -> usuario.rol != Role.SuperAdmin && rolNuevo != Role.SuperAdmin
            else -> false
        }
    }

    private fun puedeEliminar(eliminador: Usuario, usuario: Usuario): Boolean {
        return when (eliminador.rol) {
            Role.SuperAdmin -> true
            Role.Admin -> usuario.rol != Role.SuperAdmin
            else -> false
        }
    }

    fun asignarAutobus(choferId: String, autobusID: String){
        _isLoading.value = true

        val asignacion = AsignacionChofer(choferId, autobusID)

        db.collection("chofer de autobus").document(choferId)
            .set(asignacion)
            .addOnSuccessListener {
                _isLoading.value = false
                _mensaje.value = "Autobus $autobusID con éxito"
                asignacionBus.value = autobusID
            }
            .addOnFailureListener{
                _isLoading.value = false
                _mensaje.value = "Error al asignar autobus: ${it.message}"
        }
    }

    fun obtenerAsignacion(choferId: String) {
        db.collection("chofer_de_autobus").document(choferId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val busId = document.getString("autobusId")
                    asignacionBus.value = busId
                } else {
                    asignacionBus.value = null
                }
            }
    }
}
