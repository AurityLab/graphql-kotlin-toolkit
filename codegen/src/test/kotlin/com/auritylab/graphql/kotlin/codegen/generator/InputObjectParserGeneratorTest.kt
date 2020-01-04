package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.codegen._TestObjects
import graphql.schema.GraphQLInputObjectType
import io.kotlintest.specs.StringSpec

internal class InputObjectParserGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = InputObjectParserGenerator(CodegenOptions(), _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val allInputObjectTypes = schema.allTypesAsList.filterIsInstance<GraphQLInputObjectType>()

        val spec = generator.getInputObjectParsers(allInputObjectTypes)

        println(spec.toString())
    }
})
