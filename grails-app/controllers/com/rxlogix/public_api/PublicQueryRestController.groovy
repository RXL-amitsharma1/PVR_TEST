package com.rxlogix.public_api

import com.rxlogix.IcsrController
import com.rxlogix.config.QuerySet
import com.rxlogix.config.SuperQuery
import com.rxlogix.dto.QueryParamDTO
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.async.Promises
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.Constants
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(['permitAll'])
class PublicQueryRestController {

    def queryService
    def customMessageService
    def signalIntegrationService
    def utilService

    /**
     * Method to fetch the query based on the passed user. Used in PVS.
     * @return : Map
     * @input : username,search,max,offset,isEvdasQuery,isFaersQuery,isSafetyQuery
     */
    def getQueriesByUser() {
        List<Map> queryList = []
        int totalCount = 0
        QueryParamDTO queryParamDTO = new QueryParamDTO(params)
        if (queryParamDTO.user) {
            queryList = queryService.getQueriesByUser(queryParamDTO)
            totalCount = queryService.countQueriesByUser(queryParamDTO)
        }
        render([queryList: queryList, totalCount: totalCount] as JSON)
    }

    /**
     * Method to fetch the nameID list.
     * @return : Map
     * @input : queryList
     */
    def queryIdNameList() {
        List<String> queryIdList = params.queryList.split(",")
        List<Map> queryIdNameList = queryService.generateQueryListByIDs(queryIdList)
        render([queryIdNameList: queryIdNameList] as JSON)
    }

    /*
    * API to fetch the count of blank parameter based on the passed queryId.
    * @return : Integer
    */

    def getParameterSize() {
        Integer size = SuperQuery.get(params.queryId)?.getParameterSize() ?: 0
        render([size: size] as JSON)
    }

    /**
     * API to fetch the queryIds used in the Set Builder based on the passed queryId.
     * @return : String
     */
    def getQueriesIdsAsString() {
        String queryIds = null
        SuperQuery superQuery = SuperQuery.get(params.queryId)
        if (superQuery) {
            if (superQuery.queryType == QueryTypeEnum.SET_BUILDER) {
                superQuery = (QuerySet) superQuery
                List<Long> ids = superQuery.queries.id
                queryIds = ids.join(",")
            } else {
                queryIds = superQuery.id
            }
        }
        render([queryIds: queryIds] as JSON)
    }

    /**
     * API to fetch the queryDetails based on the passed queryId.
     * @return : Map
     * @input : queryId
     */
    def getQueryDetail() {
        SuperQuery superQuery = SuperQuery.get(params.queryId)
        render([result: queryDetail(superQuery)] as JSON)
    }

    /**
     * API to fetch the detail of the Non Valid Cases Query.
     * @return : Map
     * @input : queryId
     */
    def getNonValidQuery() {
        SuperQuery superQuery = SuperQuery.findByIsDeletedAndOriginalQueryIdAndNonValidCases(false, 0, true)
        render([result: superQuery ? queryDTO(superQuery) : [:]] as JSON)
    }

    /**
     * API to fetch the query detail list based on the passed queryIds list.
     * @return : List<Map>
     * @input : List<queryId>
     */
    def getQueryListDetail() {
        List<Map> result = []
        if (params.queryIds && params.list("queryIds")) {
            try {
                List<Long> queryIds = params.list("queryIds")?.collect { it as Long }
                List<SuperQuery> superQueryList = SuperQuery.getAll(queryIds)
                result = superQueryList.collect { SuperQuery superQuery -> queryDetail(superQuery) }
            } catch (NumberFormatException nfe) {
                log.error("Invalid number in getQueryListDetail ${nfe.message}")
            }
        }
        render([result: result] as JSON)
    }

    /**
     * API to fetch the query expression values for Query based on the passed queryId.
     * @return : List<Map>
     * @input : queryId
     */
    def queryExpressionValuesForQuery(Long queryId) {
        List<Map> result = queryService.queryExpressionValuesForQuery(queryId) ?: []
        render(result as JSON)
    }

    /**
     * API to fetch the query expression values for Query Set based on the passed queryId.
     * @return : List<List<Map>>
     * @input : queryId
     */
    def queryExpressionValuesForQuerySet(Long queryId) {
        List<List<Map>> result = queryService.queryExpressionValuesForQuerySet(queryId) ?: []
        render(result as JSON)
    }

    /**
     * API to fetch the Custom SQL values for Custom SQL Query based on the passed queryId.
     * @return : List<Map>
     * @input : queryId
     */
    def customSQLValuesForQuery(Long queryId) {
        List<Map> result = queryService.customSQLValuesForQuery(queryId) ?: []
        render(result as JSON)
    }

    def getQueryByName(String queryName) {
        SuperQuery superQuery = queryService.findQueryByName(queryName)
        render([result: superQuery ? queryDTO(superQuery) : [:]] as JSON)
    }

