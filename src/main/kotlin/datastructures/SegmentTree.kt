package main.kotlin

import main.kotlin.SegmentTree.Companion.INVALID
import main.kotlin.graphs.GraphWiz
import main.kotlin.hackerrank.*

class SegmentTree(private val overallRange: IntRange) {

    private val root: Item = Item(overallRange)

    init {
        initialize(root)
    }

    private fun initialize(item: Item) {
        if (item.isNotEmpty()) {
            val range = item.range
            val difference = range.last - range.first
            item.left = Item(IntRange(range.first, range.first + difference / 2))
            item.right = Item(IntRange(range.first + difference / 2 + 1, range.last))
            initialize(item.left)
            initialize(item.right)
        }

    }

    fun query(range: IntRange = overallRange): Int {
        if (range.first < root.first || range.last > root.last) {
            return INVALID
        }
        return query(root, range)
    }

    private fun query(
        item: Item,
        range: IntRange
    ): Int {
        if (item.doesNotOverlapWith(range)) {
            return INVALID
        }

        if (range.contains(item.range)) {
            return item.value + item.lazy
        }
        item.propagateLazyToChildren()
        item.resetLazy()
        item.value = maxOf(query(item.left, range), query(item.right, range))
        return item.value
    }

    fun update(point: Int, value: Int) {
        update(point..point, value)
    }

    fun update(range: IntRange, value: Int) {
        update(root, range, value)
    }


    private fun update(
        item: Item,
        range: IntRange,
        value: Int
    ) {
        if (item.doesNotOverlapWith(range)) {
            return
        }
        if (item.isLeaf()) {
            item.propagateLazyToSelf()
            item.resetLazy()
            item.value += value
            return
        }
        if (range.contains(item.range)) {
            item.lazy += value
            return
        }
        item.propagateLazyToChildren()
        update(item.left, range, value)
        update(item.right, range, value)
        item.value = maxOf(item.left.value, item.right.value)
        item.resetLazy()
    }

    fun asGraphWizDotFile() {
        GraphWiz.toDotFile { bfs() }
    }

    private fun bfs(item: Item = root): String {
        var output = ""
        if (item.isNotEmpty()) {
            output += "\"$item\" -- \"${item.left}\"\n"
            output += "\"$item\" -- \"${item.right}\"\n"
            output += bfs(item.left)
            output += bfs(item.right)
        }
        return output
    }

    class Item constructor(
        val range: IntRange,
        var value: Int = 0,
        var lazy: Int = 0
    ) {
        val first: Int get() = range.first
        val last: Int get() = range.last
        var left: Item = EMPTY
        var right: Item = EMPTY

        fun isEmpty(): Boolean {
            return range.first == range.last
        }

        fun isLeaf(): Boolean {
            return isEmpty()
        }

        fun propagateLazyToChildren() {
            arrayOf(left, right).forEach { it.lazy += lazy }
        }

        fun propagateLazyToSelf() {
            value += lazy
        }

        fun resetLazy() {
            lazy = 0
        }


        fun isNotEmpty(): Boolean {
            return !isEmpty()
        }

        fun contains(point: Int): Boolean {
            return range.contains(point)
        }

        fun contains(other: IntRange): Boolean {
            return range.contains(other)
        }

        fun doesNotOverlapWith(other: IntRange): Boolean {
            return (last < other.first || first > other.last)
        }

        override fun toString(): String {
            return "[$range][$value][$lazy]"
        }
    }

    companion object {
        val EMPTY = Item(IntRange.EMPTY)
        const val INVALID = Int.MIN_VALUE

    }

}

fun IntRange.contains(other: IntRange): Boolean {
    return first <= other.first && last >= other.last
}

val queryDuration = Duration("Query")
val updateDuration = Duration("Update")
val graphWizDuration = Duration("GraphWiz")
fun main(args: Array<String>) {
    setDevelopmentFlag(listOf("test").toTypedArray())
    sandbox(500000000) {
//        val inputs = mutableListOf(7, 8, 1, 4, 3, 9, 8, 2, 6, 4, 10)
//        log(inputs)
//        val segmentTree = withTimeToExecution("Init") { SegmentTree(IntRange(0, inputs.size)) }
//        withTimeToExecution("Update") {
//            inputs.forEachIndexed { i, v ->
//                segmentTree.update(i, v)
//            }
//        }
//        val ranges = listOf(0..0, 2..7, 3..6, 5..10, 6..9)
//        withTimeToExecution("QueryAndUpdateWithValidation") {
//            ranges.forEach {
//                validateQuery(it, inputs, segmentTree)
//            }
//            listOf(2..7, 3..9, 4..5, 1..10).forEach {
//                update(it, inputs, segmentTree, 1)
//            }
//            debugLog(inputs)
//            ranges.forEach {
//                validateQuery(it, inputs, segmentTree)
//            }
//            listOf(queryDuration, updateDuration, graphWizDuration).forEach { debugLog(it) }
//        }
    }
}

fun bruteForceQuery(inputs: List<Int>, range: IntRange): Int {
    return range.map { inputs[it] }.max() ?: INVALID
}

fun bruteForceUpdate(inputs: MutableList<Int>, range: IntRange, value: Int) {
    return range.forEach { inputs[it] += value }
}

fun validateQuery(
    range: IntRange,
    inputs: List<Int>,
    segmentTree: SegmentTree
) {
    val max = queryDuration.timed { segmentTree.query(range) }
//    debugLog("Validating query for $range")
    graphWizDuration.timed {
        segmentTree.asGraphWizDotFile()
    }
//    assertEquals(bruteForceQuery(inputs, range), max, "For $range")
}

fun update(
    range: IntRange,
    inputs: MutableList<Int>,
    segmentTree: SegmentTree,
    value: Int
) {
//    debugLog("updating for $range, with $value")
    updateDuration.timed { segmentTree.update(range, value) }
    bruteForceUpdate(inputs, range, value)
}