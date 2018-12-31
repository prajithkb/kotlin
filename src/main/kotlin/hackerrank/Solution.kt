package main.kotlin.hackerrank

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.math.BigInteger
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

val writer = BufferedWriter(OutputStreamWriter(System.out))
fun println(message: Any) {
    writer.write(message.toString())
    writer.newLine()
}

inline fun sandbox(within: Long = 5000, crossinline block: () -> Any) {
    writer.use {
        completeWithin(within) {
            withTimeToExecution("main") {
                block()
            }
        }
    }
}

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
//        tree[to].children.add(tree[from].id)
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


/******* utility functions *************/

val mod = 1000000007L

val MOD = BigInteger(mod.toString())

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    val a = scan.nextLine()
    val b = scan.nextLine()
    sandbox(5000) {
        //        withTimeToExecution("xorAndSum") {
//            println(xorAndSum(a, b))
//        }
        withTimeToExecution("xorAndSumOptimal") {
            println(xorAndSumOptimal(a, b))
        }
    }
}

const val limit = 314159

fun xorAndSum(a: String, b: String): Long {
    val A = BigInteger(a, 2)
    val B = BigInteger(b, 2)
    var sum = BigInteger("0", 2)
    for (i in 0..limit) {
        val xorI = A.xor(B.shiftLeft(i))
//        debugLog("A:${A.toString(2)}|Bshli:${B.shiftLeft(i).toString(2)}|xor:${xorI.toString(2)}, $xorI")
        sum = sum.add(xorI)
//        sum = sum.mod(MOD)
    }
    return sum.mod(MOD).toLong()
}

fun xorAndSumOptimal(a: String, b: String): Long {
    val reversedA = a.reversed()
    val reversedB = b.reversed()
    val totalLength = if (reversedA.length > (reversedB.length + limit)) reversedA.length else reversedB.length + limit
    var sum = 0L
    val indexes = Array(totalLength) { 0L }
    val countOfOnes = Array(totalLength) { 0L }
    populateCountOfOnes(countOfOnes, reversedB)
    val binaryChars = Array(totalLength) { '0' }
    reversedA.forEachIndexed { index, c ->
        binaryChars[index] = c
    }
//    debugLog(binaryChars.joinToString(""))
    for (index in 0 until totalLength) {
        val c = binaryChars[index]
        val i = if (c == '1') {
            limit - countOfOnesWithIn(limit, countOfOnes, index) + 1
        } else {
            countOfOnesWithIn(limit, countOfOnes, index)
        }
        indexes[index] = i
        sum += 2L.modPow(index, mod) * i
        sum %= mod
    }
//    debugLog(indexes.withIndex().joinToString { "${it.value}*2^${it.index}" })
    return sum
}

fun countOfOnesWithIn(limit: Int, countOfOnes: Array<Long>, index: Int): Long {
    return if (limit >= index) {
        countOfOnes[index]
    } else {
        countOfOnes[index] - countOfOnes[index - limit - 1]
    }
}

private fun populateCountOfOnes(countOfOnes: Array<Long>, reversedB: String) {
    if (reversedB.first() == '1') {
        countOfOnes[0] = 1
    }
    countOfOnes.withIndex().filter { it.index > 0 }.forEach { (index, _) ->
        if (index < reversedB.length && reversedB[index] == '1') {
            countOfOnes[index] = countOfOnes[index - 1] + 1
        } else {
            countOfOnes[index] = countOfOnes[index - 1]
        }
    }
}
