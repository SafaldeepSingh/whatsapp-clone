package com.safaldeepsingh.whatsappclone.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.safaldeepsingh.whatsappclone.ChatActivity
import com.safaldeepsingh.whatsappclone.R
import com.safaldeepsingh.whatsappclone.entities.Chat
import com.squareup.picasso.Picasso
import java.time.YearMonth
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatItemViewHolder>() {
    private val dataSet = mutableListOf<Chat>()
    companion object{
        const val EXTRA_CHAT = "chatObject"
    }
    class ChatItemViewHolder(private val parentAdapter: ChatAdapter, private val containerView: View) : RecyclerView.ViewHolder(containerView) {
        var chat: Chat? = null
        val profileImage: ImageView = containerView.findViewById(R.id.listChat_profileImage)
        val userName: TextView = containerView.findViewById(R.id.listChat_name)
        val lastMessage: TextView = containerView.findViewById(R.id.listChat_message)
        val lastMessageDateTime: TextView = containerView.findViewById(R.id.listChat_lastMessageTime)
        val unreadMessagesCount: TextView = containerView.findViewById(R.id.listChat_unreadMessagesCount)

        init {
            //Click Listeners and whatever else to manage View
            containerView.setOnClickListener {
                val intent = Intent(containerView.context, ChatActivity::class.java)
                intent.putExtra(EXTRA_CHAT,chat)
                containerView.context.startActivity(intent)
            }
        }
    }
    public fun setData(dataSet: List<Chat>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    public fun addData(Chat: Chat){
        dataSet.add(Chat)
        dataSet.sortByDescending { it.lastMessageSentAt }
        notifyDataSetChanged()
    }

    public fun removeData(Chat: Chat){
        dataSet.remove(Chat)
        notifyDataSetChanged()
    }
    fun clearData(){
        dataSet.clear()
    }

    //inflate the customView
    override fun onCreateViewHolder(parent: ViewGroup, customViewType: Int): ChatItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_chat,parent, false) //false because the recycler will add to the view hierarchy when it is time
        return ChatItemViewHolder(this, view)
    }

    //Called by the layoutManager to replace the content(data) of the CustomView
    override fun onBindViewHolder(holder: ChatItemViewHolder, positionInDataSet: Int) {
        val currentData = dataSet[positionInDataSet]
        holder.chat = currentData
        if(currentData==null)
            return
        if(currentData.profileImage == null)
            holder.profileImage.setImageResource(R.drawable.profile)
        else
            Picasso.get().load(currentData.profileImage).into(holder.profileImage)
        holder.userName.text = currentData.userName
        holder.lastMessage.text = currentData.lastMessage
        holder.lastMessageDateTime.text = formatMessageTime(currentData.lastMessageSentAt)
        if(currentData.noOfUnreadMessages > 0){
            holder.unreadMessagesCount.visibility = View.VISIBLE
            holder.unreadMessagesCount.text = currentData.noOfUnreadMessages.toString()
        }
        else
            holder.unreadMessagesCount.visibility = View.GONE
    }

    override fun getItemCount() = dataSet.size

    private fun formatMessageTime(date: Date): String{
        val days = listOf("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
        val now = Date()
        if(date.equals(now)) return "now"
        else if (date.month == now.month && date.date == now.date)
            return String.format("%02d:%02d", date.hours, date.minutes)
        else if (
            (date.month == now.month && date.date == now.date - 1)
            || (date.month == now.month-1 && now.date == 1 && date.month == YearMonth.of(date.year,date.month).lengthOfMonth()-1)
        )
            return "Yesterday"
        else
            return days[date.day]
    }
}











