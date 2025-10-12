package com.wisme.research.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Responsive design utilities for adapting UI to different screen sizes
 * 
 * Screen size categories:
 * - SMALL: < 600dp width (phones in portrait)
 * - MEDIUM: 600-840dp width (large phones, small tablets)
 * - LARGE: > 840dp width (tablets, foldables)
 */

enum class ScreenSize {
    SMALL,      // < 600dp width (phones in portrait)
    MEDIUM,     // 600-840dp width (large phones, small tablets)
    LARGE       // > 840dp width (tablets, foldables)
}

@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    
    return when {
        screenWidthDp < 600.dp -> ScreenSize.SMALL
        screenWidthDp < 840.dp -> ScreenSize.MEDIUM
        else -> ScreenSize.LARGE
    }
}

/**
 * Responsive text styles based on screen size and Material Design typography
 */
object ResponsiveTextStyles {
    
    @Composable
    fun displayLarge(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.headlineLarge
            ScreenSize.MEDIUM -> MaterialTheme.typography.displaySmall
            ScreenSize.LARGE -> MaterialTheme.typography.displayMedium
        }
    }
    
    @Composable
    fun displaySmall(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.headlineMedium
            ScreenSize.MEDIUM -> MaterialTheme.typography.headlineLarge
            ScreenSize.LARGE -> MaterialTheme.typography.displaySmall
        }
    }
    
    @Composable
    fun headlineLarge(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.headlineMedium
            ScreenSize.MEDIUM -> MaterialTheme.typography.headlineLarge
            ScreenSize.LARGE -> MaterialTheme.typography.displaySmall
        }
    }
    
    @Composable
    fun headingLarge(): TextStyle {
        return headlineLarge()
    }
    
    @Composable
    fun heading(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.titleLarge
            ScreenSize.MEDIUM -> MaterialTheme.typography.headlineSmall
            ScreenSize.LARGE -> MaterialTheme.typography.headlineMedium
        }
    }

    @Composable
    fun headlineMedium(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.headlineSmall
            ScreenSize.MEDIUM -> MaterialTheme.typography.headlineMedium
            ScreenSize.LARGE -> MaterialTheme.typography.headlineLarge
        }
    }

    @Composable
    fun titleLarge(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.titleMedium
            ScreenSize.MEDIUM -> MaterialTheme.typography.titleLarge
            ScreenSize.LARGE -> MaterialTheme.typography.headlineSmall
        }
    }

    @Composable
    fun titleMedium(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.titleSmall
            ScreenSize.MEDIUM -> MaterialTheme.typography.titleMedium
            ScreenSize.LARGE -> MaterialTheme.typography.titleLarge
        }
    }

    @Composable
    fun bodyLarge(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.bodyMedium
            ScreenSize.MEDIUM -> MaterialTheme.typography.bodyLarge
            ScreenSize.LARGE -> MaterialTheme.typography.titleMedium
        }
    }

    @Composable
    fun bodyMedium(): TextStyle {
        return MaterialTheme.typography.bodyMedium
    }
    
    @Composable
    fun body(): TextStyle {
        return bodyMedium()
    }

    @Composable
    fun bodySmall(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.labelLarge
            ScreenSize.MEDIUM -> MaterialTheme.typography.bodySmall
            ScreenSize.LARGE -> MaterialTheme.typography.bodyMedium
        }
    }

    @Composable
    fun labelLarge(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.labelMedium
            ScreenSize.MEDIUM -> MaterialTheme.typography.labelLarge
            ScreenSize.LARGE -> MaterialTheme.typography.bodySmall
        }
    }
    
    @Composable
    fun caption(): TextStyle {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> MaterialTheme.typography.labelSmall
            ScreenSize.MEDIUM -> MaterialTheme.typography.labelMedium
            ScreenSize.LARGE -> MaterialTheme.typography.labelLarge
        }
    }
}

/**
 * Responsive font sizes for when you need TextUnit values
 */
