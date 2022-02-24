package com.safaldeepsingh.whatsappclone

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.safaldeepsingh.Userapp.db.UserTable
import com.safaldeepsingh.whatsappclone.adapter.ChatAdapter
import com.safaldeepsingh.whatsappclone.entities.Chat
import com.safaldeepsingh.whatsappclone.entities.Message
import com.safaldeepsingh.whatsappclone.entities.User
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    companion object{
//        const val userId = "TpM6mFoLecJPbr9wvDhm"
    }

    val db = Firebase.firestore
    private var chatListner: ListenerRegistration? = null
    private val chatAdapter = ChatAdapter()
    private var currentUser: User? = null
    private lateinit var currentUserId: String

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ,this::onSendDataBackward)
    private fun onSendDataBackward(result: ActivityResult){
        if(result.resultCode == RESULT_OK){
            val intent = result.data
            if(intent != null && intent.hasExtra(ContactsActivity.EXTRA_NEW_CHAT)){
                val newChat = intent.getSerializableExtra(ContactsActivity.EXTRA_NEW_CHAT) as? Chat
                if(newChat != null){
                    Log.d("new CHat","$newChat")
                    val newIntent = Intent(this, ChatActivity::class.java)
                    newIntent.putExtra(ChatAdapter.EXTRA_CHAT, newChat)
                    startActivity(newIntent)
                }
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newChat: FloatingActionButton = findViewById(R.id.main_newChat)

        //get user from sqlite
        val userTable = UserTable(this)
        currentUser = userTable.getUser()
        currentUserId = currentUser?.id as String
        val chatsRecyclerView: RecyclerView = findViewById(R.id.main_chats)
        chatsRecyclerView.adapter = chatAdapter

//        getChats(currentUserId)

        newChat.setOnClickListener {
            resultLauncher.launch(Intent(this, ContactsActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        chatListner =  db.collection("chatRooms")
            .whereArrayContains("members",currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }
                val noChats: TextView = findViewById(R.id.main_noChats)

                if (snapshot != null ) {
                    if(!snapshot.isEmpty){
                    noChats.visibility = View.GONE
                    Log.d("Chat Listener", "Chat updated")
                    manageChatData(snapshot)
                    }
                    else
                        noChats.visibility = View.VISIBLE
                } else {
                    Log.d("TAG", "Current data: null")
                }
            }
    }
    override fun onPause() {
        super.onPause()
        chatListner?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true //True shows Menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_logout->{
                val userTable = UserTable(this)
                if(userTable.delete())
                    finish()
                else{
                    Toast.makeText(this,"Something Went Wrong", Toast.LENGTH_LONG).show()
                }


            }
        }
        return true
    }

    private fun getChats(userId: String) {
        val chatRoomsRef = db.collection("chatRooms")
        chatRoomsRef.whereArrayContains("members",userId).get()
            .addOnSuccessListener { documents ->
                if(documents != null)
                    manageChatData(documents)
            }
            .addOnFailureListener { e ->
                Log.e("getChats Error","$e")
            }
    }

    private fun manageChatData(documents: QuerySnapshot) {
        chatAdapter.clearData()
        val chats = mutableListOf<Chat?>()
        val chatsData = mutableListOf<Map<String, Any>>()

        for(document in documents){
            chats.add(null)
            chatsData.add(document.data)
        }
        for((index,chatRoom) in chatsData.withIndex()){
            val members = chatRoom["members"] as ArrayList<String>
            val otherUserId = if (members[0]==currentUserId) members[1] else members[0]
            //get other user details
            db.collection("users").document(otherUserId).get()
                .addOnSuccessListener { document ->
                    val userData = document.data
                    val messagesData = chatRoom["messages"] as ArrayList<Any>
                    var (messages, noOfUnreadMessages) = dataToMessages(messagesData)
                    messages = messages as List<Message>
                    if(userData != null)
                    {
                        val chat = Chat(
                            userData["profilePhoto"] as String?,
                            userId = otherUserId,
                            userData["username"] as String,
                            messages = messages,
                            noOfUnreadMessages = noOfUnreadMessages as Int,
                            lastMessage = messages.last().content,
                            lastMessageSentAt = messages.last().sentAt
                        )
                        chats.set(index,chat)
                        Log.d("add chat","")
                        chatAdapter.addData(chat)
                    }
                }
                .addOnCompleteListener {
                    val nullChat = chats.find { it == null }
                    if(nullChat == null){
//                    Log.d("getchats on complete","$chats")
//                    Log.d("getchats on complete","${chats.size}")
//                    chats.sortByDescending { it?.lastMessageSentAt }
//                    chatAdapter.setData(chats as List<Chat>)
                    }
                }

        }

    }

    private fun dataToMessages(messagesData: ArrayList<Any>): Array<Any> {
        val messages = mutableListOf<Message>()
        var noOfUnreadMessagesCount = 0
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
            if(message.seenAt == null && message.receiverId == currentUserId)
                noOfUnreadMessagesCount++
            messages.add(message)
        }
        return  arrayOf(messages, noOfUnreadMessagesCount)
    }

    private fun addUser(user: User){
// Create a new user with a first and last name
        val user = hashMapOf(
            "username" to user.username,
            "password" to user.password,
            "phoneNumber" to user.phoneNumber,
            "profilePhoto" to user.profilePhoto
        )

// Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("Add User", "User added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Add User Error", "Error adding user", e)
            }
    }
    private fun getUser(id: String){
        db.collection("users").document(id).get()
            .addOnSuccessListener { document ->
                val user = document.data as User
                Log.d("getUser","$user")
            }
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
    private fun createNewChatRoom(chatRoomsRef: CollectionReference,message: Message, members: ArrayList<String>) {
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

    private fun sampleAddUser(){
        var user = User(
            username = "Safal",
            password = "test",
            phoneNumber = "+1 4389225402",
            profilePhoto = "https://media-exp1.licdn.com/dms/image/C4E03AQFyutUDbG2JVg/profile-displayphoto-shrink_200_200/0/1599491524172?e=1650499200&v=beta&t=Sj9i8cWudcwyl51sLGamQaGlnzGZzp71xLyPcCKL8ZY"
        )
        addUser(user)
        user = User(
            username = "Anuj",
            password = "test",
            phoneNumber = "+1 4389225403",
            profilePhoto = "https://media-exp1.licdn.com/dms/image/C4D03AQGgrzu8OMqIjw/profile-displayphoto-shrink_200_200/0/1617496325061?e=1650499200&v=beta&t=MQNc-4OtTVSNtDvbhgCdmDAJQ0sGDz_-SouDI4B5Dt8"
        )
        addUser(user)
        user = User(
            username = "Rohith",
            password = "test",
            phoneNumber = "+1 4389225401",
            profilePhoto = "https://media-exp1.licdn.com/dms/image/C5103AQGIOY2W3FGDnw/profile-displayphoto-shrink_200_200/0/1517340105113?e=1648684800&v=beta&t=uK_hAFBx9fYIqkL2ta3duWhIj3LbsUvmurT1FOP2RRI"
        )
        addUser(user)

    }
    private fun sampleMessage(){
        val message = Message(
            content = "Hi",
            senderId = "TpM6mFoLecJPbr9wvDhm",
            receiverId = "YztjrG5JQXjvbJgzR4uQ",
        )
        addMessage(message)
//        val message = Message(
//            content = "Hello",
//            receiverId = "TpM6mFoLecJPbr9wvDhm",
//            senderId = "YztjrG5JQXjvbJgzR4uQ",
//        )
//        addMessage(message)

    }
}