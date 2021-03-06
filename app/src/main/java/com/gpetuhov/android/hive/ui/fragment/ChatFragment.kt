package com.gpetuhov.android.hive.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.databinding.FragmentChatBinding
import com.gpetuhov.android.hive.domain.model.Message
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.presentation.presenter.ChatFragmentPresenter
import com.gpetuhov.android.hive.presentation.view.ChatFragmentView
import com.gpetuhov.android.hive.ui.adapter.MessagesAdapter
import com.gpetuhov.android.hive.ui.fragment.base.BaseFragment
import com.gpetuhov.android.hive.ui.viewmodel.ChatMessagesViewModel
import com.gpetuhov.android.hive.util.*
import com.pawegio.kandroid.toast
import kotlinx.android.synthetic.main.fragment_chat.*
import timber.log.Timber

// Shows LIMITED number of last messages in the chat with real-time updates.
// User can send new messages here.
// User can see ALL chat messages in ChatArchiveFragment.

class ChatFragment : BaseFragment(), ChatFragmentView {

    companion object {
        private const val TAG = "ChatFragment"
    }

    @InjectPresenter lateinit var presenter: ChatFragmentPresenter

    private var messagesAdapter: MessagesAdapter? = null
    private var binding: FragmentChatBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Adjust_resize is needed to push activity up, when keyboard is shown,
        // so that the recycler view will scroll to previously shown position after keyboard is shown.
        setActivitySoftInputResize()

        showMainHeader(
            { presenter.navigateUp() },
            { presenter.openUserDetails() },
            { presenter.openChatArchive() }
        )

        showBottomNavigationView()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        binding?.presenter = presenter

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendButtonEnabled(false)

        val viewModel = ViewModelProviders.of(this).get(ChatMessagesViewModel::class.java)

        messagesAdapter = MessagesAdapter(presenter, viewModel.messages.value)

        initMessagesList()

        viewModel.messages.observe(this, Observer<MutableList<Message>> { messageList ->
            messagesAdapter?.setMessages(messageList)
        })
        viewModel.secondUser.observe(this, Observer<User> { secondUser ->
            presenter.secondUserUid = secondUser.uid
            setMainHeaderUserPic(secondUser)
            setMainHeaderTitle(secondUser.getUsernameOrName())
            setMainHeaderOnlineAndLastSeen(secondUser.isOnline, secondUser.getLastSeenTime(), secondUser.isDeleted)
        })
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()

        // This is needed to restore last scroll position
        // and to show or hide bottom navigation on keyboard show or hide.
        // This will be triggered only if windowSoftInputMode="adjustResize" is set for the activity.
        getActivityRootView()?.addOnLayoutChangeListener(presenter.layoutChangeListener)
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()

        getActivityRootView()?.removeOnLayoutChangeListener(presenter.layoutChangeListener)
    }

    // === ChatFragmentView ===

    override fun sendButtonEnabled(isEnabled: Boolean) {
        message_send_button.isEnabled = isEnabled
    }

    override fun showScrollDownButton() {
        if (scroll_down_button.visibility != View.VISIBLE) scroll_down_button.show()
    }

    override fun hideScrollDownButton() {
        if (scroll_down_button.visibility == View.VISIBLE) scroll_down_button.hide()
    }

    override fun clearMessageText() = message_text.setText("")

    override fun openUserDetails() {
        val action = ChatFragmentDirections.actionChatFragmentToUserDetailsFragment()
        findNavController().navigate(action)

        hideSoftKeyboard()
    }

    override fun openChatArchive() {
        val action = ChatFragmentDirections.actionChatFragmentToChatArchiveFragment()
        findNavController().navigate(action)

        hideSoftKeyboard()
    }

    override fun scrollToPosition(position: Int) = messages.scrollToPosition(position)

    override fun scrollToPositionWithOffset(position: Int) {
        messages.post {
            // Scroll like this, because
            // RecyclerView.scrollToPosition() does not move item to top of the list,
            // it just scrolls until item is visible on screen.
            (messages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
        }
    }

    override fun showBottomNavigation() = showBottomNavigationView()

    override fun hideBottomNavigation() = hideBottomNavigationView()

    override fun showToast(message: String) {
        toast(message)
    }

    override fun navigateUp() {
        findNavController().navigateUp()
        hideSoftKeyboard()
    }

    // === Private methods ===

    private fun getActivityRootView() = activity?.findViewById<View>(R.id.main_activity_root_view)

    private fun initMessagesList() {
        messages.layoutManager = object : LinearLayoutManager(context, RecyclerView.VERTICAL, true) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
                // This is needed to avoid exception on navigation between chat and user details
                try {
                    super.onLayoutChildren(recycler, state)
                } catch (e: IndexOutOfBoundsException) {
                    Timber.tag(TAG).d(e)
                }
            }
        }

        messages.adapter = messagesAdapter
        messages.addOnScrollListener(presenter.scrollListener)
    }
}