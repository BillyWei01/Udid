package horizon.task

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


object TaskCenter {
    private val cpuCount = Math.min(4, Runtime.getRuntime().availableProcessors())
    internal val executor: ThreadPoolExecutor = ThreadPoolExecutor(cpuCount, cpuCount,
            60L, TimeUnit.SECONDS, LinkedBlockingDeque())
}