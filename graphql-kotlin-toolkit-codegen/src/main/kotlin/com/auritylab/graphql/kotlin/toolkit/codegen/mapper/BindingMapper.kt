package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.squareup.kotlinpoet.ClassName

/**
 * Mapping for the types provided by the support module.
 */
class BindingMapper {
    val valueType: ClassName = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "Value")

    val abstractEnvType: ClassName =
        ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "AbstractEnv")

    val metaField = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "MetaField")
    val metaFieldWithReference =
        ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "MetaFieldWithReference")

    val metaObjectType = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "MetaObjectType")
    val metaInterfaceType = ClassName("com.auritylab.graphql.kotlin.toolkit.codegenbinding.types", "MetaInterfaceType")
}
