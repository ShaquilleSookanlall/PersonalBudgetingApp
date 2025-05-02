package com.example.personalbudgetingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personalbudgetingapp.databinding.ItemEntryBinding

class EntryAdapter(
    private val entries: List<ExpenseEntry>,
    private val categories: List<Category>,
    private val onItemClick: (ExpenseEntry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    inner class EntryViewHolder(val binding: ItemEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: ExpenseEntry) {
            val category = categories.find { it.id == entry.categoryId }
            with(binding) {
                tvEntryDescription.text = entry.description
                tvEntryDetails.text = "Category: ${category?.name}, Amount: R${entry.amount}, Date: ${entry.date}"
                if (!entry.photoUri.isNullOrEmpty()) {
                    ivEntryPhoto.visibility = View.VISIBLE
                    Glide.with(root.context).load(entry.photoUri).into(ivEntryPhoto)
                } else {
                    ivEntryPhoto.visibility = View.GONE
                }

                root.setOnClickListener {
                    onItemClick(entry)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size
}
