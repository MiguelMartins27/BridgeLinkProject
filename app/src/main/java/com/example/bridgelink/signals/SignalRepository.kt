package com.example.bridgelink.signals

import com.example.bridgelink.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignalRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("signals")

    fun fetchSignals(onDataFetched: (List<Signal>) -> Unit) {
        val signalsList = mutableListOf<Signal>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                signalsList.clear()

                for (signalSnapshot in snapshot.children) {
                    val id = signalSnapshot.child("id").getValue(Int::class.java) ?: 0
                    val iconUrl = signalSnapshot.child("iconUrl").getValue(String::class.java) ?: ""
                    val description = signalSnapshot.child("description").getValue(String::class.java) ?: ""
                    val latitude = signalSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = signalSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                    signalsList.add(Signal(id, iconUrl, description, latitude, longitude))
                }

                // Pass the signals list to the calling function
                onDataFetched(signalsList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun saveSignal(signal: Signal, onComplete: (Boolean) -> Unit) {
        val newSignalRef = database.push()
        newSignalRef.setValue(signal)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    val signalIcons = listOf(
        R.drawable.signs_angry,
        R.drawable.signs_anoyed,
        R.drawable.signs_bbs,
        R.drawable.signs_bts,
        R.drawable.signs_cliff,
        R.drawable.signs_danger,
        R.drawable.signs_dead,
        R.drawable.signs_deepwaters,
        R.drawable.signs_difficultterrain,
        R.drawable.signs_donotuse,
        R.drawable.signs_dontlitter,
        R.drawable.signs_dontpee,
        R.drawable.signs_dontsleep,
        R.drawable.signs_foward,
        R.drawable.signs_garbage,
        R.drawable.signs_great,
        R.drawable.signs_highgrass,
        R.drawable.signs_hotbaths,
        R.drawable.signs_laugh,
        R.drawable.signs_left,
        R.drawable.signs_mountain,
        R.drawable.signs_right,
        R.drawable.signs_slowdown,
        R.drawable.signs_smile,
        R.drawable.signs_thumbsup
    )
}
