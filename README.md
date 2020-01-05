# GraphQL Kotlin Toolkit
![GitHub Actions](https://github.com/AurityLab/graphql-kotlin-toolkit/workflows/Gradle/badge.svg)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
![Maven Central](https://img.shields.io/maven-central/v/com.auritylab.graphql-kotlin-toolkit/codegen?label=codegen)

A toolkit for GraphQL specifically with [Kotlin](https://kotlinlang.org/). This toolkit provides some useful tools which are compatible with [graphql-java](https://github.com/graphql-java/graphql-java).

Currently available tools:
* [Code generation](#code-generation)
* More coming soon!

## Usage
This toolkit provides a Gradle plugin to simply the usage.

### Code generation plugin
```kotlin
plugins {
  // Apply the plugin with the latest version.
    id("com.auritylab.graphql-kotlin-toolkit.codegen") version "0.1.0"
}

// Configure the code generation.
graphqlKotlinCodegen {
    // Define your schemas.
    schemas.from(fileTree("src/main/resources/graphql").matching { include("*.graphqls") })

    // (Optional) Set a prefix for all generated classes.
    generatedGlobalPrefix.set("GQL")

    // (Optional) Set a specific package for all generated classes.
    generatedBasePackage.set("com.auritylab.tutors.graphql._generated")
}
```

## Code generation
The code generation of this toolkit follows a different approach than some other available code generators for GraphQL.
For every resolver (also called DataFetcher in graphql-java) there will be a new `abstract class` which can be extended to implement the resolver.
Each generated resolver contains specific code to access the arguments in a more type safe way (including input and enum types).

