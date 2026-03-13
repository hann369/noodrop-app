package com.noodrop.app.ui.subscription

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State ─────────────────────────────────────────────────────────────────────
data class SubscriptionState(
    val subscription: SubscriptionStatus = SubscriptionStatus(),
    val premiumProduct: AppProduct?      = null,
    val isLoading: Boolean               = false,
    val purchaseResult: PurchaseResult?  = null,
    val errorMessage: String?            = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────
@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repo: NoodropRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(
                    repo.subscriptionFlow(),
                    repo.appProductsFlow(),
                ) { subscription, appProducts ->
                    val premiumProduct = appProducts.find { p -> p.id == "premium-subscription" }
                        ?: appProducts.find { p ->
                            p.id.contains("premium", ignoreCase = true) ||
                            p.name.contains("premium", ignoreCase = true) ||
                            p.name.contains("subscription", ignoreCase = true)
                        }
                    _state.value.copy(subscription = subscription, premiumProduct = premiumProduct)
                }.collect { _state.value = it }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun subscribe() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, purchaseResult = null) }
            try {
                val uid       = auth.currentUser?.uid ?: run {
                    _state.update { it.copy(isLoading = false, purchaseResult = PurchaseResult(false, "Nicht eingeloggt")) }
                    return@launch
                }
                val productId = _state.value.premiumProduct?.id ?: "premium-subscription"
                val result    = repo.purchaseAppProduct(productId, uid)
                _state.update { it.copy(isLoading = false, purchaseResult = result) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading      = false,
                        purchaseResult = PurchaseResult(false, e.message ?: "Purchase failed"),
                    )
                }
            }
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repo.cancelSubscription()
                _state.update { it.copy(isLoading = false, purchaseResult = result) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading      = false,
                        purchaseResult = PurchaseResult(false, e.message ?: "Cancellation failed"),
                    )
                }
            }
        }
    }

    fun openCustomerPortal() {
        // Stripe Customer Portal URL — set this in Stripe Dashboard → Settings → Billing → Customer portal
        // Get your portal link: https://billing.stripe.com/p/login/...
        val portalUrl = "https://billing.stripe.com/p/login/14A8wR5687Ey9hmfny1B600" // ← replace with your portal URL
        _state.update {
            it.copy(
                purchaseResult = PurchaseResult(
                    success     = true,
                    downloadUrl = portalUrl,
                )
            )
        }
    }

    fun clearPurchaseResult() = _state.update { it.copy(purchaseResult = null) }
    fun clearError()          = _state.update { it.copy(errorMessage = null) }
}

// ── Sheet ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSheet(
    onClose: () -> Unit,
    vm: SubscriptionViewModel = hiltViewModel(),
) {
    val s       = vm.state.collectAsState().value
    val context = LocalContext.current

    // Handle Stripe redirect
    LaunchedEffect(s.purchaseResult) {
        s.purchaseResult?.let { result ->
            if (result.success) {
                val urlToOpen = result.downloadUrl
                    ?: result.sessionId?.let { "https://checkout.stripe.com/pay/$it" }
                if (urlToOpen != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            kotlinx.coroutines.delay(3500)
            vm.clearPurchaseResult()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        modifier         = Modifier.fillMaxHeight(0.92f),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = {
            Box(
                Modifier
                    .padding(top = 14.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline),
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Hero header ───────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                NdOrange.copy(alpha = 0.20f),
                                NdOrange.copy(alpha = 0.06f),
                                Color.Transparent,
                            )
                        )
                    ),
            ) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(NdOrange.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✦", fontSize = 30.sp, color = NdOrange)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Noodrop Premium",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Unlock your full cognitive potential",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                Modifier.padding(horizontal = 22.dp).padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Current plan ──────────────────────────────────────────────
                if (s.subscription.isActive && s.subscription.plan != SubscriptionPlan.FREE) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NdGreen.copy(alpha = 0.08f))
                            .border(1.dp, NdGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Active Plan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(s.subscription.plan.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        GreenChip("✓ Active")
                    }
                }

                // ── Benefits list ─────────────────────────────────────────────
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("What you get", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    PremiumBenefit("🔬", "All Premium Protocols", "Focus, Longevity & more")
                    PremiumBenefit("📊", "Advanced Analytics", "Compound impact & mood trends")
                    PremiumBenefit("🚀", "Future Protocols", "Automatic access as we add more")
                    PremiumBenefit("🎯", "Priority Support", "Direct access to our team")
                }

                // ── Pricing card ──────────────────────────────────────────────
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    NdOrange.copy(alpha = 0.18f),
                                    NdOrange.copy(alpha = 0.06f),
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(listOf(NdOrange.copy(alpha = 0.6f), NdOrange.copy(alpha = 0.2f))),
                            shape = RoundedCornerShape(18.dp),
                        )
                        .padding(18.dp),
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Premium", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    s.premiumProduct?.priceformatted?.takeIf { it.isNotBlank() } ?: "€9,99 / month",
                                    style      = MaterialTheme.typography.headlineSmall,
                                    color      = NdOrange,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NdGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text("Best Value", style = MaterialTheme.typography.labelSmall, color = NdGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        if (s.subscription.isActive && s.subscription.plan != SubscriptionPlan.FREE) {
                            // ── Already subscribed ────────────────────────────
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("✓ You're subscribed", style = MaterialTheme.typography.labelLarge, color = NdGreen, fontWeight = FontWeight.SemiBold)
                                }
                                // Manage Subscription Button → Stripe Customer Portal
                                Button(
                                    onClick  = { vm.openCustomerPortal() },
                                    enabled  = !s.isLoading,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape    = RoundedCornerShape(12.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    Text("Manage Subscription", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        } else {
                            Button(
                                onClick  = { vm.subscribe() },
                                enabled  = !s.isLoading,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = NdOrange),
                            ) {
                                if (s.isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Start Premium", style = MaterialTheme.typography.labelLarge, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // ── Cancel info text ──────────────────────────────────────────
                if (s.subscription.isActive && s.subscription.plan != SubscriptionPlan.FREE) {
                    Text(
                        "To cancel your subscription, use Manage Subscription above.",
                        style     = MaterialTheme.typography.labelSmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }

                // ── Feedback ──────────────────────────────────────────────────
                s.purchaseResult?.let { result ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (result.success) NdGreen.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(if (result.success) "✅" else "❌")
                        Text(
                            if (result.success) "Redirecting to checkout…"
                            else result.errorMessage ?: "Purchase failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (result.success) NdGreen else MaterialTheme.colorScheme.error,
                        )
                    }
                }

                s.errorMessage?.let {
                    Text("⚠️ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Text(
                    "Secure payments via Stripe · Cancel anytime",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ── Benefit Row ───────────────────────────────────────────────────────────────
@Composable
private fun PremiumBenefit(icon: String, title: String, subtitle: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NdOrange.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center,
        ) { Text(icon, fontSize = 20.sp) }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
