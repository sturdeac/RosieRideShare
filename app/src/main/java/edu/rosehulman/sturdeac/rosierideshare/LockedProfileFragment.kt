package edu.rosehulman.sturdeac.rosierideshare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_fragment.view.*

class LockedProfileFragment(user: User) : Fragment() {

    var user = user
    val userRef = FirebaseFirestore.getInstance()
        .collection(Constants.USER_COLLECTION)
        .document(user.id)

    val storageRef: StorageReference = FirebaseStorage
        .getInstance()
        .reference
        .child("user_photos")

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRef.addSnapshotListener { snapshot, exception ->
            if(exception != null){
                Log.e(Constants.TAG, "Error with User Listener", exception)
                return@addSnapshotListener
            }
            user = User.fromSnapshot(snapshot!!)
            updateView(rootView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_fragment_locked, container, false)
        rootView = view
        return view
    }

    private fun updateView(view: View){
        storageRef.child(user.id).downloadUrl.addOnSuccessListener {data ->
            Picasso.get()
                .load(data)
                .into(view.profile_pic)
        }.addOnFailureListener {
            view.profile_pic.setImageResource(R.mipmap.ic_launcher_round)
        }
        view.name_text_view.text = user.name
        view.email_text_view.text = user.email
        view.major_text_view.text = user.major
        view.year_text_view.text = user.year
    }

}