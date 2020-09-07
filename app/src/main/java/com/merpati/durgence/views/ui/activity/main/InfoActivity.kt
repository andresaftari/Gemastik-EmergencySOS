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
import com.merpati.durgence.currentLat
import com.merpati.durgence.currentLng
import com.merpati.durgence.utils.helper.LocationHelper
import com.merpati.durgence.views.ui.MainActivity
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private var lastKnownLocation: Location? = null
    private var locationHelper: LocationHelper? = null
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

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