package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.enums.*
import com.rxlogix.pvdictionary.config.DictionaryConfig
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.test.TestUtils
import com.rxlogix.user.*
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.transform.recurrence.Frequency
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTimeZone
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.text.DateFormat
import java.text.SimpleDateFormat

@ConfineMetaClassChanges([ReportTemplate, SuperQuery, DateUtil, CaseLineListingTemplate, ViewHelper, Configuration, DictionaryGroup])
class ConfigurationServiceSpec extends Specification implements DataTest, ServiceUnitTest<ConfigurationService> {

    private static final String SIMPLE_DATE = "yyyy-MM-dd HH:mm"
    public static final user = "unitTest"
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    @Shared caseMasterTable
    @Shared lmCountriesTable
    @Shared caseMasterColumnCountry
    @Shared caseInformationRFG
    @Shared countryOfIncidenceRF

    /**
     * Generally speaking the recurrence JSON string ignores the timeZone object; it uses the timezone in the passed in startDateTime
     *
     * Use -Duser.timezone=GMT to force tests to run in a different timezone
     * use --echoOut for force println output into standard out
     */
    void setup() {
    }

    void setupSpec() {
        mockDomains TemplateQuery,Configuration, DictionaryGroup, PeriodicReportConfiguration, DeliveryOption, User, UserGroup, Role, UserRole, Query, ReportField, ReportFieldGroup, SourceTableMaster, SourceColumnMaster, QueryExpressionValue, Tenant, CaseLineListingTemplate,DeliveryOption, ReportFieldInfo, ReportFieldInfoList,SourceProfile, IcsrReportConfiguration, ParameterValue, SuperQuery, ReportField, GlobalDateRangeInformation, QueryValueList, ExecutedTemplateQuery, ExecutedDateRangeInformation, ExecutedConfiguration, ExecutedCaseLineListingTemplate, ExecutedGlobalDateRangeInformation
        // force the tests to run in the TEST_TZ
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
        buildReportFields()
        mockTemplateServiceForReportTemple()
    }

    def cleanupSpec() {
        cleanUpTemplateServiceMock()
        // set the TZ back to what it was
        TimeZone.setDefault(ORIGINAL_TZ)
    }

    private void mockTemplateServiceForReportTemple() {
        def templateService = new MockFor(TemplateService).proxyInstance()
        ReportTemplate.metaClass.getTemplateService = {
            return templateService
        }

        CaseLineListingTemplate.metaClass.getTemplateService = {
            return templateService
        }
    }

