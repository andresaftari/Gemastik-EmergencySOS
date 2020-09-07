package com.merpati.durgence.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FirstAid(
    val thumbnail: Int,
    val nameIndo: String
) : Parcelable