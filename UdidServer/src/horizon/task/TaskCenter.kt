package horizon.task

import horizon.task.executor.LaneExecutor
import horizon.task.executor.PipeExecutor
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 这一套Executor本来是给Android终端设计的，但用来跑服务端也没有问题
 *
 * 链接：https://github.com/No89757/Task
 */
object TaskCenter {
    private val cpuCount = Runtime.getRuntime().availableProcessors()
    private val threadFactory = object : ThreadFactory {
        private val mCount = AtomicInteger(1)
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "Task #" + mCount.getAndIncrement())
        }
    }

    internal val poolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
            5, 256,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(),
            threadFactory)

    // 常规的任务调度器，可控制任务并发，支持任务优先级
    val io = PipeExecutor(64)
    val computation = PipeExecutor(Math.min(Math.max(2, cpuCount), 8), 1024)

//    val laneIO = LaneExecutor(io, true)
//    val laneCP = LaneExecutor(computation, true)

    // 相同的tag的任务会被串行执行，相当于串行的Executor(不过是tag相同的任务各自串行）
    // 可以用于一些日志类的任务
    val serial = LaneExecutor(PipeExecutor(Math.min(Math.max(2, cpuCount), 4)))
}