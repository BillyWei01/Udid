package com.horizon.task.base

/**
 * Define priority of task.
 * Supply four levels:
 * IMMEDIATE: task will be execute immediately,
 * HIGH, NORMAL, LOW: task in high, normal, low priority.
 */
object Priority {
    const val IMMEDIATE = Integer.MAX_VALUE
    const val HIGH = 1
    const val NORMAL = 0
    const val LOW = -1
}
