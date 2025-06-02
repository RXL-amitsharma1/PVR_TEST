package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.time.LocalDateTime
import java.time.temporal.TemporalAmount

@ConfineMetaClassChanges([WorkflowRule, WorkflowJustification, QualitySubmission])
class WorkflowServiceSpec extends Specification implements DataTest, ServiceUnitTest<WorkflowService> {


    def setupSpec() {
        mockDomains User, Role, UserRole, WorkflowJustification, WorkflowRule, ExecutedReportConfiguration, WorkflowState, QualitySubmission
    }

    def "Test moveAutomationStatuses method"() {
        given:
        WorkflowState newState = new WorkflowState(name: WorkflowState.NEW_NAME).save(validate: false)
        WorkflowState progressState = new WorkflowState(name: WorkflowState.INPROGRESS_NAME).save(validate: false)
        WorkflowState closedState = new WorkflowState(name: WorkflowState.CLOSED_NAME).save(validate: false)

        WorkflowState.metaClass.static.getDefaultWorkState = { -> return newState }

        WorkflowRule workflowRule1 = new WorkflowRule(
                id: 1L,
                name: "1",
                configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION,
                initialState: newState,
                targetState: progressState,
                autoExecuteInDays: 1,
                assignedToUser: new User()
        )
        WorkflowRule workflowRule2 = new WorkflowRule(
                id: 2L,
                name: "2",
                configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION,
                initialState: progressState,
                targetState: closedState,
                autoExecuteInDays: 7,
                autoExecuteExcludeWeekends: true,
                assignedToUser: new User()
        )
        workflowRule1.save(flush: true, validate: false)
        workflowRule2.save(flush: true, validate: false)

        WorkflowRule.metaClass.static.findAllByIsDeletedAndAutoExecuteInDaysIsNotNull = { Boolean b ->
            return [workflowRule1, workflowRule2]
        }

        QualitySubmission quality = new QualitySubmission(id: 1).save(validate: false)

        WorkflowJustification.metaClass.static.getAllLatestJustifications = { List<WorkflowState> states ->
            def result = [
                    new WorkflowJustification(
                            fromState: progressState,
                            toState: progressState,
                            routedBy: new User(),
                            qualitySubmission: new QualitySubmission()
                    ),
                    new WorkflowJustification(
                            fromState: progressState,
                            toState: progressState,
                            routedBy: new User(),
                            qualityCaseData: new QualityCaseData()
                    ),
                    new WorkflowJustification(
                            fromState: progressState,
                            toState: progressState,
                            routedBy: new User(),
                            qualitySubmission: new QualitySubmission()
                    )
            ]
            result[1].dateCreated = LocalDateTime.now() - (15 as TemporalAmount) as Date
            result[2].dateCreated = LocalDateTime.now() - (15 as TemporalAmount) as Date
            return result
        }

        QualitySubmission.metaClass.static.findAllByIsDeletedAndWorkflowStateUpdatedDateLessThanAndWorkflowState = { Boolean b, Date d, WorkflowState wf ->
            return [quality]
        }

        def capturedSaves = []
        service.CRUDService = [
                save: { Object o ->
                    capturedSaves << o
                }
        ]

        service.metaClass.assignPvqWorkflow = { WorkflowJustification workflowJustificationInstance, Boolean withoutValidation ->
            return [:]
        }

        service.metaClass.movePvqPvcStatusTrn = { entity, rule ->
            return [:]
        }

        service.seedDataService = [getApplicationUserForSeeding: { return null }]
        service.qualityService = [getIdToUpdateWorkflow: { String type, List ids, Long id ->
            [goodIds: [quality.id]]
        }]

        when:
        service.moveAutomationStatuses()

        then:
        capturedSaves.size() == 2
    }


}
