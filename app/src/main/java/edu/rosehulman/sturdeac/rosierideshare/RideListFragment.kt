package edu.rosehulman.sturdeac.rosierideshare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_UID = "UID"

class RideListFragment: Fragment() {

    private var uid: String? = null
    lateinit var adapter: RideListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
            Log.d(Constants.RIDES_TAG,"uid: $uid")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(R.layout.ride_list_fragment, container, false) as RecyclerView
        adapter = RideListAdapter(context!!, uid!!)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        adapter.addSnapshotListener()
        return recyclerView
    }

    companion object {
        @JvmStatic
        fun newInstance(uid: String) =
            RideListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
    }

}
