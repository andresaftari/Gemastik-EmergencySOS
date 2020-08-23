package com.merpati.durgence.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Users(
    val uid: String? = null,
    val name: String? = null,
    val number: String? = null
//    val latitude: String? = null,
//    val longitude: String? = null
) : Parcelable