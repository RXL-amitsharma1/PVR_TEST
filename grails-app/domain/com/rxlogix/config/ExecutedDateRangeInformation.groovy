package com.rxlogix.config


class ExecutedDateRangeInformation extends BaseDateRangeInformation {
    Date executedAsOfVersionDate

    static belongsTo = [executedTemplateQuery:ExecutedTemplateQuery]

    static mapping = {
        table name: "EX_DATE_RANGE"

        executedTemplateQuery column: "EX_TEMPLATE_QUERY_ID"
        executedAsOfVersionDate column: "EXECUTED_AS_OF"
    }

    static constraints = {
        executedAsOfVersionDate (nullable: false)
    }

    @Override
    Date getNextRunDate() {
        return executedTemplateQuery.usedConfiguration?.nextRunDate
    }

    @Override
    Date getAsOfVersionDate() {
        return executedTemplateQuery.usedConfiguration?.asOfVersionDate
    }

    @Override
    String getConfigSelectedTimeZone() {
        return executedTemplateQuery.usedConfiguration?.configSelectedTimeZone
    }

    @Override
    BaseDateRangeInformation getGlobalDateRangeInformation() {
         (executedTemplateQuery.usedConfiguration instanceof  ExecutedPeriodicReportConfiguration) ?executedTemplateQuery?.usedConfiguration?.executedGlobalDateRangeInformation:null
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
