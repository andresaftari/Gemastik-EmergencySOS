package com.merpati.durgence.views

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.merpati.durgence.DB_USERS
import com.andresaftari.durgence.R
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userID: String

    companion object {
        const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        btn_register.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        pb_loading?.visibility = View.VISIBLE

        val name = editUserName.editText?.text.toString()
        val number = editUserNumber.editText?.text.toString()

        val newName = "$name@durgence.merpati.com"

        try {
            if (name.isEmpty() || number.isEmpty()) validateForms()
            else {
                userID = auth.currentUser!!.uid

                auth.createUserWithEmailAndPassword(newName, number).apply {
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            Snackbar.make(
                                btn_register,
                                "Welcome, $name!",
                                Snackbar.LENGTH_SHORT
                            ).show()

                            val newNumber = "+62${number.substring(1, number.length)}"

                            database.apply {
                                child(DB_USERS).child(userID).child("Name").setValue(name)
                                child(DB_USERS).child(userID).child("Number").setValue(newNumber)
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
                            "Failed! ${e.message} === ${e.printStackTrace()}",
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
}