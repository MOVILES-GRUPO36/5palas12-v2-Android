package com.papigelvez.a5palas12.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelFactory(private val model: HomeModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(model) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
