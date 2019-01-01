package main.kotlin.hackerrank

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis

/******* utility functions *************/

data class DevOverrides(var isDebug: Boolean, var readFromFile: Boolean)

val devOverrides = DevOverrides(false, false)

/** Non null ***/

inline fun <T : Any> T?.whenNotNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

inline fun <T : Any> T?.whenNull(f: () -> Unit) {
    if (this == null) f()
}

/** Logging ***/

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

/** Timed execution ****/

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


/*** Read and Write ****/

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

val writer = BufferedWriter(OutputStreamWriter(System.out))
fun println(message: Any) {
    writer.write(message.toString())
    writer.newLine()
}

val isDebug =
    java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
val defaultTimeOut = if (isDebug) 50000000L else 5000L
inline fun sandbox(within: Long = defaultTimeOut, crossinline block: () -> Any) {
    val within =
        writer.use {
            completeWithin(within) {
                withTimeToExecution("main") {
                    block()
                }
            }
        }
}

/** Graphs ***/


data class Node(
    var id: Int,
    val children: MutableList<Int> = mutableListOf(),
    var nimber: Int = 0,
    val childNimbers: MutableList<Int> = mutableListOf(),
    var visited: Boolean = false
)

fun createTree(numberOfNodes: Int, numberOfEdges: Int, scan: Scan): Array<Node> {
    val tree = Array(numberOfNodes + 1) { i -> Node(i) }
    for (treeRowItr in 1..numberOfEdges) {
        val (from, to) = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()
        tree[from].children.add(tree[to].id)
    }
    return tree
}

fun Array<Node>.bfs(root: Node) {
    val visited = Array(this.size) { false }
    bfs(root, visited)
}

fun Array<Node>.bfs(root: Node, visited: Array<Boolean>): Int {
    var edges = 0
    visited[root.id] = true
    root.children.filter { !visited[it] }.map { this[it] }.forEach { node ->
        edges += bfs(node, visited) + 1
    }
    return edges
}

/** Game theory ***/

fun List<Int>.grundyNumber(): Int {
    var possibleGrundyNumber = 0
    val sortedSet = this.toSortedSet()
    while (sortedSet.contains(possibleGrundyNumber)) {
        possibleGrundyNumber++
    }
    return possibleGrundyNumber
}

fun List<Int>.nimSum(): Int {
    return this.fold(0) { i, acc -> acc xor i }
}

fun Long.modPow(power: Int, mod: Long): Long {
    // Initialize result
    var res = 1L
    var y = power.toLong()
    // Update x if it is more
    // than or equal to p
    var x = this % mod
    while (y > 0) {
        // If y is odd, multiply x
        // with result
        if ((y and 1) == 1L)
            res = (res * x) % mod

        // y must be even now
        // y = y / 2
        y = y shr 1
        x = (x * x) % mod
    }
    return res

}

fun <T> Array<T>.from(index: Int): List<IndexedValue<T>> {
    return this.withIndex().filter { it.index >= index }
}

fun <T> List<T>.from(index: Int): List<IndexedValue<T>> {
    return this.withIndex().filter { it.index >= index }
}


/******* utility functions ( above) *************/


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    sandbox {
        val q = scan.nextLine().trim().toInt()
        for (qItr in 1..q) {
            val nm = scan.nextLine().split(" ")
            val n = nm[0].trim().toInt()
            val m = nm[1].trim().toLong()
            val a = scan.nextLine().split(" ").map { it.trim().toLong() }.toTypedArray()
            withTimeToExecution("suboptimalSum") {
                //                log(suboptimalSum(a, m))
            }

            withTimeToExecution("optimalSum") {
                log(optimalSum(a, m))
            }

        }

    }
}

