# Getting started
The Spring Boot starter configures most parts by it self. You just have to configure your GraphQL schemas.

## Dependency
#### Gradle
```kotlin
dependencies {
    implementation("com.auritylab.graphql-kotlin-toolkit:spring-boot-starter:0.3.0")
}
```

#### Maven
```xml
<dependency>
    <groupId>com.auritylab.graphql-kotlin-toolkit</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Schemas
After adding the dependencies you need to tell the integration where to search for your schemas.
All you have to do is to add a Bean of type [`GQLSchemaSupplier`](../../graphql-kotlin-toolkit-spring/src/main/kotlin/com/auritylab/graphql/kotlin/toolkit/spring/configuration/GQLSchemaConfiguration.kt).


Using schema files which are located in your resources folder:
```kotlin
@Configuration
class GraphQLConfiguration {
    @Bean
    fun schemaSupplier() = GQLSchemaSupplier.ofResourceFiles(
        "graphql/schema.graphqls"
        // Other schema files
    )
}
```
