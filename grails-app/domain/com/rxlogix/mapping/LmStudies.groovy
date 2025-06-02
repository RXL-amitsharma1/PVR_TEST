package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmStudies implements SelectableList, Serializable {

    BigDecimal studyId
    BigDecimal id
    String studyNum
    String lang

    static mapping = {
        datasource "pva"
        table "VW_STUDY_NUM_ALL"
        cache: "read-only"
        version false
        id composite: ['studyId', 'lang']
        studyId column: "STUDY_KEY", type: "big_decimal"
        studyNum column: "STUDY_NUM"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        id(nullable:false, unique:false)
        studyNum(blank:false, maxSize:35)

    }

    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct lms.studyNum from LmStudies lms where lms.lang = :lang order by lms.studyNum asc",[lang:lang])
//        return LmStudies.findAll().unique().collect { it.studyNum }.sort()
    }

    static List<LmProtocols> fetchProtocolsByStudy(BigDecimal studyId,String currentLang) {
        List<LmProtocols> protocols = []
        List protocolsId = LmStudiesLmProtocolsMapping.createCriteria().list {
            projections {
                property('protocolId')
            }
            eq('studyId', studyId)
        }
        if (protocolsId) {
            protocolsId.collate(999).each { list ->
                protocols += LmProtocols.createCriteria().list {
                    inList("protocolId", list)
                    eq("lang", currentLang)
                }
            }
        }
        return protocols
    }
}
