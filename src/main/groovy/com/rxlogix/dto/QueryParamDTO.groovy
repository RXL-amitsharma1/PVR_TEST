package com.rxlogix.dto

import com.rxlogix.user.User

class QueryParamDTO {
    User user
    String search = ""
    int max
    int offset
    boolean isFaersQuery
    boolean isEvdasQuery
    boolean isSafetyQuery
    boolean isNonParameterisedQuery
    boolean isEmbaseQuery

    QueryParamDTO(Map params) {
        user = User.findByUsernameIlikeAndEnabled(params.username, true)
        search = params.search
        max = params.max ? params.max as Integer : 30
        offset = params.offset ? params.offset as Integer : 0
        isFaersQuery = params.isFaersQuery?.toBoolean()
        isEvdasQuery = params.isEvdasQuery?.toBoolean()
        isSafetyQuery = params.isSafetyQuery?.toBoolean()
        isNonParameterisedQuery = params.isNonParameterisedQuery?.toBoolean()
        isEmbaseQuery = params.isEmbaseQuery?.toBoolean()
    }
}
