package edu.rosehulman.sturdeac.rosierideshare

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.rosehulman.rosefire.Rosefire
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_screen_fragment.*
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity(), MainScreenFragment.OnSelectedListener, RideListFragment.OnAccpetedRideSelectedListener {
    private val RC_ROSEFIRE_LOGIN = 1001
    val REQUEST_CHECK_SETTINGS = 1
    private val WRITE_EXTERNAL_STORAGE_PERMISSION = 2
    var user: User? = null
    var mainScreenFragment = MainScreenFragment(user)

    private val auth = FirebaseAuth.getInstance()
    lateinit var authListener: FirebaseAuth.AuthStateListener

    private var userRef = FirebaseFirestore
            .getInstance()
        .collection(Constants.USER_COLLECTION)

    fun getCurrentUser(): User{
        return user!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initializeListeners()
        auth.addAuthStateListener(authListener)
        Log.d(Constants.TAG, "on create")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = "notification channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1234", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPermissions() {
        // Check to see if we already have permissions
        Log.d(Constants.TAG,"checking permission")
        if (ContextCompat
                .checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If we do not, request them from the user
            Log.d(Constants.TAG,"requesting permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.d(Constants.TAG, "Permission granted")
                } else {
                    // permission denied
                }
                return
            }
        }
    }

    private fun initializeListeners() {
        authListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            val authUser = auth.currentUser
            Log.d(Constants.TAG, "In the auth listener, user is $authUser")
            if (authUser != null) {
                Log.d(Constants.TAG, "uid: ${authUser.uid}")
                Log.d(Constants.TAG, "name: ${authUser.displayName}")
                Log.d(Constants.TAG, "email: ${authUser.email}")
                Log.d(Constants.TAG, "phone: ${authUser.phoneNumber}")
                Log.d(Constants.TAG, "photo: ${authUser.photoUrl}")

                userRef.document(authUser.uid).get().addOnSuccessListener { snapshot ->
                    if (snapshot.data != null) {
                        user = User.fromSnapshot(snapshot)
                        Log.d(Constants.TAG, "USER ID::::${user!!.id}")
                    }
                    mainScreenFragment = MainScreenFragment(user)
                    val ft = supportFragmentManager.beginTransaction()
                    ft.replace(R.id.fragment_container, mainScreenFragment)
                    ft.commit()
                    checkPermissions()
                }
            } else {
                onRosefireLogin()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                mainScreenFragment.enableLocation()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }

        }else if (requestCode == RC_ROSEFIRE_LOGIN) {
            val result = Rosefire.getSignInResultFromIntent(data)
            if (!result.isSuccessful) {
                Log.d(Constants.TAG, "login failed")
                return
            }

            FirebaseAuth.getInstance().signInWithCustomToken(result.token).addOnCompleteListener {task: Task<AuthResult> ->

                userRef.document(result.username).get().addOnSuccessListener { snapshot ->
                    if (snapshot.data == null) {
                        val user = User(
                            name = result.name,
                            email = result.email
                        )
                        userRef.document(result.username).set(user)
                            .addOnSuccessListener {
                                Log.d(Constants.TAG, "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(Constants.TAG, "Error writing document", e)
                            }
                    }
                }

                sleep(2000)
                Log.d(Constants.TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful())
                if (!task.isSuccessful()) {
                    Log.w(Constants.TAG, "signInWithCustomToken", task.getException())
                    Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                true
            }
            R.id.action_show_list-> {
                onRideListSelected(user!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // rosefire
    private fun onRosefireLogin() {
        val signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefire_key))
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN)

    }



    override fun onProfileSelected(user: User){
        val profileFragment = ProfileFragment(user)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, profileFragment)
        ft.addToBackStack("profile")
        ft.commit()
    }

    override fun onAcceptedRideSelected(userId: String) {
        //get firebase user with id
        userRef.document(userId).get().addOnSuccessListener { snapshot ->
            val user = User.fromSnapshot(snapshot)
            val lockedProfileFragment = LockedProfileFragment(user)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment_container, lockedProfileFragment)
            ft.addToBackStack("locked profile")
            ft.commit()
        }
    }

    override fun onRideListSelected(user: User){
        Log.d(Constants.RIDES_TAG,"ride list")
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, RideListFragment.newInstance(user))
        ft.addToBackStack("ride list")
        ft.commit()
    }

    override fun onFindRideSelected(user: User, ride: Ride){
        Log.d(Constants.RIDES_TAG,"find ride")
        createNewRide(user, ride)

        //Crashes when user hits find ride and keyboard is already put away
/*
        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                currentFocus.windowToken,
                InputMethodManager.SHOW_FORCED
            )

 */
        val toast = Toast.makeText(applicationContext, "Your Ride was added to the Ride List", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM,0,250)
        toast.show()
    }

    fun createNewRide(user: User, ride: Ride){
        val adapter = RideListAdapter(this, user, null)
        adapter.add(ride)
    }

    override fun onBackPressed() {

        super.onBackPressed()
        mainScreenFragment.updateView(mainScreenFragment.rootView)
    }


}
