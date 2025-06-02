package com.rxlogix.api


import com.rxlogix.config.JobExecutionHistory
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([JobExecutionHistory])
class JobExecutionHistoryRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<JobExecutionHistoryRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomain JobExecutionHistory
    }

    void "test list with search string"(){
        JobExecutionHistory jobExecutionHistory = new JobExecutionHistory(jobTitle: "Auto Reason Of Delay", jobStartRunDate: new Date(), jobEndRunDate: new Date(), jobRunStatus: "SUCCESS", jobRunRemarks: "",createdBy: "user")
        jobExecutionHistory.save(failOnError:true,validate:false,flush:true)
        JobExecutionHistory.metaClass.static.getAllJobExecutionHistoryBySearchString = { String jobTitle, String search -> return jobExecutionHistory}
        when:
        params.searchString = "Auto Reason Of Delay"
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.list()
        then:
        response.json.aaData[0].size() == 7
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test list with no search string"(){
        JobExecutionHistory jobExecutionHistory = new JobExecutionHistory(jobTitle: "Auto Reason Of Delay", jobStartRunDate: new Date(), jobEndRunDate: new Date(), jobRunStatus: "SUCCESS", jobRunRemarks: "",createdBy: "user")
        jobExecutionHistory.save(failOnError:true,validate:false,flush:true)
        JobExecutionHistory.metaClass.static.getAllJobExecutionHistoryBySearchString = { String jobTitle, String search -> new Object(){
            List list(Object o){
                return []
            }
            int count(){
                return 0
            }
        }
        }
        when:
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.list()
        then:
        response.json.aaData == []
        response.json.recordsTotal == 0
        response.json.recordsFiltered == 0
    }

}