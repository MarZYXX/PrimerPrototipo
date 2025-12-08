package com.example.primerprototipo.viewmodel

import com.example.primerprototipo.model.Usuario

// Modelo de datos para el usuario
object serRepository {
    private val users = mutableListOf<Usuario>()

    fun addUser(usuario: Usuario) {
    }

    fun findUserByEmail(email: String): Usuario? {
        return null
    }

    fun eliminarUsuario(correo: String): Boolean {
        return false
    }

    fun getAllPreloadedUsers(): List<Usuario> {
        return emptyList()
    }
}
