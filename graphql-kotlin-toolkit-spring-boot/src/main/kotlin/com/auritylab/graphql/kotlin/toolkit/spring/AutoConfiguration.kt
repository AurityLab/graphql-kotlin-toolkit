package com.auritylab.graphql.kotlin.toolkit.spring

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@ConditionalOnWebApplication
class AutoConfiguration
