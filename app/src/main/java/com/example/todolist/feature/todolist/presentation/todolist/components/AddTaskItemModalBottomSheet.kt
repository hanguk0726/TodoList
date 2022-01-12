package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.todolist.common.ui.theme.ScrimColor
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun AddTaskItemModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    focusRequester: FocusRequester,
    textField: @Composable () -> Unit,
    addButton: @Composable () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val isShowing = state.targetValue == ModalBottomSheetValue.Expanded

    LaunchedEffect(key1 = isShowing) {
        if (isShowing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    BackHandler(
        enabled = isShowing,
        onBack = {
            scope.launch {
                keyboardController?.hide()
                state.hide()
            }
        }
    )

    ModalBottomSheetLayout(
        modifier = Modifier.navigationBarsWithImePadding(),
        sheetState = state,
        sheetElevation = 0.dp,
        scrimColor = ScrimColor,
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(16.dp)
            ) {
                textField()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                addButton()
            }
        },
    ) {

    }
}
