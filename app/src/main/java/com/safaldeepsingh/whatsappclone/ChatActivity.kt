package com.safaldeepsingh.whatsappclone

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.safaldeepsingh.Userapp.db.UserTable
import com.safaldeepsingh.whatsappclone.adapter.ChatAdapter
import com.safaldeepsingh.whatsappclone.adapter.MessageAdapter
import com.safaldeepsingh.whatsappclone.entities.Chat
import com.safaldeepsingh.whatsappclone.entities.Message
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var userId: String
    private lateinit var otherUserId: String
    private var messageListner: ListenerRegistration? = null

    private lateinit var currentUserId: String
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesRecyclerView:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val backButton: ImageView = findViewById(R.id.chat_backButton)
        val newMessage: EditText = findViewById(R.id.chat_newMessage)
        val sendMessage: ImageView = findViewById(R.id.chat_sendMessage)
        messagesRecyclerView = findViewById(R.id.chat_messages)

        //get UserId from DB
        val userTable = UserTable(this)
        currentUserId = userTable.getUser()?.id as String
        messageAdapter = MessageAdapter(currentUserId)

        messagesRecyclerView.adapter = messageAdapter
        val myIntent = intent
        if(intent!= null && intent.hasExtra(ChatAdapter.EXTRA_CHAT)){
            val chat = intent.getSerializableExtra(ChatAdapter.EXTRA_CHAT) as? Chat
            if(chat != null){
                val profileImage: ImageView = findViewById(R.id.chat_profileImage)
                val name: TextView = findViewById(R.id.chat_name)
                if(chat.profileImage != null)
                    Picasso.get().load(chat.profileImage).into(profileImage)
                else
                    profileImage.setImageResource(R.drawable.profile)
                name.text = chat.userName
                userId = currentUserId
                otherUserId = chat.userId
                getMessages()

                sendMessage.setOnClickListener {
                    if(newMessage.text.toString() != "")
                    {
                        //send Message
                        val message = Message(
                            content = newMessage.text.toString(),
                            senderId = currentUserId,
                            receiverId = chat.userId
                        )
                        addMessage(message)
                        newMessage.setText("")
                    }
                }
            }
        }

        backButton.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        val members = arrayListOf(userId, otherUserId)
        members.sort()
        messageListner =
            db.collection("chatRooms")
                .whereEqualTo("members",members)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
//                    Log.d("TAG", "Current data: ${snapshot.documents}")
                        manageMessageData(snapshot)
                    } else {
                        Log.d("TAG", "Current data: null")
                    }

                }
    }

    override fun onPause() {
        super.onPause()
        messageListner?.remove()
    }

    private fun addMessage(message: Message){
        val chatRoomsRef = db.collection("chatRooms")
        val members: ArrayList<String> = arrayListOf(message.senderId,message.receiverId)
        members.sort()
        //first check if chat room exist
        val query = chatRoomsRef.whereEqualTo("members", members)
        query.get()
            .addOnSuccessListener { documents ->
                //if doesnt exist
                if(documents.isEmpty)
                    createNewChatRoom(chatRoomsRef, message, members)
                //if exist
                else{
                    for(document in documents){
                        val chatRoomMessages: ArrayList<Any> = document.data.get("messages") as ArrayList<Any>
                        chatRoomMessages.add(message)
                        val data = hashMapOf("messages" to chatRoomMessages)
                        chatRoomsRef.document(document.id).set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d("Add Message","Message added to existing chatRoom")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Add Message","Cant add message to existing chatroom - $e")
                            }
                        break
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.d("add Message-> check chatRoom Error","$exception")
            }


    }
    private fun createNewChatRoom(chatRoomsRef: CollectionReference, message: Message, members: ArrayList<String>) {
        val chatRoom = hashMapOf(
            "members" to members,
            "messages" to arrayListOf(message)
        )
        chatRoomsRef
            .add(chatRoom)
            .addOnSuccessListener { documentReference ->
                Log.d("Add ChatRoom", "ChatRoom added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Add ChatRoom Error", "Error adding chatRoom", e)
            }

    }

    private fun getMessages() {
        val chatRoomsRef = db.collection("chatRooms")
        val members = arrayListOf(userId, otherUserId)
        members.sort()
        chatRoomsRef.whereEqualTo("members",members).get()
            .addOnSuccessListener { documents ->
                if(documents != null)
                manageMessageData(documents)
            }
            .addOnFailureListener { e ->
                Log.e("getChats Error","$e")
            }
    }

    private fun manageMessageData(documents: QuerySnapshot) {
        for(document in documents){
            val messageData = document["messages"] as ArrayList<Any>
            val (messages,updatedMessages,updateMessages) = dataToMessages(messageData, document.id)
            messageAdapter.setData(messages as List<Message>)
            messagesRecyclerView.scrollToPosition(messageAdapter.itemCount-1)
            //set SeenAt for Messages
            if(updateMessages as Boolean){
                val data = hashMapOf("messages" to updatedMessages)
                db.collection("chatRooms").document(document.id).set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Chat -> update seenAt","Success")
                    }
                    .addOnFailureListener {
                        Log.d("Chat -> update seenAt","Failure")
                    }

            }
            break
        }

    }

    private fun dataToMessages(messagesData: ArrayList<Any>, chatRoomId: String): Array<Any> {
        val messages = mutableListOf<Message>()
        var updateMessage = false
        val updatedMessages = mutableListOf<Message>()
        for(data in messagesData){
            val messageData = data as HashMap<String, Any>
            val message = Message(
                id = messageData["id"] as String,
                content = messageData["content"] as String,
                senderId = messageData["senderId"] as String,
                receiverId = messageData["receiverId"] as String,
                sentAt = (messageData["sentAt"] as Timestamp).toDate(),
                seenAt = (messageData["seenAt"] as Timestamp?)?.toDate(),
            )
            val updatedMessage = message
            if(message.seenAt == null){
                if(message.receiverId == currentUserId){
                    updateMessage = true
                    updatedMessage.seenAt = Date()
                }
            }
            messages.add(message)
            updatedMessages.add(updatedMessage)
        }
        return  arrayOf(messages,updatedMessages, updateMessage)
    }

}