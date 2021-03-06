package com.gpetuhov.android.hive.presentation.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.interactor.FavoritesInteractor
import com.gpetuhov.android.hive.domain.interactor.SearchInteractor
import com.gpetuhov.android.hive.domain.model.Offer
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.domain.repository.Repo
import com.gpetuhov.android.hive.presentation.view.SearchListFragmentView
import com.gpetuhov.android.hive.util.Constants
import com.gpetuhov.android.hive.util.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@InjectViewState
class SearchListFragmentPresenter :
    MvpPresenter<SearchListFragmentView>(),
    SearchInteractor.Callback,
    FavoritesInteractor.Callback {

    @Inject lateinit var repo: Repo
    @Inject lateinit var settings: Settings

    var queryLatitude = Constants.Map.DEFAULT_LATITUDE
    var queryLongitude = Constants.Map.DEFAULT_LONGITUDE
    var queryRadius = Constants.Map.DEFAULT_RADIUS
    var queryText = ""

    var searchResultList = mutableListOf<User>()

    // Keeps current text entered in search dialog
    private var tempQueryText = ""

    private val searchInteractor = SearchInteractor(this)
    private var favoritesInteractor = FavoritesInteractor(this)

    init {
        HiveApp.appComponent.inject(this)
    }

    // === SearchInteractor.Callback ===

    override fun onSearchComplete() = viewState.onSearchComplete()

    // === FavoritesInteractor.Callback ===

    override fun onFavoritesError(errorMessage: String) = viewState.showToast(errorMessage)

    // === Public methods ===

    fun initSearchQueryText() {
        queryText = settings.getSearchQueryText()
    }

    fun navigateUp() = viewState.navigateUp()

    fun onResume() {
        repo.setSearchListActive(true)
        search()
    }

    fun onPause() {
        repo.stopGettingSearchResultUpdates()
        repo.setSearchListActive(false)
    }

    fun updateSearchResult(searchResult: MutableMap<String, User>) {
        sortSearchResultList(searchResult.values.toMutableList()) { sortedList ->
            searchResultList.clear()
            searchResultList.addAll(sortedList)
            viewState.updateUI()
        }
    }

    fun showDetails(userUid: String, offerUid: String) {
        // This is needed to get user details immediately from the already available search results
        repo.initSearchUserDetails(userUid)
        viewState.showDetails(offerUid)
    }

    fun favorite(isFavorite: Boolean, userUid: String, offerUid: String) =
        favoritesInteractor.favorite(isFavorite, userUid, offerUid)

    fun filterIsDefault() = settings.getSearchFilter().isDefault

    fun sortIsDefault() = settings.getSearchSort().isDefault

    fun showFilter() = viewState.showFilter()
    
    fun showSort() = viewState.showSort()

    // --- Search dialog ---

    fun showSearchDialog() = viewState.showSearchDialog()

    // Prefill search dialog with currently entered text or current value
    fun getSearchPrefill() = if (tempQueryText != "") tempQueryText else queryText

    fun updateTempQueryText(newQueryText: String) {
        tempQueryText = newQueryText
    }

    fun startSearch() {
        queryText = tempQueryText
        dismissSearchDialog()
        search()
    }

    fun dismissSearchDialog() {
        tempQueryText = ""
        viewState.dismissSearchDialog()
    }

    // === Private methods ===

    private fun search() {
        viewState.onSearchStart()
        settings.setSearchQueryText(queryText)
        searchInteractor.search(queryLatitude, queryLongitude, queryRadius, queryText)
    }

    private fun sortSearchResultList(unsortedList: MutableList<User>, onComplete: (MutableList<User>) -> Unit) {
        GlobalScope.launch {
            val sortedList = mutableListOf<User>()
            val userList = mutableListOf<User>()
            val offerList = mutableListOf<User>()

            // Separate users and offers into different lists
            unsortedList.forEach { if (it.offerSearchResultIndex == -1) userList.add(it) else offerList.add(it) }

            val sort = settings.getSearchSort()

            offerList.sortWith(Comparator { user1, user2 ->
                val offer1 = user1.getSearchedOffer()
                val offer2 = user2.getSearchedOffer()

                if (offer1 != null && offer2 != null) {
                    val compareResult = when {
                        sort.isSortByTitle -> sortByNameOrTitle(offer1.title, offer2.title)
                        sort.isSortByPrice -> sortByPrice(offer1, offer2)
                        sort.isSortByRating -> sortByRating(offer1.rating, offer2.rating)
                        sort.isSortByReviewCount -> sortByCount(offer1.reviewCount, offer2.reviewCount)
                        sort.isSortByFavoriteStarCount -> sortByCount(offer1.starCount.toInt(), offer2.starCount.toInt())
                        sort.isSortByPhotoCount -> sortByCount(offer1.photoList.size, offer2.photoList.size)
                        else -> sortByDistance(offer1.distance, offer2.distance)
                    }

                    sortOrder(compareResult, !sort.isSortOrderAscending)

                } else {
                    0
                }
            })

            userList.sortWith(Comparator { user1, user2 ->
                // Users can by sorted only by name and rating and cannot be sorted by price
                val compareResult = when {
                    sort.isSortByTitle -> sortByNameOrTitle(user1.getUsernameOrName(), user2.getUsernameOrName())
                    sort.isSortByRating -> sortByRating(user1.averageRating, user2.averageRating)
                    sort.isSortByReviewCount -> sortByCount(user1.totalReviewsCount, user2.totalReviewsCount)
                    sort.isSortByFavoriteStarCount -> sortByCount(user1.totalStarCount.toInt(), user2.totalStarCount.toInt())
                    sort.isSortByPhotoCount -> sortByCount(user1.photoList.size, user2.photoList.size)
                    else -> sortByDistance(user1.distance, user2.distance)
                }

                sortOrder(compareResult, !sort.isSortOrderAscending)
            })

            if (sort.isSortOffersFirst) {
                sortedList.addAll(offerList)
                sortedList.addAll(userList)
            } else {
                sortedList.addAll(userList)
                sortedList.addAll(offerList)
            }

            launch(Dispatchers.Main) { onComplete(sortedList) }
        }
    }

    private fun sortByPrice(offer1: Offer, offer2: Offer): Int {
        return when {
            offer1.isFree && offer2.isFree -> 0
            !offer1.isFree && offer2.isFree -> 1
            offer1.isFree && !offer2.isFree -> -1
            else -> comparePrice(offer1, offer2)
        }
    }

    private fun comparePrice(offer1: Offer, offer2: Offer): Int {
        return when {
            offer1.price > offer2.price -> 1
            offer1.price == offer2.price -> 0
            else -> -1
        }
    }

    private fun sortByNameOrTitle(name1: String, name2: String): Int {
        return when {
            name1 > name2 -> 1
            name1 == name2 -> 0
            else -> -1
        }
    }

    private fun sortByRating(rating1: Float, rating2: Float): Int {
        return when {
            rating1 > rating2 -> 1
            rating1 == rating2 -> 0
            else -> -1
        }
    }

    private fun sortByCount(count1: Int, count2: Int): Int {
        return when {
            count1 > count2 -> 1
            count1 == count2 -> 0
            else -> -1
        }
    }

    private fun sortOrder(compareResult: Int, isDescending: Boolean): Int {
        return if (isDescending) {
            // If descending we change compare result to the opposite
            when(compareResult) {
                -1 -> 1
                1 -> -1
                else -> 0
            }
        } else {
            // Otherwise do not change compare result
            compareResult
        }
    }

    private fun sortByDistance(distance1: Double, distance2: Double): Int {
        return when {
            distance1 > distance2 -> 1
            distance1 == distance2 -> 0
            else -> -1
        }
    }
}