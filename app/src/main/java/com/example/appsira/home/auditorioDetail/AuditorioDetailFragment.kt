package com.example.appsira.home.auditorioDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.appsira.R
import com.example.appsira.core.ResponseService
import com.example.appsira.core.model.Auditorio
import com.example.appsira.databinding.FragmentAuditorioDetailBinding
import com.example.appsira.home.auditorios.AuditoriosSharedViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AuditorioDetailFragment : Fragment() {

    private var _binding: FragmentAuditorioDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AuditorioDetailViewModel>()
    private val sharedViewModel by activityViewModels<AuditoriosSharedViewModel>()
    private lateinit var auditorio: Auditorio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditorioDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selected = sharedViewModel.selectedAuditorio.value
        if (selected == null) {
            findNavController().navigateUp()
            return
        }
        auditorio = selected
        bindAuditorioInfo()
        setupListeners()
        observeViewModel()
    }

    private fun bindAuditorioInfo() {
        binding.tvNombre.text = auditorio.nombre
        binding.tvCapacidad.text =
            getString(R.string.capacidad_personas, auditorio.capacidad)
        Glide.with(binding.ivAuditorio)
            .load(auditorio.imagen)
            .placeholder(R.color.brand_primary_light)
            .centerCrop()
            .into(binding.ivAuditorio)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnMicMenos.setOnClickListener {
            val actual = binding.tvNumMicrofonos.text.toString().toInt()
            if (actual > 0) binding.tvNumMicrofonos.text = (actual - 1).toString()
        }
        binding.btnMicMas.setOnClickListener {
            val actual = binding.tvNumMicrofonos.text.toString().toInt()
            if (actual < 10) binding.tvNumMicrofonos.text = (actual + 1).toString()
        }

        binding.btnFecha.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.selecciona_fecha_reserva))
                .setCalendarConstraints(constraints)
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val fecha = sdf.format(Date(millis))
                binding.btnFecha.text = fecha
                viewModel.seleccionarFecha(fecha, auditorio.id)
            }
            picker.show(parentFragmentManager, "RESERVA_DATE_PICKER")
        }

        binding.btnHora.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(10)
                .setMinute(0)
                .setTitleText(getString(R.string.selecciona_hora_reserva))
                .build()
            picker.addOnPositiveButtonClickListener {
                val hora = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
                binding.btnHora.text = hora
                viewModel.seleccionarHora(hora)
            }
            picker.show(parentFragmentManager, "RESERVA_TIME_PICKER")
        }

        binding.btnReservar.setOnClickListener {
            viewModel.reservar(
                auditorio = auditorio,
                sistemaAudio = binding.switchAudio.isChecked,
                maestroCeremonia = binding.switchMaestro.isChecked,
                mesaCentral = binding.switchMesa.isChecked,
                numMicrofonos = binding.tvNumMicrofonos.text.toString().toInt()
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.disponibilidadState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> {
                                binding.tvDisponibilidad.isVisible = true
                                binding.tvDisponibilidad.text =
                                    getString(R.string.verificando_disponibilidad)
                                binding.tvDisponibilidad.setTextColor(
                                    requireContext().getColor(R.color.brand_primary_light)
                                )
                                binding.btnReservar.isEnabled = false
                            }
                            is ResponseService.Success -> {
                                binding.tvDisponibilidad.isVisible = true
                                if (state.data) {
                                    binding.tvDisponibilidad.text =
                                        getString(R.string.fecha_disponible)
                                    binding.tvDisponibilidad.setTextColor(
                                        requireContext().getColor(R.color.estado_disponible)
                                    )
                                    binding.btnReservar.isEnabled = true
                                } else {
                                    binding.tvDisponibilidad.text =
                                        getString(R.string.fecha_ocupada)
                                    binding.tvDisponibilidad.setTextColor(
                                        requireContext().getColor(R.color.estado_ocupado)
                                    )
                                    binding.btnReservar.isEnabled = false
                                }
                            }
                            is ResponseService.Error -> {
                                binding.tvDisponibilidad.isVisible = true
                                binding.tvDisponibilidad.text = state.error
                                binding.tvDisponibilidad.setTextColor(
                                    requireContext().getColor(R.color.estado_ocupado)
                                )
                                binding.btnReservar.isEnabled = false
                            }
                            null -> binding.tvDisponibilidad.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.reservaState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> binding.btnReservar.isEnabled = false
                            is ResponseService.Success -> {
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.reserva_exitosa),
                                    Snackbar.LENGTH_LONG
                                ).show()
                                viewModel.consumeReservaState()
                                findNavController().navigateUp()
                            }
                            is ResponseService.Error -> {
                                binding.btnReservar.isEnabled = true
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                                viewModel.consumeReservaState()
                            }
                            null -> Unit
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
