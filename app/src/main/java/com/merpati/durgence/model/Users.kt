package com.merpati.durgence.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// Model will be use on read data later
@Parcelize
data class Users(
    val uid: String = "",
    val name: String = "",
    val number: String = "",
    val status: String = "",
    val face: String? = "",
    val fullname: String? = "",
    val age: String? = "",
    val latitude: String? = "",
    val longitude: String? = ""
) : Parcelable