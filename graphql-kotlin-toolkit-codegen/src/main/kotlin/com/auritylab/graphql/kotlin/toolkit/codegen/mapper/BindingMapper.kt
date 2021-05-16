package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.squareup.kotlinpoet.ClassName

/**
 * Mapping for the types provided by the support module.
 */
class BindingMapper {
    val valueType: ClassName = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "Value")

    val abstractEnvType: ClassName = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "AbstractEnv")

    val metaObjectTypeFieldType = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "MetaObjectTypeField")
}
