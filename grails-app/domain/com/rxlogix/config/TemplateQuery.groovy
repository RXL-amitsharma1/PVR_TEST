package com.rxlogix.config

import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
@DirtyCheck
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['configuration', 'periodicReportConfiguration', 'icsrProfileConfiguration', 'icsrReportConfiguration'])
class TemplateQuery extends BaseTemplateQuery {
    static auditable =  true
    ReportTemplate template
    SuperQuery query
    DateRangeInformation dateRangeInformationForTemplateQuery = new DateRangeInformation()
    boolean draftOnly = false

    //For the one-to-many dynamic form
    int index
    boolean dynamicFormEntryDeleted
    List<QueryValueList> queryValueLists
    List<TemplateValueList> templateValueLists

    static propertiesToUseWhileCopying = ['userGroup','template', 'query', 'queryLevel', 'header', 'title', 'footer', 'headerProductSelection', 'granularity','priority','rootCause','responsibleParty','issueType', 'assignedToGroups', 'assignedToUsers','summarySql','actionsSql','investigationSql','actions','summary','investigation', 'headerDateRange', 'draftOnly', 'privacyProtected', 'blindProtected','displayMedDraVersionNumber']

    static transients = ['dynamicFormEntryDeleted']

    static belongsTo = [report: ReportConfiguration]

    static hasOne = [dateRangeInformationForTemplateQuery: DateRangeInformation]

    static hasMany = [queryValueLists: QueryValueList, templateValueLists: TemplateValueList]

    static mapping = {
        table name: "TEMPLT_QUERY"
        tablePerHierarchy false

        queryValueLists joinTable: [name: "TEMPLT_QRS_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "TEMPLT_QUERY_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        templateValueLists joinTable: [name: "TEMPLT_QRS_TEMPLT_VALUES", column: "TEMPLT_VALUE_ID", key: "TEMPLT_QUERY_ID"], indexColumn: [name: "TEMPLT_VALUE_IDX"]

        template column: "RPT_TEMPLT_ID", cascade: "none"
        query column: "SUPER_QUERY_ID", cascade: "none"
        report column: "RCONFIG_ID", cascade: "none"

        draftOnly column: "DRAFT_ONLY"
        index column: "INDX"
    }

    static constraints = {
        template(nullable: false)
        query(nullable: true)
        dateRangeInformationForTemplateQuery(nullable: false)
        dynamicFormEntryDeleted(bindable: true)
        queryValueLists(cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists?.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.TemplateQuery.parameterValues.valueless"
            }
            return hasValues
        })
        templateValueLists(cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists?.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.TemplateQuery.parameterValues.valueless"
            }
            return hasValues
        })
    }

    @Override
    Date getStartDate() {
        return dateRangeInformationForTemplateQuery?.reportStartAndEndDate[0]
    }

    @Override
    Date getEndDate() {
        return dateRangeInformationForTemplateQuery?.reportStartAndEndDate[1]
    }

    @Override
    BaseConfiguration getUsedConfiguration() {
        return GrailsHibernateUtil.unwrapIfProxy(report)
    }

    @Override
    ReportTemplate getUsedTemplate() {
        return GrailsHibernateUtil.unwrapIfProxy(template)
    }

    @Override
    SuperQuery getUsedQuery() {
//        Added to JavaAssist Proxy Object cast exception http://stackoverflow.com/questions/5622481/removing-proxy-part-of-grails-domain-object
        return GrailsHibernateUtil.unwrapIfProxy(query)
    }

    @Override
    List<TemplateValueList> getUsedTemplateValueLists() {
        return templateValueLists
    }

    @Override
    List<QueryValueList> getUsesQueryValueLists() {
        return queryValueLists
    }

    @Override
    BaseDateRangeInformation getUsedDateRangeInformationForTemplateQuery() {
        return dateRangeInformationForTemplateQuery
    }

    boolean showReassessDateDiv() {
        def template = this.template
        boolean result = false
        if (template && template?.instanceOf(TemplateSet)) {
            TemplateSet.get(template.id)?.nestedTemplates?.each {
                if (it.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE  && !it?.templtReassessDate) result = true
            }
        }
        else if (template) {
            result = template && (template.instanceOf(DataTabulationTemplate) || template.instanceOf(CaseLineListingTemplate)) && template.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE  && !template?.templtReassessDate
        }
        return result
    }

    boolean showQueryReassessDateDiv() {
        def query = this.query
        boolean result = false
        if (query && query?.instanceOf(QuerySet)) {
            QuerySet.get(query.id)?.queries?.each {
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
    Map<String,String> getPOIInputsKeysValues(){
        if (template) {
            if (template.id && !template.isAttached()) {
                template = ReportTemplate.get(template.id)
            }
            Set<ParameterValue> poiParameters = report?.poiInputsParameterValues
            Map poiInputKeysValuesMap = [:]
            GrailsHibernateUtil.unwrapIfProxy(template).getPOIInputsKeys().each { String key ->
                poiInputKeysValuesMap.put(key, poiParameters.find { it.key == key }?.value)
            }
            return poiInputKeysValuesMap
        }
        return [:]
    }

    static namedQueries = {
        usuageByQuery { SuperQuery superQuery ->
            'query' {
                eq('isDeleted', false)
                eq('id', superQuery?.id)
            }
            'report' {
                eq('isDeleted', false)
            }
        }

        getByQueryValueLists{QueryValueList qvl ->
            queryValueLists {
                idEq(qvl.id)
            }
        }

        queryUsedByConfigurations { SuperQuery query ->
            projections {
                distinct 'report'
            }
            usuageByQuery(query)
        }

        queryUsedByConfigurationsCount { SuperQuery query ->
            projections {
                countDistinct('report')
            }
            usuageByQuery(query)
        }

        usuageByTemplate { ReportTemplate reportTemplate ->
            'template' {
                eq('isDeleted', false)
                eq('id', reportTemplate?.id)
            }

            'report' {
                eq('isDeleted', false)
            }
        }

        countUsuageByTemplate { ReportTemplate reportTemplate ->
            projections {
                countDistinct('id')
            }
            usuageByTemplate(reportTemplate)
        }
    }

    @Override
    public String toString() {
        return "${template?.name} ${query ? ' - ' + query.name : ''}"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && (oldValues?.keySet()?.contains("queryValueLists") || oldValues?.keySet()?.contains("templateValueLists")) || oldValues?.keySet()?.contains("dateRangeInformationForTemplateQuery")) {
            withNewSession {
                TemplateQuery tq = TemplateQuery.read(id);
                if (oldValues?.keySet()?.contains("queryValueLists")) oldValues.put("queryValueLists", tq.queryValueLists?.toString())
                if (oldValues?.keySet()?.contains("templateValueLists")) oldValues.put("templateValueLists", tq.templateValueLists?.toString())
                if (oldValues?.keySet()?.contains("dateRangeInformationForTemplateQuery")) oldValues.put("dateRangeInformationForTemplateQuery", tq.dateRangeInformationForTemplateQuery?.toString())
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }

    String getInstanceIdentifierForAuditLog() {
        return toString()
    }
}
