package com.safaldeepsingh.Userapp.db

import android.content.ContentValues
import android.content.Context
import com.safaldeepsingh.musicapp.db.UserDbContract
import com.safaldeepsingh.whatsappclone.entities.User

class UserTable(context: Context) {
    private var dbHelper= UserDbHelper(context)
    fun insert(User: User): Long{
        val values = ContentValues().apply {
            put(UserDbContract.UserTable.ID, User.id)
            put(UserDbContract.UserTable.NAME, User.username)
            put(UserDbContract.UserTable.PASSWORD, User.password)
            put(UserDbContract.UserTable.PROFILE_PHOTO, User.profilePhoto)
            put(UserDbContract.UserTable.PHONE_NUMBER, User.phoneNumber)
        }
        val writeToDb = dbHelper.writableDatabase
        val newRowId = writeToDb.insert(UserDbContract.UserTable.TABLE_NAME, null, values)
        return newRowId
    }
    fun getUser(): User?{
        val readFromDb = dbHelper.readableDatabase
        val projection = arrayOf(
            UserDbContract.UserTable.ID,
            UserDbContract.UserTable.NAME,
            UserDbContract.UserTable.PASSWORD,
            UserDbContract.UserTable.PROFILE_PHOTO,
            UserDbContract.UserTable.PHONE_NUMBER
        )


        val cursor = readFromDb.query(
            UserDbContract.UserTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        var user:User? =null        //cursor starts at -1
        with(cursor){

        if(cursor.moveToNext()){
            user = User(
                getString(getColumnIndexOrThrow(UserDbContract.UserTable.ID)),
                getString(getColumnIndexOrThrow(UserDbContract.UserTable.NAME)),
                getString(getColumnIndexOrThrow(UserDbContract.UserTable.PHONE_NUMBER)),
                getString(getColumnIndexOrThrow(UserDbContract.UserTable.PASSWORD)),
                getString(getColumnIndexOrThrow(UserDbContract.UserTable.PROFILE_PHOTO)),
            )
        }
        close()
        }
        return user
    }
    fun delete(): Boolean{
        val dbWrite = dbHelper.writableDatabase
        val deletedRows = dbWrite.delete(
            UserDbContract.UserTable.TABLE_NAME,
            null,
            null
        )
        return deletedRows >= 1
    }
}