package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum

class ExecutedCaseSeriesDateRangeInformation extends BaseDateRangeInformation {

    static propertiesToUseForCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']

    static belongsTo = [executedCaseSeries: ExecutedCaseSeries]

    static mapping = {
        table name: "EX_CS_DATE_RANGE_INFO"
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
        return null
    }

    @Override
    Date getAsOfVersionDate() {
        return executedCaseSeries.asOfVersionDate
    }

    @Override
    String getConfigSelectedTimeZone() {
        return executedCaseSeries.owner.preference?.timeZone ?: "UTC"
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        return null
    }

    @Override
    List getReportStartAndEndDate (){
        return [dateRangeStartAbsolute, dateRangeEndAbsolute]
    }

    @Override
    public String toString(){
        super.toString()
    }

}
