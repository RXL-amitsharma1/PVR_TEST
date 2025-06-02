package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.gorm.dirty.checking.DirtyCheck
import grails.util.Holders


import java.text.SimpleDateFormat

@DirtyCheck
abstract class BaseDateRangeInformation {
    Integer relativeDateRangeValue = 1
    Date dateRangeStartAbsolute
    Date dateRangeEndAbsolute
    DateRangeEnum dateRangeEnum = DateRangeEnum.CUMULATIVE

    static final Date MIN_DATE = Holders.config.pvreports.cumulative.startDate ?
            new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT).parse(Holders.config.pvreports.cumulative.startDate) :
            new SimpleDateFormat(DateUtil.DATETIME_FMT).parse("01-01-0001 00:00:01");

    static mapWith = "none"

    static mapping = {
        dateRangeStartAbsolute column: "DATE_RNG_START_ABSOLUTE"
        dateRangeEndAbsolute column: "DATE_RNG_END_ABSOLUTE"
        dateRangeEnum column: "DATE_RNG_ENUM"
        relativeDateRangeValue column: "RELATIVE_DATE_RNG_VALUE"
    }

    static constraints = {

        dateRangeEnum(nullable:false, validator: { val, obj ->
            if (obj.dateRangeEnum == DateRangeEnum.CUSTOM) {

                if (obj.dateRangeStartAbsolute == null && obj.dateRangeEndAbsolute == null) {
                    return "com.rxlogix.config.startdate.and.enddate.required"
                }

                if (obj.dateRangeStartAbsolute == null) {
                    return "com.rxlogix.config.startdate.required"
                }
                if (obj.dateRangeEndAbsolute == null) {
                    return "com.rxlogix.config.enddate.required"
                }

                if (obj.dateRangeStartAbsolute.after(obj.dateRangeEndAbsolute)) {
                    return "com.rxlogix.config.startdate.after.enddate"
                }

            }
        })

        dateRangeStartAbsolute(nullable:true,blank:true)
        dateRangeEndAbsolute(nullable:true,blank:true)
        relativeDateRangeValue(min:1,max:999999999)
    }

    abstract Date getNextRunDate();
    abstract Date getAsOfVersionDate();
    abstract String getConfigSelectedTimeZone();
    abstract BaseDateRangeInformation getGlobalDateRangeInformation();

    List getReportStartAndEndDate() {
        return getReportStartAndEndDateForDate(getNextRunDate() ?: new Date())
    }

    List getReportStartAndEndDateForDate(Date nextRunDate) {
        if (this.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE) {
            return globalDateRangeInformation.reportStartAndEndDate
        }
        if (this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {

            DateRangeEnum relativeDateRange = this.dateRangeEnum
            return RelativeDateConverter.(relativeDateRange.value())(new java.util.Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, 'UTC')
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            return [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            Date endDate = asOfVersionDate ?: new Date()
            if(Holders.config.getProperty('pvreports.cumulative.startDate')){
                Date startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT,Holders.config.getProperty('pvreports.cumulative.startDate'))
                return [startDate, endDate]
            }
            else {
                return [MIN_DATE, endDate]
            }
        }
    }

    public String toString() {
        String dateRange = ViewHelper.getMessage(dateRangeEnum.getI18nKey())
        if (dateRangeEnum == DateRangeEnum.CUSTOM) {
            String startDateRange = DateUtil.formatDateToString(dateRangeStartAbsolute)
            String endDateRange = DateUtil.formatDateToString(dateRangeEndAbsolute)
            return "$dateRange : [$startDateRange - $endDateRange]"
        } else if (dateRangeEnum in DateRangeEnum.getRelativeDateOperatorsWithX()) {
            return "$dateRange - $relativeDateRangeValue"
        } else {
            return dateRange
        }
    }

    boolean hasSameRange(BaseDateRangeInformation comp) {
        if (comp.dateRangeEnum != this.dateRangeEnum) return false
        if (comp.dateRangeEnum in [DateRangeEnum.CUMULATIVE,
                                   DateRangeEnum.YESTERDAY,
                                   DateRangeEnum.LAST_WEEK,
                                   DateRangeEnum.LAST_MONTH,
                                   DateRangeEnum.LAST_YEAR,
                                   DateRangeEnum.PR_DATE_RANGE,
                                   DateRangeEnum.TOMORROW,
                                   DateRangeEnum.NEXT_WEEK,
                                   DateRangeEnum.NEXT_MONTH,
                                   DateRangeEnum.NEXT_YEAR,
                                   DateRangeEnum.YESTERDAY,
                                   DateRangeEnum.YESTERDAY,
                                   DateRangeEnum.YESTERDAY,
                                   DateRangeEnum.YESTERDAY,
                                   DateRangeEnum.YESTERDAY,
        ]) return true
        if ((comp.dateRangeEnum in [DateRangeEnum.LAST_X_DAYS,
                                    DateRangeEnum.LAST_X_WEEKS,
                                    DateRangeEnum.LAST_X_MONTHS,
                                    DateRangeEnum.LAST_X_YEARS,
                                    DateRangeEnum.NEXT_X_DAYS,
                                    DateRangeEnum.NEXT_X_WEEKS,
                                    DateRangeEnum.NEXT_X_MONTHS,
                                    DateRangeEnum.NEXT_X_YEARS
        ]) && (comp.relativeDateRangeValue == this.relativeDateRangeValue)) return true

        if ((comp.dateRangeEnum == DateRangeEnum.CUSTOM)
                && (comp.dateRangeStartAbsolute == this.dateRangeStartAbsolute)
                && (comp.dateRangeEndAbsolute == this.dateRangeEndAbsolute)) return true

        return false
    }

}
