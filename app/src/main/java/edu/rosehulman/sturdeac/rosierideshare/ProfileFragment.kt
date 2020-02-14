package edu.rosehulman.sturdeac.rosierideshare

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener.add
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.dialog_add.view.*
import kotlinx.android.synthetic.main.profile_fragment.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_screen_fragment.view.*

import kotlinx.android.synthetic.main.profile_fragment.view.*
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class ProfileFragment(var user: User?) : Fragment() {

    private val WRITE_EXTERNAL_STORAGE_PERMISSION = 2

    val userRef = FirebaseFirestore.getInstance()
        .collection(Constants.USER_COLLECTION)
        .document(user!!.id)

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
            Log.d(Constants.TAG, "PROFILE LISTENER: ${snapshot.toString()}")
            user = User.fromSnapshot(snapshot!!)
            updateView(rootView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_fragment, container, false)
        rootView = view
        view.edit_pic_button.setOnClickListener {
            launchChooseIntent()
        }
        view.edit_user_info_button.setOnClickListener {
            launchEditDialog()
        }

        updateView(view)

        return view
    }

    private fun launchEditDialog() {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(getString(R.string.dialog_title))

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add, null, false)

        view.major_edit_text.setText(user?.major)
        view.year_edit_text.setText(user?.year)

        builder.setView(view)

        builder.setPositiveButton(android.R.string.ok){_, _ ->
            user?.major = view.major_edit_text.text.toString()
            user?.year = view.year_edit_text.text.toString()

            edit(user)
        }

        builder.setNegativeButton(android.R.string.cancel, null)

        builder.create().show()
    }

    private fun updateView(view: View){
        storageRef.child(user!!.id).downloadUrl.addOnSuccessListener {data ->
            Picasso.get()
                .load(data)
                .into(view.profile_pic)
        }.addOnFailureListener {
            view.profile_pic.setImageResource(R.mipmap.ic_launcher_round)
        }
        view.name_text_view.text = user?.name
        view.email_text_view.text = user?.email
        view.major_text_view.text = user?.major
        view.year_text_view.text = user?.year
    }

    fun add(localPath: String) {
        ImageRescaleTask(localPath).execute()
    }

    private fun edit(user: User?){
        userRef.set(user!!)
    }

    private fun launchChooseIntent() {
        val choosePictureIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        choosePictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        choosePictureIntent.type = "image/*"
        if (choosePictureIntent.resolveActivity(context!!.packageManager) != null) {
            startActivityForResult(choosePictureIntent, 1)

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    sendGalleryPhoto(data)
                }
            }
        }
    }
    private fun sendGalleryPhoto(data: Intent?) {
        if (data != null && data.data != null) {
            val location = data.data!!.toString()
            ImageRescaleTask(location).execute()
        }
    }

    inner class ImageRescaleTask(val localPath: String) : AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg p0: Void?): Bitmap? {
            val ratio = 2
            return BitmapUtils.rotateAndScaleByRatio(context!!, localPath, ratio)
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            setProfilePic(bitmap)
            storageAdd(localPath, bitmap)
        }
    }

    private fun setProfilePic(bitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val bm: Bitmap = BitmapFactory.decodeByteArray(data,0,data.size)
        profile_pic.setImageBitmap(bm)
        storageRef.child(user!!.id).downloadUrl
            .addOnSuccessListener {
                user?.pic = it.toString()
                edit(user)
            }
            .addOnFailureListener{
                Log.d(Constants.TAG, "setting pic in firebase failed")
            }
        updateView(view!!)
        //Change user's profile picture in firebase

    }

    private fun storageAdd(localPath: String, bitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val id = user?.id
        var uploadTask = storageRef.child(id!!).putBytes(data)
        uploadTask.addOnFailureListener{
            Log.d(Constants.TAG, "Image upload failed: $localPath $id")
        }.addOnSuccessListener {
            Log.d(Constants.TAG, "Image upload succeeded: $localPath $id")
        }

    }

}
