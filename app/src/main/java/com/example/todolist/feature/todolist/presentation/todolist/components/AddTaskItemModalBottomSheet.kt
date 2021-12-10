package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.ScrimColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun AddTaskItemModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    focusRequester: FocusRequester,
    shouldShowMainBottomSheetScaffold: MutableState<Boolean>,
    textField: @Composable () -> Unit,
) {

    val isShowing = state.targetValue != ModalBottomSheetValue.Hidden

    val keyboardController = LocalSoftwareKeyboardController.current

    val bringIntoViewRequester = BringIntoViewRequester()
    if (isShowing) {
        SideEffect {
            focusRequester.requestFocus()
            scope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
        shouldShowMainBottomSheetScaffold.value = false
    } else {
        shouldShowMainBottomSheetScaffold.value = true
        keyboardController?.hide()
        LocalFocusManager.current.clearFocus()
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
        modifier = Modifier.
            bringIntoViewRequester(bringIntoViewRequester),
        sheetState = state,
        scrimColor = ScrimColor,
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(16.dp)
            ) {
                textField()
            }
        },
    ) {

    }
}
