package com.example.todolist.feature.todolist.presentation.todolist

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.common.ui.theme.themedBlue
import com.example.todolist.common.util.ConnectionState
import com.example.todolist.common.util.connectivityState
import com.example.todolist.feature.todolist.presentation.components.CustomSnackbarHost
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.editTaskItem.EditTaskItemViewModel
import com.example.todolist.feature.todolist.presentation.todolist.components.*
import com.example.todolist.feature.todolist.presentation.todolist.util.getTargetPage
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.feature.todolist.presentation.util.TaskListsState
import com.example.todolist.feature.todolist.presentation.util.noRippleClickable
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(
    ExperimentalSnapperApi::class,
    ExperimentalPagerApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel(),
    editTaskItemViewModel: EditTaskItemViewModel,
) {

    val mainScaffoldState = rememberScaffoldState()

    val scope = rememberCoroutineScope()

    val recomposeKey = remember { mutableStateOf(0) }

    val showDeleteTaskListDialogState = remember { mutableStateOf(false) }

    val showMainScaffoldBottomAppBar = remember { mutableStateOf(value = true) }

    val addTaskItemFocusRequester = remember { FocusRequester() }

    val addTaskItemModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val menuLeftModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val menuRightModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val pagerState = rememberPagerState(
        initialPage = viewModel.loadLastSelectedTaskListPosition(),
    )
    val taskListsState = viewModel.taskListsState.value

    val currentPageState = { getTargetPage(pagerState) }

    val selectedTaskListId = {
        val isTaskListsLoaded =
            taskListsState.taskLists.size > currentPageState() && currentPageState() >= 0 && taskListsState.taskLists.isNotEmpty()
        if (isTaskListsLoaded) {
            taskListsState.taskLists[currentPageState()].id!!
        } else {
            Log.e("TodoListScreen", "Couldn't get taskList")
            -1L
        }
    }

    //Effects
    ApplyTodoListScreenTheme()
    AlertNetworkConnectionState(mainScaffoldState)
    EmitSelectTaskListTabEvent(pagerState, taskListsState, viewModel, selectedTaskListId)
    DisposeMainScaffoldBottomAppBarVisibility(
        addTaskItemModalBottomSheetState,
        showMainScaffoldBottomAppBar,
        menuLeftModalBottomSheetState,
        menuRightModalBottomSheetState
    )
    DisposeModalBottomSheetState(
        menuLeftModalBottomSheetState, menuRightModalBottomSheetState
    )
    ObserveEditTaskItemEvent(editTaskItemViewModel, mainScaffoldState)
    ObserveUiEvent(
        viewModel,
        mainScaffoldState,
        pagerState,
        addTaskItemModalBottomSheetState,
        showDeleteTaskListDialogState,
        menuRightModalBottomSheetState,
        recomposeKey
    )


    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        scaffoldState = mainScaffoldState,
        snackbarHost = {
            CustomSnackbarHost(it)
        },
        topBar = {
            Spacer(
                modifier = Modifier
                    .statusBarsHeight()
                    .fillMaxWidth()
            )
        },
        floatingActionButton = {
            FadeSlideAnimatedVisibility(showMainScaffoldBottomAppBar) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (addTaskItemModalBottomSheetState.isVisible) {
                                addTaskItemModalBottomSheetState.hide()
                            } else {
                                viewModel.clearTaskItemTitleTextField()
                                addTaskItemModalBottomSheetState.show()
                            }
                        }
                    },
                ) {
                    Icon(
                        Icons.Filled.Add, "add new task item",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },

        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,

        bottomBar = {
            FadeSlideAnimatedVisibility(showMainScaffoldBottomAppBar) {
                BottomAppBar(
                    modifier = Modifier.navigationBarsPadding(),
                    cutoutShape = RoundedCornerShape(50),
                    elevation = if (isSystemInDarkTheme()) 0.dp else AppBarDefaults.BottomAppBarElevation,
                    content = {
                        BottomMenu(
                            scope, menuLeftModalBottomSheetState, menuRightModalBottomSheetState
                        )
                    },
                )
            }
        }
    ) { bottomAppBarPadding ->
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Task",
                    Modifier
                        .fillMaxWidth(),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (taskListsState.taskLists.isNotEmpty()) {
                TodoListScrollableTabRow(
                    viewModel,
                    navController,
                    scope,
                    currentPageState,
                    pagerState,
                    taskListsState
                )
                SemiTransparentDivider()
                TodoListTaskItems(
                    viewModel,
                    pagerState,
                    taskListsState,
                    bottomAppBarPadding,
                    navController,
                    scope,
                    recomposeKey
                )
            }
        }
    }
    TodoListAddTaskItemModalBottomSheet(
        scope,
        addTaskItemModalBottomSheetState,
        addTaskItemFocusRequester,
        viewModel,
        selectedTaskListId
    )

    TodoListMenuLeftModalBottomSheet(
        scope,
        menuLeftModalBottomSheetState,
        viewModel,
        pagerState,
        navController
    )
    if (taskListsState.taskLists.isNotEmpty()) {
        TodoListMenuRightModalBottomSheet(
            viewModel,
            selectedTaskListId,
            menuRightModalBottomSheetState,
            navController,
            taskListsState
        )
    }
    DeleteDialog(
        viewModel,
        scope,
        showDeleteTaskListDialogState,
        menuRightModalBottomSheetState,
        selectedTaskListId
    )

}


