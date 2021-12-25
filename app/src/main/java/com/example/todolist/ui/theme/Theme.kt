package com.example.todolist.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// White도 정의
private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,
    surface = Color.DarkGray,
    onSurface = Color.White,
    onBackground = Color.White,
    background = LightBlack
)

private val LightColorPalette = lightColors(
    background = Color.White,
    onBackground = Color.Black,
    onSecondary = Color.Black,
    onPrimary = Color.Black,
    onSurface = Color.Gray,
    primary = Color.White



    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

val themedBlue
    @Composable
    get() = if(isSystemInDarkTheme()) LightBlue else Blue

val themedGray
    @Composable
    get() = if(isSystemInDarkTheme()) Color.LightGray else Color.Gray

@Composable
fun TodoListTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val systemUiController = rememberSystemUiController()

    lateinit var colors : Colors
    lateinit var typography: Typography
    if(darkTheme){
        colors = DarkColorPalette
        typography = TypographyInDarkTheme
        systemUiController.setNavigationBarColor(
            color = Color.DarkGray,
            navigationBarContrastEnforced = false
        )
    }else{
        colors = LightColorPalette
        typography = TypographyInLightTheme
        systemUiController.setNavigationBarColor(
            color = Color.White,
            navigationBarContrastEnforced = false
        )
    }
    systemUiController.setStatusBarColor(
        color = Color.Transparent
    )

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}