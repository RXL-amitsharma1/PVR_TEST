package com.rxlogix.api

import com.rxlogix.config.JobExecutionHistory
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class JobExecutionHistoryRestController extends RestfulController implements SanitizePaginationAttributes {

    JobExecutionHistoryRestController() {
        super(JobExecutionHistory)
    }

    def list(String jobTitle) {
        sanitize(params)
        def jobExecutionHistoryNameQuery = JobExecutionHistory.getAllJobExecutionHistoryBySearchString(jobTitle, params.searchString)
        List<JobExecutionHistory> jobExecutionHistoryList = jobExecutionHistoryNameQuery.list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        List<Map> jobExecutionHistories = jobExecutionHistoryList.collect {
            [id: it.id, jobTitle: it.jobTitle, jobStartRunDate: it.jobStartRunDate, jobEndRunDate: it.jobEndRunDate, jobRunStatus: it.jobRunStatus ? message(code: it.jobRunStatus?.i18nKey) : '', jobRunRemarks: it.jobRunRemarks, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT)]
        }
        render([aaData: jobExecutionHistories, recordsTotal: JobExecutionHistory.getAllJobExecutionHistoryBySearchString(jobTitle, null).count(), recordsFiltered: jobExecutionHistoryNameQuery.count()] as JSON)
    }
}