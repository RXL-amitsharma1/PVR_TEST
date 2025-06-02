package com.rxlogix.dynamicReports.reportTypes

import grails.converters.JSON
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField
import net.sf.jasperreports.engine.data.JRAbstractTextDataSource
import net.sf.jasperreports.engine.data.JRCsvDataSource
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.grails.web.json.JSONObject

import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

/**
 * Created by gologuzov on 26.02.16.
 */
@Slf4j
class TemplateSetCsvDataSource extends JRAbstractTextDataSource {
    static final String GROUPING_FILE_NAME = "grouping.json"

    private String caseNumberColumnName
    private String caseNumber
    private TarArchiveInputStream tarArchiveInputStream
    private Map<String, byte[]> subreportData

    TemplateSetCsvDataSource() {
        subreportData = new HashMap<>()
    }
    private JSONObject groupingData

    /**
     * InputStream represents a .tar.gz file which is parsed.
     * 1. Example for report with grouping:
     * data.tar.gz
     *  |
     *  |-> -575612968.tar.gz
     *  |   |-> grouping.json
     *  |   |-> 3738.csv
     *  |   |-> 3744.csv
     *  |-> -756078086.tar.gz
     *      |-> grouping.json
     *      |-> 3738.csv
     *      |-> 3744.csv
     * 2. Example for non grouped report:
     * data.tar.gz
     *  |
     *  |-> report.tar.gz
     *      |-> 3738.csv
     *      |-> 3744.csv
     *
     * @param is
     * @param caseNumberColumnName
     */
    TemplateSetCsvDataSource(InputStream is, String caseNumberColumnName, String caseNumber = null) {
        subreportData = new HashMap<>()
        this.caseNumberColumnName = caseNumberColumnName
        this.caseNumber = caseNumber
        if (is) {
            tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(is))
        }
    }

    @Override
    boolean next() throws JRException {
        subreportData.clear()
        if (tarArchiveInputStream) {
            TarArchiveEntry entry = tarArchiveInputStream.nextTarEntry
            if (caseNumber != null) {
                while (entry && entry.name != ('./' + caseNumber.hashCode() + '.tar.gz')) { //TODO need to add better handling
                    entry = tarArchiveInputStream.nextEntry
                }
            }
            if (entry) {
                fillSubreportData()
            }
            if (entry) {
                return true
            }
            tarArchiveInputStream.close()
            return false
        }
        return false
    }

    @Override
    Object getFieldValue(JRField jrField) throws JRException {
        // As collection while creating dataSource columns in XMLReportOutputBuilder using hyphen (uniqueIdentifierXmlTag) with ReportFieldInfoId but grouping data doesn't contain reportFieldInfo.
        String actualFieldName = StringUtils.substringBefore(jrField.name, "-")
        if (groupingData) {
            return groupingData[actualFieldName]
        } else if (caseNumberColumnName?.equals(actualFieldName) && caseNumber) {
            return caseNumber
        }
        // Support only Grouping based CSV data.
        return null
    }

    JRDataSource getSubreportDataSource(long subreportId, List<String> columnNames) {
        def data = subreportData.get(subreportId + ".csv")
        if (data) {
            JRCsvDataSource subDataSource = new JRCsvDataSource(new ByteArrayInputStream(data), StandardCharsets.UTF_8.name())
            subDataSource.setColumnNames(columnNames.toArray(new String[columnNames.size()]))
            subDataSource.setDatePattern("yyyy-MM-dd'T'HH:mm:ssZ")
            return subDataSource
        }
        return new JREmptyDataSource(0)
    }

    private void fillSubreportData() {
        TarArchiveInputStream subreportsInputStream = null
        try {
            subreportsInputStream = new TarArchiveInputStream(new GZIPInputStream(tarArchiveInputStream))
            TarArchiveEntry entry = subreportsInputStream.getNextTarEntry();
            while (entry != null) {
                String fileName = entry.name
                if (fileName.contains("/")) {
                    fileName = fileName.substring(fileName.indexOf("/") + 1)
                }
                byte[] data = IOUtils.toByteArray(subreportsInputStream)
                if (fileName == GROUPING_FILE_NAME) {
                    groupingData = JSON.parse(new InputStreamReader(new ByteArrayInputStream(data)))
                } else {
                    subreportData.put(fileName, data)
                }
                entry = subreportsInputStream.getNextTarEntry();
            }
        } catch (Exception e) {
            log.error("Error while reading for subreport in TemplateSetCSV: ${e.message}")
            //TODO: Primitive way to catch this. Need a more elegant way to handle streams for Jasper report generation
            subreportsInputStream?.close()
            tarArchiveInputStream?.close()
        }
    }
}