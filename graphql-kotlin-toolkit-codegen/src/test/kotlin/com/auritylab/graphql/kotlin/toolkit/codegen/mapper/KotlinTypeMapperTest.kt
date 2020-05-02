package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.COLLECTION
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import graphql.Scalars
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLUnionType
import java.math.BigDecimal
import java.math.BigInteger
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class KotlinTypeMapperTest {
    @Nested
    @DisplayName("getKotlinType()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetKotlinType_Scalars {
        val kotlinTypeMapper = KotlinTypeMapper(TestObject.options.copy(generateAll = true), TestObject.generatedMapper)

        @ParameterizedTest
        @MethodSource(provideScalarTestTypesPointer)
        fun `should generate types for scalars correctly`(type: GraphQLType, expected: TypeName) {
            // Assert against each given type and expect the type to be the given expected type name.
            Assertions.assertEquals(expected, kotlinTypeMapper.getKotlinType(type))
        }

        @Test
        fun `should generate input object type correctly`() {
            val testInputObject = GraphQLInputObjectType.newInputObject()
                .name("TestInput")
                .build()

            // Generate the type name reference.
            val result = kotlinTypeMapper.getKotlinType(testInputObject)
            Assertions.assertNotNull(result)

            // Cast to a ClassName to access additional properties.
            result as ClassName

            Assertions.assertEquals("TestInput", result.simpleName)
            Assertions.assertTrue(result.isNullable)
        }

        @Test
        fun `should generate enum type correctly`() {
            val testEnumType = GraphQLEnumType.newEnum()
                .name("TestEnum")
                .value("FIRST")
                .value("SECOND")
                .value("THIRD")
                .build()

            // Generate the type name reference.
            val result = kotlinTypeMapper.getKotlinType(testEnumType)
            Assertions.assertNotNull(result)

            // Cast to a ClassName to access additional properties.
            result as ClassName

            Assertions.assertEquals("TestEnum", result.simpleName)
            Assertions.assertTrue(result.isNullable)
        }

        @Test
        fun `should generate object type with representation correctly`() {
            val testObjectType = GraphQLObjectType.newObject()
                .name("TestObjectType")
                .withDirective(
                    GraphQLDirective.newDirective()
                        .name("kRepresentation")
                        .argument(
                            GraphQLArgument.newArgument()
                                .name("class")
                                .type(Scalars.GraphQLString)
                                .value("kotlin.String")
                        )
                )
                .build()

            val result = kotlinTypeMapper.getKotlinType(testObjectType)
            Assertions.assertNotNull(result)
            Assertions.assertEquals(STRING.copy(true), result)
        }

        @Test
        fun `should generate object type with generated correctly`() {
            val testObjectType = GraphQLObjectType.newObject()
                .name("TestObjectType")
                .build()

            val result = kotlinTypeMapper.getKotlinType(testObjectType)
            Assertions.assertNotNull(result)

            result as ClassName

            Assertions.assertEquals("TestObjectType", result.simpleName)
            Assertions.assertTrue(result.isNullable)
        }

        @Test
        fun `should generate interface type with representation correctly`() {
            val testInterfaceType = GraphQLInterfaceType.newInterface()
                .name("TestInterfaceType")
                .withDirective(
                    GraphQLDirective.newDirective()
                        .name("kRepresentation")
                        .argument(
                            GraphQLArgument.newArgument()
                                .name("class")
                                .type(Scalars.GraphQLString)
                                .value("kotlin.String")
                        )
                )
                .build()

            val result = kotlinTypeMapper.getKotlinType(testInterfaceType)
            Assertions.assertNotNull(result)
            Assertions.assertEquals(STRING.copy(true), result)
        }

        @Test
        fun `should generate interface type with generated correctly`() {
            val testInterfaceType = GraphQLInterfaceType.newInterface()
                .name("TestInterfaceType")
                .withDirective(getRepresentationDirective())
                .build()

            val result = kotlinTypeMapper.getKotlinType(testInterfaceType)
            Assertions.assertNotNull(result)

            Assertions.assertEquals(STRING.copy(nullable = true), result)
            Assertions.assertTrue(result.isNullable)
        }

        @Test
        fun `should generate union type with same types correctly`() {
            val representationDirective = getRepresentationDirective()
            val firstPossibleType = getDummyObjectType("FirstObjectType", representationDirective)
            val secondPossibleType = getDummyObjectType("SecondObjectType", representationDirective)
            val thirdPossibleType = getDummyObjectType("ThirdObjectType", representationDirective)

            val testUnionType = GraphQLUnionType.newUnionType()
                .name("TestUnion")
                .possibleTypes(firstPossibleType, secondPossibleType, thirdPossibleType)
                .build()

            val result = kotlinTypeMapper.getKotlinType(testUnionType)
            Assertions.assertNotNull(result)
            Assertions.assertEquals(STRING.copy(nullable = true), result)
        }

        @Test
        fun `should generate union type with no representation correctly`() {
            val firstPossibleType = getDummyObjectType("FirstObjectType")
            val secondPossibleType = getDummyObjectType("SecondObjectType")
            val thirdPossibleType = getDummyObjectType("ThirdObjectType")

            val testUnionType = GraphQLUnionType.newUnionType()
                .name("TestUnion")
                .possibleTypes(firstPossibleType, secondPossibleType, thirdPossibleType)
                .build()

            val result = kotlinTypeMapper.getKotlinType(testUnionType)
            Assertions.assertNotNull(result)
            Assertions.assertEquals(ANY.copy(nullable = true), result)
        }

        @Test
        fun `should generate union type with different representations correctly`() {
            val firstPossibleType = getDummyObjectType("FirstObjectType")
            val secondPossibleType = getDummyObjectType("SecondObjectType", getRepresentationDirective("kotlin.String"))
            val thirdPossibleType = getDummyObjectType("ThirdObjectType")

            val testUnionType = GraphQLUnionType.newUnionType()
                .name("TestUnion")
                .possibleTypes(firstPossibleType, secondPossibleType, thirdPossibleType)
                .build()

            val result = kotlinTypeMapper.getKotlinType(testUnionType)
            Assertions.assertNotNull(result)
            Assertions.assertEquals(ANY.copy(nullable = true), result)
        }

        @Test
        fun `should generate doubleNull type correctly`() {
            val type = Scalars.GraphQLString

            val field = GraphQLFieldDefinition.newFieldDefinition()
                .name("simple")
                .type(type)
                .withDirective(getDoubleNullDirective())
                .build()

            val result = kotlinTypeMapper.getKotlinType(type, field)
            Assertions.assertNotNull(result)

            val rawType = (result as ParameterizedTypeName).rawType

            Assertions.assertEquals("V", rawType.simpleName)
            Assertions.assertTrue(result.isNullable)

            Assertions.assertEquals(1, result.typeArguments.size)
            Assertions.assertEquals(STRING.copy(nullable = true), result.typeArguments[0])
        }

        @Test
        fun `should generate standard list type correctly`() {
            val innerType = Scalars.GraphQLString
            val type = GraphQLList(innerType)

            val result = kotlinTypeMapper.getKotlinType(type)
            Assertions.assertNotNull(result)

            val rawType = (result as ParameterizedTypeName).rawType

            Assertions.assertEquals(COLLECTION, rawType)
            Assertions.assertTrue(result.isNullable)

            Assertions.assertEquals(1, result.typeArguments.size)
            Assertions.assertEquals(STRING.copy(true), result.typeArguments[0])
        }

        @Test
        fun `should generate custom list type correctly`() {
            val innerType = Scalars.GraphQLString
            val type = GraphQLList(innerType)

            val result = kotlinTypeMapper.getKotlinType(type, listType = LIST)
            Assertions.assertNotNull(result)

            val rawType = (result as ParameterizedTypeName).rawType

            Assertions.assertEquals(LIST, rawType)
            Assertions.assertTrue(result.isNullable)

            Assertions.assertEquals(1, result.typeArguments.size)
            Assertions.assertEquals(STRING.copy(true), result.typeArguments[0])
        }
    }

    private fun getDoubleNullDirective(): GraphQLDirective =
        GraphQLDirective.newDirective()
            .name("kDoubleNull")
            .build()

    private fun getGenerateDirective(): GraphQLDirective =
        GraphQLDirective.newDirective()
            .name("kGenerate")
            .build()

    /**
     * Will build a 'kRepresentation' directive with the given [clazz] as value for the class argument.
     */
    private fun getRepresentationDirective(clazz: String = "kotlin.String"): GraphQLDirective =
        GraphQLDirective.newDirective()
            .name("kRepresentation")
            .argument(
                GraphQLArgument.newArgument()
                    .name("class")
                    .type(Scalars.GraphQLString)
                    .value(clazz)
            )
            .build()

    /**
     * Will create a new dummy ObjectType with the givne [name] and the given [directives].
     */
    private fun getDummyObjectType(
        name: String = "TestObjectType",
        vararg directives: GraphQLDirective
    ): GraphQLObjectType =
        GraphQLObjectType.newObject()
            .name(name)
            .withDirectives(*directives)
            .build()

    companion object {
        const val provideScalarTestTypesPointer =
            "com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapperTest#provideScalarTestTypes"

        @JvmStatic
        fun provideScalarTestTypes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Scalars.GraphQLInt, INT.copy(true)),
                Arguments.of(Scalars.GraphQLFloat, DOUBLE.copy(true)),
                Arguments.of(Scalars.GraphQLString, STRING.copy(true)),
                Arguments.of(Scalars.GraphQLBoolean, BOOLEAN.copy(true)),
                Arguments.of(Scalars.GraphQLID, STRING.copy(true)),
                Arguments.of(Scalars.GraphQLLong, LONG.copy(true)),
                Arguments.of(Scalars.GraphQLShort, SHORT.copy(true)),
                Arguments.of(Scalars.GraphQLByte, BYTE.copy(true)),
                Arguments.of(Scalars.GraphQLBigInteger, BigInteger::class.asTypeName().copy(true)),
                Arguments.of(Scalars.GraphQLBigDecimal, BigDecimal::class.asTypeName().copy(true)),
                Arguments.of(Scalars.GraphQLChar, CHAR.copy(true))
            )
        }
    }
}
