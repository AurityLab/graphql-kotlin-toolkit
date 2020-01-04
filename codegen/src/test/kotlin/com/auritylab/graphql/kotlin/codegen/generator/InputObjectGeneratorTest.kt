package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.PoetOptions
import com.auritylab.graphql.kotlin.codegen._TestObjects
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
