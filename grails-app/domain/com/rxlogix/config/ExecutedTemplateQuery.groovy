package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import grails.util.Holders
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
@SectionModuleAudit(parentClassName = ['executedReportConfiguration','executedIcsrProfileConfiguration','executedIcsrReportConfiguration', 'executedPeriodicReportConfiguration', 'executedConfiguration'])
class ExecutedTemplateQuery extends BaseTemplateQuery {
    ReportTemplate executedTemplate
    ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery
    SuperQuery executedQuery //@TODO should this be "ExecutedSuperQuery" ????
    List<ExecutedQueryValueList> executedQueryValueLists
    List<ExecutedTemplateValueList> executedTemplateValueLists
    boolean draftOnly = false
    ReportResult finalReportResult
    ReportResult draftReportResult
    Boolean manuallyAdded = false
    String onDemandSectionParams

    static belongsTo = [executedConfiguration: ExecutedReportConfiguration]

    static hasOne = [executedDateRangeInformationForTemplateQuery: ExecutedDateRangeInformation]

    static hasMany = [executedQueryValueLists: ExecutedQueryValueList, executedTemplateValueLists: ExecutedTemplateValueList]

    static transients = ['reportResult']

    static mapping = {
        table name: "EX_TEMPLT_QUERY"

        tablePerHierarchy false
        executedQueryValueLists joinTable: [name: "EX_TEMPLT_QRS_EX_QUERY_VALUES", column: "EX_QUERY_VALUE_ID", key: "EX_TEMPLT_QUERY_ID"], indexColumn: [name: "EX_QUERY_VALUE_IDX"]
        executedTemplateValueLists joinTable: [name: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", column: "EX_TEMPLT_VALUE_ID", key: "EX_TEMPLT_QUERY_ID"], indexColumn: [name: "EX_TEMPLT_VALUE_IDX"]

        executedConfiguration column: "EX_RCONFIG_ID", cascade: "none", lazy: false
        executedTemplate column: "EX_TEMPLT_ID", cascade: "none" , lazy: false
        executedQuery column: "EX_QUERY_ID", cascade: "none"
        finalReportResult column: "REPORT_RESULT_ID"
        finalReportResult cascade: 'all'
        draftReportResult column: "DRAFT_REPORT_RESULT_ID"
        draftReportResult cascade: 'all'
        draftOnly column: "DRAFT_ONLY"
        manuallyAdded column: "MANUALLY_ADDED"
        onDemandSectionParams column: "ON_DEMAND_SECTION_PARAMS"
    }

    static constraints = {
        draftReportResult(nullable: true)
        finalReportResult(nullable: true)
        executedTemplate(nullable: false)
        executedQueryValueLists(nullable: true)
        executedDateRangeInformationForTemplateQuery(nullable: false)
        executedQuery(nullable: true)
        manuallyAdded(nullable: true)
        onDemandSectionParams(nullable: true)
    }

    transient String getFooterText() {
        String text = ''
        if (displayMedDraVersionNumber) {
            text += "${reportResult?.medDraVersion}"
            if (footer) {
                text += "\\n"
            }
        }
        text += footer ? footer.replace('\r\n','\\r\\n') : ''
        return text
    }

    boolean isScheduled() {
        return reportResult.executionStatus in [ReportExecutionStatusEnum.SCHEDULED, ReportExecutionStatusEnum.DELIVERING]
    }

    ReportResult getReportResult(Boolean isInDraftMode = false) {
        if (executedConfiguration instanceof ExecutedConfiguration) {
            return finalReportResult
        } else {
            if (isInDraftMode || executedConfiguration?.status in [ReportExecutionStatusEnum.GENERATED_DRAFT, ReportExecutionStatusEnum.GENERATING_DRAFT, ReportExecutionStatusEnum.GENERATED_CASES, ReportExecutionStatusEnum.GENERATING_NEW_SECTION]) {
                return draftReportResult
            } else {
                return finalReportResult
            }
        }
    }

    void setReportResult(ReportResult reportResult) {
        if (executedConfiguration && (executedConfiguration instanceof ExecutedConfiguration
                || executedConfiguration.status in [ReportExecutionStatusEnum.GENERATING_FINAL_DRAFT, ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT])) {
            finalReportResult = reportResult
        } else {
            finalReportResult = reportResult
            ReportResult draft = new ReportResult(MiscUtil.getObjectProperties(reportResult))
            draftReportResult = draft
        }
    }

    @Override
    Date getStartDate() {
        return executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute
    }

    @Override
    Date getEndDate() {
        return executedDateRangeInformationForTemplateQuery?.dateRangeEndAbsolute
    }

    @Override
    transient BaseConfiguration getUsedConfiguration() {
        return GrailsHibernateUtil.unwrapIfProxy(executedConfiguration)
    }

    @Override
    ReportTemplate getUsedTemplate() {
        return GrailsHibernateUtil.unwrapIfProxy(executedTemplate)
    }

    @Override
    SuperQuery getUsedQuery() {
        return GrailsHibernateUtil.unwrapIfProxy(executedQuery)
    }

    @Override
    List<TemplateValueList> getUsedTemplateValueLists() {
        return executedTemplateValueLists
    }

    @Override
    List<QueryValueList> getUsesQueryValueLists() {
        return executedQueryValueLists
    }

    @Override
    BaseDateRangeInformation getUsedDateRangeInformationForTemplateQuery() {
        return executedDateRangeInformationForTemplateQuery
    }

    @Override
    Map<String, String> getPOIInputsKeysValues() {
        if (executedTemplate) {
            Set<ParameterValue> poiParameters = executedConfiguration.poiInputsParameterValues
            Map poiInputKeysValuesMap = [:]
            executedTemplate.getPOIInputsKeys().each { String key ->
                poiInputKeysValuesMap.put(key, poiParameters.find { it.key == key }?.value)
            }
            return poiInputKeysValuesMap
        }
        return [:]
    }

    String getCaseSeriesName() {
        title != executedConfiguration.reportName ?
                executedConfiguration.reportName + " : " + title : executedConfiguration.reportName + " : " + executedTemplate.name
    }

    Boolean hasCaseNumberAndReportData() {
        String caseNumberFieldName = usedConfiguration.sourceProfile.caseNumberFieldName
        executedTemplate.allSelectedFieldsInfo.any {
            caseNumberFieldName.equals(it.reportField.name)
        } && reportResult.reportRows
    }

    Date getGlobalEndDate() {
        [executedConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute,
         executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute].min()
    }

    // For PVR-21232 -- For the calculation of Max End i.e Max End Date = Max End Date from all the sections and global date which have definite dates.
    Date getMaxEndDate() {
        Date maxEndDate = null
        List<ExecutedTemplateQuery> executedTemplateQueries = executedConfiguration.getExecutedTemplateQueriesForProcessing()
        List<ExecutedTemplateQuery> templateQueriesForMaxEnd = executedTemplateQueries?.findAll {
            (it.executedDateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.CUMULATIVE) && (it.executedDateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.PR_DATE_RANGE)
        }
        if (templateQueriesForMaxEnd) {
            maxEndDate = templateQueriesForMaxEnd*.endDate.max()
        }

        Date globalEndDate  = null
        if (executedConfiguration.executedGlobalDateRangeInformation?.dateRangeEnum != DateRangeEnum.CUMULATIVE){
            globalEndDate = executedConfiguration.executedGlobalDateRangeInformation?.dateRangeEndAbsolute
        }
        return (maxEndDate && globalEndDate) ? [maxEndDate, globalEndDate].max() : maxEndDate ?: globalEndDate ?: null
    }

    boolean showReassessDateDiv() {
        ReportTemplate template = this.executedTemplate
        boolean result = false
        if (template && template?.instanceOf(TemplateSet)) {
            ExecutedTemplateSet.get(template.id)?.nestedTemplates?.each {
                if (it.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE  && !it?.templtReassessDate) result = true
            }
        }
        else if (template) {
            result = template && (template.instanceOf(DataTabulationTemplate) || template.instanceOf(CaseLineListingTemplate)) && template.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE  && !template?.templtReassessDate
        }
        return result
    }

    boolean showQueryReassessDateDiv() {
        def query = this.executedQuery
        boolean result = false
        if (query && query?.instanceOf(QuerySet)) {
            ExecutedQuerySet.get(query.id)?.queries?.each {
                if (!result && it.queryType == QueryTypeEnum.QUERY_BUILDER) {
                    it = Query.get(it.id)
                    result = (it?.instanceOf(Query)) && it?.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE && !it?.reassessListednessDate
                }
            }
        }
        else if (query && query?.instanceOf(Query)) {
            query = Query.get(query.id)
            result = query && (query?.instanceOf(Query)) && query?.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE && !query?.reassessListednessDate
        }
        return result
    }

    @Override
    public String toString() {
        return "$executedTemplate.name ${executedQuery ? ' - ' + executedQuery.name : ''}"
    }

    boolean isVisible(User user = null) {
        User currentUser = user ?: Holders.applicationContext.getBean("userService").currentUser
        if (currentUser.isAdmin()) return true
        if (userGroup) {
            return userGroup?.users?.find { it.id == currentUser.id }
        } else {
            return executedConfiguration.isViewableBy(currentUser)
        }
    }

}