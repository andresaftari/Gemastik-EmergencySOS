package com.merpati.durgence.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Services(
    val thumbnail: Int,
    val nameIndo: String?,
    val nameEng: String?
) : Parcelable