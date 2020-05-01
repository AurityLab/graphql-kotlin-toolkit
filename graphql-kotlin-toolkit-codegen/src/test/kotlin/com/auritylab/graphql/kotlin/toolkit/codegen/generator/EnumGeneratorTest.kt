package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import graphql.schema.GraphQLEnumType
import kotlin.reflect.full.isSubclassOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class EnumGeneratorTest : AbstractCompilationTest() {
    @Test
    fun shouldGenerateCompilableCode() {
        val generator =
            EnumGenerator(testEnum, TestObject.options, TestObject.kotlinTypeMapper, TestObject.generatedMapper)

        // Compile the generator output.
        val generatedClass = compile(generator)

        // Assert that the generated class is of type enum.
        Assertions.assertTrue(generatedClass.isSubclassOf(Enum::class))

        val enumConstants = generatedClass.java.enumConstants as Array<Enum<*>>

        // Assert the size of the enum constants.
        Assertions.assertEquals(4, enumConstants.size)

        // Assert against the defined values.
        testEnum.values.forEachIndexed { i, definition ->
            Assertions.assertEquals(definition.name, enumConstants[i].name)
        }
    }
}

private val testEnum = GraphQLEnumType.newEnum()
    .name("ETestEnum")
    .value("first")
    .value("second")
    .value("third")
    .value("fourth")
    .build()
