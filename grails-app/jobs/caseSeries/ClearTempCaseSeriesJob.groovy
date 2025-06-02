package caseSeries

import grails.gorm.multitenancy.WithoutTenant
import grails.util.Holders

class ClearTempCaseSeriesJob {

    def caseSeriesService
    def concurrent = false
    def group = "RxLogixPVR"

    static triggers = {
        cron name: 'deleteTempCaseSeriesTrigger', cronExpression: Holders.config.getRequiredProperty('deletePreviewQueryJob.cronExpression')
    }

    @WithoutTenant
    def execute() {
        log.info("################Deleting Temp Case Series##############")
        caseSeriesService.deleteTemporaryCaseSeries()
        log.info("###########Finished deleting Temp Case Series!##########")
    }
}
