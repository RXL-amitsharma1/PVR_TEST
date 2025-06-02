import com.rxlogix.config.WorkflowState
import com.rxlogix.config.WorkflowStateReportAction
import com.rxlogix.enums.ReportActionEnum

databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1536672817055-1") {
        createTable(tableName: "WORKFlOW_EXECUTORS") {
            column(name: "WORKFLOW_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTOR_ID", type: "number(19,0)")

            column(name: "EXECUTORS_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1536672817055-2") {
        createTable(tableName: "WORKFlOW_EXECUTORS_GROUP") {
            column(name: "WORKFLOW_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTOR_GROUP_ID", type: "number(19,0)")

            column(name: "EXECUTORS_GROUP_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1536672817055-147") {
        addForeignKeyConstraint(baseColumnNames: "EXECUTOR_ID", baseTableName: "WORKFlOW_EXECUTORS", constraintName: "FK_lfidoka6mqlikyavqgsqgvk3q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1536672817055-148") {
        addForeignKeyConstraint(baseColumnNames: "EXECUTOR_GROUP_ID", baseTableName: "WORKFlOW_EXECUTORS_GROUP", constraintName: "FK_4ovqiwmqkflm34m62iqcq87h4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1537366464704-1") {
        createTable(tableName: "WORKFLOW_STATE_ACTION") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORKFLOW_STACTIONPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_ACTION", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFlOW_STATE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1537366464704-2") {
        createTable(tableName: "WORKFlOWSTATE_ACT_EXTRS") {
            column(name: "WORKFLOW_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTOR_ID", type: "number(19,0)")

            column(name: "EXECUTORS_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "1537366464704-3") {
        createTable(tableName: "WORKFlOWSTATE_ACT_EXTRS_GR") {
            column(name: "WORKFLOW_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTOR_GROUP_ID", type: "number(19,0)")

            column(name: "EXECUTORS_GROUP_IDX", type: "number(10,0)")
        }
    }
    changeSet(author: "forxsv (generated)", id: "1537366464704-154") {
        addForeignKeyConstraint(baseColumnNames: "WORKFlOW_STATE_ID", baseTableName: "WORKFLOW_STATE_ACTION", constraintName: "FK_kqnbmtvm8rw7b84p4vt4wisdv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1537366464704-155") {
        addForeignKeyConstraint(baseColumnNames: "EXECUTOR_ID", baseTableName: "WORKFlOWSTATE_ACT_EXTRS", constraintName: "FK_5l15ph9mfxuxev2wiyqmbprqp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1537366464704-156") {
        addForeignKeyConstraint(baseColumnNames: "EXECUTOR_GROUP_ID", baseTableName: "WORKFlOWSTATE_ACT_EXTRS_GR", constraintName: "FK_h11g4j1oylrfb1bvyh9xq6h8f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }
    changeSet(author: "forxsv (generated)", id: "1537366464704-158") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty or already executed via 1537366464704-157', onErrorMessage: 'table is empty or already executed via 1537366464704-157') {
            not {
                changeSetExecuted(author: "forxsv (generated)", id: "1537366464704-157", changeLogFile: "2018-09-11-add-executors-to-workflow.groovy")
            }
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM WORKFLOW_STATE_REPORT_ACTIONS where rownum <2;')
        }
        grailsChange {
            change {
                try {
                    WorkflowState.withNewSession { session ->
                        def resultList = session.createSQLQuery("select * from WORKFLOW_STATE_REPORT_ACTIONS").list();
                        resultList.each{

                            WorkflowState workflowState = WorkflowState.get(it[0])
                            WorkflowStateReportAction action = new WorkflowStateReportAction(reportAction:it[1] as ReportActionEnum)
                            workflowState.addToReportActions(action)
                            workflowState.save(flush: true, failOnError: true)
                        }
                        session.flush()
                        session.clear()
                        session.close()
                    }

                } catch (Exception ex) {
                    println "##### Error Occurred while updating WorkflowState liquibase change set 1537366464704-157 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1537366464706-139") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyTableName: 'WORKFLOW_STATE_REPORT_ACTIONS', foreignKeyName: 'FKBF8786E04B2AF5FD')
        }
        dropForeignKeyConstraint(baseTableName: "WORKFLOW_STATE_REPORT_ACTIONS", constraintName: "FKBF8786E04B2AF5FD")
    }
    changeSet(author: "forxsv (generated)", id: "1537366464705-140") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            tableExists(tableName: "WORKFLOW_STATE_REPORT_ACTIONS")
        }
        dropTable(tableName: "WORKFLOW_STATE_REPORT_ACTIONS")
    }
}
