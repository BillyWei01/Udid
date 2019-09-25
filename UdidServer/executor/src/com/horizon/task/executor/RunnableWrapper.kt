package com.horizon.task.executor

@Suppress("EqualsOrHashCode")
internal class RunnableWrapper constructor(
        private val r: Runnable,
        private val trigger: Trigger) : Runnable {
    override fun run() {
        try {
            r.run()
        } finally {
            trigger.next()
        }
    }

    override fun equals(other: Any?): Boolean {
        // 这个 r == other 很重要，因为通常是用外部的Runnable来索引队列中的元素，
        return this === other || r === other
    }
}