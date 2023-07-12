package com.example.kotlinxiotest.okio

import okio.Buffer
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Sink
import okio.Timeout
import okio.buffer
import okio.use

expect val systemFileSystem: FileSystem

class OkioPrefixer(
  private val source: String,
  private val destination: String,
) {
  fun prefix() {
    val sourcePath = source.toPath()
    val sourceSource = systemFileSystem.source(sourcePath)
      .buffer()
    val destinationPath = destination.toPath()
    val destinationSink = systemFileSystem.sink(destinationPath)
    val teeSink = TeeSink(destinationSink, StdoutSink())
      .buffer()
    teeSink.use { sink ->
      sourceSource.use { source ->
        var lineIndex = 1
        var line: String?
        while (true) {
          line = source.readUtf8Line() ?: break
          val lineIndexFormatted = lineIndex++.toString().padStart(5)
          sink.writeUtf8(lineIndexFormatted)
          sink.writeByte(' '.code)
          sink.writeUtf8(line)
          sink.writeByte('\n'.code)
        }
      }
    }
  }
}

class TeeSink(
  private val a: Sink,
  private val b: Sink,
) : Sink {
  override fun close() {
    a.close()
    b.close()
  }

  override fun flush() {
    a.flush()
    b.flush()
  }

  override fun write(source: Buffer, byteCount: Long) {
    val peek = Buffer().apply { source.copyTo(this, 0, byteCount) }
    a.write(peek, byteCount)
    b.write(source, byteCount)
  }

  override fun timeout(): Timeout {
    return Timeout.NONE
  }
}

class StdoutSink : Sink {
  override fun close() {}

  override fun flush() {}

  override fun write(source: Buffer, byteCount: Long) {
    print(source.readUtf8(byteCount))
  }

  override fun timeout(): Timeout {
    return Timeout.NONE
  }
}
