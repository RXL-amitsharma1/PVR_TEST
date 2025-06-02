package com.rxlogix.dictionary

import com.rxlogix.util.DbUtil
import grails.util.Holders

//This domain is for making sources wise JSON data.
class DictionaryGroupData {

    Long id
    String data
    boolean isDeleted
    Integer tenantId
    String groupName
    Integer type

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().supported.datasource)
        table " DICT_GRP_DATA"
        version false
        data column: "GRP_DATA", sqlType: DbUtil.longStringType
        groupName column: 'DICT_GRP_NAME'
        type column: 'DICT_GRP_TYPE'
        tenantId column: 'TENANT_ID'
        id column: "DICT_GRP_ID", generator: "assigned"
    }
}
