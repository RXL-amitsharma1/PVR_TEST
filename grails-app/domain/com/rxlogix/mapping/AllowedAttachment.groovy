package com.rxlogix.mapping

import com.rxlogix.hibernate.EscapedILikeExpression

class AllowedAttachment implements Serializable {
    
    Long id
    String name
    Integer langId

    static constraints = {
        name nullable: true
    }
    
    static mapping = {
        datasource "pva"
        table "VW_LCF_CHARACTERISTICS"
        
        cache: "read-only"
        version false
        
        id column: "ID", type: "long", generator: "assigned"
        name column: "CHARACTERISTICS"
        langId column: 'LANG_ID'
    }

    static namedQueries = {

        getAllAllowedAttachmentBySearchStringAndlangId { String search, Integer langId ->
            eq('langId', langId)
            if (search) {
                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search.trim())}%")
            }
        }

    }

}
