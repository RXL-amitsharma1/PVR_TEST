package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.ReportMeasures
import com.rxlogix.dynamicReports.reportTypes.TemplateSetCsvDataSource
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.util.FileUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.StreamingJsonBuilder
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringEscapeUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject
import org.hibernate.StatelessSession
import org.hibernate.Transaction
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl

import java.nio.charset.StandardCharsets
import java.sql.Clob
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.util.zip.GZIPOutputStream
import java.sql.Timestamp

@Transactional
class ReportResultService {

    public static Pattern urlPattern = Pattern.compile(Holders.config.getRequiredProperty('url.field.regex'),Pattern.CASE_INSENSITIVE)
    static String CIOMS_I_CASE_NUMBER_FIELD_NAME = "MFR_CONTROL_NO_24B"
    private static final String DEFAULT_TEMPLATE_SET_DATA_DIR = "report"


    def sqlGenerationService
    def grailsApplication
    def customMessageService
    def templateService
    def qualityService
    def sessionFactory
    def utilService
    def CRUDService

    List processTemplate(BaseTemplateQuery templateQuery, boolean hasQuery, Sql sql, Locale locale,boolean manual = false, boolean generatingIcsr = false, boolean viewSql = false) {
        List result = []
        if (viewSql) return result
        int sidewiseMatrixCount = 0
        int hasQueryCheck = 1
        int privacyProtected = templateQuery.privacyProtected ? 1 : 0
        int blindProtected = templateQuery.blindProtected ? 1 : 0
        Boolean isCiomsITemplate = (templateQuery.usedTemplate?.isCiomsITemplate())
        Boolean isMedWatchTemplate = (templateQuery.usedTemplate?.isMedWatchTemplate())
        Map map = [':BLINDED_CIOMS_CHECKBOX_VALUE:': blindProtected as String, ':PRIVACY_CIOMS_CHECKBOX_VALUE:': privacyProtected as String]

        if (!hasQuery) {
            hasQueryCheck = 0
        }

        if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.NON_CASE) {
            result.add(sqlGenerationService.generateNonCaseReportSQL(templateQuery))
        } else if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            // pkg_cioms_i.p_main (pi_has_query, pi_privacy, pi_blinded)
            if (isCiomsITemplate) {
                sql.execute("begin pkg_cioms_i.p_main(?,?,?); end;", [hasQueryCheck, privacyProtected, blindProtected])
            } else if(isMedWatchTemplate) {
                sql.execute("begin pkg_case_mw_form.p_main(?,?,?); end;", [hasQueryCheck, privacyProtected, blindProtected])
            }

