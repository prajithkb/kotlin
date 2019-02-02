package main.kotlin.hackerrank

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.*
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

class Scan(private val reader: BufferedReader) {
    fun nextLine(): String {
        return reader.readLine()
    }
}

val writer = BufferedWriter(OutputStreamWriter(System.out))
fun println(message: Any) {
    kotlin.io.println(message)
//    writer.write(message.toString())
//    writer.newLine()
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
    var parent: Int = -1,
    var isLeaf: Boolean = false
)

class Graph(
    val numberOfNodes: Int,
    private val nodes: Array<Node> = Array(numberOfNodes + 1) { i -> Node(i) },
    private var root: Int = 1
) {

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

    fun bfs(root: Node): Int {
        return bfs(root, Array(numberOfNodes + 1) { false })
    }

    fun bfs(root: Node, visited: Array<Boolean>): Int {
        var edges = 0
        visited[root.id] = true
        root.children
            .filter { !visited[it] }
            .map { this.nodes[it] }.forEach { node ->
                edges += bfs(node, visited) + 1
            }
        return edges
    }

    override fun toString(): String {
        return nodes.joinToString("\n")
    }

    fun asATree(): Graph {
        val tree = Graph(numberOfNodes)
        tree.setRoot(root().id)
        intoTree(root(), Array(this.numberOfNodes + 1) { false }, tree)
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


fun Scan.asGraph(numberOfNodes: Int, numberOfEdges: Int = numberOfNodes - 1): Graph {
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

fun List<Long>.cumulativeSum(): List<Long> {
    val cumulativeSum = MutableList(this.size + 1) { 0L }
    cumulativeSum.forEachIndexed { index, _ ->
        when (index) {
            0 -> cumulativeSum[0] = 0
            else -> cumulativeSum[index] = cumulativeSum[index - 1] + this[index - 1]
        }
    }
    return cumulativeSum
}

val MOD = 10L.pow(9).plus(7)

/******* utility functions ( above) *************/

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    sandbox(5000000) {
        val n = scan.nextLine().trim().toInt()
        val goodConfiguration = Array(n + 1) { 1L }
        val nearlyGoodConfiguration = Array(n + 1) { 1L }
        val roads = withTimeToExecution("asGraph") { scan.asGraph(n) }
        val result = withTimeToExecution("kingdomDivision") {
            kingdomDivision(
                roads,
                nearlyGoodConfiguration,
                goodConfiguration
            )
        }
        println(result)
    }
}


fun kingdomDivision(
    roads: Graph,
    nearlyGoodConfiguration: Array<Long>,
    goodConfiguration: Array<Long>
): Long {
    var r = roads
    withTimeToExecution("asTree") {
        r = roads.asATree()
    }

//    withTimeToExecution("visitAllPaths") {
//        r.visitAllPaths(r.root(), nearlyGoodConfiguration, goodConfiguration)
//    }
    withTimeToExecution("visitAllPathsRecursive") {
        r.visitAllPathsRecursive(nearlyGoodConfiguration, goodConfiguration)
    }

//    roads.visitAllPathsRecursive(nearlyGoodConfiguration, goodConfiguration)
//    debugLog(goodConfiguration.withIndex().joinToString("\n"))
//    debugLog(nearlyGoodConfiguration.withIndex().joinToString("\n"))
    return (2 * goodConfiguration[1]) % MOD
}

fun Graph.visitAllPathsRecursive(
    nearlyGoodConfiguration: Array<Long>,
    goodConfiguration: Array<Long>
) {
    val stack = ArrayDeque<Node>()
    val visited = Array(this.numberOfNodes + 1) { false }
    stack.add(this.root())
    while (stack.isNotEmpty()) {
        val currentNode = stack.peek()
//        debugLog(currentNode)
        if (!visited[currentNode.id]) {
            currentNode.children
                .map { this[it] }
                .forEach {
                    it.parent = currentNode.id
                    stack.push(it)
                }
            visited[currentNode.id] = true
        } else {
            stack.pop()
            if (currentNode.isLeaf) {
                nearlyGoodConfiguration[currentNode.id] = 1
                goodConfiguration[currentNode.id] = 0
            } else {
                currentNode.children
                    .forEach {
                        nearlyGoodConfiguration[currentNode.id] =
                                nearlyGoodConfiguration[currentNode.id].modMultiply(goodConfiguration[it], MOD)

                    }
                currentNode.children
                    .map {
                        goodConfiguration[it].modMultiply(2L, MOD).modAdd(nearlyGoodConfiguration[it], MOD)
                    }
                    .forEach {
                        goodConfiguration[currentNode.id] = goodConfiguration[currentNode.id].modMultiply(it, MOD)
                    }
                goodConfiguration[currentNode.id] =
                        goodConfiguration[currentNode.id].modSubtract(nearlyGoodConfiguration[currentNode.id], MOD)

            }
        }

    }
}


fun Graph.visitAllPaths(
    root: Node,
    nearlyGoodConfiguration: Array<Long>,
    goodConfiguration: Array<Long>
) {
    if (root.children.isEmpty()) {
        root.isLeaf = true
        nearlyGoodConfiguration[root.id] = 1
        goodConfiguration[root.id] = 0
        return
    }
    root.children
        .map { this[it] }
        .forEach {
            it.parent = root.id
            visitAllPaths(it, nearlyGoodConfiguration, goodConfiguration)
        }
    root.children
        .forEach {
            nearlyGoodConfiguration[root.id] = nearlyGoodConfiguration[root.id].modMultiply(goodConfiguration[it], MOD)
        }
    root.children
        .map {
            goodConfiguration[it].modMultiply(2L, MOD).modAdd(nearlyGoodConfiguration[it], MOD)
        }
        .forEach {
            goodConfiguration[root.id] = goodConfiguration[root.id].modMultiply(it, MOD)
        }
    goodConfiguration[root.id] = goodConfiguration[root.id].modSubtract(nearlyGoodConfiguration[root.id], MOD)
}




