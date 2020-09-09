package com.merpati.durgence.views.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andresaftari.durgence.R
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.merpati.durgence.*
import com.merpati.durgence.model.Services
import com.merpati.durgence.model.Users
import com.merpati.durgence.utils.adapter.ServiceAdapter
import com.merpati.durgence.utils.api.LocationService
import com.merpati.durgence.utils.api.WeatherService
import com.merpati.durgence.utils.api.data.response.LocationResponse
import com.merpati.durgence.utils.api.data.response.WeatherResponse
import com.merpati.durgence.utils.helper.LocationHelper
import com.merpati.durgence.views.ui.activity.about.AboutActivity
import com.merpati.durgence.views.ui.activity.authentication.LogoActivity
import com.merpati.durgence.views.ui.activity.faq.FaqActivity
import kotlinx.android.synthetic.main.activity_logo.*
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val list = ArrayList<Services>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retainInstance = true
        pb_home.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        when (savedInstanceState) {
            null -> {
                list.addAll(getListServices())
                populateViews()
            }
            else -> {
                val stateList = savedInstanceState.getParcelableArrayList<Services>(STATE_LIST)
                if (stateList != null) list.addAll(stateList)
            }
        }
        helloUser()
        LocationHelper(requireContext()).getLocation(currentLat, currentLng)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STATE_LIST, list)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setMenuAction(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_more, menu)
    }

    @SuppressLint("Recycle")
    private fun getListServices(): ArrayList<Services> {
        val dataImage = resources.obtainTypedArray(R.array.serviceThumb)
        val dataNameIndo = resources.getStringArray(R.array.serviceName_indo)
        val dataNameEng = resources.getStringArray(R.array.serviceName_eng)

        val list = ArrayList<Services>()

        for (position in dataNameIndo.indices) {
            val service = Services(
                dataImage.getResourceId(position, -1),
                dataNameIndo[position],
                dataNameEng[position]
            )
            list.add(service)
            Log.i(TAG, "list: $list")
        }
        return list
    }

    @SuppressLint("SetTextI18n")
    private fun helloUser() = database.child(DB_USERS).child(auth.uid!!).apply {
        addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get user name
                val user = snapshot.getValue(Users::class.java)
                val username = user?.name.toString()

                Log.i(TAG, username)

                // Get 24-Hour format greetings
                val calendar = Calendar.getInstance()

                when (calendar.get(Calendar.HOUR_OF_DAY)) {
                    in 5..11 -> tv_welcome?.text = "Halo $username,\nSelamat Pagi!"
                    in 12..14 -> tv_welcome?.text = "Halo $username,\nSelamat Siang!"
                    in 15..18 -> tv_welcome?.text = "Halo $username,\nSelamat Sore!"
                    in 19..24 -> tv_welcome?.text = "Halo $username,\nSelamat Malam!"
                    in 0..4 -> tv_welcome?.text = "Halo $username,\nSelamat Malam!"
                }

                // Get current weather condition
                getCurrentWeather()
                Log.i(TAG, tv_welcome?.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "Failed! ${error.message}")
            }
        })
    }

    private fun populateViews() {
        try {
            pb_home.visibility = View.GONE
            rv_services?.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(activity)
                adapter = ServiceAdapter(list)
            }
        } catch (e: Exception) {
            Log.i(TAG, "${e.message} --- ${e.printStackTrace()}")
        } catch (e: KotlinNullPointerException) {
            Log.i(TAG, "${e.message} --- ${e.printStackTrace()}")
        }
    }

    private fun setMenuAction(menuID: Int) {
        when (menuID) {
            R.id.action_about -> {
                startActivity(Intent(activity, AboutActivity::class.java))
            }
            R.id.action_faq -> {
                startActivity(Intent(activity, FaqActivity::class.java))
            }
            R.id.action_logout -> {
                database.child(DB_USERS).child(auth.uid!!).child("status").setValue("0")
                auth.signOut()
                startActivity(
                    Intent(activity, LogoActivity::class.java).putExtra(
                        "LOGOUT",
                        "Anda telah logout"
                    )
                )
            }
        }
    }

    fun getCurrentWeather() {
        database.child(DB_USERS).child(auth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.i(TAG, snapshot.childrenCount.toString())

                        val lat = snapshot.getValue(Users::class.java)?.latitude
                        val lon = snapshot.getValue(Users::class.java)?.longitude

                        // Coordinate API
                        val locationResponse = LocationResponse(lat.toString(), lon.toString())
                        val coordRetrofit = Retrofit.Builder()
                            .baseUrl(COORDINATE_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        val coordService = coordRetrofit.create(LocationService::class.java)
                        val post = coordService.postCoordinate(
                            locationResponse.latitude,
                            locationResponse.longitude
                        )

                        post.enqueue(object : Callback<LocationResponse> {
                            override fun onResponse(
                                call: Call<LocationResponse>, response: Response<LocationResponse>
                            ) {
                                Log.i(TAG, "Posted to api! - ${response.body()}")
                            }

                            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                                Snackbar.make(
                                    pb_loading,
                                    t.message.toString(),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        })

                        // Weather API
                        val retrofit = Retrofit.Builder()
                            .baseUrl(WEATHER_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        val service = retrofit.create(WeatherService::class.java)
                        val call = service.getCurrentWeatherData(lat!!, lon!!, WEATHER_API_KEY)

                        Log.i("Weather", "$lat - $lon")

                        call.enqueue(object : Callback<WeatherResponse> {
                            override fun onResponse(
                                call: Call<WeatherResponse>, response: Response<WeatherResponse>
                            ) {
                                if (response.code() == 200) {
                                    val weatherResponse = response.body()!!

                                    val weather = weatherResponse.weather[0].main
                                    Log.i("Weather", "current weather - $weather")

                                    if (activity != null && isAdded) when (weather) {
                                        "Clouds" -> Glide.with(requireActivity())
                                            .load(R.drawable.clear_cloudy)
                                            .into(iv_weather)

                                        "Clear" -> Glide.with(requireActivity())
                                            .load(R.drawable.sunny)
                                            .into(iv_weather)

                                        "Mist" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Haze" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Smoke" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Dust" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Fog" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Sand" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Ash" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Squall" -> Glide.with(requireActivity())
                                            .load(R.drawable.mist)
                                            .into(iv_weather)

                                        "Tornado" -> Glide.with(requireActivity())
                                            .load(R.drawable.tornado)
                                            .into(iv_weather)

                                        "Rain" -> Glide.with(requireActivity())
                                            .load(R.drawable.rainy)
                                            .into(iv_weather)

                                        "Drizzle" -> Glide.with(requireActivity())
                                            .load(R.drawable.shower)
                                            .into(iv_weather)

                                        "Thunderstorm" -> Glide.with(requireActivity())
                                            .load(R.drawable.rain_stormy)
                                            .into(iv_weather)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                                Snackbar.make(
                                    iv_weather,
                                    "Error: ${t.message}",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("Weather", error.message)
                }
            })
    }

    companion object {
        private const val STATE_LIST = "state_list"
        private const val TAG = "HomeFragment"
    }
}