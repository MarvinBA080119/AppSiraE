package com.example.appsira.home.reservaEdit

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
import com.example.appsira.core.model.Reserva
import com.example.appsira.databinding.FragmentReservaEditBinding
import com.example.appsira.home.reservas.ReservasSharedViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReservaEditFragment : Fragment() {

    private var _binding: FragmentReservaEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ReservaEditViewModel>()
    private val sharedViewModel by activityViewModels<ReservasSharedViewModel>()
    private lateinit var reserva: Reserva

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservaEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selected = sharedViewModel.selectedReserva.value
        if (selected == null) {
            findNavController().navigateUp()
            return
        }
        reserva = selected
        viewModel.init(reserva)
        bindReservaInfo()
        setupListeners()
        observeViewModel()
    }

    private fun bindReservaInfo() {
        binding.tvNombre.text = reserva.auditorioNombre
        binding.tvCapacidad.text =
            getString(R.string.capacidad_personas, reserva.auditorioCapacidad)
        Glide.with(binding.ivAuditorio)
            .load(reserva.auditorioImagen)
            .placeholder(R.color.brand_primary_light)
            .centerCrop()
            .into(binding.ivAuditorio)

        // Precargar los valores actuales de la reserva
        binding.etNombreEvento.setText(reserva.nombreEvento)
        binding.switchAudio.isChecked = reserva.sistemaAudio
        binding.switchMaestro.isChecked = reserva.maestroCeremonia
        binding.switchMesa.isChecked = reserva.mesaCentral
        binding.tvNumMicrofonos.text = reserva.numMicrofonos.toString()
        binding.btnFecha.text = reserva.fecha
        binding.btnHora.text = reserva.hora
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
                viewModel.seleccionarFecha(fecha)
            }
            picker.show(parentFragmentManager, "EDIT_DATE_PICKER")
        }

        binding.btnHora.setOnClickListener {
            val partes = binding.btnHora.text.toString().split(":")
            val horaInicial = partes.getOrNull(0)?.toIntOrNull() ?: 10
            val minutoInicial = partes.getOrNull(1)?.toIntOrNull() ?: 0
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(horaInicial)
                .setMinute(minutoInicial)
                .setTitleText(getString(R.string.selecciona_hora_reserva))
                .build()
            picker.addOnPositiveButtonClickListener {
                val hora = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
                binding.btnHora.text = hora
                viewModel.seleccionarHora(hora)
            }
            picker.show(parentFragmentManager, "EDIT_TIME_PICKER")
        }

        binding.btnGuardar.setOnClickListener {
            viewModel.guardarCambios(
                nombreEvento = binding.etNombreEvento.text.toString(),
                sistemaAudio = binding.switchAudio.isChecked,
                maestroCeremonia = binding.switchMaestro.isChecked,
                mesaCentral = binding.switchMesa.isChecked,
                numMicrofonos = binding.tvNumMicrofonos.text.toString().toInt()
            )
        }

        binding.btnEliminar.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.eliminar_reserva_titulo))
                .setMessage(
                    getString(
                        R.string.cancelar_reserva_mensaje,
                        reserva.auditorioNombre,
                        binding.btnFecha.text
                    )
                )
                .setNegativeButton(getString(R.string.accion_no), null)
                .setPositiveButton(getString(R.string.accion_si_eliminar)) { _, _ ->
                    viewModel.eliminarReserva()
                }
                .show()
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
                                binding.btnGuardar.isEnabled = false
                            }
                            is ResponseService.Success -> {
                                binding.tvDisponibilidad.isVisible = true
                                if (state.data) {
                                    binding.tvDisponibilidad.text =
                                        getString(R.string.fecha_disponible)
                                    binding.tvDisponibilidad.setTextColor(
                                        requireContext().getColor(R.color.estado_disponible)
                                    )
                                    binding.btnGuardar.isEnabled = true
                                } else {
                                    binding.tvDisponibilidad.text =
                                        getString(R.string.fecha_ocupada)
                                    binding.tvDisponibilidad.setTextColor(
                                        requireContext().getColor(R.color.estado_ocupado)
                                    )
                                    binding.btnGuardar.isEnabled = false
                                }
                            }
                            is ResponseService.Error -> {
                                binding.tvDisponibilidad.isVisible = true
                                binding.tvDisponibilidad.text = state.error
                                binding.tvDisponibilidad.setTextColor(
                                    requireContext().getColor(R.color.estado_ocupado)
                                )
                                binding.btnGuardar.isEnabled = false
                            }
                            null -> binding.tvDisponibilidad.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.guardarState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> binding.btnGuardar.isEnabled = false
                            is ResponseService.Success -> {
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.reserva_actualizada),
                                    Snackbar.LENGTH_LONG
                                ).show()
                                viewModel.consumeGuardarState()
                                findNavController().navigateUp()
                            }
                            is ResponseService.Error -> {
                                binding.btnGuardar.isEnabled = true
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                                viewModel.consumeGuardarState()
                            }
                            null -> Unit
                        }
                    }
                }

                launch {
                    viewModel.eliminarState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> binding.btnEliminar.isEnabled = false
                            is ResponseService.Success -> {
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.reserva_cancelada),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                viewModel.consumeEliminarState()
                                findNavController().navigateUp()
                            }
                            is ResponseService.Error -> {
                                binding.btnEliminar.isEnabled = true
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                                viewModel.consumeEliminarState()
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
