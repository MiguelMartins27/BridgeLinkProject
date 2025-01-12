package com.example.bridgelink.danger.area

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DangerAreaRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("danger_areas")

    fun fetchDangerAreas(onDataFetched: (List<DangerArea>) -> Unit) {
        val dangerAreasList = mutableListOf<DangerArea>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dangerAreasList.clear() // Clear previous data

                for (dangerAreaSnapshot in snapshot.children) {
                    val name = dangerAreaSnapshot.child("name").getValue(String::class.java) ?: ""
                    val latitude = dangerAreaSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = dangerAreaSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                    val radius = dangerAreaSnapshot.child("radius").getValue(Double::class.java) ?: 0.0

                    dangerAreasList.add(DangerArea(name, latitude, longitude, radius))
                }

                // Pass the danger areas list to the calling function
                onDataFetched(dangerAreasList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }
}
