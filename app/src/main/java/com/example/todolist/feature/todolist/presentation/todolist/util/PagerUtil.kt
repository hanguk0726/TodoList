package com.example.todolist.feature.todolist.presentation.todolist.util

import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlin.math.absoluteValue
import kotlin.math.max

@OptIn(ExperimentalPagerApi::class)
fun getTargetPage(
    pagerState: PagerState,
): Int {
    val targetDistance = (pagerState.targetPage - pagerState.currentPage).absoluteValue
    val fraction = (pagerState.currentPageOffset / max(targetDistance, 1)).absoluteValue
    return if (fraction > 0.5f) {
        pagerState.targetPage
    } else {
        pagerState.currentPage
    }
}