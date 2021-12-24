package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.ScrimColor
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.CoroutineScope

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun MenuRightModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    taskListId: Long = -1L,
    changeTaskListName: @Composable () -> Unit,
    deleteTaskList: @Composable () -> Unit,
    deleteCompletedItems: @Composable () -> Unit,
) {

    val itemPaddingWithSpacer  = 56
    val bottomPadding = 72

    ModalBottomSheetLayout(
        modifier = Modifier
            .navigationBarsPadding(),
        sheetState = state,
        sheetElevation = 0.dp,
        scrimColor = ScrimColor,
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        sheetContent = {
            val padding = bottomPadding + itemPaddingWithSpacer * 2
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(padding.dp)
                    .padding(0.dp, 8.dp, 8.dp, 8.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    item {
                        changeTaskListName()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        deleteTaskList()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        deleteCompletedItems()
                    }
                }
            }
        },
    ) {

    }
}