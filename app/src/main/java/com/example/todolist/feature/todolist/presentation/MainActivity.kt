package com.example.todolist.feature.todolist.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.todolist.common.ui.theme.TodoListTheme
import com.example.todolist.common.util.synchronization.SynchronizeWorker
import com.example.todolist.feature.todolist.presentation.addEditTaskList.AddTaskListScreen
import com.example.todolist.feature.todolist.presentation.editTaskItem.EditTaskItemScreen
import com.example.todolist.feature.todolist.presentation.editTaskItem.EditTaskItemViewModel
import com.example.todolist.feature.todolist.presentation.todolist.TodoListScreen
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigInteger
import javax.inject.Inject

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalMaterialApi::class
)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var androidId : BigInteger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity"," ANDROID_ID :: $androidId")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val synchronizeWorkRequest =
            OneTimeWorkRequest.Builder(SynchronizeWorker::class.java)
                .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueue(synchronizeWorkRequest)
        setContent {
            TodoListTheme {
                ProvideWindowInsets(
                    windowInsetsAnimationsEnabled = false
                ) {
                    Surface(color = MaterialTheme.colors.background) {
                        val navController = rememberNavController()
                        val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
                            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                        }

                        NavHost(
                            navController = navController,
                            startDestination = Screen.TodoListScreen.route
                        ) {
                            composable(route = Screen.TodoListScreen.route) {
                                val editTaskItemViewModel =
                                    hiltViewModel<EditTaskItemViewModel>(viewModelStoreOwner = viewModelStoreOwner)
                                TodoListScreen(
                                    navController = navController,
                                    editTaskItemViewModel = editTaskItemViewModel
                                )

                            }
                            composable(
                                route = Screen.AddEditTaskListScreen.route +
                                        "?taskListId={taskListId}",
                                arguments = listOf(
                                    navArgument(
                                        name = "taskListId"
                                    ) {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) {
                                val taskListId = it.arguments?.getLong("taskListId") ?: -1L
                                AddTaskListScreen(
                                    navController = navController,
                                    taskListId = taskListId
                                )
                            }
                            composable(
                                route = Screen.EditTaskItemScreen.route +
                                        "?taskItemId={taskItemId}",
                                arguments = listOf(
                                    navArgument(
                                        name = "taskItemId"
                                    ) {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) {
                                val viewModel =
                                    hiltViewModel<EditTaskItemViewModel>(viewModelStoreOwner = viewModelStoreOwner)
                                val taskItemId = it.arguments?.getLong("taskItemId") ?: -1L
                                EditTaskItemScreen(
                                    navController = navController,
                                    taskItemId = taskItemId,
                                    viewModel = viewModel
                                )
                            }
                        }


                    }

                }


            }
        }
    }
}
