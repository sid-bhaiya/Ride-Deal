package com.example.database

import androidx.room.*
import com.example.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- USER QUERIES ---
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    // --- BIKE QUERIES ---
    @Query("SELECT * FROM bikes ORDER BY brand ASC")
    fun getAllBikesFlow(): Flow<List<Bike>>

    @Query("SELECT * FROM bikes ORDER BY brand ASC")
    suspend fun getAllBikesSync(): List<Bike>

    @Query("SELECT * FROM bikes WHERE id = :id LIMIT 1")
    fun getBikeById(id: Int): Flow<Bike?>

    @Query("SELECT * FROM bikes WHERE id = :id LIMIT 1")
    suspend fun getBikeByIdSync(id: Int): Bike?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBike(bike: Bike): Long

    @Update
    suspend fun updateBike(bike: Bike)

    @Delete
    suspend fun deleteBike(bike: Bike)

    // --- BOOKING QUERIES ---
    @Query("SELECT * FROM bookings ORDER BY id DESC")
    fun getAllBookingsFlow(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY id DESC")
    fun getBookingsForUserFlow(userId: Int): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingByIdFlow(id: Int): Flow<Booking?>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingByIdSync(id: Int): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    // --- INVOICE QUERIES ---
    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    fun getInvoiceForBooking(bookingId: Int): Flow<Invoice?>

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getInvoiceForBookingSync(bookingId: Int): Invoice?

    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    // --- PAYMENT QUERIES ---
    @Query("SELECT * FROM payments WHERE bookingId = :bookingId ORDER BY timestamp DESC")
    fun getPaymentsForBooking(bookingId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    // --- AGREEMENT QUERIES ---
    @Query("SELECT * FROM agreements WHERE bookingId = :bookingId LIMIT 1")
    fun getAgreementForBooking(bookingId: Int): Flow<Agreement?>

    @Query("SELECT * FROM agreements WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getAgreementForBookingSync(bookingId: Int): Agreement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgreement(agreement: Agreement): Long

    // --- NOTIFICATION QUERIES ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: Int)
}
