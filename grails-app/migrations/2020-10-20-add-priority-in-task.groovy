import com.rxlogix.enums.PriorityEnum

databaseChangeLog = {

    changeSet(author: "Sargam", id: "1536656112626-2") {
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
            column(name:"PRIORITY", value:"Medium")
        }

        sql("ALTER TABLE TASK MODIFY PRIORITY VARCHAR2(255) NOT NULL;")

    }
}