@Composable
private fun ApplyTodoListScreenTheme() {
    rememberSystemUiController().run {
        if (isSystemInDarkTheme()) {
            setNavigationBarColor(
                color = Color.DarkGray
            )
        } else {
            setNavigationBarColor(
                color = Color.White
            )
        }
        setStatusBarColor(
            color = Color.Transparent
        )
    }
}

@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun AlertNetworkConnectionState(mainScaffoldState: ScaffoldState) {
    val connection by connectivityState()
    val isDisconnected = connection == ConnectionState.Unavailable

    LaunchedEffect(isDisconnected) {
        if (isDisconnected) {
            mainScaffoldState.snackbarHostState.showSnackbar(
                message = "인터넷에 연결되어 있지 않습니다. 일부 기능은 사용할 수 없으며 다시 온라인 상태가 되면 변경사항이 저장됩니다.",
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun DeleteDialog(
    viewModel: TodoListViewModel,
    scope: CoroutineScope,
    showDeleteTaskListDialogState: MutableState<Boolean>,
    menuRightModalBottomSheetState: ModalBottomSheetState,
    selectedTaskListId: () -> Long
) {
    val dialogTypeState = viewModel.dialogType.value

    if (showDeleteTaskListDialogState.value) {
        val titleText: String
        val contentText: String
        val confirmButtonText = "삭제"
        val eventWhenConfirm: TodoListEvent

        when (dialogTypeState) {
            is TodoListViewModel.DialogType.DeleteTaskList -> {
                titleText = "목록을 삭제하시겠습니까?"
                contentText = "목록에 작성된 모든 할 일이 삭제됩니다. 삭제하시겠습니까?"
                eventWhenConfirm = TodoListEvent.DeleteTaskList(selectedTaskListId())
            }
            is TodoListViewModel.DialogType.DeleteCompletedTaskItem -> {
                titleText = "완료된 할 일을 모두 삭제하시겠습니까?"
                contentText = ""
                eventWhenConfirm = TodoListEvent.DeleteCompletedTaskItems(selectedTaskListId())
            }
        }
        AlertDialog(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(8.dp),
            onDismissRequest = { showDeleteTaskListDialogState.value = false },
            title = { Text(titleText) },
            text = { Text(contentText) },
            confirmButton = {
                PureTextButton(
                    text = confirmButtonText, textColor = themedBlue,
                    paddingValues = PaddingValues(8.dp, 8.dp, 16.dp, 16.dp)
                ) {
                    showDeleteTaskListDialogState.value = false
                    scope.launch {
                        menuRightModalBottomSheetState.hide()
                    }
                    viewModel.onEvent(eventWhenConfirm)
                }
            },
            dismissButton = {
                PureTextButton(
                    text = "취소", textColor = themedBlue,
                    paddingValues = PaddingValues(8.dp, 8.dp, 16.dp, 16.dp)
                ) {
                    showDeleteTaskListDialogState.value = false
                }
            },
        )
    }
}


@ExperimentalPagerApi
@Composable
private fun TodoListScrollableTabRow(
    viewModel: TodoListViewModel,
    navController: NavController,
    scope: CoroutineScope,
    currentPageState: () -> Int,
    pagerState: PagerState,
    taskListsState: TaskListsState
) {
    ScrollableTabRow(
        selectedTabIndex = currentPageState(),
        edgePadding = 8.dp,
        backgroundColor = MaterialTheme.colors.background,
        indicator = @Composable { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier
                    .pagerTabIndicatorOffset(pagerState, tabPositions)
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                        )
                    ),
                color = themedBlue
            )
        }) {
        taskListsState.taskLists.forEachIndexed { index, taskList ->
            val isSelected = currentPageState() == index
            Tab(selected = isSelected, onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }, text = {
                Text(
                    text = taskList.name,
                    color = if (isSelected) themedBlue else MaterialTheme.colors.onSurface
                )
            }, selectedContentColor = themedBlue)
        }
        TextButton(
            onClick = { navController.navigate(Screen.AddEditTaskListScreen.route) },
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Icon(
                Icons.Filled.Add, "add new task list",
                tint = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("새 목록", fontSize = 14.sp)
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalSnapperApi
@ExperimentalPagerApi
@Composable
private fun TodoListTaskItems(
    viewModel: TodoListViewModel,
    pagerState: PagerState,
    taskListsState: TaskListsState,
    bottomAppBarPadding: PaddingValues,
    navController: NavController,
    scope: CoroutineScope,
    recomposeKey: MutableState<Int>
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showCompletedTaskItems = remember { mutableStateOf(value = false) }
    val isShowCompletedTaskItemsButtonEnabled = remember { mutableStateOf(true) }
    val isShowCompletedTaskItemsButtonRotated = remember { mutableStateOf(false) }
    val showCompletedTaskItemsButtonAngle: Float by animateFloatAsState(
        targetValue = if (isShowCompletedTaskItemsButtonRotated.value) 180F else 0F,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            isShowCompletedTaskItemsButtonEnabled.value = true
        }
    )
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { viewModel.refresh() },
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true,
                contentColor = themedBlue,
                shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                refreshingOffset = 32.dp
            )
        }
    ) {
        HorizontalPager(
            taskListsState.taskLists.size,
            state = pagerState,
            flingBehavior = rememberFlingBehaviorMultiplier(
                multiplier = 0.5f,
                baseFlingBehavior = PagerDefaults.flingBehavior(pagerState)
            ),
        ) { pageIndex ->
            key(recomposeKey.value) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomAppBarPadding.calculateBottomPadding())
                ) {
                    val eachTaskListId = taskListsState.taskLists[pageIndex].id!!
                    val itemList = viewModel.getTaskItemsToDisplay(eachTaskListId)
                    items(
                        itemList,
                        key = { it.id!!.toString() + "uncompleted" }) { taskItem ->
                        AnimatedVisibility(
                            visible = !taskItem.isCompleted,
                            enter = expandVertically(
                                animationSpec = tween(delayMillis = 700),
                                expandFrom = Alignment.Top
                            ) + fadeIn(animationSpec = tween(delayMillis = 700)),
                            exit = fadeOut(animationSpec = tween(delayMillis = 400)) +
                                    shrinkVertically(
                                        animationSpec = tween(delayMillis = 700),
                                        shrinkTowards = Alignment.Top
                                    )
                        ) {
                            ListItem(
                                Modifier
                                    .clickable {
                                        navController.navigate(
                                            Screen.EditTaskItemScreen.route +
                                                    "?taskItemId=${taskItem.id}"
                                        )
                                    }
                                    .fillMaxSize(),
                                text = {
                                    Text(taskItem.title)
                                },
                                icon = {
                                    TaskItemCompletionButton(
                                        taskItem.isCompleted,
                                        onClick = {
                                            scope.launch {
                                                viewModel.onEvent(
                                                    TodoListEvent.ToggleTaskItemCompletionState(
                                                        taskItem
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }

                    item {
                        val count = itemList.count { el -> el.isCompleted }
                        AnimatedVisibility(
                            visible = itemList.any { el -> el.isCompleted },
                            enter = fadeIn(
                                animationSpec = tween(
                                    delayMillis = 400,
                                    durationMillis = 500
                                )
                            ),
                            exit = fadeOut(
                                animationSpec = tween(
                                    delayMillis = 400,
                                    durationMillis = 500
                                )
                            )
                        ) {
                            SemiTransparentDivider()
                            ListItem(
                                Modifier.clickable(
                                    enabled = isShowCompletedTaskItemsButtonEnabled.value
                                ) {
                                    showCompletedTaskItems.value =
                                        !showCompletedTaskItems.value
                                    isShowCompletedTaskItemsButtonRotated.value =
                                        !isShowCompletedTaskItemsButtonRotated.value
                                    isShowCompletedTaskItemsButtonEnabled.value = false
                                },
                                text = { Text("완료됨(${count}개)") },
                                trailing = {
                                    Icon(
                                        Icons.Default.ExpandMore,
                                        "show completed takeItem",
                                        Modifier.rotate(showCompletedTaskItemsButtonAngle)
                                    )
                                }
                            )
                        }
                    }

                    if (showCompletedTaskItems.value) {
                        items(
                            itemList,
                            key = { it.id!!.toString() + "completed" }) { taskItem ->
                            AnimatedVisibility(
                                visible = taskItem.isCompleted,
                                enter = expandVertically(
                                    animationSpec = tween(delayMillis = 700),
                                    expandFrom = Alignment.Top
                                ) + fadeIn(animationSpec = tween(delayMillis = 700)),
                                exit = fadeOut(animationSpec = tween(delayMillis = 400)) +
                                        shrinkVertically(
                                            animationSpec = tween(delayMillis = 700),
                                            shrinkTowards = Alignment.Top
                                        )
                            ) {
                                ListItem(
                                    Modifier
                                        .clickable {
                                            navController.navigate(
                                                Screen.EditTaskItemScreen.route +
                                                        "?taskItemId=${taskItem.id}"
                                            )
                                        }
                                        .fillMaxSize(),
                                    text = {
                                        Text(taskItem.title)
                                    },
                                    icon = {
                                        TaskItemCompletionButton(
                                            taskItem.isCompleted,
                                            onClick = {
                                                scope.launch {
                                                    viewModel.onEvent(
                                                        TodoListEvent.ToggleTaskItemCompletionState(
                                                            taskItem
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun BottomMenu(
    scope: CoroutineScope,
    menuLeftModalBottomSheetState: ModalBottomSheetState,
    menuRightModalBottomSheetState: ModalBottomSheetState,
) {
    Row {
        IconButton(onClick = {
            scope.launch {
                menuLeftModalBottomSheetState.show()
            }
        }) {
            Icon(Icons.Default.Menu, "show menu on left")
        }
        Spacer(modifier = Modifier.weight(1.0f))
        IconButton(onClick = {
            scope.launch {
                menuRightModalBottomSheetState.show()
            }
        }) {
            Icon(Icons.Default.MoreVert, "show menu on right")
        }
    }
}


@ExperimentalMaterialApi
@Composable
private fun TodoListMenuRightModalBottomSheet(
    viewModel: TodoListViewModel,
    selectedTaskListId: () -> Long,
    menuRightModalBottomSheetState: ModalBottomSheetState,
    navController: NavController,
    taskListsState: TaskListsState
) {
    MenuRightModalBottomSheet(
        state = menuRightModalBottomSheetState,
        changeTaskListName = {
            ListItem(
                modifier = Modifier
                    .noRippleClickable {
                        navController.navigate(
                            Screen.AddEditTaskListScreen.route +
                                    "?taskListId=${selectedTaskListId()}"
                        )
                    },
                text = { Text("목록 이름 변경") }
            )
        },
        deleteTaskList = {
            val isEnabled = taskListsState.taskLists.size > 1
            ListItem(
                modifier = Modifier
                    .noRippleClickable(
                        enabled = isEnabled
                    ) {
                        viewModel.onEvent(TodoListEvent.ConfirmDeleteTaskList(selectedTaskListId()))
                    },
                text = {
                    Text(
                        "목록 삭제",
                        color = if (isEnabled) Color.Unspecified else Color.Gray
                    )
                }
            )
        },

        deleteCompletedItems = {
            val itemList = viewModel.getTaskItemsToDisplay(selectedTaskListId())
            val isEnabled = itemList.any { el -> el.isCompleted }
            ListItem(
                modifier = Modifier
                    .noRippleClickable(
                        enabled = isEnabled
                    ) {
                        viewModel.onEvent(
                            TodoListEvent.ConfirmDeleteCompletedTaskItems(
                                selectedTaskListId()
                            )
                        )
                    },
                text = {
                    Text(
                        "완료된 할 일 모두 삭제",
                        color = if (isEnabled) Color.Unspecified else Color.Gray
                    )
                }
            )
        }
    )
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
private fun TodoListMenuLeftModalBottomSheet(
    scope: CoroutineScope,
    menuLeftModalBottomSheetState: ModalBottomSheetState,
    viewModel: TodoListViewModel,
    pagerState: PagerState,
    navController: NavController
) {
    MenuLeftModalBottomSheet(
        scope = scope,
        state = menuLeftModalBottomSheetState,
        taskListsState = viewModel.taskListsState,
        pagerState = pagerState,
        addTaskListItemButton = {
            ListItem(
                modifier = Modifier
                    .noRippleClickable {
                        navController.navigate(
                            Screen.AddEditTaskListScreen.route +
                                    "?taskListId=${-1L}"
                        )
                    },
                icon = { Icon(Icons.Filled.Add, "add new task list") },
                text = { Text("새 목록 만들기") })
        }
    )
}

@ExperimentalMaterialApi
@Composable
private fun TodoListAddTaskItemModalBottomSheet(
    scope: CoroutineScope,
    addTaskItemModalBottomSheetState: ModalBottomSheetState,
    addTaskItemFocusRequester: FocusRequester,
    viewModel: TodoListViewModel,
    selectedTaskListId: () -> Long
) {
    val taskItemTitleState = viewModel.taskItemTitle.value
    AddTaskItemModalBottomSheet(
        scope = scope,
        state = addTaskItemModalBottomSheetState,
        focusRequester = addTaskItemFocusRequester,
        textField = {
            TransparentHintTextField(
                text = taskItemTitleState.text,
                hint = taskItemTitleState.hint,
                onValueChange = {
                    viewModel.onEvent(TodoListEvent.EnterTaskItemTitle(it))
                },
                isHintVisible = taskItemTitleState.isHintVisible,
                textStyle = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxHeight()
                    .focusRequester(addTaskItemFocusRequester),
            )
        },
        addButton = {
            PureTextButton(
                text = "저장",
                textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                onClick = {
                    viewModel.onEvent(TodoListEvent.SaveTaskItem(selectedTaskListId()))
                })
        }
    )

}

@ExperimentalPagerApi
@Composable
private fun EmitSelectTaskListTabEvent(
    pagerState: PagerState,
    taskListsState: TaskListsState,
    viewModel: TodoListViewModel,
    selectedTaskListId: () -> Long
) {
    LaunchedEffect(key1 = pagerState.isScrollInProgress) {
        if (taskListsState.taskLists.isNotEmpty()) {
            viewModel.onEvent(TodoListEvent.SelectTaskList(selectedTaskListId()))
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun DisposeMainScaffoldBottomAppBarVisibility(
    addTaskItemModalBottomSheetState: ModalBottomSheetState,
    showMainScaffoldBottomAppBar: MutableState<Boolean>,
    menuLeftModalBottomSheetState: ModalBottomSheetState,
    menuRightModalBottomSheetState: ModalBottomSheetState
) {
    LaunchedEffect(
        key1 = addTaskItemModalBottomSheetState.targetValue
    ) {
        val isLeftModalBottomSheetHidden =
            menuLeftModalBottomSheetState.targetValue == ModalBottomSheetValue.Hidden
        val isRightModalBottomSheetHidden =
            menuRightModalBottomSheetState.targetValue == ModalBottomSheetValue.Hidden
        showMainScaffoldBottomAppBar.value =
            isLeftModalBottomSheetHidden && isRightModalBottomSheetHidden
    }
}

@ExperimentalMaterialApi
@Composable
private fun DisposeModalBottomSheetState(
    menuLeftModalBottomSheetState: ModalBottomSheetState,
    menuRightModalBottomSheetState: ModalBottomSheetState
) {
    LaunchedEffect(
        key1 = true
    ) {
        launch {
            menuLeftModalBottomSheetState.hide()
            menuRightModalBottomSheetState.hide()
        }
    }
}

@Composable
private fun ObserveEditTaskItemEvent(
    editTaskItemViewModel: EditTaskItemViewModel,
    mainScaffoldState: ScaffoldState
) {
    LaunchedEffect(key1 = true) {
        editTaskItemViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditTaskItemViewModel.UiEvent.ShowSnackbar -> {
                    val snackbarResult =
                        mainScaffoldState.snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                        )
                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        event.action()
                    }
                }
                else -> {
                }
            }
        }
    }
}


@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
private fun ObserveUiEvent(
    viewModel: TodoListViewModel,
    mainScaffoldState: ScaffoldState,
    pagerState: PagerState,
    addTaskItemModalBottomSheetState: ModalBottomSheetState,
    showDeleteTaskListDialogState: MutableState<Boolean>,
    menuRightModalBottomSheetState: ModalBottomSheetState,
    recomposeKey: MutableState<Int>
) {
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is TodoListViewModel.UiEvent.ShowSnackbar -> {
                    val snackbarResult = mainScaffoldState.snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                    )
                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        event.action()
                    }
                }
                is TodoListViewModel.UiEvent.ScrollTaskListPosition -> {
                    pagerState.scrollToPage(event.index)
                }
                is TodoListViewModel.UiEvent.SaveTaskItem -> {
                    launch {
                        addTaskItemModalBottomSheetState.hide()
                    }
                }
                is TodoListViewModel.UiEvent.ShowConfirmDialog -> {
                    showDeleteTaskListDialogState.value = true
                }
                is TodoListViewModel.UiEvent.CloseMenuRightModalBottomSheet -> {
                    launch {
                        menuRightModalBottomSheetState.hide()
                    }
                }
                is TodoListViewModel.UiEvent.Recompose -> run {
                    recomposeKey.value += 1
                }
            }
        }
    }
}