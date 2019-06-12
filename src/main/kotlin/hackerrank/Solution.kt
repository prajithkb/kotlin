package main.kotlin.hackerrank

import main.kotlin.SegmentTree
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals


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

class Duration(val name: String, var duration: Long = 0) {
    override fun toString(): String {
        return "Duration(name=$name, duration=$duration ms)"
    }

    inline fun <T> timed(block: () -> T): T {
        var value: T? = null
        val time = measureTimeMillis {
            value = block()
        }
        this.duration += time
        return value!!
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
    val outputScanner = Scan(File("/Users/kprajith/Desktop/REMOVE_THIS_output.txt").bufferedReader())
    val LIMIT = 100005
    sandbox(500000000) {
        val segmentTree = withTimeToExecution("Init") { SegmentTree(IntRange(0, LIMIT)) }
        val t = scan.nextInt()
        for (i in 1..t) {
            val (d, m) = scan.nextInts()
            segmentTree.update(d..LIMIT, m)
            val max = segmentTree.query(0..LIMIT)
            log(max)
            assertEquals(outputScanner.nextInt(), max, "For $i")
        }

    }
}





