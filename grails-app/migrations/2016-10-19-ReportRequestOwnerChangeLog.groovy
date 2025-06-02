import com.rxlogix.config.ReportRequest
import com.rxlogix.user.User

databaseChangeLog = {

    changeSet(author: "Meenakshi (generated)", id: "1476877398138-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'OWNER_ID')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "OWNER_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Meenakshi (generated)", id: "1476877398138-2") {
        grailsChange {
            change {
                try {
                    int count = ReportRequest.count()
                    int max = 10
                    int offset = 0
                    while (offset < count) {
                        ReportRequest.withNewSession { session ->
                            ReportRequest.list([max: max, offset: offset, order: 'asc', sort: 'id']).each { rr ->
                                Long id = User.findByUsername(rr.createdBy).id
                                sql.executeUpdate("update REPORT_REQUEST set OWNER_ID = ? WHERE OWNER_ID is null ", [id])
                            }
                            offset += max
                            session.flush()
                            session.clear()
                            session.close()
                            session.connection()?.close()
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating old records for ReportRequest liquibase change-set 1476877398138-1 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "Meenakshi (generated)", id: "1476877398138-3") {
        addNotNullConstraint(tableName: "REPORT_REQUEST", columnName: "OWNER_ID")
    }

}
