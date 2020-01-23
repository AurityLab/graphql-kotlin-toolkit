# Code generation with Spring Boot integration
This code generation can be configured to generate code which can simplify usage with the Spring Boot integration from this toolkit.

### Usage
To enable it add the following property to the Gradle Plugin configuration:
```kotlin
graphqlKotlinCodegen {
    // ...
    enableSpringBootIntegration.set(true)
    // ...
}
```

### Effects on the generated code
The code generator will add the [GQLResolver](../spring-boot-integration/annotations.md#GQLResolver) annotation to the generated resolvers.

To be more precise take a look at the following example:
```kotlin
@GQLResolver(CONTAINER, FIELD) // <- This annotation will be added.
abstract class GQLQueryGetUser : DataFetcher<Any> {
  abstract fun resolve(env: GQLEnv<Any>): Any

  override fun get(env: DataFetchingEnvironment): Any {
    val map = env.arguments
    return resolve(env = GQLEnv<Any>(env))
  }

  companion object Meta {
    const val CONTAINER: String = "Query"

    const val FIELD: String = "getUser"
  }
}
```
