package main.kotlin.hackerrank
import java.util.*
import kotlin.system.measureTimeMillis

/******* utility functions *************/


data class Ref<T>(var value: T)

val isDevelopment = Ref(false)

inline fun <T : Any> T?.whenNotNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

inline fun <T : Any> T?.whenNull(f: () -> Unit) {
    if (this == null) f()
}

enum class Level {
    DEBUG,
    ACTUAL
}

fun log(message: Any?, level: Level) {
    when (level) {
        Level.DEBUG -> if (isDevelopment.value) println("DEBUG: $message")
        Level.ACTUAL -> println(message)
    }
}

fun log(message: Any?) {
    println(message)
}

fun debugLog(message: Any?) {
    log(message, Level.DEBUG)
}

inline fun <T> withTimeToExecution(block: () -> T): T {
    return withTimeToExecution("Default", block)
}

inline fun <T> withTimeToExecution(operationName: String = "Default", block: () -> T): T {
    var value: T? = null
    val time = measureTimeMillis {
        value = block()
    }
    debugLog("Elapsed time: $time ms for Operation: $operationName")
    return value!!
}


fun setDevelopmentFlag(args: Array<String>) {
    isDevelopment.value = args.contains("test")
}

/******* utility functions *************/


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = Scanner(System.`in`)
    val t = scan.nextLine().trim().toInt()
    withTimeToExecution("Overall") {
        for (tItr in 1..t) {
            val n = scan.nextLine().trim().toInt()
            val arr = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()
            val result = withTimeToExecution("Loop[${tItr}].equal()") { equal(arr) }
            println(result)
        }
    }
}

fun equal(arr: Array<Int>): Int {
    val sortedList = arr.toList().sorted()
    debugLog(sortedList)
    val smallest = sortedList.first()
    val baselines = baselines(smallest)
    var minOperations = Int.MAX_VALUE
    baselines.forEach {
        val mutableList = sortedList.toMutableList()
        var operations = 0
        while (mutableList.size > 1) {
            val largest = mutableList.last()
            val index = mutableList.size - 1
            operations += countOperations(largest, it)
            debugLog("$largest reduced to $it after $operations operations")
            mutableList.removeAt(index)
        }
        minOperations = minOf(minOperations, operations)

    }
    return minOperations

}

fun countOperations(largest: Int, smallest: Int): Int {
    var delta = delta(largest, smallest)
    var itemToReduce = largest
    var operations = 0
    while (delta > 0) {
        itemToReduce -= delta
        operations++
        delta = delta(itemToReduce, smallest)
    }
    return operations
}

fun delta(from: Int, to: Int): Int {
    val x = from - to
    return when {
        x >= 5 -> 5
        x >= 2 -> 2
        x >= 1 -> 1
        else -> x
    }
}

fun baselines(from: Int): List<Int> {
    val result = mutableListOf(from)
    if (from >= 1) {
        result.add(from - 1)
    }
    if (from >= 2) {
        result.add(from - 2)
    }
    return result
}


/**
1
110
53 361 188 665 786 898 447 562 272 123 229 629 670 848 994 54 822 46 208 17 449 302 466 832 931 778 156 39 31 777 749 436 138 289 453 276 539 901 839 811 24 420 440 46 269 786 101 443 832 661 460 281 964 278 465 247 408 622 638 440 751 739 876 889 380 330 517 919 583 356 83 959 129 875 5 750 662 106 193 494 120 653 128 84 283 593 683 44 567 321 484 318 412 712 559 792 394 77 711 977 785 146 936 914 22 942 664 36 400 857
82

10605

! 10605
 */






