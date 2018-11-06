package com.gpetuhov.android.hive.domain.model

import com.google.android.gms.maps.model.LatLng

// Represents data both for the current user and search results.
// Models at domain layer are just POJOs for keeping data.
data class User(
    var uid: String,
    var name: String,
    var username: String,
    var email: String,
    var service: String,
    var isVisible: Boolean,
    var isOnline: Boolean,
    var location: LatLng
) {
    val hasUsername get() = username != ""
    val hasService get() = service != ""
    fun getUsernameOrName() = if (hasUsername) username else name
}