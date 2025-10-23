package com.example.primerprototipo.model

import java.io.Serializable

class Usuario : Serializable {
    private var nombre: String = ""
    private var correo: String = ""
    private var contraseña: String = ""

    fun setNombre(n: String) { nombre = n }
    fun setCorreo(c: String) { correo = c }
    fun setContraseña(p: String) { contraseña = p }

    fun getNombre() = nombre
    fun getCorreo() = correo
    fun getContraseña() = contraseña
}
