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

inline fun sandbox(crossinline block: () -> Any) {
    writer.use {
        completeWithin(5000) {
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

fun bfs(root: Node, nodes: Array<Node>) {
    val visited = Array(nodes.size) { false }
    bfs(root, nodes, visited)
}

fun bfs(root: Node, nodes: Array<Node>, visited: Array<Boolean>): Int {
    var edges = 0
    visited[root.id] = true
    root.children.filter { !visited[it] }.map { nodes[it] }.forEach { node ->
        edges += bfs(node, nodes, visited) + 1
    }
    return edges
}


/******* utility functions *************/


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    sandbox {
        val nm = scan.nextLine().split(" ")
        val n = nm[0].trim().toInt()
        val m = nm[1].trim().toInt()
        val tree = createTree(n + 1, m, scan)
        tree.filter { !it.visited }.forEach { bfsAndCalculateNimber(it, tree) }
//        debugLog { tree.joinToString("\n") }
        val q = scan.nextLine().trim().toInt()
        for (qItr in 1..q) {
            val queryCount = scan.nextLine().trim().toInt()
            val query = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()
            val result = bendersPlay(n, tree, query)
            println(result)
        }
    }
}

fun grundyNumber(list: List<Int>): Int {
    var possibleGrundyNumber = 0
    val sortedSet = list.toSortedSet()
    while (sortedSet.contains(possibleGrundyNumber)) {
        possibleGrundyNumber++
    }
//    debugLog("set: $sortedSet, grundyNumber: $possibleGrundyNumber")
    return possibleGrundyNumber
}


fun bfsAndCalculateNimber(root: Node, nodes: Array<Node>): Int {
    val nimbers = mutableListOf<Int>()
    if (!root.visited) {
        root.children.map { nodes[it] }.forEach { node ->
            if (node.visited) {
                nimbers.add(node.nimber)
            } else {
                nimbers.add(bfsAndCalculateNimber(node, nodes))
            }
        }
        root.childNimbers.addAll(nimbers)
        root.nimber = grundyNumber(nimbers)
        root.visited = true
    }
    return root.nimber
}

fun bendersPlay(n: Int, tree: Array<Node>, query: Array<Int>): String {
    val nimSum = query.map { tree[it].nimber }.fold(0) { i, acc -> acc xor i }
    return if (nimSum == 0) "Iroh" else "Bumi"
}