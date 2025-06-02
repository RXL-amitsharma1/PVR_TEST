package com.rxlogix.enums


enum ReportRequestTypeEnum {

    PBRER("PBRER"),
    DSUR("DSUR"),
    JPSR("JPSR"),
    JDSUR("JDSUR"),
    NUPR("NUPR"),
    AD_HOC_REPORT("Adhoc Report"),
    DATA_ANALYSIS("Data Analysis"),
    INSPECTION_REQUEST("Inspection Request"),
    HAR("Health Authority Request"),
    DA("Data Analysis"),
    QUERY("Query"),
    Template("Template")

    final String value

    ReportRequestTypeEnum(String value) {
        this.value = value
    }

}