package com.noodrop.app.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.NdGreen
import com.noodrop.app.ui.theme.NdOrange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// - ViewModel -
data class SubscriptionState(
    val subscription: SubscriptionStatus = SubscriptionStatus(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val purchaseResult: PurchaseResult? = null,
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.subscriptionFlow(),
                repo.productsFlow(),
            ) { subscription, products ->
                SubscriptionState(
                    subscription = subscription,
                    products = products,
                )
            }.collect { _state.value = it }
        }
    }

    fun purchaseProduct(productId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repo.purchaseProduct(productId)
            _state.update { it.copy(isLoading = false, purchaseResult = result) }
        }
    }

    fun clearPurchaseResult() {
        _state.update { it.copy(purchaseResult = null) }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repo.cancelSubscription()
            _state.update { it.copy(isLoading = false, purchaseResult = result) }
        }
    }
}

// - Screen -
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSheet(
    onClose: () -> Unit,
    vm: SubscriptionViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxHeight(0.9f),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Upgrade to Premium",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Current Plan
            NdCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Current Plan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        s.subscription.plan.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (s.subscription.isActive) NdOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (s.subscription.isActive) {
                        Text(
                            "Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = NdOrange,
                        )
                    } else {
                        Text(
                            "Free Plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Benefits
            NdCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "✨ Premium Benefits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    BenefitItem("🔬 Access to all Premium Protocols")
                    BenefitItem("📊 Advanced Analytics & Insights")
                    BenefitItem("💡 Personalized Compound Recommendations")
                    BenefitItem("📈 Unlimited Stack History")
                    BenefitItem("🎯 Priority Support")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Subscription Plans
            Text(
                "Choose Your Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            // Premium Plan
            SubscriptionPlanCard(
                plan = SubscriptionPlan.PREMIUM,
                isCurrent = s.subscription.plan == SubscriptionPlan.PREMIUM && s.subscription.isActive,
                onSelect = { vm.purchaseProduct("premium_monthly") },
                isLoading = s.isLoading,
            )

            Spacer(Modifier.height(12.dp))

            // Pro Plan
            SubscriptionPlanCard(
                plan = SubscriptionPlan.PRO,
                isCurrent = s.subscription.plan == SubscriptionPlan.PRO && s.subscription.isActive,
                onSelect = { vm.purchaseProduct("pro_monthly") },
                isLoading = s.isLoading,
            )

            Spacer(Modifier.height(16.dp))

            // Cancel Subscription (if active)
            if (s.subscription.isActive && s.subscription.plan != SubscriptionPlan.FREE) {
                NdOutlineButton(
                    "Cancel Subscription",
                    onClick = { vm.cancelSubscription() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }

            // Purchase Result
            s.purchaseResult?.let { result ->
                Spacer(Modifier.height(16.dp))
                if (result.success) {
                    NdCard {
                        Text(
                            "✅ Purchase successful!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NdGreen,
                        )
                    }
                } else {
                    NdCard {
                        Text(
                            "❌ ${result.errorMessage ?: "Purchase failed"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                LaunchedEffect(result) {
                    kotlinx.coroutines.delay(3000)
                    vm.clearPurchaseResult()
                }
            }

            Spacer(Modifier.height(16.dp))

            // Footer
            Text(
                "Secure payments powered by Stripe",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    isLoading: Boolean,
) {
    val borderColor = if (isCurrent) NdOrange else MaterialTheme.colorScheme.outline
    val bgColor = if (isCurrent) NdOrange.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(enabled = !isCurrent && !isLoading, onClick = onSelect)
            .padding(20.dp),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        plan.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        plan.price,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NdOrange,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "per month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isCurrent) {
                    Text(
                        "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = NdOrange,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (plan == SubscriptionPlan.PREMIUM) {
                Text("• Access to Premium Protocols", style = MaterialTheme.typography.bodySmall)
                Text("• Basic Analytics", style = MaterialTheme.typography.bodySmall)
                Text("• Email Support", style = MaterialTheme.typography.bodySmall)
            } else if (plan == SubscriptionPlan.PRO) {
                Text("• All Premium Protocols", style = MaterialTheme.typography.bodySmall)
                Text("• Advanced Analytics", style = MaterialTheme.typography.bodySmall)
                Text("• Priority Support", style = MaterialTheme.typography.bodySmall)
                Text("• Custom Protocol Requests", style = MaterialTheme.typography.bodySmall)
            }

            if (!isCurrent) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onSelect,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NdOrange,
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Text("Subscribe Now")
                    }
                }
            }
        }
    }
}
