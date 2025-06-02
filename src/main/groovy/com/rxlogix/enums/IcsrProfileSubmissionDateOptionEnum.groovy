package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil

enum IcsrProfileSubmissionDateOptionEnum {
    MDNPos,
    MDN,
    ACK

    public getI18nKey() {
        return "app.submission.date.${this.name()}"
    }

    String getKey() {
        name()
    }

    static List<IcsrProfileSubmissionDateOptionEnum> getAsList() {
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        return values().collect { [id: it.key, name: customMessageService.getMessage(it.getI18nKey())] }
    }

    static List<IcsrProfileSubmissionDateOptionEnum> getACKRecord() {
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        return [[id: ACK.key, name: customMessageService.getMessage(ACK.getI18nKey())]]
    }
}