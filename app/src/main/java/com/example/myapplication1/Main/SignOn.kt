package com.example.myapplication1.Main


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.myapplication1.User.StatusSignOnUser

import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.ActivitySignOnBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import kotlin.coroutines.CoroutineContext

class SignOn : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivitySignOnBinding

    private lateinit var btn_back: AppCompatButton
    private lateinit var btn_register: AppCompatButton
    private lateinit var name: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var password_check: TextInputEditText
    private lateinit var email: TextInputEditText


    private var url_logo: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var user_Item_list: MutableList<UserItem>


    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignOnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val this_activity = this
        job = Job()

        //Activity buttons
        btn_back = binding.btnBack
        btn_register = binding.btnAccept

        //Back button
        btn_back.setOnClickListener {
            onBackPressed()
        }


        //binding user attributes
        name = binding.editTextName
        password = binding.editTextPassword
        password_check = binding.editTextPasswordCheck
        email = binding.editTextEmail


        btn_register.setOnClickListener {
             setup()


        }

    }

    private fun setup() {

        val this_activity = this
        val calendar = Calendar.getInstance().time
        val dateFormat = DateFormat.getDateInstance().format(calendar)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()
        user_Item_list = Utilities.getUserList(db_ref)


        if (name.text.toString().trim().isEmpty() ||
            password.text.toString().trim().isEmpty() ||
            password_check.text.toString().trim().isEmpty() ||
            email.text.toString().trim().isEmpty()
        ) {
            Log.e("RegisterActivity", "Missing data in the form")
            Toast.makeText(
                applicationContext, "Missing data in the form", Toast.LENGTH_SHORT
            ).show()
        } else {
            db_ref.child("Shop").child("Users").orderByChild("name")
                .equalTo(name.text.toString().trim())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // El nombre de usuario ya existe
                            Log.e("RegisterActivity", "This username already exists. Please choose a different one.")
                            Toast.makeText(
                                applicationContext,
                                "This username already exists. Please choose a different one.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // El nombre de usuario no existe, puedes continuar con el registro
                            registerUser()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Error al buscar el nombre de usuario
                        Log.e("RegisterActivity", "Error checking username: ${databaseError.message}")
                        Toast.makeText(
                            applicationContext,
                            "Error checking username: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun registerUser() {
        val this_activity = this
        val calendar = Calendar.getInstance().time
        val dateFormat = DateFormat.getDateInstance().format(calendar)

        if (password.text.toString() != password_check.text.toString()) {
            Log.e("RegisterActivity", "Passwords do not match")
            Toast.makeText(
                applicationContext, "Passwords do not match", Toast.LENGTH_SHORT
            ).show()
        } else {
            var id_gen: String? = db_ref.child("Shop").child("Users").push().key

            launch {
                val androidId =
                    Settings.Secure.getString(
                        applicationContext.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Registro exitoso
                            val user = task.result?.user
                            if (user != null) {
                                // Aquí puedes realizar cualquier acción adicional, como agregar el usuario a la base de datos
                                Log.d("RegisterActivity", "User registration successful")
                                val userType = if(email.text.toString().contains("@admin")) "admin" else "user"
                                val id_gen = user.uid
                                Utilities.user_add(
                                    db_ref,
                                    id_gen,
                                    userType,
                                    name.text.toString(),
                                    email.text.toString(),
                                    password.text.toString(),
                                    dateFormat.toString(),
                                    "",
                                    StatusSignOnUser.CREATED,
                                    androidId
                                )

                                showLogin(user.email ?: "", ProviderType.BASIC)
                                Toast.makeText(
                                    applicationContext,
                                    "User created",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBackPressed()

                                Utilities.courrutine_thing(
                                    this_activity,
                                    applicationContext,
                                    "User successfully created"
                                )

                                val activity = Intent(applicationContext, Login::class.java)
                                startActivity(activity)
                            } else {

                                // El usuario es nulo, manejar el caso de manera adecuada
                                Toast.makeText(
                                    applicationContext,
                                    "User already exist",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Error al registrar al usuario
                            val exception = task.exception
                            if (exception != null) {
                                Log.d("RegisterActivity", "Error creating user: ${exception.message}")


                                Log.e(
                                    "RegisterActivity",
                                    "Error al registrar usuario: ${exception.message}"
                                )
                                Toast.makeText(
                                    applicationContext,
                                    "Error creating user: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }
    }

    fun showLogin(email: String, provider: ProviderType) {
        val activity = Intent(applicationContext, Login::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(activity)
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
}
