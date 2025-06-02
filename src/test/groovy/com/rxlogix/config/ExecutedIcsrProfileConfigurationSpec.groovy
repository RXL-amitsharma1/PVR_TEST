package com.rxlogix.config

import com.rxlogix.enums.AuthorizationTypeEnum
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ExecutedIcsrProfileConfigurationSpec extends Specification implements DomainUnitTest<ExecutedIcsrProfileConfiguration> {

    def setupSpec() {
        mockDomains User, ReportTemplate, SuperQuery, ExecutedIcsrTemplateQuery, ExecutedTemplateQuery, ExecutedReportConfiguration
    }

    void "test order of execution of templatequery"() {
        when:
        User normalUser = new User(username: 'normalUser')
        ExecutedReportConfiguration icsrProfileConfiguration = new ExecutedIcsrProfileConfiguration(reportName: 'test')
        icsrProfileConfiguration.addToExecutedTemplateQueries(new ExecutedIcsrTemplateQuery(
                executedTemplate: new ReportTemplate(name: "test", createdBy: normalUser),
                executedConfiguration: icsrProfileConfiguration, createdBy: normalUser.username, modifiedBy: normalUser.username, dueInDays: 13, orderNo: 0, authorizationType: AuthorizationTypeEnum.INVESTIGATIONAL_DRUG))

        icsrProfileConfiguration.addToExecutedTemplateQueries(new ExecutedIcsrTemplateQuery(
                executedTemplate: new ReportTemplate(name: "test1", createdBy: normalUser), executedQuery: new SuperQuery(name: 'test1'),
                executedConfiguration: icsrProfileConfiguration, createdBy: normalUser.username, modifiedBy: normalUser.username, dueInDays: 3, orderNo: 1, authorizationType: AuthorizationTypeEnum.INVESTIGATIONAL_DRUG))

        icsrProfileConfiguration.addToExecutedTemplateQueries(new ExecutedIcsrTemplateQuery(
                executedTemplate: new ReportTemplate(name: "test2", createdBy: normalUser),
                executedConfiguration: icsrProfileConfiguration, createdBy: normalUser.username, modifiedBy: normalUser.username, dueInDays: 1, orderNo: 2, authorizationType: AuthorizationTypeEnum.INVESTIGATIONAL_DRUG))

        icsrProfileConfiguration.addToExecutedTemplateQueries(new ExecutedIcsrTemplateQuery(
                executedTemplate: new ReportTemplate(name: "test3", createdBy: normalUser), executedQuery: new SuperQuery(name: 'test1'),
                executedConfiguration: icsrProfileConfiguration, createdBy: normalUser.username, modifiedBy: normalUser.username, dueInDays: 13, orderNo: 3, authorizationType: AuthorizationTypeEnum.INVESTIGATIONAL_DRUG))

        icsrProfileConfiguration.addToExecutedTemplateQueries(new ExecutedIcsrTemplateQuery(
                executedTemplate: new ReportTemplate(name: "test1", createdBy: normalUser),
                executedConfiguration: icsrProfileConfiguration, createdBy: normalUser.username, modifiedBy: normalUser.username, dueInDays: 2, orderNo: 4, authorizationType: AuthorizationTypeEnum.INVESTIGATIONAL_DRUG))

        then: "Sort by Due In days when executing"
        icsrProfileConfiguration.executedTemplateQueriesForProcessing*.dueInDays == [1, 2, 3, 13, 13]
        icsrProfileConfiguration.executedTemplateQueriesForProcessing*.orderNo == [2, 4, 1, 0, 3] //maintains natural order in case same dueInDays
        icsrProfileConfiguration.executedTemplateQueriesForProcessing*.executedTemplate*.name != ['test', 'test1', 'test2', 'test3', 'test1']
    }

}
