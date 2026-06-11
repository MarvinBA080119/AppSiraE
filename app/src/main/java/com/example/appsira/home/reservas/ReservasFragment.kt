package com.example.appsira.home.reservas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appsira.R
import com.example.appsira.core.FragmentCommunicator
import com.example.appsira.core.ResponseService
import com.example.appsira.databinding.FragmentReservasBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ReservasFragment : Fragment() {

    private var _binding: FragmentReservasBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReservasViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private val adapter = ReservasAdapter { reserva ->
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.cancelar_reserva_titulo))
            .setMessage(
                getString(R.string.cancelar_reserva_mensaje, reserva.auditorioNombre, reserva.fecha)
            )
            .setNegativeButton(getString(R.string.accion_no), null)
            .setPositiveButton(getString(R.string.accion_si_cancelar)) { _, _ ->
                viewModel.cancelarReserva(reserva.id)
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservasBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator

        binding.rvReservas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservas.adapter = adapter

        observeState()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadReservas()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.reservasState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> communicator.manageLoader(true)
                            is ResponseService.Success -> {
                                communicator.manageLoader(false)
                                adapter.submitList(state.data)
                                binding.tvEmpty.isVisible = state.data.isEmpty()
                            }
                            is ResponseService.Error -> {
                                communicator.manageLoader(false)
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            }
                            null -> Unit
                        }
                    }
                }

                launch {
                    viewModel.cancelarState.collect { state ->
                        when (state) {
                            is ResponseService.Success -> {
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.reserva_cancelada),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                viewModel.consumeCancelarState()
                            }
                            is ResponseService.Error -> {
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                                viewModel.consumeCancelarState()
                            }
                            else -> Unit
                        }
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
