package com.rxlogix.enums

import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.IcsrReportConfiguration

enum ReportFormatEnum {

    HTML("html", "HTML"),
    PDF("pdf", "PDF"),
    XLSX("xlsx", "Excel"),
    DOCX("docx", "Word"),
    PPTX("pptx", "PowerPoint"),
    XML("xml", "XML"),
    ZIP("zip", "ZIP"),
    R3XML("r3xml", "R3XML"),
    CSV("csv", "CSV")

    final String key
    final String displayName

    ReportFormatEnum(String key, String displayName){
        this.key = key
        this.displayName = displayName
    }

    public getI18nKey() {
        return "app.reportFormat.${this.name()}"
    }

    static List<ReportFormatEnum> getEmailShareOptions(Class aClass = null) {
        List ignoreFormats = [HTML, XML, R3XML, ZIP, CSV]

        if (aClass in [IcsrProfileConfiguration, IcsrReportConfiguration]) {
            return [PDF, XML, R3XML]
        }

        return values().findAll { !(it in ignoreFormats) }
    }

    static List<ReportFormatEnum> getEmailSubmitOptions() {
        return [PDF, XLSX, DOCX]
    }
    static List<ReportFormatEnum> getCapa8dShareOptions() {
        List ignoreFormats = [HTML]
        ignoreFormats.add(XML)
        ignoreFormats.add(ZIP)
        return values().findAll {!(it in ignoreFormats)}
    }
}