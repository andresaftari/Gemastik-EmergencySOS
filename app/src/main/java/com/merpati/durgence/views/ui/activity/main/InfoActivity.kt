package com.merpati.durgence.views.ui.activity.main

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.andresaftari.durgence.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.currentLat
import com.merpati.durgence.currentLng
import com.merpati.durgence.utils.helper.LocationHelper
import com.merpati.durgence.views.ui.MainActivity
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var lastKnownLocation: Location? = null
    private var locationHelper: LocationHelper? = null
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Setup Location Helper
        locationHelper = LocationHelper(this@InfoActivity)
        locationHelper!!.checkPermission()

        // check availability of play services
        if (locationHelper!!.checkGooglePlayServices()) {

            // Building the GoogleApi client
            locationHelper!!.buildGoogleApiClient()
            lastKnownLocation = locationHelper?.location

            if (lastKnownLocation != null) {
                currentLat = lastKnownLocation!!.latitude
                currentLng = lastKnownLocation!!.longitude

                database.child(DB_USERS).child(auth.uid!!).apply {
                    child("latitude").setValue(currentLat.toString())
                    child("longitude").setValue(currentLng.toString())
                }
            }

            // Request location updates
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
            try {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            } catch (e: SecurityException) {
                Log.i("InfoActivity", "${e.message} - ${e.printStackTrace()}")
            } catch (e: Exception) {
                Log.i("InfoActivity", "${e.message} - ${e.printStackTrace()}")
            }
        }

        btn_info.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // Initiate the location listener
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLat = location.latitude
            currentLng = location.longitude

            if (auth.currentUser != null) {
                database.child(DB_USERS).child(auth.uid!!).apply {
                    child("latitude").setValue(currentLat.toString())
                    child("longitude").setValue(currentLng.toString())
                }
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onConnected(bundle: Bundle?) {
        lastKnownLocation = locationHelper?.location
    }

    override fun onConnectionSuspended(connection: Int) {
        locationHelper?.connectGoogleApiClient()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("InfoActivity", "${connectionResult.errorCode} - ${connectionResult.errorMessage}")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) = locationHelper!!.onRequestPermission(requestCode, permissions, grantResults)
}