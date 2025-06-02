package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil
import grails.util.Holders
import org.springframework.context.MessageSourceResolvable


enum PeriodicReportTypeEnum implements MessageSourceResolvable {

    PBRER,
    IND,
    DSUR,
    PADER,
    PSUR,
    JPSR,
    JDSUR,
    NUPR,
    ACO,
    VOLUME9A,
    SUSAR,
    ADDENDUM,
    CSUR,
    OTHER,
    RESD

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.periodicReportType.${this.name()}"
    }

    @Override
    Object[] getArguments() { [] as Object[] }

    @Override
    String[] getCodes() {
        [getI18nKey()] as String[]
    }

    @Override
    String getDefaultMessage() { name() }

    static List<PeriodicReportTypeEnum> getAsList() {
        List result = values().sort { it.ordinal() }
        return Holders.config.getProperty('reportTypes.additional.hide', Boolean) ? (result - [JPSR, JDSUR, NUPR, PSUR, RESD]) : result
    }

    public static List<QueryTypeEnum> searchBy(String search) {
        if (!search) {
            return []
        }
        search = search.toLowerCase()
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        getAsList().findAll { customMessageService.getMessage(it.i18nKey).toLowerCase().contains(search) }
    }
}
