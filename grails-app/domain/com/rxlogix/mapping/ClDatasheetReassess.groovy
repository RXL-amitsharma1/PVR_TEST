package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClDatasheetReassess implements SelectableList {

    BigDecimal id
    String sheetName
    String hasChildrenFlag
    Long tenantId

    static mapping = {
        datasource "pva"
        table "VW_DATASHEET_REASSESS"

        cache: "read-only"
        version false

        id column: "DATASHEET_ID", type: "big_decimal", generator: "assigned"
        sheetName column: "SHEET_NAME"
        tenantId column: "TENANT_ID"
        hasChildrenFlag column: "PARENT_DS"
    }

    static constraints = {
        id(nullable:false) //not unique, can have revisions
        sheetName(blank:false, maxSize:40)
        hasChildrenFlag(blank:true)

    }

    @Override
    List<Object> getSelectableList(String lang) {
        withNewSession {
            return executeQuery("select distinct cdr.sheetName from ClDatasheetReassess cdr order by cdr.sheetName asc")
//        return ClDatasheetReassess.findAll().unique().collect { it.sheetName }.sort()
        }
    }

    List<Object> getSelectableList(String lang, Long tenantId) {
        withNewSession {
            return ClDatasheetReassess.findAllByTenantId(tenantId)?.sort { it.sheetName }
        }
    }
}