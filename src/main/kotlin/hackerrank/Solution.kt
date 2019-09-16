package main.kotlin.hackerrank

import main.kotlin.graphs.GraphWiz
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class DisjointSet<Item>(size: Int) {

    data class Element<Item>(
        val value: Item?,
        var index: Int
    )

    private val elements: Array<Element<Item>?> = arrayOfNulls(size)

    private val parentIndexSizePairs: Array<Pair<Int, Int>?> = arrayOfNulls(size)


    fun add(singleNode: Int) {
        add(Element<Item>(null, singleNode))
    }

    fun add(singleNode: Element<Item>) {
        findParentIndexOrCreate(singleNode)
    }

    fun union(
        from: Int,
        to: Int
    ): Boolean {
        return union(Element<Item>(null, from), Element<Item>(null, to))
    }

    fun union(
        from: Element<Item>,
        to: Element<Item>
    ): Boolean {
        val (toParentIndex, toParentSize) = findParentIndexPairOrCreate(to)
        val (fromParentIndex, fromParentSize) = findParentIndexPairOrCreate(from)
        if (toParentIndex != fromParentIndex) {
            val newParentIndexSizePair = Pair(toParentIndex, toParentSize + fromParentSize)
            parentIndexSizePairs[fromParentIndex] = newParentIndexSizePair
            parentIndexSizePairs[toParentIndex] = newParentIndexSizePair
            return true
        } else {
            return false
        }
    }

    private fun findParentIndexPairOrCreate(item: Element<Item>): Pair<Int, Int> {
        return findParentIndexOrCreate(item).let { parentIndexSizePairs[it] ?: Pair(it, 1) }
    }

    fun sizeOfConnectedComponents(from: Int): Int {
        return find(from)?.let { parentIndexSizePairs[it]?.second } ?: 0

    }

    private fun sizeOfConnectedComponents(from: Element<Item>): Int {
        return sizeOfConnectedComponents(from.index)
    }

    private fun findParentIndexOrCreate(item: Element<Item>): Int {
        val itemRep = find(item.index)
        if (itemRep == null) {
            parentIndexSizePairs[item.index] = Pair(item.index, 1)
            elements[item.index] = item
        }
        return itemRep ?: item.index
    }


    fun find(index: Int?): Int? {
        if (index == null || parentIndexSizePairs.size <= index || parentIndexSizePairs[index] == null) {
            return null
        }
        if (parentIndexSizePairs[index]?.first === index) {
            return index
        } else {
            val result = find(parentIndexSizePairs[index]?.first)
            return result?.also {
                parentIndexSizePairs[index] = parentIndexSizePairs[it]
            }
        }
    }

    fun getConnectedComponents(): List<List<Int>> {
        val connectedComponents = elements
            .filterNotNull()
            .map { element -> element.index }
            .groupBy { i -> find(i) }
        return connectedComponents.values.toList()
    }

    override fun toString(): String {
        return getConnectedComponents().toString()
    }

    fun toDebugString(): String {
        return "parentIndexSizePairs: ${parentIndexSizePairs
            .withIndex()
            .joinToString(",", "[", "]") { (index, pair) ->
                "{index|$index:pair|$pair}"
            }}\n" +
                "elements: ${elements.filterNotNull().joinToString(",")}"
    }


}

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
val OUTPUT_FILE = "/Users/kprajith/Desktop/REMOVE_THIS_output.txt"
val INPUT_FILE = "/Users/kprajith/Desktop/REMOVE_THIS.txt"

