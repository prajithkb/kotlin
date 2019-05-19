package main.kotlin.hackerrank

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
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

/** Logging ***/

enum class Level {
    ERROR,
    DEBUG,
    ACTUAL
}

fun log(message: Any?, level: Level) {
    when (level) {
        Level.DEBUG, Level.ERROR -> if (devOverrides.isDebug) fastPrintln("$level: $message")
        Level.ACTUAL -> fastPrintln(message)
    }
}

fun log(message: Any?) {
    fastPrintln(message)
}

fun debugLog(message: Any?) {
    debugLog(Level.DEBUG) { message?.toString() }
}

fun debugLog(level: Level = Level.DEBUG, message: Any) {
    debugLog(level) { message.toString() }
}

inline fun debugLog(level: Level = Level.DEBUG, block: () -> Any?) {
    if (devOverrides.isDebug) {
        log(block(), level)
    }

}

/** Timed execution ****/

data class Duration(val name: String, var duration: Long = 0) {
    override fun toString(): String {
        return "Duration(name=$name, duration=$duration ms)"
    }
}

inline fun <T> Duration.timed(block: () -> T): T {
    var value: T? = null
    val time = measureTimeMillis {
        value = block()
    }
    this.duration += time
    return value!!
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
        debugLog(Level.ERROR, "Failed end complete within $timeoutInMilliSecs ms")
        return null
    } catch (e: ExecutionException) {
        if (e.cause is AssertionError) {
            debugLog(Level.ERROR, "Execution exception ${e.cause} ms")
        } else {
            throw e
        }
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

class Scan(private val reader: BufferedReader) {
    fun nextLine(): String {
        return reader.readLine()
    }

    fun nextInt(): Int {
        return this.nextInts().first()
    }

    fun nextInts(): List<Int> {
        return this.nextLine().split(" ").map { it.trim().toInt() }
    }

    fun nextLong(): Long {
        return this.nextLongs().first()
    }

    fun nextLongs(): List<Long> {
        return this.nextLine().split(" ").map { it.trim().toLong() }
    }
}

val writer = BufferedWriter(OutputStreamWriter(System.out))
fun fastPrintln(message: Any?) {
    writer.write(message?.toString())
    writer.newLine()
}

val isDebug =
    java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
val defaultTimeOut = if (isDebug) 50000000L else 5000L
inline fun sandbox(within: Long = defaultTimeOut, crossinline block: () -> Any) {
    debugLog("Running with timeout of $within")
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
    var parent: Int = -1,
    var isLeaf: Boolean = false
)

class Graph(
    val numberOfNodes: Int,
    private val nodes: Array<Node> = Array(numberOfNodes + 1) { i -> Node(i) },
    private var root: Int = 1
) {

    fun size(): Int {
        return numberOfNodes + 1
    }

    fun root(): Node {
        return nodes[root]
    }

    fun setRoot(root: Int) {
        this.root = root
    }

    operator fun get(id: Int): Node {
        return nodes[id]
    }

    fun connect(from: Int, to: Int) {
        nodes[from].children.add(nodes[to].id)
        nodes[to].children.add(nodes[from].id)
    }

    override fun toString(): String {
        return nodes.joinToString("\n")
    }

    fun asATree(root: Int): Graph {
        val tree = Graph(numberOfNodes)
        tree.setRoot(root)
        intoTree(this[root], Array(this.numberOfNodes + 1) { false }, tree)
        tree.nodes.forEach {
            it.isLeaf = it.children.isEmpty()
        }
        return tree
    }

    private fun intoTree(
        node: Node,
        visited: Array<Boolean>,
        tree: Graph
    ) {
        visited[node.id] = true
        node.children
            .filter { !visited[it] }
            .map { nodes[it] }
            .forEach {
                tree[node.id].children.add(it.id)
                tree[it.id].parent = node.id
                intoTree(it, visited, tree)
            }
    }

}


fun Scan.toGraph(numberOfNodes: Int, numberOfEdges: Int = numberOfNodes - 1): Graph {
    val graph = Graph(numberOfNodes)
    for (treeRowItr in 1..numberOfEdges) {
        val (from, to) = this.nextLine()
            .split(" ")
            .map { it.trim().toInt() }
            .toTypedArray()
        graph.connect(from, to)
    }
    return graph
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
    // than or equal end p
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


fun Int.pow(exponent: Int): Int {
    return Math.pow(this.toDouble(), exponent.toDouble()).toInt()
}

fun Long.pow(exponent: Int): Long {
    return Math.pow(this.toDouble(), exponent.toDouble()).toLong()
}

fun Long.modMultiply(value: Long, mod: Long): Long {
    return ((this % mod) * (value % mod)) % mod
}

fun Long.modAdd(value: Long, mod: Long): Long {
    return ((this % mod) + (value % mod)) % mod
}

fun Long.modSubtract(value: Long, mod: Long): Long {
    var r = ((this % mod) - (value % mod)) % mod
    while (r < 0) {
        r += mod
    }
    return r
}


fun <T> Array<T>.from(index: Int): List<IndexedValue<T>> {
    return this.withIndex().filter { it.index >= index }
}

fun <T> List<T>.from(index: Int): List<IndexedValue<T>> {
    return this.withIndex().filter { it.index >= index }
}


val MOD = 10L.pow(9).plus(7)

private fun visitedArray(tree: Graph) = BooleanArray(tree.size())

/******* utility functions ( above) *************/

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    sandbox(5000) {
        val t = scan.nextInt()
        for (tItr in 1..t) {
            val arrCount = scan.nextInt()
            val arr = scan.nextLongs()
            whatsNext(arr)
        }

    }
}


fun whatsNext(input: List<Long>) {
    val result = nextSmallestSameBitSetNumber(input)
    log(result.size)
    log(result.joinToString(" "))
}


fun nextSmallestSameBitSetNumber(input: List<Long>): List<Long> {
    val mutableList = input.toMutableList()
    when {
        // 4 1111 -> 10111
        input.size == 1 -> {
            val ones = input[0]
            mutableList[0] = 1
            mutableList.add(1, 1)
            mutableList.add(2, ones - 1)
        }
        // 4,3 1111000 -> 10000111
        input.size == 2 -> {
            val ones = input[0]
            val zeros = input[1]
            mutableList[0] = 1
            mutableList[1] = zeros + 1
            mutableList.add(2, ones - 1)
        }
        // 4,3,2,1 1111000110 -> 1111001001
        input.size % 2 == 0 -> {
            val zeros = input[input.size - 1] // 1
            val ones = input[input.size - 2] // 2
            val trailingZeros = input[input.size - 3] // 3
            // Next two lines, flipping zero to 1
            if (trailingZeros - 1 > 0) {
                mutableList[input.size - 3] = trailingZeros - 1
                mutableList[input.size - 2] = 1
                // remaining zeros
                mutableList[input.size - 1] = zeros + 1
                // remaining ones
                mutableList.add(ones - 1)
            } else {
                mutableList[input.size - 4]++
                mutableList[input.size - 3] = 0
                mutableList[input.size - 2] = zeros + 1
                // remaining zeros
                mutableList[input.size - 1] = ones - 1
            }

        }
        // 4,3,2 111100011 -> 111100101
        else -> {
            val ones = input[input.size - 1]
            val trailingZeros = input[input.size - 2]
            if (trailingZeros - 1 > 0) {
                mutableList[input.size - 2] = trailingZeros - 1
                mutableList[input.size - 1] = 1
                mutableList.add(1)
                mutableList.add(ones - 1)
            } else {
                mutableList[input.size - 3]++
                mutableList[input.size - 2] = 1
                mutableList[input.size - 1] = ones - 1
            }
        }

    }
    return mutableList.filter { it > 0 }
}




