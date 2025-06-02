package com.rxlogix.enums

public enum ActionItemCategoryEnum {
    REPORT_REQUEST,
    REQUEST_MISSING_INFORMATION,
    PROCESS_CASE,
    PERIODIC_REPORT,
    CONFIGURE_REPORT,
    ADHOC_REPORT,
    REVIEW_REPORT,
    QUALITY_MODULE,
    QUALITY_MODULE_PREVENTIVE,
    QUALITY_MODULE_CORRECTIVE,
    DRILLDOWN_RECORD,
    IN_DRILLDOWN_RECORD;

    String getKey() {
        name()
    }

    public String getI18nKey() {
        return "app.actionItemCategory.${this.getKey()}"
    }
}