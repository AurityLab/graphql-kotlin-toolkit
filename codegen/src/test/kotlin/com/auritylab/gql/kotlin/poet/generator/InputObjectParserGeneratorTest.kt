package com.auritylab.gql.kotlin.poet.generator

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet._TestObjects
import graphql.schema.GraphQLInputObjectType
import io.kotlintest.specs.StringSpec

internal class InputObjectParserGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = InputObjectParserGenerator(PoetOptions(), _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val allInputObjectTypes = schema.allTypesAsList.filterIsInstance<GraphQLInputObjectType>()

        val spec = generator.getInputObjectParsers(allInputObjectTypes)

        println(spec.toString())
    }
})
