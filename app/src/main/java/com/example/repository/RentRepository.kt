package com.example.repository

import android.content.Context
import com.example.database.AppDao
import com.example.database.RentDatabase
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RentRepository(private val appDao: AppDao) {

    companion object {
        @Volatile
        private var INSTANCE: RentRepository? = null

        fun getInstance(context: Context): RentRepository {
            return INSTANCE ?: synchronized(this) {
                val db = RentDatabase.getDatabase(context)
                val repo = RentRepository(db.appDao)
                INSTANCE = repo
                repo
            }
        }
    }

    // --- Users ---
    val allUsers: Flow<List<User>> = appDao.getAllUsers()
    val loggedInUser: Flow<User?> = appDao.getLoggedInUserFlow()

    suspend fun getLoggedInUserSync(): User? = withContext(Dispatchers.IO) {
        appDao.getLoggedInUserSync()
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        appDao.getUserByEmail(email)
    }

    suspend fun getUserByIdSync(id: Int): User? = withContext(Dispatchers.IO) {
        appDao.getUserByIdSync(id)
    }

    suspend fun signUpUser(user: User): Long = withContext(Dispatchers.IO) {
        appDao.insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        appDao.updateUser(user)
    }

    suspend fun logoutAllUsers() = withContext(Dispatchers.IO) {
        val users = appDao.getLoggedInUserSync()
        if (users != null) {
            appDao.updateUser(users.copy(isLoggedIn = false))
        }
    }

    // --- Bikes ---
    val allBikes: Flow<List<Bike>> = appDao.getAllBikesFlow()

    suspend fun getAllBikesSync(): List<Bike> = withContext(Dispatchers.IO) {
        appDao.getAllBikesSync()
    }

    fun getBikeById(id: Int): Flow<Bike?> = appDao.getBikeById(id)

    suspend fun insertBike(bike: Bike): Long = withContext(Dispatchers.IO) {
        appDao.insertBike(bike)
    }

    suspend fun updateBike(bike: Bike) = withContext(Dispatchers.IO) {
        appDao.updateBike(bike)
    }

    suspend fun deleteBike(bike: Bike) = withContext(Dispatchers.IO) {
        appDao.deleteBike(bike)
    }

    // --- Bookings ---
    val allBookings: Flow<List<Booking>> = appDao.getAllBookingsFlow()

    fun getBookingsForUser(userId: Int): Flow<List<Booking>> = appDao.getBookingsForUserFlow(userId)

    fun getBookingById(bookingId: Int): Flow<Booking?> = appDao.getBookingByIdFlow(bookingId)

    suspend fun getBookingByIdSync(bookingId: Int): Booking? = withContext(Dispatchers.IO) {
        appDao.getBookingByIdSync(bookingId)
    }

    suspend fun createBooking(booking: Booking): Long = withContext(Dispatchers.IO) {
        val bookingId = appDao.insertBooking(booking)
        
        // Update bike status to Reserved/Rented if booking is immediately approved
        val bike = appDao.getBikeByIdSync(booking.bikeId)
        if (bike != null) {
            val updatedStatus = when (booking.status) {
                "Active", "Approved" -> "Reserved"
                else -> bike.status
            }
            appDao.updateBike(bike.copy(status = updatedStatus))
        }
        
        bookingId
    }

    suspend fun updateBookingStatus(bookingId: Int, newStatus: String) = withContext(Dispatchers.IO) {
        val booking = appDao.getBookingByIdSync(bookingId)
        if (booking != null) {
            appDao.updateBooking(booking.copy(status = newStatus))
            
            // Sync bike inventory status accordingly
            val bike = appDao.getBikeByIdSync(booking.bikeId)
            if (bike != null) {
                val bikeStatus = when (newStatus) {
                    "Active" -> "Rented"
                    "Returned" -> "Available"
                    "Cancelled" -> "Available"
                    "Approved" -> "Reserved"
                    else -> bike.status
                }
                appDao.updateBike(bike.copy(status = bikeStatus))
            }

            // Create notification
            val notification = Notification(
                userId = booking.userId,
                title = "Booking Update: $newStatus",
                message = "Your booking for Bike #${booking.bikeId} has been updated to '$newStatus'.",
                timestamp = System.currentTimeMillis()
            )
            appDao.insertNotification(notification)
        }
    }

    suspend fun updateBooking(booking: Booking) = withContext(Dispatchers.IO) {
        appDao.updateBooking(booking)
    }

    // --- Invoices ---
    val allInvoices: Flow<List<Invoice>> = appDao.getAllInvoices()

    fun getInvoiceForBooking(bookingId: Int): Flow<Invoice?> = appDao.getInvoiceForBooking(bookingId)

    suspend fun getInvoiceForBookingSync(bookingId: Int): Invoice? = withContext(Dispatchers.IO) {
        appDao.getInvoiceForBookingSync(bookingId)
    }

    suspend fun insertInvoice(invoice: Invoice): Long = withContext(Dispatchers.IO) {
        appDao.insertInvoice(invoice)
    }

    suspend fun updateInvoice(invoice: Invoice) = withContext(Dispatchers.IO) {
        appDao.updateInvoice(invoice)
    }

    // --- Payments ---
    val allPayments: Flow<List<Payment>> = appDao.getAllPayments()

    fun getPaymentsForBooking(bookingId: Int): Flow<List<Payment>> = appDao.getPaymentsForBooking(bookingId)

    suspend fun processPayment(payment: Payment) = withContext(Dispatchers.IO) {
        appDao.insertPayment(payment)
        
        // Update invoice matching bookingId
        val invoice = appDao.getInvoiceForBookingSync(payment.bookingId)
        if (invoice != null) {
            val newPaid = invoice.paidAmount + payment.amount
            val newDue = (invoice.totalRent - newPaid).coerceAtLeast(0.0)
            appDao.updateInvoice(invoice.copy(paidAmount = newPaid, dueAmount = newDue))

            // Update booking status if dues are fully cleared and status was "Due Payment"
            val booking = appDao.getBookingByIdSync(payment.bookingId)
            if (booking != null && booking.status == "Due Payment" && newDue <= 0.0) {
                appDao.updateBooking(booking.copy(status = "Approved"))
            }

            // Send notification about the successful transaction
            val notification = Notification(
                userId = booking?.userId ?: 0,
                title = "Payment Confirmed ✅",
                message = "Payment of ${payment.amount} BDT via ${payment.method} has been received. Transaction ID: ${payment.transactionId}.",
                timestamp = System.currentTimeMillis()
            )
            appDao.insertNotification(notification)
        }
    }

    // --- Agreements ---
    fun getAgreementForBooking(bookingId: Int): Flow<Agreement?> = appDao.getAgreementForBooking(bookingId)

    suspend fun getAgreementForBookingSync(bookingId: Int): Agreement? = withContext(Dispatchers.IO) {
        appDao.getAgreementForBookingSync(bookingId)
    }

    suspend fun signAgreement(agreement: Agreement): Long = withContext(Dispatchers.IO) {
        appDao.insertAgreement(agreement)
    }

    // --- Notifications ---
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>> = appDao.getNotificationsForUser(userId)

    suspend fun addNotification(notification: Notification) = withContext(Dispatchers.IO) {
        appDao.insertNotification(notification)
    }

    suspend fun markNotificationsRead(userId: Int) = withContext(Dispatchers.IO) {
        appDao.markAllNotificationsAsRead(userId)
    }

    // --- Pre-populations ---
    suspend fun populateInitialData() = withContext(Dispatchers.IO) {
        // 1. Populate Users
        val usersCount = appDao.getLoggedInUserSync()
        val allUsersList = appDao.getUserByEmail("admin@ridedeal.com")
        if (allUsersList == null) {
            appDao.insertUser(
                User(
                    fullName = "Ride Deal Admin",
                    email = "admin@ridedeal.com",
                    phone = "01711223344",
                    role = "Admin",
                    isLoggedIn = false
                )
            )

            appDao.insertUser(
                User(
                    fullName = "Sajid Khan",
                    email = "hellosajid71@gmail.com",
                    phone = "01999888777",
                    role = "Customer",
                    nidNo = "34591029348",
                    drivingLicenseNo = "DK-20392AB",
                    isLoggedIn = true // Log him in by default as demo user
                )
            )
        }

        // 2. Populate Bikes
        val bikes = appDao.getAllBikesSync()
        if (bikes.isEmpty()) {
            val initialBikes = listOf(
                Bike(
                    model = "Yamaha R15 V4",
                    brand = "Yamaha",
                    plateNumber = "Dhaka Metro-LA-1234",
                    engineNumber = "E54321-YAM",
                    chassisNumber = "C12345-R15",
                    cc = 155,
                    rentalPrice = 2500.0,
                    deposit = 5000.0,
                    status = "Available",
                    photoRes = "r15",
                    specs = "155cc | 18.4 HP | Dual Channel ABS | Quickshifter",
                    terms = "1. Rider must wear helmets and protective gear at all times. 2. Any physical damage is payable by standard damage liability policies. 3. Bike must be returned with standard 95 Octane fuel equivalent to level at pickup."
                ),
                Bike(
                    model = "Suzuki Gixxer SF",
                    brand = "Suzuki",
                    plateNumber = "Dhaka Metro-HA-5678",
                    engineNumber = "E98765-SUZ",
                    chassisNumber = "C98765-SF",
                    cc = 155,
                    rentalPrice = 1800.0,
                    deposit = 4000.0,
                    status = "Available",
                    photoRes = "gixxer",
                    specs = "155cc | 13.4 HP | Single Channel ABS",
                    terms = "1. Maximum speed inside City limit is 60km/h. 2. Document copy must be produced. 3. Return fuel same as checkout level."
                ),
                Bike(
                    model = "Honda CB Hornet 160R",
                    brand = "Honda",
                    plateNumber = "Dhaka Metro-KA-9012",
                    engineNumber = "E11223-HON",
                    chassisNumber = "C22334-HRN",
                    cc = 163,
                    rentalPrice = 1500.0,
                    deposit = 3000.0,
                    status = "Available",
                    photoRes = "hornet",
                    specs = "163cc | 15.1 HP | Combined Braking System",
                    terms = "1. Helmets are mandatory for both rider and pillion. 2. Carry your hardcopy driving license. 3. Octane fuel level of at least half reserve required."
                ),
                Bike(
                    model = "Suzuki Intruder 150",
                    brand = "Suzuki",
                    plateNumber = "Dhaka Metro-BA-1122",
                    engineNumber = "E44556-INT",
                    chassisNumber = "C55667-IND",
                    cc = 155,
                    rentalPrice = 1700.0,
                    deposit = 4000.0,
                    status = "Rented",
                    photoRes = "intruder",
                    specs = "155cc | Comfortable Cruiser | Single Channel ABS",
                    terms = "1. Cruiser is for speed cruise and comfortable ride. Hard cornering is not advised. 2. Security deposit returned after inspection."
                ),
                Bike(
                    model = "Vespa VXL 150",
                    brand = "Vespa",
                    plateNumber = "Dhaka Metro-M-3344",
                    engineNumber = "E77889-VES",
                    chassisNumber = "C88990-VXL",
                    cc = 150,
                    rentalPrice = 1400.0,
                    deposit = 3000.0,
                    status = "Available",
                    photoRes = "vespa",
                    specs = "150cc | Retro design | Automatic CVT gearbox",
                    terms = "1. Easy scooter riding. No gear clutch needed. 2. Underseat trunk fits one helmet. 3. Return clean."
                ),
                Bike(
                    model = "Kawasaki Ninja 125",
                    brand = "Kawasaki",
                    plateNumber = "Dhaka Metro-DA-4455",
                    engineNumber = "E12131-KAW",
                    chassisNumber = "C15161-NIN",
                    cc = 125,
                    rentalPrice = 2200.0,
                    deposit = 6000.0,
                    status = "Maintenance",
                    photoRes = "ninja",
                    specs = "125cc | Premium Sport Finish | Uni-Trak suspension",
                    terms = "1. Premium 125cc sport bike requires strict Octane fueling only. 2. Any track usage is strictly prohibited."
                )
            )

            for (bike in initialBikes) {
                appDao.insertBike(bike)
            }
        }
    }
}
