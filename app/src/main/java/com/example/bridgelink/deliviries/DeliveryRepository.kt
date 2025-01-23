package com.example.bridgelink.deliveries

import android.net.Uri
import com.example.bridgelink.deliviries.Delivery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class DeliveryRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("deliveries")
    private val storage: StorageReference = FirebaseStorage.getInstance().reference.child("delivery_images")

    // Fetch deliveries as before
    fun fetchDeliveries(): Flow<List<Delivery>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deliveriesList = snapshot.children.mapNotNull {
                    it.toDelivery()?.copy(key = it.key ?: "") // Ensure key is set from snapshot
                }
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
            val deliveryWithKey = delivery.copy(key = key) // Create new delivery with key
            database.child(key).setValue(deliveryWithKey).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    private suspend fun uploadImage(imageUri: Uri): String {
        val ref = storage.child("${System.currentTimeMillis()}.jpg")
        return ref.putFile(imageUri).await().storage.downloadUrl.await().toString()
    }

    suspend fun markDeliveryAsComplete(delivery: Delivery): Boolean {
        return try {
            // Check if the delivery has a valid key
            if (delivery.key.isBlank()) {
                throw Exception("Delivery key is missing")
            }

            // Set the "delivered" field to true in the delivery record
            database.child(delivery.key).child("delivered").setValue(true).await()

            // Get the current user's ID
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Increment the 'deliveries' count in the user's record
                val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                userRef.child("deliveries").run {
                    // Increment the 'deliveries' field by 1
                    setValue(ServerValue.increment(1))
                }.await()
            }

            true
        } catch (e: Exception) {
            // Handle the exception, could log it or show a toast to the user
            false
        }
    }


    fun fetchDeliveriesForUser(userId: String): Flow<List<Delivery>> {
        return flow {
            val database = FirebaseDatabase.getInstance()
            val deliveriesRef = database.getReference("deliveries")

            // Query deliveries where the user ID matches the current user's ID
            val snapshot = deliveriesRef.orderByChild("user").equalTo(userId).get().await()
            val deliveries = snapshot.children.mapNotNull { it.getValue(Delivery::class.java) }
            emit(deliveries)
        }
    }


    private fun DataSnapshot.toDelivery(): Delivery? {
        return try {
            Delivery(
                key = key ?: "", // Get key from snapshot
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
