package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import groovy.time.TimeCategory
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ActionItem])
class EmailServiceSpec extends Specification implements DataTest, ServiceUnitTest<EmailService> {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    def setup() {
        // force the tests to run in the TEST_TZ
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
    }

    def cleanup() {
        // set the TZ back to what it was
        TimeZone.setDefault(ORIGINAL_TZ)
    }

    def setupSpec() {
        mockDomains User, Role, UserRole, UserGroup, UserGroupUser, ActionItem, ExecutedCaseSeries, ExecutedCaseDeliveryOption
    }

    void "test getActionItemNotifications"() {
        given:
        List<User> userList = []
        (0..6).each {
            def preferenceNormal = new Preference(locale: new Locale("en"), timeZone: "UTC")
            User u = new User(username: 'user' + it, preference: preferenceNormal)
            userList << u.save(validate: false, failOnError: true)
        }
        def preferenceNormal = new Preference(locale: new Locale("en"), timeZone: "UTC")
        User u = new User(username: 'user7', preference: preferenceNormal)
        userList << u.save(validate: false, failOnError: true)

        service.metaClass.static.getUserHoursInHisLocale = { User user -> user == userList[7] ? "01" : "07" }
        UserGroup ug = new UserGroup(name: "group1")
        ug.save(validate: false, failOnError: true)

        UserGroupUser ugu1 = new UserGroupUser(userGroup: ug, user: userList[2])
        ugu1.save(validate: false, failOnError: true)
        UserGroupUser ugu2 = new UserGroupUser(userGroup: ug, user: userList[3])
        ugu2.save(validate: false, failOnError: true)
        Date now = new Date()
        now.set(minute: 59, second: 59)
        use(TimeCategory) {
            ActionItem ai
            //----should not be in notifications---
            ai = new ActionItem(description: "description1-0", assignedTo: userList[7], createdBy: userList[7].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now)
            ai.save(validate: false, failOnError: true)

            ai = new ActionItem(description: "description2-0", assignedGroupTo: ug, createdBy: userList[7].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now + 2.day)
            ai.save(validate: false, failOnError: true)

            ai = new ActionItem(description: "description3-0", assignedTo: userList[4], createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now + 4.day)
            ai.save(validate: false, failOnError: true)

            ai = new ActionItem(description: "description4-0", assignedTo: userList[5], createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now + 6.day)
            ai.save(validate: false, failOnError: true)

            //--- should be in notifications-----
            ai = new ActionItem(description: "description1", assignedTo: userList[1], createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now)
            ai.save(validate: false, failOnError: true)
            ai = new ActionItem(description: "description2", assignedGroupTo: ug, createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now + 5.day)
            ai.save(validate: false, failOnError: true)

            ai = new ActionItem(description: "description3", assignedTo: userList[4], createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now + 3.day)
            ai.save(validate: false, failOnError: true)

            ai = new ActionItem(description: "description4", assignedTo: userList[5], createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now + 1.day)
            ai.save(validate: false, failOnError: true)

            ai = new ActionItem(description: "description5", assignedTo: userList[6], createdBy: userList[0].username, isDeleted: false, status: StatusEnum.OPEN)
            ai.dueDate = (now - 1.day)
            ai.save(validate: false, failOnError: true)
        }
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getAllEnabledUsers(1) { id ->
            List enableUser = []
            userList.each {
                if (UserGroupUser.exists(id, it.id))
                    enableUser.add(it)
            }
            return enableUser
        }
        ActionItem.metaClass.static.createCriteria = {
            new Object() {
                List<ActionItem> list(Closure cl) {
                    ActionItem.findAllByIsDeletedAndStatusNotEqual(false, StatusEnum.CLOSED).each {
                        it.userService = userServiceMock.proxyInstance()
                    }
                }
            }
        }
        when:
        grailsApplication.config.pvreports.sendNotificationHour = "07"
        def notifications = service.getActionItemNotifications()
        List notificationsForUser = []
        (0..6).each { index ->
            notificationsForUser << notifications.find { it.user == userList[index] }
        }

        then:
        notifications.size() == 7
        notificationsForUser[0].today.size() == 1
        notificationsForUser[0].overdue.size() == 1
        notificationsForUser[0].oneDay.size() == 1
        notificationsForUser[0].threeDay.size() == 1
        notificationsForUser[0].fiveDay.size() == 1
        notificationsForUser[1].today.size() == 1
        notificationsForUser[2].fiveDay.size() == 1
        notificationsForUser[3].fiveDay.size() == 1
        notificationsForUser[4].threeDay.size() == 1
        notificationsForUser[5].oneDay.size() == 1
        notificationsForUser[6].overdue.size() == 1
    }

