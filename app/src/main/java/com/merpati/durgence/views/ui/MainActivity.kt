package com.merpati.durgence.views.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.andresaftari.durgence.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.merpati.durgence.views.ui.fragments.HomeFragment
import com.merpati.durgence.views.ui.fragments.MenuFragment
import com.merpati.durgence.views.ui.fragments.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.component_toolbar.*

class MainActivity : AppCompatActivity() {
    private var currentId: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToolbar()

        bottom_nav.itemIconTintList = null
        bottom_nav.setOnNavigationItemSelectedListener(onNavigationItemSelected)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, HomeFragment())
            commit()
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private val onNavigationItemSelected =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val fragments = fun(fragmentId: Int, fragment: Fragment) {
                if (currentId != fragmentId) supportFragmentManager.beginTransaction().apply {
                    replace(R.id.nav_host_fragment, fragment)
                    commit()
                }
            }

            when (item.itemId) {
                R.id.action_home -> fragments(R.id.action_home, HomeFragment())
                R.id.action_menu -> fragments(R.id.action_menu, MenuFragment())
                R.id.action_profile -> fragments(R.id.action_profile, ProfileFragment())
            }

            currentId = item.itemId
            true
        }
}