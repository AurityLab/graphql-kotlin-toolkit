# Schema configuration
To make your schema work perfectly with this code generator you need to add some additional information to the schema using the following directives.
By default, the code generator **will generate code for all available types**, to disable this behavior see [here](#disable-generating-code-for-everything).

#### Directives
```graphql
# Defines the representation of a object or scalar in the kotlin code.
directive @kRepresentation(class: String!) on OBJECT | SCALAR | INTERFACE | ENUM

# Tells the code generator that a data class for the object shall be generated.
directive @kGenerate on OBJECT

# Tells the code generator that a resolver shall be generated.
directive @kResolver on FIELD_DEFINITION | OBJECT | INTERFACE   

# Tells the code generator to wrap the value into another nullable wrapper.
directive @kDoubleNull on INPUT_FIELD_DEFINITION | ARGUMENT_DEFINITION

# Tells the code generator that the field definition should support pagination.
directive @kPagination on FIELD_DEFINITION                                                     
```

### Bind object type to existing class (`@kRepresentation`)
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

#### Enums
When binding existing enums using `@kRepresentation`, the generated code slightly differs to a normal object.
The generated enum provides converter functions to convert from the generated enum to the representation enum and vice versa. 

Example:
```kotlin
/// Will convert the DEFAULT enum constant to the matching representation enum constant.
GQLEUserType.DEFAULT.presentation 
// - or -
GQLEUserType.DEFAULT()


// Will convert the DEFAULT representation enum constant to the matching enum constant of the generated enum.
GQLEUserType.of(EUserType.DEFAULT)
// - or -
GQLEUserType(EUserType.DEFAULT)

```

### Avoid generating all resolvers (`@kResolver`)
When building a large GraphQL API there can be a lot of resolvers. By default, the code generator will generate code for every resolver.
This behavior can be adjusted using the `generateAll` option (See [usage](#disable-generating-code-for-everything)). When setting it to `false` the code generator will only generate code if the `@kResolver` directive has been added in the schema.
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

### Avoid generating all object types (`@kGenerate`)
In GraphQL you mostly use object types to represent your data. By default, the code generator will generate a `data class` for each object type if no `@kRepresentation` is given.

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

### Double nullability (`@kDoubleNull`)
Sometimes there are cases where you want to update certain data, but the attribute you want to update is nullable.

Given the following mutation `updateUser(name: String, surname: String)` which allows you to partially update your User.
The attribute `surname` can be null within your database, which means the mutation can be called with following data: 
`{"name": "test", "surname": null}`. 
When explicitly setting the `surname` to `null` you simply tell your resolver, that you want to set the `surname` to `null`.

Now to the case where `@kDoubleNull` comes into play: You may never want to update the `surname` attribute, therefore you may 
call the mutation with the following data: `{"name": "test"}`. When not using the `@kDoubleNull` directive you get `String?`
as type for the `surname` attribute, which makes it unable to differ if the user want's to explicitly set the value to `null` or simply not update it. 

When using `@kDoubleNull` on your mutation parameter (`updateUser(name: String, surname: String @kDoubleNull)`) you get `V<String?>?` as type for the `surname` attribute.
As the type is now double nullable you can differ if the value shall be set to null, or the value should not be updated at all.
 
 
### Pagination (`@kPagination`)
This code generator also supports the [GraphQL Cursor Connections Specification](https://facebook.github.io/relay/graphql/connections.htm) which allows you to easily implement pagination on your queries.

#### With [Spring Boot Integration](/docs/spring-boot-integration/getting-started.md)
Simply add the `@kPagination` directive to your field definition, and the code generator and the Spring Boot Integration will do the rest for you:
```graphql
type Query {
    # ...
    getUsers: [User] @kPagination
    # ...
}
```

#### Standalone
When using the code generator without the Spring Boot Integration you have to define the `*Connection`, `*Edge` and `PageInfo` types by yourself.

This may look like the following example:
```graphql
type Query {
    # ...
    getUsers(first: Int, last: Int, after: String, before: String): UserConnection @kPagination
    # ...
}

type UserConnection {
    edges: [UserEdge]
    pageInfo: PageInfo!
}

type UserEdge {
    node: User
    cursor: String!
}

type PageInfo {
    hasNextPage: Boolean!
    hasPreviousPage: Boolean!
    startCursor: String!
    endCursor: String!
}
```

As seen in the example you have to define the `UserConnection`, `UserEdge` and the `PageInfo` (you only have to define this type once!).
By adding the `@kPagination` directive you still get the easy to use field resolver generated by the code generator.


## Disable generating code for everything
As described above, it will generate code for all available types, this behavior can be controlled using the following property:
```kotlin
graphqlKotlinCodegen {
    // ...
    generateAll.set(false)
    // ...
}
```
