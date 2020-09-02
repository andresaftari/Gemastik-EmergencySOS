package com.merpati.durgence.views.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andresaftari.durgence.R
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.views.ui.fragments.HomeFragment
import com.merpati.durgence.views.ui.fragments.MenuFragment
import com.merpati.durgence.views.ui.fragments.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        auth = FirebaseAuth.getInstance()
//        database = FirebaseDatabase.getInstance().reference

//        btn_logout.setOnClickListener {
//            database.child(DB_USERS).child(auth.uid!!).child("status").setValue("0")
//            auth.signOut()
//            startActivity(Intent(this, RegisterActivity::class.java))
//        }

        supportActionBar?.title = null

        bottom_nav.setOnTabSelectedListener { position, _ ->
            when (position) {
                0 -> {
                    val home = HomeFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, home)
                        .commit()
                }
                1 -> {
                    val menu = MenuFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, menu)
                        .commit()
                }
                2 -> {
                    val profiles = ProfileFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, profiles)
                        .commit()
                }
            }
            return@setOnTabSelectedListener true
        }
        createBottomNavigation()
    }

    private fun createBottomNavigation() {
        val home = AHBottomNavigationItem("HOME", R.drawable.ic_home_on)
        val menu = AHBottomNavigationItem("MENU", R.drawable.ic_aid_on)
        val profile = AHBottomNavigationItem("PROFILE", R.drawable.ic_profile_account)

        bottom_nav.apply {
            addItem(home)
            addItem(menu)
            addItem(profile)
            titleState = AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE
            currentItem = 0
        }
    }
}