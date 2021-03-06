# GraphQL Kotlin Toolkit
[![GitHub Actions](https://github.com/AurityLab/graphql-kotlin-toolkit/workflows/Gradle/badge.svg)](https://github.com/AurityLab/graphql-kotlin-toolkit/actions)
[![codecov](https://codecov.io/gh/AurityLab/graphql-kotlin-toolkit/branch/master/graph/badge.svg?token=e8c5dSYCAS)](https://codecov.io/gh/AurityLab/graphql-kotlin-toolkit)
[![Maven Central](https://img.shields.io/maven-central/v/com.auritylab.graphql-kotlin-toolkit/codegen?label=codegen)](https://mvnrepository.com/artifact/com.auritylab.graphql-kotlin-toolkit/codegen)
[![Maven Central](https://img.shields.io/maven-central/v/com.auritylab.graphql-kotlin-toolkit/spring-boot?label=spring%20boot%20integration)](https://mvnrepository.com/artifact/com.auritylab.graphql-kotlin-toolkit/spring-boot)

A toolkit for GraphQL, specifically for [Kotlin](https://kotlinlang.org/). This toolkit provides some useful tools that are compatible with [graphql-java](https://github.com/graphql-java/graphql-java).

## Code generation
This tool follows the **schema-first** approach, in which you first write your *schema.graphqls* files and implement the server-side code for it afterwards.
This code generator additionally creates an interface for each resolver. 
These can be used to implement each resolver in a clean way. The tool also provides specific parameters for each argument, allowing a more type safe way to access the incoming data.
This code generator also **supports Kotlin's null safety feature**!

Example resolver:
```kotlin
class MutationUpdateUser : GQLMutationUpdateUser {
    override fun resolve(input: GQLUpdateUserInput, env: GQLMutationUpdateUser.Env): User {
        TODO("implement your resolver")
    }
}
```

**Getting started [here](docs/codegen/gettings-started.md)!**


## Spring Boot integration
This integration works in a more opinionated way as it provides additional annotations which can be used to register code for various GraphQL types.
It also comes with a servlet, which handles all GraphQL requests.

**Getting started [here](docs/spring-boot-integration/getting-started.md)!**


## Documentation
* Code generation
    * [Getting started (Gradle Plugin)](docs/codegen/gettings-started.md)
    * [Schema configuration](docs/codegen/schema-configuration.md)
    * [Advanced configuration (Gradle Plugin)](docs/codegen/advanced-configuration.md)
    * [Code generation with Spring Boot integration](docs/codegen/code-generation-with-spring-boot-integration.md)
* Spring Boot integration
    * [Getting started](docs/spring-boot-integration/getting-started.md)
    * [Annotations](docs/spring-boot-integration/annotations.md)
