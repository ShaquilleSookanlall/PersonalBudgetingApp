package com.example.personalbudgetingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.personalbudgetingapp.databinding.ItemCategoryNameBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryListAdapter(
    private var categories: List<Category>,
    private val db: AppDatabase,
    private val onCategoryDeleted: () -> Unit
) : RecyclerView.Adapter<CategoryListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryNameBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.tvCategoryName.text = category.name

        holder.binding.ivOptionsMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.category_menu, popup.menu)

            // Optional: Force custom background (requires AppCompat theme override for popupMenuStyle)
            try {
                val popupField = PopupMenu::class.java.getDeclaredField("mPopup")
                popupField.isAccessible = true
                val menu = popupField.get(popup)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)
            } catch (_: Exception) { }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_delete -> {
                        AlertDialog.Builder(view.context)
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete '${category.name}'?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.appDao().deleteCategoryById(category.id)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(view.context, "Category deleted", Toast.LENGTH_SHORT).show()
                                        onCategoryDeleted()
                                    }
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = categories.size
}
