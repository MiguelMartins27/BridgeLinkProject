package com.example.bridgelink.post.office

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PostOfficeRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("post_offices")

    fun fetchPostOffices(onDataFetched: (List<PostOffice>) -> Unit) {
        val postOfficesList = mutableListOf<PostOffice>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postOfficesList.clear() // Clear previous data

                for (postOfficeSnapshot in snapshot.children) {
                    val name = postOfficeSnapshot.child("name").getValue(String::class.java) ?: ""
                    val latitude = postOfficeSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = postOfficeSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                    postOfficesList.add(PostOffice(name, latitude, longitude))
                }

                // Pass the post offices list to the calling function
                onDataFetched(postOfficesList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }
}
