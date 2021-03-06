package com.gpetuhov.android.hive.models

import com.gpetuhov.android.hive.domain.model.Favorite
import com.gpetuhov.android.hive.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FavoriteTest {

    private lateinit var favorite: Favorite

    @Before
    fun initUser() {
        favorite = Constants.DUMMY_FAVORITE
    }

    @Test
    fun isFavoriteOfferTest() {
        assertEquals(false, favorite.isOffer())
        favorite.offerUid = "259gu549g"
        assertEquals(true, favorite.isOffer())
    }
}