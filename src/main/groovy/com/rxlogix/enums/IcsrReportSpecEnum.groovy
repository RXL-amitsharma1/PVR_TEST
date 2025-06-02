package com.rxlogix.enums

enum IcsrReportSpecEnum {
    E2B_R2("R2"),
    E2B_R3("R3")

    String suffix

    private IcsrReportSpecEnum(String suffix) {
        this.suffix = suffix
    }
}