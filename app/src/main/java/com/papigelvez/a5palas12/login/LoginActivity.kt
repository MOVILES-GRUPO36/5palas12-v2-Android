package com.papigelvez.a5palas12.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.databinding.ActivityLoginBinding
import com.papigelvez.a5palas12.home.HomeActivity
import com.papigelvez.a5palas12.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val sharedPreferences by lazy { getSharedPreferences("LoginCreds", Context.MODE_PRIVATE) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        checkStoredCredentials()

        initUI()

        enableEdgeToEdge()
    }

    //check if credentials are in SharedPreferences, if they are, redirect to HomeActivity
    private fun checkStoredCredentials() {
        val savedMail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (!savedMail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initUI() {
        binding.etEmail.filters = arrayOf(InputFilter.LengthFilter(30))
        binding.etPassword.filters = arrayOf(InputFilter.LengthFilter(30))
        initListeners()
    }

    private fun initListeners() {
        binding.txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

                    if (it.isSuccessful) {
                        saveCredentials(email, password)
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val exception = it.exception
                        val errorMessage = when (exception) {
                            is FirebaseNetworkException -> "No internet connection. Please check your network."
                            else -> exception?.localizedMessage ?: "Authentication failed. Please try again."
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCredentials(email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }
}
