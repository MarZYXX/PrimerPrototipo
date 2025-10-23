package com.example.primerprototipo.viewmodel

import com.example.primerprototipo.model.Usuario

object UserRepository {
    private val users = mutableListOf<Usuario>()

    fun addUser(usuario: Usuario) {
        users.add(usuario)
    }

    fun findUserByEmail(email: String): Usuario? {
        return users.find { it.correo == email }
    }
}
