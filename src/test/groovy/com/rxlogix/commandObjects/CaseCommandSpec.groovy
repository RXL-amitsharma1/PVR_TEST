package com.rxlogix.commandObjects

import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class CaseCommandSpec extends Specification {

    def "Test for validating file for case number"() {
        given:
        CaseCommand caseCommand = new CaseCommand()
        File reportFile = File.createTempFile("temp", "")
//        MultipartFile multipartFile = new MockMultipartFile("mainExcelFileName.xlsx", new FileInputStream(new File("/home/sargam/mainExcelFileName.xlsx")))
        caseCommand.file = reportFile as MultipartFile
        caseCommand.caseNumber =  caseNumber
        expect:
        caseCommand.validate(['file']) == validated
        where:
        caseNumber        ||  validated
        "US1001243775"    ||  true
        "AB1001243775"    ||  true
    }

    def "Test for validating null file for case number"() {
        given:
        CaseCommand caseCommand = new CaseCommand()
        caseCommand.file = null
        caseCommand.caseNumber =  caseNumber
        expect:
        caseCommand.validate(['file']) == validated
        where:
        caseNumber        ||  validated
        null              ||  false
        "AB1001243775"    ||  true
    }

    void 'test justification cannot be null'() {
        when:
        CaseCommand caseCommand = new CaseCommand()
        caseCommand.justification = null

        then:
        !caseCommand.validate(['justification'])
        caseCommand.errors['justification'].code == 'nullable'
    }

    void 'test caseNumber can be null'() {
        when:
        CaseCommand caseCommand = new CaseCommand()
        caseCommand.caseNumber = null
        then:
        caseCommand.validate(['caseNumber'])
    }

    void 'test versionNumber can be null'() {
        when:
        CaseCommand caseCommand = new CaseCommand()
        caseCommand.versionNumber = null
        then:
        caseCommand.validate(['versionNumber'])
    }

    void 'test caseNumber can be blank'() {
        when:
        CaseCommand caseCommand = new CaseCommand()
        caseCommand.caseNumber = ''
        then:
        caseCommand.validate(['caseNumber'])
    }

    void 'test versionNumber can be blank'() {
        when:
        CaseCommand caseCommand = new CaseCommand()
        caseCommand.versionNumber = ''
        then:
        caseCommand.validate(['versionNumber'])
    }
}
