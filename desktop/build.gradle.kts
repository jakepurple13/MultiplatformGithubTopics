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
        args += listOf(
            "--add-opens java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
        )

        jvmArgs += listOf(
            "--add-opens java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
        )
        mainClass = "StartKt"
        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "GitHub Topics"
            packageVersion = "1.0.0"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            fun iconFile(extension: String) = project.file("src/jvmMain/resources/github_logo.$extension")
            macOS {
                iconFile.set(iconFile("icns"))
            }
            windows {
                iconFile.set(iconFile("ico"))
                dirChooser = true
                console = true
            }
            linux {
                iconFile.set(iconFile("png"))
            }
        }
    }
}

tasks.register("BuildAboutLibraries") {
    doFirst {
        exec {
            workingDir(projectDir.parentFile)
            commandLine(
                "./gradlew",
                "desktop:exportLibraryDefinitions",
                "-PaboutLibraries.exportPath=src/jvmMain/resources/"
            )
        }
    }
}

/*tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    dependsOn("BuildAboutLibraries")
}*/