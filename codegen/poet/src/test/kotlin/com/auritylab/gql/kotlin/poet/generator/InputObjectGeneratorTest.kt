package com.auritylab.gql.kotlin.poet.generator

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet._TestObjects
import graphql.schema.GraphQLInputObjectType
import io.kotlintest.specs.StringSpec

internal class InputObjectGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = InputObjectGenerator(PoetOptions(), _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val spec = generator.getInputObject(schema.getType("GetUserFilterInput") as GraphQLInputObjectType)

        println(spec.toString())
    }
})
