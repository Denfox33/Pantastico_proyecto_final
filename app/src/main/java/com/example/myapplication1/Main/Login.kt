package com.example.myapplication1.Main


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.myapplication1.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


enum class ProviderType {
    BASIC,
    GOOGLE
}

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var btn_toggle_login: AppCompatButton
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var btn_login: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Check login state
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // If user is already logged in, redirect to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        btn_toggle_login = binding.btnUserPass
        buttonRegister = binding.buttonRegister
        btn_login = binding.buttonLogin

        btn_toggle_login.setOnClickListener {
            toggleVisibility()
        }

        buttonRegister.setOnClickListener {
            val intent = Intent(this@Login, SignOn::class.java)
            startActivity(intent)
        }


        btn_login.setOnClickListener {
            val email = binding.editTextUser.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Save login state
                            val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true)
                            if (email.endsWith("@admin.com")) {
                                // If the user is an admin, save it in SharedPreferences
                                editor.putBoolean("isAdmin", true)
                            } else {
                                editor.putBoolean("isAdmin", false)
                            }
                            editor.apply()

                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Login, "Authentication error", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this@Login, "Empty email or password", Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun toggleVisibility() {
        with(binding) {
            if (textInputLayoutUser.visibility == View.VISIBLE) {
                textInputLayoutUser.visibility = View.GONE
                textInputLayoutPassword.visibility = View.GONE
                buttonLogin.visibility = View.GONE
                textViewRegister.visibility = View.GONE
                buttonRegister.visibility = View.GONE
                btnGoogle.visibility = View.VISIBLE
                checkboxRememberPassword.visibility = View.GONE
            } else {
                textInputLayoutUser.visibility = View.VISIBLE
                textInputLayoutPassword.visibility = View.VISIBLE
                buttonLogin.visibility = View.VISIBLE
                textViewRegister.visibility = View.VISIBLE
                buttonRegister.visibility = View.VISIBLE
                btnGoogle.visibility = View.GONE
                checkboxRememberPassword.visibility = View.VISIBLE
            }
        }
    }
}