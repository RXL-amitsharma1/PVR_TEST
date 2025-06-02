package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.user.User
import groovy.time.BaseDuration
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.TimeZoneRegistry
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import net.fortuna.ical4j.util.Dates
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class DateUtil {

    private static Logger logger = LoggerFactory.getLogger(getClass())
    public static final DATEPICKER_UTC_FORMAT = "MM/dd/yyyy HH:mm:ss"
    public static final DATEPICKER_FORMAT_AM_PM = "dd-MMM-yyyy hh:mm:ss a"
    public static final DATEPICKER_FORMAT_AM_PM_HOURS = "dd-MMM-yyyy hh:mm a"
    public static final DATEPICKER_DATE_TIME_FORMAT = "dd-MMM-yyyy hh:mm:ss"
    static final String DATETIME_FMT = "dd-MM-yyyy HH:mm:ss"
//    Need to be in sync with Application.js DEFAULT_DATE_DISPLAY_FORMAT
    public static final DATEPICKER_FORMAT = "dd-MMM-yyyy"
    public static final PR_CALENDAR_DATE_FORMAT = "yyyy-MM-dd"

    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    public static final String ISO_TIME_FORMAT = "HH:mm:ss"
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd"
    public static final String DATEPICKER_JFORMAT = "yyyy/MM/dd"
    public static final String SCHEDULE_DATE_JSON_FORMAT = "yyyy-MM-dd'T'HH:mmXXX"

    public static final String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"
    public static final String JSON_DATE_WITHOUT_OFFSET = "yyyy-MM-dd'T'HH:mm"

    public static final String DATE_FORMAT_AM_PM = "dd-MMM-yyyy hh:mm a"
    public static final String DATE_FORMAT_AM_PM_WITH_OFFSET = "dd-MMM-yyyy hh:mm a XXX"

    public static final Long MILI_SECOND_IN_DAY = 1000 * 24 * 60 * 60

    public static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    def static dtf = DateTimeFormat.forPattern('ZZ')

    public static final TimeZoneRegistry TZ_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry()
    public static final List<String> DATE_FORMATS = [
            DATEPICKER_UTC_FORMAT,
            DATEPICKER_FORMAT_AM_PM,
            DATEPICKER_FORMAT_AM_PM_HOURS,
            DATEPICKER_DATE_TIME_FORMAT,
            DATETIME_FMT,
            DATEPICKER_FORMAT,
            PR_CALENDAR_DATE_FORMAT,
            ISO_DATE_TIME_FORMAT,
            ISO_TIME_FORMAT,
            ISO_DATE_FORMAT,
            DATEPICKER_JFORMAT,
            SCHEDULE_DATE_JSON_FORMAT,
            JSON_DATE,
            JSON_DATE_WITHOUT_OFFSET,
            DATE_FORMAT_AM_PM,
            DATE_FORMAT_AM_PM_WITH_OFFSET,
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy"
    ]
    static String getTimezone(User currentUser) {

        String tz = currentUser?.preference?.timeZone
        return "name : ${tz},offset : ${getOffsetString(tz)} "
    }

    static String getTimezone(TimeZoneEnum timeZoneEnum) {
        return "name : ${timeZoneEnum.getTimezoneId()},offset : ${timeZoneEnum.getGmtOffset()} "
    }

    static String getOffsetString(String timeZoneId) {
        return getOffsetString(timeZoneId, new Date())
    }

    static String getOffsetString(String timeZoneId, Date date) {
        TimeZone tz = TZ_REGISTRY.getTimeZone(timeZoneId)
        int offsetForToday = tz.getOffset(date.time)
        long hours = TimeUnit.MILLISECONDS.toHours(offsetForToday)
        long minutes = TimeUnit.MILLISECONDS.toMinutes(offsetForToday) - TimeUnit.HOURS.toMinutes(hours)
        // avoid -4:-30 issue
        minutes = Math.abs(minutes)

        String result = ""
        if (hours >= 0) {
            result = String.format("+%02d:%02d", hours, minutes)
        } else {
            result = String.format("-%02d:%02d", Math.abs(hours), minutes)
        }
        return result;
    }

    def static getTimezoneForRunOnce(User currentUser) {
        String tz = currentUser?.preference?.timeZone
        return """name" :"${tz}","offset" : "${getOffsetString(tz)}"""
    }

    /*
    Sets a Start Date type of field populated via a DatePicker widget, but take into account a time zone passed in
    (often the User Preferences Timezone).
    The final date/time set is determined by the timezone passed in, but will be set in UTC.

    Ex.  10-Mar-2016 chosen in DatePicker with User Preference Timezone set to EST will result in Mar 10, 05:00:00 UTC
*/

    static Date getStartDate(String dateFromDatePicker, def timezone, Locale locale) {
        Date start = null
        SimpleDateFormat sdf = new SimpleDateFormat(getShortDateFormatForLocale(locale))
        sdf.setLenient(false)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        try {
            start = sdf.parse(dateFromDatePicker)
        } catch (ParseException pe) {
            start = null
        }
        return start
    }

    /*
    Sets an End Date type of field populated via a DatePicker widget, but do NOT take into User Preferences Timezone.
    This will be set as the date chosen, in UTC timezone.

    Ex.   10-Mar-2016 chosen in DatePicker will result in 3/10/2016 00:00:00 UTC
    */

    static Date getStartDate(String dateToDatePicker, Locale locale) {
        return getStartDate(dateToDatePicker, "UTC", locale)
    }

    /*
        Sets an End Date type of field populated via a DatePicker widget, but take into account a time zone passed in
        (often the User Preferences Timezone).
        The final date/time set is determined by the timezone passed in, but will be set in UTC.

        Ex.  20-Mar-2016 chosen in DatePicker with User Preference Timezone set to EST will result in Mar 21, 03:59:59 UTC
    */

    static Date getEndDate(String dateToDatePicker, def timezone, Locale locale) {
        Date end = null
        SimpleDateFormat sdf = new SimpleDateFormat(getShortDateFormatForLocale(locale))
        sdf.setLenient(false)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        try {
            use(TimeCategory) {
                end = sdf.parse(dateToDatePicker) + 1.day - 1.second
            }
        } catch (ParseException pe) {
            end = null
        }
        return end
    }

    /*
        Sets an End Date type of field populated via a DatePicker widget, but do NOT take into User Preferences Timezone.
        This will be set as the date chosen, in UTC timezone.

        Ex.   20-Mar-/2016 chosen in DatePicker will result in 3/20/2016 11:59:59 UTC
     */

    static Date getEndDate(String dateToDatePicker, Locale locale) {
        return getEndDate(dateToDatePicker, "UTC", locale)
    }

    static String dateRangeString(dateGiven,String timezone) {
        if(dateGiven) {
            if (dateGiven instanceof java.sql.Timestamp) {
              return  getDateRangeStringForGivenTimezone(new Date(dateGiven.getTime()), timezone)
            } else {
                return getDateRangeStringForGivenTimezone(dateGiven, timezone)
            }

        } else {
            return null
        }
    }

    private static String getDateRangeStringForGivenTimezone(Date date, String tz) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATEPICKER_FORMAT)
        sdf.setTimeZone(TimeZone.getTimeZone(tz))
        String dateValue = sdf.format(date) // gives string date original value
        def val = dateValue.tokenize(" ")
        return val[0]
    }

    static Date parseDateWithTimeZone(String dateStr, String format, String timeZone) {
        Date date = null
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        if (dateStr) {
            date = sdf.parse(dateStr)
        }
        return date
    }

    static Date StringToDate(String strDate, String format) {
        if (!strDate) {
            return null
        }
        org.joda.time.format.DateTimeFormatter formatter =
                DateTimeFormat.forPattern(format).withOffsetParsed();

        DateTime dateTime = formatter.parseDateTime(strDate);
        GregorianCalendar cal = dateTime.toGregorianCalendar();

        return cal.getTime()
    }

    static String StringFromDate(Date date, String format, String timeZone){
        String out;
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        if(timeZone)
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        try{
            out = sdf.format(date)
        }catch (ParseException e){
            return date?.toString();
        }
        return out;

    }

    def static SimpleDateReformat(String strDate, String fromFormat, String toFormat){
        SimpleDateFormat from = new SimpleDateFormat(fromFormat);
        SimpleDateFormat to = new SimpleDateFormat(toFormat);
        String reformattedDate
        try {

             reformattedDate= to.format(from.parse(strDate));
        } catch (ParseException e) {
            return strDate
        }

        return reformattedDate;

    }


     static Date parseDate(String date, String dateFormat){

        Date dateAsString = null
        try {
            dateAsString = Date.parse(dateFormat, date)
        } catch (ParseException) {

        }

        return dateAsString

    }

    def static toDateString(Date date, String format) {
        if (date) {
            def formatter = DateTimeFormat.forPattern(format)
            new DateTime(date).toString(formatter)
        } else {
            ""
        }
    }

    static Date getDateWithDayEndTime(Date date) {
        if (!date) {
            return date
        }
        use(TimeCategory) {
            return date.clearTime() + 1.day - 1.second
        }
    }

    static Date getDateWithDayStartTime(Date date) {
        if (!date) {
            return date
        }
        return date.clearTime()
    }

    static String getShortDateFormatForLocale(Locale locale) {
        String format
        try {
            def messageSource = MiscUtil.getBean("messageSource")
            format = messageSource.getMessage("default.date.format.short", null, locale)
        }
        catch (Exception e) {
            logger.error(e.message)
            format = DATEPICKER_FORMAT
        }
        return format
    }

    static String getLongDateFormatForLocale(Locale locale, boolean withTimeZone = false) {
        String format
        try {
            def messageSource = MiscUtil.getBean("messageSource")
            format = messageSource.getMessage(withTimeZone? "default.date.format.long.tz" : "default.date.format.long", null, locale)
        }
        catch (Exception e) {
            logger.error(e.message)
            format = DATEPICKER_FORMAT_AM_PM
        }
        return format
    }

    static String getLongDateStringForTimeZone(Date date, String timeZone, Boolean showTimeZone = false) {
        if (!date) return ""
        SimpleDateFormat sdf = new SimpleDateFormat(DATEPICKER_FORMAT_AM_PM)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        return sdf.format(date) + (showTimeZone ? " " + timeZone : "")
    }

    static String getLongDateStringForLocaleAndTimeZone(Date date, Locale locale, String timeZone, Boolean showTimeZone = false) {
        if (!date) return ""
        String format = getLongDateFormatForLocale(locale, showTimeZone)
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        return sdf.format(date)
    }

    static def getCurrentDateInUTC(Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(getShortDateFormatForLocale(locale))
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
    }

    static Date parseDateWithLocaleAndTimeZone(String date, String format, Locale locale, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format,locale)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        return sdf.parse(date)
    }

    static String formatDateToString(Date date) {
        String format
        try {
            def customMessageService = MiscUtil.getBean("customMessageService")
            format = customMessageService.getMessage("default.date.format.long")
        }
        catch (Exception e) {
            logger.error(e.message)
            format = DATEPICKER_FORMAT_AM_PM
        }
        return date.format(format)
    }

    static String calculateDueDays(Date dueDate) {
        String dueInDays = null
        Date currentDate = new Date()
        Long interval = (dueDate.getTime() - currentDate.getTime())
        int days = (interval) / MILI_SECOND_IN_DAY
        if (Math.abs(days) < 1) {
            dueInDays = (interval / (1000*60*60)) + " hours"
        } else {
            dueInDays = days + " days"
        }
        return dueInDays
    }

    static String getShortDateStringForLocaleAndTimeZone(Date date, Locale locale, String timeZone, Boolean showTimeZone = false) {
        if (!date) return ""
        String format = getShortDateFormatForLocale(locale)
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        return sdf.format(date)
    }

    static Date getDateByLocaleAndTimezone(String dateString, String timezone, Locale locale) {
        Date date = null
        SimpleDateFormat sdf = new SimpleDateFormat(getShortDateFormatForLocale(locale))
        sdf.setLenient(false)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        try {
            use(TimeCategory) {
                date = sdf.parse(dateString)
            }
        } catch (ParseException pe) {
            date = null
        }
        return date
    }

    static Date addDaysSkippingWeekends(Date date, int numDays) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        for (int i = 0; i < numDays; i++) {
            dateCal.add(dateCal.DATE, 1);
            if(dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY  ){
                dateCal.add(dateCal.DATE, 1);
                i--;
            }
        }
        return dateCal.getTime();
    }

    static Date minusDaysSkippingWeekends(Date date, int numDays) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        for (int i = 0; i < numDays; i++) {
            dateCal.add(dateCal.DATE, -1);
            if (dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                dateCal.add(dateCal.DATE, -1);
                i--;
            }
        }
        return dateCal.getTime();
    }

    static String getFormattedDateForLastSuccessfulEtlRun(String timeZone, def executedETLDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(ViewHelper.getMessage("default.date.format.long.tz"))
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        String dateValue = Constants.BLANK_STRING
        if (executedETLDate instanceof Date)
            dateValue = sdf.format(executedETLDate)
        return dateValue
    }

    public static net.fortuna.ical4j.model.DateTime plusDuration(final net.fortuna.ical4j.model.DateTime date, final BaseDuration duration) {
        final Calendar cal = Dates.getCalendarInstance(date)
        cal.setTime(date)
        cal.add(Calendar.YEAR, duration.getYears())
        cal.add(Calendar.MONTH, duration.getMonths())
        cal.add(Calendar.DAY_OF_YEAR, duration.getDays())
        cal.add(Calendar.HOUR_OF_DAY, duration.getHours())
        cal.add(Calendar.MINUTE, duration.getMinutes())
        cal.add(Calendar.SECOND, duration.getSeconds())
        cal.add(Calendar.MILLISECOND, duration.getMillis())
        return new net.fortuna.ical4j.model.DateTime(cal.getTime())
    }

    public static net.fortuna.ical4j.model.DateTime minusDuration(final net.fortuna.ical4j.model.DateTime date, final BaseDuration duration) {
        final Calendar cal = Dates.getCalendarInstance(date)
        cal.setTime(date);
        cal.add(Calendar.YEAR, -duration.getYears())
        cal.add(Calendar.MONTH, -duration.getMonths())
        cal.add(Calendar.DAY_OF_YEAR, -duration.getDays())
        cal.add(Calendar.HOUR_OF_DAY, -duration.getHours())
        cal.add(Calendar.MINUTE, -duration.getMinutes())
        cal.add(Calendar.SECOND, -duration.getSeconds())
        cal.add(Calendar.MILLISECOND, -duration.getMillis())
        return new net.fortuna.ical4j.model.DateTime(cal.getTime())
    }

    public static Date parseDate(String dateString, String format, String timezoneId) {
        DateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TZ_REGISTRY.getTimeZone(timezoneId))
        return sdf.parse(dateString)
    }

    public static String toDateStringWithTimeInAmPmFormat(User currentUser) {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM).withZone(DateTimeZone.forID(currentUser?.preference?.timeZone))
        new DateTime(new Date()).toString(formatter)
    }

    public static String toDateStringWithTimeInAmPmFormatWithoutSec(User currentUser) {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM_HOURS).withZone(DateTimeZone.forID(currentUser?.preference?.timeZone))
        new DateTime(new Date()).toString(formatter)
    }

    public static List toDateAndTimeInAmPmFormat(String timeZone, Date submissionDate = null) {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM_HOURS).withZone(DateTimeZone.forID(timeZone))
        Date dateToFormat = submissionDate != null ? submissionDate : new Date();
        String datetime = new DateTime(dateToFormat).toString(formatter)

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DATEPICKER_FORMAT_AM_PM_HOURS)
        LocalDateTime localDateTime = LocalDateTime.parse(datetime, inputFormatter)

        // Format the date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        String formattedDate = localDateTime.format(dateFormatter)

        // Format the time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        String formattedTime = localDateTime.format(timeFormatter)

        return [formattedDate, formattedTime]
    }

    public static List genenrateTimeDropDownList(String currentTimeString) {

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        // Parse the current time string to LocalTime
        LocalTime currentTime = LocalTime.parse(currentTimeString.toUpperCase(), inputFormatter)

        // Filter time slots to include only future times from current time
        List<String> filteredTimeSlots = generateTimeSlots().stream()
                .filter { LocalTime.parse(it, outputFormatter).isAfter(currentTime.minusMinutes(1)) }
                .collect(Collectors.toList())

        filteredTimeSlots.add(0, currentTimeString)

        return filteredTimeSlots.collect { [id: it, text: it]}
    }

    public static List<String> generateTimeSlots() {
        return ["12:00 AM", "12:30 AM", "01:00 AM", "01:30 AM", "02:00 AM", "02:30 AM", "03:00 AM", "03:30 AM", "04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM", "06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM", "09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"]
    }

    public static Date covertToDateWithTimeZone(Date date, String format, String timeZoneId) {
        logger.info("DateUtil: date = ${date}, format = ${format} and timeZoneId = ${timeZoneId}")
        DateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId))
        logger.info("DateUtil: Formatting date as per date format and timezone provided.")
        String dateString = sdf.format(date)
        logger.info("DateUtil: dateString = ${dateString} and outputDate = ${Date.parse(Constants.DateFormat.NO_TZ, dateString)}")
        return Date.parse(Constants.DateFormat.NO_TZ, dateString)
    }

    static boolean checkDate(String date, String dateFormat) {
        String dateString = date.trim()
        if (dateString.length() != dateFormat.length()) {
            return false
        }
        try {
            return Date.parse(dateFormat, dateString)
        } catch (ParseException e) {
            logger.error("Error date ${dateString} checking for ${dateFormat} format: ${e.message}")
            return false
        }
    }

    static String decodedViewDateFormat (String inputDate, String locale) {
        String gmtTimeZone = ''
        String outputDate = inputDate
        Integer inputDateLength
        SimpleDateFormat dateTimeFormatterWithSeconds
        SimpleDateFormat dateTimeFormatterWithoutSeconds
        SimpleDateFormat dateFormatterWithDate
        try {
            if (inputDate?.contains('+')) {
                String[] parts = inputDate.split("\\+")
                inputDate = parts[0]
                gmtTimeZone = " GMT+" + parts[1]
            }

            inputDateLength = inputDate?.length()
            if (locale?.equals("ja")) {
                dateTimeFormatterWithSeconds = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.JAPAN)
                dateTimeFormatterWithoutSeconds = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.JAPAN)
                dateFormatterWithDate = new SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN)
            } else {
                dateTimeFormatterWithSeconds = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH)
                dateTimeFormatterWithoutSeconds = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH)
                dateFormatterWithDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
            }

            switch (inputDateLength) {
                case 14:
                    Date dateWithSeconds = new SimpleDateFormat("yyyyMMddHHmmss").parse(inputDate)
                    return outputDate + " (" + dateTimeFormatterWithSeconds.format(dateWithSeconds) + "${gmtTimeZone})"
                case 12:
                    Date dateWithoutSeconds = new SimpleDateFormat("yyyyMMddHHmm").parse(inputDate)
                    return outputDate + " (" + dateTimeFormatterWithoutSeconds.format(dateWithoutSeconds) + "${gmtTimeZone})"
                case 8:
                    Date date = new SimpleDateFormat("yyyyMMdd").parse(inputDate)
                    return outputDate + " (" + dateFormatterWithDate.format(date) + "${gmtTimeZone})"
                case 6:
                    String year = inputDate.substring(0, 4)
                    String month = inputDate.substring(4)
                    if (locale.equals("ja")) {
                        return outputDate + " (" + year + "年" + month + "月)"
                    } else {
                        String monthName = ''
                        switch (month) {
                            case "01": monthName = "Jan"; break
                            case "02": monthName = "Feb"; break
                            case "03": monthName = "Mar"; break
                            case "04": monthName = "Apr"; break
                            case "05": monthName = "May"; break
                            case "06": monthName = "Jun"; break
                            case "07": monthName = "Jul"; break
                            case "08": monthName = "Aug"; break
                            case "09": monthName = "Sep"; break
                            case "10": monthName = "Oct"; break
                            case "11": monthName = "Nov"; break
                            case "12": monthName = "Dec"; break
                            default: monthName = "??"; break
                        }
                        return outputDate + " (??-" + monthName + "-" + year + ")"
                    }
                case 4:
                    if (locale.equals("ja")) {
                        return outputDate + " (" + inputDate + "年)"
                    } else {
                        return outputDate + " (" + "??-??-" + inputDate + ")"
                    }
                default:
                    return outputDate
            }
        } catch (ParseException e) {
            logger.error("Date parsing failed for input date: ${outputDate}. Error: ${e.getMessage()}")
        } catch (Exception e) {
            logger.error("An unexpected error occurred while formatting date ${outputDate}. Error:  ${e.getMessage()}")
        }
        return outputDate
    }

}
