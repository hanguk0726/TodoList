package com.example.todolist.feature.todolist.presentation.editTaskItem

import TransparentHintTextField
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todolist.common.ui.theme.themedBlue
import com.example.todolist.common.ui.theme.themedGray
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.presentation.components.CustomSnackbarHost
import com.example.todolist.feature.todolist.presentation.editTaskItem.components.EditTaskItemTaskListIdModalBottomSheet
import com.example.todolist.feature.todolist.presentation.todolist.TodoListTextFieldState
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.util.noRippleClickable
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun EditTaskItemScreen(
    navController: NavController,
    taskItemId: Long,
    viewModel: EditTaskItemViewModel
) {


    val scaffoldState = rememberScaffoldState()
    val taskItemTitleState = viewModel.taskItemTitle.value
    val taskItemDetailState = viewModel.taskItemDetail.value

    val taskItemDetailFocusRequester = remember { FocusRequester() }

    val scope = rememberCoroutineScope()

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val taskListsState = viewModel.taskListsState.value
    val taskItem = viewModel.taskItemState.value

    BackHandler(
        enabled = true,
        onBack = {
            scope.launch {
                viewModel.onEvent(EditTaskItemEvent.SaveTaskItem)
            }
        }
    )

    ApplyEditTaskItemScreenTheme()
    ObserveUiEvent(viewModel, navController, taskItemId)

    Scaffold(
        Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        snackbarHost = {
            CustomSnackbarHost(it)
        },
        topBar = {
            Spacer(modifier = Modifier.statusBarsPadding())
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.navigationBarsPadding(),
                elevation = if (isSystemInDarkTheme()) 0.dp else 16.dp,
                content = {
                    Spacer(modifier = Modifier.weight(1.0f))
                    ChangeTaskItemCompleteStateBottomSheet(taskItem, viewModel)
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TopMenu(viewModel)

            ChangeTaskListOfTaskItemBar(
                scope, modalBottomSheetState, viewModel
            )
            TaskItemTitleTextField(taskItemTitleState, viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            TaskItemDetailTextField(viewModel, taskItemDetailFocusRequester, taskItemDetailState)
        }
    }
    if (taskItem != null) {
        EditTaskItemTaskListIdModalBottomSheet(
            scope = scope,
            state = modalBottomSheetState,
            taskListsState = taskListsState,
            taskItem = taskItem,
            onClickTaskList = { _taskItemId: Long, _taskItemTaskListId ->
                viewModel.onEvent(
                    EditTaskItemEvent.ChangeTaskListOfTaskItem(
                        _taskItemId, _taskItemTaskListId
                    )
                )
            }
        )
    }
}

@Composable
private fun ApplyEditTaskItemScreenTheme() {
    rememberSystemUiController().run {
        if (isSystemInDarkTheme()) {
            setNavigationBarColor(
                color = Color.DarkGray,
                darkIcons = false
            )
            setStatusBarColor(
                color = Color.Transparent,
                darkIcons = false
            )
        } else {
            setNavigationBarColor(
                color = Color.White,
                darkIcons = true
            )
            setStatusBarColor(
                color = Color.Transparent,
                darkIcons = true
            )
        }
    }
}

@DelicateCoroutinesApi
@Composable
private fun ChangeTaskItemCompleteStateBottomSheet(
    taskItem: TaskItem?,
    viewModel: EditTaskItemViewModel
) {
    if (taskItem != null) {
        PureTextButton(
            text = if (taskItem.isCompleted) "???????????? ??????" else "????????? ??????",
            textColor = themedBlue,
            noRipple = true
        ) {
            viewModel.onEvent(EditTaskItemEvent.ToggleAndSaveTaskItemCompletionState)
        }
    }
}

@DelicateCoroutinesApi
@Composable
private fun TopMenu(viewModel: EditTaskItemViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(onClick = {
            viewModel.onEvent(EditTaskItemEvent.SaveTaskItem)
        }) {
            Icon(
                Icons.Default.Close,
                "save and close editTaskItem screen",
                tint = themedGray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Spacer(modifier = Modifier.weight(1.0f))
        IconButton(onClick = {
            viewModel.onEvent(EditTaskItemEvent.DeleteTaskItem)
        }) {
            Icon(
                Icons.Default.DeleteOutline,
                "delete taskItem",
                tint = themedGray
            )
        }
    }
}


@ExperimentalMaterialApi
@Composable
private fun ChangeTaskListOfTaskItemBar(
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: EditTaskItemViewModel
) {
    Row(
        Modifier
            .noRippleClickable {
                scope.launch {
                    modalBottomSheetState.show()
                }
            }
            .padding(16.dp, 8.dp)
    ) {
        if (viewModel.currentTaskListOfTaskItem != null) {
            Text(
                text = viewModel.currentTaskListOfTaskItem!!.name,
                color = themedBlue,
                fontSize = 14.sp
            )
            Icon(
                Icons.Default.ArrowDropDown,
                "show editTaskListModal",
                tint = themedBlue
            )
        }
    }
}


@DelicateCoroutinesApi
@Composable
private fun TaskItemTitleTextField(
    taskItemTitleState: TodoListTextFieldState,
    viewModel: EditTaskItemViewModel
) {
    TransparentHintTextField(
        modifier = Modifier.padding(16.dp),
        text = taskItemTitleState.text,
        hint = taskItemTitleState.hint,
        textStyle = MaterialTheme.typography.body1,
        isHintVisible = taskItemTitleState.isHintVisible,
        onValueChange = {
            viewModel.onEvent(EditTaskItemEvent.EnterTaskItemTitle(it))
        },
    )
}

@DelicateCoroutinesApi
@Composable
private fun TaskItemDetailTextField(
    viewModel: EditTaskItemViewModel,
    taskItemDetailFocusRequester: FocusRequester,
    taskItemDetailState: TodoListTextFieldState
) {
    Box(
        modifier = Modifier.clickable {
            taskItemDetailFocusRequester.requestFocus()
        },
    ) {
        Row {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                Icons.Default.Notes, "taskItem detail",
                tint = themedGray
            )
            Spacer(modifier = Modifier.width(16.dp))
            TransparentHintTextField(
                modifier = Modifier
                    .focusRequester(taskItemDetailFocusRequester),
                text = taskItemDetailState.text,
                hint = taskItemDetailState.hint,
                textStyle = MaterialTheme.typography.body1,
                isHintVisible = taskItemDetailState.isHintVisible,
                onValueChange = {
                    viewModel.onEvent(EditTaskItemEvent.EnterTaskItemDetail(it))
                },
            )
        }
    }
}

@Composable
private fun ObserveUiEvent(
    viewModel: EditTaskItemViewModel, navController: NavController,
    taskItemId: Long
) {
    LaunchedEffect(key1 = true) {
        viewModel.loadTaskItemValues(taskItemId)
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditTaskItemViewModel.UiEvent.DeleteTaskItem -> {
                    navController.navigateUp()
                }
                is EditTaskItemViewModel.UiEvent.SaveTaskItem -> {
                    navController.navigateUp()
                }
                is EditTaskItemViewModel.UiEvent.ShowSnackbar -> {
                }
            }
        }
    }

}