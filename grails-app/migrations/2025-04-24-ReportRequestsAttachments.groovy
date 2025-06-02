import com.rxlogix.RxCodec
import com.rxlogix.config.ReportRequestAttachment
import groovy.sql.Sql
import groovyx.gpars.GParsPool

databaseChangeLog = {

    changeSet(author: "gunjan", id: "202505051621-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                tableExists(tableName: "REPORT_REQUEST_ATTACH")
            }
        }
        createTable(tableName: "REPORT_REQUEST_ATTACH") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_REQUES_ATTTPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA", type: "long raw")

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "report_request_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "gunjan", id: "202505051621-2") {
        grailsChange{
            change{
                Sql sql = null
                int processedCount = 0
                int skippedCount = 0
                Long lastIdProcessed = 0
                try {
                    println "Encoding Report Request Attachment Names"
                    sql = new Sql(ctx.getBean('reportExecutorService').getReportConnectionForPVR())
                    int offset = 0
                    List data = ReportRequestAttachment.createCriteria().list([max: 100, offset: offset, sort: 'id', readOnly: true]) {
                        projections {
                            property('id')
                            property('name')
                        }
                    }.collect {
                        [name: it[1] , id: it[0] as Long]
                    }
                    while (data) {
                        try {
                            List needToProcess = []
                            GParsPool.withPool{
                                needToProcess = data.findAllParallel {
                                    String decodedValue = RxCodec.decode(it.name)
                                    if (decodedValue) {
                                        skippedCount++
                                        return false
                                    }
                                    return true
                                }}
                            if (needToProcess) {
                                sql.withBatch(100, "UPDATE REPORT_REQUEST_ATTACH SET NAME = :NAME WHERE ID = :ID") { ps ->
                                    needToProcess.each { map ->
                                        ps.addBatch(NAME: RxCodec.encode(map.name), ID: map.id)
                                        processedCount++
                                        lastIdProcessed = map.id
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            println "Error encountered while encoding at $offset: " + ex.message
                            ex.printStackTrace(System.out)
                        }
                        data = ReportRequestAttachment.createCriteria().list([max: 100, offset: offset, sort: 'id', readOnly: true]) {
                            projections {
                                property('id')
                                property('name')
                            }
                        }.collect {
                            [name: it[1], id: it[0] as Long]
                        }
                        offset += 100
                    }
                } finally {
                    println("Processed ${processedCount} records. Skipped ${skippedCount} already encoded records. Last processed ID was ${lastIdProcessed}")
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "gunjan", id: "202505051621-3") {
        preConditions(onFail: "MARK_RAN") {
            not {
                foreignKeyConstraintExists(
                        foreignKeyName: "FK_jy6noas2jeekjkm1pjle2snrj"
                )
            }
        }
        addForeignKeyConstraint(baseColumnNames: "report_request_id", baseTableName: "REPORT_REQUEST_ATTACH", constraintName: "FK_jy6noas2jeekjkm1pjle2snrj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
    }

    changeSet(author: "gunjan", id: "202505051621-7") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(8000 char)", tableName: "REPORT_REQUEST")
    }

    changeSet(author: "gunjan", id: "202505051621-5") {
        modifyDataType(columnName: "NAME", newDataType: "VARCHAR(1000)", tableName: "report_request_attach")
    }

}