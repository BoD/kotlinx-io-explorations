package com.example.kotlinxiotest

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.sink
import kotlinx.io.files.source
import kotlinx.io.readLine
import kotlinx.io.writeString

@OptIn(ExperimentalStdlibApi::class)
fun main(av: Array<String>) {
  println("Hello, World!")
  val sourcePath = Path(av[0])
  val destinationPath = Path(av[1])
  destinationPath.sink().buffered().use { sink ->
    sourcePath.source().buffered().use { source ->
      var lineIndex = 1
      var line: String?
      while (true) {
        line = source.readLine() ?: break
        val lineIndexFormatted = lineIndex++.toString().padStart(3)
        sink.writeString(lineIndexFormatted)
        sink.writeByte(' '.code.toByte())
        sink.writeString(line)
        sink.writeByte('\n'.code.toByte())
      }
    }
  }
  println("Done")
}
