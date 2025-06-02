package com.rxlogix.config.metadata

import com.rxlogix.config.QualityField
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class QualityFieldSpec extends Specification implements DomainUnitTest<QualityField> {

    QualityField qualityField

    def createNewQualityField() {
        qualityField = new QualityField(id: 8L, qualityModule: 'testQualityFieldModule',
                fieldName: 'testFieldName', fieldType: 'testFieldType')
    }

    def setup() {
        QualityField qualityField2 = new QualityField(id: 1L, qualityModule: 'testQualityFieldModule',
                fieldName: 'testFieldName', fieldType: 'testFieldType')
        qualityField2.save(failOnError:true)
    }

    def cleanup() {
    }

    void "test qualityModule cannot be null"() {
        given:
            createNewQualityField()
        when:"qualityModule equals value"
            qualityField.qualityModule=value
        then:
            qualityField.validate()==result

        where:
            value                | result
            null                 | false
            ''                   | true
            'testQualityModule1' | true
    }
    void "test fieldName cannot be null"() {
        given:
            createNewQualityField()
        when:"fieldName equals value"
            qualityField.fieldName=value
        then:
            qualityField.validate()==result

        where:
            value           | result
            null            | false
            ''              | true
            'testFieldName' | true
    }
    void "test fieldType cannot be null"() {
        given:
            createNewQualityField()
        when:"fieldType equals value"
            qualityField.fieldType=value
        then:
            qualityField.validate()==result

        where:
            value           | result
            null            | false
            ''              | true
            'testFieldType' | true
    }
    void "test isReportIdExists"(){
        given:
            createNewQualityField()
            qualityField.reportIds<<32L
            Long reportId =value
        when:
            boolean result=qualityField.isReportIdExists(reportId)
        then:
            println(qualityField.reportIds)
            result==expectedResult
        where:
            value | expectedResult
            40L   | false
            32L   | true
            null  | false

    }
}
