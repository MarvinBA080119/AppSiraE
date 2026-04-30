package com.example.appsira

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.appsira.core.FragmentCommunicator
import com.example.appsira.core.ResponseService
import com.example.appsira.databinding.FragmentRecuperarContraBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RecuperarContraFragment : Fragment() {

    private var _binding: FragmentRecuperarContraBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RecuperarContraViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecuperarContraBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupValidation() {
        binding.btnSendLink.isEnabled = false
        binding.etEmailRecover.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val email = binding.etEmailRecover.text.toString().trim()
        binding.tilEmailRecover.error = viewModel.validateEmail(email)
        binding.btnSendLink.isEnabled = viewModel.isRecoverFormValid(email)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnSendLink.setOnClickListener {
            val email = binding.etEmailRecover.text.toString().trim()
            viewModel.requestPasswordReset(email)
        }
        binding.tvGoLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recoverState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnSendLink.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            Snackbar.make(
                                binding.root,
                                "Te enviamos las instrucciones a tu correo",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnSendLink.isEnabled = true
                            Snackbar.make(binding.root, state.error,
                                Snackbar.LENGTH_LONG).show()
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
