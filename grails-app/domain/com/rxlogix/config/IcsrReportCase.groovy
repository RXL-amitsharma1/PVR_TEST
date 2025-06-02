package com.rxlogix.config

import com.rxlogix.hibernate.EscapedILikeExpression
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils

class IcsrReportCase implements Serializable{

    Long exIcsrTemplateQueryId
    String caseNumber
    Long versionNumber
    String profileName
    String productName
    String eventPreferredTerm
    String susar
    String downgrade
    Long tenantId
    Long processedReportId //for icsr-adhoc CIOMS parameter needed from DB

    static constraints = {
        profileName nullable: true
        productName nullable: true
        eventPreferredTerm nullable: true
        susar nullable: true
        downgrade nullable: true
    }

    static mapping = {
        datasource "pva"
        table name: 'VW_ADHOC_E2B_CASE_TRACNG_DETLS'
        version false
        id composite: ['exIcsrTemplateQueryId', 'caseNumber', 'versionNumber']
        exIcsrTemplateQueryId column: 'SECTION_ID'
        caseNumber column: 'CASE_NUM'
        versionNumber column: 'VERSION_NUM'
        profileName column: 'PARTNER_NAME'
        productName column: 'PRODUCT_NAME'
        eventPreferredTerm column: 'EVENT_NAME'
        susar column: 'SUSAR'
        downgrade column: 'DOWNGRADE'
        tenantId column: 'TENANT_ID'
        processedReportId column: 'PROCESSED_REPORT_ID'
    }

    static namedQueries = {

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        getAllBySearchterm { Long id, String term ->
            eq('exIcsrTemplateQueryId', id)
            if (term) {
                or {
                    iLikeWithEscape('caseNumber', "%${EscapedILikeExpression.escapeString(term)}%")
                    iLikeWithEscape('profileName', "%${EscapedILikeExpression.escapeString(term)}%")
                    iLikeWithEscape('productName', "%${EscapedILikeExpression.escapeString(term)}%")
                    iLikeWithEscape('eventPreferredTerm', "%${EscapedILikeExpression.escapeString(term)}%")
                    iLikeWithEscape('susar', "%${EscapedILikeExpression.escapeString(term)}%")
                    iLikeWithEscape('downgrade', "%${EscapedILikeExpression.escapeString(term)}%")
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }
    }

    String uniqueIdentifier() {
        return "${exIcsrTemplateQueryId}**${caseNumber}**${versionNumber}"
    }
}
