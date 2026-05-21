package com.raktavahini.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raktavahini.app.databinding.ActivityDonationLogBinding

class DonationLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationLogBinding
    private lateinit var dbHelper: DatabaseHelper
    private var donorId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        donorId = intent.getIntExtra("donor_id", 1)

        supportActionBar?.title = "Donation Log"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadDonorInfo()
        loadDonationHistory()

        binding.btnLogDonation.setOnClickListener {
            showLogDonationDialog()
        }
    }

    private fun loadDonorInfo() {
        val donor = dbHelper.getDonorById(donorId)
        donor?.let {
            binding.tvDonorInfo.text = "${it.name} • ${it.bloodGroup} • ${it.location}"
        }
    }

    private fun loadDonationHistory() {
        val history = dbHelper.getDonationHistory(donorId)
        if (history.isEmpty()) {
            binding.rvDonationHistory.visibility = View.GONE
            binding.tvNoDonations.visibility = View.VISIBLE
        } else {
            binding.rvDonationHistory.visibility = View.VISIBLE
            binding.tvNoDonations.visibility = View.GONE
            binding.rvDonationHistory.layoutManager = LinearLayoutManager(this)
            binding.rvDonationHistory.adapter = HistoryAdapter(history)
        }
    }

    private fun showLogDonationDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }
        val etHospital = EditText(this).apply {
            hint = "Hospital / Blood Bank name"
        }
        layout.addView(etHospital)

        AlertDialog.Builder(this)
            .setTitle("Log New Donation 🩸")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val hospital = etHospital.text.toString().ifEmpty { "Unknown Hospital" }
                val logId = dbHelper.logDonation(donorId, hospital, 1)
                if (logId > 0) {
                    val donor = dbHelper.getDonorById(donorId)
                    donor?.let {
                        NotificationHelper.sendThankYouNotification(this, it.name)
                    }
                    Toast.makeText(this, "✅ Donation logged! Thank you 🩸", Toast.LENGTH_LONG).show()
                    loadDonationHistory()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    inner class HistoryAdapter(private val records: List<DonationRecord>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(android.R.id.text1)
            val tvHospital: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = records[position]
            holder.tvDate.text = "🩸 ${record.date}"
            holder.tvHospital.text = "🏥 ${record.hospital}"
        }

        override fun getItemCount() = records.size
    }
}