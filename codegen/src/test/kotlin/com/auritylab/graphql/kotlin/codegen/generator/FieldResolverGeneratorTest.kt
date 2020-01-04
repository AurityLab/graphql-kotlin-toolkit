package com.auritylab.graphql.kotlin.codegen.generator

import com.auritylab.graphql.kotlin.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.codegen._TestObjects
import io.kotlintest.specs.StringSpec

internal class FieldResolverGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val fieldResolverGenerator = FieldResolverGenerator(_TestObjects.mockOptions, _TestObjects.kotlinTypeMapper, _TestObjects.nameMapper)

        val schema = _TestObjects.schema


        val spec = fieldResolverGenerator.getFieldResolver(schema.queryType, schema.queryType.getFieldDefinition("getUser"))

        println(spec.toString())
    }
})
