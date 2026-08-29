package com.example.barnyhealth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo

class OnboardingActivity : AppCompatActivity() {

    private lateinit var petNameInput: EditText
    private lateinit var petPhotoImage: ImageView
    private lateinit var petPhotoPlaceholder: TextView
    private lateinit var continueButton: Button
    private lateinit var petPhotoClear: ImageView
    private lateinit var selectPhotoButton: FloatingActionButton

    private var photoUri: String? = null  // теперь это путь к файлу
    private val settingsDataStore by lazy { SettingsDataStore(this) }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Копируем файл во внутреннее хранилище
            val filePath = copyImageToInternalStorage(it)
            photoUri = filePath

            // Показываем изображение
            petPhotoImage.setImageURI(Uri.fromFile(File(filePath)))
            petPhotoImage.visibility = View.VISIBLE
            petPhotoPlaceholder.visibility = View.GONE
            petPhotoClear.visibility = View.VISIBLE
            selectPhotoButton.visibility = View.GONE
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        petNameInput = findViewById(R.id.pet_name_input)
        petPhotoImage = findViewById(R.id.pet_photo_image)
        petPhotoPlaceholder = findViewById(R.id.pet_photo_placeholder)
        continueButton = findViewById(R.id.continue_button)
        petPhotoClear = findViewById(R.id.pet_photo_clear)
        selectPhotoButton = findViewById(R.id.select_photo_button)

        selectPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        continueButton.setOnClickListener {
            saveSettingsAndContinue()
        }

        petPhotoClear.setOnClickListener {
            // Удалить файл из внутреннего хранилища
            photoUri?.let { filePath ->
                File(filePath).delete()
            }

            photoUri = null
            petPhotoImage.setImageDrawable(null)
            petPhotoImage.visibility = View.GONE
            petPhotoPlaceholder.visibility = View.VISIBLE
            petPhotoClear.visibility = View.GONE
            selectPhotoButton.visibility = View.VISIBLE
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): String {
        val fileName = "pet_photo.jpg"
        val file = File(filesDir, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    private fun checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveSettingsAndContinue() {
        val petName = petNameInput.text.toString().trim()

        if (petName.isEmpty()) {
            petNameInput.error = getString(R.string.error_pet_name_required)
            return
        }

        lifecycleScope.launch {
            settingsDataStore.setPetName(petName)
            photoUri?.let { filePath ->
                settingsDataStore.setPetPhotoUri(filePath)
            }
            settingsDataStore.setOnboardingCompleted(true)

            val prefs = getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("completed", true).apply()

            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
            finish()
            android.util.Log.d("Onboarding", "Saving completed = true")
        }
    }
}