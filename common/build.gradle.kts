import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("kotlinx-serialization")
    id("io.realm.kotlin")
    kotlin("kapt")
    id("com.mikepenz.aboutlibraries.plugin")
}

group = "com.example"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    androidTarget()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    /*js(IR) {
        browser()
    }*/
    sourceSets {
        val ktorVersion = extra["ktor.version"] as String
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.material)
                api(compose.materialIconsExtended)
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-cio:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("io.ktor:ktor-client-logging:$ktorVersion")
                api("org.ocpsoft.prettytime:prettytime:5.0.2.Final")
                api("com.alialbaali.kamel:kamel-image:0.4.1")
                api("com.mikepenz:multiplatform-markdown-renderer:0.6.1")
                api("io.realm.kotlin:library-base:1.9.0")
                api("com.mikepenz:aboutlibraries-core:10.5.2")
                api("com.mikepenz:aboutlibraries-compose:10.5.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
                api("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
                api("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
                api("io.coil-kt:coil-compose:2.4.0")
                api("io.coil-kt:coil-gif:2.4.0")
                api("com.google.accompanist:accompanist-flowlayout:0.28.0")
                api("com.google.accompanist:accompanist-navigation-material:0.30.1")
                api("androidx.navigation:navigation-compose:2.6.0")
                api("com.fragula2:fragula-compose:2.8")

                val markwonVersion = "4.6.2"
                api("io.noties.markwon:core:$markwonVersion")
                api("io.noties.markwon:ext-strikethrough:$markwonVersion")
                api("io.noties.markwon:ext-tables:$markwonVersion")
                api("io.noties.markwon:html:$markwonVersion")
                api("io.noties.markwon:linkify:$markwonVersion")
                api("io.noties.markwon:image-coil:$markwonVersion")
                api("io.noties.markwon:syntax-highlight:$markwonVersion") {
                    exclude("org.jetbrains", "annotations-java5")
                }
                configurations["kapt"].dependencies.add(
                    DefaultExternalModuleDependency(
                        "io.noties",
                        "prism4j-bundler",
                        "2.0.0"
                    )
                )

                api("pl.droidsonroids.gif:android-gif-drawable:1.2.25")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                api(compose.desktop.components.splitPane)
                api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
                api("me.friwi:jcefmaven:108.4.13")
                api("com.github.Dansoftowner:jSystemThemeDetector:3.6")
            }
        }
        val desktopTest by getting

        /*val jsMain by getting {
            dependencies {
                api(compose.web.core)
                api("io.ktor:ktor-client-js:$ktorVersion")
            }
        }*/
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


configure<com.mikepenz.aboutlibraries.plugin.AboutLibrariesExtension> {
    registerAndroidTasks = false
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    dependsOn("exportLibraryDefinitions")
}