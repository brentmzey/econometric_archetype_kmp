plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.econometrics"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.knowm.xchart:xchart:3.8.7")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.11")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}

sourceSets {
    main {
        kotlin.srcDirs("src/commonMain/kotlin", "src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources")
    }
    test {
        kotlin.srcDirs("src/commonTest/kotlin", "src/jvmTest/kotlin")
        resources.srcDirs("src/jvmTest/resources")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

application {
    mainClass.set("com.econometrics.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.econometrics.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
