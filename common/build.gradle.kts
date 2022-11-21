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
                api("io.ktor:ktor-client-core:2.1.3")
                api("io.ktor:ktor-client-cio:2.1.3")
                api("io.ktor:ktor-client-content-negotiation:2.1.3")
                api("io.ktor:ktor-serialization-kotlinx-json:2.1.3")
                api("io.ktor:ktor-client-logging:2.1.3")
                api("org.ocpsoft.prettytime:prettytime:5.0.2.Final")
                api("com.alialbaali.kamel:kamel-image:0.4.0")
                api("com.mikepenz:multiplatform-markdown-renderer:0.6.1")
                api("io.realm.kotlin:library-base:1.4.0")
                api("com.mikepenz:aboutlibraries-core:10.5.1")
                api("com.mikepenz:aboutlibraries-compose:10.5.1")
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
                api("io.coil-kt:coil-gif:2.2.2")
                api("com.google.accompanist:accompanist-flowlayout:0.27.0")
                api("com.google.accompanist:accompanist-navigation-material:0.27.0")
                api("androidx.navigation:navigation-compose:2.5.3")
                api("com.google.accompanist:accompanist-swiperefresh:0.27.0")
                api("com.fragula2:fragula-compose:2.4")

                val markwon_version = "4.6.2"
                api("io.noties.markwon:core:$markwon_version")
                api("io.noties.markwon:ext-strikethrough:$markwon_version")
                api("io.noties.markwon:ext-tables:$markwon_version")
                api("io.noties.markwon:html:$markwon_version")
                api("io.noties.markwon:linkify:$markwon_version")
                api("io.noties.markwon:image-coil:$markwon_version")
                api("io.noties.markwon:syntax-highlight:$markwon_version") {
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
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                api(compose.desktop.components.splitPane)
                api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
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


configure<com.mikepenz.aboutlibraries.plugin.AboutLibrariesExtension> {
    registerAndroidTasks = false
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    dependsOn("exportLibraryDefinitions")
}