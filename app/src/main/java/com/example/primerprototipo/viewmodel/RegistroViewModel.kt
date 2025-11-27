package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario

class RegistroViewModel : ViewModel() {

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para el resultado del registro
    private val _registroResult = MutableLiveData<RegistroResult>()
    val registroResult: LiveData<RegistroResult> = _registroResult

    fun registro(nombre: String, correo: String, contrasena: String, confirmarContrasena: String) {
        // Validaciones básicas
        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            _registroResult.value = RegistroResult.Error("Todos los campos son obligatorios")
            return
        }

        if (contrasena != confirmarContrasena) {
            _registroResult.value = RegistroResult.Error("Las contraseñas no coinciden")
            return
        }

        // Indicar que el proceso ha comenzado
        _isLoading.value = true

        // Verificar si el correo ya está en uso
        if (UserRepository.findUserByEmail(correo) != null) {
            _registroResult.value = RegistroResult.Error("El correo electrónico ya está en uso")
            _isLoading.value = false
            return
        }

        // Crear el nuevo usuario con rol por defecto 'Usuario'
        val nuevoUsuario = Usuario(
            id = "user-${System.currentTimeMillis()}",
            nombre = nombre,
            correo = correo,
            contrasena = contrasena,
            rol = Role.Usuario // Rol por defecto para nuevos registros
        )

        // Agregar el usuario al repositorio
        UserRepository.addUser(nuevoUsuario)

        // Simular un pequeño retraso (opcional, para visualización)
        // En una app real, aquí se haría la llamada a la red o a la base de datos
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _isLoading.value = false
            _registroResult.value = RegistroResult.Success(nuevoUsuario)
        }, 1000)
    }

    // Clases selladas para representar el resultado del registro
    sealed class RegistroResult {
        data class Success(val usuario: Usuario) : RegistroResult()
        data class Error(val mensaje: String) : RegistroResult()
    }
}
