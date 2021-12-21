package com.example.todolist.feature.todolist.presentation.editTaskItem

import com.example.todolist.feature.todolist.presentation.addEditTaskList.AddEditTaskListEvent

sealed class EditTaskItemEvent{
    data class EnterTaskItemTitle(val value: String): EditTaskItemEvent()
    data class EnterTaskItemDetail(val value: String): EditTaskItemEvent()
    object SaveTaskItem: EditTaskItemEvent()
    object DeleteTaskItem: EditTaskItemEvent()


}
