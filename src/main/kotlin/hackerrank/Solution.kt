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
//        return Scanner(File("/Users/kprajith/Desktop/REMOVE_THIS.txt"))
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


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    completeWithin(5000) {
        withTimeToExecution {
            val q = scan.nextLine().trim().toInt()
            for (qItr in 1..q) {
                val nm = scan.nextLine().split(" ")
                val n = nm[0].trim().toLong()
                val m = nm[1].trim().toInt()
                val s = scan.nextLine().split(" ").map { it.trim().toLong() }.toTypedArray()
                val result = stoneDivision(n, s)
                println(result)
            }
        }

    }
}

fun stoneDivision(n: Long, options: Array<Long>): Long {
    val piles = mutableMapOf<Long, Long>()
    return divide(piles, options, n)
}

fun divide(
    piles: MutableMap<Long, Long>,
    options: Array<Long>,
    n: Long
): Long {
    var bestOption = 0L
    options
        .filter { n % it == 0L && n != it }
        .forEach { pick ->
            val multiplier = n / pick
            piles[pick].whenNull { piles[pick] = divide(piles, options, pick) }
            piles[pick].whenNotNull { bestOption = maxOf(1 + it * multiplier, bestOption) }
        }
    piles[n] = bestOption
    return bestOption
}
