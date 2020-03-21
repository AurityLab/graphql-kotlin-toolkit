# Getting started (Gradle Plugin)
The code generation can easily be used with the provided Gradle plugin.
The plugin will automatically hook into your `build` task and generate the code before it. 
It also creates the required sourceset for the generated code by itself.

### Plugin setup
```kotlin
plugins {
  // Apply the plugin with the latest version.
    id("com.auritylab.graphql-kotlin-toolkit.codegen") version "0.3.0"
}

// Configure the code generation.
graphqlKotlinCodegen {
    // Define your schemas.
    schemas.from(fileTree("src/main/resources/graphql").matching { include("*.graphqls") })
}
```

After you've configured the plugin you may [**continue with configuring your schema**](schema-configuration.md).

Further configuration options for the plugin can be found [here](advanced-configuration.md).
