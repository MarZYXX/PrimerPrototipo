package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun login(correo: String, contraseña: String) {
        _loading.value = true

        if (correo.isEmpty() || contraseña.isEmpty()) {
            _errorMessage.value = "Por favor llena todos los campos"
            _loading.value = false
            return
        }

        // Simulación de login
        if (correo == "admin@gmail.com" && contraseña == "1234") {
            _loginSuccess.value = true
            _errorMessage.value = ""
        } else {
            _loginSuccess.value = false
            _errorMessage.value = "Correo o contraseña incorrectos"
        }

        _loading.value = false
    }
}
