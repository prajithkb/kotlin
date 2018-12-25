package main.kotlin.hackerrank

import java.io.BufferedReader
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis

/******* utility functions *************/

data class DevOverrides(var isDebug: Boolean, var readFromFile: Boolean)


val devOverrides = DevOverrides(false, false)

inline fun <T : Any> T?.whenNotNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

inline fun <T : Any> T?.whenNull(f: () -> Unit) {
    if (this == null) f()
}

enum class Level {
    ERROR,
    DEBUG,
    ACTUAL
}

fun log(message: Any?, level: Level) {
    when (level) {
        Level.DEBUG, Level.ERROR -> if (devOverrides.isDebug) println("$level: $message")
        Level.ACTUAL -> println(message)
    }
}

fun log(message: Any?) {
    println(message)
}

fun debugLog(message: Any) {
    debugLog(Level.DEBUG) { message.toString() }
}

fun debugLog(level: Level = Level.DEBUG, message: Any) {
    debugLog(level) { message.toString() }
}

inline fun debugLog(level: Level = Level.DEBUG, block: () -> Any) {
    if (devOverrides.isDebug) {
        log(block(), level)
    }

}

inline fun <T> withTimeToExecution(operationName: String = "Overall", block: () -> T): T {
    var value: T? = null
    val time = measureTimeMillis {
        value = block()
    }
    debugLog { "Elapsed time: $time ms for Operation: $operationName" }
    return value!!
}

fun <T> completeWithin(timeoutInMilliSecs: Long = 5000, block: () -> T): T? {
    try {
        return CompletableFuture
            .supplyAsync(block)
            .get(timeoutInMilliSecs, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
        debugLog(Level.ERROR, "Failed to complete within $timeoutInMilliSecs ms")
        return null
    }
}

fun setDevelopmentFlag(args: Array<String>) {
    devOverrides.isDebug = args.contains("test")
    devOverrides.readFromFile = args.contains("readFromFile")
}


fun scanner(): Scan {
    if (devOverrides.readFromFile) {
        return Scan(File("/Users/kprajith/Desktop/REMOVE_THIS.txt").bufferedReader())
    } else {
        return Scan(System.`in`.bufferedReader())
    }
}

class Scan(val reader: BufferedReader) {
    fun nextLine(): String {
        return reader.readLine()
    }


}


/******* utility functions *************/

enum class MATCHTYPE {
    UPPERCASE_MATCH,
    MATCH,
    MISMATCH,
    LOWERCASE_MISMATCH
}


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    completeWithin(500000) {
        withTimeToExecution("main") {
            val q = scan.nextLine().trim().toInt()
            for (qItr in 1..q) {
                val a = scan.nextLine()
                val b = scan.nextLine()
                val result = abbreviation(a, b)
                println(result)
            }
        }
    }

}

// Complete the abbreviation function below.
fun abbreviation(a: String, b: String): String {
    val dp = Array(a.length + 1) { Array(b.length + 1) { 0 } }
    dp[0][0] = 1
    fillFirstColumn(dp, a)
    for (i in 1 until dp.size) {
        for (j in 1 until dp[0].size) {
            if (a[i - 1] == b[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1]
            } else if (a[i - 1].toUpperCase() == b[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1] or dp[i - 1][j]

            } else if (a[i - 1].isLowerCase()) {
                dp[i][j] = dp[i - 1][j]
            } else if (a[i - 1].isUpperCase()) {
                dp[i][j] = 0
            }
        }
    }
    dp.forEachIndexed { index, v ->
        if (index == 0) {
            debugLog("\t\t\t${b.toCharArray().joinToString("\t,")}")
            debugLog("\t :\t${v.joinToString("\t,")}")
        } else {
            debugLog("\t${a[index - 1]}:\t${v.joinToString("\t,")}")
        }

    }
    return if (dp[a.length][b.length] == 1) "YES" else "NO"
}

fun fillFirstColumn(dp: Array<Array<Int>>, a: String) {
    var foundUpperCase = false
    for (i in 1 until dp.size) {
        if (a[i - 1].isLowerCase() && !foundUpperCase) {
            dp[i][0] = 1
        }
        if (a[i - 1].isUpperCase()) {
            foundUpperCase = true
        }
    }
}

