package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class TemplateSet extends ReportTemplate implements ITemplateSet {
    static auditable = true
    boolean excludeEmptySections = false
    List<ReportTemplate> nestedTemplates
    boolean linkSectionsByGrouping = false
    boolean sectionBreakByEachTemplate = false

    static hasMany = [nestedTemplates: ReportTemplate]

    static mapping = {
        tablePerHierarchy false
        table name: "TEMPLT_SET"
        id column: "ID"

        excludeEmptySections column: "EXCLUDE_EMPTY_SECTIONS"
        linkSectionsByGrouping column: "LINK_SECTIONS_BY_GROUPING"
        sectionBreakByEachTemplate column: "SECTION_BREAK_BY_EACH_TEMPLATE"
        nestedTemplates joinTable: [name: "TEMPLT_SET_NESTED", column: "NESTED_TEMPLT_ID", key: "TEMPLT_SET_ID"], indexColumn: [name: "NESTED_TEMPLT_IDX"], cascade: "none"
    }

    static constraints = {
        nestedTemplates(nullable: false, minSize: 1)
    }

    static namedQueries = {
        usuageByTemplate { ReportTemplate reportTemplate ->
            eq('isDeleted', false)
            'nestedTemplates' {
                eq('id', reportTemplate?.id)
            }
        }

        countUsuageByTemplate { ReportTemplate reportTemplate ->
            projections {
                countDistinct('id')
            }
            usuageByTemplate(reportTemplate)
        }
    }

    Map getChangesForNestedTemplates(theInstance, Map params, String instanceName) {
        String oldNestedTemplates = (params?.oldNestedTemplates) ? params.oldNestedTemplates.toString() : null
        String newNestedTemplates = theInstance.nestedTemplates ? theInstance.nestedTemplates.toString() : null
        if (params && oldNestedTemplates != newNestedTemplates) {
            def value = [entityName   : instanceName,
                         entityId     : theInstance.id,
                         fieldName    : "Nested Templates",
                         originalValue: "${oldNestedTemplates ?: '(None)'}",
                         newValue     : "${newNestedTemplates ?: '(None)'}"]
            return value
        }
        return null
    }

    def getInstanceIdentifierForAuditLog() {
        return name
    }

    @Override
    Set<String> getPOIInputsKeys() {
        Set<String> poiInputsSet = []
        nestedTemplates.each {
            it?.getPOIInputsKeys()?.each {
                poiInputsSet.add(it)
            }
        }
        return poiInputsSet
    }

    @Override
    transient List<ReportFieldInfo> getAllSelectedFieldsInfo() {
        List<ReportFieldInfo> result = []
        nestedTemplates.each { def template ->
            if (template instanceof CaseLineListingTemplate) {
                if (template.columnList?.reportFieldInfoList) {
                    result.addAll(template.columnList?.reportFieldInfoList)
                }
                if (template.groupingList?.reportFieldInfoList) {
                    result.addAll(template.groupingList?.reportFieldInfoList)
                }
                if (template.rowColumnList?.reportFieldInfoList) {
                    result.addAll(template.rowColumnList?.reportFieldInfoList)
                }
                if (template.serviceColumnList?.reportFieldInfoList) {
                    result.addAll(template.serviceColumnList?.reportFieldInfoList)
                }
            } else if (template instanceof DataTabulationTemplate) {
                if (template.groupingList?.reportFieldInfoList) {
                    result.addAll(template.groupingList?.reportFieldInfoList)
                }
                if (template.rowList?.reportFieldInfoList) {
                    result.addAll(template.rowList?.reportFieldInfoList)
                }
            }
        }
        return result.sort { it.setId }
    }

    @Override
    public String toString() {
        super.toString()
    }
}
