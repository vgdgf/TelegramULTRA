package com.example.telegramultra.queue

import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue

class EventQueue {

    private val queue = ConcurrentLinkedQueue<String>()

    fun push(filePath: String) {
        if (!queue.contains(filePath)) {
            queue.add(filePath)
            Log.d(TAG, "Queued: $filePath (queue size: ${queue.size})")
        }
    }

    fun consume(onNext: (String) -> Unit) {
        while (queue.isNotEmpty()) {
            queue.poll()?.let { onNext(it) }
        }
    }

    fun size(): Int = queue.size

    companion object {
        private const val TAG = "EventQueue"
    }
}
