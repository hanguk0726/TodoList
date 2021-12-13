package com.example.todolist.feature.todolist.presentation.addEditTaskList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddTaskListScreen(
    navController: NavController,
    viewModel: AddEditTaskListViewModel = hiltViewModel()
) {
// 저장 이후 수정도
    val scaffoldState = rememberScaffoldState()
    val taskListNameState = viewModel.taskListName.value

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is AddEditTaskListViewModel.UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddEditTaskListViewModel.UiEvent.SaveTaskList -> {
                    navController.navigateUp()
                }
            }
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .clickable {
                            navController.navigateUp()
                        }
                        .padding(8.dp)
                ){
                    Icon(Icons.Default.Close, "close add task list screen")
                }
                Text("새 목록 만들기")
                PureTextButton(text = "완료", textColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) {
                    viewModel.onEvent(AddEditTaskListEvent.SaveTaskList)
                }
            }

            Divider(
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxHeight()
                    .height(1.dp)
            )

            TransparentHintTextField(
                text = taskListNameState.text,
                hint = taskListNameState.hint,
                onValueChange = {
                    viewModel.onEvent(AddEditTaskListEvent.EnterTaskListName(it))
                }
            )
            Divider(
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxHeight()
                    .height(1.dp)
            )
        }
    }
}