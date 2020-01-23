package com.auritylab.graphql.kotlin.toolkit.spring

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

@Component
@ComponentScan
@EnableConfigurationProperties(GQLProperties::class)
class GQLConfiguration
