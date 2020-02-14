package edu.rosehulman.sturdeac.rosierideshare

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_screen_fragment.view.*
import kotlinx.android.synthetic.main.row_view.view.*

class RideViewHolder(itemView: View, private val adapter: RideListAdapter): RecyclerView.ViewHolder(itemView)  {

    private val riderImageView: ImageView = itemView.findViewById(R.id.rider_pic)
    private val riderTextView: TextView = itemView.findViewById(R.id.rider_text_view)
    private val locationTextView: TextView = itemView.findViewById(R.id.location_text_view)
    private val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
    private val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)

    private var cardView: CardView

    init {
        itemView.setOnClickListener {
            adapter.showDialog(adapterPosition)
        }
        itemView.setOnLongClickListener {
            //if user matches rider, let them delete
            true
        }
        cardView = itemView.row_card_view
    }

    fun bind(ride: Ride) {
        Log.d(Constants.RIDES_TAG, "binding card")
        if(ride.rider!!.pic == "") {
            riderImageView.setImageResource(ride.rider!!.defaultPic)
        }
        else{
            val myUri = Uri.parse(ride.rider!!.pic)
            Picasso.get()
                .load(myUri)
                .into(riderImageView)
        }
        if(ride.accepted){
            itemView.setBackgroundColor(Color.GREEN)
        }
        riderTextView.text = ride.rider?.name
        locationTextView.text = ride.location
        dateTextView.text = ride.date
        timeTextView.text = ride.time

    }


}
