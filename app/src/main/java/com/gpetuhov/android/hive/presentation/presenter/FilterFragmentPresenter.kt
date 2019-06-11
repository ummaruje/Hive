package com.gpetuhov.android.hive.presentation.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.model.Filter
import com.gpetuhov.android.hive.presentation.view.FilterFragmentView
import com.gpetuhov.android.hive.util.Settings
import javax.inject.Inject

@InjectViewState
class FilterFragmentPresenter : MvpPresenter<FilterFragmentView>() {

    @Inject lateinit var settings: Settings

    private var filter = Filter()
    private var isFilterChanged = false

    init {
        HiveApp.appComponent.inject(this)
    }

    // === Public methods ===

    // --- Init presenter ---

    fun init() {
        // This check is needed to prevent overwriting changed filter
        // with filter from settings on screen rotation.
        if (!isFilterChanged) filter = settings.getSearchFilter()
    }

    // --- Basic filter params ---

    fun showUsersOffersAll() {
        setFilterChanged()
        filter.setShowUsersOffersAll()
    }

    fun isShowUsersOffersAll() = filter.isShowUsersOffersAll

    fun showUsersOnly() {
        setFilterChanged()
        filter.setShowUsersOnly()
    }

    fun isShowUsersOnly() = filter.isShowUsersOnly

    fun showOffersOnly() {
        setFilterChanged()
        filter.setShowOffersOnly()
    }

    fun isShowOffersOnly() = filter.isShowOffersOnly

    // --- Clear filter ---

    fun showClearFilterDialog() = viewState.showClearFilterDialog()

    fun clearFilter() {
        setFilterChanged()
        filter = Filter()
        viewState.updateUI()
        dismissClearFilterDialog()
    }

    fun dismissClearFilterDialog() = viewState.dismissClearFilterDialog()

    // --- Show results ---

    fun showResult() {
        settings.setSearchFilter(filter)
        navigateUp()
    }

    // --- Navigation ---

    fun navigateUp() = viewState.navigateUp()

    // === Private methods ===

    private fun setFilterChanged() {
        isFilterChanged = true
    }
}