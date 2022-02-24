package com.safaldeepsingh.whatsappclone.entities

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.*

data class Chat(
    val profileImage: String? = null,
    val userId: String,
    val userName: String,
    val messages: List<Message>,
    val lastMessage: String,
    val lastMessageSentAt: Date,
    val noOfUnreadMessages: Int,
    val wasLastMessageSentByUser: Boolean = false,
    val wasLastMessageSeen: Boolean = false
): Serializable {}