package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._TestObjects
import io.kotlintest.specs.StringSpec

internal class FieldResolverGeneratorTest : StringSpec({
    "should generate file spec correctly" {
        val fieldResolverGenerator = FieldResolverGenerator(
            _TestObjects.mockOptions,
            _TestObjects.kotlinTypeMapper,
            _TestObjects.implementerMapper, _TestObjects.nameMapper,
            _TestObjects.argumentCodeBlockGenerator
        )

        val schema = _TestObjects.schema

        val spec =
            fieldResolverGenerator.getFieldResolver(schema.queryType, schema.queryType.getFieldDefinition("getUser"))

        println(spec.toString())
    }
})
