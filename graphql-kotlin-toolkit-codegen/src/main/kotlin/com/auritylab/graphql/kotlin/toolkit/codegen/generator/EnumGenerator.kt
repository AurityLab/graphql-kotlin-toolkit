package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.BindingMapper
import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLEnumType

/**
 * Implements a [AbstractClassGenerator] which will generate the source code for a [GraphQLEnumType].
 * This will generate the actual `enum` which an additional [String] value.
 */
internal class EnumGenerator(
    private val enumType: GraphQLEnumType,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper,
    bindingMapper: BindingMapper,
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper, bindingMapper) {
    override val fileClassName: ClassName = getGeneratedType(enumType)

    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildEnumClass(enumType))
    }

    private fun buildEnumClass(enum: GraphQLEnumType): TypeSpec {
        return TypeSpec.enumBuilder(getGeneratedType(enum))
            // Create the primary constructor with a "stringValue" parameter.
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("stringValue", String::class)
                    .build()
            )
            // Create the "stringValue" property which will be initialized by the previously created primary constructor.
            .addProperty(
                PropertySpec.builder("stringValue", String::class)
                    .initializer("stringValue")
                    .build()
            )
            .also { builder ->
                // Go through all enum values and create enum constants within this enum.
                enum.values.forEach { enum ->
                    builder.addEnumConstant(
                        enum.name,
                        TypeSpec.anonymousClassBuilder()
                            .addSuperclassConstructorParameter("%S", enum.name)
                            .build()
                    )
                }

                // If the "kRepresentation" directive is set on the enum, we have to add a parser/converter function
                DirectiveFacade.representation.getArguments(enum)?.className
                    ?.let {
                        // Add the output property/function.
                        builder.addProperty(buildParserProperty(it))
                        builder.addFunction(buildPropertyOperatorFunction())

                        // Add the input functions within a companion, as they are static.
                        builder.addType(
                            TypeSpec.companionObjectBuilder()
                                .addFunction(buildInputParser(it))
                                .addFunction(buildInputParserOperatorFunction(it))
                                .build()
                        )
                    }
            }
            .build()
    }

    /**
     * Will build a parser property which is capable of converting the enum constant into the given
     * [representationClass]. The function utilizes the `.valueOf(...)` method on the enum constant.
     */
    private fun buildParserProperty(representationClass: String): PropertySpec {
        // Resolve the class name and the valueOf function of the class.
        val parsedClass = ClassName.bestGuess(representationClass)

        // Build the converter code.
        val code = CodeBlock.builder()
            .beginControlFlow("return try")
            .addStatement("%L(name)", MemberName(parsedClass, "valueOf").canonicalName)
            .endControlFlow()
            .beginControlFlow("catch(ex: IllegalArgumentException)")
            .addStatement(
                "throw NoSuchElementException(%P)",
                "Enum value '\$name' could not be found on enum '${parsedClass.canonicalName}'"
            )
            .endControlFlow()
            .build()

        return PropertySpec.builder("representation", parsedClass)
            .getter(FunSpec.getterBuilder().addCode(code).build())
            .build()
    }

    /**
     * Will build a function which overrides the invoke operator and delegates to the "representation" property
     * to resolve the representation.
     */
    private fun buildPropertyOperatorFunction(): FunSpec {
        return FunSpec.builder("invoke")
            .addModifiers(KModifier.OPERATOR)
            .addCode("return representation")
            .build()
    }

    /**
     * Will build a function which accepts the representation enum as input and returns the matching generated
     * enum constant. This also utilizes the #valueOf(...) method of the enum.
     */
    private fun buildInputParser(representationClass: String): FunSpec {
        val code = CodeBlock.builder()
            .beginControlFlow("return try")
            .addStatement("valueOf(input.name)")
            .endControlFlow()
            .beginControlFlow("catch(ex: IllegalArgumentException)")
            .addStatement(
                "throw NoSuchElementException(%P)",
                "Enum value '\${input.name}' could not be found on enum '\${this::class.java.canonicalName}'"
            )
            .endControlFlow()
            .build()

        // Build the function with the input and the generated code.
        return FunSpec.builder("of")
            .addParameter("input", ClassName.bestGuess(representationClass))
            .addCode(code)
            .build()
    }

    /**
     * Will build a invoke function override, which delegates to the #of(...) function to convert the
     * representation enum to the generated enum constant.
     */
    private fun buildInputParserOperatorFunction(representationClass: String): FunSpec {
        return FunSpec.builder("invoke")
            .addModifiers(KModifier.OPERATOR)
            .addParameter("input", ClassName.bestGuess(representationClass))
            .addCode("return of(input)")
            .build()
    }
}
