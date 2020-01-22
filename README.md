# GraphQL Kotlin Toolkit
![GitHub Actions](https://github.com/AurityLab/graphql-kotlin-toolkit/workflows/Gradle/badge.svg)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
![Maven Central](https://img.shields.io/maven-central/v/com.auritylab.graphql-kotlin-toolkit/codegen?label=codegen)

A toolkit for GraphQL, specifically for [Kotlin](https://kotlinlang.org/). This toolkit provides some useful tools that are compatible with [graphql-java](https://github.com/graphql-java/graphql-java).

Currently available tools:
* [Code generation](#code-generation)
* [Spring Boot integration](#spring-boot-integration)

## Code generation
This tool follows the **schema-first** approach, in which you first write your *schema.graphqls* files and implement the server-side code for it afterwards.
This code generator additionally creates an abstract class for each resolver. 
These can be extended to implement the resolver in a clean way. The tool also provides specific parameters for each argument, allowing a more type safe way to access the incoming data.
This code generator also **supports Kotlin's null safety feature**!

### Getting started
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
    generatedBasePackage.set("com.auritylab.graphql.generated")
    
    // (Optional) If everything should be generated.
    generateAll.set(true)
}
```

To provide additional information to the generator it's recommended to add the following directives to the schema:
```graphql
directive @kRepresentation(class: String!) on OBJECT | SCALAR
directive @kGenerate on OBJECT
directive @kResolver on FIELD_DEFINITION | OBJECT | INTERFACE                                                        
```

[**-> Configure your schema to adjust the generated code for your needs.**](docs/codegen/schema.md)

## Spring Boot integration
This integration works in a more opinionated way as it provides additional annotations which can be used to register code for various GraphQL types.
It also comes with a servlet, which handles all GraphQL requests.

### Getting started
By using the starter everything you need to get started will be automatically configured.

#### Gradle
```kotlin
dependencies {
    implementation("com.auritylab.graphql-kotlin-toolkit:spring-boot-starter:0.1.0")
}
```

#### Maven
```xml
<dependency>
    <groupId>com.auritylab.graphql-kotlin-toolkit</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```


### Annotations
You can register your code through the following annotations which are provided by this integration.

####  [**GQLResolver**](https://github.com/AurityLab/graphql-kotlin-toolkit/blob/master/spring/src/main/kotlin/com/auritylab/graphql/kotlin/toolkit/spring/annotation/GQLResolver.kt)
Defines a resolver using the given container name and field name.

Example:
```kotlin
@GQLResolver("Query", "getMe")
class QueryGetUserResolver : DataFetcher<Any> {
    override fun get(env: DataFetchingEnvironment): Any = TODO("implement")
}
```

#### [**GQLTypeResolver**](https://github.com/AurityLab/graphql-kotlin-toolkit/blob/master/spring/src/main/kotlin/com/auritylab/graphql/kotlin/toolkit/spring/annotation/GQLTypeResolver.kt)
Defines a new TypeResolver for the given type name and scope.

Example:
```kotlin
@GQLTypeResolver("User", GQLTypeResolver.Scope.INTERFACE)
class UserTypeResolver : TypeResolver {
    override fun getType(env: TypeResolutionEnvironment?): GraphQLObjectType = TODO("implement")
}
```

#### [**GQLScalar**](https://github.com/AurityLab/graphql-kotlin-toolkit/blob/master/spring/src/main/kotlin/com/auritylab/graphql/kotlin/toolkit/spring/annotation/GQLScalar.kt)
Defines a new Scalar for the given scalar name.

Example:
```kotlin
@GQLScalar("NewString")
class NewStringCoercing : Coercing<String, String> {
    override fun parseValue(input: Any): String = TODO("implement")
    override fun parseLiteral(input: Any?): String = TODO("implement")
    override fun serialize(dataFetcherResult: Any): String = TODO("implement")
}
```

#### [**GQLDirective**](https://github.com/AurityLab/graphql-kotlin-toolkit/blob/master/spring/src/main/kotlin/com/auritylab/graphql/kotlin/toolkit/spring/annotation/GQLDirective.kt)
Defines a new Directive for the given directive name.

Example:
```kotlin
@GQLDirective("authentication")
class AuthenticationDirective : SchemaDirectiveWiring {
    override fun onField(env: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition = TODO("implement")
}

```

#### [**GQLResolvers**](https://github.com/AurityLab/graphql-kotlin-toolkit/blob/master/spring/src/main/kotlin/com/auritylab/graphql/kotlin/toolkit/spring/annotation/GQLResolvers.kt) 
Defines multiple resolvers for the given [GQLResolver](#GQLResolver).

Example:
```kotlin
@GQLResolvers(
    GQLResolver("PrivateUser", "age"),
    GQLResolver("PublicUser", "age")
)
class UserAgeResolver : DataFetcher<Any> {
    override fun get(env: DataFetchingEnvironment): Any = TODO("implement")
}
```
