package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Usuario

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(correo: String, contrasena: String) {
        // Validación de campos vacíos
        if (correo.isEmpty() || contrasena.isEmpty()) {
            _loginResult.value = LoginResult.Error("Por favor, llena todos los campos")
            return
        }

        // Validación de formato de correo
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _loginResult.value = LoginResult.Error("Correo electrónico inválido")
            return
        }

        _isLoading.value = true
    }

    sealed class LoginResult {
        data class Success(val usuario: Usuario) : LoginResult()
        data class Error(val mensaje: String) : LoginResult()
    }
}