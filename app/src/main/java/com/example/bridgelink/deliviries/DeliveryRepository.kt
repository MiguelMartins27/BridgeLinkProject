package com.example.bridgelink.deliveries

import android.net.Uri
import com.example.bridgelink.deliviries.Delivery
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DeliveryRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("deliveries")
    private val storage: StorageReference = FirebaseStorage.getInstance().reference.child("delivery_images")

    fun fetchDeliveries(onDataFetched: (List<Delivery>) -> Unit) {
        val deliveriesList = mutableListOf<Delivery>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deliveriesList.clear()

                for (deliverySnapshot in snapshot.children) {
                    val weight = deliverySnapshot.child("weight").getValue(Int::class.java) ?: 0
                    val size = deliverySnapshot.child("size").getValue(String::class.java) ?: ""
                    val fragile = deliverySnapshot.child("fragile").getValue(Boolean::class.java) ?: false
                    val condition = deliverySnapshot.child("condition").getValue(String::class.java) ?: ""
                    val imageUri = deliverySnapshot.child("imageUri").getValue(String::class.java) ?: ""

                    deliveriesList.add(Delivery(weight, size, fragile, condition, imageUri))
                }

                onDataFetched(deliveriesList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    fun saveDelivery(delivery: Delivery, imageUri: Uri, onComplete: (Boolean) -> Unit) {
        val newDeliveryRef = database.push()
        val imageRef = storage.child(newDeliveryRef.key!! + ".jpg")

        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val updatedDelivery = delivery.copy(imageUri = downloadUri.toString())
                newDeliveryRef.setValue(updatedDelivery)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    }
}
