import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class PrintLintReportsTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFiles: ConfigurableFileCollection

    @get:Internal
    abstract val workspaceRoot: DirectoryProperty

    @TaskAction
    fun printReports() {
        val rootDirFile = workspaceRoot.orNull?.asFile
        var printedAny = false

        reportFiles.files
            .sortedBy { report -> report.absolutePath }
            .forEach { report ->
                if (!report.isFile) {
                    return@forEach
                }

                val content = report.readText().trim()
                if (content.isEmpty() || content == "No issues found.") {
                    return@forEach
                }

                printedAny = true
                val label = rootDirFile?.let { root -> report.relativeTo(root).path } ?: report.path
                println("\n===== $label =====")
                println(content)
            }

        if (!printedAny) {
            println("===== printLintReports =====")
            println("no issues found")
        }
    }
}

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
    tasks.register<PrintLintReportsTask>("printLintReports") {
        group = "verification"
        description = "Prints lint text reports from all modules."
        workspaceRoot.set(rootProject.layout.projectDirectory)
        reportFiles.from(
            rootProject.allprojects.map { project -> project.layout.buildDirectory.file("reports/lint-results-debug.txt") },
        )
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
