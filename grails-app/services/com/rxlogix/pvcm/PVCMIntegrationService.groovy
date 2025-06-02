package com.rxlogix.pvcm

import com.rxlogix.admin.AdminIntegrationApiService
import com.rxlogix.config.SuperQuery
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional

import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import grails.util.Holders
import groovyx.net.http.Method
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import groovy.sql.Sql

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import grails.converters.JSON

@Transactional
/**
 *  author : anurag kumar gupta
 * classname : PVCMIntegrationService
 * description : This new class is added to write all the method which need to be integrated with the PVCM
 */
class PVCMIntegrationService {

    def queryService
    def utilService
    def sqlGenerationService
    AdminIntegrationApiService adminIntegrationApiService



    /**
     * Method to execute the queries and if there is a data then set value as true else false
     * @return : JSONObject
     * @input : intakeCaseId and rountingConditionIds as queryIds
     */
    def runRoutingCondition(String intakeCaseId, List queryIds) {
        log.info("Execution of queries started for intake Case Id : "+intakeCaseId);
        ExecutorService executor = Executors.newFixedThreadPool(queryIds.size());  // Thread size for queue can be handled from here currently it is total size of queries list
        final List<CompletableFuture<String>> taskList = new ArrayList<>();
        int taskId = 1;
        for (int i = 0; i < queryIds.size(); i++) {
            taskList.add(submitTasks(intakeCaseId, queryIds[i], executor))
            taskId += 1;
        }

        CompletableFuture<?>[] allTasksArray = taskList.toArray(new CompletableFuture<?>[taskList.size()])
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(allTasksArray);
        try {
            allTasks.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception while getting the all task data")
            e.printStackTrace();
        }
        if(taskList.size() > 0) {
            List queryResultList = []
            try {
                for (CompletableFuture<String> task : taskList) {
                    String[] idAndResult = task.get().split(":")
                    queryResultList.add(['queryId': idAndResult[0], 'result': idAndResult[1]])
                }
                return (['intakeCaseId': intakeCaseId, 'routingCondition': queryResultList] as JSON)
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception while getting task data result")
                e.printStackTrace();
            } finally {
                // Shutdown the executor to close the threads
                executor.shutdown();
            }
            log.info("Execution of queries finished for intake Case Id : "+intakeCaseId);
        }
        return null
    }

    private CompletableFuture<String> submitTasks(String intakeCaseId, Long queryId, ExecutorService executor) {
        CompletableFuture<String> task = CompletableFuture.supplyAsync({ ->
            // Simulate some computation
            Sql sql
            try {
                User.withNewSession {
                    SuperQuery superQueryInstance = SuperQuery.findById(queryId)
                    if(superQueryInstance) {
                        sql = new Sql(utilService.getReportConnection())
                        Long tenantId = 0L
                        String caseId = null
                        Long versionNum = 0L
                        String caseRecordSql = "SELECT TENANT_ID, CASE_ID, VERSION_NUM FROM V_SAFETY_IDENTIFICATION WHERE INTAKE_CASE_ID=?"
                        def caseInfo = sql.firstRow(caseRecordSql, [intakeCaseId])
                        if (!caseInfo) {
                            return queryId + ":" + false
                        }else {
                            tenantId = caseInfo.TENANT_ID
                            caseId = caseInfo.CASE_ID
                            versionNum = caseInfo.VERSION_NUM
                        }

                        StringBuilder insertSql = new StringBuilder("Begin ")
                        insertSql.append("Insert into GTT_VERSIONS (TENANT_ID, CASE_ID, VERSION_NUM) values (${tenantId}, ${caseId}, ${versionNum});\n")
                        insertSql.append('END;')
                        sql.execute(insertSql.toString().trim())

                        insertSql = new StringBuilder("Begin ")
                        insertSql.append("Insert into GTT_VERSIONS_BASE (TENANT_ID, CASE_ID, VERSION_NUM) values (${tenantId}, ${caseId}, ${versionNum});\n")
                        insertSql.append('END;')
                        sql.execute(insertSql.toString().trim())

                        String initialParamsInsert = sqlGenerationService.insertIntoGttForPVCMWorkflow(queryId, caseId)
                        if (initialParamsInsert) {
                            sql.execute(initialParamsInsert)
                        }
                        Locale defaultLocale = new Locale(System.properties.get("user.language") ?: "en")
                        //Here we don't have the addition query json, ValueList or poiparameters so passing this as null. And in the last we don't have owner details so there will be also passing null as a parameter
                        String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(superQueryInstance, null, null, null, defaultLocale, false, null, null, false)
                        if (insertQueryData) {
                            sql.execute(insertQueryData)
                            sql.call("{call pkg_create_report_sql.p_main_query}")
                            boolean result = false
                            String gttQueryCaseListSql = "SELECT COUNT(1) as COUNT FROM GTT_QUERY_CASE_LIST where CASE_ID=? and VERSION_NUM=?"
                            def rows = sql.firstRow(gttQueryCaseListSql, [caseId, versionNum])
                            if(rows) {
                                if(rows["COUNT"] && rows["COUNT"]>=1) {
                                    result=true
                                }
                            }
                            return queryId + ":" + result
                        }
                    }else {
                        return queryId + ":" + false
                    }
                }
            } catch (SQLException e) {
                log.error("Exception caught while running the routing condition for Intake Case Id : ${intakeCaseId} and QueryId = ${queryId}")
                return queryId + ":" + false
            }catch(InterruptedException e) {
                log.error("Exception in ActionItemNotificationJob: ${e.message}")
                return queryId + ":" + false
            }finally {
                sql?.close()
            }
        }, executor);
        return task;
    };

    /**
     * Method to check query object contains the PVCM tag or not, if yes then call pv admin API
     * @return : void
     * @input : not required
     */
    def checkAndInvokeRoutingCondition(SuperQuery queryInstance, boolean isPreviouslyTagExist) {
        log.info("Checking the Routing Condition exist and then invoke the Routing Condition API")
        boolean isPvcmTagExist = queryInstance?.tags?.any { it.name == Holders.config.getProperty('pvcm.workflowTag')}
        if(isPvcmTagExist || isPreviouslyTagExist) {
            invokeRoutingConditionAPI()
        }
    }

    /**
     * Method to inform that there is an create/update/delete in the query table whose tag is PVCM - Workflow
     * @return : void
     * @input : not required
     */
    void invokeRoutingConditionAPI() {
        try {
            def pvAdminUrl = Holders.config.getProperty('pvadmin.api.url')
            def path = Holders.config.getProperty('pvadmin.api.routing.condition.url')
            if (pvAdminUrl && path) {
                log.info("invoking the routing condition api : " + pvAdminUrl + path)
                Map response = adminIntegrationApiService.postData(pvAdminUrl+path, null, null, Method.POST)
                log.debug("Response from admin : " + response)
                if (response.status == HttpStatus.SC_OK) {
                    log.info("Got success response for Routing Condition with url : " + pvAdminUrl + path)
                } else {
                    log.info("No response for Routing Condition with url : " + pvAdminUrl + path)
                }
            }else {
                log.info("baseUrl or endpoint url is not configured correctly")
            }
        }catch(Exception exception) {
            log.error(exception)
        }
    }

}
