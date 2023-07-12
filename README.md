# Kotlinx-io explorations

This repository contains some explorations of the [Kotlinx-io](https://github.com/Kotlin/kotlinx-io) library.

## Context

[Okio](https://github.com/square/okio) has been a go-to IO library for Java and Kotlin, in particular Kotlin Multiplatform, for a while. It
is a mature and stable library.
Recently, JetBrains have released version `0.2.0` of their Kotlinx-io library, which is based on/inspired by Okio.

I have no specific insight into the creation of kotlinx-io, but my understanding is that one goal is to have this more of a "standard"
Kotlin library, maintained by JetBrains, rather than a third-party library. If this becomes a widely adopted library, this will help library
authors with deciding what IO library to use / expose in their APIs (i.e. having a Kotlin/KotlinX dependency is less controversial than
using a third-party one - arguably).

## API

The API of Kotlinx-io is very analogous to Okio, but not identical.

From what I can tell, the way this was done is that the main classes from Okio where copied over to Kotlinx-io, and then the API was tweaked
to either be more idiomatic Kotlin, or improve naming. I assume Java compat is also less of a priority.

The 4 main classes from Okio are there: `Buffer`, `Source`, `Sink` and `ByteString`, with mostly the exact same API.

To have an example, in this project is a little CLI app that takes 2 files as arguments and will read the first one and output it to the
second one, with a line number prefixed for each line. It will also print the prefixed lines to the console.

Here's an annotated diff of both versions:

```
package com.example.kotlinxiotest.okio                          |       package com.example.kotlinxiotest.kotlinxio
                                                                        
import okio.Buffer                                              |       import kotlinx.io.Buffer                                        | Package differences
import okio.FileSystem                                          |       import kotlinx.io.RawSink
import okio.Path.Companion.toPath                               |       import kotlinx.io.buffered
import okio.Sink                                                |       import kotlinx.io.files.Path
import okio.Timeout                                             |       import kotlinx.io.files.sink
import okio.buffer                                              |       import kotlinx.io.files.source
import okio.use                                                 |       import kotlinx.io.readLine
                                                                >       import kotlinx.io.readString
                                                                >       import kotlinx.io.writeString
                                                                        
class OkioPrefixer(                                             |       class KotlinxIoPrefixer(
  private val source: String,                                             private val source: String,
  private val destination: String,                                        private val destination: String,
) {                                                                     ) {
  fun prefix() {                                                          fun prefix() {
    val sourcePath = source.toPath()                            |           val sourcePath = Path(source)                               | Different ways of getting a Source/Sink from a Path. Note: Path is experimental in kotlinx-io
    val sourceSource = systemFileSystem.source(sourcePath       |           val sourceSource = sourcePath.source()
      .buffer()                                                 |             .buffered()
    val destinationPath = destination.toPath()                  |           val destinationPath = Path(destination)
    val destinationSink = systemFileSystem.sink(destinati       |           val destinationSink = destinationPath.sink()
    val teeSink = TeeSink(destinationSink, StdoutSink())                    val teeSink = TeeSink(destinationSink, StdoutSink())
      .buffer()                                                 |             .buffered()                                               | buffer() renamed to bufferred()
                                                                >           @OptIn(ExperimentalStdlibApi::class)                        | Okio has its own .use, whereas with kotlinx-io we use the stdlib one (which is experimental) 
    teeSink.use { sink ->                                                   teeSink.use { sink ->
      sourceSource.use { source ->                                            sourceSource.use { source ->
        var lineIndex = 1                                                       var lineIndex = 1
        var line: String?                                                       var line: String?
        while (true) {                                                          while (true) {                                          
          line = source.readUtf8Line() ?: break                 |                 line = source.readLine() ?: break                     | readUtf8Line() renamed to readLine()
          val lineIndexFormatted = lineIndex++.toString()                         val lineIndexFormatted = lineIndex++.toString()
          sink.writeUtf8(lineIndexFormatted)                    |                 sink.writeString(lineIndexFormatted)                  | writeUtf8 renamed to writeString
          sink.writeByte(' '.code)                              |                 sink.writeByte(' '.code.toByte())                     | writeByte() now takes a Byte, not an Int
          sink.writeUtf8(line)                                  |                 sink.writeString(line)
          sink.writeByte('\n'.code)                             |                 sink.writeByte('\n'.code.toByte())
        }                                                                       }
      }                                                                       }
    }                                                                       }
  }                                                                       }
}                                                                       }
                                                                        
class TeeSink(                                                          class TeeSink(
  private val a: Sink,                                          |         private val a: RawSink,                                       | Okio's Sink (top level interface) has been renamed to RawSink, while BufferedSink (implements Sink) has been renamed to Sink (implements RawSink)
  private val b: Sink,                                          |         private val b: RawSink,
) : Sink {                                                      |       ) : RawSink {
  override fun close() {                                                  override fun close() {
    a.close()                                                               a.close()
    b.close()                                                               b.close()
  }                                                                       }
                                                                        
  override fun flush() {                                                  override fun flush() {
    a.flush()                                                               a.flush()
    b.flush()                                                               b.flush()
  }                                                                       }
                                                                        
  override fun write(source: Buffer, byteCount: Long) {                   override fun write(source: Buffer, byteCount: Long) {
    val peek = Buffer().apply { source.copyTo(this, 0, by                   val peek = Buffer().apply { source.copyTo(this, 0, by
    a.write(peek, byteCount)                                                a.write(peek, byteCount)
    b.write(source, byteCount)                                              b.write(source, byteCount)
  }                                                                       }
                                                                <       
  override fun timeout(): Timeout {                             <                                                                       | No more timeouts in kotlinx-io
    return Timeout.NONE                                         <       
  }                                                             <       
}                                                                       }
                                                                        
class StdoutSink : Sink {                                       |       class StdoutSink : RawSink {
  override fun close() {}                                                 override fun close() {}
                                                                        
  override fun flush() {}                                                 override fun flush() {}
                                                                        
  override fun write(source: Buffer, byteCount: Long) {                   override fun write(source: Buffer, byteCount: Long) {
    print(source.readUtf8(byteCount))                           |           print(source.readString(byteCount))
  }                                                                       }
                                                                <       
  override fun timeout(): Timeout {                             <       
    return Timeout.NONE                                         <       
  }                                                             <       
}                                                                       }
                                                                <       
expect val systemFileSystem: FileSystem                         <                                                                       | Okio uses FileSystem to abstract file operations and get Sink/Source from a Path. Kotlinx-io uses Path directly.
                                                                <
```

## Missing parts

From what I can tell, Kotlinx-io will not be a complete replacement for Okio, or at least it is not yet for now. Here are a few things that
are in Okio but not in Kotlinx-io:

- FileSystem
- Pipe
- Compression (Deflater, Inflater, Gzip)
- Hashing (md5, sha1, etc.)
- Encryption/Decryption (Cipher)
- Getting a Source/Sink from a Socket

## Targets

Both libraries target the JVM, JS (Node, Browser) and Native (Apple, Linux, Windows, Android).

WASM:

- Okio: support is ongoing: https://github.com/square/okio/issues/1203
- Kotlinx-io: not supported yet: https://github.com/Kotlin/kotlinx-io/issues/164


## TODO

- Look at how easy it would be to have Okio <-> kotlinx-io adapters
- Benchmarks?
