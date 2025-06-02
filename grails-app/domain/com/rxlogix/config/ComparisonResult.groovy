package com.rxlogix.config


import com.rxlogix.hibernate.EscapedILikeExpression

class ComparisonResult {
    Long entityId1
    Long entityId2
    String entityName1
    String entityName2
    String entityType
    Boolean result
    Boolean supported = true
    String message
    Date dateCreated = new Date()
    String data

    static constraints = {
        message nullable: true
    }

    static mapping = {
        table name: "COMPARISON_RESULT"
        id column: "ID"
        version column: "VERSION"
        entityId1 column: "ENTITY_ID_1"
        entityId2 column: "ENTITY_ID_2"
        entityName1 column: "ENTITY_NAME_1"
        entityName2 column: "ENTITY_NAME_2"
        entityType column: "ENTITY_TYPE"
        dateCreated column: "DATE_CREATED"
        result column: "RESULT"
        message column: "MESSAGE"
        supported column: "SUPPORTED"
        data column: "DATA", lazy: true
    }
    static namedQueries = {
        fetchBySearchString { String search ->
            if (search) {
                or {
                    iLikeWithEscape("entityName1", "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('entityName2', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
        }
    }
}
