import com.rxlogix.config.ExecutedCaseDeliveryOption
import com.rxlogix.config.ExecutedCaseSeries

databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "149861244444-17") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty or already executed via 149861244444-16', onErrorMessage: 'table is empty or already executed via 149861244444-16') {
            not {
                changeSetExecuted(author: "prashantsahi (generated)", id: "149861244444-16", changeLogFile: "2018-01-18-add-DeliveryOption-to-CaseSeries.groovy")
            }
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM EX_CASE_SERIES where rownum <2;')
        }
        grailsChange {
            change {
                try {
                    ExecutedCaseSeries.withNewSession { session ->

                        List<ExecutedCaseSeries> execCSList = ExecutedCaseSeries.findAllByIsTemporary(false).findAll {
                            !it.executedDeliveryOption
                        }
                        ExecutedCaseDeliveryOption executedCaseDeliveryOption
                        execCSList.each {
                            executedCaseDeliveryOption = new ExecutedCaseDeliveryOption()
                            it.executedDeliveryOption = executedCaseDeliveryOption.addToSharedWith(it.owner)
                            it.save(flush: true)
                        }
                        session.clear()
                        session.close()
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while creating Case Delivery Option for Executed Case Series liquibase change set 149861244444-16 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }
}