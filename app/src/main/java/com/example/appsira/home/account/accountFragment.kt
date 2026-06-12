package com.example.appsira.home.account

import android.content.Intent
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
import com.example.appsira.R
import com.example.appsira.core.FragmentCommunicator
import com.example.appsira.core.ResponseService
import com.example.appsira.databinding.FragmentAccountBinding
import com.example.appsira.onboarding.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class accountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator

        binding.tvEmail.text = auth.currentUser?.email
            ?: getString(R.string.cuenta_sin_sesion)

        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        observeState()
        viewModel.loadProfile()
        return binding.root
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            bindProfile(state.data)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.infoContainer.isVisible = false
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun bindProfile(profile: com.example.appsira.onboarding.personal.model.UserProfile) {
        binding.infoContainer.isVisible = true
        binding.tvNombreCompleto.text =
            getString(R.string.cuenta_nombre_completo, profile.firstName, profile.lastName)
        binding.tvUsernameValue.text = profile.userName
        binding.tvPhoneValue.text = profile.phone
        binding.tvBirthDateValue.text = profile.birthDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
