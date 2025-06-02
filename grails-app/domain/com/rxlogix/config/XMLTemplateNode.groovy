package com.rxlogix.config


import com.rxlogix.enums.XMLNodeDateFormat
import com.rxlogix.enums.XMLNodeElementType
import com.rxlogix.enums.XMLNodeType
import com.rxlogix.mapping.E2BLocaleName

class XMLTemplateNode implements Comparable<XMLTemplateNode> {
    // Common fields
    XMLNodeElementType elementType
    XMLNodeType type
    String tagName
    String tagColor
    int orderingNumber
    XMLTemplateNode parent
    String e2bElement
    String e2bElementName
    E2BLocaleName e2bElementNameLocale
    String sourceFieldLabel
    String sourceFieldLabelVal

    // Tag Properties fields
    ReportTemplate template
    ReportFieldInfo filterFieldInfo

    // Source Field fields
    ReportFieldInfo reportFieldInfo
    String customSQLFieldInfo
    String customSQLFilterFiledInfo
    XMLNodeDateFormat dateFormat

    // Static value fields
    String value

    static mapping = {
        table name: "XML_TEMPLT_NODE"
        id column: "ID"
        elementType column: "ELEMENT_TYPE"
        type column: "TYPE"
        tagName column: "TAG_NAME"
        tagColor column: "TAG_COLOR"
        orderingNumber column: "ORDERING_NUMBER", defaultValue: 0
        template column: "CLL_TEMPLT_ID",cascade: "none"
        filterFieldInfo column: "FILTER_FIELD_INFO_ID"
        reportFieldInfo column: "RPT_FIELD_INFO_ID"
        dateFormat column: "DATE_FORMAT"
        value column: "VALUE"
        customSQLFieldInfo column: "CUSTOM_SQL_FIELD_INFO_ID"
        customSQLFilterFiledInfo column: "CUSTOM_SQL_FILTER_FIELD_INFO"
        e2bElement column: "E2B_ELEMENT"
        e2bElementName column: "E2B_ELEMENT_NAME"
        e2bElementNameLocale column: "E2B_ELEMENT_NAME_LOCALE_ID"
        sourceFieldLabel column: "SOURCE_FIELD_LABEL"
        sourceFieldLabelVal column: "SOURCE_FIELD_LABEL_VAL"
        children cascade: 'all-delete-orphan', sort: "orderingNumber"
    }

    //static belongsTo = [parent: XMLTemplateNode]
    static hasMany = [children: XMLTemplateNode]
    static mappedBy = [children: 'parent']

    static constraints = {
        template nullable: true
        filterFieldInfo nullable: true
        reportFieldInfo nullable: true, validator: { val, obj ->
            if (val && !obj.template) {
                return "com.rxlogix.config.xmltemplatenode.reportfield.template.invalid"
            }
        }
        dateFormat nullable: true
        value nullable: true
        tagColor nullable: true
        customSQLFieldInfo nullable: true
        customSQLFilterFiledInfo nullable: true
        e2bElement nullable: true
        e2bElementName nullable: true
        e2bElementNameLocale nullable: true
        sourceFieldLabel nullable: true
        sourceFieldLabelVal nullable: true
    }

    @Override
    int compareTo(XMLTemplateNode o) {
        return orderingNumber <=> o.orderingNumber
    }

    transient boolean isPrimary(){
        return (!parent)
    }

    public String toString() {
        return tagName
    }
}
