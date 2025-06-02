import com.rxlogix.config.RCAMandatory
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReasonOfDelayFieldEnum

databaseChangeLog = {

    changeSet(author: "rishabh", id: "202302122255-1") {
        createTable(tableName: "RCA_MANDATORY_FIELDS") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "OWNER_APP", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "FIELD", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rishabh", id: "202302122255-2") {
        createTable(tableName: "RCA_MANDATORY_WFS") {
            column(name: "RCA_MANDATORY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "WFS_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "RCA_WFS_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "rishabh", id: "202302122255-3") {
        createTable(tableName: "RCA_EDITABLE_WFS") {
            column(name: "RCA_MANDATORY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "WFS_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "RCA_WFS_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "rishabh", id: "202302122255-4") {
        createTable(tableName: "RCA_EDIT_USERS") {
            column(name: "RCA_MANDATORY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "USER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "RCA_USER_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "rishabh", id: "202302122255-5") {
        createTable(tableName: "RCA_EDIT_USRGRPS") {
            column(name: "RCA_MANDATORY_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "USER_GRP_ID", type: "number(19,0)")  {
                constraints(nullable: "false")
            }
            column(name: "RCA_GRP_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "rishabh", id: "202302122255-6") {
        addForeignKeyConstraint(baseColumnNames: "WFS_ID", baseTableName: "RCA_MANDATORY_WFS", constraintName: "FK_STATE_RCA_WFS_1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "WFS_ID", baseTableName: "RCA_EDITABLE_WFS", constraintName: "FK_STATE_RCA_WFS_2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "RCA_EDIT_USERS", constraintName: "FK_PVUSER_RCA_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "USER_GRP_ID", baseTableName: "RCA_EDIT_USRGRPS", constraintName: "FK_GROUP_RCA_USRGRP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

    changeSet(author: "rishabh", id: "202302122255-7") {
        grailsChange {
            change {
                try {
                        ReasonOfDelayAppEnum.each { ownerApp ->
                            ReasonOfDelayFieldEnum.each { field ->
                                if (!(ownerApp==ReasonOfDelayAppEnum.PVQ && (field==ReasonOfDelayFieldEnum.Root_Cause_Class || field==ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat)))
                                sql.execute("INSERT into RCA_MANDATORY_FIELDS (ID,OWNER_APP,FIELD) values (HIBERNATE_SEQUENCE.nextval,?,?)", [ownerApp.name(),field.name()])
                            }
                        }
                } catch (Exception ex) {
                    println "##### Error Occurred while adding RCA mandatory fields liquibase change set 202302122255-7 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

}
