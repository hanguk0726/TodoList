package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.animation.*
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.todolist.components.AddTaskItemModalBottomSheet
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.launch

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


    val mainScaffoldState = rememberScaffoldState()
    val shouldShowMainBottomSheetScaffold = remember { mutableStateOf(value = true) }

    val coroutineScope = rememberCoroutineScope()

    val addTaskItemModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val addTaskItemFocusRequester = remember { FocusRequester() }

    AnimatedVisibility(
        visible = shouldShowMainBottomSheetScaffold.value,
        enter = fadeIn() + slideIn(
            animationSpec = TweenSpec(durationMillis = 200)
        ) { fullSize ->
            IntOffset(
                0,
                fullSize.height
            )
        },
        exit = fadeOut() + slideOut(
            animationSpec = TweenSpec(durationMillis = 200)
        ) { fullSize ->
            IntOffset(
                0,
                fullSize.height
            )
        },
    ) {
        Scaffold(
            scaffoldState = mainScaffoldState,
            content = {
            },
            topBar = {
                Spacer(modifier = Modifier.statusBarsPadding())
            },
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
                    },
                ) {
                    Icon(Icons.Filled.Add, "add new task item")
                }
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.Center,

            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.navigationBarsPadding(),
                    cutoutShape = RoundedCornerShape(50),
                    elevation = 0.dp,
                    content = {
                    },
                )
            }
        )
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
                    onDone = { keyboardController?.hide() })
            )
        },
        actionButton = {
            PureTextButton(text = "저장", textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f), onClick = {})
        }
    )
}