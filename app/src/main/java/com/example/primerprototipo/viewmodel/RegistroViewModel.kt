package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroViewModel : ViewModel() {

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para el resultado del registro
    private val _registroResult = MutableLiveData<RegistroResult>()
    val registroResult: LiveData<RegistroResult> = _registroResult

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()

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

        auth.createUserWithEmailAndPassword(correo, contrasena).addOnSuccessListener {
            authResult -> val uid = authResult.user?.uid ?: ""

            val nuevoUsuario = Usuario(
                id = uid,
                nombre = nombre,
                correo = correo,
                rol = Role.Usuario//
            )

            val usermap = hashMapOf(
                "id" to uid,
                "nombre" to nombre,
                "correo" to correo,
                "rol" to nuevoUsuario.rol.name
            )

            database.collection("usuarios").document(uid).set(usermap)
                .addOnSuccessListener{
                    _isLoading.value = false
                    _registroResult.value = RegistroResult.Success(nuevoUsuario)
                }
                .addOnFailureListener{ e ->
                    _isLoading.value = false
                    _registroResult.value = RegistroResult.Error("Error al guardar el usuario en la base de datos")
                }

            // Verificar si el correo ya está en uso
//            if (UserRepository.findUserByEmail(correo) != null) {
//                _registroResult.value = RegistroResult.Error("El correo electrónico ya está en uso")
//                _isLoading.value = false
//                return
//            }

            // Agregar el usuario al repositorio
            UserRepository.addUser(nuevoUsuario)

//            // Simular un pequeño retraso (opcional, para visualización)
//            // En una app real, aquí se haría la llamada a la red o a la base de datos
//            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
//                _isLoading.value = false
//                _registroResult.value = RegistroResult.Success(nuevoUsuario)
//            }, 1000)
        }
    }

    // Clases selladas para representar el resultado del registro
    sealed class RegistroResult {
        data class Success(val usuario: Usuario) : RegistroResult()
        data class Error(val mensaje: String) : RegistroResult()
    }
}
