package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.SqlGenIDDTO
import com.rxlogix.config.CustomSQLQuery
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ParameterValue
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.dto.BlindedUsersDTO
import com.rxlogix.enums.CallbackStatusEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.helper.LocaleHelper
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.FieldProfileFields
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.async.Promises
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.StreamingJsonBuilder
import groovy.sql.Sql
import groovyx.net.http.Method
import groovy.json.JsonBuilder
import org.apache.http.HttpStatus
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class SignalIntegrationService {

    def dynamicReportService
    def CRUDService
    GrailsApplication grailsApplication
    def signalIntegrationApiService
    def userService
    def utilService
    final static String SAVE_SIGNAL_REPORT = "/signal/api/signalReport"
    final static String SYNC_PRODUCT_GROUP = "/signal/api/productGroup/syncProductGroup"
    final static String SYNC_TEMPLATE_QUERY = "/signal/api/productGroup/updateTemplateQuery"
    final static String SYNC_BLINDED_DATA = "/signal/api/productGroup/updateGroupFieldMapping"
    private static final String DATETIME_FMT = "dd-MM-yyyy HH:mm:ss"
    private static final String DATETIME_FRMT = "dd-MMM-yyyy HH:mm:ss"


    def getSignalReportInfo(configuration) {
        SignalReportInfo signalReportInfo = SignalReportInfo.findByConfigurationAndIsGenerating(configuration, true)
        signalReportInfo
    }

    /**
     * Method to post the signal report result from the executed report configuration method to pvs application.
     * @param executedConfiguration
     * @return
     */
    def saveSignalReportResult(ExecutedReportConfiguration executedConfiguration, SignalReportInfo signalReportInfo) {
        log.info("Generating the signal report.")
        try {
            if (signalReportInfo) {
                //Fetch the reports.
                Map xlsFormat = [outputFormat: ReportFormatEnum.XLSX.name()]
                File reportFileXlsx = dynamicReportService.createMultiTemplateReport(executedConfiguration, xlsFormat)
                Map pdfFormat = [outputFormat: ReportFormatEnum.PDF.name()]
                File reportFilePdf = dynamicReportService.createMultiTemplateReport(executedConfiguration, pdfFormat)
                Map docxFormat = [outputFormat: ReportFormatEnum.DOCX.name()]
                File reportFileDocx = dynamicReportService.createMultiTemplateReport(executedConfiguration, docxFormat)
                String userName = executedConfiguration.owner.getUsername()

                String url = grailsApplication.config.pvsignal.url

                StringWriter writer = new StringWriter()

                try {
                    StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
                    builder.reportBuilder(
                            pdfReport: reportFilePdf.getBytes(), wordReport: reportFileDocx.getBytes(), excelReport: reportFileXlsx.getBytes(), exConfigId: executedConfiguration.id,
                            reportName: signalReportInfo.reportName, userName: userName, linkUrl: signalReportInfo.linkUrl, userId: signalReportInfo.userId, isReportGenerated: true
                    )
                    signalIntegrationApiService.postData(url, SAVE_SIGNAL_REPORT, writer.toString(), Method.POST)
                } catch (Exception ex) {
                    ex.printStackTrace()
                    return [status: -1]
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
        }
        log.info("Exiting the signal report generation flow.")
    }

    def sendErrorNotification(ExecutedReportConfiguration executedConfiguration, SignalReportInfo signalReportInfo) {
        log.error("Error occured while generating the signal report \"${executedConfiguration.reportName}\".")
        try {
            if (signalReportInfo) {
                String userName = executedConfiguration.owner.getUsername()

                String url = grailsApplication.config.pvsignal.url

                StringWriter writer = new StringWriter()

                try {
                    StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
                    builder.reportBuilder(
                            exConfigId: executedConfiguration.id, reportName: signalReportInfo.reportName, userName: userName, linkUrl: signalReportInfo.linkUrl, userId: signalReportInfo.userId, isReportGenerated: false
                    )
                    signalIntegrationApiService.postData(url, SAVE_SIGNAL_REPORT, writer.toString(), Method.POST)
                } catch (Exception ex) {
                    ex.printStackTrace()
                    return [status: -1]
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
        }
    }

    def notifyExecutedCaseSeriesStatus(ExecutionStatus executionStatus) {
        Promises.task {
            ExecutionStatus.withNewSession {
                try {
                    Map data = [executionStatus: executionStatus.executionStatus, executedSeriesId: executionStatus.entityId, isTemporary: ExecutedCaseSeries.get(executionStatus.entityId)?.isTemporary]
                    log.info("Notifying for Case Series with  ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL} and Data: ${data}")
                    Map response = signalIntegrationApiService.postCallback(executionStatus.callbackURL, data, Method.GET)
                    if (response.status == HttpStatus.SC_OK) {
                        if (response.result.status) {
                            log.info("Got success response for ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL}")
                            ExecutionStatus.executeUpdate("update ExecutionStatus set callbackStatus=:callbackStatus where id=:id", [id: executionStatus.id, callbackStatus: CallbackStatusEnum.ACKNOWLEDGED])
                        }
                    } else {
                        log.info("No response for ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL}")
                    }
                } catch (Exception ex) {
                    log.error("Got some error while notifying for ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL}")
                    log.error(ex.message)
                }
            }
        }
    }

    def notifyExecutedConfigurationStatus(ExecutionStatus executionStatus) {
        Promises.task {
            ExecutionStatus.withNewSession {
                try {
                    Map data = [executionStatus: executionStatus.executionStatus, executedConfigurationId: executionStatus.entityId]
                    log.info("Notifying for Report with ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL} and Data: ${data}")
                    Map response = signalIntegrationApiService.postCallback(executionStatus.callbackURL, data, Method.GET)
                    if (response.status == HttpStatus.SC_OK) {
                        if (response.result.status) {
                            log.info("Got success response for ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL}")
                            ExecutionStatus.executeUpdate("update ExecutionStatus set callbackStatus=:callbackStatus where id=:id", [id: executionStatus.id, callbackStatus: CallbackStatusEnum.ACKNOWLEDGED])
                        }
                    } else {
                        log.info("No response for Report with ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL}")
                    }
                } catch (Exception ex) {
                    log.error("Got some error while notifying for ExecutionStatus ID: ${executionStatus.id} with callbackURL ${executionStatus.callbackURL}")
                    log.error(ex.message)
                }
            }
        }
    }

    void notifySignalForUpdate(def updateInstance) {
        try {
            String baseUrl = grailsApplication.config.pvsignal.url
            log.info("signalIntegrationService - notifySignalForUpdate called for " + updateInstance.getClass())
            String name = (updateInstance instanceof DictionaryGroup) ? updateInstance.getGroupName() : updateInstance.getName()
            Map data = ['id': updateInstance.getId(), 'name': name, 'isTemplate': false]
            String path = updateInstance instanceof DictionaryGroup ? SYNC_PRODUCT_GROUP : SYNC_TEMPLATE_QUERY
            data['isTemplate'] = updateInstance instanceof ReportTemplate
            JSON dataJSON = data as JSON
            Map response = signalIntegrationApiService.postData(baseUrl, path, dataJSON.toString(), Method.POST)
            log.info("CallBack to PVSignal for Update - status: " + response.status)
        }
        catch (Exception e) {
            log.error("Exception while notifying PVSignal for Update in SignalIntegrationService - notifySignalForUpdate", e)
        }
    }

    /**
     * This method is used to send the updated Field Profile data to PVS using the PVS API endpoint
     * <br><code>/signal/api/productGroup/updateGroupFieldMapping</code>.
     * @param userGroup User Group linked to the updated Field Profile
     * @param isPrivacyProfile Flag to indicate whether the updated Profile was the one associated with PV Admin or not
     * @return void
     */
    void updateBlindedDataToSignal(UserGroup userGroup, boolean isPrivacyProfile = false) {
        String baseUrl = grailsApplication.config.pvsignal.url
        log.info("signalIntegrationService - updateBlindedDataToSignal called for " + userGroup)
        String path = SYNC_BLINDED_DATA
        Map responseMap = [:]
        List<BlindedUsersDTO> blindedUsersDTOList = []
        String adminProfileName = Holders.config.getProperty("pvadmin.privacy.field.profile")
        try {
            userGroup.getUsers().each { User user ->
                if (user && !user.enabled) {
                    blindedUsersDTOList.add(new BlindedUsersDTO(true, user, user.username))
                } else if (user && user.enabled) {
                    BlindedUsersDTO blindedUsersDTO = new BlindedUsersDTO(false, user, user.username)
                    blindedUsersDTO.blindedFieldIds = userService.fetchUniqueFieldIdList(blindedUsersDTO.blindedFieldIds)
                    blindedUsersDTO.protectedFieldIds = userService.fetchUniqueFieldIdList(blindedUsersDTO.protectedFieldIds)
                    blindedUsersDTOList.add(blindedUsersDTO)
                }
            }
            responseMap.put("users", blindedUsersDTOList)
            responseMap.put("groupName", userGroup.isBlinded ? userGroup.name : null)
            responseMap.put("adminProfileName", adminProfileName)
            List<String> adminProfileFields = FieldProfileFields.findAllByFieldProfileAndIsProtected(FieldProfile.findByNameAndIsDeleted(adminProfileName, false), true).collect {it.reportField.name}
            responseMap.put("adminProfileFields", adminProfileName ? userService.fetchUniqueFieldIdList(adminProfileFields) : [])
            JSON dataJSON = responseMap as JSON
            Map response = signalIntegrationApiService.postData(baseUrl, path, dataJSON.toString(), Method.POST)
            log.info("CallBack to PVSignal for Update - status: " + response.status)
        } catch (Exception ex) {
            log.error("Exception while notifying PVSignal for Update in SignalIntegrationService - updateBlindedDataToSignal", ex)
        }
    }

    void cacheTableInsertionHandler(SuperQuery superQuery, boolean update_flag, String currUser, Locale locale) {
        String currDate = new Date().format("yyyy-MM-dd HH:mm:ss")
        superQuery = GrailsHibernateUtil.unwrapIfProxy(superQuery)
        String queryString = """
        DECLARE lastInsertedRow ROWID;
        BEGIN
        """
        Long query_id = superQuery.id
        if (update_flag) {
            queryString = queryString + """
            DELETE FROM PVS_CACHED_QUERY_SETS WHERE QUERY_ID = ${query_id};
            DELETE FROM PVS_CACHED_QUERY_DETAILS WHERE QUERY_ID = ${query_id};
            """
        }
        SqlGenIDDTO sqlGenIDDTO = new SqlGenIDDTO()
        queryString = queryString + insertQueriesDataToCacheTable(superQuery, sqlGenIDDTO, null, 0, [], [], null, locale, null, query_id, currDate, currUser)
        queryString = queryString + "\n END; \n"
        Sql sql = new Sql(utilService.getReportConnectionForPVR())
        try {
            sql.execute(queryString)
        } finally {
            sql?.close()
        }
    }

    String insertQueriesDataToCacheTable(SuperQuery superQuery, SqlGenIDDTO sqlGenIDDTO, String joinOperator, int parent, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale, Integer query_flag, Long query_id, String currDate, String currUser) {
        String insertQuery = ""
        superQuery = GrailsHibernateUtil.unwrapIfProxy(superQuery)
        if (superQuery?.queryType == QueryTypeEnum.QUERY_BUILDER) {
            insertQuery = insertQuery + buildQueryFromJSONQuery(superQuery.JSONQuery, sqlGenIDDTO, parent, joinOperator, superQuery.hasBlanks, blanks, poiInputParams, locale, query_flag, query_id, currDate, currUser)
        } else if (superQuery?.queryType == QueryTypeEnum.CUSTOM_SQL) {
            superQuery = (CustomSQLQuery) superQuery
            String sqlQuery = superQuery.customSQLQuery
            insertQuery += "INSERT INTO PVS_CACHED_QUERY_SETS (QUERY_ID,CUSTOM_SQL,GROUP_ID,PARENT_GROUP_ID,QUERY_FLAG,SET_ID,SET_OPERATOR,LAST_INS_UPD_DT,LAST_INS_UPD_USR) values (${query_id},${sqlQuery ? "'${sqlQuery.replaceAll("'", "''")}'" : null},${parent ?: 0},${parent ?: 0},${query_flag},${sqlGenIDDTO.value},${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},TO_TIMESTAMP('${currDate}', 'YYYY-MM-DD HH24:MI:SS'),${currUser ? "'${currUser}'" : null}); \n"
        } else if (superQuery?.queryType == QueryTypeEnum.SET_BUILDER) {
            Map dataMap = MiscUtil.parseJsonText(superQuery.JSONQuery)
            Map allMap = dataMap.all
            List containerGroupsList = allMap.containerGroups
            insertQuery = insertQuery + insertSqlStatementFromQuerySetStatement(containerGroupsList, sqlGenIDDTO, new SqlGenIDDTO(), 0, 0, joinOperator, blanks, customSqlBlanks, poiInputParams, locale, query_flag, query_id, currDate, currUser)
        }
        insertQuery += ""
        return insertQuery
    }

    String buildQueryFromJSONQuery(String JSONQuery, SqlGenIDDTO sqlGenIDDTO, int parent, String joinOperator, boolean hasBlanks, List blanks, Set<ParameterValue> poiInputParams, Locale locale, Integer query_flag, Long query_id, String currDate, String currUser) {
        Map dataMap = MiscUtil.parseJsonText(JSONQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String query = "INSERT INTO PVS_CACHED_QUERY_SETS (QUERY_ID,CUSTOM_SQL,GROUP_ID,PARENT_GROUP_ID,QUERY_FLAG,SET_ID,SET_OPERATOR,LAST_INS_UPD_DT,LAST_INS_UPD_USR) values (${query_id},null,${parent},${parent},${query_flag},$sqlGenIDDTO.value,${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},TO_TIMESTAMP('${currDate}', 'YYYY-MM-DD HH24:MI:SS'),${currUser ? "'${currUser}'" : null}); \n"
        query = query + insertSqlStatementFromQueryBuilderStatement(containerGroupsList, sqlGenIDDTO.value, new SqlGenIDDTO(), 0, 0, null, hasBlanks, blanks, poiInputParams, locale, query_id, currDate, currUser)
        return query
    }

    private String insertSqlStatementFromQueryBuilderStatement(
            def data, Integer setId, SqlGenIDDTO sqlGenIDDTO, int parent, Integer index, String joinOperator, boolean hasBlanks, List<ParameterValue> blanks, Set<ParameterValue> poiInputParams, Locale locale, Long query_id, String currDate, String currUser) {
        if (data instanceof Map && data.expressions) {
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            String groupInsert = "INSERT INTO PVS_CACHED_QUERY_DETAILS (QUERY_ID,CUSTOM_INPUT,FIELD_ID,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,JAVA_VARIABLE,PARENT_GROUP_ID,SET_ID,LAST_INS_UPD_DT,LAST_INS_UPD_USR) values (${query_id},null,null,null,null,${sqlGenIDDTO.value},${data.keyword ? ("'${data.keyword.toUpperCase()}'") : null},null,${parent},$setId,TO_TIMESTAMP('${currDate}', 'YYYY-MM-DD HH24:MI:SS'),${currUser ? "'${currUser}'" : null}); \n"
            return groupInsert + insertSqlStatementFromQueryBuilderStatement(data.expressions, setId, sqlGenIDDTO, sqlGenIDDTO.value, 0, data.keyword as String, hasBlanks, blanks, poiInputParams, locale, query_id, currDate, currUser)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                    query = query + insertSqlStatementFromQueryBuilderStatement(val, setId, sqlGenIDDTO, parent, i, joinOperator, hasBlanks, blanks, poiInputParams, locale, query_id, currDate, currUser)
                }
                return query
            } else {
                //CUSTOM_INPUT LOGIC
                String customInput = null
                if (hasBlanks && (!data.value || data.value.toString().matches(Constants.POI_INPUT_PATTERN_REGEX))) {
                    customInput = data.value
                    ParameterValue parameterValue = blanks ? blanks?.get(0) : null

                    if (parameterValue) {
                        data.value = parameterValue.value
                        blanks.remove(parameterValue)
                    }
                }
                if (customInput && data.value && poiInputParams*.key.contains(customInput)) {
                    ParameterValue parameterValue = poiInputParams.find { it.key == customInput }
                    parameterValue.value = data.value
                }
                data.value = normalizeValue(data.field, data.op, data.value, locale)
                String datasheetValues = null
                if (data.field == "dvListednessReassessQuery" && data.RLDS) {
                    datasheetValues = "'" + data.RLDS.split(";").join("'~!@#@!~'") + "," + (data.RLDS_OPDS == "true" ? 1 : 0) + "'"
                }
                String outSql = ""
                String apostrophe = "";
                if (data.value?.size() > 3999) {
                    data.value.split("(?<=\\G.{3999})").eachWithIndex { String part, int i ->
                        String entry
                        if (apostrophe == "'")
                            entry = part.substring(1)
                        else
                            entry = part

                        if (part[part.length() - 1] == "'" && part[part.length() - 2] != "'") {
                            apostrophe = "'"
                            entry = entry + "'"
                        } else {
                            apostrophe = ""
                        }

                        if (i == 0)
                            outSql = "INSERT INTO PVS_CACHED_QUERY_DETAILS (QUERY_ID,ADDL_PARAMS,CUSTOM_INPUT,FIELD_ID,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,JAVA_VARIABLE,PARENT_GROUP_ID,SET_ID,LAST_INS_UPD_DT,LAST_INS_UPD_USR) values (${query_id},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null},${customInput ? "'${customInput}'" : null},$index,'${data.op?.toUpperCase()}','${entry}',${parent},null,'${data.field}',${parent},$setId,TO_TIMESTAMP('${currDate}', 'YYYY-MM-DD HH24:MI:SS'),${currUser ? "'${currUser}'" : null}) returning ROWID into lastInsertedRow; \n"
                        else
                            outSql += "UPDATE PVS_CACHED_QUERY_DETAILS SET FIELD_VALUES=FIELD_VALUES||'${entry}' WHERE ROWID=lastInsertedRow;\n "
                    }
                } else {
                    boolean isRptCompare = data.value?.matches(Constants.RPT_INPUT_PATTERN_REGEX) ?: false
                    if (isRptCompare) {
                        data.value = data.value.replace(Constants.RPT_INPUT_PREFIX, '');
                    }
                    outSql = "INSERT INTO PVS_CACHED_QUERY_DETAILS (QUERY_ID,ADDL_PARAMS,CUSTOM_INPUT,FIELD_ID,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,JAVA_VARIABLE,PARENT_GROUP_ID,IS_FIELD_COMPARE,SET_ID,LAST_INS_UPD_DT,LAST_INS_UPD_USR) values (${query_id},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null}, ${customInput ? "'${customInput}'" : null},$index,'${data.op?.toUpperCase()}', ${data.value ? "'${data.value}'" : null},${parent},null,'${data.field}',${parent},${isRptCompare ? 1 : 0},$setId,TO_TIMESTAMP('${currDate}', 'YYYY-MM-DD HH24:MI:SS'),${currUser ? "'${currUser}'" : null}); \n"
                }
                return outSql
            }
        }
    }

    private String normalizeValue(String field, String op, String value, Locale locale) {
        if (op in QueryOperatorEnum.valuelessOperators*.name()) {
            value = null //fix for valueless operator contatining operator as value
        }
        if (value) {
            ReportField reportField = ReportField.findByNameAndIsDeleted(field, false)
            boolean isDate = value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)
            if (reportField && reportField.isDate()) {
                if (isDate) {
                    if (op in QueryOperatorEnum.numericValueDateOperators*.name()) {
                        return value
                    }
                    //Try to parse date in any of available formats as Date input can be in any of the format as per last updated by user.
                    Date parsedDate = null
                    LocaleHelper.supportedLocales.find { Locale lc ->
                        String dateFormat = reportField.getDateFormat(lc.toString())
                        if (dateFormat) {
                            if (!value.contains(':') && dateFormat.contains(':')) {
                                parsedDate = DateUtil.parseDate((value + " 00:00:00"), dateFormat)
                            } else {
                                parsedDate = DateUtil.parseDate(value, dateFormat)
                            }
                        }
                        if (!parsedDate) {
                            dateFormat = DATETIME_FRMT
                            parsedDate = DateUtil.parseDate((value + " 00:00:00"), dateFormat)
                        }
                        return !!parsedDate
                    }
                    return parsedDate?.format(DATETIME_FMT)
                }
            }
            if (op in [QueryOperatorEnum.EQUALS.name(), QueryOperatorEnum.NOT_EQUAL.name()] && value.indexOf(';') != -1) {
                value = "'" + value.split(";").join("'~!@#@!~'") + "'"
            } else if (reportField?.isString()) {
                value = "'" + value + "'"
            } else {
                value = "'" + value + "'"
            }
            return value.replaceAll("'", "''")
        }
        return value
    }

    private String insertSqlStatementFromQuerySetStatement(
            def data, SqlGenIDDTO sqlGenIDDTO, SqlGenIDDTO parentSqlGenIDTO, Integer parent, Integer index, String joinOperator, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale, Integer query_flag, Long query_id, String currDate, String currUser) {
        if (data instanceof Map && data.expressions) {
            parentSqlGenIDTO.value = parentSqlGenIDTO.value + 1
            String groupInsert = "INSERT INTO PVS_CACHED_QUERY_SETS (QUERY_ID,CUSTOM_SQL,GROUP_ID,PARENT_GROUP_ID,QUERY_FLAG,SET_ID,SET_OPERATOR,LAST_INS_UPD_DT,LAST_INS_UPD_USR) values (${query_id},null,${parentSqlGenIDTO.value},${parent},${query_flag},${sqlGenIDDTO.value},${data.keyword ? "'${data.keyword.toUpperCase()}'" : null},TO_TIMESTAMP('${currDate}', 'YYYY-MM-DD HH24:MI:SS'),${currUser ? "'${currUser}'" : null}) ;\n"
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            return groupInsert + insertSqlStatementFromQuerySetStatement(data.expressions, sqlGenIDDTO, parentSqlGenIDTO, parentSqlGenIDTO.value, 0, data.keyword as String, blanks, customSqlBlanks, poiInputParams, locale, query_flag, query_id, currDate, currUser)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                    sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                    query = query + insertSqlStatementFromQuerySetStatement(val, sqlGenIDDTO, parentSqlGenIDTO, parent, i, joinOperator, blanks, customSqlBlanks, poiInputParams, locale, query_flag, query_id, currDate, currUser)
                }
                return query
            } else {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                return insertQueriesDataToCacheTable(SuperQuery.load(data.query), sqlGenIDDTO, joinOperator, parent, blanks, customSqlBlanks, poiInputParams, locale, query_flag, query_id, currDate, currUser)
            }
        }
    }

}
