package com.auritylab.graphql.kotlin.toolkit.common.directive

import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.UnExecutableSchemaGenerator
import org.junit.jupiter.api.Test

internal class DirectiveFacadeTest {
    @Test
    fun shouldValidateCorrectDirectivesProperly() {
        // This throws an exception if the validation fails.
        DirectiveFacade.validateAllOnSchema(getSchema("allDirectives.graphqls"))
    }

    companion object {
        private fun getSchema(name: String): GraphQLSchema {
            // Load the raw schema from the current class loader.
            val rawSchema =
                String(Thread.currentThread().contextClassLoader.getResourceAsStream(name)!!.readAllBytes())

            // Create an executable schema.
            return UnExecutableSchemaGenerator.makeUnExecutableSchema(
                TypeDefinitionRegistry().merge(
                    SchemaParser().parse(
                        rawSchema
                    )
                )
            )
        }
    }
}
