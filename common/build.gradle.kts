plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("kotlinx-serialization")
}

group = "com.example"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.material)
                api(compose.materialIconsExtended)
                api("io.ktor:ktor-client-core:2.1.2")
                api("io.ktor:ktor-client-cio:2.1.2")
                api("io.ktor:ktor-client-content-negotiation:2.1.2")
                api("io.ktor:ktor-serialization-kotlinx-json:2.1.2")
                api("io.ktor:ktor-client-logging:2.1.2")
                api("org.ocpsoft.prettytime:prettytime:5.0.2.Final")
                api("com.alialbaali.kamel:kamel-image:0.4.0")
                api("com.mikepenz:multiplatform-markdown-renderer:0.6.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.5.1")
                api("androidx.core:core-ktx:1.9.0")
                api("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
                api("androidx.lifecycle:lifecycle-runtime-compose:2.6.0-alpha03")
                api("io.coil-kt:coil-compose:2.2.2")
                api("com.google.accompanist:accompanist-flowlayout:0.27.0")
                api("androidx.navigation:navigation-compose:2.5.3")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}