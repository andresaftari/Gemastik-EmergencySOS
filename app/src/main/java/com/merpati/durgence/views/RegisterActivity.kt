package com.merpati.durgence.views

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andresaftari.durgence.R
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.merpati.durgence.DB_USERS
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var userID: String
    private lateinit var newName: String
    private lateinit var email: String
    private lateinit var newNumber: String

    companion object {
        const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        btn_register.setOnClickListener {
            val name = editUserName.editText?.text.toString()

            if (name.contains(" ")) {
                newName = name.replace(" ", "").toLowerCase(Locale.ROOT)
                email = "$newName@durgence.merpati.com"
            } else email = "$newName@durgence.merpati.com".toLowerCase(Locale.ROOT)

            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                val isNewUser = task.result!!.signInMethods!!.isEmpty()

                if (isNewUser) {
                    registerUser()
                    Log.i(TAG, "Is new user!")
                } else {
                    loginUser()
                    Log.i(TAG, "Is old user!")
                }
            }
        }
    }

    private fun validateForms() {
        when {
            TextUtils.isEmpty(editUserName.editText?.text) -> {
                Snackbar.make(
                    btn_register,
                    "Silakan input nama Anda",
                    Snackbar.LENGTH_SHORT
                ).show()

                pb_loading?.visibility = View.GONE
            }

            TextUtils.isEmpty(editUserNumber.editText?.text) -> {
                Snackbar.make(
                    btn_register,
                    "Silakan input nomor HP Anda",
                    Snackbar.LENGTH_SHORT
                ).show()

                pb_loading?.visibility = View.GONE
            }
        }
    }

    private fun loginUser() {
        pb_loading?.visibility = View.VISIBLE

        val name = editUserName.editText?.text.toString()
        val number = editUserNumber.editText?.text.toString()

        if (name.contains(" ")) {
            newName = name.replace(" ", "").toLowerCase(Locale.ROOT)
            email = "$newName@durgence.merpati.com"
        } else email = "$newName@durgence.merpati.com".toLowerCase(Locale.ROOT)

        Log.i(TAG, "Email: ${email.toLowerCase(Locale.ROOT)}")

        try {
            if (name.isEmpty() || number.isEmpty()) validateForms()
            else {
                userID = auth.currentUser!!.uid

                auth.signInWithEmailAndPassword(email, number).apply {
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            Snackbar.make(
                                btn_register,
                                "Welcome, $name!",
                                Snackbar.LENGTH_SHORT
                            ).show()

                            pb_loading?.visibility = View.GONE
                            database.child(DB_USERS).child(userID).child("status").setValue("1")

                            startActivity(
                                Intent(
                                    this@RegisterActivity,
                                    MainActivity::class.java
                                )
                            )
                        } else
                            Snackbar.make(
                                btn_register,
                                "Check your connection!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                    }
                    addOnFailureListener { e: Exception ->
                        Snackbar.make(
                            btn_register,
                            "${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        pb_loading?.visibility = View.GONE
                    }
                }
            }

        } catch (e: FirebaseAuthException) {
            Log.i(TAG, "Failed! ${e.message} --- ${e.printStackTrace()}")
        }
    }

    private fun registerUser() {
        pb_loading?.visibility = View.VISIBLE

        val name = editUserName.editText?.text.toString()
        val number = editUserNumber.editText?.text.toString()

        if (name.contains(" ")) {
            newName = name.replace(" ", "").toLowerCase(Locale.ROOT)
            email = "$newName@durgence.merpati.com"
        } else email = "$newName@durgence.merpati.com".toLowerCase(Locale.ROOT)

        Log.i(TAG, "Email: ${email.toLowerCase(Locale.ROOT)}")

        try {
            if (name.isEmpty() || number.isEmpty()) validateForms()
            else {
                userID = auth.currentUser!!.uid

                auth.createUserWithEmailAndPassword(email, number).apply {
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            newNumber = if (number.substring(1) != "+62")
                                "+62${number.substring(1, number.length)}"
                            else number

                            database.apply {
                                child(DB_USERS).child(userID).child("Name").setValue(name)
                                child(DB_USERS).child(userID).child("Number").setValue(newNumber)
                                child(DB_USERS).child(userID).child("Email").setValue(email)
                                child(DB_USERS).child(userID).child("status").setValue("1")
                            }
                            pb_loading?.visibility = View.GONE

                            startActivity(
                                Intent(
                                    this@RegisterActivity,
                                    MainActivity::class.java
                                )
                            )

                            Log.i(TAG, "+62${number.substring(1, number.length)}")
                            Log.i(TAG, "Hello, $name")
                        }
                    }
                    addOnFailureListener { e: Exception ->
                        Snackbar.make(
                            btn_register,
                            "${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        pb_loading?.visibility = View.GONE
                    }
                }
            }

        } catch (e: FirebaseAuthException) {
            Log.i(TAG, "Failed! ${e.message} --- ${e.printStackTrace()}")
        }
    }
}