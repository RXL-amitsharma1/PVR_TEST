package com.rxlogix

import com.rxlogix.config.ExcludeCase
import com.rxlogix.config.IncludeCase
import com.rxlogix.config.BalanceMinusQuery
import com.rxlogix.config.SourceProfile
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.poi.xssf.usermodel.*
import com.rxlogix.util.ViewHelper
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import org.grails.core.exceptions.GrailsRuntimeException

import java.sql.Connection
import java.text.SimpleDateFormat

@Transactional(readOnly = true)
class BalanceMinusQueryService {

    public static final String DATE_FMT = "dd-MM-yyyy"

    def dataSource_pva
    def qualityService
    def configurationService
    def userService
    def utilService

    def fetchBalanceMinusQueryList(def params, String tableName) {
        def sql = new Sql(dataSource_pva)
        def results
        Integer totalCount = 0
        try {
            String totalQuery = "SELECT COUNT(1) as count from "+tableName
            results = sql.rows("SELECT * from "+tableName+" OFFSET ${params.offset} ROWS FETCH NEXT ${params.max} ROWS ONLY")
            totalCount = sql.firstRow(totalQuery)["count"]
        } catch (Exception ex) {
            log.error("Unknown exception occurred fetching balance minus query list fetchBalanceMinusQueryList()" + ex.getMessage())
            ex.printStackTrace()
            return [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        } finally {
            if (sql) {
                sql.close()
            }
        }
        return [aaData: results, recordsTotal: totalCount, recordsFiltered: totalCount]
    }

    void initializeGttTablesForBMQuery(BalanceMinusQuery bmQueryInstance) {
        def sql
        try {
            log.info("Passing the parameter value to DB Started");
            sql = new Sql(getReportConnection())
            bmQueryInstance?.bmQuerySections?.eachWithIndex() { bmQuerySection, i ->
                log.info("Passing the parameter value to DB for the dataSource :: "+bmQuerySection.sourceProfile.sourceName);
                StringBuilder insertGttParamsQuery = new StringBuilder("Begin ")
                StringBuilder insertGttFilterQuery = new StringBuilder()
                String cleaningUpGttTable = ("Begin execute immediate ('Truncate table GTT_REPORT_INPUT_PARAMS');" +
                        "execute immediate ('Truncate table GTT_FILTER_KEY_VALUES'); END; ")
                insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATA_SOURCE','${bmQuerySection.sourceProfile.sourceName}');")
                insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TYPE','${bmQuerySection.executeFor}');")
                if (bmQuerySection.executeFor == 'ETL_START_DATE') {
                    String executionStartDate = bmQuerySection.executionStartDate?.format(DateUtil.DATETIME_FMT)
                    String executionEndDate = DateUtil.getDateWithDayEndTime(bmQuerySection.executionEndDate)?.format(DateUtil.DATETIME_FMT)
                    insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VALIDATION_START_DATE','${executionStartDate}');")
                    insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VALIDATION_END_DATE','${executionEndDate}');")
                } else if (bmQuerySection.executeFor == 'LAST_X_DAYS') {
                    insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LAST_X_DAYS','${bmQuerySection.xValue}');")
                } else if (bmQuerySection.executeFor == 'LAST_X_ETL') {
                    insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LAST_X_ETL','${bmQuerySection.xValue}');")
                } else if (bmQuerySection.executeFor == 'CASE_LIST') {
                    bmQuerySection?.includeCases?.each { obj ->
                        insertGttFilterQuery.append("INSERT INTO GTT_FILTER_KEY_VALUES (CODE, TEXT) VALUES('DATAVAL_INPUT_CASE_LIST','${obj.caseNumber}');")
                    }
                }
                insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_EXCLUDE','${bmQuerySection.flagCaseExclude}');")
                if (bmQuerySection.flagCaseExclude) {
                    bmQuerySection?.includeCases?.each { obj ->
                        insertGttFilterQuery.append("INSERT INTO GTT_FILTER_KEY_VALUES (CODE, TEXT) VALUES('DATAVAL_EXCLUDE_CASE_LIST','${obj.caseNumber}');")
                    }
                }
                insertGttParamsQuery.append("Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_TABLE_LIST','${bmQuerySection?.distinctTables?.size() ? 1 : 0}');")
                bmQuerySection?.distinctTables?.each { obj ->
                    insertGttFilterQuery.append("INSERT INTO GTT_FILTER_KEY_VALUES (CODE, TEXT) VALUES('DATAVAL_INPUT_TABLE_LIST','${obj.entity}');")
                }
                insertGttParamsQuery.append('END;')
                def start = DateUtil.StringToDate(bmQueryInstance.startDateTime, com.rxlogix.Constants.DateFormat.WITHOUT_SECONDS)
                def startDate = new java.sql.Timestamp(start.getTime())
                sql.execute(cleaningUpGttTable)
                log.debug("GTT Report Input Params : "+insertGttParamsQuery.toString())
                sql.execute(insertGttParamsQuery.toString())
                if(insertGttFilterQuery.toString()) {
                    log.debug("GTT Filter Key Values : "+insertGttFilterQuery.toString())
                    sql.execute("BEGIN " +insertGttFilterQuery.toString()+ "END;")
                }
                sql.call('{call PKG_BQMQ_JOB.P_CREATE_SCHEDULE_FROM_UI(?,?)}', [startDate, getRecurrenceForBQMQ(bmQueryInstance.repeatInterval)])
                log.debug("PKG_BQMQ_JOB.P_CREATE_SCHEDULE_FROM_UI called Successfully")
            }
            log.info("Passing the parameter value to DB End");
        } catch (Exception e) {
            log.error("Error occurred in initialize GttTables For BMQuery", e.getMessage())
            throw e
        } finally {
            sql?.close()
        }
    }

    def getValidationSummaryList(def params, String tableName) {
        def sql = new Sql(dataSource_pva)
        def results
        Integer totalCount = 0
        Integer filteredCount = 0
        String whereClause = ''
        try {
            String totalQuery = "SELECT COUNT(1) as count from "+tableName
            if (params.searchString && params.searchString.toString().trim()!='') {
                whereClause = " WHERE " + getValidationSummaryWhereClause(params.searchString.toString().trim().toUpperCase())
            }
            String filteredQuery = "SELECT COUNT(1) as count from "+tableName + whereClause
            results = sql.rows("SELECT * from "+tableName+ whereClause+" order by ${params.sort} ${params.direction} OFFSET ${params.offset} ROWS FETCH NEXT ${params.max} ROWS ONLY")
            totalCount = sql.firstRow(totalQuery)["count"]
            filteredCount = sql.firstRow(filteredQuery)["count"]
        } catch (Exception ex) {
            log.error("Unknown exception occurred fetching validation summary list getValidationSummaryList()" + ex.getMessage())
            ex.printStackTrace()
            return [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        } finally {
            if (sql) {
                sql.close()
            }
        }
        return [aaData: results, recordsTotal: totalCount, recordsFiltered: filteredCount]
    }

    private String getValidationSummaryWhereClause(String searchString) {
        String whereClause = ''
        whereClause += "UPPER(VALIDATION_TYPE) LIKE '%${searchString}%' OR UPPER(SOURCE_TABLE) LIKE '%${searchString}%' OR " +
                "UPPER(TARGET_TABLE) LIKE '%${searchString}%' OR UPPER(SRC_COLUMN_NAME) LIKE '%${searchString}%' OR " +
                "UPPER(TGT_COLUMN_NAME) LIKE '%${searchString}%' OR UPPER(STATUS) LIKE '%${searchString}%' OR " +
                "UPPER(SOURCE_COUNT) LIKE '%${searchString}%' OR UPPER(TARGET_COUNT) LIKE '%${searchString}%' OR " +
                "UPPER(ELAPSED_MINUTES) LIKE '%${searchString}%'"
        return whereClause
    }

    def getValidationLogList(def params, String tableName) {
        def sql = new Sql(dataSource_pva)
        def results
        Integer totalCount = 0
        Integer filteredCount = 0
        String whereClause = ''
        try {
            String totalQuery = "SELECT COUNT(1) as count from "+tableName
            if (params.searchString && params.searchString.toString().trim()!='') {
                whereClause = " WHERE " + getValidationLogWhereClause(params.searchString.toString().trim().toUpperCase())
            }
            String filteredQuery = "SELECT COUNT(1) as count from "+tableName + whereClause
            results = sql.rows("SELECT * from "+tableName+ whereClause+" order by ${params.sort} ${params.direction} OFFSET ${params.offset} ROWS FETCH NEXT ${params.max} ROWS ONLY")
            totalCount = sql.firstRow(totalQuery)["count"]
            filteredCount = sql.firstRow(filteredQuery)["count"]
        } catch (Exception ex) {
            log.error("Unknown exception occurred fetching validation log list getValidationLogList()" + ex.getMessage())
            ex.printStackTrace()
            return [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        } finally {
            if (sql) {
                sql.close()
            }
        }
        return [aaData: results, recordsTotal: totalCount, recordsFiltered: filteredCount]
    }

    private String getValidationLogWhereClause(String searchString) {
        String whereClause = ''
        whereClause += "UPPER(VALIDATION_TYPE) LIKE '%${searchString}%' OR UPPER(SOURCE_TABLE) LIKE '%${searchString}%' OR " +
                "UPPER(TARGET_TABLE) LIKE '%${searchString}%' OR UPPER(SRC_COLUMN_NAME) LIKE '%${searchString}%' OR " +
                "UPPER(TGT_COLUMN_NAME) LIKE '%${searchString}%' OR UPPER(SOURCE_VALUE) LIKE '%${searchString}%' OR " +
                "UPPER(TARGET_VALUE) LIKE '%${searchString}%' OR UPPER(IMPACTED_PK) LIKE '%${searchString}%' OR " +
                "UPPER(CASE_ID) LIKE '%${searchString}%' OR UPPER(CASE_NUMBER) LIKE '%${searchString}%' OR " +
                "UPPER(LAST_UPDATE_TIME) LIKE '%${searchString}%'"
        return whereClause
    }

    byte[] exportToExcel(metadata) {
        XSSFWorkbook workbook = new XSSFWorkbook()
        metadata.each {
            workbook = qualityService.writeDataToWorkbook(it.data, it, workbook, "BQMQ")
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    Map<String, List> getBQMQData(SourceProfile sourceProfile) {
        def sql = new Sql(dataSource_pva)
        Map<String, List> bqMqData = [:]
        List results = []
        try {
            String tableName = sourceProfile?.isCentral ? "DATAVAL_EXEC_STATUS_VW" : "DATAVAL_EXEC_STATUS_VW_AFF"
            sql.rows("SELECT * from "+ tableName).collect {
                results.add([it.VALIDATION_KEY, it.VALIDATION_VALUE])
            }
            bqMqData.put("bqMqExStatusData", results)
            results = []
            tableName = sourceProfile?.isCentral ? "DATAVAL_EXEC_SUMMARY_VW" : "DATAVAL_EXEC_SUMMARY_VW_AFF"
            sql.rows("SELECT * from "+tableName).collect {
                results.add([it.VALIDATION_TYPE, it.SOURCE_TABLE, it.TARGET_TABLE, it.SRC_COLUMN_NAME, it.TGT_COLUMN_NAME, it.SOURCE_COUNT, it.TARGET_COUNT, it.ELAPSED_MINUTES, it.STATUS])
            }
            bqMqData.put("bqMqSummaryData", results)
            results = []
            tableName = sourceProfile?.isCentral ? "DATAVAL_EXEC_LOG_VW" : "DATAVAL_EXEC_LOG_VW_AFF"
            sql.rows("SELECT * from "+tableName).collect {
                results.add([it.VALIDATION_TYPE, it.SOURCE_TABLE, it.TARGET_TABLE, it.SRC_COLUMN_NAME, it.TGT_COLUMN_NAME, it.SOURCE_VALUE, it.TARGET_VALUE, it.IMPACTED_PK, it.CASE_ID, it.CASE_NUMBER, it.LAST_UPDATE_TIME])
            }
            bqMqData.put("bqMqLogData", results)
        } catch (Exception ex) {
            log.error("Unknown exception occurred fetching result from table, getBQMQResultBasedOnTable()" + ex.getMessage())
            ex.printStackTrace()
        } finally {
            if (sql) {
                sql.close()
            }
        }
        return bqMqData
    }

    List createMetadataListForBQMQ(Map<String, List> bqMqData) {
        List metadataList = []
        metadataList.add([sheetName: "BQ-MQ Criteria",
                          columns  : [
                                  [title: ViewHelper.getMessage("app.validation.key.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.validation.value.label"), width: 80]
                          ],
                          data: bqMqData['bqMqExStatusData']
        ])
        metadataList.add([sheetName: "Validation Summary",
                          columns  : [
                                  [title: ViewHelper.getMessage("app.validation.type.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.src.table.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.tgt.table.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.src.column.name.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.tgt.column.name.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.src.count.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.tgt.count.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.elapsed.minutes.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.label.status"), width: 25]
                          ],
                          data: bqMqData['bqMqSummaryData']])
        metadataList.add([sheetName: "Validation Log",
                          columns  : [
                                  [title: ViewHelper.getMessage("app.validation.type.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.src.table.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.tgt.table.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.src.column.name.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.tgt.column.name.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.src.value.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.tgt.value.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.impacted.pk.label"), width: 80],
                                  [title: ViewHelper.getMessage("app.case.id.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.caseNumber.label"), width: 25],
                                  [title: ViewHelper.getMessage("app.last.update.time.label"), width: 25]
                          ],
                          data: bqMqData['bqMqLogData']])
        return metadataList
    }

    String getRecurrenceForBQMQ(String recurrencePattern) throws Exception {
        if (recurrencePattern && !MiscUtil.validateRecurrence(recurrencePattern)) {
            throw new GrailsRuntimeException("### RepeatInterval isn't a valid recurrence pattern ###")
        }
//        As DB team handling for the Run Once using second attribute value should be shared as null
        if(recurrencePattern.contains("FREQ=RUN_ONCE")) {
            recurrencePattern = null
        }
        return recurrencePattern
    }

    String getTimezone(timezone) {
        if(timezone) {
            return "name : ${timezone},offset : ${DateUtil.getOffsetString(timezone)}"
        }
        return null
    }

    /**
     * Method to schedule the bmQuery schedule.
     * @param schedule
     * @return
     */
    def enable() {
        Sql sql =  new Sql(dataSource_pva)
        try {
            User currentUser = (User) userService.getUser()
            String cleaningUpGttTable = ("Begin execute immediate ('Truncate table GTT_REPORT_INPUT_PARAMS'); END; ")
            String insertQuery = "INSERT INTO GTT_REPORT_INPUT_PARAMS(PARAM_KEY,PARAM_VALUE) VALUES('USERNAME','${currentUser.username}')"
            sql.execute(cleaningUpGttTable)
            sql.execute(insertQuery)
            sql.call("{call PKG_BQMQ_JOB.P_ENABLE}")
        } catch(Exception ex){
            log.error(ex.message)
            throw ex;
        }finally{
            sql?.close()
        }
    }

    /**
     * Method to disable the bmQuery schedule.
     * @param schedule
     * @return
     */
    def disable() {
        Sql sql =  new Sql(dataSource_pva)
        try{
            User currentUser = (User) userService.getUser()
            String cleaningUpGttTable = ("Begin execute immediate ('Truncate table GTT_REPORT_INPUT_PARAMS'); END; ")
            String insertQuery = "INSERT INTO GTT_REPORT_INPUT_PARAMS(PARAM_KEY,PARAM_VALUE) VALUES('USERNAME','${currentUser.username}')"
            sql.execute(cleaningUpGttTable)
            sql.execute(insertQuery)
            sql.call("{call PKG_BQMQ_JOB.P_DISABLE}")
        } catch(Exception ex) {
            log.error(ex.message)
            throw ex
        } finally {
            sql?.close()
        }
    }

    Integer getBmQueryStatus() {
        Sql sql = new Sql(dataSource_pva)
        Integer result = 0
        try {
            String query = "SELECT VALIDATION_VALUE FROM DATAVAL_CONSTANTS WHERE VALIDATION_KEY='VALIDATION_STATUS'"
            result = Integer.parseInt(sql.firstRow(query)["VALIDATION_VALUE"])
        } catch (Exception ex) {
            log.error("Unknown exception occurred fetching validation summary list getValidationSummaryList()" + ex.getMessage())
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        return result
    }

    private Connection getReportConnection() {
        return utilService.getReportConnection()
    }

}
