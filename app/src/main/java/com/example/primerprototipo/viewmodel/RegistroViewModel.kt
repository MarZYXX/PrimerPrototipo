package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistroViewModel : ViewModel() {
    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> get() = _registroExitoso

    private val _errorMensaje = MutableLiveData<String>()
    val errorMensaje: LiveData<String> get() = _errorMensaje

    fun registrarUsuario(nombre: String, correo: String, pass: String, confirmar: String) {
        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || confirmar.isEmpty()) {
            _errorMensaje.value = "Por favor llena todos los campos"
            return
        }
        if (pass != confirmar) {
            _errorMensaje.value = "Las contraseñas no coinciden"
            return
        }

        _registroExitoso.value = true
        _errorMensaje.value = ""
    }
}
