package com.rxlogix.mapping

import com.rxlogix.hibernate.EscapedILikeExpression

class SafetyCalendar implements Serializable {
    
    Long id
    String name
    boolean isDeleted
    
    static constraints = {
        name nullable: true
    }
    
    static mapping = {
        datasource "pva"
        table "VW_HOLIDAY_CALENDAR"
        cache: "read-only"
        version false
        id column: "CALENDAR_ID", type: "long", generator: "assigned"
        name column: "CALENDAR_NAME"
        isDeleted column: 'DELETED'
        
    }

    static namedQueries = {

        getAllSafetyCalenderBySearchString { String search ->
            if (search) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search.trim())}%")
            }
            eq('isDeleted', false)
        }

    }
}
