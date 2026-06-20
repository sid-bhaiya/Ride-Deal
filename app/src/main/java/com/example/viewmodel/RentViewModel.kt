package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.RentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface AppScreen {
    object Splash : AppScreen
    object Auth : AppScreen
    object Home : AppScreen
    data class BikeDetail(val bikeId: Int) : AppScreen
    data class Checkout(val bikeId: Int, val startDate: Long, val endDate: Long, val deliveryType: String) : AppScreen
    data class InvoiceDetail(val bookingId: Int) : AppScreen
    object AdminDashboard : AppScreen
    object UserProgressHistory : AppScreen
    object WalletPayments : AppScreen
    object BookingCalendar : AppScreen
}

class RentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RentRepository.getInstance(application)

    // Current navigation state
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Navigation Backstack
    private val backstack = mutableListOf<AppScreen>()

    // Current Logged-in User
    val currentUser: StateFlow<User?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Bikes flow
    val allBikes: StateFlow<List<Bike>> = repository.allBikes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Bookings Flow
    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Invoices Flow
    val allInvoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Payments Flow
    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User Bookings
    val userBookings: StateFlow<List<Booking>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getBookingsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications for logged-in user
    val userNotifications: StateFlow<List<Notification>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getNotificationsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtering, searching state
    val searchPattern = MutableStateFlow("")
    val selectedBrandFilter = MutableStateFlow<String?>(null)
    val selectedCcFilter = MutableStateFlow<String?>(null) // "All", "125cc", "150cc", "160cc+"
    val selectedStatusFilter = MutableStateFlow<String?>(null) // "All", "Available", "Rented", "Maintenance"

    val filteredBikes: StateFlow<List<Bike>> = combine(
        allBikes,
        searchPattern,
        selectedBrandFilter,
        selectedCcFilter,
        selectedStatusFilter
    ) { bikes, search, brand, cc, status ->
        bikes.filter { bike ->
            val matchSearch = bike.model.contains(search, ignoreCase = true) || 
                              bike.brand.contains(search, ignoreCase = true)
            val matchBrand = brand == null || bike.brand == brand
            val matchCc = cc == null || cc == "All" || when (cc) {
                "125cc" -> bike.cc <= 125
                "150cc" -> bike.cc > 125 && bike.cc <= 150
                "160cc+" -> bike.cc > 150
                else -> true
            }
            val matchStatus = status == null || status == "All" || bike.status.equals(status, ignoreCase = true)
            
            matchSearch && matchBrand && matchCc && matchStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Initial prepopulation
            repository.populateInitialData()
        }
    }

    fun getBikeById(id: Int): Flow<Bike?> = repository.getBikeById(id)

    fun signUpUser(user: User) {
        viewModelScope.launch {
            repository.signUpUser(user)
        }
    }

    // Navigation operations
    fun navigateTo(screen: AppScreen) {
        backstack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun goBack() {
        if (backstack.isNotEmpty()) {
            _currentScreen.value = backstack.removeAt(backstack.lastIndex)
        } else {
            _currentScreen.value = AppScreen.Home
        }
    }

    // User Session Operations
    fun login(email: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                repository.signUpUser(user.copy(isLoggedIn = true))
                onSuccess()
            } else {
                // If it is admin, let them login
                if (email == "admin@ridedeal.com") {
                    val newAdmin = User(fullName = "Ride Deal Admin", email = "admin@ridedeal.com", phone = "01711223344", role = "Admin", isLoggedIn = true)
                    repository.signUpUser(newAdmin)
                    onSuccess()
                } else if (email == "customer@ridedeal.com" || email == "customer@gmail.com") {
                    val newCustomer = User(fullName = "Sajid Khan", email = "hellosajid71@gmail.com", phone = "01999888777", role = "Customer", isLoggedIn = true)
                    repository.signUpUser(newCustomer)
                    onSuccess()
                } else {
                    // Automatically auto-create user for ease of prototyping! This is incredibly robust!
                    val newUser = User(fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() }, email = email, phone = "017" + (1000000..9999999).random(), role = "Customer", isLoggedIn = true)
                    repository.signUpUser(newUser)
                    onSuccess()
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAllUsers()
            navigateTo(AppScreen.Auth)
        }
    }

    fun updateProfile(name: String, phone: String, nid: String, license: String, nidPath: String? = null, licensePath: String? = null) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateUser(user.copy(
                    fullName = name,
                    phone = phone,
                    nidNo = nid,
                    drivingLicenseNo = license,
                    nidPath = nidPath,
                    licensePath = licensePath
                ))
            }
        }
    }

    // Booking Creation
    fun createAndProceedBooking(
        bikeId: Int,
        startDate: Long,
        endDate: Long,
        deliveryType: String,
        couponCode: String?,
        discount: Double,
        deposit: Double,
        accessories: Map<String, Any>,
        signaturePoints: String?, // signature drawn
        paymentMethod: String?,
        paidNow: Double,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val bike = repository.getBikeById(bikeId).first() ?: return@launch

            val days = (((endDate - startDate) / (1000 * 60 * 60 * 24)).coerceAtLeast(1L)).toInt()
            val totalRent = bike.rentalPrice * days
            val totalBill = totalRent + deposit - discount
            val finalStatus = if (paidNow >= totalBill) "Approved" else "Due Payment"

            val booking = Booking(
                userId = user.id,
                bikeId = bikeId,
                startDate = startDate,
                endDate = endDate,
                durationDays = days,
                deliveryType = deliveryType,
                couponCode = couponCode,
                status = finalStatus,
                hasHelmet = accessories["helmet"] as? Boolean ?: true,
                hasLock = accessories["lock"] as? Boolean ?: true,
                hasSpareKey = accessories["spareKey"] as? Boolean ?: false,
                hasRegCopy = accessories["regCopy"] as? Boolean ?: true,
                fuelStatus = accessories["fuel"] as? String ?: "Full"
            )

            val bookingId = repository.createBooking(booking).toInt()

            // Save e-signature/agreement if drawn
            if (signaturePoints != null) {
                repository.signAgreement(
                    Agreement(
                        bookingId = bookingId,
                        customerSignatureBase64 = signaturePoints,
                        agreementDate = System.currentTimeMillis()
                    )
                )
            }

            // Create Invoice
            val invoiceNumber = "RD-INV-${10000 + bookingId}"
            val invoice = Invoice(
                bookingId = bookingId,
                invoiceNumber = invoiceNumber,
                totalRent = totalRent,
                paidAmount = paidNow,
                dueAmount = (totalBill - paidNow).coerceAtLeast(0.0),
                depositAmount = deposit,
                discountAmount = discount,
                paymentDeadline = startDate
            )
            repository.insertInvoice(invoice)

            // Process Payment details if paid
            if (paidNow > 0.0 && paymentMethod != null) {
                val payment = Payment(
                    bookingId = bookingId,
                    invoiceNumber = invoiceNumber,
                    amount = paidNow,
                    timestamp = System.currentTimeMillis(),
                    method = paymentMethod,
                    transactionId = "TXN" + UUID.randomUUID().toString().take(8).uppercase()
                )
                repository.processPayment(payment)
            }

            // Add confirm notification
            repository.addNotification(
                Notification(
                    userId = user.id,
                    title = "Ride Confirmed! 🏍️",
                    message = "Your booking for ${bike.model} was success! Duration: $days Days.",
                    timestamp = System.currentTimeMillis()
                )
            )

            onComplete(bookingId)
        }
    }

    // Pay outstanding booking dues
    fun payOutstandingDues(bookingId: Int, invoiceId: Int, dueToPay: Double, method: String) {
        viewModelScope.launch {
            val booking = repository.getBookingByIdSync(bookingId) ?: return@launch
            val invoice = repository.getInvoiceForBookingSync(bookingId) ?: return@launch
            
            val trxId = "TXN" + UUID.randomUUID().toString().take(8).uppercase()
            val payment = Payment(
                bookingId = bookingId,
                invoiceNumber = invoice.invoiceNumber,
                amount = dueToPay,
                timestamp = System.currentTimeMillis(),
                method = method,
                transactionId = trxId
            )
            repository.processPayment(payment)
        }
    }

    // Admin Operations
    fun adminAddNewBike(bike: Bike) {
        viewModelScope.launch {
            repository.insertBike(bike)
        }
    }

    fun adminModBike(bike: Bike) {
        viewModelScope.launch {
            repository.updateBike(bike)
        }
    }

    fun adminDeleteBike(bike: Bike) {
        viewModelScope.launch {
            repository.deleteBike(bike)
        }
    }

    fun adminUpdateBookingStatus(bookingId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, newStatus)
        }
    }

    fun adminUpdateAccessories(bookingId: Int, helmet: Boolean, lock: Boolean, key: Boolean, reg: Boolean, fuel: String) {
        viewModelScope.launch {
            val booking = repository.getBookingByIdSync(bookingId)
            if (booking != null) {
                val updatedBooking = booking.copy(
                    hasHelmet = helmet,
                    hasLock = lock,
                    hasSpareKey = key,
                    hasRegCopy = reg,
                    fuelStatus = fuel
                )
                repository.updateBooking(updatedBooking)
                
                repository.addNotification(
                    Notification(
                        userId = booking.userId,
                        title = "Accessories Updated 🎒",
                        message = "Your rental accessories/fuel check was updated by Staff.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Dashboard calculations
    fun getDashboardStats(
        bikes: List<Bike>,
        bookings: List<Booking>,
        invoices: List<Invoice>
    ): DashboardWidgets {
        val totalBikes = bikes.size
        val availableBikes = bikes.count { it.status == "Available" }
        val activeRentals = bookings.count { it.status == "Active" }
        val pendingPaymentsNum = bookings.count { it.status == "Due Payment" }
        
        val totalRevenue = invoices.sumOf { it.paidAmount }
        val outstandingDues = invoices.sumOf { it.dueAmount }
        
        // Simple distinct customer count
        val totalCustomers = bookings.map { it.userId }.distinct().size

        return DashboardWidgets(
            totalBikes = totalBikes,
            availableBikes = availableBikes,
            activeRentals = activeRentals,
            monthlyRevenue = totalRevenue,
            pendingPayments = outstandingDues,
            totalCustomers = totalCustomers,
            outstandingDuesCount = pendingPaymentsNum
        )
    }

    fun formatEpochDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}

data class DashboardWidgets(
    val totalBikes: Int,
    val availableBikes: Int,
    val activeRentals: Int,
    val monthlyRevenue: Double,
    val pendingPayments: Double,
    val totalCustomers: Int,
    val outstandingDuesCount: Int
)
