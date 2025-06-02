package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.XMLNodeDateFormat
import com.rxlogix.enums.XMLNodeElementType
import com.rxlogix.enums.XMLNodeType
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.json.JsonBuilder

@CollectionSnapshotAudit
class XMLTemplate extends ReportTemplate implements ITemplateSet {
    static transients = ['linkSectionsByGrouping']
    static auditable = true
    XMLTemplateNode rootNode
    // These are ExecutedCaseLineListingTemplates
    Set<ReportTemplate> nestedTemplates

    boolean linkSectionsByGrouping = true

    static hasMany = [nestedTemplates: ReportTemplate]

    static mapping = {
        tablePerHierarchy false
        table name: "XML_TEMPLT"
        id column: "ID"
        rootNode column: "XML_TEMPLT_NODE_ID", cascade: "all-delete-orphan"
        nestedTemplates joinTable: [name: "XML_TEMPLT_CLL", column: "CLL_TEMPLT_ID", key: "XML_TEMPLT_ID"], indexColumn: [name: "NESTED_TEMPLT_IDX"], cascade: "none"
    }

    static constraints = {
        nestedTemplates(nullable: false, minSize: 0)
        rootNode(nullable: false)
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

    boolean isNotExportable(ReportFormatEnum format) {
        return (format in [ReportFormatEnum.XML, ReportFormatEnum.R3XML, ReportFormatEnum.PDF, ReportFormatEnum.HTML])
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
    public String toString() {
        super.toString()
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            newValues.put("rootNode", nodeJson(rootNode))
        }
        if (newValues && oldValues) {
            newValues.put("rootNode", nodeJson(rootNode))
            withNewSession {
                oldValues.put("rootNode", nodeJson(XMLTemplateNode.get(this.rootNode?.id)))
            }
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    String nodeJson(XMLTemplateNode node) {
        new JsonBuilder(nodeToMap(node)).toPrettyString()
    }

    Map nodeToMap(XMLTemplateNode node) {
        if (!node) return null
        Map props = ["elementType", "type", "tagName", "tagColor", "orderingNumber", "e2bElement", "e2bElementName", "e2bElementNameLocale",
                     "sourceFieldLabel", "sourceFieldLabelVal", "customSQLFieldInfo", "customSQLFilterFiledInfo", "dateFormat",
                     "value"].findAll { node."${it}" != null }.collectEntries { [(it): node."${it}"?.toString() ?: ""] }
        if (node.template) props.template = node.template.name + "(id: " + node.template.id + ")"
        if (node.filterFieldInfo?.reportField) props.filterField = node.filterFieldInfo?.reportField?.name
        if (node.reportFieldInfo?.reportField?.name) props.field = node.reportFieldInfo?.reportField?.name
        if (node.e2bElementNameLocale) {
            props.e2bElementNameLocale = node.e2bElementNameLocale.e2bLocale + " - " + node.e2bElementNameLocale.e2bLocaleElementName
        }
        Set children = []
        node.children?.findAll { it }?.each {
            children << nodeToMap(it)
        }
        if (children) props.children = children
        return props
    }
}
