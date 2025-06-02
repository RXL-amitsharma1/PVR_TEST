package com.rxlogix.config

import com.rxlogix.hibernate.EscapedILikeExpression

class ComparisonQueue {
    Long entityId1
    Long entityId2
    String entityName1
    String entityName2
    String entityType
    Status status
    Date dateCompared
    Date dateCreated = new Date()
    String message

    static constraints = {
        message nullable: true
        dateCompared nullable: true
    }

    static mapping = {
        table name: "COMPARISON_QUEUE"
        entityId1 column: "ENTITY_ID_1"
        entityId2 column: "ENTITY_ID_2"
        entityName1 column: "ENTITY_NAME_1"
        entityName2 column: "ENTITY_NAME_2"
        entityType column: "ENTITY_TYPE"
        status column: "STATUS"
        message column: "MESSAGE"
        dateCreated column: "DATE_CREATED"
        dateCompared column: "DATE_COMPARED"
    }

    enum Status {
        WAITING, COMPLETED, ERROR

    }

    static namedQueries = {
        fetchBySearchString { String search, Status status1 ->
            if (search) {
                or {
                    iLikeWithEscape("entityName1", "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('entityName2', "%${EscapedILikeExpression.escapeString(search)}%")
                }

            }
            eq("status", status1)
        }
    }
}
