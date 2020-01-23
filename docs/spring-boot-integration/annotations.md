# Annotations
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
