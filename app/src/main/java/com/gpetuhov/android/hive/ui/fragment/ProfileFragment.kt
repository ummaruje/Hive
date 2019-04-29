package com.gpetuhov.android.hive.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns.PHONE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.airbnb.epoxy.EpoxyRecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.ui.viewmodel.CurrentUserViewModel
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.presentation.presenter.ProfileFragmentPresenter
import com.gpetuhov.android.hive.presentation.view.ProfileFragmentView
import com.gpetuhov.android.hive.ui.epoxy.profile.controller.ProfileListController
import com.gpetuhov.android.hive.ui.fragment.base.BaseFragment
import com.gpetuhov.android.hive.util.*
import com.pawegio.kandroid.toast

class ProfileFragment : BaseFragment(), ProfileFragmentView {

    companion object {
        private const val RC_USERPIC_PICKER = 1001
        private const val RC_USER_PHOTO_PICKER = 1002
    }

    @InjectPresenter lateinit var presenter: ProfileFragmentPresenter

    private var controller: ProfileListController? = null

    private var usernameDialog: MaterialDialog? = null
    private var descriptionDialog: MaterialDialog? = null
    private var signOutDialog: MaterialDialog? = null
    private var deleteUserDialog: MaterialDialog? = null
    private var deletePhotoDialog: MaterialDialog? = null
    private var phoneDialog: MaterialDialog? = null
    private var emailDialog: MaterialDialog? = null
    private var skypeDialog: MaterialDialog? = null
    private var facebookDialog: MaterialDialog? = null
    private var twitterDialog: MaterialDialog? = null
    private var instagramDialog: MaterialDialog? = null
    private var youTubeDialog: MaterialDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Adjust_pan is needed to prevent activity from being pushed up by the keyboard
        setActivitySoftInputPan()

        hideMainHeader()
        showBottomNavigationView()

        initDialogs()

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        controller = ProfileListController(presenter)
        controller?.onRestoreInstanceState(savedInstanceState)

        val profileRecyclerView = view.findViewById<EpoxyRecyclerView>(R.id.profile_recycler_view)
        profileRecyclerView.adapter = controller?.adapter

        val viewModel = ViewModelProviders.of(this).get(CurrentUserViewModel::class.java)
        viewModel.currentUser.observe(this, Observer<User> { user ->
            controller?.changeUser(user)
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // This is needed to prevent memory leaks.
        // Here we intentially dismiss dialogs directly, not via the presenter,
        // so that Moxy command queue doesn't change.
        dismissDialogs()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_USERPIC_PICKER && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) presenter.changeUserPic(selectedImageUri)
        }

