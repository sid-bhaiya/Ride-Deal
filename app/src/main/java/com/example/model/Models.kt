package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String = "Customer", // Customer, Admin
    val nidNo: String = "",
    val drivingLicenseNo: String = "",
    val nidPath: String? = null,
    val licensePath: String? = null,
    val cardSaved: Boolean = false,
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "bikes")
data class Bike(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val model: String,
    val brand: String,
    val plateNumber: String,
    val engineNumber: String,
    val chassisNumber: String,
    val cc: Int,
    val rentalPrice: Double, // Daily price
    val deposit: Double,
    val status: String, // "Available", "Rented", "Maintenance", "Reserved"
    val photoRes: String, // String representation of drawable or image URL
    val specs: String, // e.g. "45 km/L | Disk Brake | Mono suspension"
    val terms: String = "1. Helmets must be worn. 2. Carry valid license. 3. Return fuel with same level."
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val bikeId: Int,
    val startDate: Long,
    val endDate: Long,
    val durationDays: Int,
    val deliveryType: String, // "Pickup" or "Delivery"
    val couponCode: String? = null,
    val status: String, // "Pending", "Approved", "Active", "Returned", "Cancelled", "Due Payment"
    
    // Accessories issued
    val hasHelmet: Boolean = true,
    val hasLock: Boolean = true,
    val hasSpareKey: Boolean = false,
    val hasRegCopy: Boolean = true,
    val fuelStatus: String = "Full" // "Full", "Half", "Empty"
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: Int,
    val invoiceNumber: String, // e.g., "RD-INV-1002"
    val totalRent: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val depositAmount: Double,
    val discountAmount: Double,
    val paymentDeadline: Long,
    val terms: String = "Please pay before pickup. Rent dues subject to late fee of 500 BDT per day."
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: Int,
    val invoiceNumber: String,
    val amount: Double,
    val timestamp: Long,
    val method: String, // "BKash", "Nagad", "Card", "Bank Transfer", "Cash"
    val transactionId: String
)

@Entity(tableName = "agreements")
data class Agreement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: Int,
    val customerSignatureBase64: String?, // Signature points or image drawn
    val agreementDate: Long,
    val termsVersion: String = "v1.0"
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
