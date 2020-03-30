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
buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    java
    kotlin("jvm") version "1.3.71"
    maven
    `maven-publish`
    signing
    id("org.jmailen.kotlinter") version "2.2.0"
}

repositories {
    mavenCentral()
    google()
}

val artifactVersion: String by project

group = "com.github.forcetower"
version = artifactVersion

val sourcesJar = task<Jar> ("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    val task = project.tasks["javadoc"] as Javadoc
    from(task.destinationDir)
    dependsOn(task)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.forcetower"
            artifactId = "juice"
            version = artifactVersion

            pom {
                name.set("Juice")
                description.set("A SAGRES Scrapper Library")
                url.set("http://github.com/ForceTower/Juice")
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
                    url.set("http://www.forcetower.dev/juice")
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
            val sonatypeUsername: String by project
            val sonatypePassword: String by project
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.71")
    implementation("org.jsoup:jsoup:1.11.2")
    implementation("com.squareup.okhttp3:okhttp:4.3.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0-RC3")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.json:json:20180130")
    implementation("commons-codec:commons-codec:1.13")

    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.0-RC")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}