package com.auritylab.graphql.kotlin.toolkit.spring

import me.lazmaid.kraph.Kraph

object TestOperations {
    val getUserQuery = Kraph {
        query {
            fieldObject("getUser") {
                field("id")
                field("name")
                field("surname")
            }
        }
    }.toGraphQueryString()

    val createUserMutation_withoutUpload = Kraph {
        mutation {
            // val uploadVar = variable("upload", "Upload", "")
            fieldObject("createUser", args = mapOf("name" to "test", "surname" to "test")) {
                field("id")
                field("name")
                field("surname")
            }
        }
    }.toGraphQueryString()

    val createUserMutation_withUpload = Kraph {
        mutation {
            val uploadVar = variable("upload", "Upload", "")
            fieldObject("createUser", args = mapOf("name" to "test", "surname" to "test", "upload" to uploadVar)) {
                field("id")
                field("name")
                field("surname")
            }
        }
    }.toGraphQueryString()
}
