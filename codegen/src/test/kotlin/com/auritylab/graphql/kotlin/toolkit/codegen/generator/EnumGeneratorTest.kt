package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.toolkit.codegen._TestObjects
import graphql.schema.GraphQLEnumType
import io.kotlintest.specs.StringSpec

internal class EnumGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val generator = EnumGenerator(_TestObjects.mockOptions, _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val spec = generator.getEnum(schema.getType("UserType") as GraphQLEnumType)

        println(spec.toString())
    }
})
