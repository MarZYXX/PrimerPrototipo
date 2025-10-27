package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario

class RegistroViewModel : ViewModel() {

    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> get() = _registroExitoso

    private val _errorMensaje = MutableLiveData<String>()
    val errorMensaje: LiveData<String> get() = _errorMensaje

    fun validarRegistro(nombre: String, correo: String, contraseña: String, confirmar: String): Boolean {
        if (nombre.isEmpty() || correo.isEmpty() || contraseña.isEmpty() || confirmar.isEmpty())
            return false

        return contraseña == confirmar
    }

    fun registrarUsuario(nombre: String, correo: String, pass: String, confirmar: String) {
         if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || confirmar.isEmpty()) {
            _errorMensaje.value = "Por favor llena todos los campos"
            _registroExitoso.value = false
            return
        }

         if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _errorMensaje.value = "Correo electrónico inválido"
            _registroExitoso.value = false
            return
        }

         if (pass.length < 6) {
            _errorMensaje.value = "La contraseña debe tener al menos 6 caracteres"
            _registroExitoso.value = false
            return
        }

         if (pass != confirmar) {
            _errorMensaje.value = "Las contraseñas no coinciden"
            _registroExitoso.value = false
            return
        }

         if (UserRepository.findUserByEmail(correo) != null) {
            _errorMensaje.value = "El correo ya está registrado"
            _registroExitoso.value = false
            return
        }

         val nuevoUsuario = Usuario(nombre, correo, pass, Role.Usuario) // CORRECCIÓN AQUÍ
        UserRepository.addUser(nuevoUsuario)

        _registroExitoso.value = true
        _errorMensaje.value = ""
    }
}