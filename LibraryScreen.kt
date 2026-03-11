package com.noodrop.app.ui.library

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.stripe.StripePaymentManager
import com.noodrop.app.ui.theme.*
import com.noodrop.app.ui.subscription.SubscriptionSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// - ViewModel -
data class LibraryState(
    val filter: ProtocolStatus?  = null,
    val list: List<Protocol>     = ProtocolData.all,
    val detail: Protocol?        = null,
    val tab: LibraryTab          = LibraryTab.PROTOCOLS,  // NEW
    val compoundSearch: String   = "",  // NEW
    val selectedCompound: Compound? = null,  // NEW
    val products: List<Product>  = emptyList(),  // NEW: From Firestore
    val toast: String?           = null,
    val purchaseResult: PurchaseResult? = null,
)

enum class LibraryTab { PROTOCOLS, COMPOUNDS, PAID_PRODUCTS }  // NEW

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.productsFlow().collect { products ->
                _state.update { it.copy(products = products.filter { p -> p.isactive }) }
            }
        }
    }

    fun setFilter(f: ProtocolStatus?) = _state.update {
        it.copy(filter = f, list = if (f == null) ProtocolData.all else ProtocolData.all.filter { p -> p.status == f })
    }
    fun openDetail(p: Protocol) = viewModelScope.launch {
        val hasAccess = repo.checkProtocolAccess(p.id)
        if (hasAccess || p.status == ProtocolStatus.COMING_SOON) {
            _state.update { it.copy(detail = p) }
        } else {
            // Show upgrade message
            _state.update { it.copy(toast = "Upgrade to Premium to access this protocol") }
        }
    }
    fun closeDetail()           = _state.update { it.copy(detail = null) }
    fun setTab(t: LibraryTab)   = _state.update { it.copy(tab = t) }  // NEW
    fun searchCompounds(q: String) = _state.update { it.copy(compoundSearch = q) }  // NEW
    fun selectCompound(c: Compound?) = _state.update { it.copy(selectedCompound = c) }  // NEW

    // NEW: Add compound to stack from library
    fun addCompoundToStack(compound: Compound, timing: Timing = Timing.MORNING) = viewModelScope.launch {
        val entry = StackEntry(
            compoundName = compound.name,
            dose = compound.defaultDose,
            timing = timing,
        )
        repo.addToStack(entry)
        _state.update { it.copy(selectedCompound = null, toast = "Added ${compound.name} to stack!") }
    }
    fun clearToast()            = _state.update { it.copy(toast = null) }

    fun clearPurchaseResult()   = _state.update { it.copy(purchaseResult = null) }

    // Load protocol into stack
    fun loadIntoStack(protocol: Protocol) = viewModelScope.launch {
        repo.loadPreset(protocol)
        _state.update { it.copy(detail = null, toast = "Loaded ${protocol.name} into stack!") }
    }

    // NEW: Purchase product functionality
    fun purchaseProduct(product: Product, stripeManager: StripePaymentManager) = viewModelScope.launch {
        _state.update { it.copy(purchaseResult = null) }
        try {
            val result = repo.purchaseProduct(product.id)
            _state.update { it.copy(purchaseResult = result) }
            if (result.success) {
                if (result.sessionId != null) {
                    stripeManager.presentCheckout(result.sessionId)
                }
                // downloadUrl handled in composable
            }
        } catch (e: Exception) {
            _state.update { it.copy(purchaseResult = PurchaseResult(false, e.message ?: "Error")) }
        }
    }
}

