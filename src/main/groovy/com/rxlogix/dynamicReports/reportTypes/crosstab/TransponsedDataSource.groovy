package com.rxlogix.dynamicReports.reportTypes.crosstab

import com.rxlogix.CustomMessageService
import com.rxlogix.config.ExecutedDataTabulationTemplate
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField
import net.sf.jasperreports.engine.JRRewindableDataSource
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

class TransponsedDataSource implements JRRewindableDataSource {
    private static String ROW_PREFIX = "ROW_"
    private static String COL_PREFIX = "COL_"

    List<JSONObject> headers

    private Map<String, HeaderTabDTO> originHeaders
    private Map<String, Map<String, String>> columnTree
    private Set<String> measureNames

    private Collection<Map<String, ?>> records
    private Iterator<Map<String, ?>> iterator
    private Map<String, ?> currentRecord

    TransponsedDataSource(ExecutedDataTabulationTemplate executedTemplate, JSONArray tabHeaders, JSONArray tabData) {
        processOriginHeaders(tabHeaders)
        transposeData(executedTemplate, tabData)
        transponseHeaders(executedTemplate, tabData)
        this.iterator = this.records.iterator()
    }

    @Override
    void moveFirst() throws JRException {
        if (this.records != null) {
            this.iterator = this.records.iterator()
        }
    }

    @Override
    boolean next() throws JRException {
        boolean hasNext = false
        if (this.iterator != null) {
            hasNext = this.iterator.hasNext()
            if (hasNext) {
                this.currentRecord = (Map)this.iterator.next()
            }
        }
        return hasNext
    }

    @Override
    Object getFieldValue(JRField field) throws JRException {
        Object value = null
        if (this.currentRecord != null) {
            value = this.currentRecord.get(field.getName())
        }
        return value
    }

    private void transponseHeaders(ExecutedDataTabulationTemplate executedTemplate, JSONArray tabData) {
        CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
        this.headers = new ArrayList<>()
        List<ReportFieldInfo> rowFieldInfos = executedTemplate.selectedFieldsRows
        String columnLabel = rowFieldInfos.inject("") { result, current ->
            if (result != "") {
                result += "\n"
            }
            return result + customMessageService.getMessage("app.reportField." + current.reportField.name)
        }

        StringBuilder firstColumnNameBuilder = new StringBuilder()
        for (int j = 0; j < executedTemplate.selectedFieldsRows.size(); j++) {
            if (firstColumnNameBuilder.size() > 0) {
                firstColumnNameBuilder.append("\n")
            }
            firstColumnNameBuilder.append(" ")
        }

        int rowIndex = 1
        List<ReportFieldInfo> columnList = getColumnList(executedTemplate)
        if (!columnList.empty) {
            addHeader(ROW_PREFIX + rowIndex, firstColumnNameBuilder.toString())
            rowIndex++
        }
        addHeader(ROW_PREFIX + rowIndex, firstColumnNameBuilder.toString() + " ")

        int columnIndex = 1
        tabData.each { def entry ->
            StringBuilder columnNameBuilder = new StringBuilder(columnLabel)
            for (int j = 0; j < executedTemplate.selectedFieldsRows.size(); j++) {
                if (columnNameBuilder.size() > 0) {
                    columnNameBuilder.append("\n")
                }
                def value = entry.get(ROW_PREFIX + (j + 1))
                columnNameBuilder.append(value == null ? ViewHelper.getEmptyLabel() : value)
            }
            if (measureNames) {
                measureNames.each {
                    addHeader(COL_PREFIX + columnIndex, "${columnNameBuilder.toString()}\n(${it})")
                    columnIndex++
                }
            } else {
                addHeader(COL_PREFIX + columnIndex, columnNameBuilder.toString())
                columnIndex++
            }
        }
    }

