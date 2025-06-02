package com.rxlogix

import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.EtlSchedule
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
class EtlScheduleSpec extends Specification implements DomainUnitTest<EtlSchedule> {

    def setup() {
    }

    def cleanup() {
    }

    void "test create schedule"() {
        given:
        def emailConfiguration = new EmailConfiguration(cc: 'admin@rxlogix.com', subject : "[pv-reports] - subject for etl", body : "pv-reports sample test")
        def eTLSchedule = new EtlSchedule(scheduleName: "ETL", startDateTime: "20115-03-31T03:23+02:00", isInitial: false, repeatInterval: "FREQ=MONTHLY;INTERVAL=6;BYDAY=WE;BYSETPOS=3;UNTIL=20140919;", emailToUsers: 'pvreports-app@rxlogix.com', emailConfiguration : emailConfiguration, sendSuccessEmail: true, pauseLongRunningETL: true, createdBy: "bootstrap", modifiedBy: "bootstrap")
        eTLSchedule.save();

        when:
        def eTLScheduleInstance = EtlSchedule.findByScheduleName("ETL")

        then:
        eTLScheduleInstance.scheduleName.equals("ETL")
        eTLScheduleInstance.id == 1
        eTLScheduleInstance.sendSuccessEmail == true
        eTLScheduleInstance.emailToUsers == 'pvreports-app@rxlogix.com'
        eTLScheduleInstance.emailConfiguration.cc == 'admin@rxlogix.com'
    }
}
