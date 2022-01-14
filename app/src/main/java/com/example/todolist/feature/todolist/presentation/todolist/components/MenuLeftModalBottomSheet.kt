package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.util.TaskListsState
import com.example.todolist.feature.todolist.presentation.todolist.util.getTargetPage
import com.example.todolist.feature.todolist.presentation.util.noRippleClickable
import com.example.todolist.common.ui.theme.ScrimColor
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun MenuLeftModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    taskListsState: State<TaskListsState>,
    pagerState: PagerState,
    addTaskListItemButton: @Composable () -> Unit
) {

    val currentPageState = { getTargetPage(pagerState) }
    val itemPaddingWithSpacer  = 56
    val bottomPadding = 72
    var heightOfBox = bottomPadding + itemPaddingWithSpacer * taskListsState.value.taskLists.size

    ModalBottomSheetLayout(
        modifier = Modifier
            .navigationBarsPadding(),
        sheetState = state,
        sheetElevation = 0.dp,
        scrimColor = ScrimColor,
        content = {},
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        sheetContent = {
            LaunchedEffect(key1 = taskListsState.value.taskLists.size) {
                heightOfBox = bottomPadding + itemPaddingWithSpacer * taskListsState.value.taskLists.size
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (heightOfBox == 0) bottomPadding.dp else heightOfBox.dp)
                    .padding(0.dp, 8.dp, 8.dp, 8.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(0.dp)
                ) {

                    itemsIndexed(taskListsState.value.taskLists) { index, item ->
                        ListItem(
                            modifier =
                            if (currentPageState() == index) {
                                Modifier
                                    .clip(
                                        RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                                    )
                                    .background(
                                        Color(0xFF82b1ff).copy(0.3f)
                                    )
                            } else {
                                Modifier
                            }.padding(start = 56.dp)
                                .noRippleClickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                        state.hide()
                                    }
                                },
                            text = { Text(item.name) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        SemiTransparentDivider()
                    }
                    item {
                        addTaskListItemButton()
                    }
                }
            }
        },
    )
}