    Map queryDetail(SuperQuery superQuery) {
        superQuery = MiscUtil.unwrapProxy(superQuery)
        Map result = [:]
        if (superQuery) {
            result = queryDTO(superQuery)
            if (superQuery.queryType == QueryTypeEnum.SET_BUILDER) {
                result.queries = superQuery.queries.collect { query ->
                    query = MiscUtil.unwrapProxy(query)
                    queryDTO(query)
                }
            }
        }
        result
    }

    /**
     * Method to fetch the query based on the tag name. Used for PVCM.
     * @return : Map
     * @input : tag name is required
     */
    def fetchQueriesByTag(String tagName){
        log.info("Fetching Queries for tagName ${tagName} ")
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            if(tagName == Constants.PVCM_WORKFLOW) {
                tagName = Holders.config.getProperty('pvcm.workflowTag')
            }
            List<Map> queryList = queryService.fetchQueriesByTag(tagName)
            if (queryList) {
                log.info("Queries for tagName : ${tagName} found successfully")
                responseDTO.setSuccessResponse(queryList, "Queries for tagName : ${tagName} found successfully")
            }else {
                log.warn("Queries for tagName : ${tagName} not found")
                responseDTO.setFailureResponse("Queries for tagName : ${tagName} not found")
            }
        } catch (Exception ex) {
            log.error("Exception occured while fetching queries based on tagName : ${tagName}")
            responseDTO.setFailureResponse("${customMessageService.getMessage('default.server.error.message')}, ${ex}")
        }
        render(responseDTO as JSON)
    }

    /**
     * Method to fetch the query based on the tag name. Used for PVCM.
     * @return : Map
     * @input : Json required which contains intakeCaseId and routingConditionIds
     */
    def fetchResultForWorkflow(){
        ResponseDTO responseDTO = new ResponseDTO()
        if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
            log.info("Fetching result for workflow")
            try {
                def workFlowRulesData = request.JSON
                log.debug("Fetching result for workflow for intakeCaseId : " + workFlowRulesData.intakeCaseId)
                //Here rounting condition Id in PVCM is same as queryId in PVR
                List<Long> routingConditionIds = workFlowRulesData.routingConditionId.toArray()
                def jsonString = Holders.applicationContext.getBean("PVCMIntegrationService").runRoutingCondition(workFlowRulesData.intakeCaseId.toString(), routingConditionIds)
                if (jsonString) {
                    log.info("routing condition runs successfully for intakeCaseId : " + workFlowRulesData.intakeCaseId)
                    responseDTO.setSuccessResponse(jsonString.toString(), "Routing Condition Successfully Completed")
                } else {
                    log.warn("Routing Condition Failed")
                    responseDTO.setFailureResponse("Routing Condition Failed")
                }
            } catch (Exception ex) {
                log.error("Exception occured while fetching result for workflow")
                responseDTO.setFailureResponse("${customMessageService.getMessage('default.server.error.message')}, ${ex}")
            }
        }else {
            responseDTO.setFailureResponse("PVCM is not enabled in PVR application")
        }
        render(responseDTO as JSON)
    }

    def seedLegacyQueryGtts(){
        log.info("Call received to seed legacy data in query GTTs")
        if(grailsApplication.config.pvsignal.url){
            Sql sql = new Sql(utilService.getReportConnectionForPVR())
            // Check if seeding has been done previously
            List<GroovyRowResult> results = null
            try{
                results = sql.rows("SELECT 1 FROM PVS_CACHED_QUERY_DETAILS WHERE LAST_INS_UPD_USR = 'Application' FETCH FIRST 1 ROWS ONLY")
            } finally {
                sql?.close()
            }
            if(!results) {
                Promises.task {
                    Logger logger = LoggerFactory.getLogger(IcsrController.class.getName())
                    User.withNewSession {
                        try {
                            SuperQuery.findAllByIsDeletedAndOriginalQueryId(false, 0l).each {
                                Locale locale = GrailsHibernateUtil.unwrapIfProxy(it.owner?.preference)?.locale ?: new Locale('en')
                                signalIntegrationService.cacheTableInsertionHandler(it, false, "Application", locale)
                            }
                            logger.info("Seeded legacy data in query GTTs")
                        } catch (e) {
                            logger.error("Fatal error while cache table insertion for legacy data", e)
                        }
                    }
                }
            }
            render([status: 200, message: "Success"] as JSON)
        } else {
            render([status: 500, message: "PVS is not integrated"] as JSON)
        }
    }

    private Map queryDTO(SuperQuery query) {
        [id                : query.id,
         name              : query.name,
         description       : query.description,
         queryType         : query.queryType?.name(),
         JSONQuery         : query.JSONQuery,
         hasBlanks         : query.hasBlanks,
         reassessListedness: query?.queryType == QueryTypeEnum.QUERY_BUILDER ? query?.reassessListedness?.name() : null,
         customSQLQuery    : query.queryType == QueryTypeEnum.CUSTOM_SQL ? query?.customSQLQuery : ""
        ]
    }

}
