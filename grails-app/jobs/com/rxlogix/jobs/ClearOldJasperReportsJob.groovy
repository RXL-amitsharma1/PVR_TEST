package com.rxlogix.jobs

class ClearOldJasperReportsJob {
    def fileService
    static concurrent = false
    static group = "RxLogixPVR"

    static triggers = {
        cron name: 'clearOldReportsTrigger', startDelay: 10000, cronExpression: '0 0 3 1/1 * ? *'    // Daily at 3:00am
    }

    def execute() {
        try {
            log.info("Deleting old reports...")
            fileService.deleteOldTempFiles()
            log.info("Finished deleted old reports!")
        } catch (Exception ex) {
            log.error("Unexpected error in ClearOldJasperReportsJob", ex)
        }
    }
}
