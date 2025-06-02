package com.rxlogix.jobs

class RefreshOneDriveSessionJob {

    def oneDriveRestService
    def concurrent = false
    def group = "RxLogixPVR"

    static triggers = {
        cron name: 'refreshUserTrigger', startDelay: 10000, cronExpression: '0 30 2 1/1 * ? *'    //Daily at 2:30am
    }

    def execute() {
        oneDriveRestService.refreshOneDriveSession()
    }
}
