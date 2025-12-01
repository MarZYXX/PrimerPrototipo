package com.example.primerprototipo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.primerprototipo.model.AsignacionChofer
import com.example.primerprototipo.model.Chofer
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.repository.ChoferRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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

    fun crearUsuario(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        correo: String,
        contrasena: String,
        rol: Role,
        creador: Usuario,
        numeroLicencia: String?,
        telefonoEmergencia: String?
    ) {
        if (!puedeCrear(creador, rol)) {
            _mensaje.value = "No tienes permiso para crear este tipo de usuario"
            return
        }

        if (rol == Role.Chofer && (numeroLicencia.isNullOrBlank() || telefonoEmergencia.isNullOrBlank())) {
            _mensaje.value = "Para un chofer, el número de licencia y teléfono de emergencia son obligatorios."
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

        secondaryAuth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                val userMap = hashMapOf(
                    "id" to uid,
                    "nombre" to nombre,
                    "apellidoPaterno" to apellidoPaterno,
                    "apellidoMaterno" to apellidoMaterno,
                    "correo" to correo,
                    "rol" to rol.name
                )

                db.collection("usuarios").document(uid).set(userMap)
                    .addOnSuccessListener {
                        if (rol == Role.Chofer) {
                            guardarDatosExtraChofer(uid, nombre, apellidoPaterno, apellidoMaterno, numeroLicencia!!, telefonoEmergencia!!)
                        } else {
                            _isLoading.value = false
                            _mensaje.value = "Usuario creado con éxito"
                            _operacionExitosa.value = true
                        }
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
        actualizador: Usuario,
        numeroLicencia: String?,
        telefonoEmergencia: String?
    ) {
        if (!puedeEditar(actualizador, usuarioOriginal, nuevoRol)) {
            _mensaje.value = "No tienes permiso para modificar este usuario"
            return
        }

        if (nuevoRol == Role.Chofer && (numeroLicencia.isNullOrBlank() || telefonoEmergencia.isNullOrBlank())) {
            _mensaje.value = "Para un chofer, el número de licencia y teléfono de emergencia son obligatorios."
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
                // Si el rol nuevo es Chofer, creamos o actualizamos sus datos extra
                if (nuevoRol == Role.Chofer) {
                    guardarDatosExtraChofer(usuarioOriginal.id, nuevoNombre, nuevoApellidoPaterno, nuevoApellidoMaterno, numeroLicencia!!, telefonoEmergencia!!)
                }
                // Si antes era Chofer y ahora no, eliminamos sus datos extra
                else if (usuarioOriginal.rol == Role.Chofer && nuevoRol != Role.Chofer) {
                    eliminarDatosExtraChofer(usuarioOriginal.id)
                    _isLoading.value = false
                    _mensaje.value = "Usuario actualizado y datos de chofer eliminados."
                    _operacionExitosa.value = true
                }
                // Si no es ni era chofer, simplemente terminamos
                else {
                    _isLoading.value = false
                    _mensaje.value = "Usuario actualizado con éxito"
                    _operacionExitosa.value = true
                }

                // Refrescar objeto local
                _usuarioEncontrado.value = usuarioOriginal.copy(nombre = nuevoNombre, apellidoPaterno = nuevoApellidoPaterno, apellidoMaterno = nuevoApellidoMaterno, rol = nuevoRol)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _mensaje.value = "Error al actualizar: ${e.message}"
            }
    }

    private fun guardarDatosExtraChofer(
        id: String,
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        numeroLicencia: String,
        telefonoEmergencia: String
    ) {
        viewModelScope.launch {
            val nuevoChofer = Chofer(
                id = id,
                nombre = nombre,
                apellidoPaterno = apellidoPaterno,
                apellidoMaterno = apellidoMaterno,
                numeroLicencia = numeroLicencia,
                telefonoEmergencia = telefonoEmergencia,
                estado = "Activo" // Estado por defecto
            )

            val resultado = ChoferRepository.guardarChofer(nuevoChofer)
            resultado.onSuccess {
                _isLoading.value = false
                _mensaje.value = "Datos de Usuario y Chofer guardados con éxito."
                _operacionExitosa.value = true
            }.onFailure {
                _isLoading.value = false
                _mensaje.value = "Usuario guardado, pero falló al guardar datos de chofer: ${it.message}"
            }
        }
    }

    private fun eliminarDatosExtraChofer(choferId: String) {
        db.collection("choferes").document(choferId).delete()
        db.collection("chofer de autobus").document(choferId).delete()
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
                if (usuario.rol == Role.Chofer) {
                    eliminarDatosExtraChofer(usuario.id)
                }
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
            Role.Admin -> rolNuevo != Role.SuperAdmin
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
                _mensaje.value = "Autobus $autobusID asignado con éxito"
                asignacionBus.value = autobusID
                _operacionExitosa.value = true
            }
            .addOnFailureListener{
                _isLoading.value = false
                _mensaje.value = "Error al asignar autobus: ${it.message}"
            }
    }

    fun obtenerAsignacion(choferId: String) {
        db.collection("chofer de autobus").document(choferId).get()
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