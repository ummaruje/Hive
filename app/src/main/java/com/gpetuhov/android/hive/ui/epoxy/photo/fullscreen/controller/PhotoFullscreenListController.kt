package com.gpetuhov.android.hive.ui.epoxy.photo.fullscreen.controller

import android.content.Context
import androidx.core.content.edit
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyController
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.presentation.presenter.PhotoFragmentPresenter
import com.gpetuhov.android.hive.ui.epoxy.photo.fullscreen.models.PhotoFullscreenItemModel_
import com.gpetuhov.android.hive.util.epoxy.carousel
import com.gpetuhov.android.hive.util.epoxy.withModelsFrom
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Inject

class PhotoFullscreenListController(private val presenter: PhotoFragmentPresenter) : EpoxyController() {

    @Inject lateinit var context: Context

    init {
        HiveApp.appComponent.inject(this)
    }

    private var photoUrlList = mutableListOf<String>()
    private var selectedPhotoPosition = 0

    override fun buildModels() {
        if (!photoUrlList.isEmpty()) {
            carousel {
                id("photo_carousel")

                // Scroll to the selected photo
                // (onBind() is called, when models are rebuilt)
                onBind { model, view, position ->
                    view.scrollToPosition(selectedPhotoPosition)
                    context.defaultSharedPreferences.edit { putInt("selectedPhotoPosition", selectedPhotoPosition) }
                }

                // This adds spacing between photos
                val padding = Carousel.Padding.dp(0, 16)
                padding(padding)

                withModelsFrom(photoUrlList) {
                    PhotoFullscreenItemModel_()
                        .id(it)
                        .photoUrl(it)
                }
            }
        }
    }

    fun setPhotos(selectedPhotoPosition: Int, photoUrlList: MutableList<String>) {
        this.selectedPhotoPosition = selectedPhotoPosition
        this.photoUrlList = photoUrlList
        requestModelBuild()
    }
}