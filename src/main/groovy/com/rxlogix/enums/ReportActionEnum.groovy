package com.rxlogix.enums

import com.rxlogix.util.ViewHelper
import org.springframework.context.MessageSourceResolvable

public enum ReportActionEnum implements MessageSourceResolvable {

    GENERATE_DRAFT,
    GENERATE_CASES,
    GENERATE_CASES_DRAFT,
    GENERATE_FINAL,
    GENERATE_CASES_FINAL,
    MARK_AS_SUBMITTED,
    SEND_TO_DMS,
    ARCHIVE,
    PUBLISHER_GEN_DRAFT,
    PUBLISHER_FINAL

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.reportActionType.${this.name()}"
    }

    @Override
    Object[] getArguments() { [] as Object[] }

    @Override
    String[] getCodes() {
        [getI18nKey()] as String[]
    }

    @Override
    String getDefaultMessage() { name() }

    static List<ReportActionEnum> getAsList() {
        return values().sort { it.ordinal() }
    }

    String getDisplayName() {
        ViewHelper.getMessage(this.getI18nKey())
    }

}