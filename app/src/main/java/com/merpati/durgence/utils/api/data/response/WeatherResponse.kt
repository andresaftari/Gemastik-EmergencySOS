package com.merpati.durgence.utils.api.data.response

import com.google.gson.annotations.SerializedName
import com.merpati.durgence.utils.api.data.local.Weather

data class WeatherResponse(
    @SerializedName("weather")
    val weather: List<Weather>,
)