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
val BITMASK = (1 shl 10) - 1

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    completeWithin(5000) {
        withTimeToExecution("main") {
            val n = scan.nextLine().trim().toInt()

            val tickets = Array(n) { "" }
            for (i in 0 until n) {
                val ticketsItem = scan.nextLine()
                tickets[i] = ticketsItem
            }
            val result = winningLotteryTicket(tickets)
            println(result)
        }
    }

}

fun winningLotteryTicket(tickets: Array<String>): Long {
    val sortedTickets = distinctTicketsCount(tickets)
//    debugLog { "sortedTickets\n" + sortedTickets.entries.joinToString("\n") { "${it.key}|${it.key.toString(2)}:${it.value}" } }
    return countPairs(sortedTickets, BITMASK)
}

private fun distinctTicketsCount(tickets: Array<String>): Map<Int, Int> {
    return tickets
        .map { it.trim() }
        .map { ticket ->
            var bits = 0
            val sortedChars = ticket.split("").filter { it != "" }.toSortedSet()
            sortedChars.map { it.toInt() }.map { int ->
                bits = bits or (1 shl int)
            }
            bits
        }.groupingBy { it }.eachCount()
}

fun countPairs(sortedTickets: Map<Int, Int>, bitmask: Int): Long {
    val setBitsMap = createSetBitsCountMap(sortedTickets)
    var sum = sortedTickets.entries.map { (k, v) ->
        val complement = bitmask xor k
        val c = setBitsMap.getOrDefault(complement, 0)
//        debugLog { "${k.toString(2)}|$k Complement $complement|${complement.toString(2)}, $c" }
        c * v
    }.sum()
    setBitsMap[1023].whenNotNull {
        sum -= it
    }
    return sum / 2
}

private fun createSetBitsCountMap(
    sortedTickets: Map<Int, Int>
): MutableMap<Int, Long> {
    val setBitsMap = mutableMapOf<Int, Long>()
    for (i in 0..1023) {
        sortedTickets.entries.forEach { (key, value) ->
            if (i.and(key) == i) {
                val count = setBitsMap.getOrDefault(i, 0L)
                setBitsMap[i] = count + value
            }
        }
    }
    return setBitsMap
}




