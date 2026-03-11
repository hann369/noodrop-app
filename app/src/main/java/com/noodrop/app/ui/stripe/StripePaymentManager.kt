package com.noodrop.app.ui.stripe

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

/**
 * Stripe Payment Manager for handling checkout sessions
 */
class StripePaymentManager(
    private val activity: ComponentActivity,
    private val publishableKey: String = "pk_test_YOUR_STRIPE_PUBLISHABLE_KEY" // TODO: Move to config
) {

    private lateinit var paymentSheet: PaymentSheet
    private var paymentResultLauncher: ActivityResultLauncher<Intent>? = null

    init {
        // Initialize Stripe
        PaymentConfiguration.init(activity, publishableKey)
        paymentSheet = PaymentSheet(activity, ::onPaymentSheetResult)

        // Register activity result launcher
        paymentResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle custom URL schemes if needed
        }
    }

    /**
     * Present Stripe Checkout with session ID
     */
    fun presentCheckout(sessionId: String) {
        val checkoutUrl = "https://checkout.stripe.com/pay/$sessionId"

        // Option 1: Use PaymentSheet (recommended for mobile)
        // paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret)

        // Option 2: Open in browser (fallback)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
        activity.startActivity(intent)
    }

    /**
     * Handle PaymentSheet results
     */
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                // Payment succeeded
                handlePaymentSuccess()
            }
            is PaymentSheetResult.Canceled -> {
                // Payment canceled
                handlePaymentCanceled()
            }
            is PaymentSheetResult.Failed -> {
                // Payment failed
                handlePaymentFailed(paymentSheetResult.error)
            }
        }
    }

    private fun handlePaymentSuccess() {
        // TODO: Update user subscription
        // TODO: Show success message
        // TODO: Navigate to download/thank you page
    }

    private fun handlePaymentCanceled() {
        // TODO: Show cancellation message
    }

    private fun handlePaymentFailed(error: Throwable) {
        // TODO: Show error message
        // TODO: Log error for debugging
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        paymentResultLauncher?.unregister()
    }
}
