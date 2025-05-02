package com.example.personalbudgetingapp

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.personalbudgetingapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var isFirebaseInitialized = false

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Permissions callback triggered: $permissions")
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d(TAG, "All permissions granted")
            initializeFirebase()
        } else {
            Log.d(TAG, "Some permissions denied")
            Toast.makeText(this, "Permissions required for some features (e.g., photo capture)", Toast.LENGTH_LONG).show()
            // Proceed with Firebase initialization even if permissions are denied
            initializeFirebase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Always set up click listeners, regardless of permissions or Firebase status
        setupClickListeners()

        // Request necessary permissions
        requestPermissions()
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions called")
        val permissionsToRequest = mutableListOf<String>()
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $permissionsToRequest")
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d(TAG, "All permissions already granted")
            initializeFirebase()
        }
    }

    private fun initializeFirebase() {
        Log.d(TAG, "initializeFirebase called")
        try {
            auth = FirebaseAuth.getInstance()
            isFirebaseInitialized = true
            Log.d(TAG, "Firebase initialized successfully")
            checkLoginStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
            Toast.makeText(this, "Firebase initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
            isFirebaseInitialized = false
        }
    }

    private fun checkLoginStatus() {
        Log.d(TAG, "checkLoginStatus called")
        if (isFirebaseInitialized && auth.currentUser != null) {
            Log.d(TAG, "User already logged in, navigating to DashboardActivity")
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            Log.d(TAG, "No user logged in or Firebase not initialized")
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")
        binding.btnLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            animateButton(it)
            if (!isFirebaseInitialized) {
                Toast.makeText(this, "Firebase not initialized, please try again later", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Log.d(TAG, "Email or password empty")
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting to sign in with email: $email")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Login successful")
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Log.w(TAG, "Login failed: ${task.exception?.message}")
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnRegister.setOnClickListener {
            Log.d(TAG, "Register button clicked")
            animateButton(it)
            if (!isFirebaseInitialized) {
                Toast.makeText(this, "Firebase not initialized, please try again later", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Log.d(TAG, "Email or password empty")
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting to register with email: $email")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Registration successful")
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Log.w(TAG, "Registration failed: ${task.exception?.message}")
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun animateButton(view: View) {
        Log.d(TAG, "animateButton called")
        // Scale down on press
        ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f).apply {
            duration = 100
            start()
        }
        ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f).apply {
            duration = 100
            start()
        }
        // Scale back up after release
        ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f).apply {
            duration = 100
            startDelay = 100
            start()
        }
        ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f).apply {
            duration = 100
            startDelay = 100
            start()
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}