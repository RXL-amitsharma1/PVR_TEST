package com.rxlogix

import com.rxlogix.config.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.transform.CompileDynamic
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import spock.lang.Specification

@CompileDynamic
class ReasonOfDelayServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReasonOfDelayService> {
    private static Long MAPPING_ID = 1

    void "Test hideWarning"() {
        given: "RCA mapping entity"
        Late.metaClass.static.get = {
            Long id -> Late mappingEntity = new Late(); mappingEntity.setId(id); mappingEntity
        }
        RootCause.metaClass.static.get = {
            Long id -> RootCause mappingEntity = new RootCause(); mappingEntity.setId(id); mappingEntity
        }
        RootCauseSubCategory.metaClass.static.get = {
            Long id -> RootCauseSubCategory mappingEntity = new RootCauseSubCategory(); mappingEntity.setId(id); mappingEntity
        }
        RootCauseClassification.metaClass.static.get = {
            Long id -> RootCauseClassification mappingEntity = new RootCauseClassification(); mappingEntity.setId(id); mappingEntity
        }
        ResponsibleParty.metaClass.static.get = {
            Long id -> ResponsibleParty mappingEntity = new ResponsibleParty(); mappingEntity.setId(id); mappingEntity
        }
        AutoReasonOfDelay.metaClass.static.findAll = {
            generateAutoReasonOfDelayList()
        }
        service.targetDatastore = new SimpleMapDatastore(['pva'], Late)
        service.targetDatastore = new SimpleMapDatastore(['pva'], RootCause)
        service.targetDatastore = new SimpleMapDatastore(['pva'], ResponsibleParty)
        service.targetDatastore = new SimpleMapDatastore(['pva'], RootCauseSubCategory)
        service.targetDatastore = new SimpleMapDatastore(['pva'], RootCauseClassification)
        service.targetDatastore = new SimpleMapDatastore(['pva'], AutoReasonOfDelay)

        when: "hideWarning is called for the mapping id and mapping active type"
        boolean result = service.hideWarning(mappingId, activeType)

        then: "result is TRUE in case of the first AutoReasonOfDelay's queriesRCA mapping id equals the selected mapping id, FALSE - if not"
        result == expected

        where:
        mappingId    | activeType               | expected
        MAPPING_ID   | 'showLate'               | true
        MAPPING_ID   | 'showRootCause'          | true
        MAPPING_ID   | 'showRootCauseSub'       | true
        MAPPING_ID   | 'showRootCauseClass'     | true
        MAPPING_ID   | ''                       | true /* ResponsibleParty as default */
        0            | 'showLate'               | false
        2            | 'showRootCause'          | false
        3            | 'showRootCauseSub'       | false
        4            | 'showRootCauseClass'     | false
        5            | ''                       | false
    }

    private List<AutoReasonOfDelay> generateAutoReasonOfDelayList() {
        AutoReasonOfDelay reasonOfDelay1 = new AutoReasonOfDelay(id: 1L)
        reasonOfDelay1.queriesRCA = [
                new QueryRCA(
                        lateId: [MAPPING_ID],
                        rootCauseId: [MAPPING_ID],
                        rootCauseSubCategoryId: [MAPPING_ID],
                        rootCauseClassId: [MAPPING_ID],
                        responsiblePartyId: [MAPPING_ID])
        ]
        AutoReasonOfDelay reasonOfDelay2 = new AutoReasonOfDelay(id: 2L)
        reasonOfDelay2.queriesRCA = [new QueryRCA()]
        [reasonOfDelay1, reasonOfDelay2]
    }
}
