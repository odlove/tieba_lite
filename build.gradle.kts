import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.ktlint) apply false
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
                    .filter { report -> report.isFile && report.length() > 0L }
                    .sortedBy { report -> report.absolutePath }
            if (reports.isEmpty()) {
                println("No lint text reports found.")
                return@doLast
            }
            reports.forEach { report ->
                println("\n===== ${report.relativeTo(rootProject.projectDir)} =====")
                println(report.readText())
            }
        }
    }

val printKtlintReports =
    tasks.register("printKtlintReports") {
        group = "verification"
        description = "Prints ktlint text reports from all modules."
        doLast {
            val reports =
                rootProject.allprojects
                    .flatMap { project ->
                        project
                            .fileTree(project.file("build/reports/ktlint")) {
                                include("**/*Check.txt")
                            }.files
                    }.filter { report -> report.isFile }
                    .sortedBy { report -> report.absolutePath }
            if (reports.isEmpty()) {
                println("No ktlint text reports found.")
                return@doLast
            }
            val nonEmptyReports = reports.filter { report -> report.length() > 0L }
            if (nonEmptyReports.isEmpty()) {
                println("No ktlint issues found.")
                return@doLast
            }
            nonEmptyReports.forEach { report ->
                println("\n===== ${report.relativeTo(rootProject.projectDir)} =====")
                println(report.readText())
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

    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<KtlintExtension> {
            outputToConsole.set(true)
            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.HTML)
            }
        }
    }
}

gradle.projectsEvaluated {
    allprojects.forEach { project ->
        project.tasks.matching { task -> task.name == "lint" }.configureEach {
            finalizedBy(printLintReports)
        }
        project.tasks.matching { task -> task.name == "ktlintCheck" }.configureEach {
            finalizedBy(printKtlintReports)
        }
    }
}
