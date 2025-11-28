package com.example.primerprototipo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// We use AndroidViewModel to get 'application' context for the secondary app trick
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

    // 1. BUSCAR USUARIO (READ)
    fun buscarUsuario(correo: String) {
        _isLoading.value = true

        db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                _isLoading.value = false
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    // Convert string role back to Enum safely
                    val rolString = doc.getString("rol") ?: "Usuario"
                    val rolEnum = try { Role.valueOf(rolString) } catch (e: Exception) { Role.Usuario }

                    val usuario = Usuario(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
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

    // 2. CREAR USUARIO (CREATE)
    fun crearUsuario(nombre: String, correo: String, contrasena: String, rol: Role, creador: Usuario) {
        if (!puedeCrear(creador, rol)) {
            _mensaje.value = "No tienes permiso para crear este tipo de usuario"
            return
        }

        _isLoading.value = true

        // TRUCO: Crear una "App Secundaria" de Firebase.
        // Si usamos la instancia normal, al crear un usuario nuevo, Firebase desloguea al Admin automáticamente.
        // Con esto, mantenemos al Admin logueado mientras creamos al otro usuario en segundo plano.
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

                // Preparar datos para Firestore
                val userMap = hashMapOf(
                    "id" to uid,
                    "nombre" to nombre,
                    "correo" to correo,
                    "rol" to rol.name
                )

                // Guardar en Firestore (Usamos la DB principal)
                db.collection("usuarios").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _mensaje.value = "Usuario creado con éxito"
                        _operacionExitosa.value = true

                        // Importante: Cerrar sesión en la app secundaria para limpiar memoria
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

    // 3. ACTUALIZAR USUARIO (UPDATE)
    fun actualizarUsuario(
        usuarioOriginal: Usuario,
        nuevoNombre: String,
        nuevoRol: Role,
        actualizador: Usuario
    ) {
        if (!puedeEditar(actualizador, usuarioOriginal, nuevoRol)) {
            _mensaje.value = "No tienes permiso para modificar este usuario"
            return
        }

        _isLoading.value = true

        // Nota: No actualizamos correo ni contraseña aquí porque requiere re-autenticación compleja.
        // Solo actualizamos Nombre y Rol en la base de datos.
        val updates = mapOf(
            "nombre" to nuevoNombre,
            "rol" to nuevoRol.name
        )

        db.collection("usuarios").document(usuarioOriginal.id)
            .update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _mensaje.value = "Usuario actualizado con éxito"
                _operacionExitosa.value = true
                // Refrescar objeto local
                _usuarioEncontrado.value = usuarioOriginal.copy(nombre = nuevoNombre, rol = nuevoRol)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _mensaje.value = "Error al actualizar: ${e.message}"
            }
    }

    // 4. ELIMINAR USUARIO (DELETE)
    fun eliminarUsuario(usuario: Usuario, eliminador: Usuario) {
        if (!puedeEliminar(eliminador, usuario)) {
            _mensaje.value = "No tienes permiso para eliminar este usuario"
            return
        }

        _isLoading.value = true

        // Solo eliminamos de Firestore. El registro de Auth queda, pero sin documento en DB
        // el usuario no podrá pasar del Login (porque el login busca el rol en DB).
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

    // --- LÓGICA DE PERMISOS ---
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
}
