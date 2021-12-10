package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.todolist.components.AddTaskItemModalBottomSheet
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val taskItemContentState = viewModel.taskItemContent.value
    val taskListNameState = viewModel.taskListName.value


    val mainBottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val shouldShowMainBottomSheetScaffold = remember { mutableStateOf(value = false) }

    val coroutineScope = rememberCoroutineScope()

    val addTaskItemModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val addTaskItemFocusRequester = remember { FocusRequester() }

    AnimatedVisibility(
        visible = shouldShowMainBottomSheetScaffold.value,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
    ) {
        BottomSheetScaffold(
            sheetContent = {

            },
            sheetShape = RectangleShape,
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (addTaskItemModalBottomSheetState.isVisible) {
                                addTaskItemModalBottomSheetState.hide()
                            } else {
                                addTaskItemModalBottomSheetState.show()
                            }
                        }
                    }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "add new task item")
                }
            },
            scaffoldState = mainBottomSheetScaffoldState
        ) {
        }
    }


    val keyboardController = LocalSoftwareKeyboardController.current

    AddTaskItemModalBottomSheet(
        scope = coroutineScope,
        state = addTaskItemModalBottomSheetState,
        focusRequester = addTaskItemFocusRequester,
        shouldShowMainBottomSheetScaffold = shouldShowMainBottomSheetScaffold,
        textField = {
            TransparentHintTextField(
                text = taskItemContentState.text,
                hint = taskItemContentState.hint,
                onValueChange = {
                    viewModel.onEvent(TodoListEvent.EnterTaskItemContent(it))
                },
                isHintVisible = taskItemContentState.isHintVisible,
                textStyle = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxHeight()
                    .focusRequester(addTaskItemFocusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {keyboardController?.hide()})
            )
        }
    )
}