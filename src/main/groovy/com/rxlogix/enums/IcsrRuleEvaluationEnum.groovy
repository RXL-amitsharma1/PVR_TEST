package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil

enum IcsrRuleEvaluationEnum {
    DEVICE_REPORTING,
    PRODUCT_LEVEL,
    CLINICAL_RESEARCH_MEASURE_REPORT

    public getI18nKey() {
        return "app.icsr.rule.evaluation.${this.name()}"
    }

    String getKey() {
        name()
    }

    static List<IcsrRuleEvaluationEnum> getAsList(boolean isJapanProfile) {
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        List<IcsrRuleEvaluationEnum> values = []
        if (isJapanProfile) {
            values = values() - [DEVICE_REPORTING]
        } else {
            values = values() - [CLINICAL_RESEARCH_MEASURE_REPORT]
        }
        return values.collect { [id: it.key, name: customMessageService.getMessage(it.getI18nKey())] }
    }

}