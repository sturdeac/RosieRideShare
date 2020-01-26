package edu.rosehulman.sturdeac.rosierideshare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.mapbox.mapboxsdk.Mapbox
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, "pk.eyJ1Ijoic3R1cmRlYWMiLCJhIjoiY2ppZWx2OWtyMDgwbDNrcXBhbnA5cG84OCJ9.Hp5qq-G8KXwAW4hUjP6QVg");
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mapbox.onCreate(savedInstanceState)

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

    // mapbox

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
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
        mapbox.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapbox.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapbox.onLowMemory()
    }

    // end mapbox
}
