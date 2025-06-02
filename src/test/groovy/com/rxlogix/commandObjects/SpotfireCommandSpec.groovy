
package com.rxlogix.commandObjects


import com.rxlogix.spotfire.SpotfireService
import grails.testing.gorm.DataTest
import spock.lang.Specification

class SpotfireCommandSpec extends Specification {

    def "Test for SpotfireCommand fullName regex validation"() {
        given:
        def spotfireService = Mock(SpotfireService)
        SpotfireCommand spotfireCommand = new SpotfireCommand(fullFileName: fullName)
        spotfireCommand.spotfireService = spotfireService
        spotfireService.fileNameExist(_) >> false
        spotfireService.invalidFileNameLength(_) >> false
        expect:
        spotfireCommand.validate(['fullFileName']) == validated
        where:
        fullName                                                 || validated
        "Contin_01-Jan-1900_23-Jun-2016_AoD_23-Jun-2016_Drug"    || true
        "Contin_01-Jan-1900_23-Jun-2016_AoD_23-Jun-2016_Drug:"   || false
        "Contin_01-Jan-1900---23-Jun-2016_AoD_23--Jun-2016_Drug" || true
        "Contin_  _Drug"                                         || true
        "Contin=Drug"                                            || false
    }

    def "Test for getAllProductFamilyIds"() {
        given:
        SpotfireCommand spotfireCommand = new SpotfireCommand([endDate: new Date(), productFamilyIds: "PG10021@!PG10071@!PG10041@!PG10031@!",type: "S",fullFileName:"Test file"])
        when:
        Set<String> productFamilyIds = spotfireCommand.getAllProductFamilyIds()
        then:
        productFamilyIds.size() == 4
    }
}
