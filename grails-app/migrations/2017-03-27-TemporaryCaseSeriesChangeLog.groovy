import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedPeriodicReportConfiguration

databaseChangeLog = {
    changeSet(author: "prashantsahi (generated)", id: "1488888888888-88") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty.', onErrorMessage: 'table is empty.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM EX_CASE_SERIES where rownum <2;')
        }
        grailsChange {
            change {
                try {

                    ExecutedCaseSeries.withNewSession { session ->
                        List<ExecutedCaseSeries> temporaryCaseSeries = ExecutedCaseSeries.findAllByIsTemporaryAndIsDeleted(true, false)
                        temporaryCaseSeries?.each { ExecutedCaseSeries exCaseSeries ->
                            if (ExecutedPeriodicReportConfiguration.findByCaseSeriesOrCumulativeCaseSeries(exCaseSeries, exCaseSeries)) {
                                exCaseSeries.isTemporary = false
                                exCaseSeries.save(failOnError: true)
                            }
                        }
                        session.flush()
                        session.clear()
                        session.close()
                        session.connection()?.close()
                    }

                } catch (Exception ex) {
                    println "##### Error Occurred while updating the temporary Case Series used in Executed Periodic Report liquibase change set 1488888888888-88 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }
}