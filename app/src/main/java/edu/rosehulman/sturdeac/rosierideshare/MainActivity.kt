package edu.rosehulman.sturdeac.rosierideshare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import edu.rosehulman.rosefire.Rosefire
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainScreenFragment.OnProfileSelectedListener {

    private val RC_ROSEFIRE_LOGIN = 1001
    val REQUEST_CHECK_SETTINGS = 1
    val mainScreenFragment = MainScreenFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        onRosefireLogin()
        setSupportActionBar(toolbar)

        if(savedInstanceState==null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment_container, mainScreenFragment)
            ft.commit()
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
                // The user cancelled the login
            }
            FirebaseAuth.getInstance().signInWithCustomToken(result.token).addOnCompleteListener {task: Task<AuthResult> ->
                Log.d("RR", "signInWithCustomToken:onComplete:" + task.isSuccessful());
                // If sign in fails, display a message to the user. If sign in succeeds
                // you should use an AuthStateListener to handle the logic for
                // signed in user and a signed out user.
                if (!task.isSuccessful()) {
                    Log.w("RR", "signInWithCustomToken", task.getException());
                    Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show();
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    // rosefire
    fun onRosefireLogin() {
        val signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefire_key))
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN)
    }

    override fun onProfileSelected(){
        val profileFragment = ProfileFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, profileFragment)
        ft.addToBackStack("profile")
        ft.commit()
    }

}
