package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.todolist.components.*
import com.example.todolist.feature.todolist.presentation.todolist.util.getTargetPage
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.ui.theme.LightBlack
import com.example.todolist.ui.theme.LightBlue
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.chrisbanes.snapper.ExperimentalSnapperApi
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

    val mainScaffoldState = rememberScaffoldState()
    val showMainBottomSheetScaffold = remember { mutableStateOf(value = true) }

    val scope = rememberCoroutineScope()

    val addTaskItemModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val addTaskItemFocusRequester = remember { FocusRequester() }

    val menuModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val _pagerState = rememberPagerState(
        initialPage = 0,
    )

    val currentPageState = { getTargetPage(_pagerState) }

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(key1 = true) {
        menuModalBottomSheetState.hide()
        viewModel.eventFlow.collectLatest { event ->
            println("mylogger event called")
            when (event) {
                is TodoListViewModel.UiEvent.ScrollToLastSelectedTaskListPosition -> {
                    _pagerState.scrollToPage(event.index)
                    viewModel.onEvent(TodoListEvent.LastTaskListPositionHasSelected)
                }
                is TodoListViewModel.UiEvent.SaveTaskItem -> {
                    println("mylogger SaveTaskItem1 ${addTaskItemModalBottomSheetState.currentValue}")
                    addTaskItemModalBottomSheetState.hide()
                    println("mylogger SaveTaskItem2 ${addTaskItemModalBottomSheetState.currentValue}")
                }
            }
        }
    }

    Scaffold(
        backgroundColor = LightBlack,
        scaffoldState = mainScaffoldState,
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
                            scope.launch {
                                menuModalBottomSheetState.show()
                            }
                        }) {
                            Icon(Icons.Default.Menu, "show menu")
                        }
                    },
                )
            }
        }
    ) {
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
                                .pagerTabIndicatorOffset(_pagerState, tabPositions)
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
                                _pagerState.animateScrollToPage(index)
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
                    if(!viewModel.lastSelectedTaskListPositionLoaded.value){
                        viewModel.onEvent(TodoListEvent.LoadLastSelectedTaskListPosition)
                    }
                }
                SemiTransparentDivider()

                HorizontalPager(
                    taskListsState.taskLists.size,
                    state = _pagerState,
                    flingBehavior = rememberFlingBehaviorMultiplier(
                        multiplier = 0.5f,
                        baseFlingBehavior = PagerDefaults.flingBehavior(_pagerState)
                    ),
                ) { pageIndex ->

                    LazyColumn(Modifier.fillMaxSize()) {
                        val eachTaskListId = taskListsState.taskLists[pageIndex].id!!
                        val selectedTaskListId = taskListsState.taskLists[currentPageState()].id!!
                        viewModel.onEvent(TodoListEvent.SelectTaskList(selectedTaskListId))
                        viewModel.onEvent(TodoListEvent.GetTaskItemsByTaskListId(eachTaskListId))
                        val itemList = viewModel.getTaskItems(eachTaskListId)
                        items(itemList) { taskItem ->
                            ListItem(
                                text = { Text(taskItem.content) }
                            )
                        }
                    }
                }
            }
        }
    }


    AddTaskItemModalBottomSheet(
        scope = scope,
        state = addTaskItemModalBottomSheetState,
        focusRequester = addTaskItemFocusRequester,
        showMainBottomSheetScaffold = showMainBottomSheetScaffold,
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
            PureTextButton(
                text = "저장",
                textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                onClick = {
                    viewModel.onEvent(TodoListEvent.SaveTaskItem)
                })
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