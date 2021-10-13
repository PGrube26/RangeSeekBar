plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.5.30"
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.3.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}

val dokkaOutputDir = "$buildDir/dokka"

tasks.getByName<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
    outputDirectory.set(file(dokkaOutputDir))
}
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.pgrube26"
                artifactId = "range_seek_bar"
                version = "1.2"

                artifact(javadocJar)

                pom {
                    name.set("RangeSeekBar")
                    description.set("A simple library to show a discrete slider to let the user set a range.")
                    url.set("https://github.com/PGrube26/RangeSeekBar")
                    developers {
                        developer {
                            id.set("pgrube26")
                            name.set("Peter Grube")
                            email.set("pgrube26@gmail.com")
                        }
                    }
                }
            }
        }
    }
}
