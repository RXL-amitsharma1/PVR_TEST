package com.rxlogix.enums

/**
 * Created by Chetan on 3/1/2016.
 */
enum AppTypeEnum {

    REPORT_REQUEST("REPORT_REQUEST"),
    PERIODIC_REPORT("PERIODIC_REPORT"),
    ADHOC_REPORT("ADHOC_REPORT"),
    QUALITY_MODULE("QUALITY_MODULE"),
    QUALITY_MODULE_CAPA("QUALITY_MODULE_CAPA"),
    DRILLDOWN_RECORD("DRILLDOWN_RECORD"),
    IN_DRILLDOWN_RECORD("IN_DRILLDOWN_RECORD"),
    PV_CENTRAL_CAPA("PV_CENTRAL_CAPA"),
    ACTION_PLAN("ACTION_PLAN")

    final String value

    AppTypeEnum(String value){
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public String getI18nKey() {
        return "app.actionItemAppType.${this.name()}"
    }

}