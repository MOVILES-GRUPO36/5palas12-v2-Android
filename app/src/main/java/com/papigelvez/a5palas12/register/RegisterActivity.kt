package com.papigelvez.a5palas12.register

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.papigelvez.a5palas12.R
import java.text.SimpleDateFormat
import com.papigelvez.a5palas12.databinding.ActivityRegisterBinding
import com.papigelvez.a5palas12.login.LoginActivity
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        initUI()
    }

    private fun initUI() {
        binding.etName.filters = arrayOf(InputFilter.LengthFilter(30))
        binding.etTelephone.filters = arrayOf(InputFilter.LengthFilter(30))
        binding.etEmail.filters = arrayOf(InputFilter.LengthFilter(30))
        binding.etPassword.filters = arrayOf(InputFilter.LengthFilter(30))
        binding.etConfirmPassword.filters = arrayOf(InputFilter.LengthFilter(30))
        initListeners()
    }

    private fun initListeners() {
        binding.txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val telephone = binding.etTelephone.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (name.isNotEmpty() && telephone.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty()) {
                if (password == confirm) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val currentDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, hh:mm:ssa z", Locale("es", "CO"))
                                val createdAt = currentDate.format(Date())

                                val userData = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "createdAt" to createdAt
                                )

                                firestore.collection("users")
                                    .add(userData)
                                    .addOnSuccessListener {
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        Toast.makeText(this, "User registered and data saved", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to save user data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                val exception = it.exception
                                val errorMessage = when (exception) {
                                    is FirebaseAuthUserCollisionException -> "This email is already registered. Try another one."
                                    is FirebaseNetworkException -> "No internet connection. Please check your network."
                                    else -> exception?.localizedMessage ?: "Registration failed. Please try again."
                                }
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}