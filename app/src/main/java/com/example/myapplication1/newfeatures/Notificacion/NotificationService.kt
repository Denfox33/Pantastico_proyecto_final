package com.example.myapplication1.newfeatures.Notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication1.Admin.AssistItem
import com.example.myapplication1.User.Product_Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationService(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var lastOrdersAmount = 0
    private var lastEventsAmount = 0

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun checkIfOrderIsNew(before: Int,after:Int): Boolean{
        return before < after
    }
    fun checkForNotifications() {
        val db_ref = FirebaseDatabase.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        GlobalScope.launch {
            db_ref.getReference("Shop/Pending_Orders").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders =
                        snapshot.children.mapNotNull { it.getValue(Product_Reservation::class.java) }
                    val currentUserPendingOrders =
                        orders.filter { it.status == "Pending" && it.user_notification == currentUserId }
                    if (currentUserPendingOrders.isNotEmpty() && checkIfOrderIsNew(lastOrdersAmount,currentUserPendingOrders.size)){
                    lastOrdersAmount = currentUserPendingOrders.size
                    showNotification(
                        "New order",
                        "You have a new order",
                        Intent(
                            context,
                            Class.forName("com.example.myapplication1.Admin.ordersflow.OrderFragmentCheck")
                        )
                            )
                    }
                     //Check for navigation
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.d("Notification Service", "Error getting data")
                }
            })
            db_ref.getReference("Shop/Pending_Events").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val events =
                        snapshot.children.mapNotNull { it.getValue(AssistItem::class.java) }

                    val currentUserPendingEvents =
                        events.filter {  it.user_notification == currentUserId }
                    if (currentUserPendingEvents.isNotEmpty() && checkIfOrderIsNew(lastEventsAmount,currentUserPendingEvents.size)){
                        lastEventsAmount = currentUserPendingEvents.size
                        showNotification(
                            "New event",
                            "You have a new person in your event",
                            Intent(
                                context,
                                Class.forName("com.example.myapplication1.Admin.eventsflow.EventFragmentCheckDeleteEdit")
                            )
                        )
                    }
                    //Check for navigation
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.d("Notification Service", "Error getting data")
                }
            })
        }

    }

    companion object {
        private const val CHANNEL_ID = "order_channel"
        private const val CHANNEL_NAME = "order_channel"
        private const val CHANNEL_DESCRIPTION = "Order notifications"
        private const val NOTIFICATION_ID = 100
    }
}
