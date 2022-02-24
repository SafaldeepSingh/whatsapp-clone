package com.safaldeepsingh.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.safaldeepsingh.Userapp.db.UserTable
import com.safaldeepsingh.whatsappclone.entities.User

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val username: EditText = findViewById(R.id.register_username)
        val phoneNumber: EditText = findViewById(R.id.register_phoneNumber)
        val profilePhoto: EditText = findViewById(R.id.register_profilePhoto)
        val password: EditText = findViewById(R.id.register_password)
        val repeatPassword: EditText = findViewById(R.id.register_repeatPassword)
        val register: Button = findViewById(R.id.register_register)
        val login: TextView = findViewById(R.id.register_login)

        register.setOnClickListener {
            if(empty(username,phoneNumber,password,repeatPassword)){
                val builder: AlertDialog.Builder = AlertDialog.Builder(this).apply {
                    setTitle("Empty Fields")
                    setMessage("Please fill all necessary fields")
                }
                builder.setPositiveButton("OK"){dialog, which ->

                }
                builder.show()

            }else if(password.text.toString() != repeatPassword.text.toString()){
                val builder: AlertDialog.Builder = AlertDialog.Builder(this).apply {
                    setTitle("Mismatch")
                    setMessage("Passwords Should Match")
                }
                builder.setPositiveButton("OK"){dialog, which ->

                }
                builder.show()
            }
            else{
                var photo: String? = profilePhoto.text.toString()
                if(photo == "")
                    photo = null
                val user: HashMap<String, Any?> = hashMapOf(
                    "username" to username.text.toString(),
                    "phoneNumber" to phoneNumber.text.toString(),
                    "password" to password.text.toString(),
                    "profilePhoto" to photo
                )
                checkUserWithSamePhoneNumberQuery(phoneNumber.text.toString())
                    .addOnSuccessListener { documents->
                        if(documents.isEmpty){
                            //register
                            registerQuery(user)
                                .addOnSuccessListener {user->
                                    Log.d("register","User Registered Successfully")
                                    val userTable = UserTable(this)
                                    val userObject = User(
                                        user.id,
                                        username.text.toString(),
                                        phoneNumber.text.toString(),
                                        password.text.toString(),
                                        photo
                                    )
                                    userTable.insert(userObject)
                                    finish()
                                }
                                .addOnFailureListener {e->
                                    Log.d("register","User Cant be Registered $e")
                                }
                        }else{
                            //same phone number already exist
                            val builder: AlertDialog.Builder = AlertDialog.Builder(this).apply {
                                setTitle("Already Exist")
                                setMessage("User with same Phone Number Already Exist")
                            }
                            builder.setPositiveButton("OK"){dialog, which ->

                            }
                            builder.show()
                        }
                    }

            }
            }
        login.setOnClickListener {
            finish()
        }
    }
    private fun empty(vararg editTexts: EditText): Boolean{
        for(editText in editTexts){
            if(editText.text.toString()=="")
                return true
        }
        return false
    }
    private fun registerQuery(user: HashMap<String, Any?>): Task<DocumentReference> {
        val db = Firebase.firestore
        val userRef = db.collection("users")
        return userRef
            .add(user)
    }
    private fun checkUserWithSamePhoneNumberQuery(phoneNumber: String): Task<QuerySnapshot> {
        val db = Firebase.firestore
        val userRef = db.collection("users")
        return userRef
                .whereEqualTo("phoneNumber",phoneNumber)
            .get()
    }

}