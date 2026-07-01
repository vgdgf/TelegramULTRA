package com.example.telegramultra.media

import android.content.Context
import android.os.FileObserver
import android.util.Log
import java.io.File

class MediaScanner(private val context: Context) {

    private val observers = mutableListOf<RecursiveFileObserver>()
    private var callback: ((String) -> Unit)? = null

    private val telegramPaths = listOf(
        "/storage/emulated/0/Telegram",
        "/storage/emulated/0/Android/media/org.telegram.messenger",
        "/storage/emulated/0/Android/media/org.telegram.messenger.web",
        "/storage/emulated/0/Android/media/org.telegram.messenger.beta"
    )

    private val allowedExtensions = setOf(
        "jpg", "jpeg", "png", "gif", "webp",
        "mp4", "mov", "avi", "mkv",
        "mp3", "ogg", "opus", "m4a", "wav",
        "pdf", "doc", "docx", "zip"
    )

    fun observe(onNewFile: (String) -> Unit) {
        callback = onNewFile
        telegramPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                val observer = RecursiveFileObserver(dir) { file ->
                    if (isAllowed(file)) {
                        Log.d(TAG, "New file detected: $file")
                        callback?.invoke(file)
                    }
                }
                observer.startWatching()
                observers.add(observer)
                Log.i(TAG, "Watching: $path")
            } else {
                Log.d(TAG, "Path not found (skipped): $path")
            }
        }
    }

    fun stop() {
        observers.forEach { it.stopWatching() }
        observers.clear()
        callback = null
        Log.i(TAG, "MediaScanner stopped")
    }

    private fun isAllowed(filePath: String): Boolean {
        val ext = File(filePath).extension.lowercase()
        return ext in allowedExtensions
    }

    companion object {
        private const val TAG = "MediaScanner"
    }
}

/**
 * FileObserver that recursively watches a directory and all subdirectories.
 */
class RecursiveFileObserver(
    private val rootDir: File,
    private val onCreated: (String) -> Unit
) {
    private val observers = mutableListOf<FileObserver>()

    fun startWatching() {
        addObserversRecursive(rootDir)
    }

    fun stopWatching() {
        observers.forEach { it.stopWatching() }
        observers.clear()
    }

    private fun addObserversRecursive(dir: File) {
        val observer = object : FileObserver(dir, CLOSE_WRITE or MOVED_TO) {
            override fun onEvent(event: Int, path: String?) {
                if (path == null) return
                val fullPath = "${dir.absolutePath}/$path"
                val file = File(fullPath)
                when {
                    event and CLOSE_WRITE != 0 && file.isFile -> onCreated(fullPath)
                    event and MOVED_TO != 0 && file.isFile -> onCreated(fullPath)
                    event and MOVED_TO != 0 && file.isDirectory -> addObserversRecursive(file)
                }
            }
        }
        observer.startWatching()
        observers.add(observer)

        dir.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
            addObserversRecursive(subDir)
        }
    }
}
