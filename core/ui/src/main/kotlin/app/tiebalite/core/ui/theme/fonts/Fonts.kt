package app.tiebalite.core.ui.theme.fonts

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.tiebalite.core.ui.R

private val HarmonySans = FontFamily(
    Font(R.font.harmonyos_sans_regular, FontWeight.Normal),
    Font(R.font.harmonyos_sans_medium, FontWeight.Medium),
    Font(R.font.harmonyos_sans_semibold, FontWeight.SemiBold),
    Font(R.font.harmonyos_sans_bold, FontWeight.Bold)
)

val Typography = Typography().copy(
    displayLarge = Typography().displayLarge.copy(fontFamily = HarmonySans),
    displayMedium = Typography().displayMedium.copy(fontFamily = HarmonySans),
    displaySmall = Typography().displaySmall.copy(fontFamily = HarmonySans),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = HarmonySans),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = HarmonySans),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = HarmonySans),
    titleLarge = Typography().titleLarge.copy(fontFamily = HarmonySans),
    titleMedium = Typography().titleMedium.copy(fontFamily = HarmonySans),
    titleSmall = Typography().titleSmall.copy(fontFamily = HarmonySans),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = HarmonySans),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = HarmonySans),
    bodySmall = Typography().bodySmall.copy(fontFamily = HarmonySans),
    labelLarge = Typography().labelLarge.copy(fontFamily = HarmonySans),
    labelMedium = Typography().labelMedium.copy(fontFamily = HarmonySans),
    labelSmall = Typography().labelSmall.copy(fontFamily = HarmonySans)
)
