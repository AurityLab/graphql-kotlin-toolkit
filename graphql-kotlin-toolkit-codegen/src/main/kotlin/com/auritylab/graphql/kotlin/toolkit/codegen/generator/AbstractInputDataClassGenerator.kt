package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.BindingMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLType

/**
 * Describes an abstract [FileGenerator] which only builds data classes based
 * on given properties ([dataProperties]).
 */
internal abstract class AbstractInputDataClassGenerator(
    private val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper, bindingMapper: BindingMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper, bindingMapper) {
    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildDataClass())
    }

    protected abstract val dataProperties: List<DataProperty>

    protected open val buildByMapMemberName: MemberName
        get() = MemberName(fileClassName, "buildByMap")

    private fun buildDataClass(): TypeSpec {
        val properties = dataProperties
            .map { Pair(it.name, getKotlinType(it.type, it.directiveContainer)) }

        return TypeSpec.classBuilder(fileClassName)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(properties.map { ParameterSpec(it.first, it.second) })
                    .build()
            )
            .addProperties(properties.map { PropertySpec.builder(it.first, it.second).initializer(it.first).build() })
            .addType(buildDataClassCompanionObject())
            .build()
    }

    private fun buildDataClassCompanionObject(): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addFunction(createBuilderFun())
            .addFunctions(
                dataProperties
                    .map {
                        argumentCodeBlockGenerator.buildResolver(
                            it.name,
                            it.type,
                            it.directiveContainer
                        )
                    }
            )
            .build()
    }

    /**
     * Will build the "buildByMap" function which takes the input map and builds the data class.
     */
    private fun createBuilderFun(): FunSpec {
        val namedParameters = dataProperties
            .joinToString(", ") { "resolve${NamingHelper.uppercaseFirstLetter(it.name)}(map)" }
        return FunSpec.builder(buildByMapMemberName.simpleName)
            .addParameter("map", MAP.parameterizedBy(STRING, ANY))
            .returns(fileClassName)
            .addStatement("return %T($namedParameters)", fileClassName)
            .build()
    }

    protected data class DataProperty(
        val name: String,
        val type: GraphQLType,
        val directiveContainer: GraphQLDirectiveContainer? = null
    )
}
