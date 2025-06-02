package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil
import org.springframework.context.MessageSourceResolvable


enum ReportSubmissionStatusEnum implements MessageSourceResolvable {

    SUBMITTED,
    SUBMISSION_NOT_REQUIRED,
    PENDING

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.reportSubmissionStatus.${this.name()}"
    }

    @Override
    Object[] getArguments() { [] as Object[] }

    @Override
    String[] getCodes() {
        [getI18nKey()] as String[]
    }

    @Override
    String getDefaultMessage() { name() }

    public static List<QueryTypeEnum> searchBy(String search) {
        if (!search) {
            return []
        }
        search = search.toLowerCase()
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        values().findAll { customMessageService.getMessage(it.i18nKey).toLowerCase().contains(search) }
    }

}
