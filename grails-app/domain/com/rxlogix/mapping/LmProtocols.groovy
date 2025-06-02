package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmProtocols implements SelectableList, Serializable {

    BigDecimal id
    BigDecimal protocolId
    String description
    String lang

    static mapping = {
        table "VW_PROTOCOL_ALL"
        datasource "pva"

        version false
        cache usage: "read-only"

        id composite: ['protocolId', 'lang']
        protocolId column: "PROTOCOL_ID", type: "big_decimal"
        description column: "PROTOCOL_DESCRIPTION"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        id(nullable:false, unique:false)
        description(maxSize: 40)
    }

    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct c.description from LmProtocols c where c.lang = :lang order by c.description asc",[lang:lang])
//        return LmProtocols.findAll().unique().collect { it.description }.sort()
    }

    static List<LmStudies> fetchStudiesByProtocols(BigDecimal protocolId, String currentLang) {
        List<LmStudies> studies = []
        List studiesId = LmStudiesLmProtocolsMapping.createCriteria().list {
            projections {
                property('studyId')
            }
            eq('protocolId', protocolId)
        }
        if (studiesId) {
            studiesId.collate(999).each { list ->
                studies += LmStudies.createCriteria().list {
                    inList("studyId", list)
                    eq("lang", currentLang)
                }
            }

        }
        return studies
    }
}
