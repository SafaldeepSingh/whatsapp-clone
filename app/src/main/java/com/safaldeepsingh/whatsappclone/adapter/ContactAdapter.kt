package com.safaldeepsingh.whatsappclone.adapter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.safaldeepsingh.whatsappclone.ContactsActivity
import com.safaldeepsingh.whatsappclone.R
import com.safaldeepsingh.whatsappclone.entities.Chat
import com.safaldeepsingh.whatsappclone.entities.Message
import com.safaldeepsingh.whatsappclone.entities.User
import com.squareup.picasso.Picasso
import java.time.YearMonth
import java.util.*

class ContactAdapter(val context: Context) : RecyclerView.Adapter<ContactAdapter.ContactItemViewHolder>() {
    private val dataSet = mutableListOf<User>()
    class ContactItemViewHolder(private val parentAdapter: ContactAdapter, private val containerView: View) : RecyclerView.ViewHolder(containerView) {
        var Contact: User? = null
        val profileImage: ImageView = containerView.findViewById(R.id.listContact_profileImage)
        val userName: TextView = containerView.findViewById(R.id.listContact_name)

        init {
            //Click Listeners and whatever else to manage View
            containerView.setOnClickListener {
                val intentToSendBackwards = Intent()
                val chat = Chat(
                    Contact?.profilePhoto,
                    Contact?.id as String,
                    Contact?.username as String,
                    listOf<Message>(),
                    "",
                    Date(),
                    0
                )
                val activity  = (parentAdapter.context as Activity)
                intentToSendBackwards.putExtra(ContactsActivity.EXTRA_NEW_CHAT, chat)
                activity.setResult(RESULT_OK, intentToSendBackwards)
                activity.finish()
            }
        }
    }

    public fun setData(dataSet: List<User>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    public fun addData(Contact: User){
        dataSet.add(Contact)
        notifyDataSetChanged()
    }

    public fun removeData(Contact: User){
        dataSet.remove(Contact)
        notifyDataSetChanged()
    }

    //inflate the customView
    override fun onCreateViewHolder(parent: ViewGroup, customViewType: Int): ContactItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_contact,parent, false) //false because the recycler will add to the view hierarchy when it is time
        return ContactItemViewHolder(this, view)
    }

    //Called by the layoutManager to replace the content(data) of the CustomView
    override fun onBindViewHolder(holder: ContactItemViewHolder, positionInDataSet: Int) {
        val currentData = dataSet[positionInDataSet]
        holder.Contact = currentData
        if(currentData.profilePhoto == null)
            holder.profileImage.setImageResource(R.drawable.profile)
        else
            Picasso.get().load(currentData.profilePhoto).into(holder.profileImage)
        holder.userName.text = currentData.username
    }

    override fun getItemCount() = dataSet.size

}











