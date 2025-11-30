package com.example.primerprototipo.model

import java.io.Serializable

data class Usuario(
    val id: String = "",
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val correo: String,
    val rol: Role
) : Serializable{
    fun nombreCompleto(): String {
        return "$nombre $apellidoPaterno $apellidoMaterno".trim()
    }
}

enum class Role{
    Usuario, Chofer, Admin, SuperAdmin
}
