package com.stardell.parpilotai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.stardell.parpilotai.viewmodel.AppTheme

// Custom Theme Colors Object
data class ParPilotColors(
    val actionTeal: Color,
    val headerText: Color,
    val backgroundStart: Color,
    val backgroundEnd: Color
)

val LocalParPilotColors = staticCompositionLocalOf {
    ParPilotColors(
        actionTeal = ClassicGreenDark,
        headerText = DarkGoldText,
        backgroundStart = ClassicGreenDark,
        backgroundEnd = ClassicGreenLight
    )
}

// We just map the Material3 colors loosely to dark mode since Par Pilot is entirely custom dark themed
private val DarkColorScheme = darkColorScheme(
    primary = ClassicGreenDark,
    secondary = ClassicGreenLight,
    background = Color.Black,
    surface = Color.DarkGray,
)

@Composable
fun ParPilotAITheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    appStyleTheme: AppStyleTheme = AppStyleTheme.CLASSIC_GREEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We only use the darkTheme toggle for baseline Material component backgrounds, 
    // but the Par Pilot identity relies entirely on the Custom ParPilotColors.
    
    val colors = when (appStyleTheme) {
        AppStyleTheme.DARK_GOLD -> ParPilotColors(
            actionTeal = DarkGoldText,
            headerText = DarkGoldText,
            backgroundStart = DarkGoldBackgroundStart,
            backgroundEnd = DarkGoldBackgroundEnd
        )
        AppStyleTheme.PRO_TOUR_BLUE -> ParPilotColors(
            actionTeal = PgaTourRed,
            headerText = Color.White,
            backgroundStart = PgaTourBlue,
            backgroundEnd = ProTourBlueBackgroundEnd
        )
        AppStyleTheme.CLASSIC_GREEN -> ParPilotColors(
            actionTeal = ClassicGreenDark,
            headerText = DarkGoldText,
            backgroundStart = ClassicGreenDark,
            backgroundEnd = ClassicGreenLight
        )
    }

    CompositionLocalProvider(LocalParPilotColors provides colors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}