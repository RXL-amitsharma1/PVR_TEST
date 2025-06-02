import com.rxlogix.config.WorkflowRule
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.util.DateUtil
import groovy.sql.GroovyResultSet
import grails.util.Holders

import java.sql.Timestamp

databaseChangeLog = {
    changeSet(author: "Rishabh", id: "202303281234-1") {           //update quality_case_data due_date
        grailsChange {
            change {
                try {
                    WorkflowRule workflowRule = WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA)
                    Integer dueInDays = workflowRule?.dueInDays ?: 0
                    Boolean excludeWeekends = workflowRule?.excludeWeekends

                    String queryString = "select q.id as id, CAST(q.workflow_state_updated_date as date) as workflow_state_updated_date, w.id as workflow_rule_id, w.due_in_days as due_in_days, w.exclude_weekends as exclude_weekends from " +
                            "quality_case_data q left join workflow_rule w on w.id=(select workflow_rule_id from workflow_justification j where quality_case_data=q.id order by " +
                            "j.date_created fetch first 1 row only) where q.due_date is null and q.workflow_state_updated_date is not null FETCH NEXT 5000 ROWS ONLY \n"

                    while (true) {
                        List list = sql.rows(queryString)
                        if (!list || (list.size() == 0)) break
                        list.each {
                            Date workflowStateUpdatedDate = it['workflow_state_updated_date'] ?: new Date()
                            if (it['workflow_rule_id'] != null) {
                                dueInDays = it['due_in_days']
                                excludeWeekends = it['exclude_weekends'] == 1 ? true : false
                            }
                            Date dueDate = excludeWeekends ? DateUtil.addDaysSkippingWeekends(workflowStateUpdatedDate, dueInDays ?: 0) : workflowStateUpdatedDate.plus(dueInDays ?: 0)
                            sql.execute("update quality_case_data set due_date = ? where id= ?", [new Timestamp(dueDate.getTime()), it['id']])
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while inserting quality_case_data due_date 202303281234-1 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "Rishabh", id: "202303281234-2") {           //update quality_submission due_date
        grailsChange {
            change {
                try {
                    WorkflowRule workflowRule = WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION)
                    Integer dueInDays = workflowRule?.dueInDays ?: 0
                    Boolean excludeWeekends = workflowRule?.excludeWeekends

                    String queryString = "select q.id as id, CAST(q.workflow_state_updated_date as date) as workflow_state_updated_date, w.id as workflow_rule_id, w.due_in_days as due_in_days, w.exclude_weekends as exclude_weekends from " +
                            "quality_submission q left join workflow_rule w on w.id=(select workflow_rule_id from workflow_justification j where quality_submission=q.id order by " +
                            "j.date_created fetch first 1 row only) where q.due_date is null and q.workflow_state_updated_date is not null FETCH NEXT 5000 ROWS ONLY \n"

                    while (true) {
                        List list = sql.rows(queryString)
                        if (!list || (list.size() == 0)) break
                        list.each {
                            Date workflowStateUpdatedDate = it['workflow_state_updated_date'] ?: new Date()
                            if (it['workflow_rule_id'] != null) {
                                dueInDays = it['due_in_days']
                                excludeWeekends = it['exclude_weekends'] == 1 ? true : false
                            }
                            Date dueDate = excludeWeekends ? DateUtil.addDaysSkippingWeekends(workflowStateUpdatedDate, dueInDays ?: 0) : workflowStateUpdatedDate.plus(dueInDays ?: 0)
                            sql.execute("update quality_submission set due_date = ? where id= ?", [new Timestamp(dueDate.getTime()), it['id']])
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while inserting quality_submission due_date 202303281234-2 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "Rishabh", id: "202303281234-3") {           //update quality_sampling due_date
        grailsChange {
            change {
                try {
                    Map nameWorkflowMap = [:]
                    Holders.config.qualityModule.additional.each { nameWorkflowMap.put(it.name, WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.getAdditional(it.workflow)))}

                    String queryString = "select q.id as id, CAST(q.workflow_state_updated_date as date) as workflow_state_updated_date, q.type as type, w.id as workflow_rule_id, w.due_in_days as due_in_days, w.exclude_weekends as exclude_weekends from " +
                            "quality_sampling q left join workflow_rule w on w.id=(select workflow_rule_id from workflow_justification j where quality_sampling=q.id order by " +
                            "j.date_created fetch first 1 row only) where q.due_date is null and q.workflow_state_updated_date is not null FETCH NEXT 5000 ROWS ONLY \n"

                    while (true) {
                        List list = sql.rows(queryString)
                        if (!list || (list.size() == 0)) break
                        list.each {
                            WorkflowRule workflowRule = nameWorkflowMap?.get(it['type']) ?: WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.QUALITY_SAMPLING)
                            Integer dueInDays = workflowRule?.dueInDays ?: 0
                            Boolean excludeWeekends = workflowRule?.excludeWeekends
                            Date workflowStateUpdatedDate = it['workflow_state_updated_date'] ?: new Date()
                            if (it['workflow_rule_id'] != null) {
                                dueInDays = it['due_in_days']
                                excludeWeekends = it['exclude_weekends'] == 1 ? true : false
                            }
                            Date dueDate = excludeWeekends ? DateUtil.addDaysSkippingWeekends(workflowStateUpdatedDate, dueInDays ?: 0) : workflowStateUpdatedDate.plus(dueInDays ?: 0)
                            sql.execute("update quality_sampling set due_date = ? where id= ?", [new Timestamp(dueDate.getTime()), it['id']])
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while inserting quality_sampling due_date 202303281234-3 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }
}
