import com.rxlogix.config.ExecutedReportConfiguration

databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "1500558380869-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'STUDY_DRUGS')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "STUDY_DRUGS", type: "CLOB") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "jitin (generated)", id: "1500558380869-6") {

        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty or already executed via 1500558380869-2', onErrorMessage: 'table is empty or already executed via 1500558380869-2') {
            not {
                or {
                    changeSetExecuted(author: "jitin (generated)", id: "1500558380869-2", changeLogFile: "2019-02-14-AddStudyDrugsColumnToExecutedReportConfiguration.groovy")
                    changeSetExecuted(author: "jitin (generated)", id: "1500558380869-4", changeLogFile: "2019-02-14-AddStudyDrugsColumnToExecutedReportConfiguration.groovy")

                }
            }
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM EX_RCONFIG where rownum <2;')
        }

        grailsChange {
            change {
                try {
                    def sqlGenerationService = ctx.getBean('sqlGenerationService')
                    List executedReportConfigurations = ExecutedReportConfiguration.findAllWhere(
                            includeAllStudyDrugsCases: true,
                            isDeleted: false
                    )
                    executedReportConfigurations.each { ExecutedReportConfiguration executedReportConfiguration ->
                        executedReportConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(executedReportConfiguration)
                        executedReportConfiguration.save(flush: true, failOnError: true)
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating old records for ExecutedReportConfiguration liquibase change-set 1500558380869-2 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }

    }
}
