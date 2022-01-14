package com.example.todolist.feature.todolist.presentation.addEditTaskList

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.todolist.TodoListTextFieldState
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddTaskListScreen(
    navController: NavController,
    taskListId: Long,
    viewModel: AddEditTaskListViewModel = hiltViewModel()
) {

    val scaffoldState = rememberScaffoldState()
    val taskListNameState = viewModel.taskListName.value

    ApplyAddEditTaskListScreenTheme()
    SetUpInitialData(taskListId, viewModel)
    ObserveUiEvent(viewModel, navController)

    Scaffold(
        Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            Spacer(modifier = Modifier.statusBarsPadding())
        },
        bottomBar = {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopMenu(navController, taskListId, viewModel)

            SemiTransparentDivider()

            AddEditTaskListTextField(taskListNameState, viewModel)

            SemiTransparentDivider()
        }
    }
}

@Composable
private fun ApplyAddEditTaskListScreenTheme() {
    rememberSystemUiController().setSystemBarsColor(
        color = MaterialTheme.colors.background
    )
}

@Composable
private fun SetUpInitialData(taskListId: Long, viewModel: AddEditTaskListViewModel) {
    LaunchedEffect(key1 = true) {
        if (taskListId == -1L) {
            viewModel.clearTaskListNameTextField()
        } else {
            viewModel.loadTaskListNameToModify(taskListId)
        }
    }
}

@Composable
private fun TopMenu(
    navController: NavController,
    taskListId: Long,
    viewModel: AddEditTaskListViewModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        IconButton(onClick = {
            navController.navigateUp()
        }) {
            Icon(
                Icons.Default.Close,
                "close add task list screen",
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (taskListId == -1L) "새 목록 만들기" else "목록 이름 변경하기", fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1.0f))
        PureTextButton(
            text = "완료",
            textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
        ) {
            viewModel.onEvent(AddEditTaskListEvent.SaveTaskList(taskListId))
        }
    }
}


@Composable
private fun AddEditTaskListTextField(
    taskListNameState: TodoListTextFieldState,
    viewModel: AddEditTaskListViewModel
) {
    TransparentHintTextField(
        modifier = Modifier.padding(16.dp),
        text = taskListNameState.text,
        hint = taskListNameState.hint,
        textStyle = MaterialTheme.typography.body1,
        isHintVisible = taskListNameState.isHintVisible,
        onValueChange = {
            viewModel.onEvent(AddEditTaskListEvent.EnterTaskListName(it))
        },
    )
}

@Composable
private fun ObserveUiEvent(viewModel: AddEditTaskListViewModel, navController: NavController) {
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditTaskListViewModel.UiEvent.SaveTaskList -> {
                    navController.navigateUp()
                }
            }
        }
    }
}