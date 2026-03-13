package com.noodrop.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.ui.common.NdButton
import com.noodrop.app.ui.theme.NdOrange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// - ViewModel -
data class OnboardingState(
    val currentPage: Int = 0,
    val isCompleted: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextPage(totalPages: Int) {
        val next = _state.value.currentPage + 1
        if (next >= totalPages) {
            completeOnboarding()
        } else {
            _state.update { it.copy(currentPage = next) }
        }
    }


    fun setPage(page: Int) {
        _state.update { it.copy(currentPage = page) }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            // Mark onboarding as completed in user preferences
            _state.update { it.copy(isCompleted = true) }
        }
    }
}

// - Onboarding Data -
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: String,
    val features: List<String> = emptyList(),
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Welcome to Noodrop",
        subtitle = "Your Personal Nootropic Journey",
        description = "Discover evidence-based nootropic optimization tailored to your unique needs.",
        icon = "🧠",
        features = listOf(
            "Personalized compound recommendations",
            "Advanced tracking & analytics",
            "Evidence-based protocols"
        )
    ),
    OnboardingPage(
        title = "Build Your Stack",
        subtitle = "Create Your Protocol",
        description = "Combine nootropic compounds based on scientific research and your goals.",
        icon = "🧪",
        features = listOf(
            "100+ compounds database",
            "Dosage optimization",
            "Timing recommendations"
        )
    ),
    OnboardingPage(
        title = "Track & Optimize",
        subtitle = "Monitor Your Progress",
        description = "Log daily metrics and see how your stack affects your cognitive performance.",
        icon = "📊",
        features = listOf(
            "Daily mood & focus tracking",
            "Performance analytics",
            "Personalized insights"
        )
    ),
    OnboardingPage(
        title = "Science-Backed",
        subtitle = "Evidence-Based Approach",
        description = "All recommendations are backed by peer-reviewed research and clinical studies.",
        icon = "🔬",
        features = listOf(
            "PubMed research links",
            "Clinical study references",
            "Safety information"
        )
    ),
)

// - Main Onboarding Screen -
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    // VM → Pager: when VM page changes (e.g. from Next button), animate pager
    LaunchedEffect(s.currentPage) {
        if (pagerState.currentPage != s.currentPage) {
            pagerState.animateScrollToPage(s.currentPage)
        }
    }

    // Pager → VM: when user swipes manually, sync VM
    LaunchedEffect(pagerState.currentPage) {
        vm.setPage(pagerState.currentPage)
    }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            OnboardingPageContent(
                page = onboardingPages[page],
                isLastPage = page == onboardingPages.lastIndex,
                onNext = {
                    if (page == onboardingPages.lastIndex) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(page + 1)
                            vm.setPage(page + 1)
                        }
                    }
                },
                onSkip = onComplete,
            )
        }

        // Page indicators
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(onboardingPages.size) { index ->
                val isSelected = index == s.currentPage
                Box(
                    Modifier
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) NdOrange
                            else NdOrange.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isLastPage: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Animated Icon
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NdOrange.copy(alpha = 0.2f),
                            NdOrange.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                page.icon,
                style = MaterialTheme.typography.displayLarge,
                color = NdOrange,
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            page.title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            page.subtitle,
            style = MaterialTheme.typography.titleLarge,
            color = NdOrange,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        // Features
        if (page.features.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                page.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("✅", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NdButton(
                text = if (isLastPage) "Get Started" else "Next",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(0.8f),
            )

            if (!isLastPage) {
                TextButton(onClick = onSkip) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
