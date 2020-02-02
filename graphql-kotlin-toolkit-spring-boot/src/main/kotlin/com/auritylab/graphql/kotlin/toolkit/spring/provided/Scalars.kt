package com.auritylab.graphql.kotlin.toolkit.spring.provided

import graphql.schema.GraphQLScalarType

val providedUploadScalar = GraphQLScalarType.newScalar().name("Upload").coercing(UploadScalarCoercing()).build()