fun scanner(): Scan {
    if (devOverrides.readFromFile) {
        return Scan(File(INPUT_FILE).bufferedReader())
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

    fun asATree(root: Int, visitor: (child: Int, parent: Int) -> Unit = { _, _ -> }): Graph {
        val tree = Graph(numberOfNodes)
        tree.setRoot(root)
        intoTree(this[root], Array(this.numberOfNodes + 1) { false }, tree, visitor)
        tree.nodes.forEach {
            it.isLeaf = it.children.isEmpty()
        }
        return tree
    }

    private fun intoTree(
        node: Node,
        visited: Array<Boolean>,
        tree: Graph,
        visitor: (child: Int, parent: Int) -> Unit
    ) {
        visited[node.id] = true
        node.children
            .filter { !visited[it] }
            .map { nodes[it] }
            .forEach {
                tree[node.id].children.add(it.id)
                tree[it.id].parent = node.id
                visitor(it.id, node.id)
                intoTree(it, visited, tree, visitor)
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


class Validator {

    fun validate(output: String) {
        if (devOverrides.isDebug) {
            val outputScanner = Scan(File(OUTPUT_FILE).bufferedReader())
            assertEquals(outputScanner.nextLine(), output)
            debugLog("SUCCESS")
        }

    }
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

data class Road(val from: Int, val to: Int, val count: Long, val countAsPower: Int)

const val MAX = 100001

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val inputScanner = scanner()
    val validator = Validator()
    sandbox(5000000) {
        val (n, m) = inputScanner.nextInts()
        val roads = mutableListOf<Road>()
        (1..m).forEach {
            val (from, to, count) = inputScanner.nextInts()
            roads.add(Road(from, to, 2.toLong().pow(count), count))
        }
        val result = roadsInHackerland(n, roads)
        println(result)
        validator.validate(result)
    }
}

fun roadsInHackerland(n: Int, roads: MutableList<Road>): String {
    val (graph, edges) = createEdgesAndConnectedComponents(n, roads)
    val tree = intoMinimalSpanningTree(graph, edges)
    val sum = sumOfSums(tree, edges)
    debugLog(sum)
    return sum
}

private fun intoMinimalSpanningTree(
    graph: Graph,
    edges: MutableMap<Pair<Int, Int>, Pair<Long, Int>>
): Graph {
    val graphWiz = GraphWiz(1)
    val tree = graph.asATree(1) { child, parent ->
        val edge = edges.getOrDefault(child to parent, Pair(0L, 0))
        graphWiz.add(child, parent, edge.second)
    }
    graphWiz.toDotFile()
    return tree
}

private fun createEdgesAndConnectedComponents(
    n: Int,
    roads: MutableList<Road>
): Pair<Graph, MutableMap<Pair<Int, Int>, Pair<Long, Int>>> {
    val ds = DisjointSet<Int>(MAX)
    val graph = Graph(n)
    val edges = mutableMapOf<Pair<Int, Int>, Pair<Long, Int>>()
    roads.sortedBy { it.count }
        .forEach { (from, to, count, countAsPower) ->
            if (ds.sizeOfConnectedComponents(from) < n) {
                val added = ds.union(from, to)
                if (added) {
                    graph.connect(from, to)
                    edges[from to to] = Pair(count, countAsPower)
                    edges[to to from] = Pair(count, countAsPower)
                }
            }
        }
    return Pair(graph, edges)
}

val allEdges = mutableListOf<Result>()

fun sumOfSums(
    tree: Graph,
    edges: MutableMap<Pair<Int, Int>, Pair<Long, Int>>
): String {
    val visited = visitedArray(tree)
    sumOfSums(tree.root(), tree, visited, edges)
    var sum = BigInteger.ZERO
    val list = mutableListOf<Int>()
    allEdges
        .onEach { list.add(it.power) }
        .filter { it.parents > 0 }
        .map {
            Triple(
                BigInteger.valueOf(2).pow(it.power),
                BigInteger.valueOf(it.children.toLong()),
                BigInteger.valueOf(it.parents.toLong())
            )
        }
        .map { it.first.multiply(it.second).multiply(it.third) }
        .onEach { sum = sum.add(it) }
    return sum.toString(2)
}

data class Result(val value: Long = 0, val power: Int = 0, val children: Int = 0, val parents: Int = 0)

fun sumOfSums(
    root: Node,
    tree: Graph,
    visited: BooleanArray,
    edges: MutableMap<Pair<Int, Int>, Pair<Long, Int>>
): Result {
    val currentNodeId = root.id
    if (visited[currentNodeId]) {
        return Result()
    }
    visited[currentNodeId] = true
    val results = root.children
        .filterNot { visited[it] }
        .map { tree[it] }
        .map { sumOfSums(it, tree, visited, edges) }
    val numberOfChildren = results.sumBy { it.children } + 1
    val (value, power) = edges.getOrDefault(currentNodeId to root.parent, Pair(-1L, -1))
    val r = Result(value, power, numberOfChildren, tree.size() - 1 - numberOfChildren)
    allEdges.add(r)
    return r
}






