package com.rxlogix.config

import com.rxlogix.util.DateUtil

class ExecutedGlobalDateRangeInbound extends BaseDateRangeInformation {

    static propertiesToUseForCopying = ['dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'relativeDateRangeValue', 'dateRangeEnum']

    static belongsTo = [executedInboundCompliance: ExecutedInboundCompliance]

    static mapping = {
        table name: "EX_GLOBAL_DATA_RANGE_INBOUND"
    }

    static constraints = {
    }

    String getDateRangeString(Locale locale){
        String dateFormat = DateUtil.getShortDateFormatForLocale(locale)
        return "${dateRangeStartAbsolute.format(dateFormat)} to ${dateRangeEndAbsolute.format(dateFormat)}"
    }

    @Override
    Date getNextRunDate() {
        return null
    }

    @Override
    Date getAsOfVersionDate() {
        return null
    }

    @Override
    String getConfigSelectedTimeZone() {
        return null
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
    public String toString() {
        super.toString()
    }
}
