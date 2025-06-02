package com.rxlogix.util.marshalling

import com.rxlogix.config.XMLTemplateNode
import com.rxlogix.util.MiscUtil
import grails.converters.JSON

/**
 * Created by gologuzov on 03.10.17.
 */
class XMLTemplateNodeMarshaller {
    void register() {
        JSON.registerObjectMarshaller(XMLTemplateNode) { XMLTemplateNode item ->
            marshalNode(item)
        }
    }

    private Map<String, ?> marshalNode(XMLTemplateNode node) {
        def map = [:]
        map['key'] = node.id
        map['title'] = node.tagName
        def data = [:]
        data['elementType'] = node.elementType?.name()
        data['type'] = node.type?.name()
        data['orderingNumber'] = node.orderingNumber
        data['tagColor'] = node.tagColor

        data['templateId'] = node.template?.id
        data['e2bElement'] = node.e2bElement
        data['e2bElementName'] = node.e2bElementName
        data['e2bLocaleElementName'] = node.e2bElementNameLocale?.e2bLocaleElementName
        data['e2bLocale'] = node.e2bElementNameLocale?.e2bLocale
        data['sourceFieldLabel'] = node.sourceFieldLabel
        data['sourceFieldLabelVal'] = node.sourceFieldLabelVal

        if (node.filterFieldInfo) {
            data['filterFieldInfo'] = MiscUtil.getObjectProperties(node.filterFieldInfo)
            data['filterFieldInfo'].id = node.filterFieldInfo.id
            if (node.filterFieldInfo?.reportField) {
                data['filterFieldInfo'].reportField = [id: node.filterFieldInfo.reportField.id, name: node.filterFieldInfo.reportField.name]
            }

            if (node.filterFieldInfo?.customField) {
                data['filterFieldInfo'].customField = [id: node.filterFieldInfo.customField.id, name: node.filterFieldInfo.customField.name]
            }
        }

        if (node.reportFieldInfo) {
            data['reportFieldInfo'] = MiscUtil.getObjectProperties(node.reportFieldInfo)
            data['reportFieldInfo'].id = node.reportFieldInfo.id
            if (node.reportFieldInfo.reportField) {
                data['reportFieldInfo'].reportField = [id: node.reportFieldInfo.reportField.id, name: node.reportFieldInfo.reportField.name]
            }

            if (node.reportFieldInfo.customField) {
                data['reportFieldInfo'].customField = [id: node.reportFieldInfo.customField.id, name: node.reportFieldInfo.customField.name]
            }
        }

        if (node.customSQLFieldInfo) {
            data['customSQLFieldInfo'] = [id: node.customSQLFieldInfo, text: node.customSQLFieldInfo]
        }

        if (node.customSQLFilterFiledInfo) {
            data['customSQLFilterFiledInfo'] = [id: node.customSQLFilterFiledInfo, text: node.customSQLFilterFiledInfo]
        }

        data['value'] = node.value
        data['dateFormat'] = node.dateFormat?.name()
        map['data'] = data
        map['children'] = node.children?.sort().collect {
            marshalNode(it)
        }
        return map
    }
}
