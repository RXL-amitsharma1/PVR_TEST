/*
package com.rxlogix.spotfire

import com.rxlogix.mapping.LmProductFamily
import grails.test.mixin.Mock
import grails.web.mapping.LinkGenerator
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


@Mock(SpotfireService)
class SpotfireServiceSpec extends Specification {
    @Shared
    def spotfireService

    def setupSpec() {
        spotfireService = new SpotfireService()
        spotfireService.grailsApplication = [config:[grails:[appBaseURL:""]]]
        def linkGenerator = new MockFor(LinkGenerator)
        linkGenerator.demand.link(2..2) { LinkedHashMap m -> ""}
        spotfireService.grailsLinkGenerator = linkGenerator.proxyInstance()
        spotfireService.grailsApplication.config.spotfire = [date: [xmlFormat: "dd-MM-yyyy"]]
    }

    def "buildConfigurationBlock should create the parameters for spotfire client for drug" () {
        def configBlock = spotfireService.buildConfigurationBlock(
                "{\"${["100696", "100259"].join(",")}\"}",
                Date.parse("dd-MM-yyyy","01-01-1900"), Date.parse("dd-MM-yyyy","31-03-2015"), Date.parse("dd-MM-yyyy","17-01-2015"), -1,"drug", 10)
        expect:
            configBlock == """drug_p1.prod_family={"100696,100259"};
drug_p2.start_date={"01-01-1900"};drug_p3.end_date={"31-03-2015"};
drug_p4.as_of_date={"17-01-2015"};drug_p5.prod_family={"100696,100259"};
drug_p6.start_date={"01-01-1900"};drug_p7.end_date={"31-03-2015"};
drug_p8.as_of_date={"17-01-2015"};drug_p9.prod_family={"100696,100259"};
drug_p10.start_date={"01-01-1900"};drug_p11.end_date={"31-03-2015"};
drug_p12.as_of_date={"17-01-2015"};drug_p13.prod_family={"100696,100259"};
drug_p14.start_date={"01-01-1900"};drug_p15.end_date={"31-03-2015"};
drug_p16.as_of_date={"17-01-2015"};drug_p17.prod_family={"100696,100259"};
drug_p18.start_date={"01-01-1900"};drug_p19.end_date={"31-03-2015"};
drug_p20.as_of_date={"17-01-2015"};drug_p21.prod_family={"100696,100259"};
drug_p22.start_date={"01-01-1900"};drug_p23.end_date={"31-03-2015"};
drug_p24.as_of_date={"17-01-2015"};drug_p25.prod_family={"100696,100259"};
drug_p26.start_date={"01-01-1900"};drug_p27.end_date={"31-03-2015"};
drug_p28.as_of_date={"17-01-2015"};drug_p29.prod_family={"100696,100259"};
drug_p30.start_date={"01-01-1900"};drug_p31.end_date={"31-03-2015"};
drug_p32.as_of_date={"17-01-2015"};drug_p33.prod_family={"100696,100259"};
drug_p34.start_date={"01-01-1900"};drug_p35.end_date={"31-03-2015"};
drug_p36.as_of_date={"17-01-2015"};drug_p37.prod_family={"100696,100259"};
drug_p38.start_date={"01-01-1900"};drug_p39.end_date={"31-03-2015"};
drug_p40.as_of_date={"17-01-2015"};drug_p41.prod_family={"100696,100259"};
drug_p42.start_date={"01-01-1900"};drug_p43.end_date={"31-03-2015"};
drug_p44.as_of_date={"17-01-2015"};drug_p45.case_list_id={"-1"};drug_p46.case_list_id={"-1"};drug_p47.case_list_id={"-1"};
drug_p48.case_list_id={"-1"};drug_p49.case_list_id={"-1"};drug_p50.case_list_id={"-1"};drug_p51.case_list_id={"-1"};drug_p52.case_list_id={"-1"};
drug_p53.case_list_id={"-1"};drug_p54.case_list_id={"-1"};drug_p55.case_list_id={"-1"};drug_p56.case_list_id={"-1"};drug_p57.case_list_id={"-1"};
drug_p58.case_list_id={"-1"};drug_p59.case_list_id={"-1"};drug_p60.case_list_id={"-1"};drug_p61.case_list_id={"-1"};
drug_p62.case_list_id={"-1"};drug_p63.case_list_id={"-1"};drug_p64.case_list_id={"-1"};drug_p65.case_list_id={"-1"};
drug_p66.case_list_id={"-1"};drug_p67.case_list_id={"-1"};drug_p68.case_list_id={"-1"};server_url={""};server_url_ip={"null"};""".replace("\n", "")
    }

    def "buildConfigurationBlock should create the parameters for spotfire client for vaccine" () {
        def configBlock = spotfireService.buildConfigurationBlock(
                "{\"${["100696", "100259"].join(",")}\"}",
                Date.parse("dd-MM-yyyy","01-01-1900"), Date.parse("dd-MM-yyyy","31-03-2015"), Date.parse("dd-MM-yyyy","17-01-2015"),-1,"vacc", 10)
        expect:
            configBlock == """vacc_p1.prod_family={"100696,100259"};
vacc_p2.start_date={"01-01-1900"};vacc_p3.end_date={"31-03-2015"};
vacc_p4.as_of_date={"17-01-2015"};vacc_p5.prod_family={"100696,100259"};
vacc_p6.start_date={"01-01-1900"};vacc_p7.end_date={"31-03-2015"};
vacc_p8.as_of_date={"17-01-2015"};vacc_p9.prod_family={"100696,100259"};
vacc_p10.start_date={"01-01-1900"};vacc_p11.end_date={"31-03-2015"};
vacc_p12.as_of_date={"17-01-2015"};vacc_p13.prod_family={"100696,100259"};
vacc_p14.start_date={"01-01-1900"};vacc_p15.end_date={"31-03-2015"};
vacc_p16.as_of_date={"17-01-2015"};vacc_p17.prod_family={"100696,100259"};
vacc_p18.start_date={"01-01-1900"};vacc_p19.end_date={"31-03-2015"};
vacc_p20.as_of_date={"17-01-2015"};vacc_p21.prod_family={"100696,100259"};
vacc_p22.start_date={"01-01-1900"};vacc_p23.end_date={"31-03-2015"};
vacc_p24.as_of_date={"17-01-2015"};vacc_p25.prod_family={"100696,100259"};
vacc_p26.start_date={"01-01-1900"};vacc_p27.end_date={"31-03-2015"};
vacc_p28.as_of_date={"17-01-2015"};vacc_p29.prod_family={"100696,100259"};
vacc_p30.start_date={"01-01-1900"};vacc_p31.end_date={"31-03-2015"};
vacc_p32.as_of_date={"17-01-2015"};vacc_p33.prod_family={"100696,100259"};
vacc_p34.start_date={"01-01-1900"};vacc_p35.end_date={"31-03-2015"};
vacc_p36.as_of_date={"17-01-2015"};vacc_p37.prod_family={"100696,100259"};
vacc_p38.start_date={"01-01-1900"};vacc_p39.end_date={"31-03-2015"};
vacc_p40.as_of_date={"17-01-2015"};vacc_p41.prod_family={"100696,100259"};
vacc_p42.start_date={"01-01-1900"};vacc_p43.end_date={"31-03-2015"};
vacc_p44.as_of_date={"17-01-2015"};vacc_p45.case_list_id={"-1"};vacc_p46.case_list_id={"-1"};vacc_p47.case_list_id={"-1"};
vacc_p48.case_list_id={"-1"};vacc_p49.case_list_id={"-1"};vacc_p50.case_list_id={"-1"};vacc_p51.case_list_id={"-1"};vacc_p52.case_list_id={"-1"};
vacc_p53.case_list_id={"-1"};vacc_p54.case_list_id={"-1"};vacc_p55.case_list_id={"-1"};vacc_p56.case_list_id={"-1"};vacc_p57.case_list_id={"-1"};
vacc_p58.case_list_id={"-1"};vacc_p59.case_list_id={"-1"};vacc_p60.case_list_id={"-1"};vacc_p61.case_list_id={"-1"};
vacc_p62.case_list_id={"-1"};vacc_p63.case_list_id={"-1"};server_url={""};server_url_ip={"null"};""".replace("\n", "")
        }

    def "findFileNameInDatabase should return false once the passing in file name is duplicated - 1" () {
        given:
            spotfireService.metaClass.getReportFiles = {["a", "b", "c"]}

        expect:
            spotfireService.findFileNameInDatabase('a') == true
    }

    def "findFileNameInDatabase should return false once the passing in file name is duplicated - 2" () {
        given:
            spotfireService.metaClass.getReportFiles = {["a", "b", "c"]}

        expect:
            spotfireService.findFileNameInDatabase('k') == false
    }

    def "fileNameExist should return true if the filename can be found in the cache but not in database"() {
        given:
            spotfireService.metaClass.getReportFiles = {["a", "b", "c"]}
            spotfireService.metaClass.findFileNameInCache = {true}

        expect:
            spotfireService.fileNameExist('x') == true
    }

    def "fileNameExist should return false if the filename cannot be found in the cache and in database"() {
        given:
            spotfireService.metaClass.getReportFiles = {["a", "b", "c"]}
            spotfireService.metaClass.findFileNameInCache = {false}

        expect:
            spotfireService.fileNameExist('x') == false
    }
    
    @Unroll
    def "testing appendLingualSuffix"(){
        given: "List of product families"
        LmProductFamily productFamily1 = new LmProductFamily(productFamilyId:1, name: familyName1, lang: "en")
        LmProductFamily productFamily2 = new LmProductFamily(productFamilyId:2, name: familyName2, lang: "en")
        LmProductFamily productFamily3 = new LmProductFamily(productFamilyId:1, name: familyName3, lang: "ja")
        LmProductFamily productFamily4 = new LmProductFamily(productFamilyId:3, name: familyName4, lang: "ja")
        List<LmProductFamily> productFamilies = [productFamily1, productFamily2, productFamily3, productFamily4]

        and: "User Locale"
        String userLocale = locale

        when:
        List results = spotfireService.appendLingualSuffix(productFamilies, userLocale)

        then:
        results == [
                [id: 1, text: resultFamilyName1],
                [id: 2, text: resultFamilyName2],
                [id: 1, text: resultFamilyName3],
                [id: 3, text: resultFamilyName4]
        ]

        where:
        familyName1 | familyName2 | familyName3 | familyName4 | locale || resultFamilyName1 | resultFamilyName2 | resultFamilyName3 | resultFamilyName4
        "family1"   | "family2"   | "family3"   | "family4"   | "en"   || "family1"         | "family2"         | "family3"         | "family4"
        "family1"   | "family2"   | "family1"   | "family4"   | "en"   || "family1"         | "family2"         | "family1 (J)"     | "family4"
        "family1"   | "family2"   | "family3"   | "family4"   | "ja"   || "family1"         | "family2"         | "family3"         | "family4"
        "family3"   | "family2"   | "family3"   | "family4"   | "ja"   || "family3 (E)"     | "family2"         | "family3"         | "family4"
    }
}
*/
