package com.papigelvez.a5palas12.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivitySearchBinding
import com.papigelvez.a5palas12.entities.RestaurantEntity
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.home.HomeModel
import com.papigelvez.a5palas12.home.HomeRestaurantAdapter
import com.papigelvez.a5palas12.home.HomeViewModel
import com.papigelvez.a5palas12.home.HomeViewModelFactory
import com.papigelvez.a5palas12.map.MapActivity
import com.papigelvez.a5palas12.profile.ProfileActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: HomeRestaurantAdapter
    private lateinit var viewModel: HomeViewModel
    private var restaurantList: List<RestaurantEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        firebaseAnalytics = Firebase.analytics

        binding = ActivitySearchBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initUI()

        searchView = binding.svBar
        searchRecyclerView = binding.rvSearchedRestaurants

        adapter = HomeRestaurantAdapter(mutableListOf())
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = adapter

        val factory = HomeViewModelFactory(HomeModel(applicationContext), this)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        viewModel.restaurantList.observe(this) { restaurantList ->
            adapter.updateRestaurants(restaurantList) // Update adapter with the full list
            setupSearch(restaurantList)
        }

        restaurantList = intent.getParcelableArrayListExtra("restaurants") ?: emptyList()
        adapter.updateRestaurants(restaurantList)
        setupSearch(restaurantList)

    }

    private fun setupSearch(restaurantList: List<RestaurantEntity>) {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    searchRecyclerView.visibility = View.GONE

                    binding.btnSushi.visibility = View.VISIBLE
                    binding.btnVegan.visibility = View.VISIBLE
                    binding.btnBuffet.visibility = View.VISIBLE
                    binding.btnGourmet.visibility = View.VISIBLE
                    binding.btnColombian.visibility= View.VISIBLE
                    binding.btnInternational.visibility = View.VISIBLE
                } else {
                    //mostrar recyclerview con filtros de busqueda y desaparecer botones
                    searchRecyclerView.visibility = View.VISIBLE

                    binding.btnSushi.visibility = View.GONE
                    binding.btnVegan.visibility = View.GONE
                    binding.btnBuffet.visibility = View.GONE
                    binding.btnGourmet.visibility = View.GONE
                    binding.btnColombian.visibility= View.GONE
                    binding.btnInternational.visibility = View.GONE

                    val filteredList = restaurantList.filter { restaurant ->
                        restaurant.name.contains(newText, ignoreCase = true)
                    }
                    adapter.updateRestaurants(filteredList)
                }
                return true
            }
        })
    }

    private fun filterByCategory(restaurantList: List<RestaurantEntity>, category: String) {
        val filteredList = restaurantList.filter { restaurant ->
            category in restaurant.categories
        }
        adapter.updateRestaurants(filteredList)

        // Optionally show a message if no restaurants match the filter
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No restaurants found for $category", Toast.LENGTH_SHORT).show()
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
        binding.linearColumnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.linearColumnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            val params = Bundle()
            params.putString("tapped_feature", "Home Feature")
            firebaseAnalytics.logEvent("features", params)
        }
        binding.btnColombian.setOnClickListener {
            searchRecyclerView.visibility = View.VISIBLE
            filterByCategory(restaurantList, "Colombiana")
        }
        binding.btnSushi.setOnClickListener {
            searchRecyclerView.visibility = View.VISIBLE
            filterByCategory(restaurantList, "Sushi")
        }
        binding.btnGourmet.setOnClickListener {
            searchRecyclerView.visibility = View.VISIBLE
            filterByCategory(restaurantList, "Gourmet")
        }
        binding.btnInternational.setOnClickListener {
            searchRecyclerView.visibility = View.VISIBLE
            filterByCategory(restaurantList, "Internacional")
        }
        binding.btnBuffet.setOnClickListener {
            searchRecyclerView.visibility = View.VISIBLE
            filterByCategory(restaurantList, "Buffet")
        }
        binding.btnVegan.setOnClickListener {
            searchRecyclerView.visibility = View.VISIBLE
            filterByCategory(restaurantList, "Vegetariano")
        }
    }
}