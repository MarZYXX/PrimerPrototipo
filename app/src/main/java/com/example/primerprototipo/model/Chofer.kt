package com.example.primerprototipo.model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.Date

data class Chofer(
    val id: String = "", // Mismo ID que en la colección de Usuarios
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val numeroLicencia: String = "",
    val estado: String = "Inactivo",
    val fechaContratacion: Timestamp = Timestamp(Date()),
    val telefonoEmergencia: String = "",
    val fotoUrl: String? = null
) : Serializable
