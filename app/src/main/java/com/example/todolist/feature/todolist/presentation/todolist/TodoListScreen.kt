package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
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
import kotlin.math.absoluteValue
import kotlin.math.max


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

    // TaskList 초기화 순서
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

    val taskItemsState = viewModel.taskItemsState.value

    val pagerState = rememberPagerState(
        initialPage = 0,
    )

    val keyboardController = LocalSoftwareKeyboardController.current
// 3 개 탭은 미리 로드해야하며 트리거도 바꿔야할 수 있다.
    LaunchedEffect(key1 = true, key2 = pagerState.currentPage) {
        menuModalBottomSheetState.hide()
        viewModel.saveLastSelectedTaskListIndex(pagerState.currentPage)
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is TodoListViewModel.UiEvent.InitialTaskListIndex -> {
                    pagerState.scrollToPage(event.index)
                }
                is TodoListViewModel.UiEvent.SaveTaskItem -> {
                    addTaskItemModalBottomSheetState.hide()
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
                    Icon(Icons.Filled.Add, "add new task item",
                        modifier = Modifier.size(36.dp))
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
                    selectedTabIndex = pagerState.currentPage,
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
                        val isSelected = pagerState.currentPage == index
                        Tab(selected = isSelected, onClick = {
                            scope.launch {
                                pagerState.scrollToPage(index)
                            }
                        }, text = {
                            Text(text = taskList.name,
                            color = if(isSelected) LightBlue else MaterialTheme.colors.onSurface )
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
                }
                SemiTransparentDivider()

                HorizontalPager(
                    taskListsState.taskLists.size,
                    state = pagerState,
                    flingBehavior = rememberFlingBehaviorMultiplier(
                        multiplier = 0.5f,
                        baseFlingBehavior = PagerDefaults.flingBehavior(pagerState)
                ),
                ) {
//  탭 이름 판정과 탭 내용 판정을 빨리 바꾸는 것은 로직상 연관될 수 있다. -> 먼저 체크하기
//                    LaunchedEffect(key1 = pagerState.currentPageOffset){
//                        val targetDistance = (pagerState.targetPage - pagerState.currentPage).absoluteValue
//                        // Our normalized fraction over the target distance
//                        val fraction = (pagerState.currentPageOffset / max(targetDistance, 1)).absoluteValue
//                        println("mylogger :: targetDistance ${targetDistance}//fraction ${fraction}" )
//
//                    }
                    LaunchedEffect(key1 = pagerState.currentPage){
                        val id = taskListsState.taskLists[pagerState.currentPage].id
                        viewModel.onEvent(TodoListEvent.ChangeTaskList(id!!))
                        viewModel.onEvent(TodoListEvent.GetTaskItemsByTaskListId(id!!))
                        println("mylogger :: taskItemsState.taskItems :: ${taskItemsState.taskItems.size}")
                    }
                    Text(" ///${taskListsState.taskLists.size}")
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(taskItemsState.taskItems) { taskItem ->
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