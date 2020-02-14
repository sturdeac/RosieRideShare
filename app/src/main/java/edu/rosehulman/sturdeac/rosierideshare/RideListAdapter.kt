package edu.rosehulman.sturdeac.rosierideshare

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.dialog_ride.view.*

class RideListAdapter(val context: Context, uid: String): RecyclerView.Adapter<RideViewHolder>() {

    private val uid = uid
    private val rides = ArrayList<Ride>()
    private val ridesRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.RIDE_COLLECTION)

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
                    rides[index] = ride
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
       ridesRef.document(rides[position].id).delete()
    }

    fun selectRide(position: Int) {
        val r = rides[position]
        ridesRef.document(r.id).set(r)
    }

    fun showDialog(position: Int = -1) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_ride, null, false
        )

        builder.setView(view)

        if (position >= 0) {
            view.ride_name_text_view.setText("Accept ride with ${rides[position].rider?.name}?")

        }

        builder.setPositiveButton("Accept Ride") { _, _ ->
            rides[position].accepted = true
            selectRide(position)
        }
        builder.setNegativeButton("Ignore", null)
        builder.show()
    }

}