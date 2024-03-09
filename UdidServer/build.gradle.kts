import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "io.github"
version = "1.0-SNAPSHOT"

val kotlin_version = "1.9.20"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation ("com.alibaba.fastjson2:fastjson2:2.0.43")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.43")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
//    implementation("ch.qos.logback:logback-classic:1.2.3")
//    implementation("org.apache.logging.log4j:log4j-api:2.23.0")
//    implementation("org.apache.logging.log4j:log4j-core:2.23.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}