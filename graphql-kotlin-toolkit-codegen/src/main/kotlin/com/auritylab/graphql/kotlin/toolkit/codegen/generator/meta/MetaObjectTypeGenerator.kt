package com.auritylab.graphql.kotlin.toolkit.codegen.generator.meta

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.AbstractClassGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.uppercaseFirst
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.BindingMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLObjectType
import kotlin.reflect.KClass

internal class MetaObjectTypeGenerator(
    private val objectType: GraphQLObjectType,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper,
    bindingMapper: BindingMapper,
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper, bindingMapper) {
    override val fileClassName: ClassName = generatedMapper.getObjectTypeMetaClassName(objectType)

    override fun build(builder: FileSpec.Builder) {
        val type = TypeSpec.objectBuilder(fileClassName)

        type.addSuperinterface(
            bindingMapper.metaObjectTypeType.parameterizedBy(
                kotlinTypeMapper.getKotlinType(objectType).copy(nullable = false)
            )
        )

        // Add the basic properties for ObjectType itself.
        type.addProperty(buildNameProperty())
        type.addProperty(buildRuntimeTypeProperty())

        // Create the property for each field.
        objectType.fieldDefinitions.forEach {
            type.addProperty(buildFieldProperty(it))
        }

        // As we depend on the fields, this property has to go after the field properties.
        type.addProperty(buildAllFieldsProperty())

        builder.addType(type.build())
    }

    /**
     * Will build the property which represents the name of the object type.
     */
    private fun buildNameProperty(): PropertySpec {
        return PropertySpec.builder("name", String::class)
            .initializer("\"" + objectType.name + "\"")
            .addModifiers(KModifier.CONST)
            .build()
    }

    /**
     * Will build the property which represent the runtime type of the object type.
     */
    private fun buildRuntimeTypeProperty(): PropertySpec {
        // Resolve the ObjectType to an actual Kotlin type. We need to copy it with non nullable because otherwise it
        // would lead to syntax errors.
        val runtimeType = kotlinTypeMapper.getKotlinType(objectType).copy(nullable = false)

        // The return type is basically just a KClass parameterized with the previously resolved runtime type.
        val returnType = KClass::class.asTypeName().parameterizedBy(runtimeType)

        // Create the actual property.
        return PropertySpec.builder("runtimeType", returnType)
            .initializer("%T::class", runtimeType)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    /**
     * Will build the property which represents the given [field] with the meta holder.
     */
    private fun buildFieldProperty(field: GraphQLFieldDefinition): PropertySpec {
        // Resolve all required types...
        val unwrapped = GraphQLTypeHelper.unwrapType(field.type)
        val refType =
            if (unwrapped is GraphQLObjectType) generatedMapper.getObjectTypeMetaClassName(unwrapped) else NOTHING
        val runtimeType = kotlinTypeMapper.getKotlinType(unwrapped).copy(nullable = false)
        val returnType = bindingMapper.metaObjectTypeFieldType.parameterizedBy(refType, runtimeType)

        // Create the implementation of the field meta and add all required properties.
        val fieldImpl = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(returnType)
            .addProperty(buildFieldNameProperty(field))
            .addProperty(buildFieldTypeProperty(field))
            .addProperty(buildFieldRuntimeTypeProperty(field))
            .addProperty(buildFieldRefProperty(field))
            .build()

        // Create the actual property.
        return PropertySpec.builder("field" + field.name.uppercaseFirst(), returnType)
            .initializer("%L", fieldImpl)
            .build()
    }

    /**
     * Will build the property which represents the name of the given [field].
     * The property also contains the 'override' modifier.
     */
    private fun buildFieldNameProperty(field: GraphQLFieldDefinition): PropertySpec {
        return PropertySpec.builder("name", String::class)
            .initializer("\"" + field.name + "\"")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    /**
     * Will build the property which represents the name of the type of the given [field].
     * The property also contains the 'override' modifier.
     */
    private fun buildFieldTypeProperty(field: GraphQLFieldDefinition): PropertySpec {
        // Unwrap the type of the field as we're not interested in arrays etc.
        val unwrapped = GraphQLTypeHelper.unwrapType(field.type)

        // Define the string name of the type. The fallback is just to be sure if there are unexpected types.
        val typeName = (unwrapped as? GraphQLNamedType)?.name ?: "__UNDEFINED"

        // Build the actual property.
        return PropertySpec.builder("type", String::class)
            .initializer("\"" + typeName + "\"")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    /**
     * Will build the property which represents the runtime type of the type of the given [field]. The runtime type
     * is expressed as a [KClass]. The property also contains the 'override' modifier.
     */
    private fun buildFieldRuntimeTypeProperty(field: GraphQLFieldDefinition): PropertySpec {
        // Unwrap the type of the field as we're not interested in arrays etc.
        val unwrapped = GraphQLTypeHelper.unwrapType(field.type)

        // Resolve the actual Kotlin type of the field. We need to copy it as non-nullable to avoid syntax errors.
        val runtimeType = kotlinTypeMapper.getKotlinType(unwrapped).copy(nullable = false)

        // Create the return type which is basically just a KClass parameterized by the resolved runtime type.
        val returnType = KClass::class.asTypeName().parameterizedBy(runtimeType)

        // Build the actual property.
        return PropertySpec.builder("runtimeType", returnType)
            .initializer("%T::class", runtimeType)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    /**
     * Will build the property which represents the reference to the meta object of the given field.
     * This might also just throw an exception if the type does not have a meta object.
     * The property also contains the 'override' modifier.
     */
    private fun buildFieldRefProperty(field: GraphQLFieldDefinition): PropertySpec {
        // Unwrap the type of the field as we're not interested in arrays etc.
        val unwrapped = GraphQLTypeHelper.unwrapType(field.type)

        // Resolve the actual reference type. To determine a reference the return type must be an ObjectType.
        val refType =
            if (unwrapped is GraphQLObjectType) generatedMapper.getObjectTypeMetaClassName(unwrapped) else NOTHING

        // Build the basic property.
        val property = PropertySpec.builder("ref", refType)
            .addModifiers(KModifier.OVERRIDE)

        // The initializer varies based on the unwrapped type.
        if (unwrapped is GraphQLObjectType) {
            // We can return the actual meta object.
            property.initializer(
                "%T",
                generatedMapper.getObjectTypeMetaClassName(unwrapped)
            )
        } else {
            // There is no meta information... Throw an exception.
            property.getter(
                FunSpec.getterBuilder()
                    .addCode(
                        "throw %T(%S)",
                        IllegalStateException::class,
                        "No reference available"
                    )
                    .build()
            )
        }

        return property.build()
    }

    /**
     * Will build the property which returns all available fields within this ObjectType. They're all in a [Set].
     */
    private fun buildAllFieldsProperty(): PropertySpec {
        // Create the return type which is a Set parameterized with the MetaObjectTypeField.
        val returnType = SET.parameterizedBy(
            bindingMapper.metaObjectTypeFieldType.parameterizedBy(STAR, STAR)
        )

        // Create the initializer with a basic "setOf(..)". All fields will be added to the set on the runtime.
        val codeBlock = CodeBlock.builder()
        codeBlock.add("setOf(")
        objectType.fieldDefinitions.forEach {
            codeBlock.add(getFieldPropertyName(it) + ",")
        }
        codeBlock.add(")")

        // Create the actual property with the previously created CodeBlock.
        return PropertySpec.builder("fields", returnType)
            .initializer(codeBlock.build())
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    /**
     * Returns the name of the field meta information property for the given [field].
     */
    private fun getFieldPropertyName(field: GraphQLFieldDefinition) = "field" + field.name.uppercaseFirst()
}
