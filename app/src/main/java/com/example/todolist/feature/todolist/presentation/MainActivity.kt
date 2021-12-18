package com.example.todolist.feature.todolist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.feature.todolist.presentation.addEditTaskList.AddTaskListScreen
import com.example.todolist.feature.todolist.presentation.todolist.TodoListScreen
import com.example.todolist.feature.todolist.presentation.util.Screen
import com.example.todolist.ui.theme.TodoListTheme
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(
        ExperimentalFoundationApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalComposeUiApi::class,
        ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TodoListTheme {
                ProvideWindowInsets(
                    windowInsetsAnimationsEnabled = false
                ) {
                    Surface(color = MaterialTheme.colors.background) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Screen.TodoListScreen.route
                        ) {
                            composable(route = Screen.TodoListScreen.route) {
                                TodoListScreen(
                                    navController = navController
                                )
                            }
                            composable(
                                route = Screen.AddEditTaskListScreen.route +
                                        "?isForAdd={isForAdd}&taskListId={taskListId}",
                                arguments = listOf(
                                    navArgument(
                                        name = "isForAdd"
                                    ){
                                        type = NavType.BoolType
                                    },
                                    navArgument(
                                        name = "taskListId"
                                    ) {
                                        type = NavType.LongType
                                        nullable = true
                                    }
                                )
                            ) {
                                val isForAdd = it.arguments?.getBoolean("isForAdd") ?: true
                                AddTaskListScreen(
                                    navController = navController,
                                    isForAdd = isForAdd
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
