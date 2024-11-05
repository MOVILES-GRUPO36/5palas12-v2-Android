package com.papigelvez.a5palas12.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityHomeBinding
import com.papigelvez.a5palas12.entities.RestaurantEntity
import com.papigelvez.a5palas12.map.MapActivity
import com.papigelvez.a5palas12.profile.ProfileActivity
import com.papigelvez.a5palas12.register.RegisterActivity
import com.papigelvez.a5palas12.search.SearchActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivityHomeBinding
    private lateinit var restaurantAdapter: HomeRestaurantAdapter
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(HomeModel(applicationContext), this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        firebaseAnalytics = Firebase.analytics

        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        initUI()
    }

    //binding del adapter
    private fun setupRecyclerView() {
        restaurantAdapter = HomeRestaurantAdapter(mutableListOf())
        binding.rvRestaurants.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = restaurantAdapter
        }
    }

    //observar los restaurantes fetcheados
    private fun observeViewModel() {
        viewModel.restaurantList.observe(this) { restaurants ->
            restaurantAdapter.updateRestaurants(restaurants)
        }
    }


    private fun initUI() {
        initListeners()
    }

    private fun initListeners() {
        binding.linearColumnMaps.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)

            val params = Bundle()
            params.putString("tapped_feature", "Map Feature")
            firebaseAnalytics.logEvent("features", params)
        }
        binding.linearColumnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)

            val params = Bundle()
            params.putString("tapped_feature", "Search Feature")
            firebaseAnalytics.logEvent("features", params)
        }
        binding.linearColumnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)

        }
    }

}