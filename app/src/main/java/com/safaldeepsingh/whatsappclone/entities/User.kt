package com.safaldeepsingh.whatsappclone.entities


data class User(
    val id: String? = null,
    val username: String,
    val phoneNumber: String,
    val password: String,
    val profilePhoto: String? = null
)
{}