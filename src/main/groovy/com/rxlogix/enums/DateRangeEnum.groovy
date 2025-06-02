package com.rxlogix.enums

import com.rxlogix.config.BaseDateRangeInformation

public enum DateRangeEnum {

    YESTERDAY('lastXDays', DateRangeValueEnum.RELATIVE),
    LAST_WEEK('lastXWeeks', DateRangeValueEnum.RELATIVE),
    LAST_MONTH('lastXMonths',DateRangeValueEnum.RELATIVE),
    LAST_YEAR('lastXYears',DateRangeValueEnum.RELATIVE),
    LAST_X_DAYS('lastXDays', DateRangeValueEnum.RELATIVE),
    LAST_X_WEEKS('lastXWeeks' ,DateRangeValueEnum.RELATIVE),
    LAST_X_MONTHS('lastXMonths',DateRangeValueEnum.RELATIVE),
    LAST_X_YEARS('lastXYears',DateRangeValueEnum.RELATIVE),
    CUMULATIVE('Cumulative',DateRangeValueEnum.CUMULATIVE),
    CUSTOM('Custom',DateRangeValueEnum.CUSTOM),
    PR_DATE_RANGE('PRDateRange',null),
    TOMORROW('nextXDays', DateRangeValueEnum.RELATIVE),
    NEXT_WEEK('nextXWeeks', DateRangeValueEnum.RELATIVE),
    NEXT_MONTH('nextXMonths',DateRangeValueEnum.RELATIVE),
    NEXT_YEAR('nextXYears',DateRangeValueEnum.RELATIVE),
    NEXT_X_DAYS('nextXDays', DateRangeValueEnum.RELATIVE),
    NEXT_X_WEEKS('nextXWeeks' ,DateRangeValueEnum.RELATIVE),
    NEXT_X_MONTHS('nextXMonths',DateRangeValueEnum.RELATIVE),
    NEXT_X_YEARS('nextXYears',DateRangeValueEnum.RELATIVE)

    final String val
    final DateRangeValueEnum dateRangeType

    DateRangeEnum(String val,DateRangeValueEnum dateRangeType) {
        this.val = val
        this.dateRangeType = dateRangeType
    }

    String getKey() {
        name()
    }

    String value() { return val }


    public static DateRangeEnum[] getDateOperators() {
        return [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS,CUMULATIVE,CUSTOM,TOMORROW, NEXT_WEEK, NEXT_MONTH, NEXT_YEAR, NEXT_X_DAYS, NEXT_X_WEEKS, NEXT_X_MONTHS, NEXT_X_YEARS]
    }

    public static DateRangeEnum[] getRelativeDateOperatorsWithX() {
        return [LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS,NEXT_X_DAYS, NEXT_X_WEEKS, NEXT_X_MONTHS, NEXT_X_YEARS]
    }

    public static List<DateRangeEnum> getReportTemplateDateRangeOptions(isFull = false) {
        return isFull ? values() : values().findAll { it != PR_DATE_RANGE }
    }

    public static List<DateRangeEnum> getPeriodicReportTemplateDateRangeOptions() {
        return [PR_DATE_RANGE, CUMULATIVE]
    }




    public getI18nKey() {
        return "app.queryOperator.${this.name()}"
    }

    static List<Date> getCumulativeDateRange(){
        return [BaseDateRangeInformation.MIN_DATE, new Date()]
    }

}