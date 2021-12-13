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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.ScrimColor
import com.google.accompanist.insets.imePadding
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
    addButton: @Composable () -> Unit
) {
    val isShowing = state.targetValue != ModalBottomSheetValue.Hidden

    val keyboardController = LocalSoftwareKeyboardController.current

    if (isShowing) {
        shouldShowMainBottomSheetScaffold.value = false
        SideEffect {
            focusRequester.requestFocus()
        }
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
        modifier = Modifier.imePadding(),
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
