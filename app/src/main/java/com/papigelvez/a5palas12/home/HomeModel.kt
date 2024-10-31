package com.papigelvez.a5palas12.home

import com.google.firebase.firestore.FirebaseFirestore
import com.papigelvez.a5palas12.entities.RestaurantEntity

class HomeModel {

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchRestaurants(onSuccess: (List<RestaurantEntity>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("restaurants")
            .get()
            .addOnSuccessListener { documents ->
                val restaurantList = documents.map { it.toObject(RestaurantEntity::class.java) }
                onSuccess(restaurantList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}