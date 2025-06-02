package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.IcsrOrganizationType
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import com.rxlogix.mapping.SafetyCalendar
import com.rxlogix.util.ViewHelper
import com.rxlogix.enums.IcsrRuleEvaluationEnum

@Secured('permitAll')
class IcsrProfileConfigurationRestController extends RestfulController implements SanitizePaginationAttributes{

    def userService
    def icsrProfileConfigurationService
    def sqlGenerationService

    IcsrProfileConfigurationRestController() {
        super(IcsrProfileConfiguration)
    }

    def list() {
        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params,currentUser, IcsrProfileConfiguration,[IcsrProfileConfiguration.class])
        boolean showXMLOption = grailsApplication.config.show.xml.option ?: false
        List<Long> idsForUser = IcsrProfileConfiguration.getAllIdsByFilter(filter, showXMLOption, params.sort, params.order).list([max: params.max, offset: params.offset])
        int recordsFilteredCount = IcsrProfileConfiguration.countRecordsByFilter(filter, showXMLOption).get()
        List<IcsrProfileConfiguration> configurationList = recordsFilteredCount ? IcsrProfileConfiguration.getAll(idsForUser) : []
        render([aaData         : briefProperties(configurationList),
                recordsTotal   : IcsrProfileConfiguration.countRecordsByFilter(new LibraryFilter(currentUser, [IcsrProfileConfiguration.class]), showXMLOption).get(),
                recordsFiltered: recordsFilteredCount] as JSON)
    }

    def profileList() {
        sanitize(params)
        User currentUser = userService.currentUser
        def executedIcsrProfileConfNameQuery = ExecutedIcsrProfileConfiguration.getAllIcsrProfileConfBySearchString(params.searchString, currentUser)
        List<ExecutedIcsrProfileConfiguration> executedIcsrProfileConfList = executedIcsrProfileConfNameQuery.list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        List<Map> icsrProfileConfigurations = executedIcsrProfileConfList.collect{
            [id: it[0], numOfExecutions: it[1], reportName: it[2], description: it[3], senderOrganizationName: it[4], senderTypeName: it[5], recipientOrganizationName: it[6], recipientTypeName: it[7],dateCreated: it[8], createdBy: it[9],lastUpdated: it[10]]
        }
        render([aaData: icsrProfileConfigurations, recordsTotal: ExecutedIcsrProfileConfiguration.countAllIcsrProfileConfBySearchString(null, currentUser).get(),
                recordsFiltered: ExecutedIcsrProfileConfiguration.countAllIcsrProfileConfBySearchString(params.searchString, currentUser).get()] as JSON)
    }

    private List briefProperties(List<ExecutedIcsrProfileConfiguration> configurations) {
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        return configurations.collect {
            [id: it.id, reportName: it.reportName, description: it.description, senderOrganization: it?.senderOrganization?.unitName, senderType: getOrganizationTypeByPreference(it?.senderOrganization?.organizationType, langId), recipientOrganization: it?.recipientOrganization?.unitName, recipientType: getOrganizationTypeByPreference(it?.recipientOrganization?.organizationType, langId), qualityChecked: it.qualityChecked, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), createdBy: it.owner.fullName, lastUpdated: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT), isDisabled: it.isDisabled]
        }
    }

    def getOrganizationTypeByPreference(Object orgType, Integer langId){
        IcsrOrganizationType orgName = IcsrOrganizationType.findByOrg_name_idAndLangId(orgType?.org_name_id, langId)
        if (!orgName) {
            Integer defaultLangId = sqlGenerationService.getPVALanguageId('en')
            orgName = IcsrOrganizationType.findByOrg_name_idAndLangId(orgType?.org_name_id, defaultLangId)
        }
        return orgName.name
    }

    def getReferenceProfileList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        render([items : IcsrProfileConfiguration.fetchAllForIcsrReport(term).list(offset: Math.max(page - 1, 0) * max, max: max).collect {
            [id: it.id, text: "${it.reportName}-${it.numOfExecutions}", senderId: it.senderOrganizationId, recipientId: it.recipientOrganizationId]
        }, total_count: IcsrProfileConfiguration.fetchAllForIcsrReport(term).count()] as JSON)
    }
    
    
    
    def getIcsrCalendarNames(String term, Integer page, Integer max) {
        List calendarNames = []
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        SafetyCalendar.withNewSession {
            def safetyCalenderNamedQuery = SafetyCalendar.getAllSafetyCalenderBySearchString(term)
            calendarNames = safetyCalenderNamedQuery.list([offset: Math.max(page - 1, 0) * max, max: max]).collect { [id: it.id, text: it.name] }
        }
        render([items : calendarNames.sort{it.text.toUpperCase()}, total_count: calendarNames.size()] as JSON)
    }

    def getExecutedProfilesList(Long id, String term) {
        sanitize(params)
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = ExecutedIcsrReportConfiguration.read(id)
        def items = IcsrProfileConfiguration.fetchAllIcsrProfileForReciver(executedIcsrReportConfiguration?.receiverId, term).list([max: params.int('max'), offset: params.int('offset')]).collect {
            [id: it.id, text: it.reportName + " - " + it.numOfExecutions]
        }
        render([items: items,
                total_count: IcsrProfileConfiguration.fetchAllIcsrProfileForReciver(executedIcsrReportConfiguration?.receiverId, term).count()] as JSON)
    }

    def profileListForManual(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        render([items : IcsrProfileConfiguration.getAllActiveIcsrProfiles(term).list(offset: Math.max(page - 1, 0) * max, max: max).collect {
            [id: it.id, text: "${it.reportName}", isJapan: it.isJapanProfile, isDevice: it.deviceReportable]
        }, total_count: IcsrProfileConfiguration.getAllActiveIcsrProfiles(term).count()] as JSON)
    }

    def templateQueryForManual(Long profileId) {
        if (!profileId) {
            render([items      : [],
                    total_count: 0] as JSON)
            return
        }
        IcsrProfileConfiguration profileConfiguration = IcsrProfileConfiguration.read(profileId)
        render([items      : profileConfiguration?.templateQueries?.unique{it.templateId}?.collect {
            [id: it.id, text: it.template.name]
        } ?: [],
                total_count: profileConfiguration?.templateQueries?.size() ?: 0] as JSON)
    }


    def getIcsrProfileDescription(Long id) {
        IcsrProfileConfiguration icsrProfileConfiguration = IcsrProfileConfiguration.read(id)
        Map result = [
                text: "${icsrProfileConfiguration?.reportName}-${icsrProfileConfiguration?.numOfExecutions}",
                id  : icsrProfileConfiguration?.id,
        ]
        render(result as JSON)
    }

    def getRuleEvaluationList(boolean isJapanProfile) {
        render([status: 200, items: ViewHelper.getIcsrRuleEvaluationList(isJapanProfile)] as JSON)
    }

    def getRuleEvaluationValue(String id) {
        if (id) {
            IcsrRuleEvaluationEnum icsrRuleEvaluationEnum = IcsrRuleEvaluationEnum.valueOf(id)
            render(ViewHelper.getIcsrRuleEvaluationValue(icsrRuleEvaluationEnum) as JSON)
        } else {
            render([id: '', text: ''] as JSON)
        }
    }

}
