package com.auritylab.graphql.kotlin.toolkit.codegen

import java.nio.file.Path

/**
 * Describes configurable options for the code generator.
 */
data class CodegenOptions(
    /**
     * Describes a [Collection] of [Path]s which point to schemas.
     * This property is necessary!
     */
    val schemas: Collection<Path>,

    /**
     * Describes a [Path] which acts as output directory for the generated code.
     */
    val outputDirectory: Path,

    /**
     * Describes a global prefix for all generated files and classes/enums/etc.
     * Defaults to `null` (no prefix).
     */
    var generatedGlobalPrefix: String? = null,

    /**
     * Describes the name of the packages which contains all generated code.
     */
    var generatedBasePackage: String = "graphql.kotlin.toolkit.codegen",

    /**
     * Describes if the code shall be generated for all found types, enums, etc.
     * If this is [true] it will ignore the generate directive.
     */
    var generateAll: Boolean = true,

    /**
     * Describes if the generated code should contain additional code
     * which can be used to simplify usage with the spring boot integration.
     */
    var enableSpringBootIntegration: Boolean = false
)
