package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.IcsrXmlService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.reportTypes.xml.JRFilterableDataSourceWrapper
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.XMLNodeElementType
import com.rxlogix.enums.XMLNodeType
import com.rxlogix.util.DateUtil
import grails.util.Holders
import groovy.util.logging.Slf4j
import groovy.xml.XmlUtil
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField
import net.sf.jasperreports.engine.design.JRDesignField
import net.sf.jasperreports.engine.design.JasperDesign
import org.grails.core.exceptions.GrailsRuntimeException
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import com.rxlogix.Constants
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Date
import java.util.Locale
import com.rxlogix.util.MiscUtil
/**
 * Created by gologuzov on 25.05.17.
 */
@Slf4j
class XMLReportOutputBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {

    static final String XML_IDENTIFIER_TOKEN='-'

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    IcsrXmlService icsrXmlService = Holders.applicationContext.getBean("icsrXmlService")

    @Override
    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        return null
    }

    @Override
    void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang) {

    }

    @Override
    void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {

    }

    File produceReportOutput(ExecutedIcsrTemplateQuery executedIcsrTemplateQuery, byte[] data, Map params, String reportName, String lang, File reportFile) {
        if (!data) {
            return null
        }
        if (!params.caseNumber || !params.versionNumber || params.versionNumber.toString().toInteger() < 1) {
            throw new GrailsRuntimeException("Invalid version for ${executedIcsrTemplateQuery?.id} ${params.caseNumber} ${params.versionNumber} for XML generation")
        }
        ExecutedXMLTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(executedIcsrTemplateQuery.executedTemplate)
        TemplateSetCsvDataSource mainDataSource
        if (executedTemplate.nestedTemplates.size() > 0) {
            if (executedTemplate.nestedTemplates.any { it.templateType == TemplateTypeEnum.CASE_LINE }) {
                CaseLineListingTemplate mainExecutedCaseLineListingTemplate = GrailsHibernateUtil.unwrapIfProxy(executedTemplate.nestedTemplates.find {
                    it.templateType == TemplateTypeEnum.CASE_LINE
                })
                mainDataSource = createDataSource(data, getGroupingColumnName(mainExecutedCaseLineListingTemplate), params.caseNumber)
            } else if (executedTemplate.nestedTemplates.any { it.templateType == TemplateTypeEnum.CUSTOM_SQL }) {
                mainDataSource = createDataSource(data, "Case Number", params.caseNumber)
            }
        } else {
            mainDataSource = new TemplateSetCsvDataSource()
        }
        Writer writer
        try {
            writer = new OutputStreamWriter(new FileOutputStream(reportFile))
            Map<Long, JRDataSource> dataSourceMap = new HashMap<>()
            String xsltName = MiscUtil.unwrapProxy(executedIcsrTemplateQuery.executedConfiguration).xsltName
            List<Node> nodes = makeXmlNodes(executedTemplate.rootNode, null, executedTemplate, mainDataSource, dataSourceMap, xsltName)
            log.info("XMLReportOutputBuilder: messageIdentifier for batchNumber = ${executedIcsrTemplateQuery.id}${XML_IDENTIFIER_TOKEN}${params.caseNumber}${XML_IDENTIFIER_TOKEN}${params.versionNumber}")
            log.info("XMLReportOutputBuilder: executedIcsrTemplateQuery = ${params.exIcsrTemplateQueryId}")
            if (nodes.size() > 0) {
                Node rootNode = nodes.first()
                icsrXmlService.replaceGenerationPlaceHoldersInXmlNodes(rootNode, "${executedIcsrTemplateQuery.id}${XML_IDENTIFIER_TOKEN}${params.caseNumber}${XML_IDENTIFIER_TOKEN}${params.versionNumber}", xsltName)
                XmlUtil.serialize(rootNode, new PrintWriter(writer))
            }
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
        return reportFile
    }

    private List<Node> makeXmlNodes(XMLTemplateNode templateNode, Node parent, ExecutedXMLTemplate executedTemplate,
                                    TemplateSetCsvDataSource mainDataSource, Map<Long, JRDataSource> dataSourceMap, String xsltName) {
        List<Node> nodes = []
        switch (templateNode.type) {
            case XMLNodeType.TAG_PROPERTIES:
                if (templateNode?.template) {
                    if (templateNode.primary) {
                        if (parent) {
                            while (mainDataSource.next()) {
                                processDataSource(templateNode, parent, executedTemplate, mainDataSource, dataSourceMap, nodes, xsltName)
                            }
                        } else {
                            mainDataSource.next()
                            processDataSource(templateNode, parent, executedTemplate, mainDataSource, dataSourceMap, nodes, xsltName)
                        }
                    } else {
                        processDataSource(templateNode, parent, executedTemplate, mainDataSource, dataSourceMap, nodes, xsltName)
                    }
                } else {
                    Node xmlNode = new Node(parent, templateNode.tagName)
                    templateNode.children?.each {
                        makeXmlNodes(it, xmlNode, executedTemplate, mainDataSource, dataSourceMap, xsltName)
                    }
                    nodes.add(xmlNode)
                }
                break
            case XMLNodeType.SOURCE_FIELD:
                ReportTemplate template = templateNode.template
                ReportFieldInfo reportFieldInfo = templateNode.reportFieldInfo
                String customSQLFieldInfo = templateNode.customSQLFieldInfo
                def value
                if (reportFieldInfo?.reportField) {
                    value = getFieldValue(dataSourceMap.get(template.id), reportFieldInfo)
                } else if (customSQLFieldInfo) {
                    value = getFieldValue(dataSourceMap.get(template.id), customSQLFieldInfo)
                } else {
                    value = templateNode.value
                }
                if (templateNode.elementType == XMLNodeElementType.ATTRIBUTE) {
                    parent.attributes().put(templateNode.tagName, value)
                } else {
                    Node xmlNode = new Node(parent, templateNode.tagName)
                    if (value) {
                        xmlNode.value = formatValue(value, templateNode, xsltName)
                    } else {
                        xmlNode.value = ""
                    }
                    templateNode.children?.each {
                        makeXmlNodes(it, xmlNode, executedTemplate, mainDataSource, dataSourceMap, xsltName)
                    }
                    nodes.add(xmlNode)
                }
                break
            default:
                break
        }
        return nodes
    }

    private processDataSource(XMLTemplateNode templateNode, Node parent, ExecutedXMLTemplate executedTemplate,
                              TemplateSetCsvDataSource mainDataSource, Map<Long, JRDataSource> dataSourceMap,
                              List<Node> nodes , String xsltName) {
        JRDataSource dataSource = null
        Long sectionTemplateId = null
        if (templateNode.template.templateType == TemplateTypeEnum.CASE_LINE) {
            ExecutedCaseLineListingTemplate sectionTemplate = GrailsHibernateUtil.unwrapIfProxy(templateNode.template)
            if (!sectionTemplate) {
                return
            }
            sectionTemplateId = sectionTemplate.id
            dataSource = mainDataSource.getSubreportDataSource(sectionTemplate.id, sectionTemplate.allSelectedFieldsInfo.collect {
                it.uniqueIdentifierXmlTag()
            })
            //Removed dependency on original templateid node due to this old generated xmls might not work.
        } else if (templateNode.template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            ExecutedCustomSQLTemplate sectionTemplate = GrailsHibernateUtil.unwrapIfProxy(templateNode.template)
            if (!sectionTemplate) {
                return
            }
            sectionTemplateId = sectionTemplate.id
            dataSource = mainDataSource.getSubreportDataSource(sectionTemplate.id, sectionTemplate.columnNamesList.replaceAll(~/[\[\]]/, '').split(",").collect {
                it.trim()
            })
        }
        // Need allSelectedFieldsInfo here due to version column avaialable in service list. Also added hyphen with reportFieldInfo id to uniquely identify field.

        if (templateNode.filterFieldInfo?.reportField) {
            ReportFieldInfo filterFieldInfo = templateNode.filterFieldInfo
            Object criteriaValue = getFilterCriteriaValue(templateNode, filterFieldInfo, dataSourceMap)
            if (criteriaValue) {
                JRFilterableDataSourceWrapper wrapper = new JRFilterableDataSourceWrapper(dataSource)
                wrapper.addFilter(getJRField(filterFieldInfo), criteriaValue)
                dataSource = wrapper
            }
        } else if(templateNode.customSQLFilterFiledInfo) {
            Object criteriaValue = getFilterCriteriaValue(templateNode, templateNode.customSQLFilterFiledInfo, dataSourceMap)
            if (criteriaValue) {
                JRFilterableDataSourceWrapper wrapper = new JRFilterableDataSourceWrapper(dataSource)
                wrapper.addFilter(getJRField(templateNode.customSQLFilterFiledInfo), criteriaValue)
                dataSource = wrapper
            }
        }
        dataSourceMap.put(sectionTemplateId, dataSource)
        while (dataSource?.next()) {
            Node xmlNode = new Node(parent, templateNode.tagName)
            templateNode.children?.each {
                makeXmlNodes(it, xmlNode, executedTemplate, mainDataSource, dataSourceMap, xsltName)
            }
            nodes.add(xmlNode)
        }
    }

    private static TemplateSetCsvDataSource createDataSource(byte[] data, String caseNumberColumnName, String caseNumber) {
        if (data) {
            return new TemplateSetCsvDataSource(new BufferedInputStream(new ByteArrayInputStream(data)), caseNumberColumnName, caseNumber)
        }
        return new TemplateSetCsvDataSource(null, caseNumberColumnName)
    }

    private static getGroupingColumnName(CaseLineListingTemplate executedCaseLineListingTemplate) {
        if (executedCaseLineListingTemplate.groupingList.reportFieldInfoList?.size() == 1) {
            List<String> fieldNameWithIndex = executedCaseLineListingTemplate.getFieldNameWithIndex()
            int columnLength = executedCaseLineListingTemplate.columnList.reportFieldInfoList.size()
            return fieldNameWithIndex[columnLength]
        } else {
            throw new RuntimeException("Subreport does not have only one grouping column")
        }
    }

    private def getFieldValue(JRDataSource dataSource, ReportFieldInfo reportFieldInfo) {
        JRField field = getJRField(reportFieldInfo)
        def value = dataSource.getFieldValue(field)
        if (Enum.class.isAssignableFrom(reportFieldInfo.reportField?.dataType)) {
            value = reportFieldInfo.reportField?.dataType?.fromString(value)
        }
        return value
    }

    private def getFieldValue(JRDataSource dataSource, String fieldName) {
        JRField field = getJRField(fieldName)
        def value = dataSource.getFieldValue(field)
        return value
    }

    private JRField getJRField(ReportFieldInfo reportFieldInfo) {
        JRDesignField field = new JRDesignField(name: reportFieldInfo.uniqueIdentifierXmlTag())
        Class dataType = reportFieldInfo.reportField?.dataType
        if (dataType == String.class
                || dataType == Boolean.class
                || Number.class.isAssignableFrom(dataType)
                || Date.class.isAssignableFrom(dataType)) {
            field.setValueClass(dataType)
        }
        return field
    }

    private JRField getJRField(String fieldName) {
        JRDesignField field = new JRDesignField(name: fieldName)
        field.setValueClass(String.class)
        return field
    }

    private String formatValue(def value, XMLTemplateNode templateNode, String xsltName) {
        if (value instanceof Date) {

            if (xsltName && xsltName.equals(Constants.PMDA)) {
                return new SimpleDateFormat('yyyyMMddHHmmss') {{
                    setTimeZone(TimeZone.getTimeZone('Asia/Tokyo'))
                }}.format(value)
            }
            return DateUtil.toDateString(value, 'yyyyMMdd')
        }
        return String.valueOf(value)
    }

    private Object getFilterCriteriaValue(XMLTemplateNode templateNode, ReportFieldInfo filterFieldInfo,  Map<Long, JRDataSource> dataSourceMap) {
        Object criteriaValue
        XMLTemplateNode parentTemplateNode = templateNode.parent
        while (criteriaValue == null && parentTemplateNode != null) {
            ReportTemplate parentTemplate = parentTemplateNode.template
            if (parentTemplate) {
                JRDataSource parentDataSource = dataSourceMap.get(parentTemplate.id)
                if (parentDataSource) {
                    try {
                        criteriaValue = getFieldValue(parentDataSource, filterFieldInfo)
                    } catch (JRException e) {
                        // Do nothing, the datasource does not have the filterField
                    }
                }
            }
            parentTemplateNode = parentTemplateNode.parent
        }
        return criteriaValue
    }

    private Object getFilterCriteriaValue(XMLTemplateNode templateNode, String filterFieldInfo,  Map<Long, JRDataSource> dataSourceMap) {
        Object criteriaValue
        XMLTemplateNode parentTemplateNode = GrailsHibernateUtil.unwrapIfProxy(templateNode.parent)
        while (criteriaValue == null && parentTemplateNode != null) {
            ReportTemplate parentTemplate = GrailsHibernateUtil.unwrapIfProxy(parentTemplateNode.template)
            if (parentTemplate) {
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo()
                List<ReportFieldInfo> reportFieldInfoList = parentTemplate.allSelectedFieldsInfo.collect { it }
                reportFieldInfoList.find {
                    String label = it.renameValue ?: it.reportField.getDisplayName(Locale.ENGLISH)
                    if(label.trim() == filterFieldInfo.trim()) {
                        reportFieldInfo = it
                    }
                    if(!reportFieldInfo){
                        ReportTemplate currTemplt = GrailsHibernateUtil.unwrapIfProxy(templateNode.template)
                        log.info("${filterFieldInfo} reportfield not present in template ${currTemplt?.name}")
                    }
                }
                JRDataSource parentDataSource = dataSourceMap.get(parentTemplate.id)
                if (parentDataSource) {
                    try {
                        criteriaValue = getFieldValue(parentDataSource, reportFieldInfo)
                    } catch (JRException e) {
                        // Do nothing, the datasource does not have the filterField
                    }
                }
            }
            parentTemplateNode = parentTemplateNode.parent
        }
        return criteriaValue
    }
}
