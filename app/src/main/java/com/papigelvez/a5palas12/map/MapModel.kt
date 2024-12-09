package com.papigelvez.a5palas12.map

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import android.util.LruCache
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.papigelvez.a5palas12.entities.RestaurantEntity

class MapModel {

    private val cache: LruCache<String, List<RestaurantEntity>> = LruCache(1)

    //obtener restaurantes de firestore
    fun fetchNearbyRestaurants(userLocation: Location, onSuccess: (List<RestaurantEntity>) -> Unit, onFailure: (String) -> Unit) {

        //la llave es la ubicacion del usuario, el valor es la lista de aquellos rests 10km a la redonda
        val cacheKey = "${userLocation.latitude}_${userLocation.longitude}_10km"
        val cachedRestaurants = getCachedNearbyRestaurants(cacheKey)

        if (cachedRestaurants != null) {
            Log.d(TAG, "Loaded restaurants from cache")
            onSuccess(cachedRestaurants)
            return
        }

        val db = Firebase.firestore
        val restaurantsCollection = db.collection("restaurants")

        restaurantsCollection.get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully fetched ${documents.size()} documents")
                val radius = 10000
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
                        val restaurantLocation = Location("").apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        }
                        if (userLocation.distanceTo(restaurantLocation) <= radius) {
                            // Create RestaurantEntity only if within the radius
                            RestaurantEntity(name, address, description, latitude, longitude, rating, photo, categories)
                        } else null
                    } else null

                    //if (name != null && latitude != null && longitude != null) {
                    //    RestaurantEntity(name, address, description, latitude, longitude, rating, photo, categories)
                    //} else null
                }
                //cachear los restaurantes a 10km a la redonda
                saveNearbyRestaurants(cacheKey, restaurants)
                Log.d(TAG, "Loaded restaurants to cache")
                onSuccess(restaurants)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting restaurant documents: ", exception)
                onFailure(exception.message ?: "No se encontro la coleccion de restaurantes")
            }
    }

    fun clearCache() {
        cache.evictAll()
    }

    fun saveNearbyRestaurants(key: String, restaurants: List<RestaurantEntity>) {
        cache.put(key, restaurants)
    }

    //obtener la lista de restaurantes del cache
    fun getCachedNearbyRestaurants(key: String): List<RestaurantEntity>? {
        return cache.get(key)
    }
}
