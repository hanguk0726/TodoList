package com.example.todolist.feature.todolist.presentation.editTaskItem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist.feature.todolist.presentation.components.CustomSnackbarHost
import com.example.todolist.feature.todolist.presentation.todolist.components.PureTextButton
import com.example.todolist.feature.todolist.presentation.todolist.components.TransparentHintTextField
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.ui.theme.LightBlue
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest

@ExperimentalMaterialApi
@Composable
fun EditTaskItemScreen(
    navController: NavController,
    taskItemId: Long,
    viewModel: EditTaskItemViewModel = hiltViewModel()
) {

    rememberSystemUiController().setStatusBarColor(
        color = MaterialTheme.colors.background
    )
    rememberSystemUiController().setNavigationBarColor(
        color = Color.DarkGray
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
                is EditTaskItemViewModel.UiEvent.DeleteTaskItem -> {
                    navController.navigateUp()

                }
                is EditTaskItemViewModel.UiEvent.SaveTaskItem -> {
                    navController.navigateUp()
                }

            }
        }
    }
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
                elevation = 0.dp,
                content = {
                    Spacer(modifier = Modifier.weight(1.0f))
                    if (viewModel.currentTaskItemCompletionState != null) {
                        PureTextButton(
                            text = if (viewModel.currentTaskItemCompletionState!!) "미완료로 표시" else "완료로 표시",
                            textColor = LightBlue,
                            noRipple = true
                        ) {
                            viewModel.onEvent(EditTaskItemEvent.ToggleAndSaveTaskItemCompletionState)
                        }
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
            ) {
                IconButton(onClick = {
                    viewModel.onEvent(EditTaskItemEvent.SaveTaskItem)
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
                }) {
                    Icon(
                        Icons.Default.DeleteOutline,
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
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.clickable {
                    taskItemDetailFocusRequester.requestFocus()
                },
            ) {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.Notes, "taskItem detail",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
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
    }
}