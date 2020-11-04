package com.schugarkub.dataguard.view.applicationslist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.viewmodel.applicationslist.ApplicationsListViewModel
import com.schugarkub.dataguard.viewmodel.applicationslist.ApplicationsListViewModelFactory
import timber.log.Timber

class ApplicationsListFragment : Fragment() {

    private lateinit var viewModel: ApplicationsListViewModel

    private lateinit var applicationsListRecyclerView: RecyclerView
    private lateinit var applicationsListAdapter: ApplicationsListAdapter
    private lateinit var applicationListLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = ApplicationsListViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ApplicationsListViewModel::class.java)

        viewModel.syncApplications()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_applications_list, container, false)

        applicationsListAdapter = ApplicationsListAdapter()
        applicationListLayoutManager = LinearLayoutManager(requireContext())
        applicationsListRecyclerView =
            layout.findViewById<RecyclerView>(R.id.applications_list_recycler_view).apply {
                setHasFixedSize(true)
                layoutManager = applicationListLayoutManager
                adapter = applicationsListAdapter
            }

        viewModel.applicationsLiveData.observe(viewLifecycleOwner) {
            applicationsListAdapter.applications = it
        }

        return layout
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}