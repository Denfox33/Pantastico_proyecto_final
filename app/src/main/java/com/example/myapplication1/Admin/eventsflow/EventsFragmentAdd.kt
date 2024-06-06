package com.example.myapplication1.Admin.eventsflow

import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication1.Admin.EventsItem
import com.example.myapplication1.Main.Utilities
import com.example.myapplication1.User.StatusSignOnUser
import com.example.myapplication1.databinding.FragmentEventsAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Calendar
import kotlin.coroutines.CoroutineContext


class EventsFragmentAdd : Fragment(), CoroutineScope {

    private var _binding: FragmentEventsAddBinding? = null
    private val binding get() = _binding!!


    private lateinit var name: EditText
    private lateinit var logo: ImageView
    private lateinit var price: EditText
    private lateinit var maxCapacity: EditText
    private lateinit var addEvent: AppCompatButton
    private lateinit var backEvent: AppCompatButton


    private var url_logo: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var event_list: MutableList<EventsItem>

    private lateinit var job: Job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEventsAddBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val this_fragment = this
        val calendar = Calendar.getInstance().time
        val dateFormat = DateFormat.getDateInstance().format(calendar)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()
        event_list = Utilities.getEventsList(db_ref)

        //binding events attributes
        name = binding.eventAddName
        logo = binding.eventAddLogo
        price = binding.eventAddPrice
        maxCapacity = binding.eventAddMaxCapacity

        //buttons for the activity
        addEvent = binding.eventAddButton
        backEvent = binding.eventCancelButton


        addEvent.setOnClickListener {

            if (name.text.toString().trim().isEmpty() ||
                price.text.toString().trim().isEmpty() ||
                maxCapacity.text.toString().trim().isEmpty()
            ) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Missing data in the form",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (url_logo == null) {
                Toast.makeText(
                    requireActivity().applicationContext, "Missing logo", Toast.LENGTH_SHORT
                ).show()
            } else if (Utilities.events_Exist(event_list, name.text.toString().trim())) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Event already exists",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                var id_gen: String? = db_ref.child("Shop").child("Events").push().key

                launch {
                    var url_logo_firebase = Utilities.save_logo(st_ref, id_gen!!, url_logo!!)

                    val androidID = Settings.Secure.getString(
                        requireActivity().contentResolver,
                        Settings.Secure.ANDROID_ID
                    )

                    Utilities.event_add(
                        db_ref = db_ref,
                        id = id_gen!!,
                        name = name.text.toString().trim(),
                        date = dateFormat.toString().trim(),
                        price = price.text.toString().trim(),
                        maxCapacity = maxCapacity.text.toString().trim().toInt(),
                        Capacity = 0,
                        url_firebase_logo = url_logo_firebase,
                        noti_status = StatusSignOnUser.CREATED,
                        user_notification = FirebaseAuth.getInstance().currentUser!!.uid
                    )

                    Utilities.courrutine_thing_fragments(
                        this_fragment,
                        requireActivity().applicationContext,
                        "Event added Successfully"
                    )


                    withContext(Dispatchers.Main) {
                        findNavController().popBackStack()
                    }
                }
            }


        }

        backEvent.setOnClickListener {
            findNavController().popBackStack()
        }

        logo.setOnClickListener {
            accesoGaleria.launch("image/*")

        }


//


    }

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            url_logo = uri
            logo.setImageURI(uri)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

}