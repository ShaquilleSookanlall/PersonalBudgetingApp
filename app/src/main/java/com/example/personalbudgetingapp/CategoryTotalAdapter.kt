package com.example.personalbudgetingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personalbudgetingapp.databinding.ItemCategoryTotalBinding


class CategoryTotalAdapter(
    private val totals: List<Pair<Category, Double>>
) : RecyclerView.Adapter<CategoryTotalAdapter.CategoryTotalViewHolder>() {

    class CategoryTotalViewHolder(val binding: ItemCategoryTotalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryTotalViewHolder {
        val binding = ItemCategoryTotalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryTotalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryTotalViewHolder, position: Int) {
        val (category, total) = totals[position]
        with(holder.binding) {
            tvCategoryName.text = category.name
            tvTotalAmount.text = total.toString()
        }
    }

    override fun getItemCount(): Int = totals.size
}