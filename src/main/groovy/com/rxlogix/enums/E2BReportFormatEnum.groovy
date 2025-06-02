package com.rxlogix.enums

public enum E2BReportFormatEnum {
    EB_XML("E2B XML"),
    EB_PDF("E2B PDF"),
    PDF("PDF")

    private final String val

    E2BReportFormatEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.ebReportFormat.${this.name()}"
    }
}