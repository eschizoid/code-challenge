import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer

plugins {
    id("idea")
    id("cz.alenkacz.gradle.scalafmt") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.github.maiflai.scalatest") version "0.25"
    kotlin("jvm") version "1.3.20"
}

allprojects {
    repositories {
        jcenter()
    }
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "cz.alenkacz.gradle.scalafmt")
    apply(plugin = "com.github.maiflai.scalatest")
    apply(plugin = "scala")
    apply(plugin = "java")
}


group = "com.com.otus.codechallenge"
version = "1.0-SNAPSHOT"

val akkaHttpVersion = "10.1.9"
val akkaHttpCirceVersion = "1.27.0"
val akkaSwaggerVersion = "2.0.3"
val circeVersion = "0.9.3"
val logbackVersion = "1.2.3"
val mongoScalaDriverVersion = "2.7.0"
val sangriaVersion = "1.4.2"
val sangriaSlowlogVersion = "0.1.8"
val sangriaCirceVersion = "1.2.1"
val scalaVersion = "2.12"
val scalaMinorVersion = "2.12.8"

dependencies {
    // Scala runtime
    compileOnly("org.scala-lang:scala-library:$scalaMinorVersion")
    compileOnly("org.scala-lang:scala-reflect:$scalaMinorVersion")
    compileOnly("org.scala-lang:scala-compiler:$scalaMinorVersion")

    // MongoDB
    implementation("org.mongodb.scala:mongo-scala-driver_$scalaVersion:$mongoScalaDriverVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Sangria GraphQL
    implementation("org.sangria-graphql:sangria_$scalaVersion:$sangriaVersion")
    implementation("org.sangria-graphql:sangria-circe_$scalaVersion:$sangriaCirceVersion")
    implementation("org.sangria-graphql:sangria-slowlog_$scalaVersion:$sangriaSlowlogVersion")

    // Akka Http support
    implementation("com.github.swagger-akka-http:swagger-akka-http_$scalaVersion:$akkaSwaggerVersion")
    implementation("com.typesafe.akka:akka-http_$scalaVersion:$akkaHttpVersion")
    implementation("de.heikoseeberger:akka-http-circe_$scalaVersion:$akkaHttpCirceVersion")

    // Circe Json support
    implementation("io.circe:circe-core_$scalaVersion:$circeVersion")
    implementation("io.circe:circe-optics_$scalaVersion:$circeVersion")
    implementation("io.circe:circe-parser_$scalaVersion:$circeVersion")

    testImplementation("com.squareup.okhttp3:okhttp:4.2.0")
    testImplementation("log4j:log4j:1.2.17")
    testImplementation("org.mockito:mockito-core:2.18.3")
    testImplementation("org.mongodb.scala:mongo-scala-driver_$scalaVersion:$mongoScalaDriverVersion")
    testImplementation("org.pegdown:pegdown:1.6.0")
    testImplementation("org.scalatest:scalatest_$scalaVersion:3.0.6")
    testImplementation("org.scalamock:scalamock_$scalaVersion:4.1.0")
}

tasks {
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes(mapOf("Main-Class" to "com.otus.codechallenge.akka.Server"))
        }
        transform(AppendingTransformer::class.java) {
            resource = "reference.conf"
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

scalafmt {
    configFilePath = ".scalafmt.conf"
}
