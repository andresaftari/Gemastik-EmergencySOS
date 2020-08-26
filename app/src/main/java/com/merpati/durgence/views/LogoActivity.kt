package com.merpati.durgence.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andresaftari.durgence.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.model.Users
import kotlinx.android.synthetic.main.activity_logo.*

class LogoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) checkAlreadyUser(user)
        else {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            pb_loading.visibility = View.GONE
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
                            else
                                startActivity(
                                    Intent(
                                        this@LogoActivity,
                                        MainActivity::class.java
                                    )
                                )
                            finish()
                        }
                    } else {
                        this@LogoActivity.database.child(DB_USERS).child(user.uid).setValue(
                            Users(
                                user.uid,
                                "",
                                "",
                                "",
                                "",
                                "0"
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
            })
        }
    }
}