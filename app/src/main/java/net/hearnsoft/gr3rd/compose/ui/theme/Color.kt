package net.hearnsoft.gr3rd.compose.ui.theme

import androidx.compose.ui.graphics.Color

// 基础颜色
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// 主色调 (Reimu 色系) - 中国应用常用的暖红色
val Primary = Color(0xFFEF5A5A) // reimu-600
val PrimaryDark = Color(0xFFBD2631) // reimu-800
val PrimaryLight = Color(0xFFFF9F9A) // reimu-400
val PrimaryVariant = Color(0xFFD83B42) // reimu-700
val PrimarySoft = Color(0xFFFFD5D1) // reimu-300
val PrimarySoftSecond = Color(0xFFFFF0EF) // reimu-100

// 次要色 (Marisa 色系) - 温暖的金黄色
val Secondary = Color(0xFFB6832E) // marisa-600
val SecondaryDark = Color(0xFF885907) // marisa-800
val SecondaryLight = Color(0xFFD7B568) // marisa-400

// 背景色系
val Background = Color(0xFFFFFFFF)
val Surface = Color(0xFFF3F4F5) // slate-100
val SurfaceVariant = Color(0xFFE8EAED) // slate-200

// 文字颜色
val TextPrimary = Color(0xFF32363F) // slate-1000
val TextSecondary = Color(0xFF707783) // slate-700
val TextHint = Color(0xFF9DA2AB) // slate-500
val TextOnPrimary = Color(0xFFFFFFFF)

// 分割线和边框
val Divider = Color(0xFFDDDFE3) // slate-300
val Outline = Color(0xFFB5B9C0) // slate-400

// 功能色
val Error = Color(0xFFD83B42) // reimu-700
val Success = Color(0xFF1CA247) // green-600
val Warning = Color(0xFFB6832E) // marisa-600
val Info = Color(0xFF5986FF) // blue-600

// 特殊用途
val IconBackground = Color(0xFFEF5A5A)
val RippleColor = Color(0x33FF9F9A) // primary_light的20%透明度
val CardBackground = PrimarySoft
val ToolbarBackground = Color(0xFFFFFFFF)

// 暗色主题颜色
val DarkPrimary = Color(0xFFFF9F9A) // 在暗色主题中使用更亮的主色
val DarkPrimaryDark = Color(0xFFEF5A5A)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val DarkTextPrimary = Color(0xFFE1E3E6)
val DarkTextSecondary = Color(0xFFB3B7C0)
val DarkDivider = Color(0xFF3C4043)
val DarkOutline = Color(0xFF5F6368)

// 保留原有的Material3颜色用于兼容
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)