package com.example.appsira.home.auditorios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appsira.R
import com.example.appsira.core.model.Auditorio
import com.example.appsira.databinding.ItemAuditorioBinding

class AuditoriosAdapter(
    private val onClick: (Auditorio) -> Unit
) : ListAdapter<Auditorio, AuditoriosAdapter.AuditorioViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Auditorio>() {
        override fun areItemsTheSame(oldItem: Auditorio, newItem: Auditorio) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Auditorio, newItem: Auditorio) =
            oldItem == newItem
    }

    inner class AuditorioViewHolder(
        private val binding: ItemAuditorioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(auditorio: Auditorio) {
            binding.tvNombre.text = auditorio.nombre
            binding.tvCapacidad.text = binding.root.context.getString(
                R.string.capacidad_personas, auditorio.capacidad
            )
            Glide.with(binding.ivAuditorio)
                .load(auditorio.imagen)
                .placeholder(R.color.brand_primary_light)
                .centerCrop()
                .into(binding.ivAuditorio)

            binding.root.setOnClickListener { onClick(auditorio) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditorioViewHolder {
        val binding = ItemAuditorioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AuditorioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuditorioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
