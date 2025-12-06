package com.dergoogler.mmrl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dergoogler.mmrl.ui.component.StatusBarStyle
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.token.LocalTypography

@Composable
fun MMRLAppTheme(
    themeColor: Int,
    darkMode: Boolean = isSystemInDarkTheme(),
    navController: NavHostController = rememberNavController(),
    providerValues: Array<ProvidedValue<*>>? = null,
    content: @Composable () -> Unit,
) {
    val color = Colors.getColor(id = themeColor)
    val colorScheme =
        when {
            darkMode -> color.darkColorScheme
            else -> color.lightColorScheme
        }

    StatusBarStyle(
        darkMode = darkMode,
    )

    if (providerValues != null) {
        CompositionLocalProvider(
            LocalNavController provides navController,
            LocalTypography provides MaterialTheme.typography,
            *providerValues,
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                shapes = Shapes,
                typography = Typography,
                content = content,
            )
        }
    } else {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = Shapes,
            typography = Typography,
            content = content,
        )
    }
}
