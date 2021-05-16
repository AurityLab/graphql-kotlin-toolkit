package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen._test.AbstractCompilationTest
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import graphql.Scalars
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObjectTypeGeneratorTest : AbstractCompilationTest() {
    val generator = ObjectTypeGenerator(
        testObjectType,
        TestObject.options,
        TestObject.kotlinTypeMapper,
        TestObject.generatedMapper,
        TestObject.supportMapper,
    )

    lateinit var generatedClass: KClass<*>

    @BeforeAll
    fun compileCode() {
        // Compile the code of the generator
        generatedClass = compile(generator).main
    }

    @Test
    fun `should generate properties correctly`() {
        val memberProperties = generatedClass.memberProperties.toList()
        Assertions.assertEquals(2, memberProperties.size)

        Assertions.assertEquals("name", memberProperties[0].name)
        Assertions.assertEquals("number", memberProperties[1].name)

        // "hasResolver" MUST NOT be present as it's annotated with kResolver.
    }
}

val testObjectType: GraphQLObjectType = GraphQLObjectType.newObject()
    .name("TestObjectType")
    .field(
        GraphQLFieldDefinition.newFieldDefinition()
            .name("name")
            .type(Scalars.GraphQLString)
    ).field(
        GraphQLFieldDefinition.newFieldDefinition()
            .name("number")
            .type(Scalars.GraphQLInt)
    )
    .field(
        GraphQLFieldDefinition.newFieldDefinition()
            .name("hasResolver")
            .type(Scalars.GraphQLInt)
            .withDirective(GraphQLDirective.newDirective().name("kResolver"))
    )
    .build()
