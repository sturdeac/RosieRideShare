package edu.rosehulman.sturdeac.rosierideshare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.rosehulman.rosefire.Rosefire
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainScreenFragment.OnProfileSelectedListener {

    private val RC_ROSEFIRE_LOGIN = 1001
    val REQUEST_CHECK_SETTINGS = 1
    var user: User? = null
    val mainScreenFragment = MainScreenFragment(user)

    private val auth = FirebaseAuth.getInstance()
    lateinit var authListener: FirebaseAuth.AuthStateListener

    private var userRef = FirebaseFirestore
            .getInstance()
        .collection(Constants.USER_COLLECTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeListeners()
        auth.addAuthStateListener(authListener)
        Log.d(Constants.TAG, "on create")

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
                    mainScreenFragment.user = user
                    val ft = supportFragmentManager.beginTransaction()
                    ft.replace(R.id.fragment_container, mainScreenFragment)
                    ft.commit()
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

                userRef.document(result.username).get().addOnSuccessListener {snapshot ->
                    if(snapshot.data == null){
                        Log.d(Constants.TAG, "add user")
                        val user = User(
                            name = result.name,
                            email = result.email
                        )
                        userRef.document(result.username).set(user)
                            .addOnSuccessListener { Log.d(Constants.TAG, "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w(Constants.TAG, "Error writing document", e) }
                    }
                }

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
            R.id.action_settings -> true
            R.id.action_logout -> {
                auth.signOut()
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

}
