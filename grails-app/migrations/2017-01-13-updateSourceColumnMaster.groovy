import com.rxlogix.config.ReportField
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "149861249791-2") {
        addColumn(tableName: "SOURCE_COLUMN_MASTER") {
            column(name: "LANG_ID", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "149861249791-4") {
        dropForeignKeyConstraint(baseTableName: "RPT_FIELD", constraintName: "FKF5EE113191FE32E7")
    }

    changeSet(author: "sachinverma (generated)", id: "149861249791-5") {
        dropUniqueConstraint(tableName: 'SOURCE_COLUMN_MASTER', constraintName: 'ARGUS_COLUMN_PK')
    }

    changeSet(author: "sachinverma (generated)", id: "149861249791-6") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "LANG_ID", type: "varchar2(255 char)")
        }

        addColumn(tableName: "CASE_SERIES") {
            column(name: "LANG_ID", type: "varchar2(255 char)")
        }
        sql("update EX_RCONFIG set LANG_ID = 'en'")
        sql("update CASE_SERIES set LANG_ID = 'en'")
    }

    changeSet(author: "sachinverma (generated)", id: "149861249791-7") {
        sql("delete from localization");
    }

    changeSet(author: "sachinverma", id: "149861249791-13") {
        grailsChange {
            change {
                Sql sql = null
                try {
                    if (!ReportField.count) {
                        return
                    }
                    sql = new Sql(ctx.getBean("dataSource_pva"))
                    List<String> newNames = sql.rows("SELECT name from RPT_FIELD").collect {
                        it.name
                    }
                    List<String> oldNames = ReportField.executeQuery("select name from ReportField where isDeleted = false")
                    println oldNames
                    List<String> namesToDelete = oldNames - newNames
                    if (!namesToDelete) {
                        return
                    }
                    namesToDelete.collate(namesToDelete.size().intdiv(300)).each { list ->
                            ReportField.withNewSession { session ->
                                ReportField.executeUpdate("update ReportField set isDeleted = true where name in (:names)", [names: list])
                                session.flush()
                                session.clear()
                                session.close()
                                session.connection()?.close()
                        }
                    }
                }
                catch (Exception ex) {
                    println "##### Error Occurred while updating the ReportFields Data ####"
                    ex.printStackTrace(System.out)
                } finally {
                    sql?.close()
                }
            }
        }
    }

}