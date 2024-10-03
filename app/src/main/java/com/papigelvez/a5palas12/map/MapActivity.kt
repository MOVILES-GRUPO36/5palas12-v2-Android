package com.papigelvez.a5palas12.map

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityMapBinding
import com.papigelvez.a5palas12.home.HomeActivity

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



        binding.linearColumnhome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

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
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")

        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
        googleMap?.addMarker(markerOptions)

    }
}