import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.protobuf) apply false
}

val unifiedCompileSdkMajor = 36
val unifiedCompileSdkMinor = 1

val printLintReports =
    tasks.register("printLintReports") {
        group = "verification"
        description = "Prints lint text reports from all modules."
        doLast {
            val reports =
                rootProject.allprojects
                    .map { project -> project.file("build/reports/lint-results-debug.txt") }
                    .sortedBy { report -> report.absolutePath }

            var printedAny = false
            reports.forEach { report ->
                if (!report.isFile || report.length() == 0L) {
                    return@forEach
                }
                printedAny = true
                println("\n===== ${report.relativeTo(rootProject.projectDir)} =====")
                println(report.readText())
            }

            if (!printedAny) {
                println("===== printLintReports =====")
                println("no issues found")
            }
        }
    }

subprojects {
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> {
            compileSdk {
                version =
                    release(unifiedCompileSdkMajor) {
                        minorApiLevel = unifiedCompileSdkMinor
                    }
            }
            lint {
                textReport = true
                htmlReport = true
            }
        }
    }

    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk {
                version =
                    release(unifiedCompileSdkMajor) {
                        minorApiLevel = unifiedCompileSdkMinor
                    }
            }
            lint {
                textReport = true
                htmlReport = true
            }
        }
    }

}

gradle.projectsEvaluated {
    allprojects.forEach { project ->
        project.tasks.matching { task -> task.name == "lint" }.configureEach {
            finalizedBy(printLintReports)
        }
    }
}
