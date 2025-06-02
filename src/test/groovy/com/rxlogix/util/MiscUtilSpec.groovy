package com.rxlogix.util

import com.rxlogix.config.Configuration
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.ReportTask
import groovy.json.JsonOutput
import net.sf.dynamicreports.report.constant.PageOrientation
import spock.lang.Ignore
import spock.lang.Specification

class MiscUtilSpec extends Specification {
    @Ignore
    void "md5ChecksumForFile should return the checksum of a file as a String"() {
        // This needs to be operating system independent. This test can fail on a Windows environment if it has line breaks:
        // http://stackoverflow.com/questions/5940514/is-a-md5-hash-of-a-file-unique-on-every-system
        expect:
        "46190f059ea1c0af37a772c920c1eb53" == MiscUtil.md5ChecksumForFile("test/unit/data/md5checksum_test.txt")
    }

    def "Pass a list to get paged result using getPagedResult"() {
        given:
        List<Integer> list = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
        List<Integer> pagedList = MiscUtil.getPagedResult(list, offset, max)
        expect:
        pagedList == newList
        where:
        max | offset || newList
        10  | 0      || [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        10  | 10     || [10, 11, 12]
        10  | 20     || []
        10  | 12     || [12]
        10  | 13     || []
        20  | 0      || [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
    }

    void "PVR-2945: Schedule Recurrence: Weekly frequency must have a day selected"() {
        given: "ScheduleDateJSON from UI"

        when: "Calculating the next run date"
        boolean result = MiscUtil.validateScheduleDateJSON(scheduleDateJSON)

        then: "Next run date is next weekday (No Saturday or Sunday)"
        result == expected

        where:
        scheduleDateJSON << ["""{"startDateTime":"2016-06-22T17:38Z","timeZone":{"text":"(GMT +00:00) UTC","selected":true,"offset":"+00:00","name":"UTC"},"recurrencePattern":"FREQ=WEEKLY;BYDAY=WE;INTERVAL=1"}""",
                             """{"startDateTime":"2016-06-22T17:38Z","timeZone":{"text":"(GMT +00:00) UTC","selected":true,"offset":"+00:00","name":"UTC"},"recurrencePattern":"FREQ=WEEKLY;BYDAY=;INTERVAL=1"}""", "FREQ=WEEKLY;BYDAY=;INTERVAL=1", "FREQ=WEEKLY;BYDAY=WE;INTERVAL=1"]
        expected << [true, false, false, false]
    }

    def "Check if pattern entered is valid"() {
        expect:
        MiscUtil.isValidPattern(term) == expected

        where:
        term               | expected
        "user"             | true
        ")(cn="            | false
        "user=ObjectClass" | false
        "(ObjectClass)"    | false
        "*"                | false
        "\$"               | false
        "="                | false
        "?"                | false
        "?="               | false
        "add^"             | false
        "abcd*def"         | false
        "abcd+def"         | false
        "+"                | false
        "|"                | false
        "abc|"             | false
        "abc.def"          | true
        "abc def"          | true

    }

    void "test isScheduleDateJSONEmpty"(){
        when:
        boolean result = MiscUtil.isScheduleDateJSONEmpty(scheduleDateJSON)
        then:
        result == expected

        where:
        scheduleDateJSON << ["""{"startDateTime":"2021-02-18T01:00Z","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}""",
                             """{"startDateTime":"NaN-NaN-NaNT01:00Z","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}""",
                             """{"startDateTime":"NaN-NaN-NaNTNaN:NaNZ","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}"""
                             ]
        expected << [false, true, true]
    }

    def "Evaluate with basic expressions"() {
        when:
        Configuration configuration = new Configuration(id: 1, createdBy: 'sachin', emailConfiguration: new EmailConfiguration(id: 2, subject: 'test', pageOrientation: PageOrientation.LANDSCAPE), reportTasks: [new ReportTask(id: 3, description: 'hello task')])

        then:
        MiscUtil.evaluate(configuration, 'emailConfiguration.subject') == Eval.x(configuration, 'x.emailConfiguration.subject')
        MiscUtil.evaluate(configuration, 'reportTasks[0]') == Eval.x(configuration, 'x.reportTasks[0]')
        MiscUtil.evaluate(configuration, 'reportTasks*.description') == Eval.x(configuration, 'x.reportTasks*.description')
        MiscUtil.evaluate(configuration, 'reportTasks[0].description') == Eval.x(configuration, 'x.reportTasks[0].description')
        MiscUtil.evaluate(configuration, 'useCaseSeries?.seriesName') == Eval.x(configuration, 'x.useCaseSeries?.seriesName')
    }

    def "Evaluate exception thrown when expression value is null "() {
        when:
        Configuration configuration = new Configuration(id: 2, createdBy: 'sachin', emailConfiguration: null)
        MiscUtil.evaluate(configuration, 'emailConfiguration.id')
        then:
        thrown(NullPointerException)
    }

}
