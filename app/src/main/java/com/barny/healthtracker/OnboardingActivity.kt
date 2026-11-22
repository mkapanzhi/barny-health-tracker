package com.barny.healthtracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barny.healthtracker.app.App
import com.barny.healthtracker.data.local.db.DatabaseProvider
import com.barny.healthtracker.data.local.db.DatabaseSeeder
import com.barny.healthtracker.data.local.db.entity.SpeciesType
import com.barny.healthtracker.data.preferences.SettingsDataStore
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File

class OnboardingActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var nameLayout: TextInputLayout
    private lateinit var group: RadioGroup
    private lateinit var error: TextView
    private lateinit var photo: ImageView
    private lateinit var placeholder: TextView
    private lateinit var clear: ImageView
    private lateinit var pick: View
    private lateinit var continueButton: Button
    private var photoPath: String? = null
    private var saving = false
    private val settings by lazy { SettingsDataStore(this) }

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val temporary = File(filesDir, "pet_photo_temp.jpg")
            val target = File(filesDir, "pet_photo.jpg")
            val result = runCatching {
                contentResolver.openInputStream(uri)?.use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Cannot open image")
                check(temporary.length() > 0)
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
                target.absolutePath
            }
            result.onSuccess { photoPath = it; renderPhoto() }
                .onFailure { Toast.makeText(this, getString(R.string.error_pet_photo_load), Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        nameInput = findViewById(R.id.pet_name_input)
        nameLayout = findViewById(R.id.pet_name_input_layout)
        group = findViewById(R.id.pet_species_group)
        error = findViewById(R.id.pet_species_error)
        photo = findViewById(R.id.pet_photo_image)
        placeholder = findViewById(R.id.pet_photo_placeholder)
        clear = findViewById(R.id.pet_photo_clear)
        pick = findViewById(R.id.select_photo_button)
        continueButton = findViewById(R.id.continue_button)
        photoPath = savedInstanceState?.getString("photo_path")
        savedInstanceState?.getString("species")?.let { value ->
            when (PetSpecies.fromStorage(value)) {
                PetSpecies.CAT -> group.check(R.id.pet_species_cat)
                PetSpecies.DOG -> group.check(R.id.pet_species_dog)
                null -> Unit
            }
        }
        renderPhoto()
        group.setOnCheckedChangeListener { _, _ -> error.visibility = View.GONE }
        pick.setOnClickListener { picker.launch("image/*") }
        clear.setOnClickListener {
            photoPath?.let { File(it).delete() }
            photoPath = null
            renderPhoto()
        }
        continueButton.setOnClickListener { saveProfile() }
    }

    private fun selectedSpecies(): PetSpecies? = when (group.checkedRadioButtonId) {
        R.id.pet_species_cat -> PetSpecies.CAT
        R.id.pet_species_dog -> PetSpecies.DOG
        else -> null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("photo_path", photoPath)
        outState.putString("species", selectedSpecies()?.storageValue)
        super.onSaveInstanceState(outState)
    }

    private fun renderPhoto() {
        val exists = photoPath?.let { File(it).isFile } == true
        photo.visibility = if (exists) View.VISIBLE else View.GONE
        placeholder.visibility = if (exists) View.GONE else View.VISIBLE
        clear.visibility = if (exists) View.VISIBLE else View.GONE
        pick.visibility = if (exists) View.GONE else View.VISIBLE
        if (exists) photo.setImageURI(Uri.fromFile(File(photoPath!!))) else photo.setImageDrawable(null)
    }

    private fun saveProfile() {
        if (saving) return
        val name = nameInput.text.toString().trim()
        val species = selectedSpecies()
        nameLayout.error = if (name.isBlank()) getString(R.string.error_pet_name_required) else null
        error.visibility = if (species == null) View.VISIBLE else View.GONE
        if (name.isBlank() || species == null) {
            if (name.isBlank()) nameInput.requestFocus()
            if (species == null) error.announceForAccessibility(getString(R.string.pet_species_required))
            return
        }
        saving = true
        continueButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val db = DatabaseProvider.get(this@OnboardingActivity)
                DatabaseSeeder(db).seed()
                val type = if (species == PetSpecies.CAT) SpeciesType.CAT else SpeciesType.DOG
                val speciesEntity = checkNotNull(db.speciesDao().getByCode(type))
                val container = (application as App).appContainer
                val active = container.petRepository.getActivePetOrNull()
                if (active == null) {
                    container.petRepository.createPet(name = name, speciesId = speciesEntity.id)
                } else {
                    check(active.speciesId == speciesEntity.id) { "Existing profile belongs to another species" }
                }
                settings.setPetName(name)
                settings.setPetPhotoUri(photoPath.orEmpty())
                val persisted = getSharedPreferences("onboarding_prefs", MODE_PRIVATE).edit()
                    .putString("pet_species", species.storageValue).commit()
                check(persisted) { "Cannot save species" }
                HealthParams.selectSpecies(species)
                settings.setOnboardingCompleted(true)
                check(getSharedPreferences("onboarding_prefs", MODE_PRIVATE).edit().putBoolean("completed", true).commit())
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                android.util.Log.e("Onboarding", "Failed to save profile", exception)
                Toast.makeText(this@OnboardingActivity, getString(R.string.error_pet_profile_save), Toast.LENGTH_LONG).show()
            } finally {
                saving = false
                continueButton.isEnabled = true
            }
        }
    }
}
