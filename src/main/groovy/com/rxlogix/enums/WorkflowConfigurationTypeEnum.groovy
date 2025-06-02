package com.rxlogix.enums

import grails.util.Holders

public enum WorkflowConfigurationTypeEnum {

    ADHOC_REPORT("Adhoc Report"),
    PERIODIC_REPORT("Periodic Report"),
    REPORT_REQUEST("Report Request"),
    ICSR_REPORT("Icsr Report"),
    CASE("Case"),
    QUALITY_CASE_DATA("Quality Case Data"),
    QUALITY_SUBMISSION("Quality Submission"),
    QUALITY_SAMPLING("Other Quality Type #1"),
    QUALITY_SAMPLING2("Other Quality Type #2"),
    QUALITY_SAMPLING3("Other Quality Type #3"),
    QUALITY_SAMPLING4("Other Quality Type #4"),
    QUALITY_SAMPLING5("Other Quality Type #5"),
    QUALITY_SAMPLING6("Other Quality Type #6"),
    QUALITY_SAMPLING7("Other Quality Type #7"),
    QUALITY_SAMPLING8("Other Quality Type #8"),
    QUALITY_SAMPLING9("Other Quality Type #9"),
    QUALITY_SAMPLING10("Other Quality Type #10"),
    QUALITY_SAMPLING11("Other Quality Type #11"),
    QUALITY_SAMPLING12("Other Quality Type #12"),
    QUALITY_SAMPLING13("Other Quality Type #13"),
    QUALITY_SAMPLING14("Other Quality Type #14"),
    QUALITY_SAMPLING15("Other Quality Type #15"),
    PVC_REASON_OF_DELAY("PV Central: Reason of Delay"),
    PVC_INBOUND("PV Central: Inbound Compliance"),
    PUBLISHER_SECTION("Publisher Section"),
    PUBLISHER_FULL("Publisher Document"),
    PUBLISHER_FULL_QC("Publisher Document QC")

    final String value

    WorkflowConfigurationTypeEnum(String value) {
        this.value = value
    }

    String toString() {
        value
    }

    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.workflowConfigurationType.${this.name()}"
    }

    static WorkflowConfigurationTypeEnum getAdditional(Integer n) {
        if (n > 1) return valueOf("QUALITY_SAMPLING" + n)
        return QUALITY_SAMPLING
    }

    static List<WorkflowConfigurationTypeEnum> getAllPVReports() {
        List<WorkflowConfigurationTypeEnum> list = [ADHOC_REPORT, PERIODIC_REPORT, REPORT_REQUEST, ICSR_REPORT]
        return values().findAll { it in list }
    }

    static List<WorkflowConfigurationTypeEnum> getAllPVCentral() {
        List<WorkflowConfigurationTypeEnum> list = [PVC_REASON_OF_DELAY, PVC_INBOUND]
        return values().findAll { it in list }
    }

    static List<WorkflowConfigurationTypeEnum> getAllPVPublisher() {
        List<WorkflowConfigurationTypeEnum> list = [PUBLISHER_SECTION,PUBLISHER_FULL,PUBLISHER_FULL_QC]
        return  values().findAll { it in list }
    }

    static List<WorkflowConfigurationTypeEnum> getAllPVQuality() {
        List<WorkflowConfigurationTypeEnum> list = [QUALITY_SAMPLING,QUALITY_SUBMISSION,QUALITY_CASE_DATA]
        for(int n=2;n<16;n++){
            list.add(valueOf("QUALITY_SAMPLING" + n));
        }
        return values().findAll { it in list }
    }

    static List<WorkflowConfigurationTypeEnum> getAllQuality() {
        List<WorkflowConfigurationTypeEnum> list = [QUALITY_SAMPLING,QUALITY_SUBMISSION,QUALITY_CASE_DATA]
        for(int n=2;n<16;n++){
            list.add(valueOf("QUALITY_SAMPLING" + n));
        }
        return list
    }

    static String getSamplingNameByType(WorkflowConfigurationTypeEnum val) {
        if (val in getAllQuality() && !(val in [QUALITY_SUBMISSION, QUALITY_CASE_DATA])) {
            if (val == QUALITY_SAMPLING) return Holders.config.qualityModule.additional.find { it.workflow == 1 }?.name
            int number = Integer.parseInt(val.key.substring(16))
            return Holders.config.qualityModule.additional.find { it.workflow == number }?.name
        }
    }
}
