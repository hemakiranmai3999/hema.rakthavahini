package com.raktavahini.app

data class Donor(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val bloodGroup: String,
    val location: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastDonationDate: String,
    val isReady: Boolean = true
)

data class DonationRecord(
    val id: Int,
    val date: String,
    val hospital: String,
    val units: Int
)

