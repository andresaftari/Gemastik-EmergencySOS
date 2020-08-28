package com.merpati.durgence

import android.annotation.SuppressLint
import android.app.Activity
import com.andresaftari.durgence.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.merpati.durgence.views.RegisterActivity

@SuppressLint("InflateParams")
object CustomDialogs {
    fun dialogOnRequestOTP(activity: Activity, handler: RegisterActivity.Companion.OnRequestOTP) {
        val layoutInflater = activity.layoutInflater
        val view = layoutInflater.inflate(R.layout.dialog_otp, null)
        handler.onLoad(view)

        val builder = MaterialAlertDialogBuilder(activity)
        builder.apply {
            setView(view)
            setPositiveButton(R.string.verification) { dialog, _ ->
                handler.onItemClick(dialog, 0)
                dialog.dismiss()
            }
        }
    }
}