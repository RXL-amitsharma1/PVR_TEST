package com.rxlogix.jobs

//The Job picks bulk download ICSR report request data from data base and executes the download operation
class BulkDownloadIcsrReportsJob {

    def icsrCaseTrackingService

    static concurrent = false

    static triggers = {
        cron name: 'BulkDownloadIcsrReportsJobTrigger', startDelay: 5000, cronExpression: '0/30 * * * * ? *' // every 30 seconds
    }

    def execute(){
        try{
            icsrCaseTrackingService.prepareBulkDownload()
        } catch (Exception e) {
            log.error("Exception in BulkDownloadIcsrReportsJob: ${e.message}")
            e.printStackTrace()
        }
    }
}
