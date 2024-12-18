package com.papigelvez.a5palas12.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityMapBinding
import com.papigelvez.a5palas12.entities.RestaurantEntity
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.profile.ProfileActivity
import com.papigelvez.a5palas12.search.SearchActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel: MapViewModel by viewModels()
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //observar el ViewModel
        observeViewModel()

        //obtener ubicacion actual
        viewModel.getCurrentLocationUser(this)

        //cargar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //botones
        initUI()

        //revisar conectividad
        startConnectivityChecker()
    }

    override fun onResume() {
        super.onResume()
        //volver a obtener la ubicacion del usuario
        viewModel.getCurrentLocationUser(this)
    }

    private fun observeViewModel() {
        //observar ubicacion actual
        viewModel.currentLocation.observe(this) { location ->
            updateCurrentLocationMarker(location)
            viewModel.fetchAllRestaurants()
        }

        //observar data de restaurantes
        viewModel.restaurantList.observe(this) { restaurants ->
            updateRestaurantMarkers(restaurants)
        }

        //observar toasts
        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    //pinear en el mapa la ubicacion actual si el mapa esta inicializado
    private fun updateCurrentLocationMarker(location: Location) {
        if (::mMap.isInitialized) {

            //en caso de que la ubicacion del usuario cambie
            mMap.clear()

            val latLng = LatLng(location.latitude, location.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            mMap.addMarker(MarkerOptions().position(latLng).title("Current Location").icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

            viewModel.fetchAllRestaurants()
        }
    }

    //pinear en el mapa los restaurantes obtenidos
    private fun updateRestaurantMarkers(restaurants: List<RestaurantEntity>) {
        if (::mMap.isInitialized) {
            for (restaurant in restaurants) {
                val location = LatLng(restaurant.latitude, restaurant.longitude)
                mMap.addMarker(MarkerOptions().position(location).title(restaurant.name))
            }
        }
    }

    //una vez el mapa este listo, obtener los restaurantes para pinearlos
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //viewModel.fetchAllRestaurants()
    }

    //solicitar permiso para utilizar ubicacion
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.getCurrentLocationUser(this)
        }
    }

    private fun startConnectivityChecker() {
        //lanzar la corrutina
        lifecycleScope.launch {
            while (true) {
                if (!isConnected()) {
                    //mostrar el mensaje de failed to load y el boton de retry
                    binding.btnRetry.visibility = View.VISIBLE
                    Toast.makeText(this@MapActivity, "Failed to load map. Check your connection.", Toast.LENGTH_LONG).show()
                } else {
                    //desaparecer el boton de retry
                    binding.btnRetry.visibility = View.GONE
                }
                delay(5000) // cada 5 segundos
            }
        }

        binding.btnRetry.setOnClickListener {
            viewModel.fetchAllRestaurants()
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun initUI() {
        initListeners()
    }

    private fun initListeners() {
        binding.linearColumnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            val params = Bundle()
            params.putString("tapped_feature", "Home Feature")
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