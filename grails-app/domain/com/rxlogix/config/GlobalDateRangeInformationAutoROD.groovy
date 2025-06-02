package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

class GlobalDateRangeInformationAutoROD extends BaseDateRangeInformation {
    static propertiesToUseForCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']

    static belongsTo = [autoReasonOfDelay : AutoReasonOfDelay]

    static mapping = {
        table name: "GLOBAL_DATE_RANGE_AUTOROD"
    }

    static constraints = {
    }

    def beforeValidate() {
        if (!DateRangeEnum.relativeDateOperatorsWithX.contains(dateRangeEnum)) {
            relativeDateRangeValue = 1
        }

    }

    @Override
    Date getNextRunDate() {
        return autoReasonOfDelay?.nextRunDate
    }

    @Override
    Date getAsOfVersionDate() {
        return null;
    }

    @Override
    String getConfigSelectedTimeZone() {
        return null;
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        return autoReasonOfDelay?.globalDateRangeInformationAutoROD
    }

    @Override
    public String toString() {
        super.toString()
    }
}