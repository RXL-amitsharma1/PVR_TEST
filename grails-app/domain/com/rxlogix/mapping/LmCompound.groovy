package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmCompound implements SelectableList,Serializable {

    String number
    String lang

    static mapping = {
        datasource "pva"
        table "VW_CLIN_REF_ALL"
        cache: "read-only"
        version false
        id composite: ['number', 'lang']
        number column: 'CLIN_REF_NUM', sqlType: 'VARCHAR2(8003 CHAR)'
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
    }

    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct lmc.number from LmCompound lmc where lmc.lang = :lang order by lmc.number asc", [lang: lang])
    }
}
