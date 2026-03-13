package com.noodrop.app.ui.library

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.subscription.SubscriptionSheet
import com.noodrop.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State ─────────────────────────────────────────────────────────────────────
data class LibraryState(
    val filter: ProtocolStatus?          = null,
    val list: List<Protocol>             = ProtocolData.all,
    val detail: Protocol?                = null,
    val tab: LibraryTab                  = LibraryTab.PROTOCOLS,
    val compoundSearch: String           = "",
    val selectedCompound: Compound?      = null,
    val products: List<Product>          = emptyList(),
    val appProducts: List<AppProduct>    = emptyList(),
    val subscription: SubscriptionStatus = SubscriptionStatus(),
    val toast: String?                   = null,
    val purchaseResult: PurchaseResult?  = null,
    val isLoading: Boolean               = false,
    // Protocol unlock dialog
    val unlockProtocol: Protocol?        = null,
    val unlockAppProduct: AppProduct?    = null,
    val isLoadingAppProduct: Boolean     = false,
)

enum class LibraryTab { PROTOCOLS, COMPOUNDS, GUIDES }

// ── ViewModel ─────────────────────────────────────────────────────────────────
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        // Collect each flow independently so one failure doesn't crash the others
        viewModelScope.launch {
            try {
                repo.productsFlow().collect { products ->
                    _state.update { it.copy(products = products.filter { p -> p.isactive }) }
                }
            } catch (e: Exception) { /* products unavailable */ }
        }
        viewModelScope.launch {
            try {
                repo.appProductsFlow().collect { appProducts ->
                    _state.update { it.copy(appProducts = appProducts.filter { ap -> ap.isActive }) }
                }
            } catch (e: Exception) { /* products-app unavailable - non-fatal */ }
        }
        viewModelScope.launch {
            try {
                repo.subscriptionFlow().collect { subscription ->
                    _state.update { it.copy(subscription = subscription) }
                }
            } catch (e: Exception) { /* subscription unavailable */ }
        }
    }

    fun setFilter(f: ProtocolStatus?) = _state.update {
        it.copy(
            filter = f,
            list   = if (f == null) ProtocolData.all
                     else ProtocolData.all.filter { p -> p.status == f }
        )
    }

    // Opens detail OR shows unlock dialog for paid protocols
    fun openDetail(p: Protocol) = viewModelScope.launch {
        val s = _state.value
        if (p.status == ProtocolStatus.FREE || p.status == ProtocolStatus.COMING_SOON) {
            _state.update { it.copy(detail = p) }
            return@launch
        }
        // PAID: check if user already has premium subscription
        if (s.subscription.isActive && s.subscription.plan != SubscriptionPlan.FREE) {
            _state.update { it.copy(detail = p) }
            return@launch
        }
        // Show unlock dialog, load AppProduct in background
        _state.update { it.copy(unlockProtocol = p, isLoadingAppProduct = true) }
        try {
            val appProduct = repo.getAppProductForProtocol(p.id)
            _state.update { it.copy(unlockAppProduct = appProduct, isLoadingAppProduct = false) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingAppProduct = false) }
        }
    }

    fun closeUnlockDialog()  = _state.update { it.copy(unlockProtocol = null, unlockAppProduct = null) }
    fun closeDetail()        = _state.update { it.copy(detail = null) }
    fun setTab(t: LibraryTab) = _state.update { it.copy(tab = t) }
    fun searchCompounds(q: String) = _state.update { it.copy(compoundSearch = q) }
    fun selectCompound(c: Compound?) = _state.update { it.copy(selectedCompound = c) }
    fun clearToast()         = _state.update { it.copy(toast = null) }
    fun clearPurchaseResult() = _state.update { it.copy(purchaseResult = null) }

    fun addCompoundToStack(compound: Compound, timing: Timing = Timing.MORNING) = viewModelScope.launch {
        repo.addToStack(StackEntry(compoundName = compound.name, dose = compound.defaultDose, timing = timing))
        _state.update { it.copy(selectedCompound = null, toast = "Added ${compound.name} to stack!") }
    }

    fun loadIntoStack(protocol: Protocol) = viewModelScope.launch {
        repo.loadPreset(protocol)
        _state.update { it.copy(detail = null, toast = "Loaded ${protocol.name} into stack!") }
    }

    // Single protocol purchase (€2.99)
    fun purchaseSingleProtocol(protocolId: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, purchaseResult = null) }
        try {
            val result = repo.purchaseAppProduct(protocolId)
            _state.update { it.copy(isLoading = false, purchaseResult = result) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, purchaseResult = PurchaseResult(false, e.message ?: "Error")) }
        }
    }

    // Guide purchase
    fun purchaseProduct(productId: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, purchaseResult = null) }
        try {
            val result = repo.purchaseProduct(productId)
            _state.update { it.copy(isLoading = false, purchaseResult = result) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, purchaseResult = PurchaseResult(false, e.message ?: "Error")) }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = hiltViewModel()) {
    val s       = vm.state.collectAsState().value
    val snack   = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showSubscription by remember { mutableStateOf(false) }

    LaunchedEffect(s.toast) {
        s.toast?.let { snack.showSnackbar(it); vm.clearToast() }
    }

    // Handle purchase result - open URL in browser
    LaunchedEffect(s.purchaseResult) {
        s.purchaseResult?.let { result ->
            if (result.success) {
                val urlToOpen = result.downloadUrl ?: result.sessionId?.let { "https://checkout.stripe.com/pay/$it" }
                if (urlToOpen != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            } else {
                snack.showSnackbar(result.errorMessage ?: "Purchase failed")
            }
            vm.clearPurchaseResult()
        }
    }

    // Protocol detail sheet
    s.detail?.let { proto ->
        ProtocolDetailSheet(
            protocol     = proto,
            hasAccess    = s.subscription.isActive || proto.status == ProtocolStatus.FREE,
            onLoadStack  = { vm.loadIntoStack(proto) },
            onDismiss    = { vm.closeDetail() },
        )
    }

    // Compound detail sheet
    s.selectedCompound?.let { compound ->
        com.noodrop.app.ui.common.CompoundDetailSheet(
            compound     = compound,
            onAddToStack = { vm.addCompoundToStack(compound) },
            onDismiss    = { vm.selectCompound(null) },
        )
    }

    // Protocol unlock dialog
    s.unlockProtocol?.let { protocol ->
        ProtocolUnlockDialog(
            protocol         = protocol,
            appProduct       = s.unlockAppProduct,
            isLoadingProduct = s.isLoadingAppProduct,
            isLoadingPurchase = s.isLoading,
            onSinglePurchase = {
                vm.purchaseSingleProtocol(protocol.id)
                vm.closeUnlockDialog()
            },
            onSubscribe      = {
                vm.closeUnlockDialog()
                showSubscription = true
            },
            onDismiss        = { vm.closeUnlockDialog() },
        )
    }

    // Subscription sheet
    if (showSubscription) {
        SubscriptionSheet(onClose = { showSubscription = false })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            LibraryHeader(
                subscription = s.subscription,
                onUpgrade    = { showSubscription = true },
            )

            // ── Tabs ──────────────────────────────────────────────────────────
            TgTabBar(
                tabs     = listOf("Protocols", "Compounds", "Guides"),
                selected = s.tab.ordinal,
                onSelect = { vm.setTab(LibraryTab.values()[it]) },
            )

            // ── Content ───────────────────────────────────────────────────────
            when (s.tab) {
                LibraryTab.PROTOCOLS  -> ProtocolsContent(s, vm, onSubscribe = { showSubscription = true })
                LibraryTab.COMPOUNDS  -> CompoundsContent(s, vm)
                LibraryTab.GUIDES     -> GuidesContent(s, vm)
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun LibraryHeader(subscription: SubscriptionStatus, onUpgrade: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "Library",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Protocols & Compounds",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!subscription.isActive || subscription.plan == SubscriptionPlan.FREE) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NdOrange, NdOrange.copy(red = 1f, green = 0.5f, blue = 0.1f)))
                    )
                    .clickable(onClick = onUpgrade)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    "✦ Premium",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NdOrange.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    "✦ ${subscription.plan.displayName}",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = NdOrange,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Telegram-style Tab Bar ────────────────────────────────────────────────────
@Composable
private fun TgTabBar(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tabs.forEachIndexed { i, label ->
            val isSelected = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    )
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isSelected) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

// ── Protocols Tab ─────────────────────────────────────────────────────────────
@Composable
private fun ProtocolsContent(s: LibraryState, vm: LibraryViewModel, onSubscribe: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Filter chips
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NdFilterChip("All",       s.filter == null,                   { vm.setFilter(null) })
            NdFilterChip("Free",      s.filter == ProtocolStatus.FREE,    { vm.setFilter(ProtocolStatus.FREE) })
            NdFilterChip("Premium",   s.filter == ProtocolStatus.PAID,    { vm.setFilter(ProtocolStatus.PAID) })
            NdFilterChip("Coming",    s.filter == ProtocolStatus.COMING_SOON, { vm.setFilter(ProtocolStatus.COMING_SOON) })
        }

        Spacer(Modifier.height(6.dp))

        s.list.forEach { proto ->
            val accentColor = when (proto.accent) {
                ProtocolAccent.ORANGE -> NdOrange
                ProtocolAccent.GREEN  -> NdGreen
                ProtocolAccent.BLUE   -> NdBlue
                ProtocolAccent.PURPLE -> NdPurple
            }
            val hasAccess = s.subscription.isActive && s.subscription.plan != SubscriptionPlan.FREE
            val appProduct = s.appProducts.find { ap -> ap.protocolId == proto.id }

            if (proto.status == ProtocolStatus.COMING_SOON) {
                // Coming soon → keep original flat card (no flip, not clickable)
                TgProtocolCard(
                    protocol   = proto,
                    hasAccess  = hasAccess,
                    appProduct = appProduct,
                    onClick    = {},
                )
            } else {
                FlipProtocolCard(
                    name        = proto.name,
                    goal        = proto.goal,
                    tags        = buildList {
                        add(proto.goal)
                        add(proto.duration)
                        if (proto.status == ProtocolStatus.PAID && !hasAccess) {
                            add(appProduct?.priceformatted?.takeIf { it.isNotBlank() } ?: "€2.99")
                        }
                        if (proto.status == ProtocolStatus.PAID && hasAccess) add("✓ Unlocked")
                    },
                    compounds   = proto.compounds.map { name ->
                        name to ""   // dose not available at list level
                    },
                    accentColor = accentColor,
                    onLoadStack = { vm.loadIntoStack(proto) },
                    onTap       = { vm.openDetail(proto) },
                )
            }
        }
    }
}

// ── Telegram-inspired Protocol Card ──────────────────────────────────────────
@Composable
private fun TgProtocolCard(
    protocol:  Protocol,
    hasAccess: Boolean,
    appProduct: AppProduct?,
    onClick:   () -> Unit,
) {
    val coming  = protocol.status == ProtocolStatus.COMING_SOON
    val isPaid  = protocol.status == ProtocolStatus.PAID
    val locked  = isPaid && !hasAccess

    val accentColor = when (protocol.accent) {
        ProtocolAccent.ORANGE -> NdOrange
        ProtocolAccent.GREEN  -> NdGreen
        ProtocolAccent.BLUE   -> NdBlue
        ProtocolAccent.PURPLE -> NdPurple
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .then(
                if (!coming) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        // Accent left bar
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(
                    Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.2f)))
                )
        )

        Row(
            Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Icon circle
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(protocol.icon, fontSize = 22.sp)
            }

            // Content
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        protocol.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f),
                    )
                    when {
                        coming -> NdChip("Soon", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        isPaid && !hasAccess -> {
                            val price = appProduct?.priceformatted?.takeIf { it.isNotBlank() } ?: "€2.99"
                            OrangeChip(price)
                        }
                        isPaid && hasAccess -> GreenChip("✓ Unlocked")
                    }
                }
                Text(
                    protocol.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NdChip(protocol.goal)
                    NdChip(protocol.duration)
                }
            }

            // Right chevron / lock
            if (!coming) {
                if (locked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint     = NdOrange,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Text(
                        "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Protocol Unlock Dialog ────────────────────────────────────────────────────
@Composable
private fun ProtocolUnlockDialog(
    protocol:          Protocol,
    appProduct:        AppProduct?,
    isLoadingProduct:  Boolean,
    isLoadingPurchase: Boolean,
    onSinglePurchase:  () -> Unit,
    onSubscribe:       () -> Unit,
    onDismiss:         () -> Unit,
) {
    val accentColor = when (protocol.accent) {
        ProtocolAccent.ORANGE -> NdOrange
        ProtocolAccent.GREEN  -> NdGreen
        ProtocolAccent.BLUE   -> NdBlue
        ProtocolAccent.PURPLE -> NdPurple
    }
    val price = appProduct?.priceformatted?.takeIf { it.isNotBlank() } ?: "€2.99"

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Column {
                // Gradient header
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(accentColor.copy(alpha = 0.2f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(protocol.icon, fontSize = 40.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            protocol.name,
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    // Close button
                    IconButton(
                        onClick  = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        protocol.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Compounds preview
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        protocol.compounds.forEach { NdChip(it) }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Option 1: Single purchase
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor.copy(alpha = 0.07f))
                            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !isLoadingPurchase && !isLoadingProduct, onClick = onSinglePurchase)
                            .padding(14.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    "Unlock this Protocol",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "One-time purchase",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isLoadingProduct) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = accentColor, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    price,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = accentColor,
                                )
                            }
                        }
                    }

                    // Divider with OR
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                        Text("or", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                    }

                    // Option 2: Subscribe
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NdOrange.copy(alpha = 0.15f), NdOrange.copy(alpha = 0.05f))
                                )
                            )
                            .border(1.dp, NdOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onSubscribe)
                            .padding(14.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        "✦ Premium",
                                        style      = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = NdOrange,
                                    )
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NdGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                    ) {
                                        Text("Best Value", style = MaterialTheme.typography.labelSmall, color = NdGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Text(
                                    "All protocols + future updates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "€9.99/mo",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = NdOrange,
                            )
                        }
                    }

                    if (isLoadingPurchase) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color    = accentColor,
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// ── Protocol Detail Sheet ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProtocolDetailSheet(
    protocol:    Protocol,
    hasAccess:   Boolean,
    onLoadStack: () -> Unit,
    onDismiss:   () -> Unit,
) {
    val accentColor = when (protocol.accent) {
        ProtocolAccent.ORANGE -> NdOrange
        ProtocolAccent.GREEN  -> NdGreen
        ProtocolAccent.BLUE   -> NdBlue
        ProtocolAccent.PURPLE -> NdPurple
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier         = Modifier.fillMaxHeight(0.88f),
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(protocol.icon, fontSize = 26.sp) }
                    Column {
                        Text(protocol.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(protocol.goal, style = MaterialTheme.typography.bodySmall, color = accentColor)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TgStatChip("Duration", protocol.duration, accentColor, Modifier.weight(1f))
                TgStatChip("Compounds", "${protocol.compounds.size}", accentColor, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            Text(protocol.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(16.dp))

            // Compounds chips
            Text("Stack", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                protocol.compounds.forEach {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) { Text(it, style = MaterialTheme.typography.labelSmall, color = accentColor) }
                }
            }

            if (protocol.detailText.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("Protocol", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(14.dp),
                ) {
                    Text(protocol.detailText, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (protocol.presetEntries.isNotEmpty() && hasAccess) {
                Spacer(Modifier.height(20.dp))
                NdButton("Load into My Stack →", onClick = onLoadStack)
                Spacer(Modifier.height(8.dp))
            }

            NdOutlineButton("Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TgStatChip(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.07f))
            .border(0.5.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Compounds Tab ─────────────────────────────────────────────────────────────
@Composable
private fun CompoundsContent(s: LibraryState, vm: LibraryViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        OutlinedTextField(
            value         = s.compoundSearch,
            onValueChange = { vm.searchCompounds(it) },
            placeholder   = { Text("Search compounds...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = NdOrange,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(Modifier.height(12.dp))

        val filtered = remember(s.compoundSearch) {
            if (s.compoundSearch.isBlank()) CompoundData.all
            else CompoundData.all.filter {
                it.name.contains(s.compoundSearch, ignoreCase = true) ||
                it.category.contains(s.compoundSearch, ignoreCase = true)
            }
        }

        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filtered.forEach { compound ->
                TgCompoundRow(compound = compound, onClick = { vm.selectCompound(compound) })
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TgCompoundRow(compound: Compound, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NdOrange.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                compound.name.take(2).uppercase(),
                style      = MaterialTheme.typography.labelMedium,
                color      = NdOrange,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(compound.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "${compound.category} · ${compound.defaultDose}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            compound.benefits.take(2).forEach {
                NdChip(it)
            }
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Guides Tab ────────────────────────────────────────────────────────────────
@Composable
private fun GuidesContent(s: LibraryState, vm: LibraryViewModel) {
    if (s.products.isEmpty()) {
        EmptyState("📚", "No guides available yet")
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        s.products.forEach { product ->
            TgProductCard(product = product, onPurchase = { vm.purchaseProduct(product.id) }, isLoading = s.isLoading)
        }
    }
}

@Composable
private fun TgProductCard(product: Product, onPurchase: () -> Unit, isLoading: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        product.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NdOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        if (product.price <= 0.0) "Free" else product.priceformatted,
                        style      = MaterialTheme.typography.labelMedium,
                        color      = NdOrange,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (product.features.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    product.features.forEach { NdChip(it) }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick  = onPurchase,
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = NdOrange),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (product.price <= 0.0) "Get for Free" else "Purchase",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
