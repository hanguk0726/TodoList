package com.example.todolist.feature.todolist.presentation.editTaskItem.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.ui.theme.ScrimColor
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

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
    taskLists: List<TaskList>
) {

    BackHandler(
        enabled = state.isVisible,
        onBack = {
            scope.async {
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
        sheetContent = {

        }
    ){

    }

}