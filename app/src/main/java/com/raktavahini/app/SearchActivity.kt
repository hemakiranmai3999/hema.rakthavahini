package com.raktavahini.app

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.raktavahini.app.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        supportActionBar?.title = "Find Donors"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bloodGroups = resources.getStringArray(R.array.blood_groups)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSearchGroup.adapter = adapter

        binding.rvDonors.layoutManager = LinearLayoutManager(this)

        binding.btnSearchNow.setOnClickListener {
            val selectedGroup = binding.spinnerSearchGroup.selectedItem.toString()
            searchDonors(selectedGroup)
        }
    }

    private fun searchDonors(bloodGroup: String) {
        val eligibleDonors = dbHelper.getEligibleDonors(bloodGroup)
        if (eligibleDonors.isEmpty()) {
            binding.rvDonors.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvResultCount.text = "No eligible $bloodGroup donors found"
        } else {
            binding.rvDonors.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
            binding.tvResultCount.text = "Found ${eligibleDonors.size} eligible $bloodGroup donor(s)"
            binding.rvDonors.adapter = DonorAdapter(eligibleDonors)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

