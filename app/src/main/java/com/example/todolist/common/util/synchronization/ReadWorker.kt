package com.example.todolist.common.util.synchronization

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReadWorker (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    override fun doWork(): Result {


        return Result.success()
    }
}
