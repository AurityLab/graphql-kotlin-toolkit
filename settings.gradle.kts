rootProject.name = "graphql-kotlin-toolkit"

// Cache build artifacts, so expensive operations do not need to be re-computed.
buildCache {
    local {
        isEnabled = !(System.getenv().containsKey("CI"))
    }
}

include(":graphql-kotlin-toolkit-codegen")
include(":graphql-kotlin-toolkit-codegen-binding")
include(":graphql-kotlin-toolkit-gradle-plugin")
include(":graphql-kotlin-toolkit-spring-boot")
include(":graphql-kotlin-toolkit-common")
include(":graphql-kotlin-toolkit-util")
