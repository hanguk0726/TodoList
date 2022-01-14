package com.example.todolist.feature.todolist.presentation.todolist

import android.annotation.SuppressLint
import android.content.Context
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
import com.example.todolist.common.util.synchronization.executeSynchronizeWork
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import com.example.todolist.feature.todolist.presentation.util.TaskListsState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    private val taskListUseCases: TaskListUseCases,
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        const val TASK_LIST_POSITION_KEY = "task_list_position_key"
    }

    private val _taskItemTitle = mutableStateOf(
        TodoListTextFieldState(
            hint = "새 할 일"
        )
    )

    val taskItemTitle: State<TodoListTextFieldState> = _taskItemTitle

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var requestLatestDataJob: Job? = null

    private val _taskListsState = mutableStateOf(TaskListsState())
    val taskListsState: State<TaskListsState> = _taskListsState

    private var taskItemStateManagerPool = HashMap<Long, TaskItemStateManager>()

    private val _dialogType = mutableStateOf<DialogType>(DialogType.DeleteTaskList)
    val dialogType: State<DialogType> = _dialogType
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    init {
        requestLatestData()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            executeSynchronizeWork(appContext)
            delay(2000L)
            _isRefreshing.emit(false)
        }
    }

    fun requestSynchronizeWork() {
        executeSynchronizeWork(appContext)
    }

    private fun requestLatestData() {
        requestLatestDataJob?.cancel()
        requestLatestDataJob = getTaskLists().launchIn(viewModelScope)
    }

    private fun getTaskLists(): Flow<List<TaskList>> {
        return taskListUseCases.getTaskLists()
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
                taskLists.forEach {
                    loadTaskItemsOnPoolByTaskListId(it.id!!)
                }
            }
    }

    private fun getTaskItems(targetTaskListId: Long): List<TaskItem> {
        return if (taskItemStateManagerPool.containsKey(targetTaskListId)) {
            taskItemStateManagerPool[targetTaskListId]!!.taskItemsState.value.taskItems
        } else {
            emptyList()
        }
    }


    fun getTaskItemsToDisplay(targetTaskListId: Long): List<TaskItem> {

        return getTaskItems(targetTaskListId).filter { !it.needToBeDeleted }
    }

    @DelicateCoroutinesApi
    fun onEvent(event: TodoListEvent) {
        when (event) {
            is TodoListEvent.EnterTaskItemTitle -> {
                enterTaskItemTitle(event.value)
            }
            is TodoListEvent.ToggleTaskItemCompletionState -> {
                toggleTaskItemCompletionState(event.taskItem)
            }

            is TodoListEvent.DeleteTaskList -> {
                deleteTaskList(event.selectedTaskListId)
            }

            is TodoListEvent.ConfirmDeleteTaskList -> {
                confirmDeleteTaskList(event.selectedTaskListId)
            }
            is TodoListEvent.ConfirmDeleteCompletedTaskItems -> {
                confirmDeleteCompletedTaskItems(event.selectedTaskListId)
            }
            is TodoListEvent.DeleteCompletedTaskItems -> {
                deleteCompletedTaskItems(event.selectedTaskListId)
            }
            is TodoListEvent.SaveTaskItem -> {
                saveTaskItem(event.selectedTaskListId)
            }
            is TodoListEvent.SelectTaskList -> {
                selectTaskList(event.selectedTaskListId)
            }
        }
    }


    private fun loadTaskItemsOnPoolByTaskListId(targetTaskListId: Long) {
        val taskItemStateManager = if (taskItemStateManagerPool.containsKey(targetTaskListId)) {
            taskItemStateManagerPool[targetTaskListId]!!
        } else {
            TaskItemStateManager()
        }

        taskItemStateManager.getTaskItemsJob?.cancel()
        taskItemStateManager.getTaskItemsJob = taskItemUseCases.getTaskItemsByTaskListId(
            targetTaskListId
        ).onEach { result ->
            taskItemStateManager._taskItemsState.value =
                taskItemStateManager.taskItemsState.value.copy(
                    taskItems = result
                )
            taskItemStateManagerPool[targetTaskListId] = taskItemStateManager
            val validIds = taskListsState.value.taskLists.map { el -> el.id }.toList()
            val newTaskItemStateManagerPool = HashMap<Long, TaskItemStateManager>()
            for (i in validIds) {
                taskItemStateManagerPool[i]?.let {
                    newTaskItemStateManagerPool[i!!] = it
                }
            }
            taskItemStateManagerPool = newTaskItemStateManagerPool
            _eventFlow.emit(UiEvent.RequestRecompose)
        }.launchIn(viewModelScope)
    }


    private suspend fun initializeFirstTaskList(taskList: TaskList): Long {
        return try {
            taskListUseCases.addTaskList(taskList).first()
        } catch (e: Exception) {
            Log.e("TodoListViewModel", e.message ?: "Couldn't create the taskList")
            -1L
        }
    }

    private suspend fun saveLastSelectedTaskListId(id: Int) {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = id
        }
    }

    private fun readLastSelectedTaskListId(): Int {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        return runBlocking {
            dataStore.data.map {
                it[dataStoreKey] ?: -1
            }.first()
        }
    }

    fun clearTaskItemTitleTextField() {
        _taskItemTitle.value = taskItemTitle.value.copy(
            text = "",
            isHintVisible = true
        )
    }

    fun loadLastSelectedTaskListPosition(): Int {
        val lastSelectedTaskListId = readLastSelectedTaskListId()
        if (lastSelectedTaskListId == -1) return 0
        val taskLists = runBlocking {
            taskListUseCases.getTaskLists.noFlow()
        }
        val lastSelectedTaskList =
            taskLists.find { el -> el.id!!.toInt() == lastSelectedTaskListId }
        lastSelectedTaskList?.let {
            return taskLists.indexOf(lastSelectedTaskList)
        }
        return 0
    }

    private fun enterTaskItemTitle(value: String) {
        _taskItemTitle.value = taskItemTitle.value.copy(
            text = value,
        )
        _taskItemTitle.value = taskItemTitle.value.copy(
            isHintVisible = taskItemTitle.value.text.isBlank(),
        )
    }

    private fun toggleTaskItemCompletionState(taskItem: TaskItem) {
        viewModelScope.launch {
            try {
                val modified = taskItem.copy(
                    isCompleted = !taskItem.isCompleted
                )
                taskItemUseCases.updateTaskItem(modified)
                if (modified.isCompleted) {
                    _eventFlow.emit(UiEvent.ShowSnackbar(
                        message = "할 일 1개가 완료됨",
                        actionLabel = "실행취소",
                        action = {
                            viewModelScope.launch {
                                taskItemUseCases.updateTaskItem(taskItem)
                            }
                        }
                    ))
                } else {
                    _eventFlow.emit(UiEvent.ShowSnackbar(
                        message = "할 일 1개가 미완료로 표시됨",
                        actionLabel = "실행취소",
                        action = {
                            viewModelScope.launch {
                                taskItemUseCases.updateTaskItem(taskItem)
                            }
                        }
                    ))
                }
            } catch (e: InvalidTaskItemException) {
                Log.e("TodoListViewModel", e.message ?: "Couldn't update taskItem")
            }
        }
    }

    @DelicateCoroutinesApi
    private fun deleteTaskList(selectedTaskListId: Long) {
        GlobalScope.launch {
            _eventFlow.emit(UiEvent.ScrollTaskListPosition(0))
        }
        viewModelScope.launch {
            try {
                val selectedTaskList =
                    taskListsState.value.taskLists
                        .find { el -> el.id!! == selectedTaskListId }
                taskListUseCases.deleteTaskList(selectedTaskList!!)
                val taskItemsToDelete =
                    taskItemUseCases.getTaskItemsByTaskListId.noFlow(selectedTaskList.id!!)
                taskItemUseCases.deleteTaskItem(*taskItemsToDelete.toTypedArray())
            } catch (e: InvalidTaskListException) {
                Log.e("TodoListViewModel", e.message ?: "Couldn't delete taskList")
            }
        }
    }

    @DelicateCoroutinesApi
    private fun confirmDeleteTaskList(selectedTaskListId: Long) {
        if (getTaskItems(selectedTaskListId).isNotEmpty()) {
            viewModelScope.launch {
                _dialogType.value = DialogType.DeleteTaskList
                _eventFlow.emit(UiEvent.ShowConfirmDialog)
            }
        } else {
            viewModelScope.launch {
                onEvent(TodoListEvent.DeleteTaskList(selectedTaskListId))
                _eventFlow.emit(UiEvent.CloseMenuRightModalBottomSheet)
            }
        }
    }

    private fun confirmDeleteCompletedTaskItems(selectedTaskListId: Long) {
        viewModelScope.launch {
            val taskItems = getTaskItems(selectedTaskListId)
            if (taskItems.isNotEmpty()) {
                val completedTaskItems = taskItems.filter { el ->
                    el.isCompleted
                }
                if (completedTaskItems.isNotEmpty()) {
                    _dialogType.value = DialogType.DeleteCompletedTaskItem
                    _eventFlow.emit(UiEvent.ShowConfirmDialog)
                }
            }
        }
    }

    private fun deleteCompletedTaskItems(selectedTaskListId: Long) {
        viewModelScope.launch {
            try {
                val taskItems = getTaskItems(selectedTaskListId)
                if (taskItems.isNotEmpty()) {
                    val completedTaskItems = taskItems.filter { el ->
                        el.isCompleted
                    }
                    if (completedTaskItems.isNotEmpty()) {
                        taskItemUseCases.deleteTaskItem(*completedTaskItems.toTypedArray())
                    }
                }
            } catch (e: InvalidTaskItemException) {
                Log.e(
                    "TodoListViewModel",
                    e.message ?: "Couldn't delete completed taskItems"
                )
            }
        }
    }

    private fun saveTaskItem(selectedTaskListId: Long) {
        viewModelScope.launch {
            try {
                taskItemUseCases.addTaskItem(
                    TaskItem(
                        title = taskItemTitle.value.text,
                        taskListId = selectedTaskListId
                    )
                )
                _eventFlow.emit(UiEvent.SaveTaskItem)
            } catch (e: InvalidTaskItemException) {
                Log.e("TodoListViewModel", e.message ?: "Couldn't save taskItem")
            }

        }
    }

    private fun selectTaskList(selectedTaskListId: Long) {
        viewModelScope.launch {
            saveLastSelectedTaskListId(selectedTaskListId.toInt())
        }
    }

    data class TaskItemStateManager(
        val _taskItemsState: MutableState<TaskItemsState> = mutableStateOf(TaskItemsState()),
        val taskItemsState: State<TaskItemsState> = _taskItemsState,
        var getTaskItemsJob: Job? = null
    )

    sealed class DialogType {
        object DeleteTaskList : DialogType()
        object DeleteCompletedTaskItem : DialogType()
    }

    sealed class UiEvent {
        data class ShowSnackbar(
            val message: String,
            val actionLabel: String? = null,
            val action: () -> Unit
        ) : UiEvent()

        data class ScrollTaskListPosition(val index: Int) : UiEvent()
        object ShowConfirmDialog : UiEvent()
        object SaveTaskItem : UiEvent()
        object CloseMenuRightModalBottomSheet : UiEvent()
        // LazyColumn DOES NOT get noticed data change in same data holder object
        // so it needs to be recomposed manually
        object RequestRecompose: UiEvent()
    }
}