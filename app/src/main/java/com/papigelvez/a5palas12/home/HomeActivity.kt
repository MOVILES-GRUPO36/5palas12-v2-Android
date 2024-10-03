package com.papigelvez.a5palas12.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityHomeBinding
import com.papigelvez.a5palas12.map.MapActivity
import com.papigelvez.a5palas12.register.RegisterActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.linearColumnmaps.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}