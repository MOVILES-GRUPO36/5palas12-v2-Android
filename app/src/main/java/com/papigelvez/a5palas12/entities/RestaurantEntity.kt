package com.papigelvez.a5palas12.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RestaurantEntity (
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Double = 0.0,
    val photo: String = "",
    val categories: List<String> = emptyList()
) : Parcelable