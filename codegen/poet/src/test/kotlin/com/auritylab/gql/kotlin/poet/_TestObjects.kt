package com.auritylab.gql.kotlin.poet

import com.auritylab.gql.kotlin.poet.mapper.KotlinTypeMapper
import com.auritylab.gql.kotlin.poet.mapper.NameMapper
import graphql.Scalars.*
import graphql.schema.*

internal object _TestObjects {
    val kotlinRepresentationDirective = { value: String ->
        GraphQLDirective.newDirective()
                .name("kotlinRepresentation")
                .argument {
                    it.name("class")
                    it.type(GraphQLString)
                    it.value(value)
                }
                .build()
    }
    val queryType = GraphQLObjectType.newObject()
            .name("Query")
            .field {
                it.name("getUser")
                it.argument {
                    it.name("showAll")
                    it.type(GraphQLBoolean)
                }
                it.argument {
                    it.name("filter")
                    it.type(GraphQLInputObjectType.newInputObject()
                            .name("GetUserFilterInput")
                            .field {
                                it.name("name")
                                it.type(GraphQLString)
                            }
                            .field {
                                it.name("sub")
                                it.type(GraphQLInputObjectType.newInputObject()
                                        .name("GetUserFilterSubInput")
                                        .field {
                                            it.name("test")
                                            it.type(GraphQLString)
                                        }
                                        .build())
                            }
                            .build())
                }
                it.type(GraphQLObjectType.newObject()
                        .name("User")
                        .field {
                            it.name("id")
                            it.type(GraphQLID)
                        }
                        .field {
                            it.name("name")
                            it.type(GraphQLString)
                        }
                        .field {
                            it.name("type")
                            it.type(GraphQLEnumType.newEnum()
                                    .name("UserType")
                                    .value("TEACHER")
                                    .value("STUDENT")
                                    .build())
                        }
                        .withDirective(kotlinRepresentationDirective("com.auritylab.gql.test.User"))
                        .build())
            }.build()

    val schema = GraphQLSchema.newSchema().query(queryType).build()


    val nameMapper = NameMapper(PoetOptions())
    val kotlinTypeMapper = KotlinTypeMapper(PoetOptions(), nameMapper, schema)

}
