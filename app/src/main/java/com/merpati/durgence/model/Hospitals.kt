package com.merpati.durgence.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Hospitals(
    val thumbnail: Int,
    val nameIndo: String,
    val location: String,
    val range: String
) : Parcelable