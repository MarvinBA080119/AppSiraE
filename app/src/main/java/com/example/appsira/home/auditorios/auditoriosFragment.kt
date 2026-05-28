package com.example.appsira.home.auditorios

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAuditoriosBinding.inflate(inflater,container, false)
        communicator = requireActivity() as FragmentCommunicator
        observeState()
        viewModel.loadTracks()
        return binding.root
    }
    fun observeState(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songState.collect { state ->
                    when(state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            Log.i("Auditorios", "Auditorios List: ${state.data}")
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }
}