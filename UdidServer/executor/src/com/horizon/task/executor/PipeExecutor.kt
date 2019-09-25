package com.horizon.task.executor

import com.horizon.task.TaskCenter
import com.horizon.task.base.Priority
import com.horizon.task.base.PriorityQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

/**
 * [PipeExecutor] 主要用于控制任务并发
 *
 * PipeExecutor 支持任务优先级，相同优先级的任务按先进先出的顺序执行。
 *
 * [windowSize]: 并发窗口，控制同时执行的任务数量；
 * 若 [windowSize] = 1, 相当于串行执行器。
 *
 * [capacity]: 任务缓冲区容量，超过容量会执行[rejectedHandler]，
 * 若 [capacity] <= 0, 不限制容量。
 */
class PipeExecutor @JvmOverloads constructor(
        windowSize: Int,
        private val capacity: Int = -1,
        private val rejectedHandler: RejectedExecutionHandler = defaultHandler) : TaskExecutor {

    private val tasks = PriorityQueue<RunnableWrapper>()
    private val windowSize: Int = if (windowSize > 0) windowSize else 1
    private var count = 0

    companion object {
        val defaultHandler = ThreadPoolExecutor.AbortPolicy()
    }


     private val trigger: Trigger = object : Trigger {
        override fun next() {
            scheduleNext()
        }
    }

    override fun execute(r: Runnable) {
        schedule(RunnableWrapper(r, trigger), Priority.NORMAL)
    }

    fun execute(r: Runnable, priority: Int) {
        schedule(RunnableWrapper(r, trigger), priority)
    }

    fun execute(r: () -> Unit, priority: Int = Priority.NORMAL) {
        execute(Runnable { r.invoke() }, priority)
    }

    @Synchronized
    internal fun scheduleNext() {
        count--
        if (count < windowSize) {
            startTask(tasks.poll())
        }
    }

    @Synchronized
    internal fun schedule(r: RunnableWrapper, priority: Int) {
        if (capacity > 0 && tasks.size() >= capacity) {
            rejectedHandler.rejectedExecution(r, TaskCenter.poolExecutor)
        }
        if (count < windowSize || priority == Priority.IMMEDIATE) {
            startTask(r)
        } else {
            tasks.offer(r, priority)
        }
    }

    private fun startTask(active: Runnable?) {
        if (active != null) {
            count++
            TaskCenter.poolExecutor.execute(active)
        }
    }

    @Synchronized
    override fun changePriority(r: Runnable, priority: Int, increment: Int): Int {
        val active = tasks.remove(r, priority)
        if (active != null) {
            val newPriority = priority + increment
            tasks.offer(active, newPriority)
            return newPriority
        }
        return priority
    }
}
