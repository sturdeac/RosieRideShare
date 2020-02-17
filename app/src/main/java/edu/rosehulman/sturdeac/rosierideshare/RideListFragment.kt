package edu.rosehulman.sturdeac.rosierideshare

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_UID = "USER"

class RideListFragment(user: User): Fragment() {

    private var currentUser: User ?= user
    lateinit var adapter: RideListAdapter
    private var listener: OnAccpetedRideSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(R.layout.ride_list_fragment, container, false) as RecyclerView
        adapter = RideListAdapter(context!!, currentUser!!, listener)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        adapter.addSnapshotListener()
        return recyclerView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnAccpetedRideSelectedListener){
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User) =
            RideListFragment(user).apply {
            }
    }

    interface OnAccpetedRideSelectedListener{
        fun onAcceptedRideSelected(user: String)
    }
}
