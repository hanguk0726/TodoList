package com.example.todolist.feature.todolist.presentation.editTaskItem

sealed class EditTaskItemEvent{

    data class EnterTaskItemTitle(val value: String): EditTaskItemEvent()
    data class EnterTaskItemDetail(val value: String): EditTaskItemEvent()
    object SaveTaskItem: EditTaskItemEvent()
    object DeleteTaskItem: EditTaskItemEvent()
    object ToggleAndSaveTaskItemCompletionState : EditTaskItemEvent()

}
