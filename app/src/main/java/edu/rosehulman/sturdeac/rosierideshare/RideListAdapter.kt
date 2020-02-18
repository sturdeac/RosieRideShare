package edu.rosehulman.sturdeac.rosierideshare

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.dialog_ride.view.*

class RideListAdapter(val context: Context, user: User, val listener: RideListFragment.OnAccpetedRideSelectedListener?): RecyclerView.Adapter<RideViewHolder>() {

    private val user = user
    private val rides = ArrayList<Ride>()
    private val ridesRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.RIDE_COLLECTION)

    // Create an explicit intent for an Activity in your app
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context,0,intent,0)


    private lateinit var listenerRegistration: ListenerRegistration

    fun addSnapshotListener() {

        listenerRegistration = ridesRef
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w(Constants.RIDES_TAG, "listen error", e)
                } else {
                    processSnapshotChanges(querySnapshot!!)
                }
            }
    }

    private fun processSnapshotChanges(querySnapshot: QuerySnapshot) {
        // Snapshots has documents and documentChanges which are flagged by type,
        // so we can handle C,U,D differently.
        for (documentChange in querySnapshot.documentChanges) {
            val ride = Ride.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    Log.d(Constants.RIDES_TAG, "Adding $ride")
                    rides.add(0, ride)
                    notifyItemInserted(0)
                }
                DocumentChange.Type.REMOVED -> {
                    Log.d(Constants.RIDES_TAG, "Removing $ride")
                    val index = rides.indexOfFirst { it.id == ride.id }
                    rides.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
                    Log.d(Constants.RIDES_TAG, "Modifying $ride")
                    val index = rides.indexOfFirst { it.id == ride.id }
                    if(ride.rider == user.id) {
                        Log.d(Constants.RIDES_TAG,"notified correct user")
                        val builder = NotificationCompat.Builder(context, "1234")
                            .setSmallIcon(R.drawable.mapbox_info_icon_default)
                            .setContentTitle("Ride Accepted!")
                            .setContentText("${ride.driver} has accepted your ride.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            // Set the intent that will fire when the user taps the notification
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                        with(NotificationManagerCompat.from(context)) {
                            // notificationId is a unique int for each notification that you must define
                            notify(1234, builder.build())
                        }
                        rides[index] = ride
                    }


                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_view, parent, false)
        return RideViewHolder(view, this)
    }

    override fun getItemCount() = rides.size

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(rides[position])
    }
    fun add(ride: Ride) {
        ridesRef.add(ride)
    }

    fun remove(position: Int) {
        if(rides[position].rider == user.id) {
            ridesRef.document(rides[position].id).delete()
        }
        else{
            Toast.makeText(
                context, "This is someone else's ride.",
                Toast.LENGTH_SHORT).show()
        }
    }

    fun selectRide(position: Int) {
        val r = rides[position]
        r.accepted = true
        r.driver = user.id

        ridesRef.document(r.id).set(r)
    }

    fun checkRideStatus(position: Int){
        val ride = rides[position]
        if(ride.accepted){
            if(ride.driver == user.id){
                listener?.onAcceptedRideSelected(ride.rider)
            }else if(ride.rider == user.id){
                listener?.onAcceptedRideSelected(ride.driver)
            }else{
                Toast
                    .makeText(
                        context,
                        "This ride has been accepted by someone else.",
                        Toast.LENGTH_LONG
                    ).show()
            }

        }else{
            showDialog(position)
        }
    }

    fun showDialog(position: Int) {
        val builder = AlertDialog.Builder(context)

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_ride, null, false
        )

        builder.setView(view)

        view.ride_name_text_view.setText("Accept ride with ${rides[position].rider}?")

        builder.setPositiveButton("Accept Ride") { _, _ ->
            selectRide(position)
        }
        builder.setNegativeButton("Ignore", null)
        builder.show()
    }
}