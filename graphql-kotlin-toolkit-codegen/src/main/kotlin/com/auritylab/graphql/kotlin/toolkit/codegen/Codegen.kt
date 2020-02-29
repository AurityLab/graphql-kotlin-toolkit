package com.auritylab.graphql.kotlin.toolkit.codegen

import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.GeneratorFactory
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the base class for the code generation.
 */
class Codegen(
    private val options: CodegenOptions
) {
    private val schema = CodegenSchemaParser(options).parseSchemas(options.schemas)
    private val nameMapper = GeneratedMapper(options)
    private val kotlinTypeMapper = KotlinTypeMapper(options, nameMapper)
    private val implementerMapper = ImplementerMapper(options, schema)
    private val outputDirectory = getOutputDirectory()
    private val argumentCodeBlockGenerator = ArgumentCodeBlockGenerator(kotlinTypeMapper, nameMapper)
    private val generatorFactory =
        GeneratorFactory(options, kotlinTypeMapper, nameMapper, argumentCodeBlockGenerator, implementerMapper)

    /**
     * Will generate code for the types of the [schema].
     */
    fun generate() {
        // Validate the directives.
        DirectiveFacade.validateAllOnSchema(schema)

        // Build the generators using the CodegenController.
        CodegenController(options, schema.allTypesAsList, generatorFactory)
            .buildGenerators()
            .forEach {
                it.generate()
                    .writeTo(outputDirectory)
            }
    }

    /**
     * Will return the output directory [Path] from the [options].
     * This method will also ensure that the directories exist.
     */
    private fun getOutputDirectory(): Path {
        val directory = options.outputDirectory

        // Ensure the existence of the base output directory.
        Files.createDirectories(directory)

        return directory
    }
}
