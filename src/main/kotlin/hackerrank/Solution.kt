package main.kotlin.hackerrank

import main.kotlin.graphs.DisjointSet
import java.util.*
import kotlin.system.measureTimeMillis

/******* utility functions *************/


data class Ref<T>(var value: T?)

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
        Level.DEBUG -> if (isDevelopment) println("DEBUG: $message")
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

var isDevelopment = false

fun setDevelopmentFlag(args: Array<String>) {
    isDevelopment = args.contains("test")
}

/******* utility functions *************/


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = Scanner(System.`in`)
    val (n, p) = scan.nextLine().split(" ").map { it.trim().toInt() }
    val astronaut = Array(p) { Array<Int>(2) { 0 } }
    for (i in 0 until p) {
        astronaut[i] = scan.nextLine()
            .split(" ")
            .map { it.trim().toInt() }
            .toTypedArray()
    }
    withTimeToExecution("journeyToMoon") {
        val result = journeyToMoon(n, astronaut)
        log(result)
    }
}

fun journeyToMoon(n: Int, astronauts: Array<Array<Int>>): Long {
    val ds = DisjointSet<Int>(n)
    astronauts.forEach { (a, b) ->
        ds.union(a, b)
    }
    for (i in 0 until n) {
        ds.add(i)
    }

    val connectedComponents = withTimeToExecution("getConnectedComponents") { ds.getConnectedComponents() }
    val sizeOfConnectedComponents = connectedComponents.map { it.size }
//    val s1 =  withTimeToExecution("findSumBruteForce") { findSum(sizeOfConnectedComponents)}
    val s2 = withTimeToExecution("findSumOptimal") { findSumOptimal(sizeOfConnectedComponents) }
//    debugLog(s1)
    debugLog(s2)
    return s2
}

private fun findSum(sizeOfConnectedComponents: List<Int>): Long {
    var sum = 0L
    for (i in 0 until sizeOfConnectedComponents.size) {
        for (j in i + 1 until sizeOfConnectedComponents.size) {
            sum += sizeOfConnectedComponents[i] * sizeOfConnectedComponents[j]
        }
    }
    return sum
}

private fun findSumOptimal(sizeOfConnectedComponents: List<Int>): Long {
    val cumulativeSumArray =
        sizeOfConnectedComponents.foldIndexed(MutableList(sizeOfConnectedComponents.size) { 0 }) { index, acc, i ->
            if (index == 0) {
                acc[index] = i
            } else {
                acc[index] = i + acc[index - 1]
            }
            acc
        }
    var sum = 0L
    val size = sizeOfConnectedComponents.size
    for (i in 0 until size) {
        sum += sizeOfConnectedComponents[i] * (cumulativeSumArray[size - 1] - cumulativeSumArray[i])
    }
    return sum
}





