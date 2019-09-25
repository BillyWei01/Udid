package com.horizon.task.executor

import com.horizon.task.base.CircularQueue
import com.horizon.task.base.Priority
import java.util.*
import java.util.concurrent.Future

/**
 * [LaneExecutor] 主要用于避免任务重复执行。
 *
 * 其原理为：
 * 1、给任务打tag，相同tag的任务视为相同的任务；
 * 2、记录进入调度的任务（委托给[executor]);
 * 3、任务提交过来，如果已经有相同的任务在进入调度，需要等待；
 * 4、若需要等待,有两种模式：丢弃模式([discard]=true)和非丢弃模式，
 *    丢弃模式，只能有一个任务等待执行，过滤后面的任务;
 *    非丢弃模式，都放入FIFO的队列（[waitingQueues]）中。
 *
 * 效果：
 * 1、tag相同的任务会串行，不同tag的任务会并发执行;
 * 2、丢弃模式可以过滤冗余执行，
 *    比如“收到更新通知->重新加载->显示”的任务，即使（几乎）同时收到通知，
 *    最多也只是一个执行，一个等待（防止前面的任务加载完了但是任务没结束，从而不能正确更新);
 * 3、非丢弃模式可以防止重复执行,
 *    比如图片加载，当相同的源的任务同时发起，如果并行执行，则会重复加载，
 *    如果使之串行，配合缓存，第一个任务完成后，后面的任务可以直接取缓存，而避免重复加载；
 *    此外，非丢弃模式下，还可以当串行执行器用（tag相同串行执行，任务队列无限容量）。
 */
class LaneExecutor(private val executor: PipeExecutor,
                   private val discard: Boolean = false) : TaskExecutor {
    // 正在调度的任务
    private val scheduledTasks = HashMap<String, Runnable>()
    // 丢弃模式下等待的任务
    private val waitingTasks by lazy { HashMap<String, TaskWrapper>() }
    // 非丢弃模式下等待的任务
    private val waitingQueues by lazy { HashMap<String, CircularQueue<TaskWrapper>>() }

    private class TaskWrapper(val r: Runnable, val priority: Int)

    private inner class LaneTrigger(val tag : String) : Trigger {
        override fun next() {
            executor.scheduleNext()
            scheduleNext(tag)
        }
    }

    private fun start(r: Runnable, tag: String, priority: Int) {
        scheduledTasks[tag] = r
        executor.schedule(RunnableWrapper(r, LaneTrigger(tag)), priority)
    }

    @Synchronized
    fun scheduleNext(tag: String) {
        scheduledTasks.remove(tag)
        if (discard) {
            waitingTasks.remove(tag)?.let { start(it.r, tag, it.priority) }
        } else {
            waitingQueues[tag]?.let { queue ->
                val wrapper = queue.poll()
                if (wrapper == null) {
                    // 如果队列清空了，则顺便把队列从HashMap移除，不然HashMap只增不减，浪费内存
                    waitingQueues.remove(tag)
                } else {
                    start(wrapper.r, tag, wrapper.priority)
                }
            }
        }
    }

    override fun execute(r: Runnable) {
        executor.execute(r)
    }

    @Synchronized
    fun execute(tag: String, r: Runnable, priority: Int = Priority.NORMAL) {
        if (tag.isEmpty()) {
            executor.execute(r, priority)
        } else if (!scheduledTasks.containsKey(tag)) {
            start(r, tag, priority)
        } else if (discard) {
            if (waitingTasks.containsKey(tag)) {
                // 丢弃模式下，如果有相同的任务在等待，则丢弃传进来的任务
                // 而如果传进来的又是 Futures(实现了Runnable), 则顺便调用其cancel()方法
                if (r is Future<*>) {
                    r.cancel(false)
                }
            } else {
                waitingTasks[tag] = TaskWrapper(r, priority)
            }
        } else {
            // 非丢弃模式下，每个tag都有一个无界队列可以缓存任务
            val queue = waitingQueues[tag]
                    ?: CircularQueue<TaskWrapper>().apply { waitingQueues[tag] = this }
            queue.offer(TaskWrapper(r, priority))
        }
    }

    fun execute(tag: String, r: () -> Unit, priority: Int = Priority.NORMAL) {
        execute(tag, Runnable { r.invoke() }, priority)
    }

    override fun changePriority(r: Runnable, priority: Int, increment: Int): Int {
        return executor.changePriority(r, priority, increment)
    }
}