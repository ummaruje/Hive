package com.gpetuhov.android.hive.ui.epoxy.profile.models

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.ui.epoxy.holder.KotlinHolder

@EpoxyModelClass(layout = R.layout.profile_details_view)
abstract class DetailsModel : EpoxyModelWithHolder<DetailsHolder>() {

    @EpoxyAttribute lateinit var username: String
    @EpoxyAttribute lateinit var userPicUrl: String
    @EpoxyAttribute lateinit var name: String
    @EpoxyAttribute lateinit var email: String

    override fun bind(holder: DetailsHolder) {
        holder.username.text = username

        // TODO: load user pic here

        holder.name.text = name
        holder.email.text = email
    }
}

class DetailsHolder : KotlinHolder() {
    val username by bind<TextView>(R.id.username_textview)
    val userPic by bind<ImageView>(R.id.user_pic)
    val name by bind<TextView>(R.id.user_name_textview)
    val email by bind<TextView>(R.id.user_email_textview)
}