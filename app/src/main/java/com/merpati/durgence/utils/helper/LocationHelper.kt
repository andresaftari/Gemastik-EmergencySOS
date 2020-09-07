package com.merpati.durgence.utils.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.andresaftari.durgence.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.internal.OnConnectionFailedListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.merpati.durgence.utils.permission.PermissionResultCallback
import com.merpati.durgence.utils.permission.Permissions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
data class LocationHelper(private val context: Context) : PermissionResultCallback {
    companion object {
        private const val PLAY_SERVICES_REQUEST = 1000
        private const val REQUEST_CHECK_SETTINGS = 2000
    }

    // Variable initiations
    private val currentActivity = context as Activity
    private var isGranted = false
    private var lastKnownLocation: Location? = null

    // Google client to interact with Google API
    private var googleApiClient: GoogleApiClient? = null

    // List of permissions
    private val permissions = ArrayList<String>()
    private val permission = Permissions(currentActivity, this)

    val location: Location?
        get() {
            if (isGranted) {
                try {
                    lastKnownLocation =
                        LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                    return lastKnownLocation
                } catch (e: SecurityException) {
                    Log.i("LocationHelper", e.printStackTrace().toString())
                } catch (e: Exception) {
                    Log.i("LocationHelper", e.printStackTrace().toString())
                }
            }
            return null
        }

    init {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.READ_CONTACTS)
    }

    override fun permissionGranted(requestCode: Int) {
        Log.i("LocationHelper", "PERMISSION GRANTED!")
        isGranted = true
    }

    override fun partialPermissionGranted(requestCode: Int, grantedPermissions: ArrayList<String>) {
        Log.i("LocationHelper", "PERMISSION PARTIALLY GRANTED!")
    }

    override fun permissionDenied(requestCode: Int) {
        Log.i("LocationHelper", "PERMISSION DENIED!")
    }

    override fun neverAskAgain(requestCode: Int) {
        Log.i("LocationHelper", "NEVER ASK AGAIN!")
    }

    // Method used to connect GoogleApiClient
    fun connectGoogleApiClient() = googleApiClient!!.connect()

    // Method to check the availability of location permissions
    fun checkPermission() = permission.checkPermissions(
        permissions,
        "Izinkan ${R.string.app_name} untuk mengakses lokasi Anda",
        1
    )

    // Method to verify google play services on the device
    fun checkGooglePlayServices(): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val resultCode = googleApi.isGooglePlayServicesAvailable(context)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(resultCode)) googleApi.getErrorDialog(
                currentActivity, resultCode, PLAY_SERVICES_REQUEST
            ).show()
            else Toast.makeText(
                context,
                "This device is not supported.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    // Method to display the location on UI
    fun getLocation(latitude: Double, longitude: Double): Address? {
        val location: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            location = geocoder.getFromLocation(
                latitude,
                longitude,
                1 // Here 1 is max location result to returned, recommended 1 to 5
            )

            return location[0]
        } catch (e: IOException) {
            Log.i("LocationHelper", e.printStackTrace().toString())
        } catch (e: Exception) {
            Log.i("LocationHelper", e.printStackTrace().toString())
        }
        return null
    }

    // Method used to build GoogleApiClient
    fun buildGoogleApiClient() {
        // Initialize the GoogleApiClient Builder
        googleApiClient = GoogleApiClient.Builder(context).apply {
            addConnectionCallbacks((currentActivity as GoogleApiClient.ConnectionCallbacks))
            addOnConnectionFailedListener { currentActivity as OnConnectionFailedListener }
            addApi(LocationServices.API)
        }.build()
        // Connect the GoogleApiClient
        googleApiClient?.connect()

        // Initiate user location updates
        val locationRequest = LocationRequest()
        locationRequest.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
        }
        // Initiate location services and status callbacks
        LocationServices.SettingsApi.apply {
            checkLocationSettings(
                googleApiClient,
                builder.build()
            ).setResultCallback { locationSettingsResult ->
                val status = locationSettingsResult.status

                when (status.statusCode) {
                    // All location settings are satisfied. The client can initialize location requests here
                    LocationSettingsStatusCodes.SUCCESS -> lastKnownLocation = location
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult()
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        status.startResolutionForResult(
                            currentActivity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.i("LocationHelper", e.printStackTrace().toString())
                    } catch (e: Exception) {
                        Log.i("LocationHelper", e.message.toString())
                    }
                    else -> {
                        // Nothing to do
                    }
                }
            }
        }
    }

    // Handles the permission results
    fun onRequestPermission(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray?
    ) = permission.onRequestPermissionsResult(requestCode, permissions, grantResults!!)

    // Handles the activity results
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> lastKnownLocation = location
                Activity.RESULT_CANCELED -> {
                    // Nothing to do
                }
                else -> {
                    // Nothing to do
                }
            }
        }
    }
}