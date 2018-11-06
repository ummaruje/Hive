package com.gpetuhov.android.hive.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.model.Message
import com.gpetuhov.android.hive.domain.repository.Repo
import javax.inject.Inject
import androidx.constraintlayout.widget.ConstraintLayout

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    @Inject lateinit var context: Context

    private var messageList = mutableListOf<Message>()

    init {
        HiveApp.appComponent.inject(this)

        for (i in 0..100) {
            messageList.add(Message(i.toString(), System.currentTimeMillis(), "Message text $i"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount() = messageList.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) = holder.bindMessage(messageList[position])

    // === Inner classes ===

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @Inject lateinit var context: Context
        @Inject lateinit var repo: Repo

        private lateinit var message: Message
        private var messageTextView: TextView = itemView.findViewById(R.id.item_message_text)

        init {
            HiveApp.appComponent.inject(this)
        }

        fun bindMessage(message: Message) {
            this.message = message
            messageTextView.text = message.text

            val startEndMargin = context.resources.getDimensionPixelOffset(R.dimen.message_margin_start_end)
            val topBottomMargin = context.resources.getDimensionPixelOffset(R.dimen.message_margin_top_bottom)
            val bigMargin = context.resources.getDimensionPixelOffset(R.dimen.message_margin_big)

            val params = ConstraintLayout.LayoutParams(messageTextView.layoutParams)

            // TODO: restore this line
//            if (message.isFromUser(repo.currentUserUid())) {
            if (message.senderUid.toInt() % 2 == 0) {
                messageTextView.setBackgroundResource(R.drawable.message_background_current_user)
                params.setMargins(bigMargin, topBottomMargin, startEndMargin, topBottomMargin)

            } else {
                messageTextView.setBackgroundResource(R.drawable.message_background)
                params.setMargins(startEndMargin, topBottomMargin, bigMargin, topBottomMargin)
            }

            messageTextView.layoutParams = params
        }
    }
}