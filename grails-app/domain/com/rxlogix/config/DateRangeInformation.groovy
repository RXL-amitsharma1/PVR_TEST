package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class DateRangeInformation extends BaseDateRangeInformation {

    DateRangeInformation(){
        this.dateRangeEnum = DateRangeEnum.PR_DATE_RANGE
    }

    static List propertiesToUseWhileCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']


    static belongsTo = [templateQuery:TemplateQuery]

    static mapping = {
        table name: "DATE_RANGE"
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
        return templateQuery.usedConfiguration?.nextRunDate
    }

    @Override
    Date getAsOfVersionDate() {
        return templateQuery.usedConfiguration?.asOfVersionDate
    }

    @Override
    String getConfigSelectedTimeZone() {
        return templateQuery.usedConfiguration?.configSelectedTimeZone
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        templateQuery?.usedConfiguration?.globalDateRangeInformation
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