fun suboptimalSum(a: Array<Long>, m: Long): Long {
    val cumulativeSum = Array(a.size + 1) { 0L }
    cumulativeSum[1] = a[0]
    a.from(1).forEach { (index, value) ->
        cumulativeSum[index + 1] = cumulativeSum[index] + value
    }
//    debugLog(cumulativeSum.joinToString())
    var leftAndRightIndex = 0 to 0
    var maximumSum = 0L
    for (i in 0 until cumulativeSum.size) {
        for (j in i + 1 until cumulativeSum.size) {
            val mod = (cumulativeSum[j] - cumulativeSum[i]) % m
            if (mod > maximumSum) {
                maximumSum = mod
//                debugLog("prev l-r: $leftAndRightIndex")
                leftAndRightIndex = i - 1 to j - 1

            }
        }
    }
//    debugLog("left - right : $leftAndRightIndex")
    //1398956404
    return maximumSum
}

fun optimalSum(a: Array<Long>, m: Long): Long {
    val cumulativeSum = cumulativeSum(a)
    val indexedCumulativeSum = cumulativeSum.map { it % m }.withIndex().sortedBy { it.value }
    val closestPair = closestPair(indexedCumulativeSum)
    val farthestPair = farthestPair(indexedCumulativeSum)
    val c = cumulativeSum[closestPair.second] - cumulativeSum[closestPair.first]
    val f = cumulativeSum[farthestPair.second] - cumulativeSum[farthestPair.first]

//    debugLog("negativeIndexPair : $closestPair | ${(cumulativeSum[closestPair.second]- cumulativeSum[closestPair.first]) % m}")
//    debugLog("positiveIndexPair : $farthestPair | ${(cumulativeSum[farthestPair.second]- cumulativeSum[farthestPair.first]) % m}")
    return if (c % m > f % m) c % m else f % m
    //1398956404
}

fun closestPair(indexedCumulativeSum: List<IndexedValue<Long>>): Pair<Int, Int> {
    val minDifferences = indexedCumulativeSum.from(1)
        .map { (index, indexedValue) -> Pair(indexedCumulativeSum[index - 1], indexedValue) }
        .filter { (prev, current) -> prev.index > current.index }
        .map { (prev, current) ->
            Pair(Pair(current.index, prev.index), prev.value - current.value)
        }
        .sortedBy { it.second }
    return minDifferences.last().first
}


fun farthestPair(indexedCumulativeSum: List<IndexedValue<Long>>): Pair<Int, Int> {
    var l = 0
    var r = indexedCumulativeSum.size - 1
    while (indexedCumulativeSum[l].index > indexedCumulativeSum[r].index) {
        r--
        if (r == 0) {
            l++
            r = indexedCumulativeSum.size - 1
        }
    }
    return indexedCumulativeSum[l].index to indexedCumulativeSum[r].index
}

private fun cumulativeSum(a: Array<Long>): Array<Long> {
    val cumulativeSum = Array(a.size + 1) { 0L }
    cumulativeSum[1] = a[0]
    a.from(1).forEach { (index, value) ->
        cumulativeSum[index + 1] = cumulativeSum[index] + value
    }
    return cumulativeSum
}

fun maximumSum(a: Array<Long>, m: Long): Long {
    val arrayWithMod = a.map { it % m }
    var leftIndex = 0
    var rightIndex = 0
    var maximumSum = 0L
    var sumTillNow = 0L
    var leftAndRightIndex = 0 to 0
    while (leftIndex < arrayWithMod.size) {
        if (sumTillNow < m && rightIndex < arrayWithMod.size) {
            sumTillNow += arrayWithMod[rightIndex]
            rightIndex++
        } else {
            while (sumTillNow >= m && leftIndex <= rightIndex) {
                sumTillNow -= arrayWithMod[leftIndex]
                leftIndex++
            }
            if (rightIndex == arrayWithMod.size) {
                leftIndex++
            }
        }
        if (sumTillNow in (maximumSum + 1)..(m - 1)) {
            maximumSum = sumTillNow
            debugLog("prev l-r: $leftAndRightIndex")
            leftAndRightIndex = leftIndex to rightIndex

        }
    }
    debugLog("left - right : $leftAndRightIndex")
    return maximumSum
}



