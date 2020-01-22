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

### Configuration
To make your schema work perfectly with this code generator you need to add some additional information to the schema using the previously given directives.

#### Bind object type to existing class (`@kRepresentation`)
Given the following schema:
```graphql
type User {
    ...
}
```
If you don't supply additional information on this type the code generator will represent this type with `Any` as it doesn't know what else to do with it.

Using the `@kRepresentation` directive you can supply the class to use.
```graphql
type User @kRepresentation(class: "com.auritylab.graphql.entity.User") {
    ...
}
```

#### Avoid generating all resolvers (`@kResolver`)
When building a large GraphQL API there can be a lot of resolvers. By default the code generator will generate code for every resolver.
This behavior can be adjusted using the `generateAll` option (See [usage](#usage)). When setting it to `false` the code generator will only generate code if the `@kResolver` directive has been added in the schema.
This is also useful as the `graphql-java` library provides a property resolver, which mostly takes most of the work.

The directive can be used on
- **Field definitions**
- **Objects** *(to generate resolvers for all field definitions in the object)*

Examples:
```graphql
# Will generate resolvers for "getUser" and "getUsers".
type Query @kResolver {
    getUser: User!
    getUsers: [User]!
}

# Will generate a resolver just for "age".
type User {
    id: ID!
    name: String
    age: Int @kResolver
}
```

#### Avoid generating all object types (`@kGenerate`)
In GraphQL you mostly use object types to represent your data. By default the code generator will generate a `data class` for each object type if no `@kRepresentation` is given.

Given the following object type:
```graphql
type User {
    ID: ID!
    name: String
    surname: String
}
```

The following `data class` would be generated:
```kotlin
data class User(
    val ID: String,
    val name: String?,
    val surname: String?
)
```

## Spring Boot integration
This integration works in a more opinionated way as it provides additional annotations which can be used to register code for various GraphQL types.
It also comes with a servlet, which handles all GraphQL requests.

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
