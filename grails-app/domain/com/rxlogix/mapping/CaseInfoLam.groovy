package com.rxlogix.mapping

import com.rxlogix.hibernate.EscapedILikeExpression

class CaseInfoLam extends CaseInfoEntity{


    static mapping = {
        datasource "pva"
        table "V_A_IDENTIFICATION"
        id composite: ['caseNumber', 'version']
        caseNumber column: "CASE_NUM"
        caseId column: "CASE_ID"
        version false
        version column: "VERSION_NUM"
    }

    static namedQueries = {
        getAllCaseBy { String search ->
            if (search) {
                iLikeWithEscape('caseNumber', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            order('caseNumber', 'asc')
            order('version', 'desc')
        }

        findAllValidValue { List validCaseList->
            projections {
                distinct('caseNumber')
            }
            inList("caseNumber", validCaseList)
        }
    }


}
