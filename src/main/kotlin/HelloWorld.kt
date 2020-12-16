package main.kotlin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    if(args.isEmpty()){
        println("Usage <number_of runs>")
        return;
    }
    println("Start")

    // Start a coroutine
    GlobalScope.launch {
        delay(1000)
        println("Hello")
    }

    Thread.sleep(2000) // wait for 2 seconds
    println("Stop")
}

fun logDuration(start: Instant, end: Instant, name: String) {
    println("[timed]:[function:$name]:[${Duration.between(start, end).toMillis()} ms, ${TimeUnit.NANOSECONDS.toMicros(Duration.between(start, end).toNanos())} us]")
}

