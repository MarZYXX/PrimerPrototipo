package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario

class GestionarCuentasViewModel : ViewModel() {

    private val _usuarioEncontrado = MutableLiveData<Usuario?>()
    val usuarioEncontrado: LiveData<Usuario?> = _usuarioEncontrado

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _operacionExitosa = MutableLiveData<Boolean>()
    val operacionExitosa: LiveData<Boolean> = _operacionExitosa

    private val _rolesDisponibles = MutableLiveData<List<Role>>()
    val rolesDisponibles: LiveData<List<Role>> = _rolesDisponibles

    fun buscarUsuario(correo: String) {
        val usuario = UserRepository.findUserByEmail(correo)
        _usuarioEncontrado.value = usuario
        if (usuario == null) {
            _mensaje.value = "Usuario no encontrado"
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

    fun crearUsuario(nombre: String, correo: String, contrasena: String, rol: Role, creador: Usuario) {
        if (!puedeCrear(creador, rol)) {
            _mensaje.value = "No tienes permiso para crear este tipo de usuario"
            return
        }

        if (UserRepository.findUserByEmail(correo) != null) {
            _mensaje.value = "El correo ya está en uso"
            return
        }

        val nuevoUsuario = Usuario(
            id = "user-${System.currentTimeMillis()}",
            nombre = nombre,
            correo = correo,
            contrasena = contrasena,
            rol = rol
        )
        UserRepository.addUser(nuevoUsuario)
        _mensaje.value = "Usuario creado con éxito"
        _operacionExitosa.value = true
    }

    fun actualizarUsuario(
        usuario: Usuario,
        nombre: String,
        correo: String,
        contrasena: String?,
        rol: Role,
        actualizador: Usuario
    ) {
        if (!puedeEditar(actualizador, usuario, rol)) {
            _mensaje.value = "No tienes permiso para modificar este usuario o asignar este rol"
            return
        }

        // Crear una copia actualizada del usuario
        val usuarioActualizado = usuario.copy(
            nombre = nombre,
            correo = correo,
            contrasena = contrasena ?: usuario.contrasena, // Mantener la original si no se provee una nueva
            rol = rol
        )

        // Eliminar el usuario antiguo y agregar la versión actualizada
        UserRepository.eliminarUsuario(usuario.correo)
        UserRepository.addUser(usuarioActualizado)

        _mensaje.value = "Usuario actualizado con éxito"
        _operacionExitosa.value = true
    }

    fun eliminarUsuario(usuario: Usuario, eliminador: Usuario) {
        if (!puedeEliminar(eliminador, usuario)) {
            _mensaje.value = "No tienes permiso para eliminar este usuario"
            return
        }
        UserRepository.eliminarUsuario(usuario.correo)
        _mensaje.value = "Usuario eliminado con éxito"
        _operacionExitosa.value = true
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
}