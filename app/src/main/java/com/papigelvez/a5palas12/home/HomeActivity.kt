package com.papigelvez.a5palas12.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivityHomeBinding
    private lateinit var restaurantAdapter: HomeRestaurantAdapter
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(HomeModel(applicationContext), this) }
    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var logoutReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        firebaseAnalytics = Firebase.analytics

        binding = ActivityHomeBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences("LoginCreds", Context.MODE_PRIVATE)

        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        fetchUserRestaurant()

        initUI()

        //terminar la actividad si se hace logout
        logoutReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "LOGOUT_EVENT") {
                    finish()
                }
            }
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(logoutReceiver, IntentFilter("LOGOUT_EVENT"))
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
            intent.putParcelableArrayListExtra("restaurants", ArrayList(viewModel.restaurantList.value ?: emptyList()))
            startActivity(intent)

            val params = Bundle()
            params.putString("tapped_feature", "Search Feature")
            firebaseAnalytics.logEvent("features", params)
        }
        //agregar granularidad temporal
        binding.linearColumnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)

        }
    }

    private fun fetchUserRestaurant() {
        CoroutineScope(Dispatchers.Main).launch {
            //obtener el email del usuario loggeado
            val email = sharedPreferences.getString("email", null)
            if (email != null) {
                try {
                    //buscar documento de usuario que tenga ese email, snapshot pueden ser varios pero solo hay 1 por usuario
                    val userSnapshot = firestore.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .await()

                    //si se encontro el documento
                    if (!userSnapshot.isEmpty) {
                        val userDocument = userSnapshot.documents.first()
                        val restaurant = userDocument.getString("restaurant") ?: ""

                        //guardar restaurante en sharedpreferences
                        sharedPreferences.edit()
                            .putString("userRestaurant", restaurant)
                            .apply()

                    } else {
                        Toast.makeText(this@HomeActivity, "No user found with this email.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@HomeActivity, "Error fetching user data. Check your connection.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logoutReceiver)
    }
}