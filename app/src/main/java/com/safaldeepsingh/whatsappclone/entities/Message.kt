package com.safaldeepsingh.whatsappclone.entities

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.*

data class Message(
    val content: String,
    val sentAt: Date = Date(),
    var seenAt: Date? = null,
    val senderId: String,
    val receiverId: String,
    val id: String? = Date().toString()+(1..100).random(),
):Serializable {}