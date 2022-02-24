package com.safaldeepsingh.musicapp.db

import android.provider.BaseColumns

object UserDbContract {
    object UserTable: BaseColumns{
        const val TABLE_NAME = "user"
        const val ID = "id"
        const val NAME = "username"
        const val PHONE_NUMBER = "phone_number"
        const val PROFILE_PHOTO = "profile_photo"
        const val PASSWORD = "password"
    }
}