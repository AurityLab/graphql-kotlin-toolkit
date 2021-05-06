package com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractClassGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

/**
 * Generator for the ObjectType Field interface.
 *
 * The generated interface contains 4 interfaces:
 * * **name**: The actual name of the field.
 * * **type**: The type of the field as string.
 * * **runtimeType**: The return type of the field as [KClass].
 * * **ref**: The reference to the meta information of the type of this field.
 */
internal class MetaObjectTypeFieldGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName = generatedMapper.getMetaObjectTypeField()

    override fun build(builder: FileSpec.Builder) {
        val type = TypeSpec.interfaceBuilder(fileClassName)

        // Add the type parameters: T defines the type for the reference and R the type of the runtime type.
        val refType = TypeVariableName("T")
        val runtimeTypeType = TypeVariableName("R", ANY)
        type.addTypeVariables(setOf(refType, runtimeTypeType))

        type.addKdoc(
            """
            @param T Type of the meta information type.
            @param R Type of the runtime type of the type.
        """.trimIndent()
        )

        // Add all required properties for the field meta.
        type.addProperty(
            PropertySpec.builder("name", String::class)
                .addKdoc("""The actual name of this field.""")
                .build()
        )
        type.addProperty(
            PropertySpec.builder("type", String::class)
                .addKdoc("""The type of this field as string.""")
                .build()
        )
        type.addProperty(
            PropertySpec.builder("runtimeType", KClass::class.asTypeName().parameterizedBy(runtimeTypeType))
                .addKdoc("""The type of this field as a [KClass]. This might be [Any] if the type could not be determined.""")
                .build()
        )
        type.addProperty(
            PropertySpec.builder("ref", refType)
                .addKdoc("""The reference to the meta information object of the type of this field.""")
                .build()
        )

        builder.addType(type.build())
    }
}
