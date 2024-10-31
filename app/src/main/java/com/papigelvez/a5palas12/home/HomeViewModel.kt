package com.papigelvez.a5palas12.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.papigelvez.a5palas12.entities.RestaurantEntity

class HomeViewModel(private val model: HomeModel) : ViewModel() {

    private val _restaurantList = MutableLiveData<List<RestaurantEntity>>()
    val restaurantList: LiveData<List<RestaurantEntity>> get() = _restaurantList

    init {
        fetchRestaurants()
    }

    private fun fetchRestaurants() {
        model.fetchRestaurants(
            onSuccess = { restaurants ->
                _restaurantList.value = restaurants
            },
            onFailure = { exception ->
                exception.printStackTrace()
            }
        )
    }
}