    private void cleanUpTemplateServiceMock() {
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }


    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"))
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private User makeNormalUser(name, team, String email = null) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_DEV', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user",email: email?:"abc@gmail.com")
        normalUser.addToTenants(tenant)
        normalUser.save(validate: false)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        return normalUser
    }

    void buildReportFields() {
        caseMasterTable = new SourceTableMaster(tableName: "V_C_IDENTIFICATION", tableAlias: "cm", tableType: "C", caseJoinOrder: 1)
        lmCountriesTable = new SourceTableMaster(tableName: "VW_LCO_COUNTRY", tableAlias: "lco", tableType: "L", caseJoinOrder: null)
        caseMasterColumnCountry = new SourceColumnMaster(tableName: caseMasterTable, columnName: "COUNTRY_ID",
                primaryKey: null, lmTableName: lmCountriesTable, lmJoinColumn: "COUNTRY_ID",
                lmDecodeColumn: "COUNTRY", columnType: "N", reportItem: "CM_COUNTRY_ID", lang:"en" )
        caseInformationRFG = new ReportFieldGroup(name: "Case Information")

        //Purposely leaving out listDomainClass
        countryOfIncidenceRF = new ReportField(name: "masterCountryId",
                fieldGroup: caseInformationRFG, sourceColumnId: caseMasterColumnCountry.reportItem,
                dataType: String.class,sourceId: 1)
    }

    void saveReportFields() {
        caseMasterTable.save(failOnError: true)
        lmCountriesTable.save(failOnError: true)
        caseMasterColumnCountry.save(failOnError: true)
        caseInformationRFG.save(failOnError: true)
        countryOfIncidenceRF.save(failOnError: true)
    }

    private toJSON(Date date, String timezoneId) {
        DateFormat df = new SimpleDateFormat(DateUtil.JSON_DATE)
        df.setTimeZone(TimeZone.getTimeZone(timezoneId))
        df.format(date)
    }

    private Date toDate(String simpleDate) {
        Date.parse(SIMPLE_DATE, simpleDate)
    }

    // Use this method to help debug time/date & timeZone issues, have to use --echoOut for force println output into standard out on console
    private void debugSummary(Date startTime, Date next, Date expectedRecurrence, String recurrenceJSON) {
        println "Summary TimeZone: ${TimeZone.default.ID}, startTime: ${startTime}, next: ${next}, expectedRecurrence: ${expectedRecurrence}"
        println "Summary recurrenceJSON: ${recurrenceJSON}"
        println "Summary times as long startTime: ${startTime.time}, next: ${next.time}, expectedRecurrence: ${expectedRecurrence.time}"
    }


    void "No scheduler"() {
        given: "A configuration with no scheduled date"
        def config = new Configuration()

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "No start time in scheduler"() {
        given: "A configuration"

        when: "We try to get the recurring date with a null startDateTime"
        def recurrenceJSON = /{"timeZone":{"name":"America\/Los_Angeles","offset":"${DateUtil.getOffsetString("America/Los_Angeles")}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "No time zone in scheduler"() {
        given: "Start date is now"
        def now = new Date()
        when: "We try to get the recurring date with a null timeZone"
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "No recurrence pattern in scheduler"() {
        given: "Start date is now"
        def now = new Date()

        when: "We try to get the recurring date with a null recurrencePattern"
        def recurrenceJSON = /{"startDateTime":"$now","timeZone":{"name":"America\/Los_Angeles","offset":"${DateUtil.getOffsetString("America/Los_Angeles")}"}}/
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "Recurrence returns a null date after recurring x number of times"() {
        given: "A configuration which recurs daily multiple times from 7 days ago"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(new Date() - 7,"Etc/GMT+7")
                }","timeZone":{"name":"Etc/GMT-7","offset":"-07:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=$count;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date after we have recurred x number of times"
        Date next = service.getNextDate(config)
        int recurrences = -1
        while (next != null) {
            next = service.getNextDate(config)
            config.nextRunDate = next
            recurrences++
        }

        then: "Next run date is null"
        recurrences == count
        next == null

        where:
        count << [2, 3, 5, 10, 15]
    }

    void "Recurrence occurs until the end date, excluding the end date unless it is the same day as the start date"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+0:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Date next
        for (int i = 0; i < 2; i++) {
            next = service.getNextDate(config)
            config.nextRunDate = next
        }

        then: "Next run date matches the recurrence rule until the end date"
        Calendar cal = Calendar.getInstance()
        Date expectedDate
        if (next) {
            cal.setTime(startTime)
            cal.add(Calendar.DAY_OF_YEAR, interval)
            expectedDate = cal.getTime()
        }
        next == expectedDate

        where:
        // Add frequency as another parameter to test
        startTime << [toDate("2014-08-24 21:30"), toDate("2014-08-24 21:30"), toDate("2014-04-24 21:30")]
        endDate << ["20140829", "20140825", "20140510"]
        interval << [3, 3, 10]
        frequency << ["DAILY", "DAILY", "DAILY"]
    }

    void "Run once 10 minutes from now"() {
        given: "A configuration with scheduled run once 10 mins from now"
        def now = new Date()
        def tenMinutesFromNow = new Date()
        tenMinutesFromNow.clearTime()
        use(TimeCategory) {
            tenMinutesFromNow += now.hours.hours + now.minutes.minutes + 10.minutes
        }
        def recurrenceJSON = """{"startDateTime":"${ toJSON(tenMinutesFromNow,"UTC")
                }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is 10 minutes from now"
        next.after(now)
        next == tenMinutesFromNow
    }


    void "PVR-131: Schedule Recurrence: None (only once) -- test PAST dates"() {
        given: "A configuration with no recurrence (run once)"
        Calendar cal = Calendar.getInstance()
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.SECOND, 0)
        Date today = cal.getTime()
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+0:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=$interval;COUNT=1;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date in the past"
        Date next = service.getNextDate(config)
        cal.setTime(next)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.SECOND, 0)
        next = cal.getTime()

        then: "Next run date is today"
        next == today

        where:
        // generate dates which have no seconds/millis
        startTime << [toDate((new Date() - 1).format(SIMPLE_DATE)),
                      toDate((new Date() - 10).format(SIMPLE_DATE)),
                      toDate((new Date() - 1000).format(SIMPLE_DATE))]
        // In this case, interval is ignored.
        interval << [1, 10, 10000]
    }

    /**
     *  CLEANED UP BELOW THIS COMMENT
     */

    void "PVR-131: Schedule Recurrence: None (only once) -- test FUTURE dates"() {
        given: "A configuration with no recurrence (run once)"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=$interval;COUNT=1;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date in the future (ignore the seconds)"
        Date next = service.getNextDate(config)

        then: "Next run date is the start date"
        next == startTime

        where:
        // generate dates which have no seconds/millis
        startTime << [toDate((new Date() + 1).format(SIMPLE_DATE)),
                      toDate((new Date() + 10).format(SIMPLE_DATE)),
                      toDate((new Date() + 1000).format(SIMPLE_DATE))]
        // In this case, interval is ignored.
        interval << [5, 6, 7]
    }

    void "PVR-132: Schedule Recurrence: Hourly -- test PAST dates"() {
        given: "A configuration which runs hourly with start date 10 minutes before"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=HOURLY;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next
        for (int i = 0; i < 2; i++) {
            next = service.getNextDate(config)
            config.nextRunDate = next
        }
        Calendar cal = Calendar.getInstance()
        cal.setTime(startTime)
        cal.add(Calendar.HOUR, interval)
        Date recurDate = cal.getTime()

        then: "Next run date is an hour later than start time"
        next.after(startTime)
        next == recurDate

        where:
        startTime << [toDate("2014-08-24 21:30"), toDate("2014-02-24 21:30"), toDate("2014-02-24 23:30")]
        interval << [1, 2, 3]
    }

    void "PVR-132: Schedule Recurrence: Hourly -- test FUTURE dates"() {
        given: "A configuration which runs hourly with future start date"
        def now = new Date()
        startTime.clearTime()
        use(TimeCategory) {
            startTime += now.hours.hours + now.minutes.minutes + 1000.minutes
        }

        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=HOURLY;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is start time"
        next == startTime

        where:
        startTime << [new Date() + 1, new Date() + 2, new Date() + 10, new Date() + 100, new Date() + 1000]
        interval << [1, 2, 5, 10, 100]
    }

    void "PVR-134: Schedule Recurrence: Weekdays"() {
        given: "A configuration which runs on weekdays"
        Calendar cal = Calendar.getInstance() // locale-specific
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
            }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        cal.setTime(next)

        then: "Next run date is next weekday (No Saturday or Sunday)"
        cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
        cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
        // Should compare the actual date

        where:
        startTime << [toDate("2014-08-23 21:30"), toDate("2014-12-28 21:30"),
                      (new Date() - 10),
                      (new Date() - 1),
                      (new Date() + 1),
                      (new Date() + 100),
                      (new Date() + 1000)]
        interval << [1, 2, 3, 4, 5, 10, 100]
    }

    void "PVR-138: Schedule Recurrence: Weekly"() {
        given: "A configuration which runs on every Tuesday"
        Calendar cal = Calendar.getInstance() // locale-specific
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+0:00"},"recurrencePattern":"FREQ=WEEKLY;BYDAY=TU;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        cal.setTime(next)

        then: "Next run date is next Tuesday"
        cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY
        // Can compare the next run date

        where:
        startTime << [toDate("2014-08-23 21:30"), toDate("2014-09-05 21:30"),
                      (new Date() - 10),
                      (new Date() - 1),
                      (new Date() + 1),
                      (new Date() + 100),
                      (new Date() + 1000)]
        interval << [1, 2, 3, 4, 5, 10, 100]
    }

    void "PVR-139: Schedule Recurrence: Monthly by day of month"() {
        given: "A configuration which runs monthly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+07:00"},"recurrencePattern":"FREQ=MONTHLY;INTERVAL=$interval;BYMONTHDAY=$onDay;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the next available month that has this day"

        debugSummary(startTime, next, expectedRecurrence, recurrenceJSON)
        next == expectedRecurrence

        where: "There are different start times"
        //First date accounts for daylights saving time
        startTime << [toDate("2014-02-23 21:30"), toDate("2014-09-05 21:30")]
        expectedRecurrence << [toDate("2014-03-30 21:30"), toDate("2014-09-05 21:30")]
        onDay << [30, 5]
        interval << [1, 2]
    }

    void "PVR-139: Schedule Recurrence: Monthly -- test BYSETPOS"() {
        given: "A configuration which runs monthly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=MONTHLY;INTERVAL=1;BYDAY=$onDay;BYSETPOS=$pos;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the first day of month"
        next == expectedRecurrence

        where:
        startTime << [toDate("2014-02-23 21:30"), toDate("2014-09-15 21:30")]
        expectedRecurrence << [toDate("2014-03-1 21:30"), toDate("2014-09-21 21:30")]
        onDay << ["SA", "SU"]
        pos << [1, 3]
    }

    void "PVR-140: Schedule Recurrence: Yearly -- test BYMONTHDAY"() {
        given: "A configuration which runs yearly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+0:00"},"recurrencePattern":"FREQ=YEARLY;BYMONTH=$onMonth;BYMONTHDAY=$onDay;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the date in recurrence pattern"
        next == expectedRecurrence

        where:
        startTime << [toDate("2014-02-03 21:30"), toDate("2014-09-15 21:30"), toDate("2014-09-15 21:30")]
        onMonth << [6, 2, 1]
        onDay << [22, 30, 1]
        expectedRecurrence << [toDate("2014-06-22 21:30"), null, toDate("2015-01-01 21:30")]
    }

    void "PVR-140: Schedule Recurrence: Yearly -- test BYSETPOS"() {
        given: "A configuration which runs yearly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
                }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=YEARLY;BYDAY=$onDay;BYSETPOS=$pos;BYMONTH=$onMonth"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the first day of month"
        next == expectedRecurrence

        where:
        startTime << [toDate("2014-05-23 21:30"), toDate("2014-09-15 21:30")]
        expectedRecurrence << [toDate("2014-09-06 21:30"), toDate("2015-01-18 21:30")]
        pos << [1, 3]
        onDay << ["SA", "SU"]
        onMonth << [9, 1]
    }

    void "PVR-2824: Schedule Recurrence: Daily, End on Date includes the last day"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 3; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << [toDate("2016-06-08 21:30"), toDate("2016-06-08 21:30"), toDate("2016-06-08 21:30")]
        endDate << ["20160608", "20160609", "20160610"]
        interval << [1, 1, 1]
        frequency << [Frequency.DAILY, Frequency.DAILY, Frequency.DAILY]
        expectedRecurrenceTimes << [1, 2, 3]
    }

    void "PVR-2909: Schedule Recurrence: Hourly, End on Date does not extend past End on Date"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 30; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << [toDate("2016-06-08 22:30"), toDate("2016-06-08 23:30"), toDate("2016-06-09 00:30"), toDate("2016-06-09 22:30")]
        endDate << ["20160609", "20160609", "20160609", "20160609"]
        interval << [1, 1, 1, 1]
        frequency << [Frequency.HOURLY, Frequency.HOURLY, Frequency.HOURLY, Frequency.HOURLY]
        expectedRecurrenceTimes << [26, 25, 24, 2]
    }

    void "PVR-22796: Schedule Recurrence: minutely"() {
        given: "A configuration which runs each 5 minutes 3 times"
        def recurrenceJSON = """{"startDateTime":"2022-07-26T08:00Z","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=MINUTELY;INTERVAL=5;COUNT=3"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set<Date> dates = []
        for (int i = 0; i < 30; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }
        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == 3
        dates[0].format("mm")=="00"
        dates[1].format("mm")=="05"
        dates[2].format("mm")=="10"
    }

    void "PVR-2909: Schedule Recurrence: Weekly, End on Date includes the last day"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;BYDAY=$day;INTERVAL=$interval;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 3; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << [toDate("2016-06-03 22:30"), toDate("2016-06-03 22:30"), toDate("2016-06-03 22:30")]
        endDate << ["20160609", "20160610", "20160611"]
        interval << [1, 1, 1]
        frequency << [Frequency.WEEKLY, Frequency.WEEKLY, Frequency.WEEKLY]
        day << ["FR", "FR", "FR"]
        expectedRecurrenceTimes << [1, 2, 2]
    }

    void "PVR-2909: Schedule Recurrence: Monthly on specific month day, End on Date includes the last day"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;BYMONTHDAY=$monthDay;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 3; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << [toDate("2016-05-01 21:30"), toDate("2016-05-01 21:30"), toDate("2016-05-01 21:30")]
        endDate << ["20160531", "20160601", "20160602"]
        interval << [1, 1, 1]
        frequency << [Frequency.MONTHLY, Frequency.MONTHLY, Frequency.MONTHLY]
        monthDay << [1, 1, 1]
        expectedRecurrenceTimes << [1, 2, 2]
    }

    void "PVR-2909: Schedule Recurrence: Monthly on every weekday, End on Date includes the last day"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;BYDAY=$day;BYSETPOS=$weekday;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 3; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << [toDate("2016-05-01 21:30"), toDate("2016-05-01 21:30"), toDate("2016-05-01 21:30")]
        endDate << ["20160604", "20160605", "20160606"]
        interval << [1, 1, 1]
        frequency << [Frequency.MONTHLY, Frequency.MONTHLY, Frequency.MONTHLY]
        day << ["SU", "SU", "SU"]
        weekday << [1, 1, 1]
        expectedRecurrenceTimes << [1, 2, 2]
    }

    void "PVR-2909: Schedule Recurrence: Yearly on specific month and day, End on Date includes the last day"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${startTime}-07:00","timeZone":{"name":"Etc/GMT+7","offset":"-07:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;BYMONTH=$month;BYMONTHDAY=$monthDay;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 3; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << ["2015-06-01T21:30", "2015-06-01T21:30", "2015-06-01T21:30"]
        endDate << ["20160531", "20170601", "20170602"]
        frequency << [Frequency.YEARLY, Frequency.YEARLY, Frequency.YEARLY]
        interval << [1, 2, 2]
        month << [6, 6, 6]
        monthDay << [1, 1, 1]
        expectedRecurrenceTimes << [1, 2, 2]
    }

    void "PVR-2909: Schedule Recurrence: Yearly on weekday of month, End on Date includes the last day"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;BYDAY=$day;BYSETPOS=$weekday;BYMONTH=$month;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        for (int i = 0; i < 3; i++) {
            Date next = service.getNextDate(config)
            config.nextRunDate = next
            if (next) {
                dates.add(next)
            }
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrenceTimes

        where:
        startTime << [toDate("2015-06-07 21:30"), toDate("2015-06-07 21:30"), toDate("2015-06-07 21:30")]
        endDate << ["20160604", "20160605", "20160606"]
        frequency << [Frequency.MONTHLY, Frequency.MONTHLY, Frequency.MONTHLY]
        interval << [1, 1, 1]
        day << ["SU", "SU", "SU"]
        weekday << [1, 1, 1]
        month << [6, 6, 6]
        expectedRecurrenceTimes << [1, 2, 2]
    }

    void "PVR-12726: Schedule Recurrence: Monthly on last day of month, include February month"() {
        given: "A configuration which runs monthly until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;BYDAY=SU,MO,TU,WE,TH,FR,SA;BYSETPOS=-1;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        Date next = service.getNextDate(config)
        while (next) {
            dates.add(next)
            config.nextRunDate = next
            next = service.getNextDate(config)
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrence.size()
        for(int i=0; i<dates.size();i++) {
            assert dates[i]==expectedRecurrence[i]
        }

        where:
        startTime << [toDate("2020-01-30 21:30")]
        endDate << ["20201231"]
        frequency << [Frequency.MONTHLY]
        interval << [1]
        expectedRecurrence << [[toDate("2020-01-31 21:30"), toDate("2020-02-29 21:30"),
                               toDate("2020-03-31 21:30"),toDate("2020-04-30 21:30"),
                               toDate("2020-05-31 21:30"),toDate("2020-06-30 21:30"),
                               toDate("2020-07-31 21:30"),toDate("2020-08-31 21:30"),
                               toDate("2020-09-30 21:30"),toDate("2020-10-31 21:30"),
                               toDate("2020-11-30 21:30"),toDate("2020-12-31 21:30")]]
    }

    void "PVR-12726: Schedule Recurrence: Yearly on last day of February start on 29th of leap year, include all February"() {
        given: "A configuration which runs yearly until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime,"UTC")
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;BYDAY=SU,MO,TU,WE,TH,FR,SA;BYSETPOS=-1;BYMONTH=2;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Set dates = []
        Date next = service.getNextDate(config)
        while (next) {
            dates.add(next)
            config.nextRunDate = next
            next = service.getNextDate(config)
        }

        then: "Next run date matches the recurrence rule until the end date"
        dates.size() == expectedRecurrence.size()
        for(int i=0; i<dates.size();i++) {
            assert dates[i]==expectedRecurrence[i]
        }

        where:
        startTime << [toDate("2020-02-29 21:30")]
        endDate << ["20250228"]
        frequency << [Frequency.YEARLY]
        interval << [1]
        expectedRecurrence << [[toDate("2020-02-29 21:30"), toDate("2021-02-28 21:30"),
                                toDate("2022-02-28 21:30"),toDate("2023-02-28 21:30"),
                                toDate("2024-02-29 21:30"),toDate("2025-02-28 21:30")]]
    }

    void "PVR-19453 Issue with DST change day report execution"(){
        given: "A configuration which runs on DST change day"
        def recurrenceJSON = '''{"startDateTime":"2021-11-07T22:49-05:00","timeZone":{"text":"(GMT -05:00) EST5EDT","selected":true,"offset":"-05:00","name":"EST5EDT"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}'''
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        then: "Next run date should be current date"
        next <= new Date()
    }

    void "PVR-19456 Issue with Skip current execution if the day is same as first occurrence"(){
        given: "A configuration which runs on first day of every month starting 01st jan 2022"
        def recurrenceJSON = '''{"startDateTime":"2022-01-01T07:30-05:00","timeZone":{"text":"(GMT -05:00) Eastern Time (US and Canada)","selected":true,"offset":"-05:00","name":"America/New_York"},"recurrencePattern":"FREQ=MONTHLY;INTERVAL=3;BYMONTHDAY=1"}'''
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        then: "Next run date should be current date"
        next.format('yyyy-MM-dd') == '2022-01-01'
    }

    void "Get query expression values from Query with blanks"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A query with blank values"
        saveReportFields()
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, JSONQuery: "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"masterCountryId\",\"op\":\"EQUALS\",\"value\":\"\",\"key\":\"1\"} ] }  ] } ,\"blankParameters\":[{\"key\":1,\"field\":\"masterCountryId\",\"op\":\"EQUALS\",\"value\":\"\"}]}",
                                 name: 'TEST QUERY: Blank values', description: 'Query with blank values', createdBy: adminUser.username, modifiedBy: adminUser.username])
        query.addToQueryExpressionValues(new QueryExpressionValue(key: 1, reportField: ReportField.findByName("masterCountryId"), operator: QueryOperatorEnum.EQUALS, value: ''))
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        when: "We retrieve parameters from the query"
        Set<QueryExpressionValue> result = query.queryExpressionValues
        then: "List of query expression values are returned"
        result.size() == 1
    }

    void "Configuration with no blank values running setBlankValues will not set blank values"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A query without blank values"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, JSONQuery: "{ \"all\": { \"containerGroups\": [   { \"expressions\": [  { \"index\": \"0\", \"field\": \"masterCountryId\", \"op\": \"EQUALS\", \"value\": \"UNITED STATES\" }  ] }  ] } }",
                                 name: 'TEST QUERY: Country = US', description: 'No blank values', createdBy: adminUser.username, modifiedBy: adminUser.username])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        and: "A configuration which uses the above query"
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(failOnError: true)
        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "V_C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        sourceTableMaster.save(failOnError: true)
        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V", lang: "en"])
        sourceColumnMaster.save(failOnError: true)
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", sourceColumnId: sourceColumnMaster.reportItem, fieldGroup: fieldGroup, dataType: String.class, sourceId: 0])
        field.save(failOnError: true)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        def templateQuery  = new TemplateQuery(template: new CaseLineListingTemplate(),dateRangeInformationForTemplateQuery: new DateRangeInformation(), query:query, createdBy: adminUser.username, modifiedBy: adminUser.username)
        Configuration config = new Configuration([template: template,  reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', tenantId:1L, owner: adminUser, isEnabled: true, createdBy: adminUser.username, modifiedBy: adminUser.username])
        config.deliveryOption = new DeliveryOption(report: config)
        config.deliveryOption.sharedWith = [adminUser]
        config.deliveryOption.attachmentFormats = [ReportFormatEnum.PDF]
        config.deliveryOption.emailToUsers = ['pvreports@rxlogix.com']
        config.addToTemplateQueries(templateQuery)
        config.sourceProfile = TestUtils.createSourceProfile()
        config.save(failOnError: true)

        when: "Blank values are retrieved"
        List<ParameterValue> parameterValues = []
        config.templateQueries.each {
            it.queryValueLists.each {
                parameterValues.addAll(it.parameterValues)
            }
            it.templateValueLists.each {
                parameterValues.addAll(it.parameterValues)
            }
        }

        then: "No blank values are returned"
        parameterValues.isEmpty() == true
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        return userMock.proxyInstance()
    }

    void "test data from received froms session in fetchConfigurationMapFromSession"() {
        given:
        SessionMock sess = [editingConfiguration: [configurationParams: (new JsonBuilder([reportName: "report1"] )).toString(), templateId: 1, templateQueryIndex: "0"]]

        when:
        def result = service.fetchConfigurationMapFromSession([continueEditing: true], sess)

        then:
        result.configurationParams.reportName == "report1"

    }

    static class SessionMock extends LinkedHashMap {
        public void removeAttribute(String s) {}
    }

    private User createUser(String username, String role) {
        def userRole = new Role(authority: role, createdBy: username, modifiedBy: username).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Normal User", preference: new Preference(locale: new Locale("en"), createdBy: username, modifiedBy: username), createdBy: username, modifiedBy: username)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    void "test for getDateRangeValueForCriteriaWithoutString"() {
        given:
        def config = new ExecutedConfiguration(
                executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), sourceProfile: new SourceProfile(sourceName: "Test"), lastRunDate: new Date(), owner: createUser("Application", "ROLE_ADMIN"), createdBy: "Application", modifiedBy: "Application", locale: new Locale("en"))
        def executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: new ExecutedCaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(argusName: "test1", reportField: new ReportField(name: "CASE_NUMBER"))])), executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(dateRangeStartAbsolute: new Date(), dateRangeEndAbsolute: new Date(), executedAsOfVersionDate: new Date()), executedConfiguration: config, createdBy: "Application", modifiedBy: "Application")
        def locale = executedTemplateQuery.executedConfiguration.locale
        DateUtil.metaClass.static.getShortDateFormatForLocale={Locale locale1-> return "dd-MMM-yyyy"}
        when:
        def result = service.getDateRangeValueForCriteriaWithoutString(executedTemplateQuery, locale)
        then:
        result == ["Cumulative", DateUtil.StringFromDate(new Date(), "dd-MMM-yyyy", null)]
    }


    void "test for replaceStringWithDate"() {
        given:
        Locale locale = new Locale('en')
        def config = new ExecutedConfiguration(
                executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), sourceProfile: new SourceProfile(sourceName: "Test"), lastRunDate: new Date(), owner: createUser("Application", "ROLE_ADMIN"), createdBy: "Application", modifiedBy: "Application", locale: new Locale("en"))
        def executedTemplateQuery = new ExecutedTemplateQuery(id: 1,  executedTemplate: new ExecutedCaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(argusName: "test1", reportField: new ReportField(name: "CASE_NUMBER"))])), executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(dateRangeStartAbsolute: new Date(), dateRangeEndAbsolute: new Date(), executedAsOfVersionDate: new Date()), executedConfiguration: config, createdBy: "Application", modifiedBy: "Application")
        String date=DateUtil.toDateString(new Date(), "dd-MMM-YYYY")
        service.getDateRangeValueForCriteriaWithoutString(_, _) >> ["Cumulative", date]
        DateUtil.metaClass.static.getShortDateFormatForLocale={Locale locale1-> return "dd-MMM-yyyy"}
        String criteriaString = Constants.REPORTING_PERIOD_START_DATE + Constants.SPACE_STRING + Constants.REPORTING_PERIOD_END_DATE
        when:
        def result = service.replaceStringWithDate(criteriaString, executedTemplateQuery, false, locale)
        then:
        result == "Cumulative" + Constants.SPACE_STRING + date
    }

    void "test for getCriteriaDate"() {
        given:
        String date=DateUtil.toDateString(new Date(), "dd-MMM-YYYY")
        Locale locale = new Locale('en')
        def config = new ExecutedConfiguration(
                executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), sourceProfile: new SourceProfile(sourceName: "Test"), lastRunDate: new Date(), owner: createUser("Application", "ROLE_ADMIN"), createdBy: "Application", modifiedBy: "Application", locale: new Locale("en"))
        def executedTemplateQuery = new ExecutedTemplateQuery(id: 1L, executedTemplate: new ExecutedCaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(argusName: "test1", reportField: new ReportField(name: "CASE_NUMBER"))])), executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(dateRangeStartAbsolute: new Date(), dateRangeEndAbsolute: new Date(), executedAsOfVersionDate: new Date()), executedConfiguration: config, createdBy: "Application", modifiedBy: "Application")
        service.getDateRangeValueForCriteriaWithoutString(_, _) >> ["Cumulative", date]
        DateUtil.metaClass.static.getShortDateFormatForLocale={Locale locale1-> return "dd-MMM-yyyy"}
        when:
        def result = service.getCriteriaDate(executedTemplateQuery, locale)
        then:
        result.get(executedTemplateQuery.id + Constants.REPORTING_PERIOD_START_DATE) == "Cumulative"
        result.get(executedTemplateQuery.id + Constants.REPORTING_PERIOD_END_DATE) == date
    }

    void "test import From Excel"() {
        given:
        Holders.config.pv.dictionary.group.enabled = true
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [], "xyz@gmail.com")
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2", createdBy: 'user', modifiedBy: 'user')
        userGroup_1.save(failOnError: true)
        userGroup_2.save(failOnError: true)
        def scheduler = "{\"startDateTime\":\"2017-06-16T00:58-05:00\",\"timeZone\":{\"text\":\"(GMT -05:00) EST\",\"selected\":true,\"offset\":\"-05:00\",\"name\":\"EST\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1\"}"
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return code }
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet worksheet = workbook.createSheet("Data");
        XSSFRow row = worksheet.createRow((short) 0)
        row = worksheet.createRow((short) 1)
        row = worksheet.createRow((short) 2)
        row.createCell((short) 11).setCellValue("Attachment format (all sections)")
        row = worksheet.createRow((short) 3)
        row.createCell((short) 0).setCellValue("report 1")
        row.createCell((short) 1).setCellValue("template 1")
        row.createCell((short) 2).setCellValue("ASPIRIN ALUMINIUM,AZASPIRIUM CHLORIDE")
        row.createCell((short) 3).setCellValue("")
        row.createCell((short) 4).setCellValue("")
        row.createCell((short) 5).setCellValue("")
        row.createCell((short) 6).setCellValue("")
        row.createCell((short) 7).setCellValue(scheduler)
        row.createCell((short) 8).setCellValue("user2, user3")
        row.createCell((short) 9).setCellValue("group, group2")
        row.createCell((short) 10).setCellValue("pvreports@rxlogix.com")
        row.createCell((short) 11).setCellValue("PDF")
        row.createCell((short) 12).setCellValue("1")
        row.createCell((short) 13).setCellValue("admin")

        Configuration.metaClass.static.findByReportNameAndIsDeletedAndTenantIdAndOwner = { String reportName, Boolean isDeleted, Long tenantId, User owner ->
            new Configuration(id: 1, reportName: "test", globalDateRangeInformation: new GlobalDateRangeInformation(), productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}',
                    scheduleDateJSON: 'scheduleDateJSON', deliveryOption: new DeliveryOption())
        }
        DictionaryGroup.metaClass.static.getAllRecordsBySearch = { Integer dicType, String term, String dataSource, User user, Integer tenantId, Boolean exactSearch ->
            new Object() {
                List<Map> list() {
                    []
                }
            }
        }

        def crudServiceMock = new MockFor(CRUDService)
        def resultConfig
        crudServiceMock.demand.saveOrUpdate(1) { o ->
            resultConfig = o
            return o
        }
        service.CRUDService = crudServiceMock.proxyInstance()
        service.metaClass.parseProducts = { r, product, columNumber, lang ->
            product["1"] = [["name": "ingr", "id": 111], ["name": "ingr2", "id": 222]]
            product["2"] = []
            product["3"] = []
            product["4"] = []
            return 5
        }
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { -> return new User() }
        userMock.demand.getAllowedSharedWithUsersForCurrentUser(0..1) { String search = null -> [normalUser_1, normalUser_2] }
        userMock.demand.getAllowedSharedWithGroupsForCurrentUser(0..1) { String search = null -> [userGroup_1, userGroup_2] }
        service.userService = userMock.proxyInstance()
        PVDictionaryConfig.setProductConfig(new DictionaryConfig(views: [["1", 'Ingredient'], ["2", 'Family']]))
        def adminUser = makeAdminUser()
        User.metaClass.findByUsernameIlike = { name ->
            adminUser
        }
        when:
        def result = service.importFromExcel(workbook)
        then:
        result.updated.size() == 0
    }

    void "test bindParameterValuesToGlobalQuery"(){
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        when:
        def params = [:]
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        service.bindParameterValuesToGlobalQuery(icsrReportConfiguration, params)
        then:
        icsrReportConfiguration.globalQueryValueLists[0].parameterValues[0].reportField.name == "report"
    }

    void "test bindParameterValuesToGlobalQuery CustomSQLValue"(){
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        when:
        def params = [:]
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].operator"] = "TOMORROW"
        service.bindParameterValuesToGlobalQuery(icsrReportConfiguration, params)
        then:
        icsrReportConfiguration.globalQueryValueLists[0].parameterValues[0].key == "true"
    }

    void "test bindSharedWith isUpdate false"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(deliveryOption: new DeliveryOption())
        periodicReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        service.userService = mockUserService.proxyInstance()
        when:
        service.bindSharedWith(periodicReportConfiguration,["UserGroup_${userGroup.id}","User_${normalUser.id}"],["UserGroup_${userGroup.id}","User_${normalUser.id}"],false)
        then:
        periodicReportConfiguration.deliveryOption.sharedWith == [normalUser]
        periodicReportConfiguration.deliveryOption.sharedWithGroup == [userGroup]
        periodicReportConfiguration.deliveryOption.executableBy == [normalUser]
        periodicReportConfiguration.deliveryOption.executableByGroup == [userGroup]
    }

    void "test bindSharedWith isUpdate true"(){
        User normalUser = makeNormalUser("user",[], "abc1@gmail.com")
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(deliveryOption: new DeliveryOption(sharedWith: [makeNormalUser("normalUser",[])],sharedWithGroup: [new UserGroup()]))
        periodicReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        service.userService = mockUserService.proxyInstance()
        when:
        service.bindSharedWith(periodicReportConfiguration,["UserGroup_${userGroup.id}","User_${normalUser.id}"],["UserGroup_${userGroup.id}","User_${normalUser.id}"],true)
        then:
        periodicReportConfiguration.deliveryOption.sharedWith == [normalUser]
        periodicReportConfiguration.deliveryOption.sharedWithGroup == [userGroup]
        periodicReportConfiguration.deliveryOption.executableBy == [normalUser]
        periodicReportConfiguration.deliveryOption.executableByGroup == [userGroup]
    }

    void "PVR-19456 Issue with DST change day report execution for start date due to offset change startdate and actual"() {
        given: "A configuration which runs on DST change day"
        def recurrenceJSON = '''{"startDateTime":"2022-11-17T02:30-04:00","timeZone":{"text":"(GMT -04:00) EST5EDT","selected":true,"offset":"-04:00","name":"EST5EDT"},"recurrencePattern":"FREQ=WEEKLY;BYDAY=WE,TH;INTERVAL=1;COUNT=3"}'''
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        then: "Next run date should be current date"
        DateFormat df = new SimpleDateFormat('dd-MM-yyyy HH:MM')
        df.setTimeZone(TimeZone.getTimeZone(DateTimeZone.UTC.ID))
        next.format('dd-MM-yyyy HH:mm') == '17-11-2022 07:30'
    }

    void "test getDateRangeValueForCriteria"(){
        given:
        def user = createUser("admin","ROLE_ADMIN")
        def executedGlobalDateRangeInfo = new ExecutedGlobalDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE, dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date()).save(flush: true, failOnError: true, validate: false)
        def executedConfig = new ExecutedConfiguration(executedGlobalDateRangeInformation: executedGlobalDateRangeInfo, createdBy: user, modifiedBy: user).save(flush: true, failOnError: true, validate: false)
        def reportTemplate = new ReportTemplate(name: "testTemplate").save(flush: true, failOnError: true, validate: false)
        def executedDateRangeInfo = new ExecutedDateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE, dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date()).save(flush: true, failOnError: true, validate: false)
        def executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: executedConfig, executedTemplate: reportTemplate, executedDateRangeInformationForTemplateQuery: executedDateRangeInfo).save(flush: true, failOnError: true, validate: false)
        def mockCustomMessageService = new MockFor(CustomMessageService)
        String result = "Cumulative until 09-Aug-2022"
        mockCustomMessageService.demand.getMessage(0..1){String code, String date -> return result}
        service.customMessageService = mockCustomMessageService.proxyInstance()
        when:
        def msg = service.getDateRangeValueForCriteria(executedTemplateQuery, new Locale("en"))
        then:
        msg == result
    }

}
