package com.dergoogler.mmrl.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.mmrl.ui.theme.color.AlmondBlossomDarkScheme
import com.dergoogler.mmrl.ui.theme.color.AlmondBlossomLightScheme
import com.dergoogler.mmrl.ui.theme.color.JeufosseDarkScheme
import com.dergoogler.mmrl.ui.theme.color.JeufosseLightScheme
import com.dergoogler.mmrl.ui.theme.color.MMRLBaseDarkScheme
import com.dergoogler.mmrl.ui.theme.color.MMRLBaseLightScheme
import com.dergoogler.mmrl.ui.theme.color.PlainAuversDarkScheme
import com.dergoogler.mmrl.ui.theme.color.PlainAuversLightScheme
import com.dergoogler.mmrl.ui.theme.color.PoppyFieldDarkScheme
import com.dergoogler.mmrl.ui.theme.color.PoppyFieldLightScheme
import com.dergoogler.mmrl.ui.theme.color.PourvilleDarkScheme
import com.dergoogler.mmrl.ui.theme.color.PourvilleLightScheme
import com.dergoogler.mmrl.ui.theme.color.SoleilLevantDarkScheme
import com.dergoogler.mmrl.ui.theme.color.SoleilLevantLightScheme
import com.dergoogler.mmrl.ui.theme.color.WildRosesDarkScheme
import com.dergoogler.mmrl.ui.theme.color.WildRosesLightScheme

sealed class Colors(
    val id: Int,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
) {
    @RequiresApi(Build.VERSION_CODES.S)
    class Dynamic(context: Context) : Colors(
        id = id,
        lightColorScheme = dynamicLightColorScheme(context),
        darkColorScheme = dynamicDarkColorScheme(context)
    ) {
        companion object {
            @Suppress("ConstPropertyName")
            const val id = -1
        }
    }

    data object Pourville : Colors(
        id = 0,
        lightColorScheme = PourvilleLightScheme,
        darkColorScheme = PourvilleDarkScheme
    )

    data object SoleilLevant : Colors(
        id = 1,
        lightColorScheme = SoleilLevantLightScheme,
        darkColorScheme = SoleilLevantDarkScheme
    )

    data object Jeufosse : Colors(
        id = 2,
        lightColorScheme = JeufosseLightScheme,
        darkColorScheme = JeufosseDarkScheme
    )

    data object PoppyField : Colors(
        id = 3,
        lightColorScheme = PoppyFieldLightScheme,
        darkColorScheme = PoppyFieldDarkScheme
    )

    data object AlmondBlossom : Colors(
        id = 4,
        lightColorScheme = AlmondBlossomLightScheme,
        darkColorScheme = AlmondBlossomDarkScheme
    )

    data object PlainAuvers : Colors(
        id = 5,
        lightColorScheme = PlainAuversLightScheme,
        darkColorScheme = PlainAuversDarkScheme
    )

    data object WildRoses : Colors(
        id = 6,
        lightColorScheme = WildRosesLightScheme,
        darkColorScheme = WildRosesDarkScheme
    )

    data object MMRLBase : Colors(
        id = 7,
        lightColorScheme = MMRLBaseLightScheme,
        darkColorScheme = MMRLBaseDarkScheme
    )

    companion object {
        private val mColors
            get() = listOf(
                Pourville,
                SoleilLevant,
                Jeufosse,
                PoppyField,
                AlmondBlossom,
                PlainAuvers,
                WildRoses,
                MMRLBase
            )

        fun getColorIds(): List<Int> {
            return mColors.map { it.id }
        }

        @JvmStatic
        fun Context.getThemeColor(id: Int): Colors {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && id == Dynamic.id) {
                Dynamic(this)
            } else {
                mColors[id]
            }
        }

        fun Context.getColorScheme(id: Int, darkMode: Boolean): ColorScheme {
            val color = getThemeColor(id)
            return when {
                darkMode -> color.darkColorScheme
                else -> color.lightColorScheme
            }
        }

        @Composable
        fun getColor(id: Int): Colors {
            val context = LocalContext.current
            return context.getThemeColor(id)
        }

        @Composable
        fun getColor(id: Int, darkMode: Boolean = isSystemInDarkTheme()): ColorScheme {
            val color = getColor(id)
            return when {
                darkMode -> color.darkColorScheme
                else -> color.lightColorScheme
            }
        }
    }
}

fun Color.darken(by: Float = 0.3f): Color {
    return copy(
        red = red * by,
        green = green * by,
        blue = blue * by,
        alpha = alpha
    )
}
