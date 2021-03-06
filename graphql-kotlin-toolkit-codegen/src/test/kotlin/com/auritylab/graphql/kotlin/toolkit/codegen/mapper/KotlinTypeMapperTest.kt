package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestObject
import com.auritylab.graphql.kotlin.toolkit.codegen._test.TestUtils
import com.auritylab.graphql.kotlin.toolkit.codegenbinding.types.Value
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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import graphql.Scalars
import graphql.schema.Coercing
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLUnionType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.math.BigInteger
import java.util.stream.Stream

internal class KotlinTypeMapperTest {
    @Nested
    @Suppress("ClassName")
    @DisplayName("getKotlinType()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetKotlinType_Scalars {
        private val kotlinTypeMapper = KotlinTypeMapper(
            TestObject.options.copy(generateAll = true),
            TestObject.generatedMapper,
            TestObject.supportMapper
        )

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
                .withDirective(TestUtils.getRepresentationDirective())
                .build()

            val result = kotlinTypeMapper.getKotlinType(testInterfaceType)
            Assertions.assertNotNull(result)

            Assertions.assertEquals(STRING.copy(nullable = true), result)
            Assertions.assertTrue(result.isNullable)
        }

        @Test
        fun `should generate union type with same types correctly`() {
            val representationDirective = TestUtils.getRepresentationDirective()
            val firstPossibleType = TestUtils.getDummyObjectType("FirstObjectType", representationDirective)
            val secondPossibleType = TestUtils.getDummyObjectType("SecondObjectType", representationDirective)
            val thirdPossibleType = TestUtils.getDummyObjectType("ThirdObjectType", representationDirective)

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
            val firstPossibleType = TestUtils.getDummyObjectType("FirstObjectType")
            val secondPossibleType = TestUtils.getDummyObjectType("SecondObjectType")
            val thirdPossibleType = TestUtils.getDummyObjectType("ThirdObjectType")

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
            val firstPossibleType = TestUtils.getDummyObjectType("FirstObjectType")
            val secondPossibleType =
                TestUtils.getDummyObjectType("SecondObjectType", TestUtils.getRepresentationDirective("kotlin.String"))
            val thirdPossibleType = TestUtils.getDummyObjectType("ThirdObjectType")

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

            Assertions.assertEquals(Value::class.java.canonicalName, rawType.canonicalName)
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

        @Test
        fun `should prefer representation directive over built-in scalar type correctly`() {
            // Build a String scalar with UUID as representation.
            val type = Scalars.GraphQLString.transform {
                it.withDirective(
                    TestUtils.getRepresentationDirective("java.util.UUID")
                )
            }

            val result = kotlinTypeMapper.getKotlinType(type)

            Assertions.assertEquals(ClassName("java.util", "UUID").copy(true), result)
        }

        @Test
        fun `should return representation for custom scalar with no representation correctly`() {
            val type = GraphQLScalarType.newScalar().name("CustomTestScalar").coercing(DummyCoercing).build()

            val result = kotlinTypeMapper.getKotlinType(type)

            Assertions.assertEquals(ANY.copy(true), result)
        }

        @Test
        fun `should return parameterized type if representation contains parameters`() {
            val type = TestUtils.getDummyObjectType(
                "Test",
                TestUtils.getRepresentationDirective("kotlin.Pair", listOf("java.util.UUID", "kotlin.String"))
            )

            val result = kotlinTypeMapper.getKotlinType(type)

            Assertions.assertEquals(
                ClassName.bestGuess("kotlin.Pair")
                    .parameterizedBy(ClassName.bestGuess("java.util.UUID"), ClassName.bestGuess("kotlin.String"))
                    .copy(true), result
            )
        }

        @Test
        fun `should throw exception on invalid parameter on kRepresentation`() {
            val type = TestUtils.getDummyObjectType(
                "Test",
                TestUtils.getRepresentationDirective(
                    "kotlin.Pair",
                    listOf("this is not a valid class", "kotlin.String")
                )
            )

            Assertions.assertThrows(IllegalArgumentException::class.java) {
                kotlinTypeMapper.getKotlinType(type)
            }
        }

        @Test
        fun `should return parameterized type with star if set on kRepresentation`() {
            val type = TestUtils.getDummyObjectType(
                "Test",
                TestUtils.getRepresentationDirective("kotlin.Pair", listOf("*", "*"))
            )

            val result = kotlinTypeMapper.getKotlinType(type)

            Assertions.assertEquals(
                ClassName.bestGuess("kotlin.Pair")
                    .parameterizedBy(STAR, STAR)
                    .copy(true), result
            )
        }

        @Test
        fun `should replace all parameters with star-projections if option is enabled`() {
            val type = TestUtils.getDummyObjectType(
                "Test", TestUtils.getRepresentationDirective(
                    "kotlin.Pair",
                    listOf("kotlin.String", "kotlin.String")
                )
            )

            val result = kotlinTypeMapper.getKotlinType(type, withStarProjectionsOnly = true)

            Assertions.assertEquals(
                ClassName.bestGuess("kotlin.Pair").parameterizedBy(STAR, STAR).copy(true),
                result
            )
        }

        @Test
        fun `should remove all parameters if option is enabled`() {
            val type = TestUtils.getDummyObjectType(
                "Test", TestUtils.getRepresentationDirective(
                    "kotlin.Pair",
                    listOf("kotlin.String", "kotlin.String")
                )
            )

            val result = kotlinTypeMapper.getKotlinType(type, withParameters = false)

            Assertions.assertEquals(ClassName.bestGuess("kotlin.Pair").copy(true), result)
        }
    }

    private fun getDoubleNullDirective(): GraphQLDirective =
        GraphQLDirective.newDirective()
            .name("kDoubleNull")
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

    object DummyCoercing : Coercing<Any, Any> {
        override fun parseValue(input: Any?): Any = TODO()
        override fun parseLiteral(input: Any?): Any = TODO()
        override fun serialize(dataFetcherResult: Any?): Any = TODO()
    }
}
