package com.example.primerprototipo.viewmodel
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario

object UserRepository {
    private val users = mutableListOf<Usuario>()
    init {
         precargados()
    }
    private fun precargados() {
        users.addAll(
            listOf(
                Usuario("Usuario Cliente", "usuario@example.com", "usuario123", Role.Usuario),
                Usuario("Chofer ", "chofer@example.com", "chofer123", Role.Chofer),
                Usuario("Administrador", "admin@example.com", "admin123", Role.Admin),
                Usuario("Super Admin", "superadmin@example.com", "super123", Role.SuperAdmin)
            )
        )
    }

    fun addUser(usuario: Usuario) {
        users.add(usuario)
    }

    fun findUserByEmail(email: String): Usuario? {
        return users.find { it.correo.equals(email, ignoreCase = true) }
    }

    fun eliminarUsuario(correo: String): Boolean {
        val usuario = findUserByEmail(correo)
        return if (usuario != null) {
            users.remove(usuario)
            true
        } else {
            false
        }
    }

    fun getAllPreloadedUsers(): List<Usuario> {
        return users.toList()
    }
}