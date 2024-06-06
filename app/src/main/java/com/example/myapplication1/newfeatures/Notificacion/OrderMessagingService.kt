package com.example.myapplication1.newfeatures.Notificacion

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.security.AccessControlContext

class OrderMessagingService(private val context: Context) : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Procesar la notificación recibida
    }
}