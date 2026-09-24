package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

object YouPlayerLogger {
    private const val MAX_LOGS = 200
    private val buffer = ConcurrentLinkedDeque<LogEntry>()
    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()

    private fun addLog(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(level = level, tag = tag, message = message, throwable = throwable)
        buffer.addFirst(entry)
        while (buffer.size > MAX_LOGS) {
            buffer.removeLast()
        }
        _logsFlow.value = buffer.toList()

        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }

    fun d(tag: String, message: String) = addLog(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = addLog(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) = addLog(LogLevel.WARN, tag, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) = addLog(LogLevel.ERROR, tag, message, throwable)

    fun clear() {
        buffer.clear()
        _logsFlow.value = emptyList()
    }
}
