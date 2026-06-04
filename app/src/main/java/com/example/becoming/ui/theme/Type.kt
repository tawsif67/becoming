package com.example.becoming.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.becoming.R

// Medieval Fonts
val CinzelFont = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_black, FontWeight.Black),
)
val TypewriterFont = FontFamily(Font(R.font.special_elite))

// Medieval Typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)
