package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil

public enum QueryTypeEnum {
    QUERY_BUILDER,
    SET_BUILDER,
    CUSTOM_SQL

    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.queryType.${this.name()}"
    }

    public static List<QueryTypeEnum> searchBy(String search) {
        if (!search) {
            return []
        }
        search = search.toLowerCase()
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        values().findAll { customMessageService.getMessage(it.i18nKey).toLowerCase().contains(search) }
    }
}

