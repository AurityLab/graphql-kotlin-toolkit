package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.Scalars
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLEnumType
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class EnumGeneratorTest : AbstractCompilationTest() {
    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should generate compilable code`() {
        val generator =
            EnumGenerator(testEnum, TestObject.options, TestObject.kotlinTypeMapper, TestObject.generatedMapper)

        // Compile the generator output.
        val generatedClass = compile(generator).main

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

    @Test
    fun `should build enum with representation correctly`() {
        val generator = EnumGenerator(
            testEnumWithRepresentation,
            TestObject.options,
            TestObject.kotlinTypeMapper,
            TestObject.generatedMapper
        )

        // Compile the generator output and add the TestEnum file as dependency.
        val compileResult = compile(generator.generate(), testEnumFileSpec)
        val generatedClass = compileResult.main
        val testEnumConstants = compileResult.classLoader.loadClass("graphql.TestEnum").enumConstants

        // Assert that the generated class is of type enum.
        Assertions.assertTrue(generatedClass.isSubclassOf(Enum::class))

        // ... Not necessary to assert against the constants -> see test above.
        val constants = generatedClass.java.enumConstants

        // Load the "representation" property reference.
        val representationProperty = generatedClass.declaredMemberProperties
            .firstOrNull { it.name == "representation" }

        // The property has to be present.
        Assertions.assertNotNull(representationProperty)
        representationProperty!!

        // The second and the first constant can be mapped correctly.
        Assertions.assertNotNull(representationProperty.call(constants[0]))
        Assertions.assertNotNull(representationProperty.call(constants[1]))

        // The 'invalid' enum value has to throw an exception.
        Assertions.assertThrows(NoSuchElementException::class.java) {
            try {
                representationProperty.call(constants[2])
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }
        }

        // Load the "of" function reference.
        val ofFunction = generatedClass.companionObject?.declaredMemberFunctions
            ?.firstOrNull { it.name == "of" }

        // The function has to be present.
        Assertions.assertNotNull(ofFunction)
        ofFunction!!

        val companionInstance = generatedClass.companionObjectInstance!!

        // The first and the second constant can be mapped correctly.
        Assertions.assertNotNull(ofFunction.call(companionInstance, testEnumConstants[0]))
        Assertions.assertNotNull(ofFunction.call(companionInstance, testEnumConstants[1]))

        // The 'internalInvalid' enum value has to throw an exception.
        Assertions.assertThrows(NoSuchElementException::class.java) {
            try {
                representationProperty.call(constants[2])
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }
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

private val testEnumWithRepresentation = GraphQLEnumType.newEnum()
    .name("ETestEnum")
    .value("first")
    .value("second")
    .value("invalid")
    .withDirective(
        GraphQLDirective.newDirective()
            .name("kRepresentation")
            .argument(
                GraphQLArgument.newArgument()
                    .type(Scalars.GraphQLString)
                    .name("class")
                    .value("graphql.TestEnum")
            )
    )
    .build()

private val testEnumFileSpec = FileSpec.builder("graphql", "TestEnum")
    .addType(
        TypeSpec.enumBuilder("TestEnum")
            .addEnumConstant("first")
            .addEnumConstant("second")
            .addEnumConstant("internalInvalid")
            .build()
    )
    .build()
