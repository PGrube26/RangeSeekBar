plugins {
    id("com.android.library")
    kotlin("android")
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

//// build a jar with source files
//task sourcesJar(type: Jar) {
//    from android.sourceSets.main.java.srcDirs
//    classifier = 'sources'
//}
//
//task javadoc(type: Javadoc) {
//    failOnError  false
//    source = android.sourceSets.main.java.sourceFiles
//    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
//    classpath += configurations.compile
//}
//
//// build a jar with javadoc
//task javadocJar(type: Jar, dependsOn: javadoc) {
//    classifier = 'javadoc'
//    from javadoc.destinationDir
//}
//
//artifacts {
//    archives sourcesJar
//    archives javadocJar
//}
