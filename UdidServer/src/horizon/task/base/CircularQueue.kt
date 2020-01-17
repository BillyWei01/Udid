package horizon.task.base

/**
 * FIFO queue
 *
 * We have tried to [java.util.LinkedList],
 * failed in java.util.LinkedList.remove(Object o),
 * same reason with [PriorityQueue]
 */
internal class CircularQueue<E> {
    private var head: Node<E>? = null
    private var tail: Node<E>? = null

    private class Node<E> internal constructor(internal var data: E) {
        internal var next: Node<E>? = null
    }

    internal fun offer(data: E) {
        val next = Node(data)
        if (head == null) {
            head = next
            tail = next
        } else {
            tail!!.next = next
            tail = next
        }
    }

    internal fun poll(): E? {
        val h: Node<E> = head ?: return null
        val e = h.data
        head = h.next
        if (head == null) {
            tail = null
        }
        return e
    }

    internal fun remove(o: Any): E? {
        val h: Node<E> = head ?: return null
        if (h.data == o) {
            return poll()
        }
        var prev: Node<E> = h
        var curr: Node<E>? = prev.next
        while (curr != null) {
            if (curr.data == o) {
                if (tail === curr) {
                    tail = prev
                }
                prev.next = curr.next
                return curr.data
            } else {
                prev = curr
                curr = curr.next
            }
        }
        return null
    }
}