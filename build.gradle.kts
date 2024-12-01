plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

(1..25)
    .map { it.toString().padStart(2, '0') }
    .forEach {
        task<JavaExec>("day$it") {
            group = "aoc"
            mainClass = "dev.ssch.day$it.Day${it}Kt"
            classpath = sourceSets["main"].runtimeClasspath
        }
    }
