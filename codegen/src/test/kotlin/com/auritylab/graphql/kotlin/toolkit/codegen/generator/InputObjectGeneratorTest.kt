package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._TestObjects
import graphql.schema.GraphQLInputObjectType
import io.kotlintest.specs.StringSpec

internal class InputObjectGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = InputObjectGenerator(_TestObjects.mockOptions, _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema

        val spec = generator.getInputObject(schema.getType("GetUserFilterInput") as GraphQLInputObjectType)

        println(spec.toString())
    }
})
