package com.petcare.calendar.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.petcare.calendar.R
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.core.theme.*

@Composable
fun PawGradientBackground(mode: Int = 0, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        val stops = when (mode) {
            1 -> listOf(SplashTop.copy(alpha = 0.92f), SplashBottom.copy(alpha = 0.96f))
            2 -> listOf(Background.copy(alpha = 0.96f), SurfaceWhite.copy(alpha = 0.94f), BackgroundLower.copy(alpha = 0.97f))
            3 -> listOf(Secondary.copy(alpha = 0.96f), Background.copy(alpha = 0.98f), SurfaceWhite.copy(alpha = 0.96f))
            else -> listOf(Background.copy(alpha = 0.96f), BackgroundLower.copy(alpha = 0.94f), Secondary.copy(alpha = 0.94f))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(stops)))
        Image(painterResource(R.drawable.logo), null, Modifier.align(Alignment.TopEnd).padding(top = 80.dp).size(180.dp), alpha = if (mode == 1) 0.06f else 0.035f, contentScale = ContentScale.Fit)
        content()
    }
}

@Composable
fun CareLayout(title: Int, subtitle: String? = null, onBack: (() -> Unit)? = null, tab: Int? = null, onNavigate: (Screen) -> Unit = {}, mode: Int = 0, stickyFooter: Boolean = false, actions: @Composable RowScope.() -> Unit = {}, footer: @Composable ColumnScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    PawGradientBackground(mode) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            BoxWithConstraints(Modifier.weight(1f).widthIn(max = 1100.dp).fillMaxWidth()) {
                val available = maxHeight
                val wide = maxWidth > maxHeight * 1.3f
                if (stickyFooter && wide) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CareBody(Modifier.weight(1f), title, subtitle, onBack, actions, content)
                        CareFooter(Modifier.weight(0.85f), available, footer)
                    }
                } else {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CareBody(Modifier.weight(1f).widthIn(max = 680.dp), title, subtitle, onBack, actions) {
                            content()
                            if (!stickyFooter) footer()
                        }
                        if (stickyFooter) CareFooter(Modifier.widthIn(max = 680.dp), available * 0.54f, footer)
                    }
                }
            }
            if (tab != null) AppBottomBar(tab, onNavigate)
        }
    }
}

@Composable
private fun CareBody(modifier: Modifier, title: Int, subtitle: String?, onBack: (() -> Unit)?, actions: @Composable RowScope.() -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.brand_eyebrow), style = MaterialTheme.typography.labelSmall, color = PrimaryDeep)
                Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
            }
            if (onBack != null) ActionText(R.string.back, onBack)
            actions()
        }
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        content()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CareFooter(modifier: Modifier, available: Dp, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), color = SurfaceWhite.copy(alpha = 0.96f), shadowElevation = 8.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(Modifier.fillMaxWidth().heightIn(max = available).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun AppBottomBar(selected: Int, onNavigate: (Screen) -> Unit) {
    val labels = listOf(R.string.pets_tab, R.string.today_tab, R.string.schedule_tab, R.string.vet_tab)
    val icons = listOf(R.drawable.icon_home, R.drawable.sprite_1, R.drawable.sprite_4, R.drawable.sprite_5)
    NavigationBar(containerColor = SurfaceWhite.copy(alpha = 0.96f), tonalElevation = 5.dp, windowInsets = WindowInsets(0, 0, 0, 0)) {
        labels.forEachIndexed { index, label ->
            NavigationBarItem(selected == index, onClick = {
                onNavigate(when (index) { 0 -> Screen.Menu; 1 -> Screen.Game(); 2 -> Screen.Schedule; else -> Screen.VetVisits })
            }, icon = { Image(painterResource(icons[index]), null, Modifier.size(25.dp), contentScale = ContentScale.Fit) }, label = { Text(stringResource(label), fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(indicatorColor = BackgroundLower, selectedTextColor = PrimaryDeep, unselectedTextColor = TextSecondary))
        }
    }
}
