# GraphQL Kotlin Toolkit
![GitHub Actions](https://github.com/AurityLab/graphql-kotlin-toolkit/workflows/Gradle/badge.svg)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
![Maven Central](https://img.shields.io/maven-central/v/com.auritylab.graphql-kotlin-toolkit/codegen?label=codegen)

A toolkit for GraphQL specifically with [Kotlin](https://kotlinlang.org/). This toolkit provides some useful tools which are compatible with [graphql-java](https://github.com/graphql-java/graphql-java).

Currently available tools:
* [Code generation](#code-generation)
* Spring Boot integration

## Code generation
The code generation of this toolkit follows the **schema-first** approach, which means you first write your _schema.graphqls_ files and implement the server-side code for it. 
This code generator especially creates a `abstract class` for each resolver which can be extended to implement the resolver in a clean way.
The generated code provides specific parameters for each argument which allows a more type safe way to access the incoming data.

### Usage
The code generation can easily be used with the provided Gradle plugin.
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

To provide additional information to the generator it's recommended to add the following directives to the schema:
```graphql
directive @kotlinRepresentation(class: String!) on OBJECT | SCALAR
directive @kotlinGenerate on FIELD_DEFINITION | OBJECT | INTERFACE                                                                  
```
