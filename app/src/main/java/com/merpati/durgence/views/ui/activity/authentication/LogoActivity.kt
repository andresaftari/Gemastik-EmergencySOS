package com.merpati.durgence.views.ui.activity.authentication

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.andresaftari.durgence.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.currentLat
import com.merpati.durgence.currentLng
import com.merpati.durgence.model.Users
import com.merpati.durgence.utils.helper.LocationHelper
import com.merpati.durgence.views.ui.MainActivity
import kotlinx.android.synthetic.main.activity_logo.*

class LogoActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var lastKnownLocation: Location? = null
    private var locationHelper: LocationHelper? = null
    private var locationManager: LocationManager? = null

    companion object {
        const val TAG = "LogoActivity"
    }

    override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        if (intent.hasExtra("LOGOUT")) {
            val logoutData = intent.getStringExtra("LOGOUT").toString()
            Snackbar.make(splashScreen, logoutData, Snackbar.LENGTH_SHORT).apply {
                anchorView = splash
                show()
            }
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Setup Location Helper
        locationHelper = LocationHelper(this@LogoActivity)

        // check availability of play services
        if (locationHelper!!.checkGooglePlayServices()) {

            // Building the GoogleApi client
            locationHelper!!.buildGoogleApiClient()
            lastKnownLocation = locationHelper?.location

            if (lastKnownLocation != null) {
                currentLat = lastKnownLocation!!.latitude
                currentLng = lastKnownLocation!!.longitude

                Log.i("LogoActivity", "$currentLat | $currentLng")

                database.child(DB_USERS).child(auth.uid!!).setValue(
                    Users(
                        latitude = currentLat.toString(),
                        longitude = currentLng.toString()
                    )
                )
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
    }

    override fun onConnected(bundle: Bundle?) {
        lastKnownLocation = locationHelper?.location
    }

    override fun onConnectionSuspended(connection: Int) {
        locationHelper?.connectGoogleApiClient()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("LogoActivity", "${connectionResult.errorCode} - ${connectionResult.errorMessage}")
    }

    // Initiate the location listener
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLat = location.latitude
            currentLng = location.longitude

            Log.i("LogoActivity", "$currentLat | $currentLng")

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

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) checkAlreadyUser(user)
        else {
            val background = object : Thread() {
                override fun run() {
                    try {
                        sleep(2500)
                        startActivity(Intent(baseContext, RegisterActivity::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            background.start()
        }
    }

    private fun checkAlreadyUser(user: FirebaseUser) {
        database.child(DB_USERS).child(user.uid).apply {
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.i(TAG, snapshot.childrenCount.toString())

                        snapshot.getValue(Users::class.java)?.apply {
                            if (name == "" && number == "")
                                startActivity(
                                    Intent(
                                        this@LogoActivity,
                                        RegisterActivity::class.java
                                    )
                                )
                            else {
                                val background = object : Thread() {
                                    override fun run() {
                                        try {
                                            sleep(2500)
                                            startActivity(
                                                Intent(
                                                    baseContext,
                                                    MainActivity::class.java
                                                )
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                background.start()
                            }
                        }
                    } else {
                        this@LogoActivity.apply {
                            database.child(DB_USERS).child(user.uid).setValue(
                                Users(
                                    user.uid,
                                    "",
                                    "",
                                    "0",
                                    "",
                                    "",
                                    "",
                                    "0.0",
                                    "0.0"
                                )
                            ).addOnCompleteListener {
                                startActivity(
                                    Intent(
                                        this@LogoActivity,
                                        RegisterActivity::class.java
                                    )
                                )
                            }
                        }
                    }
                }
            })
        }
    }
}