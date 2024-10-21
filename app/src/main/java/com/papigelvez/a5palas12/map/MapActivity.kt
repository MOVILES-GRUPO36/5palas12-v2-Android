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

    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), permissionCode)
            return
        }
        val getLocation = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            location ->

            if (location != null) {
                currentLocation = location
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()


                val mapFragment = supportFragmentManager.findFragmentById(
                    R.id.map_fragment) as SupportMapFragment
                mapFragment.getMapAsync(this)


            }
        }
    }

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

    override fun onMapReady(googleMap: GoogleMap) {

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        var markerOptions = MarkerOptions().position(latLng).title("Current Location")

        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
        googleMap?.addMarker(markerOptions)

        fetchRestaurantLocation { document ->
            if (document != null) {
                // Process the document
                val lat = document.getDouble("lat")
                val lng = document.getDouble("lng")
                // Handle the location or mark it on the map
                if (lat != null && lng != null) {
                    val location = LatLng(lat, lng)
                    markerOptions = MarkerOptions().position(latLng)
                    googleMap.addMarker(MarkerOptions()
                        .position(location)
                        .title(document.getString("name")))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                }

            }
        }

    }

    private fun fetchRestaurantLocation(callback: (DocumentSnapshot?) -> Unit) {
        val db = Firebase.firestore
        val lugares = db.collection("lugares")

        val docRef = lugares.document("restLaCand1")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    callback(document) // Return the document via callback
                } else {
                    Log.d(TAG, "No such document")
                    callback(null) // Document does not exist
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                callback(null) // Return null on failure
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