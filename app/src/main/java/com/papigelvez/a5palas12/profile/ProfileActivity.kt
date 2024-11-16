package com.papigelvez.a5palas12.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityProfileBinding
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.login.LoginActivity
import com.papigelvez.a5palas12.map.MapActivity
import com.papigelvez.a5palas12.search.SearchActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val sharedPreferences by lazy { getSharedPreferences("LoginCreds", Context.MODE_PRIVATE) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfileBinding.inflate(layoutInflater)

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
        binding.linearRowLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.remove("email")
            editor.remove("password")
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}