    private void transposeData(ExecutedDataTabulationTemplate executedTemplate, JSONArray tabData) {
        this.records = new LinkedList<>()
        CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
        List<ReportFieldInfo> columnList = getColumnList(executedTemplate)
        if (columnTree) {
            columnTree.each { Map.Entry<String, Map<String, HeaderTabDTO>> columnGroupEntry ->
                HeaderTabDTO firstHeaderTabDTO = columnGroupEntry.value.values().first()
                Map<String, Object> item = new LinkedHashMap<>()
                int rowIndex = 1
                if (firstHeaderTabDTO.columnIndex && !columnList.empty) {
                    ReportFieldInfo reportFieldInfo = columnList[firstHeaderTabDTO.columnIndex - 1]
                    String reportFieldName = customMessageService.getMessage("app.reportField." + reportFieldInfo.reportField.name)
                    item.put(ROW_PREFIX + rowIndex, reportFieldName)
                    rowIndex++
                }
                item.put(ROW_PREFIX + rowIndex, trimLabel(columnGroupEntry.key))

                int columnIndex = 1
                tabData.each { JSONObject dataEntry ->
                    columnGroupEntry.value.each { Map.Entry<String, HeaderTabDTO> columnEntry ->
                        def value = dataEntry.get(columnEntry.value.columnName)
                        item.put(COL_PREFIX + columnIndex, value)
                        columnIndex++
                    }
                }
                records.add(item)
            }
        } else {
            originHeaders.entrySet().each { Map.Entry<String, HeaderTabDTO> headerEntry ->
                if (!headerEntry.key.startsWith(ROW_PREFIX)) {
                    Map<String, Object> item = new LinkedHashMap<>()
                    HeaderTabDTO headerTabDTO = headerEntry.value

                    int rowIndex = 1
                    if (headerTabDTO.columnIndex && !columnList.empty) {
                        ReportFieldInfo reportFieldInfo = columnList[headerTabDTO.columnIndex - 1]
                        String reportFieldName = customMessageService.getMessage("app.reportField." + reportFieldInfo.reportField.name)
                        item.put(ROW_PREFIX + rowIndex, reportFieldName)
                        rowIndex++
                    }
                    item.put(ROW_PREFIX + rowIndex, trimLabel(headerEntry.value.columnLabel))

                    int columnIndex = 1
                    tabData.each { JSONObject entry ->
                        def value = entry.get(headerEntry.key)
                        item.put(COL_PREFIX + columnIndex, value)
                        columnIndex++
                    }
                    records.add(item)
                }
            }
        }
    }

    private void processOriginHeaders(JSONArray tabHeaders) {
        this.originHeaders = new LinkedHashMap<>()
        for (JSONObject header : tabHeaders) {
            HeaderTabDTO headerTabDTO = new HeaderTabDTO(header)
            originHeaders.put(headerTabDTO.columnName, headerTabDTO)
            if (headerTabDTO.simpleColumnLabel) {
                if (!this.measureNames) {
                    this.measureNames = []
                }
                this.measureNames.add(headerTabDTO.measureName)

                if (!this.columnTree) {
                    this.columnTree = new LinkedHashMap<>()
                }
                Map<String, String> columns = this.columnTree.get(headerTabDTO.simpleColumnLabel)
                if (!columns) {
                    columns = new LinkedHashMap<>()
                    this.columnTree.put(headerTabDTO.simpleColumnLabel, columns)
                }
                columns.put(headerTabDTO.measureName, headerTabDTO)
            }
        }
    }

    private void addHeader(String key, String value) {
        JSONObject header = new JSONObject()
        header.put(key, value)
        this.headers.add(header)
    }

    private static trimLabel(String label) {
        return label
                .replaceAll("[\\r\\n]+", "")
                .trim()
    }

    private static List<ReportFieldInfo> getColumnList(ExecutedDataTabulationTemplate executedTemplate) {
        List<ReportFieldInfo> result = []
        executedTemplate.columnMeasureList.each {
            if (it.columnList) {
                result.addAll(it.columnList*.reportFieldInfoList.flatten())
            }
        }
        return result
    }
}