// - Screen -
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val s by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val stripeManager = remember { StripePaymentManager(activity) }
    LaunchedEffect(s.toast) { s.toast?.let { snack.showSnackbar(it); vm.clearToast() } }

    LaunchedEffect(s.purchaseResult) {
        s.purchaseResult?.let { result ->
            if (result.success) {
                if (result.downloadUrl != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.downloadUrl))
                    context.startActivity(intent)
                }
            } else {
                snack.showSnackbar(result.errorMessage ?: "Purchase failed")
            }
            vm.clearPurchaseResult()
        }
    }

    s.detail?.let { proto ->
        ModalBottomSheet(onDismissRequest = vm::closeDetail, containerColor = MaterialTheme.colorScheme.surface) {
            ProtocolDetailSheet(proto, onLoad = { vm.loadIntoStack(proto) }, onDismiss = vm::closeDetail)
        }
    }

    // Compound Detail Sheet
    s.selectedCompound?.let { compound ->
        CompoundDetailSheet(
            compound = compound,
            onDismiss = { vm.selectCompound(null) },
            onAddToStack = { vm.addCompoundToStack(compound) },
        )
    }

    // Subscription Sheet
    var showSubscription by remember { mutableStateOf(false) }
    if (showSubscription) {
        SubscriptionSheet(onClose = { showSubscription = false })
    }

    Scaffold(snackbarHost = { SnackbarHost(snack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Knowledge Hub", style = MaterialTheme.typography.displaySmall)

            // Tab Navigation
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TabButton(
                    label = "Protocols",
                    selected = s.tab == LibraryTab.PROTOCOLS,
                    onClick = { vm.setTab(LibraryTab.PROTOCOLS) },
                    modifier = Modifier.weight(1f),
                )
                TabButton(
                    label = "Compounds",
                    selected = s.tab == LibraryTab.COMPOUNDS,
                    onClick = { vm.setTab(LibraryTab.COMPOUNDS) },
                    modifier = Modifier.weight(1f),
                )
                TabButton(
                    label = "Paid",
                    selected = s.tab == LibraryTab.PAID_PRODUCTS,
                    onClick = { vm.setTab(LibraryTab.PAID_PRODUCTS) },
                    modifier = Modifier.weight(1f),
                )
            }

            when (s.tab) {
                LibraryTab.PROTOCOLS -> ProtocolsTab(s, vm, onUpgrade = { showSubscription = true })
                LibraryTab.COMPOUNDS -> CompoundsTab(s, vm)
                LibraryTab.PAID_PRODUCTS -> PaidProductsTab(s, vm, stripeManager)
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun ProtocolsTab(s: LibraryState, vm: LibraryViewModel, onUpgrade: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NdFilterChip("All", s.filter == null, { vm.setFilter(null) })
            NdFilterChip("Free", s.filter == ProtocolStatus.FREE, { vm.setFilter(ProtocolStatus.FREE) })
            NdFilterChip("Premium", s.filter == ProtocolStatus.PAID, { vm.setFilter(ProtocolStatus.PAID) })
            NdFilterChip("Soon", s.filter == ProtocolStatus.COMING_SOON, { vm.setFilter(ProtocolStatus.COMING_SOON) })
        }
        s.list.forEach { proto -> ProtocolCard(proto, onClick = { vm.openDetail(proto) }, onUpgrade = onUpgrade) }
    }
}

@Composable
private fun CompoundsTab(s: LibraryState, vm: LibraryViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Search bar
        OutlinedTextField(
            value = s.compoundSearch,
            onValueChange = { vm.searchCompounds(it) },
            placeholder = { Text("Search compounds...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )

        // Filter by category
        val categories = CompoundData.all.map { it.category }.distinct()
        val selectedCategory = remember { mutableStateOf<String?>(null) }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NdFilterChip("All", selectedCategory.value == null, { selectedCategory.value = null })
            categories.forEach { cat ->
                NdFilterChip(cat, selectedCategory.value == cat, { selectedCategory.value = cat })
            }
        }

        // Filter compounds by search + category
        val filteredCompounds = CompoundData.all.filter { compound ->
            (s.compoundSearch.isEmpty() || compound.name.contains(s.compoundSearch, ignoreCase = true)) &&
            (selectedCategory.value == null || compound.category == selectedCategory.value)
        }

        if (filteredCompounds.isEmpty()) {
            NdCard {
                EmptyState("[Search]", "No compounds found. Try adjusting your filters.")
            }
        } else {
            filteredCompounds.forEach { compound ->
                CompoundCard(
                    compound = compound,
                    onClick = { vm.selectCompound(compound) },
                )
            }
        }
    }
}

@Composable
private fun PaidProductsTab(s: LibraryState, vm: LibraryViewModel, stripeManager: StripePaymentManager) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (s.products.isEmpty()) {
            NdCard {
                EmptyState("📚", "No paid protocols available yet. Check back soon!")
            }
        } else {
            s.products.forEach { product ->
                ProductCard(product = product, onPurchase = { vm.purchaseProduct(product, stripeManager) })
            }
        }
    }
}
@Composable
private fun ProtocolCard(p: Protocol, onClick: () -> Unit, onUpgrade: () -> Unit) {
    val accent  = p.accent.toColor()
    val coming  = p.status == ProtocolStatus.COMING_SOON
    val isPaid  = p.status == ProtocolStatus.PAID

    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !coming && !isPaid, onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Text(p.icon, fontSize = 28.sp)
                when (p.status) {
                    ProtocolStatus.FREE        -> GreenChip("Free")
                    ProtocolStatus.PAID        -> OrangeChip(p.price)
                    ProtocolStatus.COMING_SOON -> NdChip("Coming Soon")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(p.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(p.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                p.compounds.take(4).forEach { NdChip(it) }
                if (p.compounds.size > 4) NdChip("+${p.compounds.size - 4}")
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("\uD83C\uDFAF ${p.goal}  \u00B7  \u23F1 ${p.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isPaid) {
                    NdButton("Upgrade", onClick = onUpgrade, modifier = Modifier.height(32.dp))
                } else if (!coming) {
                    Text("View \u2192", style = MaterialTheme.typography.labelMedium, color = NdOrange)
                }
            }
        }

        // Accent top bar
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(accent)
        )

        // Coming-soon overlay
        if (coming) {
            Box(
                Modifier.matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.surface) {
                    Text("Coming Soon", style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
        }

        // Paid overlay
        if (isPaid) {
            Box(
                Modifier.matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔒 Premium Protocol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Upgrade to access", style = MaterialTheme.typography.bodyMedium)
                    NdButton("Upgrade Now", onClick = onUpgrade)
                }
            }
        }
    }
}

