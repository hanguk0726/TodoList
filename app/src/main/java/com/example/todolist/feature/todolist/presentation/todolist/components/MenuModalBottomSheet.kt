package com.example.todolist.feature.todolist.presentation.todolist.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.ScrimColor
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.coroutines.CoroutineScope

@ExperimentalMaterialApi
@Composable
fun MenuModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    items: List<@Composable () -> Unit>
) {
    ModalBottomSheetLayout(
        modifier = Modifier
            .navigationBarsPadding(),
        sheetState = state,
        sheetElevation = 0.dp,
        scrimColor = ScrimColor,
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(8.dp)
            ) {
                items.forEach {
                    item -> item()
                }
            }
        },
    ) {

    }
}