object ResponsiveFontSizes {
    
    @Composable
    fun displayLarge(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 40.sp
            ScreenSize.MEDIUM -> 48.sp
            ScreenSize.LARGE -> 56.sp
        }
    }
    
    @Composable
    fun displaySmall(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 32.sp
            ScreenSize.MEDIUM -> 36.sp
            ScreenSize.LARGE -> 40.sp
        }
    }
    
    @Composable
    fun headlineLarge(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 28.sp
            ScreenSize.MEDIUM -> 32.sp
            ScreenSize.LARGE -> 36.sp
        }
    }
    
    @Composable
    fun headingLarge(): TextUnit {
        return headlineLarge()
    }
    
    @Composable
    fun heading(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 20.sp
            ScreenSize.MEDIUM -> 22.sp
            ScreenSize.LARGE -> 24.sp
        }
    }

    @Composable
    fun headlineMedium(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 24.sp
            ScreenSize.MEDIUM -> 26.sp
            ScreenSize.LARGE -> 28.sp
        }
    }

    @Composable
    fun titleLarge(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 20.sp
            ScreenSize.MEDIUM -> 22.sp
            ScreenSize.LARGE -> 24.sp
        }
    }

    @Composable
    fun titleMedium(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 16.sp
            ScreenSize.MEDIUM -> 18.sp
            ScreenSize.LARGE -> 20.sp
        }
    }

    @Composable
    fun bodyLarge(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 16.sp
            ScreenSize.MEDIUM -> 17.sp
            ScreenSize.LARGE -> 18.sp
        }
    }

    @Composable
    fun bodyMedium(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 14.sp
            ScreenSize.MEDIUM -> 15.sp
            ScreenSize.LARGE -> 16.sp
        }
    }
    
    @Composable
    fun body(): TextUnit {
        return bodyMedium()
    }

    @Composable
    fun bodySmall(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 12.sp
            ScreenSize.MEDIUM -> 13.sp
            ScreenSize.LARGE -> 14.sp
        }
    }

    @Composable
    fun labelLarge(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 14.sp
            ScreenSize.MEDIUM -> 15.sp
            ScreenSize.LARGE -> 16.sp
        }
    }
    
    @Composable
    fun caption(): TextUnit {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 10.sp
            ScreenSize.MEDIUM -> 11.sp
            ScreenSize.LARGE -> 12.sp
        }
    }
}

/**
 * Responsive spacing values
 */
object ResponsiveSpacing {
    
    @Composable
    fun extraSmall(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 4.dp
            ScreenSize.MEDIUM -> 6.dp
            ScreenSize.LARGE -> 8.dp
        }
    }
    
    @Composable
    fun small(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 8.dp
            ScreenSize.MEDIUM -> 12.dp
            ScreenSize.LARGE -> 16.dp
        }
    }

    @Composable
    fun medium(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 16.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.LARGE -> 24.dp
        }
    }

    @Composable
    fun large(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 24.dp
            ScreenSize.MEDIUM -> 32.dp
            ScreenSize.LARGE -> 40.dp
        }
    }

    @Composable
    fun extraLarge(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 32.dp
            ScreenSize.MEDIUM -> 48.dp
            ScreenSize.LARGE -> 64.dp
        }
    }

    @Composable
    fun horizontal(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 16.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.LARGE -> 32.dp
        }
    }

    @Composable
    fun vertical(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.SMALL -> 16.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.LARGE -> 32.dp
        }
    }
}

/**
 * Screen dimension utilities
 */
@Composable
fun screenWidthDp(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

@Composable
fun screenHeightDp(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}

/**
 * Get percentage of screen width
 */
@Composable
fun widthPercent(percent: Float): Dp {
    return (screenWidthDp() * percent)
}

/**
 * Get percentage of screen height
 */
@Composable
fun heightPercent(percent: Float): Dp {
    return (screenHeightDp() * percent)
}
