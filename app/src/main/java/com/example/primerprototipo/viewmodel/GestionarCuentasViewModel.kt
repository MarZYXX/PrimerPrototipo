package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Usuario
import com.example.primerprototipo.model.Role

class GestionarCuentasViewModel : ViewModel() {

    private val _rolesDisponibles = MutableLiveData<List<Role>>()
    val rolesDisponibles: LiveData<List<Role>> = _rolesDisponibles

    private val _usuarioEncontrado = MutableLiveData<Usuario?>()
    val usuarioEncontrado: LiveData<Usuario?> = _usuarioEncontrado

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _operacionExitosa = MutableLiveData<Boolean>()
    val operacionExitosa: LiveData<Boolean> = _operacionExitosa

     fun cargarRolesDisponibles(esSuperAdmin: Boolean) {
        val roles = if (esSuperAdmin) {
             listOf(Role.Usuario, Role.Chofer, Role.Admin, Role.SuperAdmin)
        } else {
             listOf(Role.Usuario, Role.Chofer)
        }
        _rolesDisponibles.value = roles
    }

    fun buscarUsuario(correo: String) {
        if (correo.isEmpty()) {
            _mensaje.value = "Ingrese un correo para buscar"
            return
        }

        val usuario = UserRepository.findUserByEmail(correo)
        if (usuario != null) {
            _usuarioEncontrado.value = usuario
            _mensaje.value = "Usuario encontrado: ${usuario.nombre}"
        } else {
            _usuarioEncontrado.value = null
            _mensaje.value = "Usuario no encontrado"
        }
    }

    fun crearUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: Role,
        usuarioActual: Usuario
    ) {
         if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            _mensaje.value = "Todos los campos son obligatorios"
            return
        }

        if (contrasena.length < 6) {
            _mensaje.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

         when (usuarioActual.rol) {
            Role.Admin -> {
                 if (rol == Role.Admin || rol == Role.SuperAdmin) {
                    _mensaje.value = "No tienes permisos para crear usuarios administradores"
                    return
                }
            }
            Role.SuperAdmin -> {
             }
            else -> {
                _mensaje.value = "No tienes permisos para gestionar usuarios"
                return
            }
        }

         if (UserRepository.findUserByEmail(correo) != null) {
            _mensaje.value = "El correo ya está registrado"
            return
        }

         val nuevoUsuario = Usuario(nombre, correo, contrasena, rol)
        UserRepository.addUser(nuevoUsuario)

        _mensaje.value = "Usuario creado exitosamente: $nombre"
        _operacionExitosa.value = true
        limpiarFormulario()
    }

    fun actualizarUsuario(
        usuarioExistente: Usuario,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevaContrasena: String?,
        nuevoRol: Role,
        usuarioActual: Usuario
    ) {
         when (usuarioActual.rol) {
            Role.Admin -> {
                 if (usuarioExistente.rol == Role.Admin || usuarioExistente.rol == Role.SuperAdmin) {
                    _mensaje.value = "No puedes modificar usuarios administradores"
                    return
                }
                 if (nuevoRol == Role.Admin || nuevoRol == Role.SuperAdmin) {
                    _mensaje.value = "No puedes asignar roles de administrador"
                    return
                }
            }
            Role.SuperAdmin -> {
                // SuperAdmin puede modificar cualquier usuario
                // No hay restricciones
            }
            else -> {
                _mensaje.value = "No tienes permisos para gestionar usuarios"
                return
            }
        }

         val usuarioActualizado = usuarioExistente.copy(
            nombre = nuevoNombre,
            correo = nuevoCorreo,
            contrasena = nuevaContrasena ?: usuarioExistente.contrasena,
            rol = nuevoRol
        )

         UserRepository.eliminarUsuario(usuarioExistente.correo)
        UserRepository.addUser(usuarioActualizado)

        _mensaje.value = "Usuario actualizado exitosamente"
        _operacionExitosa.value = true
    }

    fun eliminarUsuario(usuario: Usuario, usuarioActual: Usuario) {
         when (usuarioActual.rol) {
            Role.Admin -> {
                 if (usuario.rol == Role.Admin || usuario.rol == Role.SuperAdmin) {
                    _mensaje.value = "No puedes eliminar usuarios administradores"
                    return
                }
            }
            Role.SuperAdmin -> {
                 if (usuario.correo == usuarioActual.correo) {
                    _mensaje.value = "No puedes eliminar tu propia cuenta"
                    return
                }
            }
            else -> {
                _mensaje.value = "No tienes permisos para eliminar usuarios"
                return
            }
        }

         UserRepository.eliminarUsuario(usuario.correo)
        _mensaje.value = "Usuario eliminado exitosamente"
        _operacionExitosa.value = true
        limpiarFormulario()
    }

    private fun limpiarFormulario() {
        _usuarioEncontrado.value = null
    }
}