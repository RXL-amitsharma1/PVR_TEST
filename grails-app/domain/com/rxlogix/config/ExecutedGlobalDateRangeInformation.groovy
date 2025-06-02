package com.rxlogix.config

import com.rxlogix.util.DateUtil

class ExecutedGlobalDateRangeInformation extends BaseDateRangeInformation {
    Date executedAsOfVersionDate
    static belongsTo = [executedReportConfiguration: ExecutedReportConfiguration]

    static mapping = {
        table name: "EX_GLOBAL_DATE_RANGE_INFO"
        executedAsOfVersionDate column: "EXECUTED_AS_OF"
    }

    static constraints = {
        executedAsOfVersionDate(nullable: false)
    }

    String getDateRangeString(Locale locale){
        String dateFormat = DateUtil.getShortDateFormatForLocale(locale)
        return "${dateRangeStartAbsolute.format(dateFormat)} to ${dateRangeEndAbsolute.format(dateFormat)}"
    }

    @Override
    Date getNextRunDate() {
        return executedReportConfiguration?.nextRunDate
    }

    @Override
    Date getAsOfVersionDate() {
        return executedReportConfiguration?.asOfVersionDate
    }

    @Override
    String getConfigSelectedTimeZone() {
        return executedReportConfiguration?.configSelectedTimeZone
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
        return executedReportConfiguration?.executedGlobalDateRangeInformation
    }

    @Override
    List getReportStartAndEndDate (){
        return [dateRangeStartAbsolute, dateRangeEndAbsolute]
    }

    @Override
    public String toString() {
        super.toString()
    }
}
