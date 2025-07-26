/*
 * This file is part of the UNES Open Source Project.
 * UNES is licensed under the GNU GPLv3.
 *
 * Copyright (c) 2019. João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
plugins {
    java
    kotlin("jvm") version "2.1.20"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
    google()
}

val artifactVersion: String by project

group = "dev.forcetower.unes"
version = artifactVersion

val sourcesJar = tasks.register<Jar> ("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    val task = project.tasks["javadoc"] as Javadoc
    from(task.destinationDir)
    dependsOn(task)
}

artifacts {
    archives(sourcesJar.get())
    archives(javadocJar.get())
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.forcetower.unes"
            artifactId = "juice"
            version = artifactVersion

            pom {
                name.set("Juice")
                description.set("A SAGRES Scrapper Library")
                url.set("https://github.com/ForceTower/Juice")
                from(components["java"])

                licenses {
                    license {
                        name.set("General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }

                developers {
                    developer {
                        id.set("forcetower")
                        name.set("João Paulo Sena")
                        email.set("joaopaulo761@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/juice.git")
                    developerConnection.set("scm:git:ssh://github.com/juice.git")
                    url.set("https://juice.forcetower.dev")
                }
            }

            artifact(sourcesJar) {
                classifier = "sources"
            }

            artifact(javadocJar) {
                classifier = "javadoc"
            }
        }
    }

    repositories {
        maven {
            val sonatypeUsername = System.getenv("sonatypeUsername") ?: "username"
            val sonatypePassword = System.getenv("sonatypePassword") ?: "password"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            name = "maven"
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("org.json:json:20231013")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("commons-codec:commons-codec:1.15")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("17"))
    }
}