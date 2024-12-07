package com.papigelvez.a5palas12.business_center

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityCreateBusinessBinding
import com.papigelvez.a5palas12.databinding.ActivityProfileBinding
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.login.LoginActivity
import com.papigelvez.a5palas12.map.MapActivity
import com.papigelvez.a5palas12.profile.ProfileActivity
import com.papigelvez.a5palas12.search.SearchActivity

class CreateBusinessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBusinessBinding
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationPermissionRequestCode = 1001

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityCreateBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("LoginCreds", Context.MODE_PRIVATE)

        //revisar permisos
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //si no tiene, solicitar
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            //ya se tienen los permisos
            fetchCurrentLocation()
        }

        initUI()
    }

    private fun initUI() {
        initListeners()
    }

    private fun initListeners() {
        binding.linearColumnMaps.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        binding.linearColumnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.linearColumnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        binding.btnSaveBusiness.setOnClickListener {
            saveBusiness()
        }
    }

    private fun saveBusiness() {
        val businessName = binding.etBusinessName.text.toString().trim()
        val businessLatitude = binding.etBusinessLatitude.text.toString().toDoubleOrNull()
        val businessLongitude = binding.etBusinessLongitude.text.toString().toDoubleOrNull()
        val businessPhoto = binding.etBusinessPhoto.text.toString().trim()
        val businessCategories = binding.etBusinessCategories.text.toString().split(",").map { it.trim() }
        val businessDescription = binding.etBusinessDescription.text.toString().trim()
        val businessRating = binding.etBusinessRating.text.toString().toDoubleOrNull()
        val businessAddress = binding.etBusinessAddress.text.toString().trim()

        if (businessName.isEmpty() || businessLatitude == null || businessLongitude == null ||
            businessPhoto.isEmpty() || businessCategories.isEmpty() || businessDescription.isEmpty() ||
            businessRating == null || businessRating !in 1.0..5.0 || businessAddress.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
        } else {
            val documentRef = firestore.collection("restaurants").document()
            val id = documentRef.id

            val restaurantData = hashMapOf(
                "id" to id,
                "name" to businessName,
                "latitude" to businessLatitude,
                "longitude" to businessLongitude,
                "photo" to businessPhoto,
                "categories" to businessCategories,
                "description" to businessDescription,
                "rating" to businessRating,
                "address" to businessAddress
            )
            firestore.collection("restaurants")
                .add(restaurantData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Restaurant added successfully!", Toast.LENGTH_SHORT).show()

                    //agregar el nombre del restaurante como atributo del usuario
                    val userEmail = sharedPreferences.getString("email", null)

                    firestore.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnSuccessListener { userSnapshot ->
                            if (!userSnapshot.isEmpty) {
                                val userDocument = userSnapshot.documents.first()
                                val userRef = userDocument.reference

                                userRef.update("restaurant", businessName)
                                    .addOnSuccessListener {
                                        //agregar restaurante a sharedPreferences
                                        sharedPreferences.edit().putString("userRestaurant", businessName).apply()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to update user: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to find user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    //cerrar actividad y redirigir a ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add restaurant: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    //obtener latitud y longitud del usuario
    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    //colocar latitud y longitud en las dos casillas correspondientes del formulario
                    binding.etBusinessLatitude.setText(latitude.toString())
                    binding.etBusinessLongitude.setText(longitude.toString())
                } else {
                    Toast.makeText(this, "Unable to fetch location.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}