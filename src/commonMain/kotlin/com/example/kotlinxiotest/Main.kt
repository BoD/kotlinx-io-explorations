package com.example.kotlinxiotest

import com.example.kotlinxiotest.okio.OkioPrefixer

fun main(av: Array<String>) {
  println("Hello, World!")
//  KotlinxIoPrefixer(av[0], av[1]).prefix()
  OkioPrefixer(av[0], av[1]).prefix()
  println("Done")
}
