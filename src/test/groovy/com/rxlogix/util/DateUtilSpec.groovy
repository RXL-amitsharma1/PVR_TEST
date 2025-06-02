package com.rxlogix.util

import com.rxlogix.EtlJobService
import com.rxlogix.config.EtlStatus
import com.rxlogix.enums.TimeZoneEnum
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

//@Mock([EtlStatus])
@ConfineMetaClassChanges([MiscUtil])
class DateUtilSpec extends Specification {

    def "Test for getOffset PST "() {
        when:
        Date dt = Date.parse("dd-MM-yyyy HH:mm:ss", "01-01-2015 01:01:01")
        then:
        DateUtil.getOffsetString("America/Los_Angeles", dt) == "-08:00"
    }

    def "Test for getOffset PDT "() {
        when:
        Date dt = Date.parse("dd-MM-yyyy HH:mm:ss", "01-07-2015 01:01:01")
        then:
        DateUtil.getOffsetString("America/Los_Angeles", dt) == "-07:00"
    }

    def "Test for getOffsetString for current date"() {
        expect:
        DateUtil.getOffsetString(code) == result
        where:
        code           || result
        "Etc/GMT-7"    || "+07:00"
        "Etc/GMT-10"   || "+10:00"
        "Etc/GMT+6"    || "-06:00"
        "EST"          || "-05:00"
        "UTC"          || "+00:00"
        "Asia/Karachi" || "+05:00"
    }

    def "Test if all Timezones GMT offsets are calculateable"() {
        when:
        TimeZoneEnum.values().each {
            DateUtil.getOffsetString(it.timezoneId)
        }
        then:
        noExceptionThrown()
    }

    def "Test getStartDate(String dateToDatePicker, def timezone) - BEFORE Daylight Savings Time"() {
        when: "10-Mar-2016 chosen in DatePicker with User Preference Timezone set to NY will result in Mar 10, 05:00:00 UTC"

        Date date = DateUtil.getStartDate("10-Mar-2016", "America/New_York", Locale.ENGLISH)

        DateTime dt = new DateTime(date)
        DateTimeZone dtZone = DateTimeZone.forID("UTC")
        DateTime dtus = dt.withZone(dtZone)

        then:
        assert dtus.toString("MMM dd, HH:mm:ss z") == "Mar 10, 05:00:00 UTC"

    }

    def "Test getStartDate(String dateToDatePicker, def timezone) - AFTER Daylight Savings Time"() {
        when: "20-Mar-2016 chosen in DatePicker with User Preference Timezone set to NY will result in Mar 20, 04:00:00 UTC"

        Date date = DateUtil.getStartDate("20-Mar-2016", "America/New_York", Locale.ENGLISH)

        DateTime dt = new DateTime(date)
        DateTimeZone dtZone = DateTimeZone.forID("UTC")
        DateTime dtus = dt.withZone(dtZone)

        then:
        assert dtus.toString("MMM dd, HH:mm:ss z") == "Mar 20, 04:00:00 UTC"

    }

    def "Test getEndDate(String dateToDatePicker, def timezone) - BEFORE Daylight Savings Time"() {
        when: "10-Mar-2016 chosen in DatePicker with User Preference Timezone set to NY will result in Mar 10, 04:59:59 UTC"

        Date date = DateUtil.getEndDate("10-Mar-2016", "America/New_York", Locale.ENGLISH)

        DateTime dt = new DateTime(date)
        DateTimeZone dtZone = DateTimeZone.forID("UTC")
        DateTime dtus = dt.withZone(dtZone)

        then:
        assert dtus.toString("MMM dd, HH:mm:ss z") == "Mar 11, 04:59:59 UTC"

    }

    def "Test getEndDate(String dateToDatePicker, def timezone) - AFTER Daylight Savings Time"() {
        when: "20-Mar-2016 chosen in DatePicker with User Preference Timezone set to NY will result in Mar 21, 03:59:59 UTC"

        Date date = DateUtil.getEndDate("20-Mar-2016", "America/New_York", Locale.ENGLISH)

        DateTime dt = new DateTime(date)
        DateTimeZone dtZone = DateTimeZone.forID("UTC")
        DateTime dtus = dt.withZone(dtZone)

        then:
        assert dtus.toString("MMM dd, HH:mm:ss z") == "Mar 21, 03:59:59 UTC"

    }

    def "Test getStartDate()" () {
        when: "10-Mar-2016 chosen in DatePicker with no Timezone set will result in Mar 10, 00:00:00 UTC"

        Date date = DateUtil.getStartDate("10-Mar-2016", Locale.ENGLISH)

        DateTime dt = new DateTime(date)
        DateTimeZone dtZone = DateTimeZone.forID("UTC")
        DateTime dtus = dt.withZone(dtZone)

        then:
        assert dtus.toString("MMM dd, HH:mm:ss z") == "Mar 10, 00:00:00 UTC"

    }

    def "Test getEndDate()" () {
        when: "20-Mar-2016 chosen in DatePicker with no Timezone set will result in Mar 20, 23:59:59 UTC"

        Date date = DateUtil.getEndDate("20-Mar-2016", Locale.ENGLISH)

        DateTime dt = new DateTime(date)
        DateTimeZone dtZone = DateTimeZone.forID("UTC")
        DateTime dtus = dt.withZone(dtZone)

        then:
        assert dtus.toString("MMM dd, HH:mm:ss z") == "Mar 20, 23:59:59 UTC"

    }


    def "Test start day time of date" () {
        when: "Date with time 20-Mar-2016 15:05:05 passed to method getDateWithDayStartTime"

        Date date = Date.parse("dd-MMM-yyyy hh:mm:ss","20-Mar-2016 15:05:05")

        Date dtus = DateUtil.getDateWithDayStartTime(date)

        then:
        assert dtus.format("MMM dd yyyy, HH:mm:ss") == "Mar 20 2016, 00:00:00"

    }

    def "Test end day time of date" () {
        when: "Date with time 20-Mar-2016 15:05:05 passed to method getDateWithDayEndTime"

        Date date = Date.parse("dd-MMM-yyyy hh:mm:ss","20-Mar-2016 15:05:05")

        Date dtus = DateUtil.getDateWithDayEndTime(date)

        then:
        assert dtus.format("MMM dd yyyy, HH:mm:ss") == "Mar 20 2016, 23:59:59"
    }

    def "Test getFormattedDateForLastSuccessfulEtlRun"() {
        given:
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "dd-MMM-yyyy hh:mm:ss a z" }
        def etlJobServiceMock=Mock(EtlJobService)
        etlJobServiceMock.getEtlStatus() >> {return [lastRunDateTime: new Date("Mon Oct 18 07:21:22 PM UTC 2021")]}
        MiscUtil.metaClass.static.getBean = {String s -> etlJobServiceMock}
        String timeZone = "IST"
        when:
        def formattedDateValue = DateUtil.getFormattedDateForLastSuccessfulEtlRun(timeZone, new Date("Mon Oct 18 07:21:22 PM UTC 2021"))
        then:
        formattedDateValue.contains("19-Oct-2021")
        formattedDateValue.contains("12:51:22 am")
    }

    def "Test to check offset during DST shift"(){
        when:
        Date beforeDstShfit = Date.parse("dd-MM-yyyy HH:mm:ss", "01-11-2022 01:01:01")
        Date afterDstShfit  = Date.parse("dd-MM-yyyy HH:mm:ss", "07-11-2022 01:01:01")
        then:
        DateUtil.getOffsetString("America/Los_Angeles", beforeDstShfit)!=DateUtil.getOffsetString("America/Los_Angeles", afterDstShfit)

    }

}
