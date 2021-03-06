package edu.rosehulman.sturdeac.rosierideshare

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

data class Ride(

    var rider: String = "",
    var location: String = "",
    var pickup: String = "",
    var date: String = "",
    var time: String = "",
    var accepted: Boolean = false,
    var driver: String = ""
) {

    @get:Exclude var id = ""

    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): Ride {
            val ride = snapshot.toObject(Ride::class.java)!!
            ride.id = snapshot.id
            return ride
        }
    }
}

