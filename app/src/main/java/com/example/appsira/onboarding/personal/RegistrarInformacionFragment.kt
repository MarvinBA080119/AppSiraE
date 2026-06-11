package com.example.appsira.onboarding.personal

import android.content.Intent
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
import com.example.appsira.R
import com.example.appsira.core.FragmentCommunicator
import com.example.appsira.core.ResponseService
import com.example.appsira.databinding.FragmentRegistrarInformacionBinding
import com.example.appsira.home.HomeActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RegistrarInformacionFragment : Fragment() {

    private var _binding: FragmentRegistrarInformacionBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegistrarInformacionViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarInformacionBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupValidation() {
        binding.btnFinish.isEnabled = false
        binding.etName.addTextChangedListener { validateAndEnable() }
        binding.etLastName.addTextChangedListener { validateAndEnable() }
        binding.etUsername.addTextChangedListener { validateAndEnable() }
        binding.etPhone.addTextChangedListener { validateAndEnable() }
        binding.etBirthDate.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val firstName = binding.etName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()

        binding.tilName.error = viewModel.validateFirstName(firstName)
        binding.tilLastName.error = viewModel.validateLastName(lastName)
        binding.tilUsername.error = viewModel.validateUsername(username)
        binding.tilPhone.error = viewModel.validatePhone(phone)
        binding.tilBirthDate.error = viewModel.validateBirthDate(birthDate)

        binding.btnFinish.isEnabled =
            viewModel.isFormValid(firstName, lastName, username, phone, birthDate)
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_picker_title))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            picker.addOnPositiveButtonClickListener { selectionUtc ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                binding.etBirthDate.setText(sdf.format(Date(selectionUtc)))
            }
            picker.show(parentFragmentManager, "BIRTH_DATE_PICKER")
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnFinish.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveProfile(
                uid = uid,
                firstName = binding.etName.text.toString().trim(),
                lastName = binding.etLastName.text.toString().trim(),
                username = binding.etUsername.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim(),
                birthDate = binding.etBirthDate.text.toString().trim()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnFinish.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnFinish.isEnabled = true
                            Snackbar.make(
                                binding.root, state.error, Snackbar.LENGTH_LONG
                            ).show()
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
