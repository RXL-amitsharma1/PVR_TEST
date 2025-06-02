import com.rxlogix.config.Query
import com.rxlogix.config.QuerySet
import grails.converters.JSON

databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1515770109368-2") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty or already executed via 1515770109368-1', onErrorMessage: 'table is empty or already executed via 1515770109368-1') {
            not {
                changeSetExecuted(author: "forxsv (generated)", id: "1515770109368-1", changeLogFile: "2018-01-12-update-JSONQuery.groovy")
            }
            sqlCheck(expectedResult: '1', 'SELECT COUNT(1) FROM SUPER_QUERY where rownum <2;')
        }
        grailsChange {
            change {
                try {
                    Query.withNewSession { session ->
                        Query.findAllByIsDeletedAndOriginalQueryId(false, 0L).each { query ->
                            def q = JSON.parse(query.JSONQuery)
                            if (q.all.containerGroups.size() > 1) {
                                def blankParameters = q.blankParameters
                                def copyAndPasteFields = q.copyAndPasteFields
                                def newJSONQuery = [all: [
                                        containerGroups: [[expressions: q.all.containerGroups, keyword: q.all.keyword]]
                                ]]
                                if (blankParameters) newJSONQuery.blankParameters = blankParameters
                                if (copyAndPasteFields) newJSONQuery.copyAndPasteFields = copyAndPasteFields
                                query.JSONQuery = newJSONQuery as JSON
                                query.save(failOnError: true, validate: false)
                                println "!  Query ID: " + query.id + " Name: " + query.name + " ----updated"
                            }
                        }
                        QuerySet.findAllByIsDeletedAndOriginalQueryId(false, 0L).each { query ->
                            def q = JSON.parse(query.JSONQuery)
                            if (q.all.containerGroups.size() > 1) {
                                def newJSONQuery = [all: [
                                        containerGroups: [[expressions: q.all.containerGroups, keyword: q.all.keyword]]
                                ]]
                                query.JSONQuery = newJSONQuery as JSON
                                query.save(failOnError: true, validate: false)
                                println "!  QuerySet ID: " + query.id + " Name: " + query.name + " ----updated"
                            }
                        }
                        session.flush()
                        session.clear()
                        session.close()
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating JSONQuery liquibase change set 1515770109368-1 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

}