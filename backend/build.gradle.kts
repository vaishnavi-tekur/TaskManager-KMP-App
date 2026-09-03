plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization") version "2.4.10"
}
repositories {
    mavenCentral()
}
dependencies {
    implementation("io.ktor:ktor-server-core-jvm:3.3.0")
    implementation("io.ktor:ktor-server-netty-jvm:3.3.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.3.0")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation(kotlin("test"))
}
application { mainClass.set("com.example.taskmanagerkmpapp.ApplicationKt") }
