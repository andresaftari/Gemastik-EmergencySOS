package com.merpati.durgence.utils.api

import com.merpati.durgence.utils.api.data.response.LocationResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LocationService {
    @FormUrlEncoded
    @POST("cord.php")
    fun postCoordinate(
        @Field("latitude") latitude: String, @Field("longitude") longitude: String
    ): Call<LocationResponse>
}