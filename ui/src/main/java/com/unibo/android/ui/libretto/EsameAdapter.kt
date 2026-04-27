package com.unibo.android.ui.libretto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unibo.android.domain.model.Esame
import com.unibo.android.ui.databinding.ItemEsameBinding

class EsameAdapter(
    private val onDeleteClick: (Esame) -> Unit
) : ListAdapter<Esame, EsameAdapter.EsameViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsameViewHolder {
        val binding = ItemEsameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EsameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EsameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EsameViewHolder(private val binding: ItemEsameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(esame: Esame) {
            binding.textNome.text = esame.nome
            binding.textVoto.text = if (esame.lode) "${esame.voto}L" else "${esame.voto}"
            binding.textCfu.text = "${esame.cfu} CFU"
            binding.textData.text = esame.dataEsame
            binding.buttonDelete.setOnClickListener { onDeleteClick(esame) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Esame>() {
        override fun areItemsTheSame(oldItem: Esame, newItem: Esame) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Esame, newItem: Esame) =
            oldItem == newItem
    }
}
