package com.example.myapplication1.User

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication1.Admin.AssistAdapter
import com.example.myapplication1.Admin.AssistItem
import com.example.myapplication1.Admin.EventsItem
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.Main.Login
import com.example.myapplication1.R
import com.example.myapplication1.databinding.FragmentProfileUserBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ProfileFragmentUser : Fragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentProfileUserBinding? = null
    private var user: UserItem? = null
    private lateinit var databaseReference: DatabaseReference
    private val binding get() = _binding!!
    val pickImageContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            }
        }
    private var imageUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileUserBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Identifica las vistas de las banderas
        val spanishFlag = view.findViewById<ImageView>(R.id.sp)
        val englishFlag = view.findViewById<ImageView>(R.id.uk)
        val frenchFlag = view.findViewById<ImageView>(R.id.fr)

        spanishFlag.setOnClickListener {
            setLocale("es") // Español
            // Recargar la actividad para aplicar los cambios
            requireActivity().recreate()
        }

        englishFlag.setOnClickListener {
            setLocale("en") // Inglés
            // Recargar la actividad para aplicar los cambios
            requireActivity().recreate()
        }

        frenchFlag.setOnClickListener {
            setLocale("fr") // Francés
            // Recargar la actividad para aplicar los cambios
            requireActivity().recreate()
        }
    }

    fun selectImage() {
        pickImageContract.launch("image/*")
    }


    private fun initViews() {


        // Read the state from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)

        // Set the switch and the app mode
        binding.switchMode.isChecked = isNightMode
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.switchMode.hint = "Day Mode"
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.switchMode.hint = "Night Mode"
        }


        binding.apply {
            lifecycleScope.launch {
                auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                Log.d("User", user?.uid ?: "No user")
                databaseReference =
                    FirebaseDatabase.getInstance().getReference("Shop").child("Users")
                        .child(user!!.uid)
                suspendCancellableCoroutine<Boolean> { cont ->
                    databaseReference.get().addOnSuccessListener {
                        if (it.exists()) {
                            this@ProfileFragmentUser.user = it.getValue(UserItem::class.java)
                            cont.resume(true)
                        }
                    }
                }
                textViewUsername.setText(this@ProfileFragmentUser.user?.name)
                textViewEmail.setText(this@ProfileFragmentUser.user?.email)
                if (this@ProfileFragmentUser.user?.url_firebase?.isNotBlank() == true) {
                    Glide.with(this@ProfileFragmentUser)
                        .load(this@ProfileFragmentUser.user?.url_firebase).into(profileImage)
                        .onLoadFailed(
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.portada
                            )
                        )
                }
            }
            binding.buttonSave.setOnClickListener {
                lifecycleScope.launch {
                    editUser()
                    findNavController().popBackStack()
                }
            }
            profileImage.setOnClickListener {
                selectImage()
            }
            binding.buttonLogout.setOnClickListener {
                // Update authentication state in SharedPreferences
                val sharedPreferences =
                    requireActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", false)
                editor.apply()

                // Sign out from FirebaseAuth
                auth.signOut()
                Log.d("ProfileFragmentUser", "Usuario cerró sesión")

                // Redirect to Login activity
                val intent = Intent(activity, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                activity?.finish()
            }
            binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Change to dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                } else {
                    // Change to light mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            if (requireContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    .getBoolean("isAdmin", false)
            ) {
                binding.buttonProductHistory.visibility = View.GONE
                binding.buttonAssistedEvents.setOnClickListener {
                    findNavController().navigate(R.id.action_profileFragment_to_assistEventsFragmentUser3)
                }
            } else {
                binding.buttonCharts.visibility = View.GONE
                binding.buttonAssistedEvents.setOnClickListener {
                    findNavController().navigate(R.id.action_profileFragmentUser_to_assistEventsFragmentUser2)
                }
                binding.buttonProductHistory.setOnClickListener {
                    findNavController().navigate(R.id.action_profileFragmentUser_to_backpackFragmentUser)
                }
            }
            binding.buttonCharts.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_chartsFragment) }
        }

        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            // Save the state in SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isNightMode", isChecked)
            editor.apply()

            if (isChecked) {
                // Change to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                // Change the hint of the switch
                binding.switchMode.hint = "Day Mode"
            } else {
                // Change to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                // Change the hint of the switch
                binding.switchMode.hint = "Night Mode"
            }
        }
    }

    private suspend fun editUser() {
        binding.apply {
            val name = textViewUsername.text.toString()
            val email = textViewEmail.text.toString()
            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(context, "Please fill all the fields correctly", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            if (imageUrl.isBlank()) {
                imageUrl = user?.url_firebase ?: ""
            }
            withContext(Dispatchers.IO) {
                updateUserInfo(name, email, imageUrl)
                updateUserInDb(user!!.copy(name = name, email = email, url_firebase = imageUrl))
            }
        }

    }

    private suspend fun updateUserInfo(name: String, email: String, imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(Uri.parse(imageUrl))
            .build()
        suspendCoroutine<Boolean> {
            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileFragmentUser", "User profile updated.")
                        // Update email
                        user.updateEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("ProfileFragmentUser", "User email address updated.")
                                }
                                it.resume(true)
                            }
                    }
                }
        }
    }

    private suspend fun updateUserInDb(updateUser: UserItem) {
        suspendCoroutine<Boolean> { continuation ->
            databaseReference.setValue(updateUser).addOnSuccessListener {
                Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
                continuation.resume(true)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to update User", Toast.LENGTH_SHORT).show()
                continuation.resume(false)
            }
        }

    }


    fun uploadImage(uri: Uri) {

        binding.buttonSave.isEnabled = false
        val storageReference =
            FirebaseStorage.getInstance().getReference("Shop").child("ProfilePic")
        val id = UUID.randomUUID().toString()
        val ref = storageReference.child(id)
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                imageUrl = it.toString()
                binding.buttonSave.isEnabled = true
            }.addOnFailureListener {
                binding.buttonSave.isEnabled = true
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun setLocale(localeName: String) {
        val locale = Locale(localeName)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}







