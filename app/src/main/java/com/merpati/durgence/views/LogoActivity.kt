package com.merpati.durgence.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.andresaftari.durgence.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.model.Users

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