package com.papigelvez.a5palas12.map

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.papigelvez.a5palas12.entities.RestaurantEntity

class MapModel {

    //obtener restaurantes de firestore
    fun fetchRestaurants(onSuccess: (List<RestaurantEntity>) -> Unit, onFailure: (String) -> Unit) {
        val db = Firebase.firestore
        val restaurantsCollection = db.collection("restaurants")

        restaurantsCollection.get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully fetched ${documents.size()} documents")
                val restaurants = documents.mapNotNull { document ->
                    val name = document.getString("name")
                    val address = document.getString("address") ?: ""
                    val description = document.getString("description") ?: ""
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val rating = document.getDouble("rating") ?: 0.0
                    val photo = document.getString("photo") ?: ""
                    val categories = document.get("categories") as List<String>
                    if (name != null && latitude != null && longitude != null) {
                        RestaurantEntity(name, address, description, latitude, longitude, rating, photo, categories)
                    } else null
                }
                onSuccess(restaurants)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting restaurant documents: ", exception)
                onFailure(exception.message ?: "No se encontro la coleccion de restaurantes")
            }
    }
}