            String customReportSQl = sqlGenerationService.generateCustomReportSQL(templateQuery, hasQuery)
            if (isCiomsITemplate || isMedWatchTemplate) {
                map.each {
                    customReportSQl = customReportSQl.replace("'${it.key}'", it.value)
                }
                customReportSQl
            }
            result.add(customReportSQl)
        } else if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            sql.execute("begin pkg_create_report_sql.p_main_tabulation; end;")

            String sql2 = sqlGenerationService.generateMatrixSQL(templateQuery, locale)[0]
            log.debug("pre data sql: " + sql2)
            // execute sql2 against pva_app db
            sql.call(sql2, [Sql.CLOB]) {
                result.add(it.characterStream.text)
            }

            String headerSql = sqlGenerationService.generateMatrixSQL(templateQuery, locale)[1]
            log.debug("pre header sql: " + headerSql)
            // execute headerSql against pva_app db
            sql.eachRow(headerSql) {
                result.add(it.toRowResult()[0].characterStream.text)
            }

            result.add(sql2 + "\n ** \n " + headerSql) // for debug purpose


        } else {
            if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
                String reportSql
                sql.call("{? = call pkg_create_report_sql.p_main()}", [Sql.VARCHAR]) { String sqlValue ->
                    reportSql = sqlValue
                }
                result.add(reportSql)
            } else if (templateQuery.usedTemplate instanceof ITemplateSet) {
                ITemplateSet templateSet = (ITemplateSet) templateQuery.usedTemplate
                BaseConfiguration configuration = templateQuery.usedConfiguration
                List sqlList = []
                int templateSetId = 0
                templateSet.nestedTemplates.eachWithIndex { ReportTemplate template, int templateIndex ->
                    template = GrailsHibernateUtil.unwrapIfProxy(template)
                    String reportSql
                    templateSetId = templateSetId + 1
                    String initialParamsInsert = sqlGenerationService.initializeReportGtts(templateQuery, template, hasQuery, locale, templateIndex, templateSetId, 0, false, manual, viewSql)
                    sql.execute(initialParamsInsert)
                    if(generatingIcsr) {
                        String queryLevel = QueryLevelEnum.CASE
                        if (configuration.deviceReportable) {
                            queryLevel = QueryLevelEnum.PRODUCT
                        }
                        sql.execute("DELETE FROM GTT_REPORT_INPUT_PARAMS WHERE PARAM_KEY = 'QUERY_LEVEL'")
                        sql.execute("INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY, PARAM_VALUE) VALUES (?, ?)", ['QUERY_LEVEL', queryLevel])
                    }

                    String icsrQueryInsert = sqlGenerationService.initializeForICSRPadder(configuration, locale)
                    if (icsrQueryInsert) {
                        sql.execute(icsrQueryInsert)
                        sql.call("{call pkg_create_version_sql.p_get_icsr_agency_name}")
                        // TODO call package PKG_NAME_ICSR_REPLACE
                    }
                    if (template.templateType == TemplateTypeEnum.CASE_LINE) {
                        // Handling of additionalWhere clause in case of TemplateSet with CLL
                        String cleaningUpGttTable = ("begin execute immediate ' begin pkg_pvr_app_util.p_truncate_table(''GTT_QUERY_DETAILS''); end;';" +
                                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_QUERY_SETS''); end;'; end;")
                        sql.execute(cleaningUpGttTable)
                        String additionalWhereClause = sqlGenerationService.getTemplateAdditionalQuery(template as CaseLineListingTemplate, locale)
                        if (additionalWhereClause) {
                            sql.execute("Begin ${additionalWhereClause} END;".toString())
                        }
                        sql.call("{? = call pkg_create_report_sql.p_main()}", [Sql.VARCHAR]) { String sqlValue ->
                            reportSql = sqlValue
                        }
                        sqlList.add(reportSql)
                    } else if (template.templateType == TemplateTypeEnum.NON_CASE) {
                        sqlList.add(template.nonCaseSql)
                    } else if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
                        BaseTemplateQuery baseTemplateQuery = new BaseTemplateQuery() {
                            @Override
                            Date getStartDate() {
                                return templateQuery.startDate
                            }
                            @Override
                            Date getEndDate() {
                                return templateQuery.endDate
                            }
                            @Override
                            BaseConfiguration getUsedConfiguration() {
                                return templateQuery.usedConfiguration
                            }
                            @Override
                            ReportTemplate getUsedTemplate() {
                                return template
                            }
                            @Override
                            SuperQuery getUsedQuery() {
                                return templateQuery.usedQuery
                            }
                            @Override
                            List<TemplateValueList> getUsedTemplateValueLists() {
                                return templateQuery.usedTemplateValueLists
                            }
                            @Override
                            List<QueryValueList> getUsesQueryValueLists() {
                                return templateQuery.usesQueryValueLists
                            }
                            @Override
                            BaseDateRangeInformation getUsedDateRangeInformationForTemplateQuery() {
                                return templateQuery.usedDateRangeInformationForTemplateQuery
                            }
                            @Override
                            Map<String, String> getPOIInputsKeysValues() {
                                return templateQuery.getPOIInputsKeysValues()
                            }
                        }

                        boolean hasQueryCurrent = true

                        if (templateQuery.usedQuery || configuration.productSelection || configuration.studySelection
                                || (configuration.usedEventSelection) || configuration.excludeNonValidCases
                                || configuration.suspectProduct || configuration.excludeDeletedCases) {
                            hasQueryCurrent = true
                        } else {
                            hasQueryCurrent = false
                        }

                        int hasQueryCheckCurrent = 1
                        boolean isciomsITemplateCurrent = template.isCiomsITemplate()
                        boolean isMedWatchTemplateCurrent = template.isMedWatchTemplate()

                        if (!hasQueryCurrent) {
                            hasQueryCheckCurrent = 0
                        }

                        // pkg_cioms_i.p_main (pi_has_query, pi_privacy, pi_blinded)
                        if (isciomsITemplateCurrent) {
                            sql.execute("begin pkg_cioms_i.p_main(?,?,?); end;", [hasQueryCheckCurrent, privacyProtected, blindProtected])
                        } else if(isMedWatchTemplateCurrent) {
                            sql.execute("begin pkg_case_mw_form.p_main(?,?,?); end;", [hasQueryCheckCurrent, privacyProtected, blindProtected])
                        }

                        String customReportSQl = sqlGenerationService.generateCustomReportSQL(baseTemplateQuery, hasQueryCurrent)
                        if (isciomsITemplateCurrent || isMedWatchTemplateCurrent) {
                            map.each {
                                customReportSQl = customReportSQl.replace("'${it.key}'", it.value)
                            }
                            customReportSQl
                        }
                        sqlList.add(customReportSQl)
                    } else if (template.templateType == TemplateTypeEnum.DATA_TAB) {
                        List dataTabSqlList = []
                        //sql.execute("begin execute immediate('truncate table gtt_tabulation'); end;")
                        sql.execute("begin pkg_create_report_sql.p_main_tabulation; end;");
                        BaseTemplateQuery baseTemplateQuery = new BaseTemplateQuery() {
                            @Override
                            Date getStartDate() {
                                return templateQuery.startDate
                            }

                            @Override
                            Date getEndDate() {
                                return templateQuery.endDate
                            }

                            @Override
                            BaseConfiguration getUsedConfiguration() {
                                return templateQuery.usedConfiguration
                            }

                            @Override
                            ReportTemplate getUsedTemplate() {
                                return template
                            }

                            @Override
                            SuperQuery getUsedQuery() {
                                return templateQuery.usedQuery
                            }

                            @Override
                            List<TemplateValueList> getUsedTemplateValueLists() {
                                return templateQuery.usedTemplateValueLists
                            }

                            @Override
                            List<QueryValueList> getUsesQueryValueLists() {
                                return templateQuery.usesQueryValueLists
                            }

                            @Override
                            BaseDateRangeInformation getUsedDateRangeInformationForTemplateQuery() {
                                return templateQuery.usedDateRangeInformationForTemplateQuery
                            }

                            @Override
                            Map<String, String> getPOIInputsKeysValues() {
                                return templateQuery.getPOIInputsKeysValues()
                            }
                        }
                        List<String> sqls = sqlGenerationService.generateMatrixSQL(baseTemplateQuery, locale)
                        String sql2 = sqls[0]
                        log.debug("pre data sql: " + sql2)
                        // execute sql2 against pva_app db
                        sql.call(sql2, [Sql.CLOB]) {
                            dataTabSqlList.add(it.characterStream.text)
                        }
                        String headerSql = sqls[1]
                        log.debug("pre header sql: " + headerSql)
                        // execute headerSql against pva_app db
                        sql.eachRow(headerSql) {
                            dataTabSqlList.add(it.toRowResult()[0].characterStream.text)
                        }
                        sqlList.add(dataTabSqlList)
                    }
                }
                result.add(sqlList)
            }
        }
        return result
    }

    File processOutputCiomsResult(def reportSql, Sql sql, BaseConfiguration configuration, ExecutedTemplateQuery executedTemplateQuery, ReportMeasures reportMeasures) {
        def formatter = new BasicFormatterImpl()
        GZIPOutputStream zipStream = null
        File tempFile = null
        try {
            tempFile = File.createTempFile(MiscUtil.generateRandomName(), ".csv.gzip", new File(grailsApplication.config.tempDirectory as String))
            log.info("Temp directory: ${grailsApplication.config.tempDirectory} file: {$tempFile.name}")
            zipStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
            Writer writer = new OutputStreamWriter(zipStream, StandardCharsets.UTF_8)

            log.debug("ReportSQL = ${formatter.format(reportSql)}")
            log.info("Running executeSingleReportSQLCSV() for Configuration: (ID:  ${configuration.id})")

            try {
                executeSingleReportSQLCSV(reportSql, sql, writer, reportMeasures, executedTemplateQuery, false, null)
            } finally {
                writer?.flush()
                writer?.close()
            }

        } finally {
            zipStream?.close()
        }
        return tempFile
    }

    File processOutputDataTab(def reportSql, String headerSql, boolean bVoidedFlag, ReportMeasures reportMeasures, ExecutedTemplateQuery executedTemplateQuery, Sql sql, Map headers) {
        def formatter = new BasicFormatterImpl()
        //==========================================================================================================
        //DATA TABULATION ONLY
        if (headerSql) {
            headers[executedTemplateQuery.usedTemplate.id] = createHeadersListDataTabulation(sql, headerSql, executedTemplateQuery.usedTemplate)
        }
        //==========================================================================================================

        List fieldNameWithIndex = []
        if (executedTemplateQuery.usedTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
            CaseLineListingTemplate template = (CaseLineListingTemplate) executedTemplateQuery.usedTemplate
            fieldNameWithIndex = template.getFieldNameWithIndex()
        }

        File tempFile = File.createTempFile(MiscUtil.generateRandomName(), ".json.gzip", new File(grailsApplication.config.tempDirectory as String))
        GZIPOutputStream zipStream = null
        def writer = null

        try {
            zipStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
            writer = new BufferedWriter(new OutputStreamWriter(zipStream, StandardCharsets.UTF_8))
            def jsonBuilder = new StreamingJsonBuilder(writer)

            if (reportSql instanceof String) {
                log.debug("ReportSQL = ${formatter.format(reportSql)}")
                log.info("Running executeSingleReportSQL() for executedTemplateQuery: (ID:  ${executedTemplateQuery.id})")
                executeSingleReportSQL(reportSql, sql, writer, jsonBuilder, executedTemplateQuery.usedTemplate.templateType, fieldNameWithIndex, reportMeasures)
            } else {
                //======================================================================================================
                // FOR TEMPLATE SET ONLY
                //======================================================================================================
                log.error("Template set should only be run as CSV.")
            }

        } finally {
            writer?.flush()
            writer?.close()
            zipStream?.close()
        }
        return tempFile
    }


    File processOutputResult(def reportSql, Sql sql, boolean bVoidedFlag, BaseConfiguration configuration, ExecutedTemplateQuery executedTemplateQuery, boolean isDrilldownCll, ReportResult result, Map headers, ReportMeasures reportMeasures) {
        def formatter = new BasicFormatterImpl()
        File tempFile = null

        if (reportSql instanceof String) {
            GZIPOutputStream zipStream = null

            try {
                tempFile = File.createTempFile(MiscUtil.generateRandomName(), ".csv.gzip", new File(grailsApplication.config.tempDirectory as String))
                log.info("Temp directory: ${grailsApplication.config.tempDirectory} file: {$tempFile.name}")
                zipStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))
                Writer writer = new OutputStreamWriter(zipStream, StandardCharsets.UTF_8)
                log.debug("ReportSQL = ${formatter.format(reportSql)}")
                log.info("Running executeSingleReportSQLCSV() for Configuration: (ID:  ${configuration.id})")

                try {
                    executeSingleReportSQLCSV(reportSql, sql, writer, reportMeasures, executedTemplateQuery, isDrilldownCll, result)
                } finally {
                    writer?.flush()
                    writer?.close()
                }

            } finally {
                zipStream?.close()
            }

        } else {
            //======================================================================================================
            // FOR TEMPLATE SET ONLY
            //======================================================================================================

            // Template set stores a .tar.gz's data as a byte[]. Each report result is stored in its own GZIP'd file.
            File directoryToArchive = new File("${grailsApplication.config.tempDirectory as String}${MiscUtil.generateRandomName()}")
            if (directoryToArchive.exists()) {
                FileUtils.deleteDirectory(directoryToArchive)
            }
            directoryToArchive.mkdir()

            ITemplateSet executedTemplateSet = (ITemplateSet) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedTemplate)

            // Subreport CSV datasources

            //Added this if block, as below it is expected that reportSql will be a map
            if (reportSql instanceof List) {
                Map temp = [:]
                /*
                    Encountered problem while using each closure,
                    Ljava/lang/Object;Ljava/lang/Object;Lgroovy/lang/Reference;Lgroovy/lang/Reference;)V,
                    so used for loop
                 */
                for (Integer i = 0; i < reportSql.size(); i++) {
                    temp.put(i, reportSql[i])
                }
                reportSql = temp
            }
            Map folderNameMap = [:]
            Integer[] folderNameCounter = [0] //Using array so that it is passed by reference instead of by value and gets updated from inside the function as well
            reportSql.each { key, value ->
                // getExecutedCLLTemplates
                ReportTemplate template = executedTemplateSet.nestedTemplates?.getAt(key)
                String reportQuery
                String headerQuery
                if (value instanceof List) {
                    reportQuery = value[0]
                    headerQuery = value[1]
                } else {
                    reportQuery = value
                }
                if (headerQuery) {
                    List headersList = createHeadersListDataTabulation(sql, headerQuery, template)
                    headers[template.id] = headersList
                }
                executeTemplateSetReportSQLCSV(reportQuery, sql, directoryToArchive, reportMeasures, template,
                        executedTemplateSet.linkSectionsByGrouping, executedTemplateSet, folderNameMap, folderNameCounter)
            }
            directoryToArchive.eachDir {
                File caseTarFile = new File(directoryToArchive, "${it.name}.tar.gz")
                FileUtil.compressFiles(it.listFiles().toList(), caseTarFile)
                FileUtils.deleteDirectory(it)
            }
            // Compress the entire directory of all the executed templates into one archive
            String directory = "${grailsApplication.config.tempDirectory as String}${MiscUtil.generateRandomName()}"
            tempFile = new File("${directory}.tar.gz")

            FileUtil.compressFiles(directoryToArchive.listFiles().toList().sort(), tempFile)
            FileUtils.cleanDirectory(directoryToArchive)
            directoryToArchive.delete()
        }
        return tempFile
    }


    private void executeSingleReportSQLCSV(String reportSql, Sql sql, Writer writer, ReportMeasures reportMeasures, ExecutedTemplateQuery executedTemplateQuery, boolean isDrilldownCll = false, ReportResult result = null) {
        boolean initColumnNamesList = false
        List<String> columnNamesList = []
        Set<String> caseNumbers
        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.CSV_JASPER)
        ReportTemplate executedTemplate = executedTemplateQuery.executedTemplate
        sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
        sql.withStatement { stmt ->
            stmt.fetchSize = grailsApplication.config.jdbcProperties.fetch_size ?: 50
        }
        Sql pvrsql
        //Check for PV Quality tags
        List<String> qualityTypes = qualityService.getTypes(executedTemplateQuery)
        WorkflowState defaultWorkState = new WorkflowState()
        if (executedTemplateQuery.executedTemplate.name == Holders.config.getProperty('pvcModule.late_processing_template') || executedTemplateQuery.executedTemplate.name == Holders.config.getProperty('pvcModule.inbound_processing_template')) {
            defaultWorkState = WorkflowState.defaultWorkState
        }
        sql.query(reportSql) { ResultSet resultSet ->
            try {
                // Create column name list for NonCaseSQLTemplate or CustomSQLTemplate if it has params
                if (executedTemplate.hasBlanks && !initColumnNamesList) {

                    if (executedTemplate.templateType == TemplateTypeEnum.NON_CASE
                            || executedTemplate.templateType == TemplateTypeEnum.CUSTOM_SQL) {
                        ResultSetMetaData rsmd = resultSet.getMetaData()

                        for (int i = 1; i <= rsmd.columnCount; i++) {
                            String currentCol = "\"${rsmd.getColumnLabel(i)}\""
                            columnNamesList.add(currentCol)
                        }

                        // Parsed later as a JSONArray
                        executedTemplate.columnNamesList = "[${columnNamesList.join(",")}]"
                        CRUDService.updateWithoutAuditLog(executedTemplate)
                        initColumnNamesList = true
                    }
                }

                int caseNumberColumnIndex = executedTemplate.allSelectedFieldsInfo.findIndexOf {
                    return it.reportField.name in SourceProfile.fetchAllCaseNumberFieldNames()
                }


                //=======Check for PV Quality
                String queryName = executedTemplateQuery?.executedQuery?.name
                Map<String, List> qualityFieldsMap = [:]
                Map<String,String> rcaData = [:]

                Long reportId = null
                Long executedReportId = null
                Long tenantId = null
                Long executedTemplateId = null
                pvrsql = new Sql(utilService.getReportConnectionForPVR())
                if (qualityTypes) {
                    ReportConfiguration reportConfiguration = ReportConfiguration.findByReportNameAndExecuting(executedTemplateQuery.usedConfiguration.reportName, true)
                    reportId = reportConfiguration.id
                    executedReportId = executedTemplateQuery.executedConfiguration.id
                    executedTemplateId = executedTemplateQuery.executedTemplate.id
                    tenantId = executedTemplateQuery.executedConfiguration.tenantId
                    ResultSetMetaData rsmd = resultSet.getMetaData()
                    rcaData.priority = executedTemplateQuery.priority
                    rcaData.rootCause = executedTemplateQuery.rootCause
                    rcaData.responsibleParty = executedTemplateQuery.responsibleParty
                    rcaData.issueType = executedTemplateQuery.issueType
                    rcaData.assignedToUserGroup = executedTemplateQuery.assignedToGroupId
                    rcaData.assignedToUser = executedTemplateQuery.assignedToUserId
                    rcaData.summarySql = executedTemplateQuery.summarySql
                    rcaData.actionsSql = executedTemplateQuery.actionsSql
                    rcaData.investigationSql = executedTemplateQuery.investigationSql
                    rcaData.actions = executedTemplateQuery.actions
                    rcaData.summary = executedTemplateQuery.summary
                    rcaData.investigation = executedTemplateQuery.investigation

                    List<String> reportFields = []
                    List<String> renameValues = []
                    List<Boolean> selectableValues = []
                    if ((executedTemplate.templateType == TemplateTypeEnum.CASE_LINE)) {
                        executedTemplate.allSelectedFieldsInfo.collect {
                            String reportFieldName = it.reportField.name
                            reportFields.add(reportFieldName)
                            renameValues.add(it.renameValue)
                            selectableValues.add(it.customExpression ? false : true)
                        }
                    } else {
                        for (int i = 1; i <= rsmd.columnCount; i++) {
                            reportFields.add(rsmd.getColumnName(i))
                        }
                    }

                    qualityTypes.each {
                        qualityFieldsMap.put(it, qualityService.getPVQFields(reportId, executedReportId, reportFields, it, rsmd, renameValues, selectableValues))
                    }
                }

                //==============================
                String caseNum;
                List<String> drilldownReportFields = []
                List<String> urlFields = []
                boolean storeReportResult = false
                if ((executedTemplate.templateType == TemplateTypeEnum.CASE_LINE) &&
                        (executedTemplateQuery.executedTemplate.name in [Holders.config.pvcModule.late_processing_template, Holders.config.pvcModule.inbound_processing_template])) {
                    storeReportResult = true
                    drilldownReportFields = templateService.getDrilldownReportFields(executedTemplate)
                    urlFields = templateService.getUrlFields(executedTemplate)
                }
                StatelessSession session = sessionFactory.openStatelessSession()
                Transaction tx = session.beginTransaction();
                Long start = System.currentTimeMillis()
                while (resultSet.next()) {
                    Map<String, Map> qualityMetadataMap = [:]
                    Map<String, List<String>> reportFields = [:]
                    qualityTypes.each {
                        qualityMetadataMap[it] = [:]
                        reportFields[it] = []
                    }
                    caseNum = null;
                    Map dataJson = [:]
                    resultSet.toRowResult().eachWithIndex { it, i ->
                        String value = ""
                        String uniqueName

                        // Need an explicit check for null, since integer 0 is considered a null value
                        if (it.value != null) {
                            //Handle Clob data
                            if (it.value instanceof Clob) {
                                value = it.value.getSubString(1, (int) it.value.length())
                            } else if (it.value instanceof Date) {
                                value = dateFormat.format(it.value)
                            } else {
                                value = it.value
                            }
                            value = StringEscapeUtils.escapeCsv(value.trim())
                            //If PV Quality
                            qualityTypes.each {
                                uniqueName = templateService.addAsUniqueFieldName(reportFields[it], qualityFieldsMap[it][i])
                                qualityMetadataMap[it][uniqueName] = value
                            }

                            if (storeReportResult) {
                                if (urlFields.contains(drilldownReportFields.get(i)) && value) {
                                    value = value.replaceAll(urlPattern) { m -> "<a href='${m[0]}' target='_blank'>${m[0]}</a>" }
                                }
                                dataJson[drilldownReportFields.get(i)] = value ?: ""
                            }
                            if ((i == caseNumberColumnIndex && executedTemplate.templateType == TemplateTypeEnum.CASE_LINE) ||
                                    (executedTemplate.ciomsI && it.key == CIOMS_I_CASE_NUMBER_FIELD_NAME)) {
                                if (caseNumbers == null) {
                                    caseNumbers = new HashSet<>()
                                }
                                caseNum = value
                                caseNumbers.add(value)
                            }
                        } else {
                            qualityTypes.each {
                                uniqueName = templateService.addAsUniqueFieldName(reportFields[it], qualityFieldsMap[it][i])
                                qualityMetadataMap[it][uniqueName] = null
                            }
                            if (storeReportResult) {
                                dataJson[drilldownReportFields.get(i)] = ""
                            }
                        }

                        if (i > 0) {
                            writer.write(",")
                        }
                        writer.write(value)

                    }
                    if (storeReportResult) {
                        DrilldownCLLData drilldownCLLData = new DrilldownCLLData()
                        drilldownCLLData.executedReportId = executedTemplateQuery.executedConfiguration.id
                        drilldownCLLData.reportResultId = result ? result.id : executedTemplateQuery.getReportResult().id
                        drilldownCLLData.cllRowData = (dataJson as JSON)
                        session.insert(drilldownCLLData)

                        if (executedTemplateQuery.executedTemplate.name == Holders.config.getProperty('pvcModule.late_processing_template') || executedTemplateQuery.executedTemplate.name == Holders.config.getProperty('pvcModule.inbound_processing_template')) {
                            Long masterCaseId = 0L
                            Long masterVersionNum = 0L
                            String processedReportId = null
                            Long masterEnterpriseId = 0L
                            Long senderId = 0L
                            try {
                                def drilldownMetadata
                                Integer res = 0
                                if (executedTemplateQuery.executedTemplate.name == Holders.config.getProperty('pvcModule.inbound_processing_template')) {
                                    drilldownMetadata = new InboundDrilldownMetadata()
                                    masterCaseId = Long.valueOf(dataJson['masterCaseId'])
                                    masterVersionNum = Long.valueOf(dataJson['masterVersionNum'])
                                    senderId = Long.valueOf(dataJson['pvcIcSenderId'])
                                    masterEnterpriseId = Long.valueOf(dataJson['masterEnterpriseId'])
                                    drilldownMetadata.updateDueDate(null)
                                    Timestamp currentDrilldownDueDate = new Timestamp(drilldownMetadata?.dueDate.getTime())
                                    Long metadataId = pvrsql.firstRow("select IN_DRILLDOWN_METADATA_ID.NEXTVAL as nextValue from dual", [])["nextValue"]
                                    res = pvrsql.call("{?= call f_update_in_ddwn_assignment(?, ?, ?, ?, ?, ?, ?)}", [Sql.INTEGER, metadataId, masterCaseId, masterVersionNum, senderId, masterEnterpriseId, defaultWorkState.id, currentDrilldownDueDate.toString()])
                                }else {
                                    drilldownMetadata = new DrilldownCLLMetadata()
                                    masterCaseId = Long.valueOf(dataJson['masterCaseId'])
                                    processedReportId = dataJson['vcsProcessedReportId']
                                    masterEnterpriseId = Long.valueOf(dataJson['masterEnterpriseId'])
                                    drilldownMetadata.updateDueDate(null)
                                    Timestamp currentDrilldownDueDate = new Timestamp(drilldownMetadata?.dueDate.getTime())
                                    Long metadataId = pvrsql.firstRow("select DRILLDOWN_METADATA_ID.NEXTVAL as nextValue from dual", [])["nextValue"]
                                    res = pvrsql.call("{?= call f_update_drilldown_assignment(?, ?, ?, ?, ?, ?)}", [Sql.INTEGER, metadataId, masterCaseId, processedReportId, masterEnterpriseId, defaultWorkState.id, currentDrilldownDueDate.toString()])
                                }
                                if (res > 0) {
                                    WorkflowRule workFlowRule = WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY)
                                    boolean isBasicRule = (workFlowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.BASIC_RULE.name()) && workFlowRule?.assignedToUserGroup && (workFlowRule?.assignToUserGroup || workFlowRule?.autoAssignToUsers)
                                    boolean isAdvanceRule = workFlowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.ADVANCED_RULE.name()
                                    if (workFlowRule && (isBasicRule || isAdvanceRule)) {
                                        AutoAssignment autoAssignment = new AutoAssignment()
                                        autoAssignment.caseId = masterCaseId
                                        autoAssignment.processedReportId = processedReportId
                                        autoAssignment.tenantId = masterEnterpriseId
                                        autoAssignment.moduleName = WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY.getKey()
                                        autoAssignment.workflowRule = workFlowRule
                                        session.insert(autoAssignment)
                                    }
                                }
                            } catch (SQLIntegrityConstraintViolationException e) {
                                log.error("Getting error while inserting in drilldown metadata table for case Id : " + masterCaseId + ", case Version : " +masterVersionNum +", processed report id : " + processedReportId + ", tenant id : " + masterEnterpriseId + ",  sender id : " + senderId + ", Error Message : " + e.getMessage())
                            }
                        }
                    }
                    writer.write("\n")


                    if (caseNum) {
                        qualityTypes.each { type ->
                            if (qualityMetadataMap[type].size() > 0)
                                qualityService.qualityDataAddExtraSave(qualityMetadataMap[type], reportId,executedTemplateId, executedReportId, caseNum, queryName, rcaData, type, pvrsql,sql, tenantId)
                        }
                    }

                    reportMeasures.rowCount++
                }

                tx.commit();
                session.close();
            } finally {
                try {
                    resultSet?.close()
                    pvrsql?.close()
                } catch (e) {
                    log.error(e.message)
                }
            }
        }
        reportMeasures.caseCount = caseNumbers?.size()
    }


    private void executeTemplateSetReportSQLCSV(String reportSql, Sql sql, File directoryToArchive,
                                                ReportMeasures reportMeasures, ReportTemplate executedTemplate,
                                                boolean linkSectionsByGrouping, ITemplateSet rootTemplate, Map folderNameMap, Integer[] folderNameCounter) {
        boolean separateDataTabulation = (!linkSectionsByGrouping && (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) )
        File outputDir
        Set<String> caseNumbers
        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.CSV_JASPER)
        sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
        sql.withStatement { stmt ->
            stmt.fetchSize = grailsApplication.config.jdbcProperties.fetch_size ?: 50
        }
        sql.query(reportSql) { ResultSet resultSet ->
            def serviceColumnIndices
            if (executedTemplate instanceof CaseLineListingTemplate) {
                serviceColumnIndices = executedTemplate.serviceColumnList?.collect {
                    executedTemplate.allSelectedFieldsInfo.indexOf(it.reportFieldInfoList[0])
                }
            }
            int caseNumberColumnIndex = -1
            if (executedTemplate instanceof CaseLineListingTemplate) {
                caseNumberColumnIndex = executedTemplate.allSelectedFieldsInfo.findIndexOf {
                    return it.reportField.name in SourceProfile.fetchAllCaseNumberFieldNames()
                }
            }
            reportMeasures.caseCount = 0L

            try {
                int rowCount=0
                while (resultSet.next()) {
                    ByteArrayOutputStream baos = null
                    OutputStreamWriter writer = null
                    boolean isEmptyRow = true
                    def jsonBuilder
                    JSONObject groupingData
                    try {
                        baos = new ByteArrayOutputStream()
                        writer = new OutputStreamWriter(baos)
                        GroovyRowResult rowResult = resultSet.toRowResult()
                        if (separateDataTabulation) {
                            jsonBuilder = new StreamingJsonBuilder(writer)
                            if (rowCount == 0) writer.write("[")
                        }
                        Map rowMap = [:]
                        rowResult.eachWithIndex { it, i ->

                            if (!linkSectionsByGrouping && (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB)) {
                                def value
                                if (it.value instanceof Clob) {
                                    //Handle Clob data
                                    value = it.value.characterStream.text
                                } else {
                                    value = it.value
                                }
                                if (value instanceof String) value = StringEscapeUtils.escapeHtml(value?.trim())
                                value = fixPercentageValue(value,it.key)
                                rowMap.put(it.key, value)
                                isEmptyRow = false
                            } else {
                                String value = ""

                                // Need an explicit check for null, since integer 0 is considered a null value
                                if (it.value != null) {
                                    //Handle Clob data
                                    if (it.value instanceof Clob) {
                                        value = it.value.characterStream.text
                                    } else if (it.value instanceof Date) {
                                        value = dateFormat.format(it.value)
                                    } else {
                                        value = it.value
                                    }

                                    value = StringEscapeUtils.escapeCsv(value.trim())
                                }

                                if (i > 0) {
                                    writer.write(",")
                                }
                                if (i == caseNumberColumnIndex) {
                                    if (caseNumbers == null) {
                                        caseNumbers = new HashSet<>()
                                    }
                                    caseNumbers.add(value)
                                } else if (!serviceColumnIndices || (value && !serviceColumnIndices?.contains(i))) {
                                    isEmptyRow = false
                                }
                                writer.write(value)
                            }
                        }
                        if (separateDataTabulation) {
                            rowMap.put("ID", rowCount)
                            if (rowCount != 0L) writer.write(",")
                            jsonBuilder.call(rowMap)
                        } else {
                            groupingData = getGroupingData(executedTemplate, rowResult)
                        }
                    } finally {
                        writer?.write("\n")
                        writer?.flush()
                        writer?.close()
                        if (!isEmptyRow) {
                            outputDir = new File(directoryToArchive, DEFAULT_TEMPLATE_SET_DATA_DIR)
                            if (linkSectionsByGrouping && groupingData) {
                                if (rootTemplate instanceof ExecutedTemplateSet) {
                                    String valueHashCode = String.valueOf(groupingData.collect { k, v -> v }.join().hashCode())
                                    String folderName = folderNameMap.get(valueHashCode)
                                    if (!folderName) {
                                        folderNameCounter[0]++
                                        folderName = String.format("%06d", folderNameCounter[0]);
                                        folderNameMap.put(valueHashCode, folderName)
                                    }
                                    outputDir = new File(directoryToArchive, folderName)
                                } else {
                                    outputDir = new File(directoryToArchive, String.valueOf(groupingData.collect { k, v -> v }.join().hashCode()))
                                }
                                File groupingFile = new File(outputDir, TemplateSetCsvDataSource.GROUPING_FILE_NAME)
                                if (!groupingFile.exists()) {
                                    appendEntryToDir(outputDir, TemplateSetCsvDataSource.GROUPING_FILE_NAME, groupingData.toString().bytes)
                                }
                            }
                            appendEntryToDir(outputDir, "${executedTemplate.id}.csv", baos.toByteArray())
                        }
                    }
                    reportMeasures.rowCount++
                    rowCount++
                }
                if (separateDataTabulation && outputDir) appendEntryToDir(outputDir, "${executedTemplate.id}.csv", "]".getBytes())
            } finally {
                try {
                    resultSet?.close()
                } catch (e) {
                    log.error(e.message)
                }
            }
        }
        reportMeasures.caseCount = caseNumbers?.size()
    }

    private void executeSingleReportSQL(String reportSql, Sql sql, BufferedWriter writer, StreamingJsonBuilder jsonBuilder,
                                        TemplateTypeEnum templateType, List fieldNameWithIndex, ReportMeasures reportMeasures) {

        writer.write("[") // hack to wrap the JSON output in a list

        sql.eachRow(reportSql) { GroovyResultSet resultSet ->
            Map rowMap = [:]
            resultSet.toRowResult().eachWithIndex { it, i ->
                def value
                if (it.value instanceof Clob) {
                    //Handle Clob data
                    value = it.value.characterStream.text
                } else {
                    value = it.value
                }

                if (templateType == TemplateTypeEnum.CASE_LINE) {
                    rowMap.put(fieldNameWithIndex[i], value)
                } else {
                    value = fixPercentageValue(value,it.key)
                    rowMap.put(it.key, value)
                }
            }
            if (templateType == TemplateTypeEnum.DATA_TAB) {
                rowMap.put("ID", reportMeasures.rowCount)
            }
            if (reportMeasures.rowCount != 0L) {
                writer.write(",")
            }
            reportMeasures.rowCount++

            jsonBuilder.call(rowMap)
        }
        writer.write("]") // hack to wrap the JSON output in a list
    }

    private Object fixPercentageValue(Object value, String key) {
        def keySplit = key.toString().split("_")
        if (keySplit.size() > 2 && keySplit[2].trim()) {
            if (keySplit[2].contains(MeasureTypeEnum.COMPLIANCE_RATE.code)) {
                return (value == 999) ? Constants.NA : new DecimalFormat("0.00").format(value)
            } else if (keySplit[2].startsWith(Constants.PERCENTAGE_COLUMN)) {
                return (value == "999%" ? Constants.NA : formatNumber(value))
            }
        }
        return value;
    }

    String formatNumber(Object s) {
        String num = s.toString()
        if (num.startsWith(".")) num = "0" + s;
        if (num.endsWith("%")) num = num.substring(0, num.size() - 1)
        if (num.indexOf(".") != -1 && num.indexOf(".") == num.size() - 2) num = num + "0"
        if (num == "00") return "0"
        if (num.endsWith(".00")) return num.substring(0, num.size() - 3)
        return num
    }

    private List createHeadersListDataTabulation(Sql sql, String headerSql, executedTemplate) {
        log.info("Executing HeaderSQL")
        List headers = []
        List<String> percentageHeaders = ['Event Percentage', 'Case Percentage', 'Prod Event Percentage', 'Report Percentage']
        List<String> totalHeaders = ['Product Events', 'Cumulative Events', 'Cases', 'Events', 'Reports']
        List<DataTabulationColumnMeasure> dataTabulationMeasures = ((List<DataTabulationColumnMeasure>) ((DataTabulationTemplate) executedTemplate).columnMeasureList)
        Map tHeaderMap = ['Cases': 'Case Count', 'Events': 'Event Count', 'Cumulative Events': 'Cumulative-Event Count', 'Product Events': 'Prod Event Count', 'Reports': 'Report Count']
        sql.eachRow(headerSql) { GroovyResultSet resultSet ->
            resultSet.toRowResult().eachWithIndex { it, i ->
                if (it.value.toString() && it.key.toString().toLowerCase().startsWith('gp_') && executedTemplate instanceof DataTabulationTemplate) {
                    String number = it.key.replaceAll("[^-?0-9]+", " ").trim().split(" ").last()
                    //Keys will be of format: GP_x_CEyz, above number will comprise of yz. z is the index pertaining to the particular dataTabulation measure
                    Integer secondLastDigit = Integer.valueOf(number.substring(0, 1))
                    Integer lastDigit = Integer.valueOf(number.substring(1))
                    DataTabulationColumnMeasure columnMeasureList = dataTabulationMeasures.get(lastDigit - 1)
                    List<DataTabulationMeasure> columnMeasures = columnMeasureList.measures
                    for (DataTabulationMeasure measures : columnMeasures) {
                        String measureTypeI18 = customMessageService.getMessage(measures.type.getI18nKey())
                        if (measureTypeI18 == "Product-Event Count") {
                            measureTypeI18 = "Prod Event Count"
                        } else if (measureTypeI18 == "Product-Event Percentage") {
                            measureTypeI18 = "Prod Event Percentage"
                        } else if (measureTypeI18 == "Compliance Rate") {
                            measureTypeI18 = "Compliance Rate Percentage"
                        } else if (measureTypeI18 == "Version Count") {
                            measureTypeI18 = "Case Version Count"
                        }

                        String pHeaderName = percentageHeaders.find { pheader -> it.value.toString().contains(pheader) }
                        String tHeaderName = totalHeaders.find { theader -> it.value.toString().equalsIgnoreCase(theader) }
                        String combineTotalHeaderName = totalHeaders.find { ttheader -> it.value.toString().contains(ttheader) }

                        if (pHeaderName && (pHeaderName == measureTypeI18.replace('Count', 'Percentage'))) {
                            measureTypeI18 = measureTypeI18.replace('Count', 'Percentage')
                        }

                        String measureDateRangeCount = (measures.dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) ? "Cumulative-${measureTypeI18}" : ((measures.dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) ? "Custom-${measureTypeI18}" : measureTypeI18)
                        if ((secondLastDigit == 4) && tHeaderName && measures.showTotal && tHeaderMap[it.value.toString()].equals(measureDateRangeCount)) {
                            it.value = it.value.toString().replaceAll(tHeaderName, "${measures.name}")
                            break;
                        }
                        if ((secondLastDigit == 4) && combineTotalHeaderName && measures.showTotal && it.value.toString().contains("(Total)") && (tHeaderMap[combineTotalHeaderName].equals(measureDateRangeCount))) {
                            it.value = it.value.toString().replaceAll(combineTotalHeaderName, "${measures.name}")
                            break;
                        }

                        if (pHeaderName && (it.value.toString().contains("(${measureDateRangeCount})") || it.value.toString().equals(measureDateRangeCount))) {
                            it.value = it.value.toString().replaceAll("Cumulative-${measureTypeI18}", "${measures.name} Percentage").replaceAll("Custom-${measureTypeI18}", "${measures.name} Percentage").replaceAll(measureTypeI18, "${measures.name} Percentage")
                            break;
                        }
                        if (!pHeaderName && (it.value.toString().contains("(${measureDateRangeCount})") || it.value.toString().equals(measureDateRangeCount))) {
                            //PVR-2789: Removing cummulative and Custom from header eg: Cumulative-Case Count would be Case count or replaced header value etc.
                            it.value = it.value.toString().replaceAll("Cumulative-", "").replaceAll("Custom-", "").replaceAll(measureTypeI18, measures.name)
                            break;
                        }
                    }
                } else if (it.key.toString().toLowerCase().startsWith("case_list")  && executedTemplate instanceof DataTabulationTemplate) {
                    Integer columnMeasureId = Integer.parseInt(it.key.toString().toLowerCase().replace("case_list", ""))
                    DataTabulationColumnMeasure columnMeasureList = dataTabulationMeasures.get(columnMeasureId - 1)
                    DataTabulationMeasure columnMeasure = columnMeasureList.measures.find {measure ->
                        measure.type == MeasureTypeEnum.CASE_LIST
                    }
                    String measureTypeI18 = customMessageService.getMessage(columnMeasure.type.getI18nKey())
                    it.value = it.value.toString().replaceAll(measureTypeI18, columnMeasure.name)
                }
                String headerLabel = it.value
                if (headers.size() == i) {
                    // get row/column names from report template
                    DataTabulationTemplate template = (DataTabulationTemplate) executedTemplate
                    List<String> names = templateService.getAllSelectedFieldNames(template)
                    if (names.size() > i) {
                        headerLabel = names[i] // rows label
                    } else {
                        headerLabel = "" // don't show column labels now
                    }
                    headers[i] = [(it.key): headerLabel]
                }
                headers.each { header ->
                    if (header.containsKey(it.key)) {
                        headerLabel = header[it.key] // stack column value labels
                        if (it.value) {
                            headerLabel += '\n' + it.value
                        }
                    }
                }
                headers[i] = [(it.key): headerLabel]
            }
        }
        return headers
    }

    private JSONObject getGroupingData(ReportTemplate executedTemplate, GroovyRowResult rowResult) {
        JSONObject groupingData
        if (executedTemplate instanceof CaseLineListingTemplate) {
            List<ReportFieldInfo> allColumns = executedTemplate.allSelectedFieldsInfo
            List<String> fieldNameWithIndex = executedTemplate?.getFieldNameWithIndex()
            ReportFieldInfoList groupingList = executedTemplate.groupingList
            if (groupingList) {
                groupingData = new JSONObject()
                groupingList?.reportFieldInfoList.each { ReportFieldInfo reportFieldInfo ->
                    String fieldName = reportFieldInfo.reportField.name
                    int index = allColumns.findIndexOf { fieldName.equals(it.reportField.name) }
                    def value = rowResult.getAt(index)
                    groupingData[fieldNameWithIndex[index]] = value
                }
            }
        } else if (executedTemplate instanceof CustomSQLTemplate) {
            groupingData =  new JSONObject()
            def value = rowResult.getProperty("Case Number")
            groupingData['Case Number'] = value
        }
        return groupingData
    }

    private void appendEntryToDir(File dir, String entryName, byte[] entryContent) throws IOException {
        dir.mkdir()
        File entryFile = new File(dir, entryName)
        entryFile.append(entryContent)
    }

}
