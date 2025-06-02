package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

class CaseSeriesDateRangeInformation extends BaseDateRangeInformation {
    static propertiesToUseForCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']

    static belongsTo = [caseSeries: CaseSeries]

    static mapping = {
        table name: "CASE_SERIRES_DATE_RANGE_INFO"
    }

    static constraints = {
    }

    @Override
    Date getNextRunDate() {
        return null
    }

    @Override
    Date getAsOfVersionDate() {
        return caseSeries?.asOfVersionDate
    }

    @Override
    String getConfigSelectedTimeZone() {
        return caseSeries.owner.preference?.timeZone?:"UTC"
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        return null
    }

    @Override
    public String toString() {
        super.toString()
    }


}
