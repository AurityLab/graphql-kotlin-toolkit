package com.auritylab.graphql.kotlin.toolkit.spring.boot.starter

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnWebApplication
@ComponentScan("com.auritylab.graphql.kotlin.toolkit.spring")
open class GraphQLAutoconfiguration
