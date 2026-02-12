plugins {
    `java-library`
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.ktlint)
}

protobuf {
    protoc {
        artifact =
            libs.protobuf.protoc
                .get()
                .toString()
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                named("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    api(libs.protobuf.javalite)
}
