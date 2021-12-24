package com.example.todolist.feature.todolist.presentation.todolist

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import com.example.todolist.feature.todolist.presentation.util.TaskListsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

//savedStateHandle

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    private val taskListUseCases: TaskListUseCases,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        const val TASK_LIST_POSITION_KEY = "task_list_position_key"
    }

    private var selectedTaskListId: Long? = null

    private val _taskItemTitle = mutableStateOf(
        TodoListTextFieldState(
            hint = "새 할 일"
        )
    )

    val taskItemTitle: State<TodoListTextFieldState> = _taskItemTitle

    private val _lastSelectedTaskListPositionLoaded = mutableStateOf(false)
    val lastSelectedTaskListPositionLoaded = _lastSelectedTaskListPositionLoaded

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getTaskListsJob: Job? = null

    private val _taskListsState = mutableStateOf(TaskListsState())
    val taskListsState: State<TaskListsState> = _taskListsState

    private val taskItemManagerPool = HashMap<Long, TaskItemManager>()

    private val _dialogType = mutableStateOf<DialogType>(DialogType.DeleteTaskList)
    val dialogType: State<DialogType> = _dialogType

    init {
        getTaskLists()
    }


    fun getTaskItems(targetTaskListId: Long):List<TaskItem>  {
        return if(taskItemManagerPool.containsKey(targetTaskListId)){
            taskItemManagerPool[targetTaskListId]!!.taskItemsState.value.taskItems
        } else {
            emptyList()
        }
    }

    fun onEvent(event: TodoListEvent) {
        when (event) {
            is TodoListEvent.EnterTaskItemTitle -> {
                _taskItemTitle.value = taskItemTitle.value.copy(
                    text = event.value,
                )
                // 한번에 init 하면 같은 시점에 대해서 isBlank 판정이 안된다.
                _taskItemTitle.value = taskItemTitle.value.copy(
                    isHintVisible = taskItemTitle.value.text.isBlank(),
                )
            }
            is TodoListEvent.ToggleTaskItemCompletionState -> {
               viewModelScope.launch {
                   try {
                       val original = event.taskItem
                       val modified =  original.copy(
                           isCompleted = !original.isCompleted
                       )
                       taskItemUseCases.updateTaskItem(modified)
                       if(modified.isCompleted){
                           _eventFlow.emit(UiEvent.ShowSnackbar(
                               message = "할 일 1개가 완료됨",
                               actionLabel = "실행취소",
                               action = {
                                   viewModelScope.launch {
                                       taskItemUseCases.updateTaskItem(original)
                                       _eventFlow.emit(UiEvent.CancelToggleTaskItemCompletionState)
                                   }
                               }
                           ))
                       }else{
                           _eventFlow.emit(UiEvent.ShowSnackbar(
                               message = "할 일 1개가 미완료로 표시됨",
                               actionLabel = "실행취소",
                               action = {
                                   viewModelScope.launch {
                                       taskItemUseCases.updateTaskItem(original)
                                       _eventFlow.emit(UiEvent.CancelToggleTaskItemCompletionState)
                                   }
                               }
                           ))
                       }
                   } catch(e: InvalidTaskItemException) {
                       Log.e("TodoListViewModel","${e.message ?: "Couldn't update taskItem"}")
                   }
               }
            }

            is TodoListEvent.DeleteTaskItem -> {

            }

            is TodoListEvent.DeleteTaskList -> {
                GlobalScope.launch {
                    _eventFlow.emit(UiEvent.ScrollTaskListPosition(0))
                }
                viewModelScope.launch {
                    try {
                        val selectedTaskList =
                            taskListsState.value.taskLists
                                .find{ el -> el.id!! == selectedTaskListId}
                        taskListUseCases.deleteTaskList(selectedTaskList!!)
                    } catch(e: InvalidTaskListException) {
                        Log.e("TodoListViewModel","${e.message ?: "Couldn't delete taskList"}")
                    }
                }
            }

            is TodoListEvent.ConfirmDeleteTaskList -> {
                if(getTaskItems(event.selectedTaskListId).isNotEmpty()){
                    viewModelScope.launch {
                        _dialogType.value = DialogType.DeleteTaskList
                        _eventFlow.emit(UiEvent.ShowConfirmDialog)
                    }
                } else {
                    viewModelScope.launch {
                        onEvent(TodoListEvent.DeleteTaskList)
                        _eventFlow.emit(UiEvent.CloseMenuRightModalBottomSheet)
                    }
                }
            }
            is TodoListEvent.ConfirmDeleteCompletedTaskItems -> {
                viewModelScope.launch {
                    val taskItems = getTaskItems(event.selectedTaskListId)
                    if(taskItems.isNotEmpty()){
                        val completedTaskItems = taskItems.filter { el ->
                            el.isCompleted
                        }
                        if(completedTaskItems.isNotEmpty()){
                            _dialogType.value = DialogType.DeleteCompletedTaskItem
                            _eventFlow.emit(UiEvent.ShowConfirmDialog)
                        }
                    }
                }
            }
            is TodoListEvent.DeleteCompletedTaskItems -> {
                viewModelScope.launch {
                    try {
                        val taskItems = getTaskItems(selectedTaskListId!!)
                        if(taskItems.isNotEmpty()){
                            val completedTaskItems = taskItems.filter { el ->
                                el.isCompleted
                            }
                            if(completedTaskItems.isNotEmpty()){
                                taskItemUseCases.deleteTaskItem(*completedTaskItems.toTypedArray())
                            }
                        }
                    } catch(e: InvalidTaskItemException) {
                        Log.e("TodoListViewModel","${e.message ?: "Couldn't delete completed taskItems"}")
                    }
                }

            }
            is TodoListEvent.SaveTaskItem -> {
                viewModelScope.launch {
                    try {
                        taskItemUseCases.addTaskItem(
                            TaskItem(
                                title = taskItemTitle.value.text,
                                taskListId = selectedTaskListId!!
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveTaskItem)
                    } catch (e: InvalidTaskItemException) {
                        Log.e("TodoListViewModel","${e.message ?: "Couldn't save taskItem"}")
                    }

                }
            }
            is TodoListEvent.SelectTaskList -> {
                selectedTaskListId = event.selectedTaskListId
                viewModelScope.launch {
                    saveLastSelectedTaskListId(event.selectedTaskListId.toInt())
                }
            }
            // 호출 빈도 수 조절
            is TodoListEvent.GetTaskItemsByTaskListId -> {
                loadTaskItemsOnPoolByTaskListId(event.taskListId)
                clearDeletedTaskItemManager()
            }
            is TodoListEvent.LoadLastSelectedTaskListPosition -> {
                viewModelScope.launch {
                    val lastSelectedTaskListId = readLastSelectedTaskListId()
                    if(lastSelectedTaskListId != -1){
                        val lastSelectedTaskList = taskListsState.value.taskLists
                            .find{ el -> el.id!!.toInt() == lastSelectedTaskListId}
                        lastSelectedTaskList?.let{
                            selectedTaskListId = lastSelectedTaskList.id
                            val index = taskListsState.value.taskLists.indexOf(lastSelectedTaskList)
                            _eventFlow.emit(UiEvent.ScrollTaskListPosition(index))
                        }
                    }
                }

            }
            is TodoListEvent.LastTaskListPositionHasSelected -> {
                _lastSelectedTaskListPositionLoaded.value = true
            }
        }
    }


    private fun loadTaskItemsOnPoolByTaskListId(targetTaskListId: Long) {
        var taskItemManager = if(taskItemManagerPool.containsKey(targetTaskListId)){
            taskItemManagerPool[targetTaskListId]!!
        } else {
            TaskItemManager()
        }
        taskItemManager.getTaskItemsJob?.cancel()
        taskItemManager.getTaskItemsJob = taskItemUseCases.getTaskItemsByTaskListId(
            targetTaskListId
        ).onEach { taskItems ->
            taskItemManager._taskItemsState.value = taskItemManager.taskItemsState.value.copy(
                taskItems = taskItems
            )
            taskItemManagerPool[targetTaskListId] = taskItemManager
        }.launchIn(viewModelScope)
    }

    private fun clearDeletedTaskItemManager() {
        val validIds = taskListsState.value.taskLists.map { el -> el.id}.toList()
        val allIds = taskItemManagerPool.keys
        for (i in allIds){
            if(validIds.contains(i)){
                taskItemManagerPool.remove(i)
            }
        }
    }

    private fun getTaskLists() {
        getTaskListsJob?.cancel()
        getTaskListsJob = taskListUseCases.getTaskLists()
            .onEach { taskLists ->
                if (taskLists.isEmpty()) {
                    val initialTaskList = TaskList(
                        name = "할 일 목록",
                        createdTimestamp = System.currentTimeMillis(),
                        id = null
                    )
                    val taskListId = initializeFirstTaskList(initialTaskList)
                    _taskListsState.value = taskListsState.value.copy(
                        taskLists = listOf(initialTaskList.copy(id = taskListId))
                    )
                } else {
                    _taskListsState.value = taskListsState.value.copy(
                        taskLists = taskLists
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initializeFirstTaskList(taskList: TaskList): Long =
        withContext(viewModelScope.coroutineContext) {
            return@withContext try {
                taskListUseCases.addTaskList(taskList).first()
            } catch (e: Exception) {
                Log.e("TodoListViewModel","${e.message ?: "Couldn't create the taskList"}")
                -1L
            }
        }

    private suspend fun saveLastSelectedTaskListId(id: Int) {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = id
        }
    }

    private suspend fun readLastSelectedTaskListId(): Int {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        return dataStore.data.map { preferences ->
            preferences[dataStoreKey] ?: -1
        }.first()
    }

    fun clearTaskItemTitleTextField() {
        _taskItemTitle.value = taskItemTitle.value.copy(
            text = "",
            isHintVisible = true
        )
    }

    data class TaskItemManager(
        val _taskItemsState: MutableState<TaskItemsState> = mutableStateOf(TaskItemsState()),
        val taskItemsState: State<TaskItemsState> = _taskItemsState,
        var getTaskItemsJob: Job? = null
    )

    sealed class DialogType{
        object DeleteTaskList: DialogType()
        object DeleteCompletedTaskItem: DialogType()
    }

    sealed class UiEvent {
        data class ShowSnackbar(
            val message: String,
            val actionLabel: String? = null,
            val action: () -> Unit
        ): UiEvent()
        data class ScrollTaskListPosition(val index: Int) : UiEvent()
        object CancelToggleTaskItemCompletionState : UiEvent()
        object ShowConfirmDialog : UiEvent()
        object SaveTaskList : UiEvent()
        object SaveTaskItem : UiEvent()
        object CompleteTaskItem : UiEvent()
        object RestoreTaskItemFromCompletion : UiEvent()
        object DeleteTaskItem : UiEvent()
        object DeleteTaskList : UiEvent()
        object CloseMenuRightModalBottomSheet: UiEvent()
    }
}