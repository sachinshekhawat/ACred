package com.example.accon

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.accon.databinding.ActivityProfileBinding
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "ProfilePrefs"
    private val KEY_USER_NAME = "user_name"

    // Activity result launchers for camera and gallery
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                binding.profileImage.setImageBitmap(it)
                // Here you can save the image to local storage or upload to server
                showToast("📸 Profile picture updated successfully!")
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                binding.profileImage.setImageURI(it)
                // Here you can save the image to local storage or upload to server
                showToast("🖼️ Profile picture updated from gallery!")
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showToast("📷 Camera permission is required to take photos!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Handle system insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        // Load saved user name from SharedPreferences
        val savedName = sharedPreferences.getString(KEY_USER_NAME, "Andaz Kumar")
        binding.textViewUserName.text = savedName
    }

    private fun setupClickListeners() {
        // Profile image click - Show dialog to change profile picture
        binding.profileImage.setOnClickListener {
            showProfileImageDialog()
        }

        // Edit profile click - Show edit dialog
        binding.imageViewEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // CRED Garage arrow click
        binding.imageViewGarageArrow.setOnClickListener {
            showToast("🚗 Garage Dashboard in progress! Rev up your patience!")
        }

        // Credit score arrow click
        binding.imageViewCreditArrow.setOnClickListener {
            showToast("📊 Score fetching API implementation will be scheduled soon! Keep your credit game strong!")
        }

        // Lifetime cashback arrow click
        binding.imageViewCashbackArrow.setOnClickListener {
            showToast("💰 Cashback history feature coming soon! Every penny counts!")
        }

        // Bank balance arrow click
        binding.imageViewBankBalanceArrow.setOnClickListener {
            showToast("🏦 Bank integration under development! Your money matters are safe with us!")
        }

        // Cashback balance next click
        binding.imageViewCashbackBalanceNext.setOnClickListener {
            showToast("💸 Cashback redemption portal launching soon! Get ready to treat yourself!")
        }

        // Coins next click
        binding.imageViewCoinsNext.setOnClickListener {
            showToast("🪙 Coin marketplace opening soon! Your virtual treasure awaits!")
        }

        // Refer next click
        binding.imageViewReferNext.setOnClickListener {
            showToast("🎁 Referral program enhancement in progress! Spread the love, earn rewards!")
        }

        // All transactions next click
        binding.imageViewTransactionsNext.setOnClickListener {
            showToast("📋 Transaction history upgrade coming soon! Track every rupee like a pro!")
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextName)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Set current name in the edit text
        editTextName.setText(binding.textViewUserName.text.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Make dialog background transparent to show our custom background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSave.setOnClickListener {
            val newName = editTextName.text.toString().trim()
            if (newName.isNotEmpty()) {
                // Save name to SharedPreferences
                sharedPreferences.edit()
                    .putString(KEY_USER_NAME, newName)
                    .apply()

                // Update UI
                binding.textViewUserName.text = newName

                dialog.dismiss()
                showToast("✅ Profile updated successfully! Looking good, $newName!")
            } else {
                showToast("❗ Please enter a valid name!")
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showProfileImageDialog() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose from Gallery", "❌ Cancel")

        AlertDialog.Builder(this)
            .setTitle("🎭 Want to change your profile image?")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                // Show rationale and request permission
                AlertDialog.Builder(this)
                    .setTitle("Camera Permission Required")
                    .setMessage("This app needs camera permission to take photos for your profile.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(cameraIntent)
        } else {
            showToast("📷 Camera app not found on your device!")
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}