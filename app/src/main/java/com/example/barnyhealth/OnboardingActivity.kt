package com.example.barnyhealth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import android.view.View
import android.widget.Button
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo

class OnboardingActivity : AppCompatActivity() {

    private lateinit var petNameInput: EditText
    private lateinit var petNameInputLayout: TextInputLayout
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
        if (uri == null) {
            return@registerForActivityResult
        }

        val filePath = copyImageToInternalStorage(uri)

        if (filePath == null) {
            Toast.makeText(
                this,
                "Не удалось загрузить фото",
                Toast.LENGTH_SHORT
            ).show()
            return@registerForActivityResult
        }

        photoUri = filePath

        petPhotoImage.setImageURI(Uri.fromFile(File(filePath)))
        petPhotoImage.visibility = View.VISIBLE
        petPhotoPlaceholder.visibility = View.GONE
        petPhotoClear.visibility = View.VISIBLE
        selectPhotoButton.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        petNameInput = findViewById(R.id.pet_name_input)
        petNameInputLayout = findViewById(R.id.pet_name_input_layout)
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
        petNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                petNameInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

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

    private fun copyImageToInternalStorage(uri: Uri): String? {
        val tempFile = File(filesDir, "pet_photo_temp.jpg")
        val targetFile = File(filesDir, "pet_photo.jpg")

        return runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            if (!tempFile.exists() || tempFile.length() == 0L) {
                tempFile.delete()
                return null
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }

            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            targetFile.absolutePath
        }.getOrNull()
    }

    private fun saveSettingsAndContinue() {
        val petName = petNameInput.text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (petName.isBlank()) {
            petNameInputLayout.error = getString(
                R.string.error_pet_name_required
            )
            petNameInput.requestFocus()
            return
        }

        petNameInputLayout.error = null

        lifecycleScope.launch {
            runCatching {
                settingsDataStore.setPetName(petName)

                if (photoUri.isNullOrBlank()) {
                    settingsDataStore.setPetPhotoUri("")
                } else {
                    settingsDataStore.setPetPhotoUri(photoUri.orEmpty())
                }

                settingsDataStore.setOnboardingCompleted(true)
            }.onSuccess {
                val prefs = getSharedPreferences(
                    "onboarding_prefs",
                    MODE_PRIVATE
                )

                prefs.edit()
                    .putBoolean("completed", true)
                    .apply()

                startActivity(
                    Intent(
                        this@OnboardingActivity,
                        MainActivity::class.java
                    )
                )

                finish()
            }.onFailure { throwable ->
                Toast.makeText(
                    this@OnboardingActivity,
                    "Не удалось сохранить профиль",
                    Toast.LENGTH_LONG
                ).show()

                android.util.Log.e(
                    "Onboarding",
                    "Failed to save profile",
                    throwable
                )
            }
        }
    }
}