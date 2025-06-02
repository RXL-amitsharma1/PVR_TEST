package com.rxlogix.enums

enum TaskTemplateTypeEnum {
        REPORT_REQUEST,
        AGGREGATE_REPORTS,
        PUBLISHER_SECTION

    String getKey() {
        name()
    }

    public String getI18nKey() {
        return "app.TaskTemplateTypeEnum.${this.name()}"
    }
}