// - Detail sheet -
@Composable
private fun ProtocolDetailSheet(p: Protocol, onLoad: () -> Unit, onDismiss: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(p.icon, fontSize = 32.sp)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("\uD83C\uDFAF ${p.goal}  \u00B7  \u23F1 ${p.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when (p.status) {
                ProtocolStatus.FREE -> GreenChip("Free")
                ProtocolStatus.PAID -> OrangeChip(p.price)
                else -> {}
            }
        }
        Text(p.detailText, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            p.compounds.forEach { NdChip(it) }
        }
        Spacer(Modifier.height(4.dp))

        // Only show Load button for Free protocols or Coming Soon
        if (p.status == ProtocolStatus.FREE || p.status == ProtocolStatus.COMING_SOON) {
            if (p.presetEntries.isNotEmpty()) NdButton("Load into My Stack \u2192", onClick = onLoad)
        } else {
            // Show upgrade message for Paid protocols
            NdCard {
                Text("🔒 This is a Premium Protocol", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Upgrade to Premium to access this protocol and unlock all premium features.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        NdOutlineButton("Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ProtocolAccent.toColor(): Color = when (this) {
    ProtocolAccent.ORANGE -> NdOrange
    ProtocolAccent.GREEN  -> NdGreen
    ProtocolAccent.BLUE   -> NdBlue
    ProtocolAccent.PURPLE -> NdPurple
}

@Composable
private fun ProductCard(product: Product, onPurchase: () -> Unit) {
    NdCard {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header with image and name
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                // Product image placeholder (if available)
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (product.image.isNotEmpty()) {
                        Text("📄", fontSize = 36.sp)
                    } else {
                        Text("📚", fontSize = 36.sp)
                    }
                }

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(product.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(product.priceformatted, style = MaterialTheme.typography.titleSmall, color = NdOrange, fontWeight = FontWeight.Bold)
                }
            }

            // Description
            if (product.description.isNotEmpty()) {
                Text(product.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Features
            if (product.features.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Features:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    product.features.forEach { feature ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("✓", color = NdGreen, fontSize = 14.sp)
                            Text(feature, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NdOutlineButton(
                    "Learn More",
                    onClick = { /* TODO: Open product details */ },
                    modifier = Modifier.weight(1f)
                )
                NdButton(
                    "Purchase",
                    onClick = onPurchase,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
