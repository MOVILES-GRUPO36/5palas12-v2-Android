package com.papigelvez.a5palas12.home

import android.content.ContentValues.TAG
import com.google.firebase.firestore.FirebaseFirestore
import com.papigelvez.a5palas12.entities.RestaurantEntity
import com.bumptech.glide.Glide
import android.content.Context
import android.util.Log


class HomeModel(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchRestaurants(onSuccess: (List<RestaurantEntity>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("restaurants")
            .get()
            .addOnSuccessListener { documents ->
                val restaurantList = documents.map { it.toObject(RestaurantEntity::class.java) }
                onSuccess(restaurantList)

                // cachear imagenes
                restaurantList.forEach { restaurant ->
                    restaurant.photo?.let { photoUrl ->
                        Glide.with(context).load(photoUrl).preload()
                        Log.d(TAG, "Successfully cached $photoUrl image")
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}