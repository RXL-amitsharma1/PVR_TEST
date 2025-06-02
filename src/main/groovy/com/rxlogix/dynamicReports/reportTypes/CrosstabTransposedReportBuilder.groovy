package com.rxlogix.dynamicReports.reportTypes


import com.rxlogix.config.ExecutedDataTabulationTemplate
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.reportTypes.crosstab.TransponsedDataSource
import grails.converters.JSON
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import org.grails.web.json.JSONArray

import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

class CrosstabTransposedReportBuilder extends CrosstabReportBuilder {

    @Override
    protected JSONArray getTabHeaders(ReportResult reportResult, JasperReportBuilder report) {
        ExecutedDataTabulationTemplate executedTemplate = reportResult.executedTemplateQuery.usedTemplate
        JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
        InputStream inputStream = new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue)))
        JSONArray data = (JSONArray) JSON.parse(inputStream, StandardCharsets.UTF_8.name())
        TransponsedDataSource dataSource = new TransponsedDataSource(executedTemplate, tabHeaders, data)
        report.setDataSource(dataSource)
        return dataSource.headers
    }
}
