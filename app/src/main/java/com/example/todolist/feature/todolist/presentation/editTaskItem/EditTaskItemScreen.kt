package com.example.todolist.feature.todolist.presentation.editTaskItem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.addEditTaskList.AddEditTaskListEvent
import com.example.todolist.feature.todolist.presentation.addEditTaskList.AddEditTaskListViewModel
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import com.example.todolist.ui.theme.LightBlue
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest

@ExperimentalMaterialApi
@Composable
fun EditTaskItemScreen(
    navController: NavController,
    taskItemId: Long,
    viewModel: EditTaskItemViewModel = hiltViewModel()
) {

    rememberSystemUiController().setSystemBarsColor(
        color = MaterialTheme.colors.background
    )
    val scaffoldState = rememberScaffoldState()
    val taskItemTitleState = viewModel.taskItemTitle.value
    val taskItemDetailState = viewModel.taskItemDetail.value

    val taskItemDetailFocusRequester = remember { FocusRequester() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.loadTaskItemValues()
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditTaskItemViewModel.UiEvent.SaveTaskItem -> {
                    navController.navigateUp()
                }
                is EditTaskItemViewModel.UiEvent.DeleteTaskItem -> {
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            Spacer(modifier = Modifier.statusBarsPadding())
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.navigationBarsPadding(),
                elevation = 0.dp,
                content = {
                    Spacer(modifier = Modifier.weight(1.0f))
                    PureTextButton(text = "완료로 표시", textColor = LightBlue) {

                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(onClick = {
                    viewModel.onEvent(EditTaskItemEvent.SaveTaskItem)
                    navController.navigateUp()
                }) {
                    Icon(
                        Icons.Default.Close,
                        "save and close editTaskItem screen",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Spacer(modifier = Modifier.weight(1.0f))
                IconButton(onClick = {
                    viewModel.onEvent(EditTaskItemEvent.DeleteTaskItem)
                    navController.navigateUp()
                }) {
                    Icon(
                        Icons.Default.Close,
                        "delete taskItem",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }


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

            ListItem(
                modifier = Modifier.clickable {
                    taskItemDetailFocusRequester.requestFocus()
                },
                icon = { Icon(Icons.Default.Notes, "taskItem detail",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) }
            ) {
                TransparentHintTextField(
                    modifier = Modifier
                        .padding(16.dp)
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
}