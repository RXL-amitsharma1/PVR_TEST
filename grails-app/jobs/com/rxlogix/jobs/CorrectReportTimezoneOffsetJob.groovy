package com.rxlogix.jobs

class CorrectReportTimezoneOffsetJob {
    static concurrent = false
    static group = "RxLogixPVR"

    def configurationService

    static triggers = {
        cron name: 'AdjustReportTimeZone', startDelay: 10000, cronExpression: '0 0 3 1/1 * ? *'    // Daily at 3:00am
    }

    def execute() {
        try{
            configurationService.adjustTimeZoneOffsetForReports()
        }
        catch(Exception e){
            log.error("Unexpected error in in ", e)
        }
    }
}
