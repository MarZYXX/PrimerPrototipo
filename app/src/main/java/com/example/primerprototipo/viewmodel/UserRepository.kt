package com.example.primerprototipo.viewmodel

import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import java.util.UUID

object UserRepository {
    private val users = mutableListOf<Usuario>()

    init {
        precargados()
    }

    private fun precargados() {
        users.addAll(
            listOf(
                Usuario(
                    id = "user-1",
                    nombre = "Usuario Cliente",
                    correo = "usuario@gmail.com",
                    contrasena = "usuario123",
                    rol = Role.Usuario
                ),
                Usuario(
                    id = "user-2",
                    nombre = "Chofer",
                    correo = "chofer@gmail.com",
                    contrasena = "chofer123",
                    rol = Role.Chofer
                ),
                Usuario(
                    id = "user-3",
                    nombre = "Administrador",
                    correo = "admin@gmail.com",
                    contrasena = "admin123",
                    rol = Role.Admin
                ),
                Usuario(
                    id = "user-4",
                    nombre = "Super Admin",
                    correo = "superadmin@gmail.com",
                    contrasena = "super123",
                    rol = Role.SuperAdmin
                )
            )
        )
    }

    fun addUser(usuario: Usuario) {
        // Asegurar que el usuario tenga un ID
        val usuarioConId = if (usuario.id.isEmpty()) {
            usuario.copy(id = "user-${UUID.randomUUID()}")
        } else {
            usuario
        }
        users.add(usuarioConId)
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