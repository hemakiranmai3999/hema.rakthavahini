package com.raktavahini.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "raktavahini.db"
        const val DATABASE_VERSION = 1
        const val TABLE_DONORS = "donors"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PHONE = "phone"
        const val COL_BLOOD_GROUP = "blood_group"
        const val COL_LOCATION = "location"
        const val COL_LATITUDE = "latitude"
        const val COL_LONGITUDE = "longitude"
        const val COL_LAST_DONATION = "last_donation_date"
        const val COL_IS_READY = "is_ready"
        const val TABLE_DONATIONS = "donation_log"
        const val COL_LOG_ID = "log_id"
        const val COL_DONOR_ID = "donor_id"
        const val COL_DONATION_DATE = "donation_date"
        const val COL_HOSPITAL = "hospital"
        const val COL_UNITS = "units"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createDonors = """
            CREATE TABLE $TABLE_DONORS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_PHONE TEXT NOT NULL,
                $COL_BLOOD_GROUP TEXT NOT NULL,
                $COL_LOCATION TEXT,
                $COL_LATITUDE REAL DEFAULT 0.0,
                $COL_LONGITUDE REAL DEFAULT 0.0,
                $COL_LAST_DONATION TEXT,
                $COL_IS_READY INTEGER DEFAULT 1
            )
        """.trimIndent()

        val createDonations = """
            CREATE TABLE $TABLE_DONATIONS (
                $COL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DONOR_ID INTEGER,
                $COL_DONATION_DATE TEXT NOT NULL,
                $COL_HOSPITAL TEXT,
                $COL_UNITS INTEGER DEFAULT 1,
                FOREIGN KEY($COL_DONOR_ID) REFERENCES $TABLE_DONORS($COL_ID)
            )
        """.trimIndent()

        db.execSQL(createDonors)
        db.execSQL(createDonations)
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DONORS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DONATIONS")
        onCreate(db)
    }

    fun insertDonor(donor: Donor): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, donor.name)
            put(COL_PHONE, donor.phone)
            put(COL_BLOOD_GROUP, donor.bloodGroup)
            put(COL_LOCATION, donor.location)
            put(COL_LATITUDE, donor.latitude)
            put(COL_LONGITUDE, donor.longitude)
            put(COL_LAST_DONATION, donor.lastDonationDate)
            put(COL_IS_READY, if (donor.isReady) 1 else 0)
        }
        return db.insert(TABLE_DONORS, null, values)
    }

    fun getEligibleDonors(bloodGroup: String): List<Donor> {
        val donors = mutableListOf<Donor>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DONORS, null,
            "$COL_BLOOD_GROUP = ?",
            arrayOf(bloodGroup),
            null, null, COL_NAME
        )
        while (cursor.moveToNext()) {
            val donor = cursorToDonor(cursor)
            if (isEligible(donor.lastDonationDate) && donor.isReady) {
                donors.add(donor)
            }
        }
        cursor.close()
        return donors
    }

    fun isEligible(lastDonationDate: String?): Boolean {
        if (lastDonationDate.isNullOrEmpty()) return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = sdf.parse(lastDonationDate) ?: return true
            val today = Calendar.getInstance().time
            val diffInDays = (today.time - lastDate.time) / (1000 * 60 * 60 * 24)
            diffInDays >= 90
        } catch (e: Exception) {
            true
        }
    }

    fun logDonation(donorId: Int, hospital: String, units: Int): Long {
        val db = writableDatabase
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val logValues = ContentValues().apply {
            put(COL_DONOR_ID, donorId)
            put(COL_DONATION_DATE, today)
            put(COL_HOSPITAL, hospital)
            put(COL_UNITS, units)
        }
        val logId = db.insert(TABLE_DONATIONS, null, logValues)
        val updateValues = ContentValues().apply {
            put(COL_LAST_DONATION, today)
        }
        db.update(TABLE_DONORS, updateValues, "$COL_ID = ?", arrayOf(donorId.toString()))
        return logId
    }

    fun updateReadyStatus(donorId: Int, isReady: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_READY, if (isReady) 1 else 0)
        }
        db.update(TABLE_DONORS, values, "$COL_ID = ?", arrayOf(donorId.toString()))
    }

    fun getDonationHistory(donorId: Int): List<DonationRecord> {
        val records = mutableListOf<DonationRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DONATIONS, null,
            "$COL_DONOR_ID = ?", arrayOf(donorId.toString()),
            null, null, "$COL_DONATION_DATE DESC"
        )
        while (cursor.moveToNext()) {
            records.add(
                DonationRecord(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_DATE)),
                    hospital = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOSPITAL)) ?: "",
                    units = cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNITS))
                )
            )
        }
        cursor.close()
        return records
    }

    fun getDonorById(id: Int): Donor? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DONORS, null, "$COL_ID = ?",
            arrayOf(id.toString()), null, null, null
        )
        val donor = if (cursor.moveToFirst()) cursorToDonor(cursor) else null
        cursor.close()
        return donor
    }

    private fun cursorToDonor(cursor: Cursor): Donor {
        return Donor(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
            bloodGroup = cursor.getString(cursor.getColumnIndexOrThrow(COL_BLOOD_GROUP)),
            location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)) ?: "",
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE)),
            lastDonationDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_DONATION)) ?: "",
            isReady = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_READY)) == 1
        )
    }

    private fun insertSampleData(db: SQLiteDatabase) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -100)
        val old = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 70)
        val recent = sdf.format(cal.time)

        val sampleDonors = listOf(
            arrayOf("Arjun Sharma", "9876543210", "O+", "Koramangala, Bengaluru", "12.9352", "77.6245", old, "1"),
            arrayOf("Priya Nair", "9123456789", "B+", "HSR Layout, Bengaluru", "12.9116", "77.6389", old, "1"),
            arrayOf("Ravi Kumar", "9988776655", "A+", "Indiranagar, Bengaluru", "12.9784", "77.6408", recent, "1"),
            arrayOf("Sneha Patel", "9765432109", "O-", "Whitefield, Bengaluru", "12.9698", "77.7500", "", "1"),
            arrayOf("Mohan Das", "9654321098", "AB+", "Jayanagar, Bengaluru", "12.9308", "77.5838", old, "1"),
            arrayOf("Ananya Roy", "9543210987", "B-", "Marathahalli, Bengaluru", "12.9591", "77.6974", old, "0")
        )

        sampleDonors.forEach { d ->
            val values = ContentValues().apply {
                put("name", d[0]); put("phone", d[1]); put("blood_group", d[2])
                put("location", d[3]); put("latitude", d[4].toDouble())
                put("longitude", d[5].toDouble()); put("last_donation_date", d[6])
                put("is_ready", d[7].toInt())
            }
            db.insert(TABLE_DONORS, null, values)
        }
    }
}