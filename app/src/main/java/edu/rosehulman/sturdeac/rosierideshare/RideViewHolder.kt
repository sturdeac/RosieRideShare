package edu.rosehulman.sturdeac.rosierideshare

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_screen_fragment.view.*
import kotlinx.android.synthetic.main.row_view.view.*

class RideViewHolder(itemView: View, private val adapter: RideListAdapter): RecyclerView.ViewHolder(itemView)  {

    private val riderImageView: ImageView = itemView.findViewById(R.id.rider_pic)
    private val riderTextView: TextView = itemView.findViewById(R.id.rider_text_view)
    private val locationTextView: TextView = itemView.findViewById(R.id.location_text_view)
    private val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
    private val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)
    private val statusBar: ProgressBar = itemView.findViewById(R.id.ride_status_bar)
    private val userRef = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION)

    private var cardView: CardView

    init {
        itemView.setOnClickListener {
            adapter.checkRideStatus(adapterPosition)
        }
        itemView.setOnLongClickListener {
            adapter.remove(adapterPosition)
            true
        }
        cardView = itemView.row_card_view
    }

    fun bind(ride: Ride) {

        userRef.document(ride.rider).get().addOnSuccessListener { snapshot ->
            val user = User.fromSnapshot(snapshot)
            if(user.pic == "") {
                riderImageView.setImageResource(user.defaultPic)
            }
            else{
                val myUri = Uri.parse(user.pic)
                Picasso.get()
                    .load(myUri)
                    .into(riderImageView)
            }
            if(ride.accepted){
                statusBar.progress = 100
            }
            riderTextView.text = user.name
            locationTextView.text = ride.location
            dateTextView.text = ride.date
            timeTextView.text = ride.time


        }

    }


}
