package com.papigelvez.a5palas12.map

import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.papigelvez.a5palas12.entities.RestaurantEntity

class MapViewModel : ViewModel() {

    private val mapModel = MapModel()

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> = _currentLocation

    private val _restaurantList = MutableLiveData<List<RestaurantEntity>>()
    val restaurantList: LiveData<List<RestaurantEntity>> = _restaurantList

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    //obtener ubicacion actual
    fun getCurrentLocationUser(activity: Activity) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _currentLocation.value = location
                _toastMessage.value = "Tus coordenadas: ${location.latitude}, ${location.longitude}"
            } else {
                _toastMessage.value = "Location not found"
            }
        }
    }

    //obtener restaurantes del modelo
    fun fetchAllRestaurants() {
        mapModel.fetchRestaurants(
            onSuccess = { restaurants -> _restaurantList.value = restaurants },
            onFailure = { error -> _toastMessage.value = "Failed to fetch restaurants: $error" }
        )
    }
}

