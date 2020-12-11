package com.schugarkub.dataguard.view.notificationsjournal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.DataGuardApplication
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.viewmodel.NotificationsJournalViewModel
import com.schugarkub.dataguard.viewmodel.NotificationsJournalViewModelFactory
import javax.inject.Inject

class NotificationsJournalFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: NotificationsJournalViewModelFactory
    private lateinit var viewModel: NotificationsJournalViewModel

    private lateinit var notificationJournalRecyclerView: RecyclerView
    private lateinit var notificationJournalAdapter: NotificationJournalAdapter
    private lateinit var notificationJournalLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as DataGuardApplication).fragmentComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(NotificationsJournalViewModel::class.java)
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

        viewModel.notificationsLiveData.observe(
            viewLifecycleOwner, { notifications ->
                notificationJournalAdapter.notifications = notifications
            }
        )

        return layout
    }
}