package com.example.personalbudgetingapp

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.personalbudgetingapp.databinding.FragmentCreateEntryBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreateEntryFragment : Fragment() {

    private var _binding: FragmentCreateEntryBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var photoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivPhoto.visibility = View.VISIBLE
            Glide.with(this).load(photoUri).into(binding.ivPhoto)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.appDao().getAllCategories()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter
            }
        }

        binding.btnAddPhoto.setOnClickListener {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.personalbudgetingapp.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        }

        binding.btnSaveEntry.setOnClickListener {
            val date = binding.etDate.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val categoryName = binding.spinnerCategory.selectedItem?.toString()
            val amount = binding.etAmount.text.toString().toDoubleOrNull()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.isEmpty() || description.isEmpty() || categoryName == null || amount == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val category = db.appDao().getAllCategories().find { it.name == categoryName }
                if (category != null) {
                    val entry = ExpenseEntry(
                        date = date,
                        description = description,
                        categoryId = category.id,
                        amount = amount,
                        photoUri = photoUri?.toString(),
                        userId = userId
                    )
                    db.appDao().insertExpenseEntry(entry)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Entry saved", Toast.LENGTH_SHORT).show()
                        clearFields()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Category not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun clearFields() {
        binding.etDate.text.clear()
        binding.etDescription.text.clear()
        binding.etAmount.text.clear()
        binding.ivPhoto.visibility = View.GONE
        photoUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
