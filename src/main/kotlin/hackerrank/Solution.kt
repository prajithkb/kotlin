package main.kotlin.hackerrank

import java.io.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis

/******* utility functions *************/

data class DevOverrides(var isDebug: Boolean, var readFromFile: Boolean)

val devOverrides = DevOverrides(false, false)

/** Logging ***/

class Logger {

    enum class Level {
        DEBUG,
        ERROR,
        VALIDATED_LOG
    }

    fun log(level: Level, message: Any?) {
        when (level) {
            Level.VALIDATED_LOG -> validatedWriteLn("$message")
            Level.DEBUG, Level.ERROR -> writeLn("$level: $message")
        }
    }

    fun output(message: Any?) {
        log(Level.VALIDATED_LOG, message)
    }

    fun debug(message: Any?) {
        if (devOverrides.isDebug) {
            log(Level.DEBUG, message)
        }
    }

    fun error(message: Any?) {
        if (devOverrides.isDebug) {
            log(Level.ERROR, message)
        }
    }
}

val logger = Logger()

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
    logger.debug("Elapsed time: $time ms for Operation: $operationName")
    return value!!
}

fun <T> completeWithin(timeoutInMilliSecs: Long = 5000, block: () -> T): T? {
    try {
        return CompletableFuture
            .supplyAsync(block)
            .get(timeoutInMilliSecs, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
        logger.error("Failed end complete within $timeoutInMilliSecs ms")
        return null
    } catch (e: ExecutionException) {
        if (e.cause is AssertionError) {
            logger.error("Execution exception ${e.cause} ms")
        } else {
            throw e
        }
        return null
    }
}

fun setDevelopmentFlag(args: Array<String>) {
    devOverrides.isDebug = args.contains("test")
    devOverrides.readFromFile = args.contains("readFromFile")
    if (devOverrides.isDebug) {
        bufferedReader = File(OUTPUT_FILE).bufferedReader()
    }
}


/*** Read and Write ****/
val OUTPUT_FILE = "/Users/kprajith/Desktop/Hackerrank_Output.txt"
val INPUT_FILE = "/Users/kprajith/Desktop/Hackerrank_Input.txt"

var bufferedReader: BufferedReader? = null

fun scanner(): Scan {
    if (devOverrides.readFromFile) {
        return Scan(File(INPUT_FILE).bufferedReader())
    } else {
        return Scan(System.`in`.bufferedReader())
    }
}

/**
 * A Scan utility class to scan and tokenize inputs
 */
class Scan(private val reader: BufferedReader) {

    private val EMPTY = "EMPTY"

    private var currentLine = EMPTY

    private var tokenIndex = 0

    private var tokens = listOf<String>()

    private fun <R> tokenize(block: () -> R): R {
        if (currentLine == EMPTY || tokenIndex == tokens.size) {
            readLine()
            tokenize()
        }
        return block()
    }

    private fun readLine() {
        currentLine = reader.readLine()
    }

    fun nextLine(): String {
        readLine()
        return currentLine
    }

    fun nextInt(): Int {
        return tokenize { tokens.map { it.trim().toInt() }[tokenIndex++] }
    }

    fun nextLong(): Long {
        return tokenize { tokens.map { it.trim().toLong() }[tokenIndex++] }
    }

    fun nextInts(): List<Int> {
        return nextLine().split(" ").map { it.trim().toInt() }
    }

    fun nextLongs(): List<Long> {
        return nextLine().split(" ").map { it.trim().toLong() }
    }

    private fun readIfIteratorIsEmpty() {
        if (tokenIndex == -1) {
            readLine()
            tokenIndex = 0
        }
        tokenIndex++
    }

    private fun tokenize() {
        tokens = currentLine.split(" ")
        tokenIndex = 0
    }


}

/**
 * A BufferedWriter that also validates the output.
 *
 * The output (each line) is validated against the expected output file.
 */
class ValidatedBufferedWriter(private val out: BufferedWriter) {

    fun validatedWriteLn(str: String) {
        writeLn(str)
        if (devOverrides.isDebug) {
            bufferedReader?.apply {
                val expected = this.readLine()
                compareAndThrow(expected, str)
            }
        }
    }

    fun writeLn(str: String) {
        out.write(str)
        out.newLine()
    }

    private fun compareAndThrow(expected: String, actual: String) {
        if (expected != actual) {
            throw AssertionError("Expected: <$expected> is not equal to actual: <$actual>")
        }
    }

    fun resource(): Closeable {
        return Closeable {
            out.close()
            bufferedReader?.close()
        }
    }
}

val writer = ValidatedBufferedWriter(BufferedWriter(OutputStreamWriter(System.out)))

fun validatedWriteLn(message: String) {
    writer.validatedWriteLn(message)
}

fun writeLn(message: String) {
    writer.writeLn(message)
}


val isDebug =
    java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
val defaultTimeOut = if (isDebug) 50000000L else 5000L

inline fun sandbox(within: Long = defaultTimeOut, crossinline block: () -> Any) {
    writer.resource().use {
        logger.debug("Running with timeout of $within")
        completeWithin(within) {
            withTimeToExecution("main") {
                block()
            }
        }
        logger.debug("SUCCESS")
    }
}

/******* utility functions ( above) *************/

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val inputScanner = scanner()
    sandbox {
        val cells = mutableListOf<IntArray>()
        val (height, _) = inputScanner.nextInts()
        for (i in 1..height) {
            cells.add(inputScanner.nextInts().toIntArray())
        }
        logger.output(calculateSurfaceArea(cells))
    }
}

fun calculateSurfaceArea(cells: MutableList<IntArray>): Any? {
    val height = cells.size
    val width = cells[0].size
    val top = cells[0].sum()
    val bottom = cells[height - 1].sum()
    val left = cells.map { it[0] }.sum()
    val right = cells.map { it[width - 1] }.sum()
    val base = 2 * height * width
    var inBetween = 0
    val horizontals = mutableListOf<Int>()
    val verticals = mutableListOf<Int>()
    for (i in 0 until height - 1) {
        for (j in 0 until width - 1) {
            val horizontal = Math.abs(cells[i][j] - cells[i][j + 1])
            horizontals.add(horizontal)
            val vertical = Math.abs(cells[i][j] - cells[i + 1][j])
            verticals.add(vertical)
            inBetween += vertical
            inBetween += horizontal
        }
    }
    for (i in 0 until width - 1) {
        inBetween += Math.abs(cells[height - 1][i] - cells[height - 1][i + 1])
    }
    for (i in 0 until height - 1) {
        inBetween += Math.abs(cells[i][width - 1] - cells[i + 1][width - 1])
    }
    return listOf(top, bottom, left, right, base, inBetween).sum()
}

