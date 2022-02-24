package com.safaldeepsingh.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.safaldeepsingh.Userapp.db.UserTable
import com.safaldeepsingh.whatsappclone.entities.User

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val phoneNumber: EditText = findViewById(R.id.login_phoneNumber)
        val password: EditText = findViewById(R.id.login_password)
        val login: Button = findViewById(R.id.login_login)
        val register: TextView = findViewById(R.id.login_register)
        val userTable = UserTable(this)

        login.setOnClickListener {
            val query = checkCredentialsQuery(phoneNumber.text.toString(), password.text.toString())
            query
                .addOnSuccessListener { documents->
                    if(documents.isEmpty){
                        //Invalid Credentials
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this).apply {
                            setTitle("Invalid Credentials")
                            setMessage("Please check your Phone Number & Password")
                        }
                        builder.setPositiveButton("OK"){dialog, which ->

                        }
                        builder.show()

                    }else{
                        for(document in documents){
                            val data = document.data
                            val user = User(
                                document.id,
                                data.get("username") as String,
                                data.get("phoneNumber") as String,
                                data.get("password") as String,
                                data.get("profilePhoto") as String,
                            )
                            //saveUser in Sqlite
                            userTable.insert(user)

                            //reset form fields
                            phoneNumber.setText("")
                            password.setText("")
                            //start main activity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)

                            break
                        }
                    }
                }
        }
        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val userTable = UserTable(this)
        val currentUser = userTable.getUser()
        if(currentUser != null && currentUser.id != null){
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
    private fun checkCredentialsQuery(phoneNumber: String, password: String): Task<QuerySnapshot> {
        val db = Firebase.firestore
        val userRef = db.collection("users")
        Log.d("login","$phoneNumber $password")
        return userRef
            .whereEqualTo("phoneNumber", phoneNumber)
            .whereEqualTo("password", password)
            .get()
    }
}