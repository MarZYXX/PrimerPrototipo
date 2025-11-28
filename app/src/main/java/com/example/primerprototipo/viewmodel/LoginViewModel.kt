package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Role
import com.example.primerprototipo.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()

    fun login(correo: String, contrasena: String) {
         if (correo.isEmpty() || contrasena.isEmpty()) {
            _loginResult.value = LoginResult.Error("Por favor, llena todos los campos")
            return
        }

         if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _loginResult.value = LoginResult.Error("Correo electrónico inválido")
            return
        }

        _isLoading.value = true

        auth.signInWithEmailAndPassword(correo, contrasena).addOnSuccessListener {authResult ->
            val uid = authResult.user?.uid
            if (uid != null) {
                recuperarRolUsuario(uid)
            } else{
                _isLoading.value = false
                _loginResult.value = LoginResult.Error("Error al obtener el UID del usuario")
            }
        }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _loginResult.value = LoginResult.Error("Error al iniciar sesión: ${e.message}")
            }

//         Thread {
//            try {
//                Thread.sleep(1000) // Simular delay de red
//
//                 val usuario = UserRepository.findUserByEmail(correo)
//
//                if (usuario != null && usuario.contrasena == contrasena) {
//                    _loginResult.postValue(LoginResult.Success(usuario))
//                } else {
//                    _loginResult.postValue(LoginResult.Error("Correo o contraseña incorrectos"))
//                }
//            } catch (e: Exception) {
//                _loginResult.postValue(LoginResult.Error("Error de conexión"))
//            } finally {
//                _isLoading.postValue(false)
//            }
//        }.start()
    }

    private fun recuperarRolUsuario(uid: String) {
        database.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    try {
                        val rolString = document.getString("rol") ?: "Usuario"
                        val rolEnum = try {
                            Role.valueOf(rolString)
                        } catch (e: Exception) {
                            Role.Usuario
                        }

                        val usuario = Usuario(
                            id = uid,
                            nombre = document.getString("nombre") ?: "Usuario",
                            correo = document.getString("correo") ?: "",
                            rol = rolEnum
                        )

                        _isLoading.value = false
                        _loginResult.value = LoginResult.Success(usuario)
                    } catch (e: Exception) {
                        _isLoading.value = false
                        _loginResult.value = LoginResult.Error("Error al obtener el rol del usuario")
                    }
                } else{
                    _isLoading.value = false
                    _loginResult.value = LoginResult.Error("Error al no encontrar usuario en base de datos")
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _loginResult.value = LoginResult.Error("Error de conexion: ${e.message}")
            }
    }

    sealed class LoginResult {
        data class Success(val usuario: Usuario) : LoginResult()
        data class Error(val mensaje: String) : LoginResult()
    }
}