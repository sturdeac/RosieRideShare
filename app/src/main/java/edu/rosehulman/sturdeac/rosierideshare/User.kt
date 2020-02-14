package edu.rosehulman.sturdeac.rosierideshare

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var major: String = "",
                var name: String = "",
                var email: String = "",
                var pic: String = "",
                var defaultPic: Int = R.mipmap.ic_launcher_round,
                var year: String = ""):Parcelable{
    @IgnoredOnParcel
    @get:Exclude var id: String = ""

    companion object{
        fun fromSnapshot(snapshot: DocumentSnapshot):User{
            val user = snapshot.toObject(User::class.java)!!
            user.id = snapshot.id
            return user
        }

    }
}