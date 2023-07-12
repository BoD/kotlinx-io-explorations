plugins {
  kotlin("multiplatform").version("1.9.0")
}

group = "com.example"
version = "1.0.0"

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }
  macosArm64 {
    binaries {
      executable {
        entryPoint = "com.example.kotlinxiotest.main"
      }
    }
  }
  linuxX64 {
    binaries {
      executable {
        entryPoint = "com.example.kotlinxiotest.main"
      }
    }
  }

  sourceSets {
    val nativeMain by creating

    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.2.1")
        implementation("com.squareup.okio:okio:3.4.0")
      }
    }
    val macosArm64Main by getting {
      dependsOn(nativeMain)
    }
    val linuxX64Main by getting {
      dependsOn(nativeMain)
    }
    val jvmMain by getting {
    }

    val jvmTest by getting {
      dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test-junit")
      }
    }
  }
}
