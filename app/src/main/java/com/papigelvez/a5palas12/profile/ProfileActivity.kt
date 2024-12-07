package com.papigelvez.a5palas12.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.business_center.CreateBusinessActivity
import com.papigelvez.a5palas12.databinding.ActivityProfileBinding
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.login.LoginActivity
import com.papigelvez.a5palas12.map.MapActivity
import com.papigelvez.a5palas12.search.SearchActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfileBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences("LoginCreds", Context.MODE_PRIVATE)

        setContentView(binding.root)

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
        //quitar email de usuario de local storage y redirigir a login
        binding.linearRowLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.remove("email")
            editor.apply()

            val intent = Intent("LOGOUT_EVENT")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(loginIntent)

            finish()
        }
        //si se presiona el boton businesscenter, desaparecer el menu de perfil y mostrar el de businesscenter
        binding.linearRowBusinessCenter.setOnClickListener {
            val userRestaurant = sharedPreferences.getString("userRestaurant", "")
            if (userRestaurant.isNullOrEmpty()) {
                binding.tvCreateBusiness.visibility = View.VISIBLE
            } else {
                binding.tvDeleteBusiness.visibility = View.VISIBLE
            }
            binding.linearUserprofile.visibility = View.GONE
            binding.businessCenterMenu.visibility = View.VISIBLE
        }
        //si se presiona el boton de atras, desaparecer el menu de businesscenter y mostrar el de perfil
        binding.backLayout.setOnClickListener {
            binding.linearUserprofile.visibility = View.VISIBLE
            binding.businessCenterMenu.visibility = View.GONE
        }
        //redirigir a la actividad de crear negocio
        binding.tvCreateBusiness.setOnClickListener {
            val intent = Intent(this, CreateBusinessActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvDeleteBusiness.setOnClickListener {
            confirmDeletion()
        }
    }

    private fun confirmDeletion() {
        val userRestaurant = sharedPreferences.getString("userRestaurant", "")
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you wish to delete your restaurant '$userRestaurant'?")
            .setPositiveButton("Yes") { _, _ ->
                deleteRestaurant()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteRestaurant() {
        val userRestaurant = sharedPreferences.getString("userRestaurant", null)
        val userEmail = sharedPreferences.getString("email", null)

        if (userRestaurant.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "No restaurant found to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isConnected()) {
            Toast.makeText(this, "No internet connection. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }

        // Launch a coroutine on the I/O dispatcher for Firestore operations
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete the restaurant from "restaurants" collection
                val querySnapshot = firestore.collection("restaurants")
                    .whereEqualTo("name", userRestaurant)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val restaurantDoc = querySnapshot.documents.first()
                    restaurantDoc.reference.delete().await()

                    // Delete the "restaurant" attribute from the user document
                    deleteUserRestaurant(userEmail, userRestaurant)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Restaurant not found in Firestore.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Failed to delete restaurant: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun deleteUserRestaurant(email: String, restaurant: String) {
        try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val userDoc = querySnapshot.documents.first()
                userDoc.reference.update("restaurant", FieldValue.delete()).await()

                // eliminar el restaurante de sharedPreferences
                withContext(Dispatchers.Main) {
                    val editor = sharedPreferences.edit()
                    editor.remove("userRestaurant")
                    editor.apply()

                    Toast.makeText(this@ProfileActivity, "Restaurant deleted successfully.", Toast.LENGTH_SHORT).show()
                    binding.tvDeleteBusiness.visibility = View.GONE
                    binding.tvCreateBusiness.visibility = View.VISIBLE
                }

            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "User document not found in Firestore.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileActivity, "Failed to update user document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}