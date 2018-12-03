package com.gpetuhov.android.hive.ui.epoxy.offer.update.models

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.ui.epoxy.holder.KotlinHolder

@EpoxyModelClass(layout = R.layout.update_offer_details_view)
abstract class UpdateOfferDetailsModel : EpoxyModelWithHolder<UpdateOfferDetailsHolder>() {

    @EpoxyAttribute lateinit var title: String
    @EpoxyAttribute lateinit var onTitleClick: () -> Unit

    @EpoxyAttribute lateinit var description: String
    @EpoxyAttribute lateinit var onDescriptionClick: () -> Unit

    override fun bind(holder: UpdateOfferDetailsHolder) {
        holder.title.text = title
        holder.title.setOnClickListener { onTitleClick() }

        holder.description.text = description
        holder.description.setOnClickListener { onDescriptionClick() }
    }
}

class UpdateOfferDetailsHolder : KotlinHolder() {
    val title by bind<TextView>(R.id.update_offer_title)
    val description by bind<TextView>(R.id.update_offer_description)
}