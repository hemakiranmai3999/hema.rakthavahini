package com.raktavahini.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DonorAdapter(private val donors: List<Donor>) :
    RecyclerView.Adapter<DonorAdapter.DonorViewHolder>() {

    inner class DonorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvDonorName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvDonorLocation)
        val tvBadge: TextView = itemView.findViewById(R.id.tvBloodGroupBadge)
        val tvStatus: TextView = itemView.findViewById(R.id.tvEligibilityStatus)
        val btnCall: Button = itemView.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donor, parent, false)
        return DonorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donors[position]
        holder.tvName.text = donor.name
        holder.tvLocation.text = "📍 ${donor.location}"
        holder.tvBadge.text = donor.bloodGroup
        holder.tvStatus.text = "✅ Eligible to Donate"

        holder.btnCall.setOnClickListener { view ->
            val context = view.context
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:${donor.phone}")
                }
                context.startActivity(callIntent)
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${donor.phone}")
                }
                context.startActivity(dialIntent)
                Toast.makeText(context, "Calling ${donor.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = donors.size
}

