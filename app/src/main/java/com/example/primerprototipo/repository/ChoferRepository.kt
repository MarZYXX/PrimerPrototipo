package com.example.primerprototipo.repository

import com.example.primerprototipo.model.Chofer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ChoferRepository {

    private const val CHOFERES_COLLECTION = "choferes"
    private val firestore = FirebaseFirestore.getInstance()

    // Agrega o actualiza un documento de chofer
    suspend fun guardarChofer(chofer: Chofer): Result<Unit> {
        return try {
            // Usamos el ID del usuario como ID del documento en la colección 'choferes'
            firestore.collection(CHOFERES_COLLECTION).document(chofer.id).set(chofer).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtiene un chofer por su ID
    suspend fun getChofer(id: String): Result<Chofer?> {
        return try {
            val document = firestore.collection(CHOFERES_COLLECTION).document(id).get().await()
            val chofer = document.toObject(Chofer::class.java)
            Result.success(chofer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}