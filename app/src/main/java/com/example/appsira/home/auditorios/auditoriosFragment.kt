package com.example.appsira.home.auditorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appsira.R
import com.example.appsira.core.FragmentCommunicator
import com.example.appsira.core.ResponseService
import com.example.appsira.databinding.FragmentAuditoriosBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class auditoriosFragment : Fragment() {

    private var _binding: FragmentAuditoriosBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AuditorioViewModel>()
    private val sharedViewModel by activityViewModels<AuditoriosSharedViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private val adapter = AuditoriosAdapter { auditorio ->
        sharedViewModel.selectAuditorio(auditorio)
        findNavController().navigate(R.id.action_auditorios_to_detail)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditoriosBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator

        binding.rvAuditorios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAuditorios.adapter = adapter

        observeState()
        viewModel.loadAuditorios()
        return binding.root
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.auditorioState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            adapter.submitList(state.data)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