        if (requestCode == RC_USER_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) presenter.addPhoto(selectedImageUri)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller?.onSaveInstanceState(outState)
    }

    // === ProfileFragmentView ===

    override fun showSignOutDialog() = signOutDialog?.show() ?: Unit

    override fun dismissSignOutDialog() = signOutDialog?.dismiss() ?: Unit

    override fun enableSignOutButton() = signOutButtonEnabled(true) ?: Unit

    override fun disableSignOutButton() = signOutButtonEnabled(false) ?: Unit

    override fun showDeleteUserDialog() = deleteUserDialog?.show() ?: Unit

    override fun dismissDeleteUserDialog() = deleteUserDialog?.dismiss() ?: Unit

    override fun enableDeleteUserButton() = deleteUserButtonEnabled(true) ?: Unit

    override fun disableDeleteUserButton() = deleteUserButtonEnabled(false) ?: Unit

    override fun showUsernameDialog() {
        // Prefill dialog with text provided by presenter
        val editText = usernameDialog?.getInputField()
        editText?.setText(presenter.getUsernamePrefill())
        editText?.setSelection(editText.text.length)
        usernameDialog?.show()
    }

    override fun dismissUsernameDialog() = usernameDialog?.dismiss() ?: Unit

    override fun showDescriptionDialog() {
        // Prefill dialog with text provided by presenter
        val editText = descriptionDialog?.getInputField()
        editText?.setText(presenter.getDescriptionPrefill())
        editText?.setSelection(editText.text.length)
        descriptionDialog?.show()
    }

    override fun dismissDescriptionDialog() = descriptionDialog?.dismiss() ?: Unit

    override fun chooseUserPic() = startPhotoPicker(RC_USERPIC_PICKER)

    override fun updateOffer(offerUid: String) {
        val action = ProfileFragmentDirections.actionNavigationProfileToUpdateOfferFragment(offerUid)
        findNavController().navigate(action)
    }

    override fun choosePhoto() = startPhotoPicker(RC_USER_PHOTO_PICKER)

    override fun showDeletePhotoDialog() = deletePhotoDialog?.show() ?: Unit

    override fun dismissDeletePhotoDialog() = deletePhotoDialog?.dismiss() ?: Unit

    override fun openPhotos(photoUrlList: MutableList<String>) {
        val photoBundle = Bundle()
        photoBundle.putStringArrayList(PhotoFragment.PHOTO_URL_LIST_KEY, ArrayList(photoUrlList))

        val action = ProfileFragmentDirections.actionNavigationProfileToPhotoFragment(photoBundle)
        findNavController().navigate(action)
    }

    override fun showPhoneDialog() {
        // Prefill dialog with text provided by presenter
        val editText = phoneDialog?.getInputField()
        editText?.setText(presenter.getPhonePrefill())
        editText?.setSelection(editText.text.length)
        phoneDialog?.show()
    }

    override fun dismissPhoneDialog() = phoneDialog?.dismiss() ?: Unit

    override fun showEmailDialog() {
        // Prefill dialog with text provided by presenter
        val editText = emailDialog?.getInputField()
        editText?.setText(presenter.getEmailPrefill())
        editText?.setSelection(editText.text.length)
        emailDialog?.show()
    }

    override fun dismissEmailDialog() = emailDialog?.dismiss() ?: Unit

    override fun showSkypeDialog() {
        // Prefill dialog with text provided by presenter
        val editText = skypeDialog?.getInputField()
        editText?.setText(presenter.getSkypePrefill())
        editText?.setSelection(editText.text.length)
        skypeDialog?.show()
    }

    override fun dismissSkypeDialog() = skypeDialog?.dismiss() ?: Unit

    override fun showFacebookDialog() {
        // Prefill dialog with text provided by presenter
        val editText = facebookDialog?.getInputField()
        editText?.setText(presenter.getFacebookPrefill())
        editText?.setSelection(editText.text.length)
        facebookDialog?.show()
    }

    override fun dismissFacebookDialog() = facebookDialog?.dismiss() ?: Unit

    override fun showTwitterDialog() {
        // Prefill dialog with text provided by presenter
        val editText = twitterDialog?.getInputField()
        editText?.setText(presenter.getTwitterPrefill())
        editText?.setSelection(editText.text.length)
        twitterDialog?.show()
    }

    override fun dismissTwitterDialog() = twitterDialog?.dismiss() ?: Unit

    override fun showInstagramDialog() {
        // Prefill dialog with text provided by presenter
        val editText = instagramDialog?.getInputField()
        editText?.setText(presenter.getInstagramPrefill())
        editText?.setSelection(editText.text.length)
        instagramDialog?.show()
    }

    override fun dismissInstagramDialog() = instagramDialog?.dismiss() ?: Unit

    override fun showYouTubeDialog() {
        // Prefill dialog with text provided by presenter
        val editText = youTubeDialog?.getInputField()
        editText?.setText(presenter.getYouTubePrefill())
        editText?.setSelection(editText.text.length)
        youTubeDialog?.show()
    }

    override fun dismissYouTubeDialog() = youTubeDialog?.dismiss() ?: Unit

    override fun openPrivacyPolicy() {
        val action = ProfileFragmentDirections.actionNavigationProfileToPrivacyPolicyFragment()
        findNavController().navigate(action)
    }

    override fun openAllReviews() {
        val action = ProfileFragmentDirections.actionNavigationProfileToReviewsAllFragment(true)
        findNavController().navigate(action)
    }

    override fun showToast(message: String) {
        toast(message)
    }

    // === Private methods ===

    private fun initDialogs() {
        initUsernameDialog()
        initDescriptionDialog()
        initSignOutDialog()
        initDeleteUserDialog()
        initDeletePhotoDialog()
        initPhoneDialog()
        initEmailDialog()
        initSkypeDialog()
        initFacebookDialog()
        initTwitterDialog()
        initInstagramDialog()
        initYouTubeDialog()
    }

    private fun initUsernameDialog() {
        if (context != null) {
            val usernameErrorMessage = context?.getString(R.string.username_not_valid)

            usernameDialog = MaterialDialog(context!!)
                .title(R.string.username)
                .noAutoDismiss()
                .cancelable(false)
                .input(
                    maxLength = Constants.User.MAX_USERNAME_LENGTH,
                    hintRes = R.string.enter_username,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val inputText = text.toString()
                    var positiveButtonEnabled = inputText.length <= Constants.User.MAX_USERNAME_LENGTH
                    presenter.updateTempUsername(inputText)

                    dialog.getInputField()?.error = if (inputText.contains(" ")) {
                        positiveButtonEnabled = false
                        usernameErrorMessage
                    } else {
                        null
                    }

                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, positiveButtonEnabled)
                }
                .positiveButton { presenter.saveUsername() }
                .negativeButton { presenter.dismissUsernameDialog() }
        }
    }

    private fun initDescriptionDialog() {
        if (context != null) {
            descriptionDialog = MaterialDialog(context!!)
                .title(R.string.about_me)
                .noAutoDismiss()
                .cancelable(false)
                .input(
                    maxLength = Constants.User.MAX_DESCRIPTION_LENGTH,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                    hintRes = R.string.enter_description,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val inputText = text.toString()
                    val positiveButtonEnabled = inputText.length <= Constants.User.MAX_DESCRIPTION_LENGTH
                    presenter.updateTempDescription(inputText)
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, positiveButtonEnabled)
                }
                .positiveButton { presenter.saveDescription() }
                .negativeButton { presenter.dismissDescriptionDialog() }
        }
    }

    private fun initSignOutDialog() {
        if (context != null) {
            signOutDialog = MaterialDialog(context!!)
                .title(R.string.sign_out)
                .message(R.string.prompt_sign_out)
                .noAutoDismiss()
                .cancelable(false)
                .positiveButton { presenter.signOut() }
                .negativeButton { presenter.signOutCancel() }
        }
    }

    private fun initDeleteUserDialog() {
        if (context != null) {
            deleteUserDialog = MaterialDialog(context!!)
                .title(R.string.delete_account)
                .message(R.string.prompt_delete_account)
                .noAutoDismiss()
                .cancelable(false)
                .positiveButton { presenter.deleteUser() }
                .negativeButton { presenter.deleteUserCancel() }
        }
    }

    private fun initDeletePhotoDialog() {
        if (context != null) {
            deletePhotoDialog = MaterialDialog(context!!)
                .title(R.string.delete_photo)
                .message(R.string.prompt_delete_photo)
                .noAutoDismiss()
                .cancelable(false)
                .positiveButton { presenter.deletePhoto() }
                .negativeButton { presenter.deletePhotoCancel() }
        }
    }

    private fun initPhoneDialog() {
        if (context != null) {
            val phoneErrorMessage = context?.getString(R.string.phone_not_valid) ?: ""

            phoneDialog = MaterialDialog(context!!)
                .title(R.string.phone_number)
                .noAutoDismiss()
                .cancelable(false)
                .input(
                    inputType = InputType.TYPE_CLASS_PHONE,
                    hintRes = R.string.enter_phone,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val inputText = text.toString()
                    presenter.updateTempPhone(inputText)

                    val phoneEmpty = inputText == ""
                    val phoneValid = PHONE.matcher(inputText).matches()
                    val validOrEmpty = phoneEmpty || phoneValid

                    dialog.getInputField()?.error = if (!validOrEmpty) phoneErrorMessage else null

                    // Enable positive button on empty or valid phone
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, validOrEmpty)
                }
                .positiveButton { presenter.savePhone() }
                .negativeButton { presenter.dismissPhoneDialog() }
        }
    }

    private fun initEmailDialog() {
        if (context != null) {
            val emailErrorMessage = context?.getString(R.string.email_not_valid) ?: ""

            emailDialog = MaterialDialog(context!!)
                .title(R.string.email)
                .noAutoDismiss()
                .cancelable(false)
                .input(
                    inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    hintRes = R.string.enter_email,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val inputText = text.toString()
                    presenter.updateTempEmail(inputText)

                    val emailEmpty = inputText == ""
                    val emailValid = EMAIL_ADDRESS.matcher(inputText).matches()
                    val validOrEmpty = emailEmpty || emailValid

                    dialog.getInputField()?.error = if (!validOrEmpty) emailErrorMessage else null

                    // Enable positive button on empty or valid email
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, validOrEmpty)
                }
                .positiveButton { presenter.saveEmail() }
                .negativeButton { presenter.dismissEmailDialog() }
        }
    }

    private fun getSocialUsernameInputDialog(
        titleId: Int,
        hintId: Int,
        onInputChange: (String) -> Unit,
        isInputValid: (String) -> Boolean,
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): MaterialDialog? {

        if (context != null) {
            val errorMessage = context?.getString(R.string.username_not_valid)

            return MaterialDialog(context!!)
                .title(titleId)
                .noAutoDismiss()
                .cancelable(false)
                .input(
                    hintRes = hintId,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val inputText = text.toString()
                    onInputChange(inputText)

                    val valid = isInputValid(inputText)
                    dialog.getInputField()?.error = if (!valid) errorMessage else null
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, valid)
                }
                .positiveButton { onPositive() }
                .negativeButton { onNegative() }

        } else {
            return null
        }
    }

    private fun emptyOrNoSpaces(inputText: String) = inputText == "" || !inputText.contains(" ")

    private fun initSkypeDialog() {
        skypeDialog = getSocialUsernameInputDialog(
            R.string.skype,
            R.string.enter_skype,
            { inputText -> presenter.updateTempSkype(inputText) },
            { inputText -> emptyOrNoSpaces(inputText) },
            { presenter.saveSkype() },
            { presenter.dismissSkypeDialog() }
        )
    }

    private fun initFacebookDialog() {
        facebookDialog = getSocialUsernameInputDialog(
            R.string.facebook,
            R.string.enter_facebook,
            { inputText -> presenter.updateTempFacebook(inputText) },
            { inputText -> emptyOrNoSpaces(inputText) },
            { presenter.saveFacebook() },
            { presenter.dismissFacebookDialog() }
        )
    }

    private fun initTwitterDialog() {
        twitterDialog = getSocialUsernameInputDialog(
            R.string.twitter,
            R.string.enter_twitter,
            { inputText -> presenter.updateTempTwitter(inputText) },
            { inputText -> emptyOrNoSpaces(inputText) },
            { presenter.saveTwitter() },
            { presenter.dismissTwitterDialog() }
        )
    }

    private fun initInstagramDialog() {
        instagramDialog = getSocialUsernameInputDialog(
            R.string.instagram,
            R.string.enter_instagram,
            { inputText -> presenter.updateTempInstagram(inputText) },
            { inputText -> emptyOrNoSpaces(inputText) },
            { presenter.saveInstagram() },
            { presenter.dismissInstagramDialog() }
        )
    }

    private fun initYouTubeDialog() {
        youTubeDialog = getSocialUsernameInputDialog(
            R.string.youtube,
            R.string.enter_youtube,
            { inputText -> presenter.updateTempYouTube(inputText) },
            { inputText -> emptyOrNoSpaces(inputText) },
            { presenter.saveYouTube() },
            { presenter.dismissYouTubeDialog() }
        )
    }

    private fun dismissDialogs() {
        dismissUsernameDialog()
        dismissDescriptionDialog()
        dismissSignOutDialog()
        dismissDeleteUserDialog()
        dismissDeletePhotoDialog()
        dismissPhoneDialog()
        dismissEmailDialog()
        dismissSkypeDialog()
        dismissFacebookDialog()
        dismissTwitterDialog()
        dismissInstagramDialog()
        dismissYouTubeDialog()
    }

    private fun signOutButtonEnabled(isEnabled: Boolean) = controller?.signOutEnabled(isEnabled)

    private fun deleteUserButtonEnabled(isEnabled: Boolean) = controller?.deleteAccountEnabled(isEnabled)
}