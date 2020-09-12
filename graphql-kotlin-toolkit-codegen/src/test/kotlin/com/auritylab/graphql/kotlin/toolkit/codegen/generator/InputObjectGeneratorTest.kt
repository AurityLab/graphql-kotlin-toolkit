package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import graphql.Scalars
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLNonNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class InputObjectGeneratorTest : AbstractCompilationTest() {
    val generator = InputObjectGenerator(
        testInputObject,
        TestObject.argumentCodeBlockGenerator,
        TestObject.options,
        TestObject.kotlinTypeMapper,
        TestObject.generatedMapper
    )

    lateinit var generatedClass: KClass<*>

    @BeforeAll
    fun compileCode() {
        // Compile the code of the generator
        generatedClass = compile(generator).main
    }

    @Test
    fun `should generate properties correctly`() {
        // Assert against the member properties.
        val memberProperties = generatedClass.memberProperties.toList()
        Assertions.assertEquals(2, memberProperties.size)
        Assertions.assertEquals("name", memberProperties[0].name)
        Assertions.assertEquals("number", memberProperties[1].name)
    }

    @Test
    fun `should generate build method correctly`() {
        Assertions.assertNotNull(generatedClass.companionObject)
        val companionObject = generatedClass.companionObject!!

        // Assert against the member functions.
        val memberFunctions = companionObject.memberFunctions

        // Search for the builder method
        val builderMethod = memberFunctions.firstOrNull { it.name.endsWith("BuildByMap") }
        Assertions.assertNotNull(builderMethod)

        // Cast to not null.
        builderMethod!!

        // Assert that the return type of the function is the generated type.
        Assertions.assertEquals(generatedClass.starProjectedType, builderMethod.returnType)

        val builderMethodParameters = builderMethod.parameters
        val mapBuilderMethodParameter = builderMethodParameters.firstOrNull { it.name == "map" }
        Assertions.assertNotNull(mapBuilderMethodParameter)
    }
}

val testInputObject = GraphQLInputObjectType.newInputObject()
    .name("TestInput")
    .field(
        GraphQLInputObjectField.newInputObjectField()
            .name("name")
            .type(GraphQLNonNull(Scalars.GraphQLString))
            .build()
    )
    .field(
        GraphQLInputObjectField.newInputObjectField()
            .name("number")
            .type(Scalars.GraphQLInt)
            .build()
    )
    .build()
