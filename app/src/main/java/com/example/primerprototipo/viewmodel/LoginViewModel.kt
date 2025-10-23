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

    fun login(correo: String, contrasena: String) {
        _loading.value = true

        val user = UserRepository.findUserByEmail(correo)

        if (user != null && user.contrasena == contrasena) {
            _loginSuccess.value = true
            _errorMessage.value = ""
        } else {
            _loginSuccess.value = false
            _errorMessage.value = "Datos incorrectos"
        }

        _loading.value = false
    }
}
