package edu.rosehulman.sturdeac.rosierideshare

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_add.view.*
import kotlinx.android.synthetic.main.dialog_add_ride.view.*
import kotlinx.android.synthetic.main.main_screen_fragment.*
import kotlinx.android.synthetic.main.main_screen_fragment.view.*

class MainScreenFragment(var user: User?) : Fragment(), PermissionsListener, LocationEngineListener,
    OnMapReadyCallback {

    var settingsClient: SettingsClient? = null
    lateinit var map: MapboxMap
    lateinit var permissionManager: PermissionsManager
    var originLocation: Location? = null
    var locationEngine: LocationEngine? = null
    var locationComponent: LocationComponent? = null
    private var listener: OnSelectedListener? = null
    lateinit var rootView: View

    val storageRef: StorageReference = FirebaseStorage
        .getInstance()
        .reference
        .child("user_photos")

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(
            context!!,
            "pk.eyJ1Ijoic3R1cmRlYWMiLCJhIjoiY2ppZWx2OWtyMDgwbDNrcXBhbnA5cG84OCJ9.Hp5qq-G8KXwAW4hUjP6QVg"
        )
        val view = inflater.inflate(R.layout.main_screen_fragment, container, false)
        rootView = view
        view.mapbox.getMapAsync(this)
        settingsClient = LocationServices.getSettingsClient(activity as MainActivity)
        view.mapbox.onCreate(savedInstanceState)
        view.home_user_profile_pic.setOnClickListener {
            listener?.onProfileSelected(user!!)
        }

        view.home_ride_button.setOnClickListener {
            openRideDialog()
            //listener?.onFindRideSelected(user!!)
            //updateView(view)
        }

        updateView(view)

        return view
    }

    private fun openRideDialog() {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(getString(R.string.ride_dialog_title))

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_ride, null, false)

        builder.setView(view)

        builder.setPositiveButton(android.R.string.ok){_, _ ->
            val ride = Ride(
                rider = user!!.id,
                location = view.home_location_edit_text.text.toString(),
                date = view.home_day_edit_text.text.toString(),
                time = view.home_time_edit_text.text.toString(),
                driver = ""
            )

            listener?.onFindRideSelected(user!!, ride)
        }

        builder.setNegativeButton(android.R.string.cancel, null)

        builder.create().show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnSelectedListener){
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnSelectedListener{
        fun onProfileSelected(user: User)
        fun onRideListSelected(user: User)
        fun onFindRideSelected(user: User, ride: Ride)
    }


    // mapbox

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            locationEngine?.requestLocationUpdates()
            locationComponent?.onStart()
        }

        mapbox.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapbox.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapbox.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationComponent?.onStop()
        mapbox.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapbox.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapbox.onLowMemory()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            context,
            "This app needs location permission to be able to show your location on the map",
            Toast.LENGTH_LONG
        ).show()

    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        } else {
            Toast.makeText(context, "User location was not granted", Toast.LENGTH_LONG).show()
            activity?.finish()
        }

    }

    override fun onLocationChanged(location: Location?) {
        location?.run {
            originLocation = this
            setCameraPosition(this)
        }

    }

    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        //1
        map = mapboxMap ?: return

        val locationRequestBuilder = LocationSettingsRequest.Builder().addLocationRequest(
            LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        )

        val locationRequest = locationRequestBuilder?.build()

        settingsClient?.checkLocationSettings(locationRequest)?.run {
            addOnSuccessListener {
                enableLocation()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            initializeLocationComponent()
            initializeLocationEngine()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(activity)
        }

    }

    @SuppressWarnings("MissingPermission")
    fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(context).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()
        locationEngine?.addLocationEngineListener(this)

        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else {
            locationEngine?.addLocationEngineListener(this)
        }

    }

    @SuppressWarnings("MissingPermission")
    fun initializeLocationComponent() {
        locationComponent = map.locationComponent
        locationComponent?.activateLocationComponent(context!!)
        locationComponent?.isLocationComponentEnabled = true
        locationComponent?.cameraMode = CameraMode.TRACKING

    }

    fun setCameraPosition(location: Location) {
        map.animateCamera(
            CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude))
        )
        map.animateCamera(CameraUpdateFactory.zoomTo(22.0))

    }

    fun EditText.updateText(text: String) {
        val focussed = hasFocus()
        if (focussed) {
            clearFocus()
        }
        setText(text)
        if (focussed) {
            requestFocus()
        }
    }

    fun updateView(view: View){
        storageRef.child(user!!.id).downloadUrl.addOnSuccessListener {data ->
            Log.d(Constants.TAG, "IMAGE URL: $data")
            Picasso.get()
                .load(data)
                .into(view.home_user_profile_pic)
            user!!.pic = data.toString()
        }.addOnFailureListener {
            Log.d(Constants.TAG, "IMAGE ISNT IN STORAGE")
            view.home_user_profile_pic.setImageResource(user!!.defaultPic)
        }

        view.main_screen_user_name.text = user?.name
    }

}