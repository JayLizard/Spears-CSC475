package com.example.photogalleryapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photogalleryapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if we already have permission, if not, request it
        checkAndRequestPermission()
    }

    override fun onResume() {
        super.onResume()
        // Recheck the permission status when the activity resumes
        checkAndRequestPermission()
    }

    private fun checkAndRequestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission granted, load the images
                        loadImages()
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.READ_MEDIA_IMAGES
                    ) -> {
                        // Show rationale and request permission
                        showPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
                    }

                    else -> {
                        // Directly request the permission
                        requestStoragePermission(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }

            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission granted, load the images
                        loadImages()
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        // Show rationale and request permission
                        showPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }

                    else -> {
                        // Directly request the permission
                        requestStoragePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun requestStoragePermission(permission: String) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            when {
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted, load the images
                    loadImages()
                }
                // Check if "Don't Ask Again" was selected
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Manifest.permission.READ_MEDIA_IMAGES
                    else
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    // If permission is permanently denied, navigate to app settings
                    navigateToAppSettings()
                }
                else -> {
                    // Permission denied, show a message
                    Toast.makeText(
                        this,
                        "Permission denied. Please grant permission to access images.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun navigateToAppSettings() {
        Toast.makeText(
            this,
            "Permission denied permanently. Please enable it in settings.",
            Toast.LENGTH_LONG
        ).show()

        // Navigate the user to the app's settings page
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showPermissionRationale(permission: String) {
        Toast.makeText(
            this,
            "We need access to your photos to display them in the gallery.",
            Toast.LENGTH_LONG
        ).show()

        // After showing rationale, request the permission
        requestStoragePermission(permission)
    }

    // Function to load images from the device
    private fun loadImages() {
        val imageUris = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val imagePath = cursor.getString(columnIndex)
                imageUris.add(imagePath)
            }
        }

        if (imageUris.isNotEmpty()) {
            // Set up the adapter and RecyclerView
            val adapter = GalleryAdapter(imageUris)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columns
        } else {
            Toast.makeText(this, "No images found!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }
}
