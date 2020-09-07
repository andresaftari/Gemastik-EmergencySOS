package com.merpati.durgence.views.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andresaftari.durgence.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.currentLat
import com.merpati.durgence.currentLng
import com.merpati.durgence.model.Services
import com.merpati.durgence.model.Users
import com.merpati.durgence.utils.adapter.ListAdapter
import com.merpati.durgence.utils.helper.LocationHelper
import kotlinx.android.synthetic.main.fragment_home.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STATE_LIST, list)
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
            Log.i(TAG, "list: $list\n")
        }
        return list
    }

    @SuppressLint("SetTextI18n")
    private fun helloUser() = database.child(DB_USERS).child(auth.uid!!).apply {
        addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get user name
                val userData = snapshot.getValue(Users::class.java)
                val username = userData?.name.toString()

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

                Log.i(TAG, tv_welcome?.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "Failed! ${error.message}")
            }
        })
    }

    private fun populateViews() {
        try {
            rv_services?.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(activity)
                adapter = ListAdapter(list)
            }
        } catch (e: Exception) {
            Log.i(TAG, "${e.message} --- ${e.printStackTrace()}")
        } catch (e: KotlinNullPointerException) {
            Log.i(TAG, "${e.message} --- ${e.printStackTrace()}")
        }
    }

    companion object {
        private const val STATE_LIST = "state_list"
        private const val TAG = "HomeFragment"
    }
}