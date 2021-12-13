package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.animation.*
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.todolist.components.AddTaskItemModalBottomSheet
import com.example.todolist.feature.todolist.presentation.todolist.components.MenuModalBottomSheet
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

    val mainScaffoldState = rememberScaffoldState()
    val shouldShowMainBottomSheetScaffold = remember { mutableStateOf(value = true) }

    val scope = rememberCoroutineScope()

    val addTaskItemModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val addTaskItemFocusRequester = remember { FocusRequester() }

    val menuModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

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
                        scope.launch {
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
                        IconButton(onClick = {
                          scope.launch {
                              menuModalBottomSheetState.show()
                          }
                        }) {
                          Icon(Icons.Default.Menu, "show menu")
                        }
                    },
                )
            }
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    AddTaskItemModalBottomSheet(
        scope = scope,
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
        addButton = {
            PureTextButton(text = "저장", textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f), onClick = {})
        }
    )


    MenuModalBottomSheet(
        scope = scope,
        state = menuModalBottomSheetState,
        items = listOf {
            ListItem(
                //새 목록 만들기 페이지로 포워드
                modifier = Modifier.clickable {

                },
                icon = { Icon(Icons.Filled.Add, "add new task list") },
                text = { Text("새 목록 만들기") })
        }
    )

}