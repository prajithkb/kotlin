package main.kotlin.graphs

import java.io.File

class GraphWiz(private val root: Node, val path: String = ".") {

    companion object {
        val FILE_NAME = "graphwiz.dot"

        fun toDotFile(path: String = ".", content: () -> String) {
            val file = File("$path/$FILE_NAME")
            clear(file)
            start(file)
            append(file, content())
            stop(file)
        }

        fun clear(file: File) {
            file.writeText("")
        }

        fun append(file: File, content: String) {
            file.appendText(content)
        }

        fun start(file: File) {
            file.appendText("@startuml\n")
            file.appendText("graph G {\n")
        }

        fun stop(file: File) {
            file.appendText("}\n")
            file.appendText("@enduml\n")
        }
    }

    private val nodes = mutableMapOf<Int, Node>()

    private val file = File("$path/$FILE_NAME")

    init {
        nodes.put(root.id, root)
        clear(file)
    }

    data class Node(val id: Int, val children: MutableSet<Int> = mutableSetOf())


    fun add(node: Node, asAChildOf: Int) {
        val parent = nodes.getOrDefault(asAChildOf, Node(asAChildOf))
        val child = nodes.getOrDefault(node.id, node)
        parent.children.add(node.id)
        nodes[asAChildOf] = parent
        nodes[node.id] = child
    }

    fun reset() {
        nodes.clear()
        nodes.put(root.id, root)
    }


    fun toDotFile() {
        start(file)
        toDotFile { bfs(root) }
        stop(file)
    }
    private fun bfs(root: Node): String {
        var output = ""
        root.children.forEach {
            output += "\"${root.id}\" -- \"$it\"\n"
            output += bfs(nodes[it]!!)
        }
        return output
    }
}