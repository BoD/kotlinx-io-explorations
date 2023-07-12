package com.example.kotlinxiotest

import kotlinx.io.readByteArray
import kotlinx.io.snapshot
import okio.ByteString.Companion.toByteString
import okio.Timeout

/*
 * okio.Source -> kotlinx.io.RawSource
 */

private class OkioSourceToKotlinxIoRawSource(
  private val okioSource: okio.Source,
) : kotlinx.io.RawSource {
  override fun close() {
    okioSource.close()
  }

  override fun readAtMostTo(sink: kotlinx.io.Buffer, byteCount: Long): Long {
    val okioBuffer = okio.Buffer()
    val read = okioSource.read(okioBuffer, byteCount)
    sink.write(okioBuffer.snapshot().toByteArray())
    return read
  }
}

fun okio.Source.asKotlinxIoRawSource(): kotlinx.io.RawSource {
  return OkioSourceToKotlinxIoRawSource(this)
}


/*
 * okio.Sink -> kotlinx.io.RawSink
 */

private class OkioSinkToKotlinxIoRawSink(
  private val okioSink: okio.Sink,
) : kotlinx.io.RawSink {
  override fun close() {
    okioSink.close()
  }

  override fun flush() {
    okioSink.flush()
  }

  override fun write(source: kotlinx.io.Buffer, byteCount: Long) {
    val okioBuffer = okio.Buffer()
    val byteArray = source.readByteArray(byteCount.toInt())
    okioBuffer.write(byteArray)
    okioSink.write(okioBuffer, byteCount)
  }
}

fun okio.Sink.asKotlinxIoRawSink(): kotlinx.io.RawSink {
  return OkioSinkToKotlinxIoRawSink(this)
}


/*
 * kotlinx.io.RawSource -> okio.Source
 */

private class KotlinxIoRawSourceToOkioSource(
  private val kotlinxIoRawSource: kotlinx.io.RawSource,
) : okio.Source {
  override fun close() {
    kotlinxIoRawSource.close()
  }

  override fun read(sink: okio.Buffer, byteCount: Long): Long {
    val kotlinxIoBuffer = kotlinx.io.Buffer()
    val read = kotlinxIoRawSource.readAtMostTo(kotlinxIoBuffer, byteCount)
    sink.write(kotlinxIoBuffer.snapshot().toByteArray())
    return read
  }

  override fun timeout(): Timeout {
    return Timeout.NONE
  }
}

fun kotlinx.io.RawSource.asOkioSource(): okio.Source {
  return KotlinxIoRawSourceToOkioSource(this)
}


/*
 * kotlinx.io.RawSink -> okio.Sink
 */

private class KotlinxIoRawSinkToOkioSink(
  private val kotlinxIoRawSink: kotlinx.io.RawSink,
) : okio.Sink {
  override fun close() {
    kotlinxIoRawSink.close()
  }

  override fun flush() {
    kotlinxIoRawSink.flush()
  }

  override fun write(source: okio.Buffer, byteCount: Long) {
    val kotlinxIoBuffer = kotlinx.io.Buffer()
    val byteArray = source.readByteArray(byteCount)
    kotlinxIoBuffer.write(byteArray)
    kotlinxIoRawSink.write(kotlinxIoBuffer, byteCount)
  }

  override fun timeout(): Timeout {
    return Timeout.NONE
  }
}

fun kotlinx.io.RawSink.asOkioSink(): okio.Sink {
  return KotlinxIoRawSinkToOkioSink(this)
}


/*
 * okio.ByteString <-> kotlinx.io.bytestring.ByteString
 */

fun okio.ByteString.asKotlinxIoByteString(): kotlinx.io.bytestring.ByteString {
  return kotlinx.io.bytestring.ByteString(this.toByteArray())
}

fun kotlinx.io.bytestring.ByteString.asOkioByteString(): okio.ByteString {
  return this.toByteArray().toByteString()
}
