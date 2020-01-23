# Schema configuration
To make your schema work perfectly with this code generator you need to add some additional information to the schema using the following directives.
By default the code generator **will generate code for all available types**, to disable this behavior see [here](#disable-generating-code-for-all-types).

#### Directives
```graphql
# Defines the representation of a object or scalar in the kotlin code.
directive @kRepresentation(class: String!) on OBJECT | SCALAR

# Tells the code generator that a data class for the object shall be generated.
directive @kGenerate on OBJECT

# Tells the code generator that a resolver shall be generated.
directive @kResolver on FIELD_DEFINITION | OBJECT | INTERFACE                                                        
```

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

## Disable generating code for all types
As described above, it will generate code for all available types, this behavior can be controlled using the following property:
```kotlin
graphqlKotlinCodegen {
    // ...
    generateAll.set(false)
    // ...
}
```