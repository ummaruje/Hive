package com.gpetuhov.android.hive.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.auth.Auth
import com.gpetuhov.android.hive.managers.*
import com.gpetuhov.android.hive.ui.fragment.base.BaseFragment
import com.gpetuhov.android.hive.ui.viewmodel.UnreadMessagesExistViewModel
import com.gpetuhov.android.hive.util.checkPermissions
import com.pawegio.kandroid.startActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 101
    }

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var auth: Auth
    @Inject lateinit var mapManager: MapManager
    @Inject lateinit var locationMapManager: LocationMapManager
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var onlineStatusManager: OnlineStatusManager

    private lateinit var navController: NavController

    private var offlineSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        HiveApp.appComponent.inject(this)

        initNavigation()
        initAuthManager()
        initOfflineSnackbar()
        checkLocationSettings()

        val viewModel = ViewModelProviders.of(this).get(UnreadMessagesExistViewModel::class.java)
        viewModel.unreadMessagesExist.observe(this, Observer<Boolean> { unreadMessagesExist ->
            updateMessagesIcon(unreadMessagesExist)
        })
    }

    // This is needed to handle back pressed inside fragments
    override fun onBackPressed() {
        val fragmentList = nav_host.childFragmentManager.fragments

        var handled = false
        for (fragment in fragmentList) {
            if (fragment is BaseFragment && fragment.userVisibleHint) {
                handled = fragment.onBackPressed()

                if (handled) break
            }
        }

        if (!handled) super.onBackPressed()
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    override fun onResume() {
        super.onResume()
        checkPlayServicesAndPermissions()
        auth.startListenAuth()
        notificationManager.onResume()
        onlineStatusManager.onResume { connected -> showHideOfflineSnackbar(!connected) }
    }

    override fun onPause() {
        super.onPause()
        auth.stopListenAuth()
        notificationManager.onPause()
        onlineStatusManager.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode != Activity.RESULT_OK) {
                toast("Please, turn on geolocation")
                checkLocationSettings()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // This is needed to reset map states if the app is closed by the user
        if (!isChangingConfigurations) {
            mapManager.resetMapState()
            locationMapManager.resetMapState()
        }
    }

    // === Private methods ===

    private fun initNavigation() {
        // Find NavController
        navController = findNavController(R.id.nav_host)

        // Tie NavHostFragment to bottom navigation bar
        navigation_view.setupWithNavController(navController)
    }

    private fun checkLocationSettings() = locationManager.checkLocationSettings(this, REQUEST_CHECK_SETTINGS)

    private fun initAuthManager() = auth.init(this::onSignIn, this::onSignOut)

    private fun checkPlayServicesAndPermissions() {
        val playServicesAvailable = locationManager.checkPlayServices(this) {
            longToast(R.string.play_services_unavailable)
            finish()
        }

        if (playServicesAvailable) {
            if (!checkPermissions(this)) {
                startActivity<PermissionsActivity>()
                finish()
            }
        }
    }

    private fun onSignIn() { /* Do nothing */ }

    private fun onSignOut() {
        // If signed out, just start AuthActivity and finish.
        // AuthActivity will handle the login process.
        startActivity<AuthActivity>()
        finish()
    }

    private fun updateMessagesIcon(unreadMessagesExist: Boolean) {
        val iconId = if (unreadMessagesExist) R.drawable.ic_mail_unread else R.drawable.ic_mail_read
        navigation_view.menu.findItem(R.id.navigation_messages).icon = ContextCompat.getDrawable(this, iconId)
    }

    private fun initOfflineSnackbar() {
        // Create snackbar
        offlineSnackbar = Snackbar.make(coordinator_layout, R.string.connecting, Snackbar.LENGTH_INDEFINITE)

        // Set snackbar action
        offlineSnackbar?.setAction(R.string.close) {offlineSnackbar?.dismiss() }

        // Change snackbar background color and width
        val offlineSnackbarView = offlineSnackbar?.view
        offlineSnackbarView?.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))
        offlineSnackbarView?.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT

        // Change snackbar text color
        val offlineSnackbarTextView = offlineSnackbarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        offlineSnackbarTextView?.setTextColor(ContextCompat.getColor(this, R.color.md_grey_600))
    }

    private fun showHideOfflineSnackbar(show: Boolean) {
        if (show) offlineSnackbar?.show()
        else offlineSnackbar?.dismiss()
    }
}
