package com.papigelvez.a5palas12.map

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityMapBinding
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.profile.ProfileActivity
import com.papigelvez.a5palas12.search.SearchActivity

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var currentLocation: Location
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationUser()

        initUI()

    }

    //obtener ubicacion actual y mostrar toast en el mapa con las coordenadas
    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), permissionCode)
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                Toast.makeText(applicationContext, "${currentLocation.latitude}, ${currentLocation.longitude}", Toast.LENGTH_LONG).show()

                val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    //solicitar permiso para utilizar ubicacion
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0]==
                PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationUser()
            }
        }
    }

    //poner los pinpoints de ubicacion actual y restaurantes
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        var currentLocationMarker = MarkerOptions().position(latLng).title("Current Location")

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
        mMap.addMarker(currentLocationMarker)

        fetchAllRestaurants()

    }

    //obtener los restaurantes desde firestore y pinear en el mapa
    private fun fetchAllRestaurants() {
        val db = Firebase.firestore
        val restaurantes = db.collection("restaurants")

        restaurantes.get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully fetched ${documents.size()} documents")
                for (document in documents) {
                    val lat = document.getDouble("latitude")
                    val lng = document.getDouble("longitude")
                    val name = document.getString("name")

                    if (lat != null && lng != null && name != null) {
                        val location = LatLng(lat, lng)
                        mMap.addMarker(MarkerOptions().position(location).title(name))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting restaurant documents: ", exception)
                Toast.makeText(this, "Failed to fetch restaurants: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun initUI() {
        initListeners()
    }

    private fun initListeners() {
        binding.linearColumnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        binding.linearColumnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.linearColumnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

}