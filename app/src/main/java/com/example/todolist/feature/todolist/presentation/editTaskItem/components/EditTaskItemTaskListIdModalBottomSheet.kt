package com.example.todolist.feature.todolist.presentation.editTaskItem.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.common.ui.theme.ScrimColor
import com.example.todolist.common.ui.theme.themedBlue
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.presentation.util.TaskListsState
import com.example.todolist.feature.todolist.presentation.util.noRippleClickable
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun EditTaskItemTaskListIdModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    taskListsState: TaskListsState,
    taskItem: TaskItem,
    onClickTaskList: (taskItemId: Long, targetTaskListId: Long) -> Unit
) {
    BackHandler(
        enabled = state.isVisible,
        onBack = {
            scope.launch {
                state.hide()
            }
        }
    )

    ModalBottomSheetLayout(
        modifier = Modifier.navigationBarsPadding(),
        sheetState = state,
        sheetElevation = 0.dp,
        scrimColor = ScrimColor,
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        content = {},
        sheetContent = {
            Column {
                Text("?????? ?????? ??????", color = Color.Gray, modifier = Modifier.padding(16.dp))
                taskListsToMoveTaskItem(
                    scope, state, taskListsState, taskItem, onClickTaskList
                )
            }
        }
    )
}

@ExperimentalMaterialApi
@Composable
private fun taskListsToMoveTaskItem(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    taskListsState: TaskListsState,
    taskItem: TaskItem,
    onClickTaskList: (taskItemId: Long, targetTaskListId: Long) -> Unit
) {
    taskListsState.taskLists.forEach { el ->
        val isCurrentlySelected = el.id == taskItem.taskListId
        Row(
            Modifier
                .noRippleClickable {
                    if (isCurrentlySelected) {
                        scope.launch {
                            state.hide()
                        }
                    } else {
                        scope.launch {
                            onClickTaskList(taskItem.id!!, el.id!!)
                            state.hide()
                        }
                    }
                }
                .padding(16.dp)
        ) {
            Text(
                el.name,
                color = if (isCurrentlySelected) themedBlue else Color.Unspecified
            )
            Spacer(modifier = Modifier.weight(1.0f))
            if (isCurrentlySelected) {
                Icon(
                    Icons.Default.Check,
                    "taskList currently selected for taskItem",
                    tint = themedBlue
                )
            }
        }
    }
}