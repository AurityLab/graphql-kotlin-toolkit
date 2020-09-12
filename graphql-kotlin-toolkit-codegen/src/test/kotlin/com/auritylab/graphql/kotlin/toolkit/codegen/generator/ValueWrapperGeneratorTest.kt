package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.asTypeVariableName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties

internal class ValueWrapperGeneratorTest : AbstractCompilationTest() {
    @Test
    fun shouldGenerateCompilableCode() {
        val generator =
            ValueWrapperGenerator(TestObject.options, TestObject.kotlinTypeMapper, TestObject.generatedMapper)

        // Compile the generated code.
        val generatedClass = compile(generator).main

        // Assert against the member properties.
        val memberProperties = generatedClass.memberProperties
        Assertions.assertEquals(1, memberProperties.size)
        Assertions.assertEquals("value", memberProperties.first().name)

        val typeParameters = generatedClass.typeParameters
        Assertions.assertEquals(1, typeParameters.size)

        // Assert that the return type of the property is equal to the type parameter of the class.
        Assertions.assertEquals(typeParameters[0].asTypeVariableName(), memberProperties.first().returnType.asTypeName())
    }
}
