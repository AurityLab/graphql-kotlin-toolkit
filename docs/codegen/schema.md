# Configuration
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
