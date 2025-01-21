package com.example.bridgelink.deliveries

import android.net.Uri
import com.example.bridgelink.deliviries.Delivery
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeliveryRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("deliveries")
    private val storage: StorageReference = FirebaseStorage.getInstance().reference.child("delivery_images")

    fun fetchDeliveries(): Flow<List<Delivery>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deliveriesList = snapshot.children.mapNotNull { it.toDelivery() }
                trySend(deliveriesList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    suspend fun saveDelivery(delivery: Delivery): Boolean {
        return try {
            val key = database.push().key ?: throw Exception("Failed to generate push key")
            database.child(key).setValue(delivery).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String {
        val ref = storage.child("${System.currentTimeMillis()}.jpg")
        return ref.putFile(imageUri).await().storage.downloadUrl.await().toString()
    }

    private fun DataSnapshot.toDelivery(): Delivery? {
        return try {
            Delivery(
                user = child("user").getValue(String::class.java) ?: "",
                weight = child("weight").getValue(Int::class.java) ?: 0,
                size = child("size").getValue(String::class.java) ?: "",
                fragile = child("fragile").getValue(Boolean::class.java) ?: false,
                condition = child("condition").getValue(String::class.java) ?: "",
                imageUri = child("imageUri").getValue(String::class.java) ?: "",
                delivered = child("delivered").getValue(Boolean::class.java) ?: false
            )
        } catch (e: Exception) {
            null
        }
    }
}
