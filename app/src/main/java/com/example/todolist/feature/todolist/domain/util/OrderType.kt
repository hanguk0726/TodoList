package com.example.todolist.feature.todolist.domain.util

sealed class OrderType {
    object Ascending: OrderType()
    object Descending: OrderType()
}
