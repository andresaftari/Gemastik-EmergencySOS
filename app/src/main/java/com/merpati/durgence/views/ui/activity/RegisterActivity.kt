package com.merpati.durgence.views.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andresaftari.durgence.R
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.merpati.durgence.DB_USERS
import com.merpati.durgence.model.Users
import com.merpati.durgence.views.dialog.BottomSheetFragment
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var verificationId: String
    private lateinit var phoneNumber: String

    companion object {
        const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        btn_register.setOnClickListener { validateForms() }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun verifyCode(code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithCredential(credential)
        } catch (e: FirebaseAuthException) {
            Log.i(TAG, e.message.toString())
        } catch (e: Exception) {
            Log.i(TAG, e.message.toString())
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) =
        auth.signInWithCredential(credential).apply {
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val name = editUserName.editText?.text.toString()
                    val number = editUserNumber.editText?.text.toString()

                    val intent =
                        Intent(
                            this@RegisterActivity,
                            MainActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("Name", name)
                            putExtra("Number", number)
                        }

                    database.child(DB_USERS).child(auth.uid!!).setValue(
                        Users(
                            auth.uid!!,
                            name,
                            number,
                            "",
                            "",
                            "1"
                        )
                    )
                    startActivity(intent)
                }
            }
            addOnFailureListener { e: Exception ->
                Snackbar.make(
                    btn_verification,
                    "Failed! ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
                Log.i(TAG, "${e.message} = ${e.printStackTrace()}")
            }
        }

    private fun validateForms() {
        when {
            TextUtils.isEmpty(editUserName.editText?.text) -> {
                editUserName.editText?.apply {
                    error = "Silakan input nama Anda"
                    requestFocus()
                }
                pb_loading?.visibility = View.GONE
            }

            TextUtils.isEmpty(editUserNumber.editText?.text) -> {
                editUserNumber.editText?.apply {
                    error = "Silakan input nomor HP Anda"
                    requestFocus()
                }
                pb_loading?.visibility = View.GONE
            }

            editUserNumber.editText?.text!!.length < 10 -> {
                editUserNumber.editText?.apply {
                    error = "Silakan input nomor HP Anda dengan benar"
                    requestFocus()
                }
                pb_loading?.visibility = View.GONE
            }

            else -> registerUser()
        }
    }

    private fun registerUser() {
        pb_loading?.visibility = View.VISIBLE

        val name = editUserName.editText?.text.toString()
        val number = editUserNumber.editText?.text.toString()

        phoneNumber = if (number.substring(1) != "+62")
            "+62${number.substring(1, number.length)}" else number

        Log.i(TAG, "Email: $name = Number: $phoneNumber")

        // Verification
        if (name.isNotEmpty() && number.isNotEmpty()) {
            val bottomSheetFragment = BottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

            val code = editUserOTP.editText?.text.toString()
            verificationId = code

            val callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(
                        codes: String,
                        forceResendingToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(code, forceResendingToken)
                        verificationId = codes
                    }

                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        val codes: String? = phoneAuthCredential.smsCode
                        if (codes != null) {
                            editUserOTP.editText?.setText(codes)
                            verifyCode(codes)
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Snackbar.make(editUserOTP, "Failed! ${e.message}", Snackbar.LENGTH_SHORT)
                            .show()
                        Log.i(TAG, "${e.message} = ${e.printStackTrace()}")
                    }
                }

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                callback
            )

            verifyCode(code)
        }
    }
}