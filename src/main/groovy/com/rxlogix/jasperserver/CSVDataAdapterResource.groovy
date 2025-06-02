package com.rxlogix.jasperserver

import com.rxlogix.Constants
import com.rxlogix.ReportExecutorService
import com.rxlogix.config.CaseLineListingTemplate
import com.rxlogix.config.CustomSQLTemplate
import com.rxlogix.config.NonCaseSQLTemplate
import com.rxlogix.config.ReportTemplate
import grails.converters.JSON
import grails.util.Holders
import groovy.xml.XmlUtil

import java.text.MessageFormat

/**
 * Created by gologuzov on 28.02.17.
 */
class CSVDataAdapterResource extends FileResource {
    FileResource csvDataFile
    ReportTemplate template

    private static final String ADAPTER_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<csvDataAdapter class=\"net.sf.jasperreports.data.csv.CsvDataAdapterImpl\">\n" +
            "\t<name>CSV Data Adapter</name>\n" +
            "\t<encoding>UTF-8</encoding>" +
            "\t<fileName>repo:{0}</fileName>\n" +
            "\t<fieldDelimiter>${XmlUtil.escapeXml(",")}</fieldDelimiter>\t"+
            "\t<recordDelimiter>${XmlUtil.escapeXml("\n")}</recordDelimiter>\n" +
            "\t<useFirstRowAsHeader>false</useFirstRowAsHeader>\n" +
            "\t<queryExecuterMode>true</queryExecuterMode>\n" +
            "\t<datePattern>${XmlUtil.escapeXml(Constants.DateFormat.CSV_JASPER)}</datePattern>\n" +
            "{1}" +
            "</csvDataAdapter>"
    private static final String COLUMN_TEMPLATE = "\t<columnNames>{0}</columnNames>"

    @Override
    public FileResourceData copyData() {
        if (!this.data) {
            List<String> columnNamesList = []
            template.refresh()
            if (template instanceof CaseLineListingTemplate) {
                columnNamesList = template?.fieldNameWithIndex
            } else if (template instanceof CustomSQLTemplate || template instanceof NonCaseSQLTemplate) {
                columnNamesList = JSON.parse(template?.columnNamesList)
            }
            StringBuilder columns = new StringBuilder()
            columnNamesList.each {
                columns.append(MessageFormat.format(COLUMN_TEMPLATE, it))
            }
            this.data = MessageFormat.format(ADAPTER_TEMPLATE, csvDataFile?.getURIString(), columns.toString()).getBytes("UTF-8")
        }
        return super.copyData()
    }
}
