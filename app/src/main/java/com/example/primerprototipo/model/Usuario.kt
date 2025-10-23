package com.example.primerprototipo.model

import java.io.Serializable

data class Usuario(
    val nombre: String,
    val correo: String,
    val contrasena: String
) : Serializable
