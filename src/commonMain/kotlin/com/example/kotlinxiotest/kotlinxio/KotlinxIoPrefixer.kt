package com.example.kotlinxiotest.kotlinxio

import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.sink
import kotlinx.io.files.source
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlinx.io.writeString

class KotlinxIoPrefixer(
  private val source: String,
  private val destination: String,
) {
  fun prefix() {
    val sourcePath = Path(source)
    val destinationPath = Path(destination)
    val teeSink = TeeSink(destinationPath.sink(), StdoutSink()).buffered()
    @OptIn(ExperimentalStdlibApi::class)
    teeSink.use { sink ->
      sourcePath.source().buffered().use { source ->
        var lineIndex = 1
        var line: String?
        while (true) {
          line = source.readLine() ?: break
          val lineIndexFormatted = lineIndex++.toString().padStart(5)
          sink.writeString(lineIndexFormatted)
          sink.writeByte(' '.code.toByte())
          sink.writeString(line)
          sink.writeByte('\n'.code.toByte())
        }
      }
    }
  }
}

class TeeSink(
  private val a: RawSink,
  private val b: RawSink,
) : RawSink {
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
}

class StdoutSink : RawSink {
  override fun close() {}

  override fun flush() {}

  override fun write(source: Buffer, byteCount: Long) {
    print(source.readString(byteCount))
  }
}
