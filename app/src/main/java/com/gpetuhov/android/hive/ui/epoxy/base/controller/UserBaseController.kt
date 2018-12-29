package com.gpetuhov.android.hive.ui.epoxy.base.controller

import android.content.Context
import android.os.Bundle
import com.airbnb.epoxy.Carousel
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.domain.model.Offer
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.ui.epoxy.base.carousel
import com.gpetuhov.android.hive.ui.epoxy.base.withModelsFrom
import com.gpetuhov.android.hive.ui.epoxy.offer.item.models.offerItem
import com.gpetuhov.android.hive.ui.epoxy.photo.item.models.PhotoOfferItemModel_
import com.gpetuhov.android.hive.ui.epoxy.user.details.models.summary
import com.gpetuhov.android.hive.util.Constants
import com.gpetuhov.android.hive.util.Settings
import com.gpetuhov.android.hive.util.getDateFromTimestampInMilliseconds

// Base controller for profile and user details
abstract class UserBaseController : BaseController() {

    companion object {
        private const val SELECTED_OFFER_PHOTO_MAP_KEY = "selectedOfferPhotoMap"
    }

    protected var user: User? = null

    private var selectedOfferPhotoMap = hashMapOf<String, Int>()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SELECTED_OFFER_PHOTO_MAP_KEY, selectedOfferPhotoMap)
    }

    override fun onRestoreInstanceState(inState: Bundle?) {
        super.onRestoreInstanceState(inState)
        val restored = inState?.getSerializable(SELECTED_OFFER_PHOTO_MAP_KEY)
        if (restored != null) selectedOfferPhotoMap = restored as HashMap<String, Int>
    }

    // === Public methods ===

    open fun changeUser(user: User) {
        this.user = user
        requestModelBuild()
    }

    // === Protected methods ===

    protected fun userOfferItem(
        context: Context,
        settings: Settings,
        offer: Offer,
        isProfile: Boolean,
        favoriteButtonVisible: Boolean,
        onFavoriteButtonClick: () -> Unit,
        onClick: () -> Unit
    ) {

        offerItemPhotoCarousel(settings, offer, !isProfile, onClick)
        offerItemDetails(context, settings, offer, isProfile, favoriteButtonVisible, onFavoriteButtonClick, onClick)
    }

    protected fun summary(context: Context) {
        summary {
            id("summary")

            val creationTimestamp = user?.creationTimestamp ?: 0
            val creationDate = getDateFromTimestampInMilliseconds(creationTimestamp)

            creationDate("${context.getString(R.string.user_creation_date)} $creationDate")
            creationDateVisible(creationTimestamp != 0L)

            val firstOfferPublishedTimestamp = user?.firstOfferPublishedTimestamp ?: 0
            val firstOfferPublishedDate = getDateFromTimestampInMilliseconds(firstOfferPublishedTimestamp)

            firstOfferCreationDate("${context.getString(R.string.user_first_offer_creation_date)} $firstOfferPublishedDate")
            firstOfferCreationDateVisible(firstOfferPublishedTimestamp != 0L)

            val activeOfferList = user?.offerList?.filter { it.isActive }
            val activeOffersCount = activeOfferList?.size ?: 0
            activeOffersCount("${context.getString(R.string.user_active_offers_count)}: $activeOffersCount")

            var totalReviewsCount = 0
            activeOfferList?.forEach { totalReviewsCount += it.reviewCount }
            totalReviewsCount("${context.getString(R.string.user_total_reviews_count)}: $totalReviewsCount")
        }
    }

    // === Private methods ===

    private fun offerItemPhotoCarousel(settings: Settings, offer: Offer, limitVisible: Boolean, onClick: () -> Unit) {
        var offerPhotoList = offer.photoList

        if (limitVisible) {
            offerPhotoList = offerPhotoList
                .filterIndexed { index, item -> index < Constants.Offer.MAX_VISIBLE_PHOTO_COUNT }
                .toMutableList()
        }

        if (!offerPhotoList.isEmpty()) {
            carousel {
                id("${offer.uid}_photo_carousel")

                val padding = Carousel.Padding.dp(16, 0, 16, 0, 0)
                padding(padding)

                onBind { model, view, position ->
                    view.clipToPadding = true
                    view.addOnScrollListener(
                        buildScrollListener { lastScrollPosition -> selectedOfferPhotoMap[offer.uid] = lastScrollPosition}
                    )
                }

                withModelsFrom(offerPhotoList) { index, photo ->
                    PhotoOfferItemModel_()
                        .id(photo.uid)
                        .photoUrl(photo.downloadUrl)
                        .onClick {
                            settings.setSelectedPhotoPosition(index)
                            onClick()
                        }
                }
            }
        }
    }

    private fun offerItemDetails(
        context: Context,
        settings: Settings,
        offer: Offer,
        offerActiveVisible: Boolean,
        favoriteButtonVisible: Boolean,
        onFavoriteButtonClick: () -> Unit,
        onClick: () -> Unit
    ) {

        offerItem {
            id(offer.uid)
            active(offer.isActive)
            activeVisible(offerActiveVisible)
            title(offer.title)
            free(offer.isFree)
            price(if (offer.isFree) context.getString(R.string.free_caps) else "${offer.price} USD")
            favorite(offer.isFavorite)
            favoriteButtonVisible(favoriteButtonVisible)
            onFavoriteButtonClick { onFavoriteButtonClick() }
            rating(offer.rating)
            reviewCount(offer.reviewCount)
            onClick {
                settings.setSelectedPhotoPosition(selectedOfferPhotoMap[offer.uid] ?: 0)
                onClick()
            }
        }
    }
}