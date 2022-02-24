package com.safaldeepsingh.Userapp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.safaldeepsingh.musicapp.db.UserDbContract

/*
*
*
*
* */

private const val SQL_CREATE_TABLE =
    "CREATE TABLE ${UserDbContract.UserTable.TABLE_NAME} ("+
            "${UserDbContract.UserTable.ID} TEXT, "+
            "${UserDbContract.UserTable.NAME} TEXT, "+
            "${UserDbContract.UserTable.PASSWORD} TEXT, "+
            "${UserDbContract.UserTable.PROFILE_PHOTO} TEXT, "+
            "${UserDbContract.UserTable.PHONE_NUMBER} INTEGER  "+
            ")"
private const val DROP_TABLE = "DROP TABLE IF EXISTS ${UserDbContract.UserTable.TABLE_NAME}"
class UserDbHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_NAME = "User.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_TABLE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        super.onDowngrade(db, oldVersion, newVersion)
    }
}