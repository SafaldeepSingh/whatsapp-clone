package com.safaldeepsingh.whatsappclone

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.safaldeepsingh.Userapp.db.UserTable
import com.safaldeepsingh.whatsappclone.adapter.ContactAdapter
import com.safaldeepsingh.whatsappclone.entities.User


class ContactsActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_NEW_CHAT = "newChat"
    }
    private val contactAdapter = ContactAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val contacts: RecyclerView = findViewById(R.id.contacts_recyclerView)
        contacts.adapter = contactAdapter
        getContactsQuery()
            .addOnSuccessListener { documents->
                val users = mutableListOf<User>()
                val userTable = UserTable(this)
                val userId = userTable.getUser()?.id
                for(document in documents){
                    val data =  document.data
                    val user = User(
                        document.id,
                        data.get("username") as String,
                        data.get("phoneNumber") as String,
                        data.get("password") as String,
                        data.get("profilePhoto") as String?,
                    )
                    if(user.id != userId)
                        users.add(user)
                }
                contactAdapter.setData(users)
            }
            .addOnFailureListener { e->
                Log.e("Contacts","Error fetching Users $e")
            }
    }


    private fun getContactsQuery(): Task<QuerySnapshot> {
        val db = Firebase.firestore
        val userRef = db.collection("users")
        return userRef.get()
    }
}