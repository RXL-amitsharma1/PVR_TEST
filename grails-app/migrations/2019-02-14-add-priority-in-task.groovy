import com.rxlogix.enums.PriorityEnum

databaseChangeLog = {

    changeSet(author: "Shubham", id: "1536656112625-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TASK', columnName: 'PRIORITY')
            }
        }

        addColumn(tableName: "TASK") {
            column(name: "PRIORITY", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }

        update(tableName: "TASK"){
            column(name:"PRIORITY", value:"${PriorityEnum.MEDIUM}")
        }

        sql("ALTER TABLE TASK MODIFY PRIORITY VARCHAR2(255) NOT NULL;")

    }
}