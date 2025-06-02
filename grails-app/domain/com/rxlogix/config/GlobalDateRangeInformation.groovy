package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
class GlobalDateRangeInformation extends BaseDateRangeInformation {

    static propertiesToUseForCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']

    static belongsTo = [reportConfiguration: ReportConfiguration]

    static mapping = {
        table name: "GLOBAL_DATE_RANGE_INFO"
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
        return reportConfiguration?.nextRunDate
    }

    @Override
    Date getAsOfVersionDate() {
        return reportConfiguration?.asOfVersionDate
    }

    @Override
    String getConfigSelectedTimeZone() {
        return reportConfiguration?.configSelectedTimeZone
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        return reportConfiguration?.globalDateRangeInformation
    }

    @Override
    public String toString() {
        super.toString()
    }
}
