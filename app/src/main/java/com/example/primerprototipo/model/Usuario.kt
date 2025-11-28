package com.example.primerprototipo.model

import java.io.Serializable

data class Usuario(
    val id: String = "",
    val nombre: String,
    val correo: String,
    val rol: Role
) : Serializable

enum class Role{
    Usuario, Chofer, Admin, SuperAdmin
}
