package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class StripePaymentState {
    object Idle : StripePaymentState()
    object Processing : StripePaymentState()
    data class Success(val chargeId: String, val amount: String) : StripePaymentState()
    data class Failed(val error: String) : StripePaymentState()
}

class StripePaymentSimulator(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("youplayer_stripe_prefs", Context.MODE_PRIVATE)

    private val _isSupporter = MutableStateFlow(prefs.getBoolean("is_supporter", false))
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _paymentState = MutableStateFlow<StripePaymentState>(StripePaymentState.Idle)
    val paymentState: StateFlow<StripePaymentState> = _paymentState.asStateFlow()

    suspend fun processStripePayment(
        amount: String = "$4.99",
        tierName: String = "Supporter Pass"
    ): Boolean = withContext(Dispatchers.IO) {
        _paymentState.value = StripePaymentState.Processing
        YouPlayerLogger.i("StripeAPI", "Initiating secure Stripe payment intent for $tierName: $amount")

        // Simulate secure Stripe network exchange
        delay(1600)

        val success = true
        if (success) {
            val chargeId = "ch_live_" + System.currentTimeMillis().toString().takeLast(8)
            prefs.edit().putBoolean("is_supporter", true).apply()
            _isSupporter.value = true
            _paymentState.value = StripePaymentState.Success(chargeId, amount)
            YouPlayerLogger.i("StripeAPI", "Payment captured successfully! Charge ID: $chargeId for $amount")
            NotificationHelper.showSupporterNotification(context)
            return@withContext true
        } else {
            _paymentState.value = StripePaymentState.Failed("Card was declined by issuing bank")
            YouPlayerLogger.e("StripeAPI", "Payment failed")
            return@withContext false
        }
    }

    fun resetPaymentState() {
        _paymentState.value = StripePaymentState.Idle
    }
}
