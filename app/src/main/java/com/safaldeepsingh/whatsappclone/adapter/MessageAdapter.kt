package com.safaldeepsingh.whatsappclone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.safaldeepsingh.whatsappclone.R
import com.safaldeepsingh.whatsappclone.entities.Message
import java.util.*

class MessageAdapter(val userId: String) : RecyclerView.Adapter<MessageAdapter.MessageItemViewHolder>() {
    private val dataSet = mutableListOf<Message>()

    class MessageItemViewHolder(private val parentAdapter: MessageAdapter, private val containerView: View) : RecyclerView.ViewHolder(containerView) {
        var message: Message? = null
        val content: TextView = containerView.findViewById(R.id.listMessage_text)
        val sentAt: TextView = containerView.findViewById(R.id.listMessage_sentAt)
        init {
            //Click Listeners and whatever else to manage View
        }
    }

    public fun setData(dataSet: List<Message>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    public fun addData(Message: Message){
        dataSet.add(Message)
        notifyDataSetChanged()
    }

    public fun removeData(Message: Message){
        dataSet.remove(Message)
        notifyDataSetChanged()
    }

    //inflate the customView
    override fun onCreateViewHolder(parent: ViewGroup, customViewType: Int): MessageItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(customViewType,parent, false) //false because the recycler will add to the view hierarchy when it is time
        return MessageItemViewHolder(this, view)
    }

    //Called by the layoutManager to replace the content(data) of the CustomView
    override fun onBindViewHolder(holder: MessageItemViewHolder, positionInDataSet: Int) {
        val currentData = dataSet[positionInDataSet]
        holder.message = currentData

        holder.content.text = currentData.content
        holder.sentAt.text = formatTime(currentData.sentAt)

    }


    override fun getItemCount() = dataSet.size
    override fun getItemViewType(position: Int): Int {
        if(dataSet[position].senderId == userId)
            return R.layout.list_message_sent
        else
            return R.layout.list_message_received
    }

    private fun formatTime(sentAt: Date): String {
        val time = String.format("%d/%d/%d %02d:%02d", sentAt.date,sentAt.month+1,(sentAt.year+1900)%2000, sentAt.hours, sentAt.minutes)
//        Log.d("format Mesage time in Chat activity",time)
        return time
    }
}











