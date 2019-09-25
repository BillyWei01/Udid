package com.horizon.task.executor


import java.util.concurrent.Executor

 interface TaskExecutor : Executor{
     fun changePriority(r: Runnable, priority: Int, increment: Int): Int
}