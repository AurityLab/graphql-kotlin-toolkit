# Advanced configuration (Gradle Plugin)
There are some advanced configuration properties for the Gradle Plugin.

| Property | Type | Default | Description |
|----------|------|----------|------------|
| `schemas` | File collection | **(required)** | Defines the schemas for the code generation. |
| `outputDirectory` | Directory | *"generated/graphql/kotlin/main/"* | Defines the output directory for the generated code. |
| `generatedGlobalPrefix` | String? | *null* | Defines the global prefix for all generated types. |
| `generatedBasePackage` | String | *"graphql.kotlin.toolkit.codegen"* | Defines the base package for the generated code.
| `generateAll` | Boolean | *true* | Defines if the code generator shall generate code for all types. ([**See here for more information**](schema-configuration.md))
| `enableSpringBootIntegration` | Boolean | *false* | Defines if the code generator shall generate code which can simplify usage with the Spring Boot Integration ([**See here for more information**](code-generation-with-spring-boot-integration.md)) 
