package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportTemplate
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.design.JasperDesign

/**
 * Created by gologuzov on 24.04.18.
 */
class UnknownReportBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {

    @Override
    void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang) {
    }

    @Override
    void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {

    }

    @Override
    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        return report.toJasperDesign()
    }
}
