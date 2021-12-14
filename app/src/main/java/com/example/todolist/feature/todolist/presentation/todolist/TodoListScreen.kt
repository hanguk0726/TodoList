package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.animation.*
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.todolist.components.AddTaskItemModalBottomSheet
import com.example.todolist.feature.todolist.presentation.todolist.components.MenuModalBottomSheet
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.ui.theme.LightBlack
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    rememberSystemUiController().run{
        setNavigationBarColor(
            color = Color.DarkGray
        )
        setStatusBarColor(
            color = Color.Transparent
        )
    }

    val taskItemContentState = viewModel.taskItemContent.value

    val taskListsState = viewModel.taskListsState.value

    val mainScaffoldState = rememberScaffoldState()
    val shouldShowMainBottomSheetScaffold = remember { mutableStateOf(value = true) }

    val scope = rememberCoroutineScope()

    val addTaskItemModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val addTaskItemFocusRequester = remember { FocusRequester() }

    val menuModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    LaunchedEffect(key1 = true) {
        menuModalBottomSheetState.hide()
    }

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
            backgroundColor = LightBlack,
            scaffoldState = mainScaffoldState,
            topBar = {
                Spacer(modifier = Modifier
                    .statusBarsHeight()
                    .fillMaxWidth()
                )
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
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                    Text("Task",
                    Modifier
                        .fillMaxWidth(),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center)
                }
                SemiTransparentDivider()
                LazyRow {
                    items(taskListsState.taskLists) {
                        taskList ->

                        // tab layouy view pager
                    }
                }
            }
        }
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
                modifier = Modifier.clickable {
                    navController.navigate(Screen.AddEditTaskListScreen.route)
                },
                icon = { Icon(Icons.Filled.Add, "add new task list") },
                text = { Text("새 목록 만들기") })
        }
    )

}