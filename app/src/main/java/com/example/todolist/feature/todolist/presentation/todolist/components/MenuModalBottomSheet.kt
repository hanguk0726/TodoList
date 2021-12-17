package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.feature.todolist.presentation.components.SemiTransparentDivider
import com.example.todolist.feature.todolist.presentation.todolist.TaskListsState
import com.example.todolist.feature.todolist.presentation.todolist.util.getTargetPage
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.ui.theme.LightBlue
import com.example.todolist.ui.theme.ScrimColor
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun MenuModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    taskListsState: State<TaskListsState>,
    pagerState: PagerState,
    addTaskListItemButton: @Composable () -> Unit
) {

    val currentPageState = { getTargetPage(pagerState) }
    var heightOfBox = 90 + 60 * taskListsState.value.taskLists.size
    println("mylogger ${heightOfBox}")
    ModalBottomSheetLayout(
        modifier = Modifier
            .navigationBarsPadding(),
        sheetState = state,
        sheetElevation = 0.dp,
        scrimColor = ScrimColor,
        sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        sheetContent = {
            //TODO  정확한 계산
LaunchedEffect(key1 = taskListsState.value.taskLists.size){
    heightOfBox = 90 + 60 * taskListsState.value.taskLists.size
}
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (heightOfBox == 0) 90.dp else heightOfBox.dp)
                    .padding(8.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(0.dp)
                ){

                    itemsIndexed(taskListsState.value.taskLists){ index, item ->
                        ListItem(
                            modifier =
                                if(currentPageState()==index){
                                    Modifier
                                        .clip(
                                            RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                                        )
                                        .background(
                                            Color(0xFF82b1ff).copy(0.3f)
                                        )
                                    } else {
                                    Modifier
                                }.padding(start = 56.dp),
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
    ) {

    }
}