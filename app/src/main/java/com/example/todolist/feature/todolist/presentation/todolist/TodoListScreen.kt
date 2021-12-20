package com.example.todolist.feature.todolist.presentation.todolist

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
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
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.todolist.components.*
import com.example.todolist.feature.todolist.presentation.todolist.util.getTargetPage
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.feature.todolist.presentation.util.noRippleClickable
import com.example.todolist.ui.theme.Blue
import com.example.todolist.ui.theme.DarkWhite
import com.example.todolist.ui.theme.LightBlack
import com.example.todolist.ui.theme.LightBlue
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


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
    viewModel: TodoListViewModel = hiltViewModel()
) {

    rememberSystemUiController().run {
        setNavigationBarColor(
            color = Color.DarkGray
        )
        setStatusBarColor(
            color = Color.Transparent
        )
    }

    val taskItemContentState = viewModel.taskItemContent.value

    val taskListsState = viewModel.taskListsState.value

    val dialogTypeState = viewModel.dialogType.value

    val mainScaffoldState = rememberScaffoldState()

    val showMainBottomSheetScaffold = remember { mutableStateOf(value = true) }

    val scope = rememberCoroutineScope()

    val addTaskItemModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val addTaskItemFocusRequester = remember { FocusRequester() }

    val menuLeftModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val menuRightModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val pagerState = rememberPagerState(
        initialPage = 0,
    )

    val showCompletedTaskItems = remember { mutableStateOf(value = false) }
    val showCompletedTaskItemsButton = remember { mutableStateOf(value = true) }

    val currentPageState = { getTargetPage(pagerState) }

    val selectedTaskListId = {
        if (taskListsState.taskLists.isNotEmpty())
            if (taskListsState.taskLists.size > currentPageState()) {
                taskListsState.taskLists[currentPageState()].id!!
            } else -1L
        else -1L
    }
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

    var showDeleteTaskListDialog by remember { mutableStateOf(false) }

    LaunchedEffect(
        key1 = addTaskItemModalBottomSheetState.targetValue
    ) {
        showMainBottomSheetScaffold.value =
            menuLeftModalBottomSheetState.targetValue == ModalBottomSheetValue.Hidden
    }

    LaunchedEffect(key1 = true) {
        async {
            menuLeftModalBottomSheetState.hide()
            menuRightModalBottomSheetState.hide()
        }
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
                is TodoListViewModel.UiEvent.CancelToggleTaskItemCompletionState -> {
                    showCompletedTaskItemsButton.value = true
                }
                is TodoListViewModel.UiEvent.ScrollTaskListPosition -> {
                    pagerState.scrollToPage(event.index)
                    viewModel.onEvent(TodoListEvent.LastTaskListPositionHasSelected)
                }
                is TodoListViewModel.UiEvent.SaveTaskItem -> {
                    async {
                        addTaskItemModalBottomSheetState.hide()
                    }
                }
                is TodoListViewModel.UiEvent.ShowConfirmDialog -> {
                    showDeleteTaskListDialog = true
                }
                else -> {
                    Log.i("TodoLisScreen", "Wrong event called :: ${event.javaClass.name}")
                }
            }
        }
    }

    Scaffold(
        backgroundColor = LightBlack,
        scaffoldState = mainScaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = DarkWhite,
                    content = {
                        Text(
                            text = data.message,
                            color = Color.Black,
                            style = MaterialTheme.typography.body1
                        )
                    },
                    action = {
                        data.actionLabel?.let { actionLabel ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(
                                    text = actionLabel,
                                    color = Blue,
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    })
            }
        },
        topBar = {
            Spacer(
                modifier = Modifier
                    .statusBarsHeight()
                    .fillMaxWidth()
            )
        },
        floatingActionButton = {
            FadeSlideAnimatedVisibility(showMainBottomSheetScaffold) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (addTaskItemModalBottomSheetState.isVisible) {
                                addTaskItemModalBottomSheetState.hide()
                            } else {
                                viewModel.clearTaskItemContentTextField()
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
            FadeSlideAnimatedVisibility(showMainBottomSheetScaffold) {
                BottomAppBar(
                    modifier = Modifier.navigationBarsPadding(),
                    cutoutShape = RoundedCornerShape(50),
                    elevation = 0.dp,
                    content = {
                        IconButton(onClick = {
                            scope.async {
                                menuLeftModalBottomSheetState.show()
                            }
                        }) {
                            Icon(Icons.Default.Menu, "show menu on left")
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                        IconButton(onClick = {
                            scope.async {
                                menuRightModalBottomSheetState.show()
                            }
                        }) {
                            Icon(Icons.Default.MoreVert, "show menu on right")
                        }
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
                ScrollableTabRow(
                    selectedTabIndex = currentPageState(),
                    edgePadding = 8.dp,
                    backgroundColor = LightBlack,
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
                            color = LightBlue
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
                                color = if (isSelected) LightBlue else MaterialTheme.colors.onSurface
                            )
                        }, selectedContentColor = LightBlue)
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
                    if (!viewModel.lastSelectedTaskListPositionLoaded.value) {
                        viewModel.onEvent(TodoListEvent.LoadLastSelectedTaskListPosition)
                    }
                }
                SemiTransparentDivider()

                HorizontalPager(
                    taskListsState.taskLists.size,
                    state = pagerState,
                    flingBehavior = rememberFlingBehaviorMultiplier(
                        multiplier = 0.5f,
                        baseFlingBehavior = PagerDefaults.flingBehavior(pagerState)
                    ),
                ) { pageIndex ->

                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = bottomAppBarPadding.calculateBottomPadding())
                    ) {
                        val eachTaskListId = taskListsState.taskLists[pageIndex].id!!
                        viewModel.onEvent(TodoListEvent.SelectTaskList(selectedTaskListId()))
                        viewModel.onEvent(TodoListEvent.GetTaskItemsByTaskListId(eachTaskListId))
                        val itemList = viewModel.getTaskItems(eachTaskListId)
                        val completedList = itemList.filter { el -> el.isCompleted }
                        val uncompletedList = itemList.filter { el -> !el.isCompleted }

                        items(uncompletedList, key = { it.id!! }) { taskItem ->
                                var visible by remember { mutableStateOf(true) }
                                AnimatedVisibility(
                                    modifier = Modifier.animateItemPlacement(),
                                    visible = visible,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    ListItem(
                                        text = { Text(taskItem.content) },
                                        icon = {
                                            TaskItemCompletionButton(
                                                taskItem.isCompleted,
                                                onClick = {
                                                    scope.async {
                                                        delay(200L)
                                                        visible = false
                                                        showCompletedTaskItemsButton.value = true
                                                        delay(300L)
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
                                visible = itemList.any { el -> el.isCompleted } && showCompletedTaskItemsButton.value,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                ListItem(
                                    Modifier.clickable(
                                        enabled = isShowCompletedTaskItemsButtonEnabled.value
                                    ) {
                                        showCompletedTaskItems.value = !showCompletedTaskItems.value
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
                                completedList,
                                key = { it.id!! }) { taskItem ->
                                    var visible by remember { mutableStateOf(true) }
                                    AnimatedVisibility(
                                        modifier = Modifier.animateItemPlacement(),
                                        visible = visible,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        ListItem(
                                            text = { Text(taskItem.content) },
                                            icon = {
                                                TaskItemCompletionButton(
                                                    taskItem.isCompleted,
                                                    onClick = {
                                                        scope.async {
                                                            delay(200L)
                                                            visible = false
                                                            if(completedList.size == 1){
                                                                showCompletedTaskItemsButton.value = false
                                                            }
                                                            delay(300L)
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
//                        }
                    }
                }
            }
        }
    }

    AddTaskItemModalBottomSheet(
        scope = scope,
        state = addTaskItemModalBottomSheetState,
        focusRequester = addTaskItemFocusRequester,
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
            )
        },
        addButton = {
            PureTextButton(
                text = "저장",
                textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                onClick = {
                    viewModel.onEvent(TodoListEvent.SaveTaskItem)
                })
        }
    )

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

    MenuRightModalBottomSheet(
        scope = scope,
        state = menuRightModalBottomSheetState,
        taskListId = selectedTaskListId(),
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
            val itemList = viewModel.getTaskItems(selectedTaskListId())
            val isEnabled = itemList.isNotEmpty()
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

    if (showDeleteTaskListDialog) {
        val titleText: String
        val contentText: String
        val confirmButtonText = "삭제"
        val eventWhenConfirm: TodoListEvent

        when (dialogTypeState) {
            is TodoListViewModel.DialogType.DeleteTaskList -> {
                titleText = "목록을 삭제하시겠습니까?"
                contentText = "목록에 작성된 모든 할 일이 삭제됩니다. 삭제하시겠습니까?"
                eventWhenConfirm = TodoListEvent.DeleteTaskList
            }
            is TodoListViewModel.DialogType.DeleteCompletedTaskItem -> {
                titleText = "완료된 할 일을 모두 삭제하시겠습니까?"
                contentText = ""
                eventWhenConfirm = TodoListEvent.DeleteCompletedTaskItems
            }
        }
        AlertDialog(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(8.dp),
            onDismissRequest = { showDeleteTaskListDialog = false },
            title = { Text(titleText) },
            text = { Text(contentText) },
            confirmButton = {
                PureTextButton(
                    text = confirmButtonText, textColor = LightBlue,
                    paddingValues = PaddingValues(8.dp, 8.dp, 16.dp, 16.dp)
                ) {
                    showDeleteTaskListDialog = false
                    viewModel.onEvent(eventWhenConfirm)
                }
            },
            dismissButton = {
                PureTextButton(
                    text = "취소", textColor = LightBlue,
                    paddingValues = PaddingValues(8.dp, 8.dp, 16.dp, 16.dp)
                ) {
                    showDeleteTaskListDialog = false
                }
            },
        )
    }
}