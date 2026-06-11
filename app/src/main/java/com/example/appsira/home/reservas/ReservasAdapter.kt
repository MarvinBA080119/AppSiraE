package com.example.appsira.home.reservas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appsira.R
import com.example.appsira.core.model.Reserva
import com.example.appsira.databinding.ItemReservaBinding

class ReservasAdapter(
    private val onCancelar: (Reserva) -> Unit
) : ListAdapter<Reserva, ReservasAdapter.ReservaViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Reserva>() {
        override fun areItemsTheSame(oldItem: Reserva, newItem: Reserva) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reserva, newItem: Reserva) =
            oldItem == newItem
    }

    inner class ReservaViewHolder(
        private val binding: ItemReservaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: Reserva) {
            val context = binding.root.context
            binding.tvNombre.text = reserva.auditorioNombre
            binding.tvFechaHora.text = context.getString(
                R.string.reserva_fecha_hora, reserva.fecha, reserva.hora
            )

            val servicios = mutableListOf<String>()
            if (reserva.sistemaAudio) servicios.add(context.getString(R.string.servicio_audio))
            if (reserva.maestroCeremonia) servicios.add(context.getString(R.string.servicio_maestro))
            if (reserva.mesaCentral) servicios.add(context.getString(R.string.servicio_mesa))
            if (reserva.numMicrofonos > 0) {
                servicios.add(context.getString(R.string.servicio_microfonos, reserva.numMicrofonos))
            }
            binding.tvServicios.isVisible = servicios.isNotEmpty()
            binding.tvServicios.text = servicios.joinToString(" · ")

            Glide.with(binding.ivAuditorio)
                .load(reserva.auditorioImagen)
                .placeholder(R.color.brand_primary_light)
                .centerCrop()
                .into(binding.ivAuditorio)

            binding.btnCancelar.setOnClickListener { onCancelar(reserva) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val binding = ItemReservaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReservaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
