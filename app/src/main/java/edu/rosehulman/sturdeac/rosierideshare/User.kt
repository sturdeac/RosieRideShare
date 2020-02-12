package edu.rosehulman.sturdeac.rosierideshare

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var availability: ArrayList<Int> = ArrayList(),
                var major: String = "",
                var name: String = "",
                var email: String = "",
                var pic: String = "",
                var year: String = ""):Parcelable{
    companion object{
        fun fromSnapshot(snapshot: DocumentSnapshot):User{
            val user = snapshot.toObject(User::class.java)!!
            return user
        }
    }
}