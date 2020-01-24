package com.auritylab.graphql.kotlin.toolkit.spring.boot.starter

import com.auritylab.graphql.kotlin.toolkit.spring.GQLConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnWebApplication
@ComponentScan(basePackageClasses = [GQLConfiguration::class])
open class GraphQLAutoconfiguration
