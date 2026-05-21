package com.raktavahini.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.raktavahini.app.databinding.ActivityDonorProfileBinding

class DonorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonorProfileBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        supportActionBar?.title = "Donor Registration"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bloodGroups = resources.getStringArray(R.array.blood_groups)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBloodGroup.adapter = adapter

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val bloodGroup = binding.spinnerBloodGroup.selectedItem.toString()
        val location = binding.etLocation.text.toString().trim()
        val lastDonation = binding.etLastDonation.text.toString().trim()
        val isReady = binding.switchReady.isChecked

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }
        if (phone.isEmpty() || phone.length < 10) {
            binding.etPhone.error = "Valid phone number required"
            return
        }

        val donor = Donor(
            name = name,
            phone = phone,
            bloodGroup = bloodGroup,
            location = location,
            lastDonationDate = lastDonation,
            isReady = isReady
        )

        val id = dbHelper.insertDonor(donor)
        if (id > 0) {
            Toast.makeText(this, "✅ Registered! Thank you, $name", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "❌ Registration failed. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

