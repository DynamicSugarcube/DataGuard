package com.schugarkub.dataguard.view.notificationsjournal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.viewmodel.BaseViewModelFactory
import com.schugarkub.dataguard.viewmodel.NotificationJournalViewModel

class NotificationsJournalFragment : Fragment() {

    private lateinit var viewModel: NotificationJournalViewModel

    private lateinit var notificationJournalRecyclerView: RecyclerView
    private lateinit var notificationJournalAdapter: NotificationJournalAdapter
    private lateinit var notificationJournalLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = BaseViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(NotificationJournalViewModel::class.java)

        viewModel.syncNotifications()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_notifications_journal, container, false)

        notificationJournalAdapter = NotificationJournalAdapter()
        notificationJournalLayoutManager = LinearLayoutManager(requireContext())
        notificationJournalRecyclerView =
            layout.findViewById<RecyclerView>(R.id.notifications_journal_recycler_view).apply {
                setHasFixedSize(true)
                layoutManager = notificationJournalLayoutManager
                adapter = notificationJournalAdapter
            }

        viewModel.notificationsLiveData.observe(this) {
            notificationJournalAdapter.notifications = it
        }

        return layout
    }
}