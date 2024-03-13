plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.library") version "8.2.1"
    id("maven-publish")
}

android {
    namespace = "ru.evolinc.servection"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        multipleVariants {
            allVariants()
            withJavadocJar()
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenRelease") {
                groupId = "ru.evolinc"
                artifactId = "evolinc"
                version = "0.1"

                from(components["release"])
            }
            create<MavenPublication>("mavenDebug") {
                groupId = "ru.evolinc"
                artifactId = "evolinc"
                version = "0.1"

                from(components["debug"])
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(kotlin("reflect"))
}