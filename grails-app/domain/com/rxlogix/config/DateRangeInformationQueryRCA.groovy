package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class DateRangeInformationQueryRCA extends BaseDateRangeInformation {

    DateRangeInformationQueryRCA(){
        this.dateRangeEnum = DateRangeEnum.PR_DATE_RANGE
    }

    static List propertiesToUseWhileCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']

    static belongsTo = [ queryRCA: QueryRCA]

    static mapping = {
        table name: "DATE_RANGE_QUERYRCA"
        queryRCA column: "QUERY_RCA_ID"
    }

    static constraints = {

    }

    def beforeValidate() {
        if(!DateRangeEnum.relativeDateOperatorsWithX.contains(dateRangeEnum) ) {
            relativeDateRangeValue = 1
        }
    }

    @Override
    Date getNextRunDate() {
        return queryRCA.usedConfiguration?.nextRunDate
    }

    @Override
    Date getAsOfVersionDate() {
        return new Date()
    }

    @Override
    String getConfigSelectedTimeZone() {
        return queryRCA.usedConfiguration?.configSelectedTimeZone
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        queryRCA?.usedConfiguration?.globalDateRangeInformationAutoROD
    }
/**
 * Returns the date object of January 1, 0000 AD to capture any case from said date.
 * @return
 */
    def getMinDate() {
        def calendar = Calendar.instance
        calendar.set(year:0000, month:Calendar.JANUARY, date:1)
        calendar.getTime()
    }

    @Override
    public String toString() {
        super.toString()
    }
}