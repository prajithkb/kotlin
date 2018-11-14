package main.kotlin.graphs

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
    ) {
        union(Element<Item>(null, from), Element<Item>(null, to))
    }

    fun union(
        from: Element<Item>,
        to: Element<Item>
    ) {
        val (toParentIndex, toParentSize) = findParentIndexPairOrCreate(to)
        val (fromParentIndex, fromParentSize) = findParentIndexPairOrCreate(from)
        val newParentIndexSizePair = Pair(toParentIndex, toParentSize + fromParentSize)
        parentIndexSizePairs[fromParentIndex] = newParentIndexSizePair
        parentIndexSizePairs[toParentIndex] = newParentIndexSizePair
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

fun main(args: Array<String>) {
    val ds = DisjointSet<String>(5)
    ds.union(
        DisjointSet.Element<String>("a", 1),
        DisjointSet.Element<String>("b", 2)
    )
    ds.union(
        DisjointSet.Element<String>("c", 3),
        DisjointSet.Element<String>("d", 4)
    )

    println(ds)
}