    void "test emailReportTo"(){
        given:
        service.metaClass.sendReport{ ExecutedCaseSeries testCaseSeries, String[] testRecipients, ReportFormatEnum[] testOutputs, boolean testAsyVal -> throw new CustomJasperException("Exception thrown while creating caseList report")}
        service.metaClass.sendMailCheck{ExecutedCaseSeries testCaseSeries -> return true}

        when:
        List<ReportFormatEnum> attachmentFormats = [ReportFormatEnum.HTML, ReportFormatEnum.PDF, ReportFormatEnum.XLSX]
        List<String> emailToUsers = ["abc@gmail.com"]
        ExecutedCaseSeries caseSeries = new ExecutedCaseSeries().save(flush: true, failOnError: true, validate: false)
        service.emailReportTo(caseSeries, emailToUsers, attachmentFormats)

        then:
        thrown(Exception)

    }

    void "test emailReportOutput"(){
        given:
        service.metaClass.sendReport{ ExecutedCaseSeries testCaseSeries, String[] testRecipients, ReportFormatEnum[] testOutputs, boolean testAsyVal -> throw new CustomJasperException("Exception thrown while creating caseList report")}
        service.metaClass.sendMailCheck{ExecutedCaseSeries testCaseSeries -> return true}

        when:
        ExecutedCaseSeries caseSeries = new ExecutedCaseSeries().save(flush: true, failOnError: true, validate: false)
        caseSeries.executedDeliveryOption = new ExecutedCaseDeliveryOption().save(flush: true, failOnError: true, validate: false)
        service.emailReportOutput(caseSeries)

        then:
        thrown(Exception)

    }

    void "test insertEmailBodyValues"() {
        given: "some report output with two sections: datatabulation template (chart and table) and cll (jast table), and it is converted to html using publisherService "
        service.metaClass.checkIfReportExceedsHtmlLimit {ExecutedTemplateQuery executedTemplateQuery -> false }
        int callCounter = 0
        service.publisherService = [getQueryTemplateData: { def entity ->
            if (callCounter == 0) {
                callCounter++
                return [
                        "chart"        : "<html><body><img src=\"file://file1.png\"/></body></html>",
                        "chartFile"    : "file1.png",
                        "chartFileSize": 1000,
                        table          : "table1"
                ]
            } else {
                return [
                        "table": "table2"
                ]
            }
        }]

        when: "when we have email body with two placeholders to insert the first and the second report section"
        String text = "hi, [sectionOutput1] , [sectionOutput2]"
        Map result = service.insertEmailBodyValues(text, new ExecutedConfiguration(executedTemplateQueries: [new ExecutedTemplateQuery(), new ExecutedTemplateQuery()]),[:])

        then:"we are getting email body with replaced placeholders with tables and reference to chart image + image itself as attachment"
        result.text == """hi, <div ><img width="684" src="cid:cid0"></div><br>table1 , table2"""
        result.attachments.size() == 1
        result.attachments[0].contentId == "cid0"
        result.attachments[0].path == "file1.png"
        result.attachments[0].size == 1000
    }
}
