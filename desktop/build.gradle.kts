import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.mikepenz.aboutlibraries.plugin")
}

group = "com.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "StartKt"
        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "demo1"//"GitHub Topics"
            packageVersion = "1.0.0"
            /*appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            macOS {
                iconFile.set(project.file("logo.icns"))
            }
            windows {
                iconFile.set(project.file("logo.ico"))
                dirChooser = true
                console = true
            }
            linux {
                iconFile.set(project.file("logo.png"))
            }*/
        }
    }
}

tasks.register("BuildAboutLibraries") {
    doFirst {
        exec {
            workingDir(projectDir)
            commandLine("./gradlew desktop:exportLibraryDefinitions -PaboutLibraries.exportPath=src/jvmMain/resources/")
        }
    }
}

/*tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    dependsOn("BuildAboutLibraries")
}*/