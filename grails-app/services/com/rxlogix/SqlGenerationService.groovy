package com.rxlogix

import com.rxlogix.commandObjects.CaseSubmissionCO
import com.rxlogix.config.*
import com.rxlogix.config.metadata.CaseColumnJoinMapping
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.dto.CaseAckSubmissionDTO
import com.rxlogix.dto.CaseStateUpdateDTO
import com.rxlogix.enums.*
import com.rxlogix.helper.LocaleHelper
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.NotTransactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.time.TimeCategory
import oracle.jdbc.OracleTypes
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject
import groovy.sql.GroovyResultSet

import java.sql.Blob
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.sql.Clob
import java.nio.file.Files
import static com.rxlogix.reportTemplate.MeasureTypeEnum.*

class SqlGenerationService {

    static transactional = false

    public static final String VERSION_TABLE_NAME = "gtt_versions"
    public static final String VERSION_BASE_TABLE_NAME = "gtt_versions_base"
    public static final String PVR_CASE_LIST_TABLE_NAME = "PVR_QUERY_CASE_LIST"
    public static final String CASE_LIST_TABLE_NAME = "gtt_query_case_list"
    public static final String DATE_FMT = "dd-MM-yyyy"
    private static final String DATETIME_FMT = "dd-MM-yyyy HH:mm:ss"
    private static final String DATETIME_FRMT = "dd-MMM-yyyy HH:mm:ss"
    private static final String DATETIME_FMT_ORA = "dd-MM-yyyy HH24:MI:SS"
    private static final String NEGATIVE_INFINITY = Holders.config.getProperty('pvreports.cumulative.startDate')
    private static final String POSITIVE_INFINITY = "31-12-9999 23:59:59"
//    TODO need to check merge
    private static final String[] CUSTOM_LM_TABLE_ALIASES = ['cls2', 'cls3', 'cls4', 'cldt', 'lsty', 'cpb', 'cprt', 'crss', 'ccls', 'ctt', 'sns', 'sbs', 'ccs', 'ccca', 'cpert', 'crm', 'crtf', 'lltyp', 'cddt', 'cdee', 'cdop', 'cdeu', 'cuod', 'ls','cdrd','cmfr','cmt','crm','mhgpt','mhhc','mhpc','mhpt','mmh','mpt','mptl','ms','msc','mshc','msio','msl','msqc','mst','mstd','msy','caur','cccj','ccsj','cifr','clsj','cpru','creo','lprt']
    private static final QUERY_LEVEL_SUBMISSION_CMR_TABLE_ALIAS = "cmr"


    public static final String SET_TABLE_NAME = "set_table"

    public static final Pattern PARTIAL_DATE_YEAR_ONLY = Pattern.compile("\\?{2}-\\?{3}-\\d{4}")
    public static final Pattern PARTIAL_DATE_MONTH_AND_YEAR = Pattern.compile("\\?{2}-[a-zA-Z]{3}-\\d{4}")
    public static final Pattern PARTIAL_DATE_FULL = Pattern.compile("\\d{2}-[a-zA-Z]{3}-\\d{4}")

    // Re-assess Listedness
    private static final RLDS = "RLDS"

    // No value is selected in configuration
    private static final NO_VALUE_SELECTED = "No Value Selected"

    public static final REPORTS_VOIDED_FIELD = "reportsVoided"
    public static final VOIDED_JAVA_VARIABLE = "voided"
    public static final VOIDED_SOURCE_COLUMN = "CMR_VOIDED"

    GrailsApplication grailsApplication
    def customMessageService
    def messageSource
    def dataSource_pva
    def templateService
    def executorThreadInfoService
    def userService
    def utilService

    // Generate the SQL necessary to build the query to generate a "version temp table".
    // This is the initial step taken for the generation of a report. The "version temp
    // table" will become an input into the next step ( generateQuerySQL ).

    public String generateVersionSQL(BaseTemplateQuery tempQuery, Locale locale) {
        //TODO: check the date range for each template query
        String result
        // If template is Data Tabulation, check if its measures have Cumulative Count or Custom Period Count
        boolean hasCumulative = false
        if (tempQuery.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            DataTabulationTemplate template = tempQuery.usedTemplate
            hasCumulative = template.hasCumulative()
        }

        BaseConfiguration config = tempQuery.usedConfiguration
        Date startDate = tempQuery.startDate
        Date endDate = tempQuery.endDate
        Boolean includeLockedVersion = config.includeLockedVersion
        Boolean excludeFu = config.excludeFollowUp
        def asOfVersionDate = config.getAsOfVersionDateCustom(false)

        if (hasCumulative) {
            startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY)
        }

        if (config.dateRangeType?.name == DateRangeTypeCaseEnum.SUBMISSION_DATE.value()) {
            boolean dataVersionFlag = false

            if (config?.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF_GENERATION_DATE) {
                dataVersionFlag = true
            }
            result = VersionSqlOnReports(startDate, endDate, dataVersionFlag, includeLockedVersion)
        } else {

            def asOfCaseVersionDate
            boolean latestVersionFlag = false

            if (config?.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                asOfCaseVersionDate = config.getAsOfVersionDate()
            } else {
                asOfCaseVersionDate = null
            }

            String initialDateFilterCol = ""
            String revMstDateFilterCol = ""
            boolean reportOnLockDate = false

            initialDateFilterCol = SourceColumnMaster.getRevMasterDateColumn(config.dateRangeType,locale)
            revMstDateFilterCol = SourceColumnMaster.getRevMasterDateColumn(config.dateRangeType, locale)

            if (config.dateRangeType?.name == DateRangeTypeCaseEnum.CASE_LOCKED_DATE.value()) {
                reportOnLockDate = true
            }

            if (config?.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
                latestVersionFlag = true
            }

            if (CheckCumulativeTabFlag(tempQuery)) {
                startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT,NEGATIVE_INFINITY)
            }
            result = VersionSQLCase(initialDateFilterCol, revMstDateFilterCol, asOfCaseVersionDate, startDate, endDate, includeLockedVersion, latestVersionFlag, reportOnLockDate, excludeFu)
        }
        return result
    }

    //TODO Latest version flag is not passed properly. test later
    private String VersionSqlOnReports(Date startDate, Date endDate, boolean generatedVersionFlag, boolean lockedOnlyFlag) {
        String lockCheck = ""
        String generatedCheck = ""
        String result = ""
        if (generatedVersionFlag) {
            generatedCheck = " AND  crm.version_start_date <= crr.generation_date "
        }
        if (lockedOnlyFlag) {
            lockCheck = " And (crm.CASE_LOCKED_FLAG =1 or crm.STATE_ID = 1) "
        }
        result = """
            SELECT crm.TENANT_ID,crm.case_id, MAX (crm.version_num) version_num
            FROM CASE_REVISION_INFO  crm, V_C_SUBMISSIONS crr
            WHERE crm.case_id = crr.case_id and crm.TENANT_ID = crr.TENANT_ID
            AND trunc(crr.DATE_SUBMISSION) >=   trunc(TO_DATE('${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
            AND trunc(crr.DATE_SUBMISSION)   <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}')) +1
            ${lockCheck}
            ${generatedCheck}
            GROUP BY  crm.TENANT_ID,crm.case_id
        """
        return result
    }

    private String VersionSQLCase(String caseMasterFilterCol, String revMasterFilterCol, Date CaseVersionAsOfDate, Date startDate, Date endDate, boolean lockedOnlyFlag, boolean latestVersionFlag, boolean lockedDateReportFlag, boolean excludeFollowup) {

        String cleanupColName = " Version_num "
        String result = ""
        String excludeFuSql = ""

        boolean includeCleanupVersion = grailsApplication.config.reports.includeDataCleanupVersion ?: true

        if (excludeFollowup) {
            excludeFuSql = " and FLAG_INITIAL_FU =0 "
        }

        if (includeCleanupVersion) {
            cleanupColName = " CLEANUP_VERSION_NUM "
        }

        // if as of date is not null then add as of date clause in SQL
        if (latestVersionFlag) {
            if (lockedDateReportFlag) {
                result = """ select crm.TENANT_ID, crm.case_id, max(crm.version_num) version_num from CASE_REVISION_INFO crm
            where exists ( select 1 from CDR_DATES crmi where crmi.case_id = crm.case_id
             and crmi.TENANT_ID = crm.TENANT_ID  and Seq_num =0
             and  trunc(crmi.DATE_LOCKED_CASE)  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
             and  trunc(crmi.DATE_LOCKED_CASE)  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1 )
                and (crm.CASE_LOCKED_FLAG =1 or crm.STATE_ID = 1)
            group by  crm.TENANT_ID, crm.case_id """
            } else {
                if (lockedOnlyFlag) {
                    result = """ select a.TENANT_ID, a.case_id , a.version_num from (select crm.TENANT_ID, crm.case_id, max(crm.version_num) version_num from CASE_REVISION_INFO crm
                        where  exists ( select 1 from CDR_DATES crmi where crmi.case_id = crm.case_id and crmi.FLAG_SIGNIFICANT =1
                         and crmi.TENANT_ID = crm.TENANT_ID ${excludeFuSql}
                         and  trunc(${caseMasterFilterCol})  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
                         and  trunc(${caseMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1 )
                         and (crm.CASE_LOCKED_FLAG =1 or crm.state_id = 1) group by  crm.TENANT_ID, crm.case_id) a where exists ( select 1  from CASE_REVISION_INFO b where
                         a.case_id = b.case_id and b.version_num = a.version_num and a.tenant_id = b.tenant_id and (b.CASE_LOCKED_FLAG =1 or b.state_id = 1)) """
                } else {
                    result = """ select crm.TENANT_ID, crm.case_id, max(crm.version_num) version_num from CASE_REVISION_INFO crm
                        where  exists ( select 1 from CDR_DATES crmi where crmi.case_id = crm.case_id and crmi.FLAG_SIGNIFICANT =1
                         and crmi.TENANT_ID = crm.TENANT_ID ${excludeFuSql}
                         and  trunc(${caseMasterFilterCol})  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
                         and  trunc(${caseMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1 )
                        group by  crm.TENANT_ID, crm.case_id """
                }
            }
        } else if (CaseVersionAsOfDate) {
            if (lockedDateReportFlag) {
                result = """ select crm.TENANT_ID, crm.case_id, max(crm.version_num) version_num from CASE_REVISION_INFO crm
            where TO_DATE('${CaseVersionAsOfDate.format(DATETIME_FMT)}','${DATETIME_FMT_ORA}')  >= decode(crm.state_id ,1,crm.version_start_date,crm.locked_date) and
            exists ( select 1 from CDR_DATES crmi where crmi.case_id = crm.case_id   and Seq_num =0  and crmi.TENANT_ID = crm.TENANT_ID
            and trunc(DATE_LOCKED_CASE)  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
            and trunc(DATE_LOCKED_CASE)  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1)
            group by  crm.TENANT_ID, crm.case_id """
            } else {
                result =""" SELECT  TENANT_ID ,case_id,   MAX(version_num) version_num FROM ( SELECT cd.case_id, cd.TENANT_ID,  cd.version_num,
               CASE WHEN cd.FLAG_SIGNIFICANT = 1 THEN cd.${caseMasterFilterCol} ELSE last_value(DECODE(cd.FLAG_SIGNIFICANT,1, cd.${caseMasterFilterCol})) ignore nulls
               over(partition BY cd.TENANT_ID, cd.case_id order by cd.version_num, cd.create_date ASC) END ${caseMasterFilterCol}, cd.locked_date
            FROM CDR_DATES_pv cd ) where trunc(${caseMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1
             AND trunc(LOCKED_DATE) < trunc(TO_DATE('${CaseVersionAsOfDate.format(DATETIME_FMT)}','${DATETIME_FMT_ORA}')+ 1) GROUP BY case_id, TENANT_ID  """
            }

        } else {
            if (lockedDateReportFlag) {
                result = """
            select crm.TENANT_ID, crm.case_id, max(crm.version_num) version_num from CASE_REVISION_INFO crm where
                 trunc(crm.${revMasterFilterCol})  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
               and trunc(crm.${revMasterFilterCol})  < trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}')) +1
              group by crm.TENANT_ID, crm.case_id """
            } else {
                if (lockedOnlyFlag) (
                        result = """
                        select  a.TENANT_ID, a.case_id, a.version_num  from (select  TENANT_ID, case_id, max(version_num) version_num from
                        (select crm.TENANT_ID, crm.case_id, crm.${cleanupColName} version_num from CASE_REVISION_INFO crm where
                         trunc(crm.${revMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}')) +1
                         and exists ( select 1 from CDR_DATES crmi where crmi.case_id = crm.case_id ${excludeFuSql} and crmi.TENANT_ID = crm.TENANT_ID
                         and crmi.FLAG_SIGNIFICANT =1 and  trunc(${caseMasterFilterCol})  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
                         and  trunc(${caseMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1 )
                         and (crm.CASE_LOCKED_FLAG =1 or crm.state_id = 1) ) group by TENANT_ID, case_id ) a  where exists ( select 1  from CASE_REVISION_INFO b where
                         a.case_id = b.case_id and b.version_num = a.version_num and a.tenant_id = b.tenant_id and (b.CASE_LOCKED_FLAG =1 or b.state_id = 1))
                """
                ) else {
                    result = """
                        select  TENANT_ID, case_id, max(version_num) version_num from
                        (select crm.TENANT_ID, crm.case_id, crm.${cleanupColName} version_num from CASE_REVISION_INFO crm where
                         trunc(crm.${revMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}')) +1
                         and exists ( select 1 from CDR_DATES crmi where crmi.case_id = crm.case_id ${excludeFuSql} and crmi.TENANT_ID = crm.TENANT_ID
                         and crmi.FLAG_SIGNIFICANT =1 and  trunc(${caseMasterFilterCol})  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
                         and  trunc(${caseMasterFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1 )
                         ) group by TENANT_ID, case_id
                      """
                }

            }
        }
        return result
    }

    private boolean CheckCumulativeTabFlag(BaseTemplateQuery templateQuery) {
        boolean boolRet = false
        if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            DataTabulationTemplate dataTabulationTemplate = templateQuery.usedTemplate
            dataTabulationTemplate.columnMeasureList.each { columnMeasureSet ->
                if (columnMeasureSet.showTotalCumulativeCases) {
                    boolRet = true
                }
                List<DataTabulationMeasure> measures = columnMeasureSet.measures
                measures.each {
                    CountTypeEnum dateRangeCount = it.dateRangeCount

                    if (!boolRet) {
                        if (dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) {
                            boolRet = true
                        }
                        if ((dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT)) {
                            boolRet = true
                        }
                    }

                }
            }
        }
        return boolRet;
    }

    public String processDictionariesWithNoDLPRev(ExecutorDTO executorDTO, versionSql) {
        return processDictionaries(executorDTO, versionSql, false)
    }

    public String processDictionariesWithDLPRev(BaseTemplateQuery templateQuery, Locale locale) {
        BaseConfiguration config = templateQuery.usedConfiguration
        if(config.dateRangeType == null){
            return null
        }
        Boolean excludeFollowUp = config.excludeFollowUp
        Date startDate = templateQuery.startDate
        Date endDate = templateQuery.endDate
//        String initialDateFilterCol = SourceColumnMaster.findByReportItem(config.dateRangeType.name).columnName
        String initialDateFilterCol = ReportField.findByName(config.dateRangeType?.name).getSourceColumn(locale).columnName
        String dateCheckSql = ""
        String excludeFollowupCheck = ""
        String includeMedicallyConfirmedCases = ""
        boolean hasWhere = false
        boolean medicallyConfirmedCasesFlag = config.includeMedicallyConfirmedCases

        if (CheckCumulativeTabFlag(templateQuery)) {
            startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY)
        }
        if (excludeFollowUp) {
            excludeFollowupCheck = """ and dc.seq_num =0 """
        }
        //PVDB-526 date check should not be present for date range = submission_date or case_locked_date
        if ((config.dateRangeType.name != DateRangeTypeCaseEnum.SUBMISSION_DATE.value()) &&
                config.dateRangeType.name != DateRangeTypeCaseEnum.CASE_LOCKED_DATE.value()) {
            dateCheckSql = """where exists ( select 1 from CDR_DATES dc where dc.TENANT_ID = t2.TENANT_ID and
                    dc.VERSION_NUM = t2.VERSION_NUM and dc.case_id = t2.case_id and dc.FLAG_SIGNIFICANT =1 ${excludeFollowupCheck}
                     and  trunc(dc.${initialDateFilterCol})  >= trunc(TO_DATE( '${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))
                     and  trunc(dc.${initialDateFilterCol})  <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1)"""
            hasWhere = true
        }
        else if (config.dateRangeType.name == DateRangeTypeCaseEnum.CASE_LOCKED_DATE.value()) // Adding check to remove unlocked deleted cases (PVR-2774)
        {
            if (!hasWhere) {
                dateCheckSql = " WHERE"
            } else {
                dateCheckSql = " AND"
            }

            dateCheckSql += " EXISTS (SELECT 1 FROM C_IDENTIFICATION ci WHERE NVL(LOCKED_DATE,ARCHIEVE_DATE) IS NOT NULL" +
                    " AND ci.TENANT_ID = t2.TENANT_ID AND ci.CASE_ID = t2.CASE_ID AND ci.VERSION_NUM = t2.VERSION_NUM)"

            hasWhere = true
        }

        // Adding sql for including only medically confirmed cases (PVR-1743)
        if (medicallyConfirmedCasesFlag) {
            if (!hasWhere) {
                includeMedicallyConfirmedCases = " WHERE"
            } else {
                includeMedicallyConfirmedCases = " AND"
            }
            includeMedicallyConfirmedCases += " EXISTS (SELECT 1 FROM CASE_FLAGS cf WHERE " +
                    " cf.TENANT_ID = t2.TENANT_ID " +
                    " AND cf.CASE_ID = t2.CASE_ID " +
                    " AND cf.VERSION_NUM = t2.version_num AND cf.${grailsApplication.config.reports.includeMedConfCases ?:'HCP_FLAG'} = 1 )"
        }

        String versionSql = " Select t2.TENANT_ID ,t2.CASE_ID ,t2.VERSION_NUM from $VERSION_BASE_TABLE_NAME t2 ${dateCheckSql} ${includeMedicallyConfirmedCases}"

        return processDictionaries(ExecutorDTO.create(config), versionSql, true)

    }

    private String processDictionaries(ExecutorDTO executorDTO, String versionSql, boolean joinDLPVersion) {
        String result = versionSql
        if ((executorDTO.usedEventSelection) || executorDTO.productSelection || executorDTO.studySelection) {
            List dictionaryFilter = getDictionaryFilter(executorDTO, joinDLPVersion)
            result = """${dictionaryFilter[0]} select TENANT_ID, case_id, version_num
                from (${versionSql}) t1 ${dictionaryFilter[1]}"""

        }
        return result
    }

    public String generateVersionTableInsert(String versionSQL) {
        return " INSERT INTO $VERSION_BASE_TABLE_NAME (TENANT_ID, case_id, version_num) ${versionSQL}"
    }

    public String generateFinalVersionTableInsert(String versionSQL) {
        return " INSERT INTO $VERSION_TABLE_NAME (TENANT_ID, case_id, version_num) ${versionSQL}"
    }

    public List getQueryEventDictFilter(ExecutorDTO executorDTO) {
        return getCaseListFromEvent(executorDTO, true, true) // Assuming DLPJoin = true
    }

    public List getQueryStudyDictFilter(ExecutorDTO executorDTO) {
        return getCaseListFromStudy(executorDTO, true, true) // Assuming DLPJoin = true
    }

    private List getDictionaryFilter(ExecutorDTO executorDTO, boolean DLPJoin) {
        String withClause = ""
        String whereClause = ""

        if (executorDTO.productSelection) {
            String productFilter = getCaseListFromProduct(executorDTO, DLPJoin)

            if (whereClause != "") {
                whereClause += " and "
            } else {
                whereClause +=" ( "
            }

            whereClause += "case_id in (${productFilter})"
        }

        if (executorDTO.studySelection) {
            def studyFilter = getCaseListFromStudy(executorDTO, DLPJoin, false)

            if (whereClause != "") {
                //           Fix for running ProductSelection and Study Selection Together
                if(executorDTO.aClass in [PeriodicReportConfiguration.class, ExecutedPeriodicReportConfiguration.class]){
                    whereClause += " or "
                } else {
                    whereClause += " and "
                }

            } else {
                whereClause +=" ( "
            }
            withClause += studyFilter[0]
            whereClause += "case_id in (${studyFilter[1]})"
        }
        if(executorDTO.productSelection || executorDTO.studySelection){
            whereClause +=" ) "
        }

        if (executorDTO.usedEventSelection) {
            def eventFilter = getCaseListFromEvent(executorDTO, DLPJoin, false)

            if (whereClause != "") {
                whereClause += " and "
                if (withClause != "" && eventFilter[0] != "") {
                    withClause += ", "
                }
            }

            withClause += eventFilter[0]
            whereClause += "case_id in (${eventFilter[1]})"
        }

        if (whereClause) {
            if (withClause != "") {
                withClause = " with " + withClause
            }
            return [withClause, "where " + whereClause]
        } else {
            return ["", ""]
        }
    }

    private String getCaseListFromProduct(ExecutorDTO executorDTO, boolean DLPJoin) {
        String DLPRevJoinSQL = ""
        if (DLPJoin) {
            DLPRevJoinSQL = "And cp.version_num = t1.version_num "
        }

        String result = """SELECT cp.case_id FROM C_PROD_IDENTIFICATION cp
                        WHERE cp.TENANT_ID = t1.TENANT_ID  and cp.case_id = t1.case_id ${DLPRevJoinSQL}"""
        //           Fix for running ProductSelection and Study Selection Together
        if (executorDTO.studySelection) {
            return result
        }
        result += """AND
            (cp.modified_product_id IN ( SELECT product_id FROM ("""
        return appendProductFilterInfo(executorDTO, result) + ")))"
    }

    private String appendProductFilterInfo(ExecutorDTO executorDTO, String result) {
        List<Map> productDetails = MiscUtil.getProductDictionaryValues(executorDTO.productSelection)
        Map ingredient = productDetails[0]
        Map family = productDetails[1]
        Map product = productDetails[2]
        Map trade = productDetails[3]

        String selectDicLevel = ""

        if (ingredient) {
            String ingredientCodes = ingredient.keySet()?.toString()
            ingredientCodes = ingredientCodes.substring(1, ingredientCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += """(SELECT lpc.product_id FROM VW_PROD_INGRED_LINK lpc
                            JOIN VW_INGREDIENT_DSP li ON (lpc.ingredient_id = li.ingredient_id and lpc.TENANT_ID = li.TENANT_ID)
                            WHERE li.ingredient_id IN (${ingredientCodes}))"""
        }

        if (family) {
            String familyCodes = family.keySet()?.toString()
            familyCodes = familyCodes.substring(1, familyCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += """(SELECT lp.product_id FROM VW_PRODUCT_DSP lp
                            JOIN VW_FAMILY_NAME_DSP lpf ON (lpf.prod_family_id = lp.prod_family_id and lpf.TENANT_ID = lp.TENANT_ID  )
                            WHERE lpf.prod_family_id IN (${familyCodes}))"""
        }

        if (product) {
            String productCodes = product.keySet()?.toString()
            productCodes = productCodes.substring(1, productCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += "(SELECT lp.product_id FROM VW_PRODUCT_DSP lp WHERE lp.product_id IN (${productCodes}))"
        }

        if (trade) {
            String tradeCodes = trade.keySet()?.toString()
            tradeCodes = tradeCodes.substring(1, tradeCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += """(SELECT llp.product_id FROM VW_PROD_LICENSE_LINK_DSP llp
                            JOIN VW_TRADE_NAME_DSP ll ON (ll.license_id = llp.license_id and ll.TENANT_ID = llp.TENANT_ID)
                            WHERE ll.license_id IN (${tradeCodes}))"""
        }

        result += selectDicLevel

        return result
    }

    private List getCaseListFromStudy(ExecutorDTO executorDTO, boolean DLPJoin, boolean querySQLSelection) {
        List<Map> studyDetails = MiscUtil.getStudyDictionaryValues(executorDTO.studySelection)
        Map protocol = studyDetails[0]
        Map study = studyDetails[1]
        Map center = studyDetails[2]
        String DLPRevJoinSQL = ""
        if (DLPJoin) {
            if (querySQLSelection) {
                DLPRevJoinSQL = "and cs.version_num = ver.version_num "
            } else {
                DLPRevJoinSQL = "And cs.version_num = t1.version_num "
            }
        }
        String withClause = ""
        String selectDicLevel = ""

        if (study) {
            String studyCodes = study.keySet()?.toString()
            studyCodes = studyCodes.substring(1, studyCodes.length() - 1) // remove "[" and "]"
            if (withClause != "") {
                withClause += " UNION "
            }
            withClause += "(SELECT ls.study_key FROM VW_STUDY_NUM_DSP ls WHERE ls.study_key IN (${studyCodes}))"

            if (selectDicLevel != "") {
                selectDicLevel += " OR "
            }
            selectDicLevel += "cs.study_id IN (${studyCodes})"
        }

        if (protocol) {
            String protocolCodes = protocol.keySet()?.toString()
            protocolCodes = protocolCodes.substring(1, protocolCodes.length() - 1) // remove "[" and "]"
            if (withClause != "") {
                withClause += " UNION "
            }
            withClause += "(SELECT ls.study_key FROM VW_STUDY_NUM_DSP ls WHERE ls.id_protocol IN (${protocolCodes}))"

            if (selectDicLevel != "") {
                selectDicLevel += " OR "
            }
            selectDicLevel += " UPPER(cs.study_project_id) IN (" +
                    " SELECT UPPER(lpt.PROTOCOL_DESCRIPTION) FROM VW_PROTOCOL_DSP lpt WHERE lpt.PROTOCOL_ID IN (${protocolCodes}))"
        }

        if (center) {
            String centerCodes = center.keySet()?.toString()
            centerCodes = centerCodes.substring(1, centerCodes.length() - 1) // remove "[" and "]"
            if (withClause != "") {
                withClause += " UNION "
            }
            withClause += """(SELECT lsc.study_key FROM VW_STUDY_CENTER_LINK lsc INNER JOIN VW_LCE_CENTER_NAME_DSP lc
                               ON (lsc.center_id = lc.center_id and lsc.TENANT_ID = lc.TENANT_ID) WHERE lc.center_id IN (${
                centerCodes
            }))"""

            if (selectDicLevel != "") {
                selectDicLevel += " OR "
            }
            selectDicLevel += " UPPER(cs.study_center_name) IN (SELECT UPPER(center_name) from VW_LCE_CENTER_NAME where center_id in (${centerCodes}))"
        }

        String whereClauseResult
        if (querySQLSelection) {
            whereClauseResult = """SELECT DISTINCT cs.case_id FROM case_study_info cs, gtt_versions ver WHERE cs.TENANT_ID = ver.TENANT_ID and cs.case_id = ver.case_id ${
                DLPRevJoinSQL
            } AND cs.version_num = ver.version_num
                AND (${selectDicLevel} OR EXISTS (SELECT 1 FROM key_filter kf WHERE cs.study_id = kf.study_key))"""
        } else {
            whereClauseResult = """SELECT DISTINCT cs.case_id FROM case_study_info cs WHERE cs.TENANT_ID = t1.TENANT_ID and cs.case_id = t1.case_id ${
                DLPRevJoinSQL
            } AND cs.version_num = t1.version_num
                AND (${selectDicLevel} OR EXISTS (SELECT 1 FROM key_filter kf WHERE cs.study_id = kf.study_key))"""
        }

        return [" key_filter AS (SELECT study_key FROM (${withClause})) ", whereClauseResult]
    }

    private List getCaseListFromEvent(ExecutorDTO executorDTO, boolean DLPJoin, boolean querySQLSelection) {
        List<Map> eventDetails = MiscUtil.getEventDictionaryValues(executorDTO.usedEventSelection)
        Map soc = eventDetails[0]
        Map hlgt = eventDetails[1]
        Map hlt = eventDetails[2]
        Map pt = eventDetails[3]
        Map llt = eventDetails[4]
        Map synonyms = eventDetails[5]
        String DLPRevJoinSQL = ""
        if (DLPJoin) {
            if (querySQLSelection) {
                DLPRevJoinSQL = "and ce.version_num = ver.version_num "
            } else {
                DLPRevJoinSQL = "And ce.version_num = t1.version_num "
            }
        }

        String ptFilter = "pt_filter AS (SELECT mmh.pt_code FROM PVR_md_hierarchy_dsp mmh"

        if (soc || hlgt || hlt || pt) {
            String whereClause = ""

            if (soc) {
                String socCodes = soc.keySet()?.toString()
                socCodes = socCodes.substring(1, socCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.soc_code IN (${socCodes})"
            }

            if (hlgt) {
                String hlgtCodes = hlgt.keySet()?.toString()
                hlgtCodes = hlgtCodes.substring(1, hlgtCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.hlgt_code IN (${hlgtCodes})"
            }

            if (hlt) {
                String hltCodes = hlt.keySet()?.toString()
                hltCodes = hltCodes.substring(1, hltCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.hlt_code IN (${hltCodes})"
            }

            if (pt) {
                while (pt.size() > 1000) {
                    String ptCodes = pt.take(1000).toString()
                    ptCodes = ptCodes.substring(1, ptCodes.length() - 1) // remove "[" and "]"
                    pt = pt.drop(1000)
                }
                String ptCodes = pt.keySet()?.toString()
                ptCodes = ptCodes.substring(1, ptCodes.length() - 1) // remove "[" and "]"

                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.pt_code IN (${ptCodes})"
            }

            ptFilter += " WHERE (${whereClause})"
        }
        if (executorDTO.limitPrimaryPath) {
            ptFilter = ptFilter + " and mmh.PRIMARY_SOC_FG = 'Y' "
        }
        ptFilter += ")"
        String lltFilter = """, llt_filter AS (SELECT llt_code FROM (SELECT mptl.llt_code FROM PVR_MD_pref_term_llt_dsp mptl
                            WHERE EXISTS (SELECT 1 FROM pt_filter t1 WHERE t1.pt_code = mptl.pt_code )))"""
        String withClause = ""
        String whereClause = ""
        if (llt) {
            while (llt.size() > 1000) {
                String lltCodes = llt.take(1000).toString()
                lltCodes = lltCodes.substring(1, lltCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " or "
                }
                whereClause += "t1.pt_code in (${lltCodes})"
                llt = llt.drop(1000)
            }
            String lltCodes = llt.keySet()?.toString()
            lltCodes = lltCodes.substring(1, lltCodes.length() - 1) // remove "[" and "]"
            if (whereClause != "") {
                whereClause += " or "
            }
            whereClause += "ce.meddra_llt_code IN (${lltCodes})"
        }

        if (synonyms) {
            if (whereClause != "") {
                whereClause += " or "
            }
            String synonymsCodes = synonyms.keySet()?.toString()
            synonymsCodes = synonymsCodes.substring(1, synonymsCodes.length() - 1) // remove "[" and "]"
            whereClause = " ce.meddra_llt_code IN ( SELECT ms.llt_code FROM PVR_MD_synonyms_dsp ms WHERE ms.syn_id IN (${synonymsCodes}))"
        }

        if (soc || hlgt || hlt || pt ) {
            if (whereClause != "") {
                whereClause += " OR "
            }
            if (querySQLSelection) {
                whereClause += "EXISTS (SELECT 1 FROM llt_filter t2, pt_filter t1 WHERE t2.llt_code = ce.meddra_llt_code)"
            } else {
                whereClause += "EXISTS (SELECT 1 FROM llt_filter t2 WHERE t2.llt_code = ce.meddra_llt_code)"
            }
            withClause = ptFilter + lltFilter
        }

        String whereClauseResult
        if (querySQLSelection) {
            whereClauseResult = """SELECT ce.case_id, ce.AE_REC_NUM FROM C_AE_IDENTIFICATION ce, gtt_versions ver WHERE ce.tenant_id = ver.TENANT_ID
                and ce.case_id = ver.case_id ${DLPRevJoinSQL} AND (${whereClause})"""
        } else {
            whereClauseResult = """SELECT ce.case_id FROM C_AE_IDENTIFICATION ce WHERE ce.tenant_id = t1.TENANT_ID
                and ce.case_id = t1.case_id ${DLPRevJoinSQL} AND (${whereClause})"""
        }

        //A check is introduced
        if (executorDTO.limitPrimaryPath) {
            whereClauseResult = whereClauseResult + " and ce.primary_path_coded_flag =1"
        }

        return [withClause, whereClauseResult]
    }

    // Generate the SQL which represents the Query filters. The SQL will generate a "filteredCaseList temp table" The
    // "filteredCaseList temp table" will become an input into the next step ( generateReportSQL ).
    public String generateQuerySQL(BaseTemplateQuery templateQuery, SuperQuery query, boolean materialize,
                                   boolean forNotValidCase, int reassessIndex, Locale locale,
                                   List<QueryExpressionValue> querySetBlanks = [], boolean isQuerySet = false) {

        def executed
        def selectFields = ""
        def queryLevel = templateQuery.queryLevel
        if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.usedConfiguration.productSelection) {
            selectFields += " cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num, "
        } else if (templateQuery.usedConfiguration.studySelection) {
            // We do not need any extra tables here
        }
        if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || (templateQuery.usedConfiguration.usedEventSelection)) {
            // query must pass case id, product sequence number and product event sequence number (PVR-117)
            selectFields += " ce.AE_REC_NUM,"
        }
        if (queryLevel == QueryLevelEnum.SUBMISSION) {
            selectFields += " cmr.PROCESSED_REPORT_ID,"
        }


        // check query set blanks, if none, create blanks
        List<QueryExpressionValue> blanks = []
        if (isQuerySet) {
            blanks = querySetBlanks
        } else {
            templateQuery.getUsesQueryValueLists()?.each {
                it.parameterValues.each {
                    if (it.hasProperty('reportField')) {
                        blanks.add(it)
                    }
                }
            }
        }

        if (query.JSONQuery) {
            executed = buildFilterSQLFromJSON(query.JSONQuery, templateQuery.usedConfiguration.nextRunDate, "UTC", blanks,
                    reassessIndex, templateQuery.usedConfiguration.evaluateDateAs, templateQuery.usedConfiguration.asOfVersionDate,locale, forNotValidCase, templateQuery)
        }

        LinkedHashMap usedLMTables = executed.usedLMTables
        String extraLMJoins = ""
        int lmTableAlias = -1

        usedLMTables.each {
            ReportField rf = ReportField.findBySourceColumnIdAndIsDeleted(it.key,false)
            lmTableAlias = (int) it.value
            String enterpriseCheck = ""
            SourceColumnMaster sourceColumnMaster = rf.getSourceColumn(locale)
            if (sourceColumnMaster.lmTableName.hasEnterpriseId == 1) {
                enterpriseCheck = """ AND ${sourceColumnMaster.tableName.tableAlias}.TENANT_ID
                      = ${sourceColumnMaster.lmTableName.tableAlias}_${lmTableAlias}.TENANT_ID"""
            }

            extraLMJoins += """
                ${sourceColumnMaster.lmJoinType == "O" ? " LEFT " : ""}
                JOIN ${sourceColumnMaster.lmTableName.tableName} ${sourceColumnMaster.lmTableName.tableAlias}_${lmTableAlias}
                ON  (${sourceColumnMaster.tableName.tableAlias}.${sourceColumnMaster.columnName}
                      = ${sourceColumnMaster.lmTableName.tableAlias}_${lmTableAlias}.${sourceColumnMaster.lmJoinColumn}${
                enterpriseCheck
            })"""

        }
        String fromClause = buildFromClauseFromReportFields(executed.reportFields, query, templateQuery, forNotValidCase, locale)

        //TODO: get prod_rec_num and event_seq_num when we implement product/event level queries.
        // return """select cm.TENANT_ID, cm.case_id, max(cm.version_num) version_num, cp.prod_rec_num prod_rec_num, ce.AE_REC_NUM event_seq_num
        // from ${buildFromClauseFromReportFields(executed.reportFields)}
        // where ${executed.result} group by cm.TENANT_ID, cm.case_id, cp.prod_rec_num, ce.AE_REC_NUM"""

        String selectClause = """select ${materialize ? "/*+ MATERIALIZE */ " : ""}cm.TENANT_ID, cm.case_id,$selectFields cm.version_num """
        if (forNotValidCase) {
            selectClause = "select 1"
        }

        def returnSql = """${selectClause} from ${fromClause} ${extraLMJoins} """

        if (executed.result != "()") {
            returnSql += """where ${executed.result} """
        }

        return returnSql
    }

    public String generateSetSQL(BaseTemplateQuery templateQuery, SuperQuery query, Locale locale) {
        def executed

        def selectFields = ","

        if (query.JSONQuery) {
            executed = buildSetSQLFromJSON(templateQuery,query)
        }

        String withClause = buildWithSQLFromQueries(templateQuery, executed.tempTables, locale)

        return """WITH ${withClause} ${executed.result}"""
    }

    public String generateCustomQuerySQL(BaseTemplateQuery templateQuery, SuperQuery query) {
        QueryLevelEnum queryLevel = templateQuery.queryLevel

        String result = "select cm.TENANT_ID, cm.case_id, cm.version_num "

        if ((queryLevel == QueryLevelEnum.CASE && templateQuery.usedConfiguration instanceof ExecutedPeriodicReportConfiguration)) {
            // no need of it @Akash
        } else if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.usedConfiguration.productSelection) {
            result += ", cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num"
        } else if (templateQuery.usedConfiguration.studySelection) {
            // We do not need any extra tables here
        }
        if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || (templateQuery.usedConfiguration.usedEventSelection)) {
            result += ", ce.ae_rec_num"
        }
        if (queryLevel == QueryLevelEnum.SUBMISSION) {
            result += ", cmr.PROCESSED_REPORT_ID"
        }

        result += " from V_C_IDENTIFICATION cm"

        if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.usedConfiguration.productSelection) {
            result += " join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num AND cm.tenant_id = cp.tenant_id)"
        } else if (templateQuery.usedConfiguration.studySelection) {
            // We do not need any extra tables here
        }
        if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || (templateQuery.usedConfiguration.usedEventSelection)) {
            result += " join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.tenant_id = ce.tenant_id)"
        }
        if (queryLevel == QueryLevelEnum.SUBMISSION) {
            result += " left join V_C_SUBMISSIONS cmr on (cm.CASE_ID = cmr.CASE_ID AND cm.tenant_id = cmr.tenant_id)"
        }

        // @TODO this is not safe to call, customSQLQuery ONLY exists on CustomSQLQuery class
        String sqlQuery = query.customSQLQuery

        // Parameters
        if (query.hasBlanks) {
            Map<String, String> parameterMap = [:]
            templateQuery.usesQueryValueLists?.each {
                it.parameterValues?.each {
                    parameterMap.put(it.key, it.value)
                }
            }
            sqlQuery = replaceMapInString(sqlQuery, parameterMap)
        }

        return """$result join $VERSION_TABLE_NAME ver
                    on (cm.case_id = ver.case_id and cm.version_num = ver.version_num) ${sqlQuery}"""
    }

    public String generateEmptyQuerySQL(BaseTemplateQuery templateQuery) {
        QueryLevelEnum queryLevel = templateQuery.queryLevel

        String result = "select cm.TENANT_ID, cm.case_id, cm.version_num "
        if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.usedConfiguration.productSelection) {
            result += ", cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num"
        } else if (templateQuery.usedConfiguration.studySelection) {
            // We do not need any extra tables here
        }
        if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || (templateQuery.usedConfiguration.usedEventSelection)) {
            result += ", ce.AE_REC_NUM"
        }
        if (queryLevel == QueryLevelEnum.SUBMISSION) {
            result += ", cmr.PROCESSED_REPORT_ID"
        }

        result += " from V_C_IDENTIFICATION cm"


        if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.usedConfiguration.productSelection) {
            String productFilter = ""
            if (templateQuery.usedConfiguration.productSelection) {
                String str = """(cp.modified_product_id IN ( SELECT product_id FROM ("""
                productFilter = " AND " + appendProductFilterInfo(ExecutorDTO.create(templateQuery.usedConfiguration), str) + ")))"
            }
            result += """ join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num
                    AND cm.TENANT_ID = cp.TENANT_ID ${productFilter})"""
        } else if (templateQuery.usedConfiguration.studySelection) {
            // We do not need any extra tables here
        }
        if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || (templateQuery.usedConfiguration.usedEventSelection)) {
            result += " join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.TENANT_ID = ce.TENANT_ID)"
        }
        if (queryLevel == QueryLevelEnum.SUBMISSION) {
            result += " left join V_C_SUBMISSIONS cmr on (cm.CASE_ID = cmr.CASE_ID AND cm.TENANT_ID = cmr.TENANT_ID)"
        }

        return """$result join $VERSION_TABLE_NAME ver
                    on (cm.case_id = ver.case_id and cm.version_num = ver.version_num)"""
    }

    private String replaceMapInString(String sqlQuery, Map<String, String> parameterMap) {
        String result = sqlQuery
        parameterMap.each { key, value ->
            Pattern p = Pattern.compile(key);
            result = p.matcher(result).replaceAll(value?:"''")
        }
        return result
    }

    public String generateCaseListInsert(String querySQL, QueryLevelEnum queryLevel, String executedReportConfigurationId, boolean hasAllReadyGeneratedCases) {
        if (hasAllReadyGeneratedCases) {
            querySQL = replaceWithQueryTable(querySQL, executedReportConfigurationId)
        }
        if (queryLevel == QueryLevelEnum.PRODUCT) {
            return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num, prod_rec_num) ${querySQL} "
        } else if (queryLevel == QueryLevelEnum.PRODUCT_EVENT) {
            return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num, prod_rec_num, AE_REC_NUM) ${querySQL} "
        } else if (queryLevel == QueryLevelEnum.SUBMISSION) {
            return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num, PROCESSED_REPORT_ID) ${querySQL} "
        }
        return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num) ${querySQL} "
    }

    private String replaceWithQueryTable(String query, String executedReportConfigurationId) {
//        TODO need to get fix columns name from Akash later
        log.debug("######## Replacing ${VERSION_TABLE_NAME} with ${PVR_CASE_LIST_TABLE_NAME} ###############")
        return query.replaceAll("${VERSION_TABLE_NAME} ver", "${PVR_CASE_LIST_TABLE_NAME} ver").replaceAll("= ver.version_num", "= ver.VERSION_NUM) "+
                "join pvr_query_info pqi on (pqi.case_series_exec_id = ver.case_series_exec_id and pqi.case_series_id =" + executedReportConfigurationId)
    }

    private int selectedPercentOption(PercentageOptionEnum percentageOption, CountTypeEnum countTypeEnum) {
        int selectedOption = 0

        if (percentageOption == PercentageOptionEnum.BY_SUBTOTAL) {
            selectedOption = 1
        } else if (percentageOption == PercentageOptionEnum.BY_TOTAL) {
            selectedOption = 2
        } else if ((percentageOption == PercentageOptionEnum.INTERVAL_TO_CUMULATIVE) && (countTypeEnum == CountTypeEnum.CUMULATIVE_COUNT)) {
            selectedOption = 3
        }
        return selectedOption
    }

    public List generateMatrixSQL(BaseTemplateQuery templateQuery, Locale locale) {
        String tabulationSQL = "select 'select x from dual' from dual"
        String headerSQL = ""
        if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {

            int rowCount = 0
            int colCount = 0
            int totalMatrixCount = 0
            String caseCountFlags = ""
            String eventCountFlags = ""
            String prodEventCountFlags = ""
            String versionCountFlags = ""
            String reportCountFlags = ""
            String complianceRateFlags = ""
            String rowCountFlags = ""
            String casePercentFlags = ""
            String eventPercentFlags = ""
            String prodEventPercentFlags = ""
            String versionPercentFlags = ""
            String reportPercentFlags = ""
            String rowPercentFlags = ""
            String countRows = ""
            String countCols = ""
            String printCaseListFlags = ""
            String printTotalColumnFlags = ""
            String printTotalCumColumnFlags = ""
            String printCaseCountSum = ""
            String printEventCountSum = ""
            String printProdEventCountSum = ""
            String printVersionCountSum = ""
            String printReportCountSum = ""
            String printComplianceRateCountSum = ""
            String printRowCountSum = ""
            String sortOrder = ""
            String sortColumnType = ""
            String sortLmCheck = ""
            String psurIndex = ""

            DataTabulationTemplate dataTabulationTemplate = templateQuery.usedTemplate

            Date intervalStartDate = templateQuery.startDate
            Date intervalEndDate = templateQuery.endDate
            rowCount = dataTabulationTemplate.selectedFieldsRows?.reportField?.size()?:0
            List<ReportField> selectedFieldsRows = dataTabulationTemplate?.selectedFieldsRows?.reportField?:[]
            def eventSocIndex = ((selectedFieldsRows.findIndexOf { it.name == "eventBodySysDict"} + 1) + (selectedFieldsRows.findIndexOf { it.name == "eventIchAll"}+ 1))
            def ichSortIndex =  ((selectedFieldsRows.findIndexOf { it.name == "eventIchAll"}+ 1) + (selectedFieldsRows.findIndexOf { it.name == "eventSocIch"} + 1))

            dataTabulationTemplate.columnMeasureList.each { block ->
                totalMatrixCount ++
            }

            dataTabulationTemplate.columnMeasureList.each { columnMeasureSet ->
                int caseCountIntervalFlag = 0
                int caseCountCumulativeFlag = 0
                int caseCountCustomFlag = 0
                int caseCountIntervalSum = 0
                int caseCountCumulativeSum = 0
                int caseCountCustomSum = 0
                int casePercentIntervalFlag = 0
                int casePercentCumulativeFlag = 0
                int casePercentCustomFlag = 0

                int eventCountIntervalFlag = 0
                int eventCountCumulativeFlag = 0
                int eventCountCustomFlag = 0
                int eventCountIntervalSum = 0
                int eventCountCumulativeSum = 0
                int eventCountCustomSum = 0
                int eventPercentIntervalFlag = 0
                int eventPercentCumulativeFlag = 0
                int eventPercentCustomFlag = 0

                int prodEventCountIntervalFlag = 0
                int prodEventCountCumulativeFlag = 0
                int prodEventCountCustomFlag = 0
                int prodEventCountIntervalSum = 0
                int prodEventCountCumulativeSum = 0
                int prodEventCountCustomSum = 0
                int prodEventPercentIntervalFlag = 0
                int prodEventPercentCumulativeFlag = 0
                int prodEventPercentCustomFlag = 0

                int versionCountIntervalFlag = 0
                int versionCountCumulativeFlag = 0
                int versionCountCustomFlag = 0
                int versionCountIntervalSum = 0
                int versionCountCumulativeSum = 0
                int versionCountCustomSum = 0
                int versionPercentIntervalFlag = 0
                int versionPercentCumulativeFlag = 0
                int versionPercentCustomFlag = 0

                int reportCountIntervalFlag = 0
                int reportCountCumulativeFlag = 0
                int reportCountCustomFlag = 0
                int reportCountIntervalSum = 0
                int reportCountCumulativeSum = 0
                int reportCountCustomSum = 0
                int reportPercentIntervalFlag = 0
                int reportPercentCumulativeFlag = 0
                int reportPercentCustomFlag = 0

                int complianceRateCountIntervalFlag = 0
                int complianceRateCountCumulativeFlag = 0
                int complianceRateCountCustomFlag = 0
                int complianceRateCountIntervalSum = 0
                int complianceRateCountCumulativeSum = 0
                int complianceRateCountCustomSum = 0

                int rowCountIntervalFlag = 0
                int rowCountCumulativeFlag = 0
                int rowCountCustomFlag = 0
                int rowCountIntervalSum = 0
                int rowCountCumulativeSum = 0
                int rowCountCustomSum = 0
                int rowPercentIntervalFlag = 0
                int rowPercentCumulativeFlag = 0
                int rowPercentCustomFlag = 0

                int sumFlag = 0
                int printCaseList = 0
                int printCaseIntCountCol = 0
                int printCaseCumCountCol = 0
                int checkPsur

                colCount = columnMeasureSet.columnList?.reportFieldInfoList?.size() ?: 0

                ReportFieldInfoList columns = columnMeasureSet.columnList

                if (colCount > 0) {
                    checkPsur = columns?.reportFieldInfoList?.reportField?.findIndexOf {
                        it.name == "masterPsurSource"
                    } + 1
                }
                if (colCount > 0) {
                    columns.reportFieldInfoList?.each { ReportFieldInfo rf ->
                        SourceColumnMaster sourceColumn = rf.reportField.getSourceColumn(locale)
                        sortOrder += rf?.sort?.value()?.substring(0, 1)?.toUpperCase() ?: "A"
                        sortLmCheck = sourceColumn.lmTableName
                        if (sortLmCheck != null) {
                            sortColumnType = sortColumnType + "V"
                        } else {
                            sortColumnType = sortColumnType + sourceColumn.columnType
                        }

                    }
                }

                boolean showTotalIntervalCases = columnMeasureSet.showTotalIntervalCases
                boolean showTotalCumulativeCases = columnMeasureSet.showTotalCumulativeCases
                if (showTotalIntervalCases) {
                    printCaseIntCountCol = 1
                } else {
                    printCaseIntCountCol = 0
                }

                if (showTotalCumulativeCases) {
                    printCaseCumCountCol = 1
                } else {
                    printCaseCumCountCol = 0
                }

                List<DataTabulationMeasure> measures = columnMeasureSet.measures

                measures.each {
                    MeasureTypeEnum type = it.type
                    // return MeasureTypeEnum.CASE_COUNT or MeasureTypeEnum.EVENT_COUNT or MeasureTypeEnum.PRODUCT_EVENT_COUNT or MeasureTypeEnum.REPORT_COUNT
                    CountTypeEnum dateRangeCount = it.dateRangeCount
                    PercentageOptionEnum percentageOption = it.percentageOption
                    // return PercentageOptionEnum.NO_PERCENTAGE or PercentageOptionEnum.BY_TOTAL or PercentageOptionEnum.BY_SUBTOTAL

                    if (it.showTotal) {
                        sumFlag = 1
                    } else {
                        sumFlag = 0
                    }
                    switch (type) {
                        case CASE_COUNT:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (caseCountIntervalFlag == 0)) {
                                caseCountIntervalFlag = 1
                                caseCountIntervalSum = sumFlag
                                casePercentIntervalFlag = selectedPercentOption(percentageOption, dateRangeCount)

                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (caseCountCumulativeFlag == 0)) {
                                caseCountCumulativeFlag = 1
                                caseCountCumulativeSum = sumFlag
                                casePercentCumulativeFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (caseCountCustomFlag == 0)) {
                                caseCountCustomFlag = 1
                                caseCountCustomSum = sumFlag
                                casePercentCustomFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            break
                        case EVENT_COUNT:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (eventCountIntervalFlag == 0)) {
                                eventCountIntervalFlag = 1
                                eventCountIntervalSum = sumFlag
                                eventPercentIntervalFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (eventCountCumulativeFlag == 0)) {
                                eventCountCumulativeFlag = 1
                                eventCountCumulativeSum = sumFlag
                                eventPercentCumulativeFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (eventCountCustomFlag == 0)) {
                                eventCountCustomFlag = 1
                                eventCountCustomSum = sumFlag
                                eventPercentCustomFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            break
                        case PRODUCT_EVENT_COUNT:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (prodEventCountIntervalFlag == 0)) {
                                prodEventCountIntervalFlag = 1
                                prodEventCountIntervalSum = sumFlag
                                prodEventPercentIntervalFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (prodEventCountCumulativeFlag == 0)) {
                                prodEventCountCumulativeFlag = 1
                                prodEventCountCumulativeSum = sumFlag
                                prodEventPercentCumulativeFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (prodEventCountCustomFlag == 0)) {
                                prodEventCountCustomFlag = 1
                                prodEventCountCustomSum = sumFlag
                                prodEventPercentCustomFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            break
                        case VERSION_COUNT:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (versionCountIntervalFlag == 0)) {
                                versionCountIntervalFlag = 1
                                versionCountIntervalSum = sumFlag
                                versionPercentIntervalFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (versionCountCumulativeFlag == 0)) {
                                versionCountCumulativeFlag = 1
                                versionCountCumulativeSum = sumFlag
                                versionPercentCumulativeFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (versionCountCustomFlag == 0)) {
                                versionCountCustomFlag = 1
                                versionCountCustomSum = sumFlag
                                versionPercentCustomFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            break
                        case REPORT_COUNT:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (reportCountIntervalFlag == 0)) {
                                reportCountIntervalFlag = 1
                                reportCountIntervalSum = sumFlag
                                reportPercentIntervalFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (reportCountCumulativeFlag == 0)) {
                                reportCountCumulativeFlag = 1
                                reportCountCumulativeSum = sumFlag
                                reportPercentCumulativeFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (reportCountCustomFlag == 0)) {
                                reportCountCustomFlag = 1
                                reportCountCustomSum = sumFlag
                                reportPercentCustomFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            break
                        case COMPLIANCE_RATE:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (complianceRateCountIntervalFlag == 0)) {
                                complianceRateCountIntervalFlag = 1
                                complianceRateCountIntervalSum = sumFlag
                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (complianceRateCountCumulativeFlag == 0)) {
                                complianceRateCountCumulativeFlag = 1
                                complianceRateCountCumulativeSum = sumFlag
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (complianceRateCountCustomFlag == 0)) {
                                complianceRateCountCustomFlag = 1
                                complianceRateCountCustomSum = sumFlag
                            }
                            break
                        case ROW_COUNT:
                            if ((dateRangeCount == CountTypeEnum.PERIOD_COUNT) && (rowCountIntervalFlag == 0)) {
                                rowCountIntervalFlag = 1
                                rowCountIntervalSum = sumFlag
                                rowPercentIntervalFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) && (rowCountCumulativeFlag == 0)) {
                                rowCountCumulativeFlag = 1
                                rowCountCumulativeSum = sumFlag
                                rowPercentCumulativeFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            if ((dateRangeCount.type() == CountTypeEnum.CUSTOM_PERIOD_COUNT.type()) && (rowCountCustomFlag == 0)) {
                                rowCountCustomFlag = 1
                                rowCountCustomSum = sumFlag
                                rowPercentCustomFlag = selectedPercentOption(percentageOption, dateRangeCount)
                            }
                            break
                        case CASE_LIST:
                            if (printCaseList == 0) {
                                printCaseList = 1
                            }
                            break
                    }
                }
                caseCountFlags += "${caseCountIntervalFlag}${caseCountCumulativeFlag}${caseCountCustomFlag}"
                eventCountFlags += "${eventCountIntervalFlag}${eventCountCumulativeFlag}${eventCountCustomFlag}"
                prodEventCountFlags += "${prodEventCountIntervalFlag}${prodEventCountCumulativeFlag}${prodEventCountCustomFlag}"
                versionCountFlags += "${versionCountIntervalFlag}${versionCountCumulativeFlag}${versionCountCustomFlag}"
                reportCountFlags += "${reportCountIntervalFlag}${reportCountCumulativeFlag}${reportCountCustomFlag}"
                complianceRateFlags += "${complianceRateCountIntervalFlag}${complianceRateCountCumulativeFlag}${complianceRateCountCustomFlag}"
                rowCountFlags += "${rowCountIntervalFlag}${rowCountCumulativeFlag}${rowCountCustomFlag}"
                casePercentFlags += "${casePercentIntervalFlag}${casePercentCumulativeFlag}${casePercentCustomFlag}"
                eventPercentFlags += "${eventPercentIntervalFlag}${eventPercentCumulativeFlag}${eventPercentCustomFlag}"
                prodEventPercentFlags += "${prodEventPercentIntervalFlag}${prodEventPercentCumulativeFlag}${prodEventPercentCustomFlag}"
                versionPercentFlags += "${versionPercentIntervalFlag}${versionPercentCumulativeFlag}${versionPercentCustomFlag}"
                reportPercentFlags += "${reportPercentIntervalFlag}${reportPercentCumulativeFlag}${reportPercentCustomFlag}"
                rowPercentFlags += "${rowPercentIntervalFlag}${rowPercentCumulativeFlag}${rowPercentCustomFlag}"
                if(rowCount == 0){
                    countRows += "1"
                }else{
                    countRows +="${rowCount}"
                }
                countCols +="${colCount}"
                psurIndex += "${checkPsur}"
                printCaseListFlags +="${printCaseList}"
                printTotalColumnFlags += "${printCaseIntCountCol}"
                printTotalCumColumnFlags += "${printCaseCumCountCol}"
                printCaseCountSum += "${caseCountIntervalSum}${caseCountCumulativeSum}${caseCountCustomSum}"
                printEventCountSum += "${eventCountIntervalSum}${eventCountCumulativeSum}${eventCountCustomSum}"
                printProdEventCountSum += "${prodEventCountIntervalSum}${prodEventCountCumulativeSum}${prodEventCountCustomSum}"
                printVersionCountSum += "${versionCountIntervalSum}${versionCountCumulativeSum}${versionCountCustomSum}"
                printReportCountSum += "${reportCountIntervalSum}${reportCountCumulativeSum}${reportCountCustomSum}"
                printComplianceRateCountSum += "${complianceRateCountIntervalSum}${complianceRateCountCumulativeSum}${complianceRateCountCustomSum}"
                printRowCountSum += "${rowCountIntervalSum}${rowCountCumulativeSum}${rowCountCustomSum}"


            }
            tabulationSQL = """{? = call  pkg_data_tabulation.f_get_matrix_sql ( ${totalMatrixCount},'${countRows}','${countCols}','${caseCountFlags}',
                            '${eventCountFlags}', '${prodEventCountFlags}','${versionCountFlags}','${casePercentFlags}','${eventPercentFlags}'
                            ,'${prodEventPercentFlags}','${versionPercentFlags}','${printCaseListFlags}','${printTotalColumnFlags}','${printTotalCumColumnFlags}'
                            ,'${printCaseCountSum}', '${printEventCountSum}', '${printProdEventCountSum}', '${printVersionCountSum}', '${reportCountFlags}','${reportPercentFlags}'
                            ,'${printReportCountSum}','${sortColumnType}','${sortOrder}', '${complianceRateFlags}', '${printComplianceRateCountSum}', '${rowCountFlags}', '${rowPercentFlags}', '${printRowCountSum}')}"""
            log.info(tabulationSQL)
            headerSQL = """select pkg_data_tabulation.f_get_matrix_headers (  ${totalMatrixCount}, '${countRows}','${countCols}',
                        '${printCaseListFlags}','${printTotalColumnFlags}','${printTotalCumColumnFlags}') from dual"""

        }
        return [tabulationSQL, headerSQL]
    }

    // Reassess Listedness for template
    public String setReassessContextForTemplate(BaseTemplateQuery templateQuery, boolean hasQuery) {
        String procedureCall = "";
        ReassessListednessEnum reassessListednessEnum = templateQuery.usedTemplate.reassessListedness
        String reassessDate = ""
        String queryTableFlag = "1"
        Date endDate = templateQuery.endDate
        Date currentDate = new Date()
        List<Date> minMaxDate = templateQuery?.usedConfiguration?.reportMinMaxDate
        String minStartDate = minMaxDate.first()?.format(DATE_FMT)

        if (reassessListednessEnum == ReassessListednessEnum.BEGINNING_OF_THE_REPORTING) {
            reassessDate = minStartDate
        } else if (reassessListednessEnum == ReassessListednessEnum.END_OF_THE_REPORTING_PERIOD) {
            reassessDate = endDate.format(DATE_FMT)
        } else if (reassessListednessEnum == ReassessListednessEnum.CUSTOM_START_DATE) {
            reassessDate = currentDate.format(DATE_FMT)
            if (templateQuery?.usedTemplate?.templtReassessDate)
                reassessDate = templateQuery.usedTemplate.templtReassessDate.format(DATE_FMT)
            else if (templateQuery?.templtReassessDate)
                reassessDate = templateQuery.templtReassessDate.format(DATE_FMT)
        }

        String datasheet = ""
        List<ReportFieldInfo> templateFields = templateQuery.usedTemplate.getAllSelectedFieldsInfo()
        templateFields?.each {
            if (it?.datasheet) {
                datasheet += it.datasheet + "," + (it.onPrimaryDatasheet ? 1 : 0) + ","
            }
        }
        if (!hasQuery) {
            queryTableFlag = "0"
        }
        if (datasheet.length() > 0) {
            datasheet = datasheet.substring(0, datasheet.length() - 1)
            procedureCall = """{call pkg_reassess_listedness.p_report('${datasheet}','${reassessDate}',${queryTableFlag})}"""
        }

        return procedureCall

    }

    public String setPbrerContext(ReportTemplate template, BaseConfiguration configuration ) {
        String procedureCall = "";
        boolean pbrerReportItems = false

        String productCodes

//        Made a null safe here so that if no selectable column it won't run.
        List<ReportField> templateFields = template.getAllSelectedFieldsInfo()?.reportField
        Locale locale = configuration.owner.preference.locale
        templateFields?.each {ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (sourceColumn.reportItem =="GBP_MEDICINAL_TYPE_ID" ||sourceColumn.reportItem =="GBP_CASE_BUCKET_ID") {
                pbrerReportItems = true
            }
        }

        if (pbrerReportItems) {
            List product = configuration.getProductDictionaryValues()[2]
            if (product) {
                productCodes = product.toString()
                productCodes = productCodes.substring(1, productCodes.length() - 1) // remove "[" and "]"
                procedureCall = """{call PKG_POPULATE_POI.p_pbrer_bucket('${productCodes}')"""
            }
        }
        log.info(procedureCall)
        return procedureCall

    }

    // Reassess Listedness for query
    public List<String> setReassessContextForQuery(BaseTemplateQuery templateQuery) {
        List<String> procedureCall = [];
        if (templateQuery?.usedQuery) {
            if (templateQuery?.usedQuery?.queryType == QueryTypeEnum.QUERY_BUILDER) {
                Query query = (Query) templateQuery.usedQuery
                procedureCall.add(getSingleQueryProcedure(MiscUtil.unwrapProxy(query), templateQuery, 0))
            } else if (templateQuery?.usedQuery?.queryType == QueryTypeEnum.SET_BUILDER) {
                QuerySet querySet = (QuerySet) templateQuery.usedQuery
                int reassessIndex = 0
                querySet.queries.each {
                    if (it.queryType == QueryTypeEnum.QUERY_BUILDER) {
                        procedureCall.add(getSingleQueryProcedure(MiscUtil.unwrapProxy(it), templateQuery, reassessIndex))
                        if (Query.get(it.id)?.reassessListedness)
                            reassessIndex++
                    }
                }
            }
        }
        return procedureCall
    }

    private String getSingleQueryProcedure(def query, BaseTemplateQuery templateQuery, int reassessIndex) {
        String procedureCall = ""
        ReassessListednessEnum reassessListednessEnum = query.reassessListedness
        String reassessDate = ""
        Date startDate = templateQuery.startDate
        Date endDate = templateQuery.endDate
        Date currentDate = new Date()
        List<Date> minMaxDate = templateQuery?.usedConfiguration?.reportMinMaxDate
        String minStartDate = minMaxDate.first()?.format(DATE_FMT)

        if (reassessListednessEnum == ReassessListednessEnum.BEGINNING_OF_THE_REPORTING) {
            reassessDate = minStartDate
        } else if (reassessListednessEnum == ReassessListednessEnum.END_OF_THE_REPORTING_PERIOD) {
            reassessDate = endDate.format(DATE_FMT)
        } else if (reassessListednessEnum == ReassessListednessEnum.CUSTOM_START_DATE) {
            reassessDate = currentDate.format(DATE_FMT)
            if (query?.reassessListednessDate)
                reassessDate = query.reassessListednessDate.format(DATE_FMT)
            else if (templateQuery?.reassessListednessDate)
                reassessDate = templateQuery.reassessListednessDate.format(DATE_FMT)
        }

        String datasheet = ""
        query.JSONQuery = query.JSONQuery.replace("\\","\\\\")
        Map queryJSON = MiscUtil.parseJsonText(query.JSONQuery)
        Map allMap = queryJSON.all
        List containerGroupsList = allMap.containerGroups
        def expressionList = containerGroupsList.get(0)
        datasheet = fetchDatasheet(expressionList.getAt('expressions'))

        if (datasheet.length() > 0) {
            datasheet = datasheet.substring(0, datasheet.length() - 1)
            procedureCall = """{call pkg_reassess_listedness.p_query(${reassessIndex},'${datasheet}','${reassessDate}')}"""
        }

        return procedureCall
    }
    /**
     * Extract datasheet values from json in form ready to use in stored procedure
     * @param expression - json entity
     * @return string representation of datasheet
    */
    public String fetchDatasheet(expression){
        String datasheet = ""
        expression.eachWithIndex{ it, index ->
            if(it.containsKey("expressions")) {
                datasheet += fetchDatasheet(it.getAt("expressions"))
            }
            if(it.getAt("field") == "dvListednessReassessQuery" && it.RLDS) {
                datasheet += it.RLDS + "," + (it.RLDS_OPDS == "true" ? 1 : 0) + ","
            }
        }
        return datasheet
    }

    // Generate the SQL which represents the transformation and retrieval of data based on the Configuration.reportTemplate
    public String generateReportSQL(BaseTemplateQuery templateQuery, boolean hasQuery, ReportTemplate template, Locale locale) {

        List<ReportField> selectedFieldsOrg = template.getAllSelectedFieldsInfo().reportField

        List<ReportField> selectedFields = new ArrayList<ReportField>()
        int dsReAssessCount = 1

        selectedFieldsOrg.each { ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (sourceColumn.reportItem == "DCEAL_REASSESS_LISTEDNESS") {
                String key = "GDR_LISTEDNESS_DS_" + dsReAssessCount
                selectedFields.add(ReportField.findBySourceColumnIdAndIsDeleted(key,false))
                dsReAssessCount++;
            } else {
                selectedFields.add(rf)
            }
        }

        BaseConfiguration config = templateQuery.usedConfiguration
        def selectClause = ""
        def String lmTableJoins = ""
        boolean addedQuerying = false
        boolean isClobSelected = false
        String orderBy = ""
        def colNameList = []
        def commaTabList = []
        def tabJoin = []
        def tabJoinColList = [:]
        String partitionByStr = ""
        int iloop = 0
        Boolean excludeFollowUp = config.excludeFollowUp
        Boolean includeLockedVersion = config.includeLockedVersion
        def boolean useDistinct = false
        def boolean productFieldSelected = false
        def boolean eventFieldSelected = false
        String productTableAlias = ""
        String eventTableAlias = ""

        // get comma-separated value in CLL
        def flagSequence = []
        if (template.templateType == TemplateTypeEnum.CASE_LINE) {
            CaseLineListingTemplate cllTemplate = (CaseLineListingTemplate) template
            cllTemplate.getAllSelectedFieldsInfo().eachWithIndex { reportFieldInfo, i ->
                if (reportFieldInfo.commaSeparatedValue) {
                    flagSequence.add(i)
                }
            }

            useDistinct = cllTemplate.columnShowDistinct
        }
        // get custom expression list
        def customExpressionObject = template.getAllSelectedFieldsInfo().customExpression
        def customExpressionSequence = []
        template.getAllSelectedFieldsInfo().eachWithIndex { rf, index ->
            if (rf.customExpression) {
                customExpressionSequence.add(index)
            }
        }

        //select clause  and LM_ table join creation

        //TODO: Manually added these if we need C_AE_IDENTIFICATION and C_PROD_IDENTIFICATION. Removed for now.
        def product_event_seq = ""
        def productEventSelectedFields = selectedFields
        if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT) {
            productEventSelectedFields += [ReportField.findByNameAndIsDeleted("productProductName", false)]
            product_event_seq = " and cp.prod_rec_num = caseList.prod_rec_num"
        } else if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT) {
            productEventSelectedFields += [ReportField.findByNameAndIsDeleted("eventPrefTerm", false), ReportField.findByNameAndIsDeleted("productProductName",false)]
            product_event_seq = " and ce.AE_REC_NUM = caseList.prod_rec_num and cp.prod_rec_num = caseList.prod_rec_num"
        } else if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
            product_event_seq = " and caseList.PROCESSED_REPORT_ID = cmr.PROCESSED_REPORT_ID "
        }

        selectedFields.each { ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (flagSequence.contains(iloop)) {
                commaTabList.add(sourceColumn.tableName.tableName)
            }
            iloop++
            if (!productFieldSelected && sourceColumn.tableName.tableName == "C_PROD_IDENTIFICATION") {
                productFieldSelected = true
                productTableAlias  = sourceColumn.tableName.tableAlias
            }
            if (!eventFieldSelected && sourceColumn.tableName.tableName == "C_AE_IDENTIFICATION") {
                eventFieldSelected = true
                eventTableAlias  = sourceColumn.tableName.tableAlias
            }
        }

        template.getAllSelectedFieldsInfo()?.each { ReportFieldInfo rf ->
            SourceColumnMaster sourceColumn = rf.reportField.getSourceColumn(locale)
            if (useDistinct && !isClobSelected && sourceColumn.columnType == "C" && rf && !rf.customExpression) {
                isClobSelected = true
                useDistinct = false
            }
            if (rf.sortLevel > 0 && sourceColumn.tableName.tableName == "C_PROD_IDENTIFICATION"){
                productFieldSelected  = false
            }
            if (rf.sortLevel > 0 && sourceColumn.tableName.tableName == "C_AE_IDENTIFICATION"){
                eventFieldSelected  = false
            }
        }
        // from clause creation
        def tableName
        def tempTableNames = []
        String caseTableFromClause = ""

        //find table list
        // QueryLevel
        productEventSelectedFields.each { ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (tableName != sourceColumn.tableName.tableName) {
                tempTableNames.add(sourceColumn.tableName.tableName)
                tableName = sourceColumn.tableName.tableName
            }
        }

        if (!tempTableNames.contains("V_C_IDENTIFICATION")) {
            tempTableNames.add("V_C_IDENTIFICATION")
        }

        if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
            tempTableNames.add(SourceTableMaster.findByTableAlias(QUERY_LEVEL_SUBMISSION_CMR_TABLE_ALIAS).tableName)
        }

        // construct SQL after finding relation between case tables
        def Integer loopCounter = 0
        boolean recursiveFlag = true

        while (recursiveFlag && loopCounter < 5) {
            def relTableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def relCaseTableRelation = relTableJoinMapping.list {
                inList("tableName.tableName", tempTableNames)
                order("mapTableName.tableName", "asc")
            }
            recursiveFlag = false
            relCaseTableRelation.each { CaseColumnJoinMapping rf ->

                if (tableName == rf.mapTableName.tableName) {
                    if (!tempTableNames.contains(rf.mapTableName.tableName)) {
                        tempTableNames.add(rf.mapTableName.tableName)
                        recursiveFlag = true
                    }
                }
                tableName = rf.mapTableName.tableName

            }
            loopCounter++
        }

        loopCounter = 0
        // sort tables in join order
        def tableJoinOrder = SourceTableMaster.createCriteria()
        def tableList = tableJoinOrder.list {
            inList("tableName", tempTableNames)
            order("caseJoinOrder", "asc")
        }
        tableList.each { SourceTableMaster tabRec ->
            def tableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def caseTableRelation = tableJoinMapping.list {
                inList("mapTableName.tableName", tempTableNames)
                eq("tableName.tableName", tabRec.tableName)
                order("mapColumnName", "asc")
            }
            if (loopCounter > 0) {
                caseTableFromClause += (tabRec.caseJoinType == "O" ? " Left " : "") + " join "
            }
            caseTableFromClause += tabRec.tableName + " " + tabRec.tableAlias
            if (caseTableRelation.size() > 0) {
                caseTableFromClause += " on ("
            }
            def int iterations = 0
            partitionByStr = ""
            caseTableRelation.each { CaseColumnJoinMapping rf ->
                if (!commaTabList.contains(rf.mapTableName.tableName) || rf.mapTableName.tableName == "V_C_IDENTIFICATION") {
                    if (iterations > 0) {
                        caseTableFromClause += " AND "
                    }
                    iterations++
                    caseTableFromClause += rf.mapTableName.tableAlias + "." + rf.mapColumnName + " = " + rf.tableName.tableAlias + "." + rf.columnName
                    if (rf.tableName.versionedData == "V" && rf.mapTableName.versionedData == "V") {
                        caseTableFromClause += " and " + rf.mapTableName.tableAlias + ".version_num = " + rf.tableName.tableAlias + ".version_num"
                    }
                    if (rf.mapTableName.hasEnterpriseId == 1) {
                        caseTableFromClause += " AND " + rf.mapTableName.tableAlias + ".TENANT_ID = " + rf.tableName.tableAlias + ".TENANT_ID"
                    }
                    if (partitionByStr != "") partitionByStr += ","

                    partitionByStr += rf.tableName.tableAlias + "." + rf.columnName;
                }
            }
            if (caseTableRelation.size() > 0) {
                caseTableFromClause += " ) "
            }
            tabJoinColList.put(tabRec.tableName, partitionByStr)
            loopCounter++
        }

        def tempString
        loopCounter = 0
        def lmTableAlias = ""

        selectedFields.each { ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (sourceColumn?.lmDecodeColumn) {
                if (sourceColumn?.lmTableName?.tableName && sourceColumn?.tableName?.tableAlias) {
                    lmTableAlias = sourceColumn.tableName.tableAlias + loopCounter // unique alias for LM table
                    lmTableJoins += (sourceColumn.lmJoinType == "O" ? " Left " : "") + " Join " + sourceColumn.lmTableName.tableName + " "
                    lmTableJoins += lmTableAlias + " on (" + sourceColumn.tableName.tableAlias + "."
                    lmTableJoins += sourceColumn.columnName + " = " + lmTableAlias + "." + sourceColumn.lmJoinColumn
                    if (sourceColumn.lmTableName.hasEnterpriseId == 1) {
                        lmTableJoins += " AND " + lmTableAlias + ".TENANT_ID = " + sourceColumn.tableName.tableAlias + ".TENANT_ID"
                    }
                    lmTableJoins += " ) "
                    tempString = lmTableAlias + "." + sourceColumn.lmDecodeColumn
                    colNameList.add(lmTableAlias + "." + sourceColumn.lmDecodeColumn)
                }
            } else {
                tempString = sourceColumn.tableName.tableAlias + "." + sourceColumn.columnName
                colNameList.add(tempString)
            }

            // for comma-separated values
            if (flagSequence.contains(loopCounter)) {
                String partitioningClauseStr = tabJoinColList[sourceColumn.tableName.tableName]
                if (partitioningClauseStr == "") {
                    partitioningClauseStr = " cm.Case_id "
                }
                if (sourceColumn.columnType == "N") {
                    selectClause += " replace( regexp_replace( regexp_replace((LISTAGG(to_char(" + tempString + "), ',') WITHIN GROUP (ORDER BY " + tempString + ") OVER (PARTITION BY " +
                            partitioningClauseStr + ")),',\\s*',',') ,'([^,]+)(,\\1)+', '\\1'),',',', ')  AS " + sourceColumn.columnName + loopCounter + ","

                } else if (sourceColumn.columnType == "D") {
                    selectClause += " replace( regexp_replace( regexp_replace((LISTAGG(to_char(" + tempString + ", 'dd-MMM-yyyy'), ',') WITHIN GROUP (ORDER BY " + tempString + ") OVER (PARTITION BY " +
                            partitioningClauseStr + ")),',\\s*',',') ,'([^,]+)(,\\1)+', '\\1'),',',', ')  AS " + sourceColumn.columnName + loopCounter + ","

                } else {
                    selectClause += " replace( regexp_replace( regexp_replace((LISTAGG(" + tempString + ", ',') WITHIN GROUP (ORDER BY " + tempString + ") OVER (PARTITION BY " +
                            partitioningClauseStr + ")),',\\s*',',') ,'([^,]+)(,\\1)+', '\\1'),',',', ')  AS " + sourceColumn.columnName + loopCounter + ","

                }
            } else if (customExpressionSequence.contains(loopCounter)) {
                selectClause += customExpressionObject[loopCounter] + ","
            } else {
                if (sourceColumn.concatField == "1") {
                    selectClause += "REPLACE_SEPARATOR(" + tempString + ",',\n')" + " AS " + sourceColumn.columnName + loopCounter + ","
                } else {
                    selectClause += tempString + " AS " + sourceColumn.columnName + loopCounter + ","
                }

            }
            loopCounter++
        }

        orderBy = "order by "
        template.sortInfo().each {
            orderBy += colNameList.get(it[0]) + " " + it[1] + ","
        }
//PVR-2390 : if no sort column is selected then sort on product sort_id if any column from C_PROD_IDENTIFICATION is selected and on event sort_id if any column from C_AE_IDENTIFICATION is selected
        if (!useDistinct && orderBy == "order by "){
            orderBy += "cm.case_num,"
        }
        if (!useDistinct && productFieldSelected){
            orderBy += "${productTableAlias}.rank_id,"
        }
        if (!useDistinct && eventFieldSelected){
            orderBy += "${eventTableAlias}.ae_rank_id,"
        }

        if (orderBy == "order by ") { // if no sort order
            orderBy = ""
        } else {
            orderBy = orderBy.substring(0, orderBy.length() - 1) // remove the last comma
        }

        // if ICH SOC in order by then replace it with ICH intl order
        orderBy = orderBy.toUpperCase().replace("CE.UD_TEXT_12","CE.UD_NUMBER_12")
        orderBy = orderBy.toUpperCase().replace("CE.MEDDRA_PT","CE.SORT_ID")

        if (useDistinct) {
            tempString = " distinct "
        } else {
            tempString = " "
        }

        String result = "select ${tempString} ${selectClause.getAt(0..selectClause.length() - 2)} from ${caseTableFromClause} ${lmTableJoins}"

        if (hasQuery) {
            result += """ where exists
                        (select 1 from $CASE_LIST_TABLE_NAME caseList
                            where cm.case_id = caseList.case_id and
                                  cm.version_num = caseList.version_num
                                  and caseList.TENANT_ID = cm.TENANT_ID
                        $product_event_seq)"""

        } else {
            result += """ where exists
                        (select 1 from $VERSION_TABLE_NAME ver
                            where cm.case_id = ver.case_id and
                                  cm.version_num = ver.version_num
                                  and ver.TENANT_ID = cm.TENANT_ID )"""
        }

        return "$result ${orderBy}"
    }

    public String generateCustomReportSQL(BaseTemplateQuery templateQuery, boolean hasQuery) {
        CustomSQLTemplate template = templateQuery.usedTemplate

        String result = "${template.customSQLTemplateSelectFrom}"
        if (hasQuery) {
            result += """ where exists
                        (select 1 from $CASE_LIST_TABLE_NAME caseList
                            where cm.case_id = caseList.case_id and
                                  cm.version_num = caseList.version_num
                                  and caseList.TENANT_ID = cm.TENANT_ID)"""
        } else {
            result += """ where exists
                        (select 1 from $VERSION_TABLE_NAME ver
                            where cm.case_id = ver.case_id and
                                  cm.version_num = ver.version_num
                                  and ver.TENANT_ID = cm.TENANT_ID)"""
        }
        if (template.customSQLTemplateWhere) {
            result += " ${template.customSQLTemplateWhere}"
        }

        // Parameters
        if (template.hasBlanks) {
            Map<String, String> parameterMap = [:]
            templateQuery.usedTemplateValueLists?.each {
                it.parameterValues?.each {
                    parameterMap.put(it.key, it.value)
                }
            }
            result = replaceMapInString(result, parameterMap)
        }

        return result
    }

    String generateNonCaseReportSQL(BaseTemplateQuery templateQuery) {
        NonCaseSQLTemplate template = templateQuery.usedTemplate
        String result = "${template.nonCaseSql}"

        // Parameters
        if (template.hasBlanks) {
            Map<String, String> parameterMap = [:]
            templateQuery.usedTemplateValueLists?.each {
                it.parameterValues?.each {
                    parameterMap.put(it.key, it.value)
                }
            }
            result = replaceMapInString(result, parameterMap)
        }

        return result
    }

    // Temporarily exposed for testing in building SQL from our JSON representation of Expressions & ExpressionGroups
    private String buildFromClauseFromReportFields(
            def selectedFields, SuperQuery query, BaseTemplateQuery templateQuery, boolean forNotValidCase, Locale locale) {
        String lmTableJoins = ""
        boolean addedVersioning = false

        if (!forNotValidCase) {
            if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT || templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.usedConfiguration.productSelection) {
                selectedFields.addAll([ReportField.findByNameAndIsDeleted("productProductName",false)])
            } else if (templateQuery.usedConfiguration.studySelection) {
                // We do not need any extra tables here
            }
            if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT || (templateQuery.usedConfiguration.usedEventSelection)) {
                selectedFields.addAll([ReportField.findByNameAndIsDeleted("eventPrefTerm",false)])
            }
        }


        //Make sure case master is always included
        BaseConfiguration usedConfiguration = templateQuery.usedConfiguration
        String caseNumberFieldName = usedConfiguration.sourceProfile.caseNumberFieldName ?: "masterCaseNum"
        selectedFields.add([ReportField.findByNameAndIsDeleted(caseNumberFieldName, false)])

        def tableName
        def tempTableNames = []
        String caseTableFromClause = ""

        //find table list
        // QueryLevel
        selectedFields.each { ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (tableName != sourceColumn.tableName.tableName) {
                tempTableNames.add(sourceColumn.tableName.tableName)
                tableName = sourceColumn.tableName.tableName
            }
        }
        if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
            tempTableNames.add(SourceTableMaster.findByTableAlias(QUERY_LEVEL_SUBMISSION_CMR_TABLE_ALIAS).tableName)
//            tableName = rf.getSourceColumn(lang).tableName.tableName
        }

        // construct SQL after finding relation between case tables
        def Integer loopCounter = 0
        boolean recursiveFlag = true

        while (recursiveFlag && loopCounter < 5) {
            def relTableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def relCaseTableRelation = relTableJoinMapping.list {
                inList("tableName.tableName", tempTableNames)
                order("mapTableName.tableName", "asc")
            }
            recursiveFlag = false
            relCaseTableRelation.each { CaseColumnJoinMapping rf ->

                if (tableName == rf.mapTableName.tableName) {
                    if (!tempTableNames.contains(rf.mapTableName.tableName)) {
                        tempTableNames.add(rf.mapTableName.tableName)
                        recursiveFlag = true
                    }
                }
                tableName = rf.mapTableName.tableName

            }
            loopCounter++
        }

        loopCounter = 0
        // sort tables in join order
        def tableJoinOrder = SourceTableMaster.createCriteria()
        def tableList = tableJoinOrder.list {
            inList("tableName", tempTableNames)
            order("caseJoinOrder", "asc")
        }

        tableList.each { SourceTableMaster tabRec ->
            if (tabRec.tableName != "GTT_DATASHEET_REASSESS") { // For re-assess listedness, don't join
                def tableJoinMapping = CaseColumnJoinMapping.createCriteria()
                def caseTableRelation = tableJoinMapping.list {
                    inList("mapTableName.tableName", tempTableNames)
                    eq("tableName.tableName", tabRec.tableName)
                    order("mapColumnName", "asc")
                }
                if (loopCounter > 0) {
                    caseTableFromClause += (tabRec.caseJoinType == "O" ? " Left " : "") + " join "
                }

                caseTableFromClause += tabRec.tableName + " " + tabRec.tableAlias
                if (caseTableRelation.size() > 0) {
                    caseTableFromClause += " on ("
                }
                def int iterations = 0

                caseTableRelation.each { CaseColumnJoinMapping rf ->
                    if (iterations > 0) {
                        caseTableFromClause += " AND "
                    }
                    iterations++
                    caseTableFromClause += rf.mapTableName.tableAlias + "." + rf.mapColumnName + " = " + rf.tableName.tableAlias + "." + rf.columnName
                    if (rf.tableName.versionedData == "V" && rf.mapTableName.versionedData == "V") {
                        caseTableFromClause += " AND " + rf.mapTableName.tableAlias + ".version_num = " + rf.tableName.tableAlias + ".version_num"
                    }
                    if (rf.mapTableName.hasEnterpriseId == 1) {
                        caseTableFromClause += " AND " + rf.mapTableName.tableAlias + ".TENANT_ID = " + rf.tableName.tableAlias + ".TENANT_ID"
                    }
//           Fix for running ProductSelection and Study Selection Together TODO need to check further if Require with Akash
                    if (rf.tableName.tableAlias == "cp" && templateQuery.usedConfiguration.productSelection && !templateQuery.usedConfiguration.studySelection) {
                        String str = """(cp.modified_product_id IN ( SELECT product_id FROM ("""
                        caseTableFromClause += " AND " + appendProductFilterInfo(ExecutorDTO.create(templateQuery.usedConfiguration), str) + ")))"
                    }

                }
                if (caseTableRelation.size() > 0) {
                    caseTableFromClause += " ) "
                }
                loopCounter++

                if (!addedVersioning && tabRec.tableName == "V_C_IDENTIFICATION") {
                    caseTableFromClause += """ join $VERSION_TABLE_NAME ver
                                            on (cm.case_id = ver.case_id and
                                                cm.version_num = ver.version_num) """
                    addedVersioning = true
                }
            }
        }

        // Concatenating select + from  clause to construct compilable SQL.
        return caseTableFromClause + lmTableJoins
    }

    public def buildSetSQLFromJSON(BaseTemplateQuery templateQuery=null,SuperQuery query=null) {
        Map tempTables = [:]
        Map dataMap
        if(query?.nonValidCases){
            dataMap=MiscUtil.parseJsonText(query.JSONQuery)
        }
        else{
            dataMap = MiscUtil.parseJsonText(templateQuery.usedQuery.JSONQuery)
        }
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String result = ""
        for (int i = 0; i < containerGroupsList.size(); i++) {
            if ((allMap.keyword) && (i > 0)) {
                result += " ${allMap.keyword} "
            }
            def executed
            if(query?.nonValidCases){
                executed=buildSetCriteriaFromGroup(containerGroupsList[i], tempTables, templateQuery,query)
            }
            else{
                executed= buildSetCriteriaFromGroup(containerGroupsList[i], tempTables, templateQuery,query)
            }
            result += "(${executed.result})"
        }
        return [result: result, tempTables: tempTables]
    }

    public String excludeNonValidCases(SuperQuery nonValidQuery, BaseTemplateQuery templateQuery, Locale locale) {
        // Non-valid cases query is not allowed to have parameters.
        // Also not allowed to be a QuerySet.
        String nonValidCasesSQL
        if (nonValidQuery.queryType == QueryTypeEnum.SET_BUILDER) {
            nonValidCasesSQL=generateSetSQL(templateQuery,nonValidQuery,locale)
        }
        else {
             nonValidCasesSQL = generateQuerySQL(templateQuery, nonValidQuery, false, true, 0, locale)
        }
        return """ NOT EXISTS ($nonValidCasesSQL AND cm.case_id = query.case_id AND cm.version_num = query.version_num
                   AND cm.TENANT_ID = query.TENANT_ID)"""
    }

    //Exclude Deleted Cases for Template Query
    public String excludeDeletedCases(SuperQuery deletedQuery, BaseTemplateQuery templateQuery, Locale locale) {
        String deletedCasesSQL = generateQuerySQL(templateQuery, deletedQuery, false, true, 0, locale)
        return """ NOT EXISTS ($deletedCasesSQL AND cm.case_id = query.case_id AND cm.version_num = query.version_num
                   AND cm.TENANT_ID = query.TENANT_ID)"""
    }

    public String suspectProductSql() {
        return """ INNER JOIN C_PROD_IDENTIFICATION cp2
                    ON query.case_id = cp2.CASE_ID
                    AND query.prod_rec_num = cp2.prod_rec_num
                    AND query.prod_version_num = cp2.version_num
                    WHERE cp2.DRUG_TYPE=1"""
    }

    //helper method, don't call this
    private def buildSetCriteriaFromGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Map tempTables, BaseTemplateQuery templateQuery=null,SuperQuery query=null) {
        String result = ""
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    result += " ${groupMap.keyword} "
                }
                def executed = buildSetCriteriaFromGroup(expressionsList[i], tempTables, templateQuery,query)
                result += "(${executed.result})";
            }
        } else {
            if (groupMap.keyword) {
                result += " ${groupMap.keyword} "
            }
            // Previously only allowed one query per querySet, but now allows any number of duplicate queries.
            String index = groupMap.index
            tempTables.put(index, groupMap.query)
            result += convertQueryToWhereClause(index, ExecutorDTO.create(templateQuery.usedConfiguration), templateQuery.queryLevel)
        }
        return [result: result, tempTables: tempTables]
    }

    private String convertQueryToWhereClause(String index, ExecutorDTO executorDTO ,QueryLevelEnum queryLevel) {
        def selectFields = ""
        if (queryLevel == QueryLevelEnum.PRODUCT && (executorDTO.aClass == ExecutedPeriodicReportConfiguration.class)) {
//            no need @Akash
        } else {
            if (queryLevel == QueryLevelEnum.PRODUCT || executorDTO.productSelection) {
                selectFields += " prod_rec_num, prod_version_num,"
            }
            if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || (executorDTO.usedEventSelection)) {
                // query must pass case id, product sequence number and product event sequence number (PVR-117)
                selectFields += " AE_REC_NUM,"
            }
            if (queryLevel == QueryLevelEnum.SUBMISSION) {
                selectFields += " PROCESSED_REPORT_ID,"
            }
        }
        return "SELECT DISTINCT TENANT_ID, CASE_ID,${selectFields} VERSION_NUM FROM ${SET_TABLE_NAME}_${index}"
    }

    // For QuerySet
    private String buildWithSQLFromQueries(BaseTemplateQuery templateQuery, LinkedHashMap tempTables, Locale locale) {
        String result = ""
        List<QueryExpressionValue> blanks = []
        boolean initializedBlanks = false

        int reassessIndex = 0
        tempTables.eachWithIndex { elem, index ->
            if (index > 0) {
                result += ", "
            }

            SuperQuery query = SuperQuery.get(elem.value)

            switch (query.queryType) {
                case QueryTypeEnum.QUERY_BUILDER:
                    // Parameters need to be shared between all the queries in a QuerySet.
                    if (!initializedBlanks) {
                        templateQuery.getUsesQueryValueLists()?.each {
                            it.parameterValues.each {
                                if (it.hasProperty('reportField')) {
                                    blanks.add(it)
                                }
                            }
                        }
                        initializedBlanks = true
                    }

                    result += "${SET_TABLE_NAME}_${elem.key} AS (${generateQuerySQL(templateQuery, query, true, false, reassessIndex,locale, blanks, true)})"
                    if (query.reassessListedness) {
                        reassessIndex++
                    }
                    break;
                case QueryTypeEnum.CUSTOM_SQL:
                    result += "${SET_TABLE_NAME}_${elem.key} AS (${generateCustomQuerySQL(templateQuery, query)})"
                    break;
                default:
                    throw SQLException
                    break;
            }
        }

        return result
    }

    //TODO: Add VERSION_NUM joins
    public def buildFilterSQLFromJSON(String dataJson, Date nextRunDate, String timezone,
                                      List<QueryExpressionValue> blanks,
                                      int reassessIndex, EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate, Locale locale, boolean forNotValidCase = false, BaseTemplateQuery templateQuery = null) {
        List<ReportField> reportFields = []
        Map lmTables = [:]
        Map usedLMTables = [:]
        dataJson = dataJson.replace("\\","\\\\")
        Map dataMap = MiscUtil.parseJsonText(dataJson)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String result = ""
        String addSubmissionDateFilter
        for (int i = 0; i < containerGroupsList.size(); i++) {
            if ((allMap.keyword) && (i > 0)) {
                result += " ${allMap.keyword} "
            }

            def executed = buildCriteriaFromGroup(containerGroupsList[i], nextRunDate, reportFields, timezone,
                    lmTables, usedLMTables, blanks, reassessIndex , evaluateCaseDateEnum,asOfCaseVersionDate, locale)

            //PVR-2771: Handling of Submission Query Level case having date range
            addSubmissionDateFilter = ""
            if (templateQuery && !forNotValidCase && (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION)
                    && (templateQuery.usedConfiguration?.dateRangeType?.name == DateRangeTypeCaseEnum.SUBMISSION_DATE.value())) {
                boolean hasCumulative= false
                if (templateQuery.usedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                    DataTabulationTemplate template = templateQuery.usedTemplate as DataTabulationTemplate
                    hasCumulative = template.hasCumulative()
                }
                Date startDate = templateQuery?.usedDateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[0]
                Date endDate = templateQuery?.usedDateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[1]
                if (hasCumulative) {
                    startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY)
                }

                addSubmissionDateFilter = " AND trunc(cmr.DATE_SUBMISSION) >=   trunc(TO_DATE('${startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))\n" +
                        "            AND trunc(cmr.DATE_SUBMISSION)   <  trunc(TO_DATE( '${endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}')) +1"

            }

            result += "(${executed.result}${addSubmissionDateFilter})"

            executed.reportFields.each {
                if (!reportFields.contains(it)) {
                    reportFields.add(it)
                }
            }
        }
        return [result: result, reportFields: reportFields, lmTables: lmTables, usedLMTables: usedLMTables]
    }

    // helper method, don't call this
    private def buildCriteriaFromGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Date nextRunDate, List<ReportField> fields, String timezone, LinkedHashMap lmTables, LinkedHashMap usedLMTables,
            List<QueryExpressionValue> blanks,
            int reassessIndex, EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate, Locale locale) {

        String result = ""
        List<ReportField> reportFields = fields;
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    if (result) {
                        result += " ${groupMap.keyword} "
                    }
                }
                def executed = buildCriteriaFromGroup(expressionsList[i],
                        nextRunDate, fields, timezone, lmTables, usedLMTables, blanks, reassessIndex, evaluateCaseDateEnum, asOfCaseVersionDate, locale)

                if (executed.result == '1=1' || executed.result == '(1=1)') {
                    def logicalAppender = "${groupMap.keyword}"
                    if (logicalAppender != "or") {
                        result += "(${executed.result})";
                    } else {

                        def orClauseKeyword = " or "

                        //A check to determine of result has 'or' and this 'or' keyword is in the last.
                        if (result.indexOf(orClauseKeyword) != -1 && result?.trim().split(orClauseKeyword)?.size() == 1) {
                            result = result.split("or")[0]
                        }
                    }
                } else {
                    result += "(${executed.result})";
                }

                executed.reportFields.each() {
                    if (!reportFields.contains(it)) {
                        reportFields.add(it)
                    }
                }
            }
        } else {
            if (groupMap.keyword) {
                result += " ${groupMap.keyword} "
            }

            ReportField reportField = ReportField.findByNameAndIsDeleted(groupMap.field,false)
            // Extra Values
            HashMap extraValues = [:]
            // Re-assess Listedness
            if (groupMap.containsKey(RLDS)) {
                extraValues.put(RLDS, groupMap.get(RLDS))
            }
            result += convertExpressionToWhereClause(
                    new Expression(reportField: reportField, value: groupMap.value,
                            operator: groupMap.op as QueryOperatorEnum),
                    nextRunDate, timezone, lmTables, usedLMTables, blanks, extraValues, reassessIndex, evaluateCaseDateEnum, asOfCaseVersionDate, locale);
            if (!reportFields.contains(reportField)) {
                reportFields.add(reportField)
            }
        }
        return [result: result, reportFields: reportFields, lmTables: lmTables]
    }

    private String convertExpressionToWhereClause(Expression e, Date nextRunDate, String timezone, LinkedHashMap lmTables,
                                                  LinkedHashMap usedLMTables, List<QueryExpressionValue> blanks,
                                                  HashMap extraValues, int reassessIndex, EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate, Locale locale) {
        String result = ""
        String columnName = ""

        if (e.value == null || e.value.equals("") || e.value?.matches(Constants.POI_INPUT_PATTERN_REGEX)) {
            // Parameters are stored in a list, which is the order they are accessed in.
            ParameterValue parameterValue = blanks ?blanks?.get(0):null

            if (parameterValue) {
                if (parameterValue.value) {
                    e.value = parameterValue.value
                } else {
                    // PVR-1628: If no value is selected for parameter in parametrized query in report configuration then all values should be returned
                    e.value = NO_VALUE_SELECTED
                }
                blanks.remove(parameterValue)
            } else {
                //If no parameter value is selected then we set value NO_VALUE_SELECTED
                e.value = NO_VALUE_SELECTED
            }
        }

        if (e.reportField.dataType == PartialDate.class) {
            result = generatePartialDateWhereClause(e, nextRunDate, timezone, locale)
            return result
        }

        SourceColumnMaster sourceColumn = e.reportField.getSourceColumn(locale)
        if (sourceColumn.lmDecodeColumn) {
            int dynamicAlias = 0
            if (lmTables.containsKey(sourceColumn.lmTableName.tableAlias)) {
                dynamicAlias = (int) lmTables.getAt(sourceColumn.lmTableName.tableAlias)
            }
            if (!usedLMTables.containsKey(sourceColumn.reportItem)) {
                dynamicAlias++
                usedLMTables[(sourceColumn.reportItem)] = dynamicAlias
                lmTables[(sourceColumn.lmTableName.tableAlias)] = dynamicAlias
            }
            columnName = "${sourceColumn.lmTableName.tableAlias}_${dynamicAlias}.${sourceColumn.lmDecodeColumn}"
        } else {
            columnName = "${sourceColumn.tableName.tableAlias}.${sourceColumn.columnName}"
        }

        boolean isClobColumn = false
        if (sourceColumn.columnType == 'C') {
//            columnName = "dbms_lob.substr(${columnName},4000,1)"
            isClobColumn = true
        }

        // PVR-1355 Akash
        // Re-assess Listedness needs to be completed in this method.
        if (!extraValues.isEmpty()) {
            String RLDSValue = extraValues.get(RLDS)
            if (RLDSValue) {
                /*
                    Add code here to process Re-assess Listedness Datasheet.

                    e.value is the value of the listedness (i.e. Listed, Unlisted, Unknown).

                    RLDSValue is the value of the datasheet (i.e. JPN, CCDS, C1).

                    RLDSValue cannot be blank.

                    Store the string in result, which is returned.
                 */

                if (e.operator == QueryOperatorEnum.IS_EMPTY) {
                    result = "${columnName} IS NULL"
                } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
                    result = "${columnName} IS NOT NULL"
                } else if (e.reportField.isString()) {
                    //First, check our custom operator. String comparisons are case insensitive.
                    String listednessList = ""
                    List tokens1 = e.normalizeValue.split(/;/) as List
                    tokens1.eachWithIndex { it, index ->
                        listednessList += "UPPER(\'${it}\'),"
                    }

                    String datasheetList = ""
                    List tokens2 = RLDSValue.split(/;/) as List
                    tokens2.eachWithIndex { it, index ->
                        datasheetList += "UPPER(\'${it}\'),"
                    }

                    if (e.operator == QueryOperatorEnum.EQUALS) {
                        result = "UPPER(${columnName}) in (${listednessList.substring(0, listednessList.length() - 1)})"
                        // check listedness
                        result += " and UPPER(gdrq.datasheet_name) in (${datasheetList.substring(0, datasheetList.length() - 1)})"
                        // check datasheet
                        result += " and gdrq.row_num = ${reassessIndex}" // check sequence number

                    } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                        result = "UPPER(${columnName}) not in (${listednessList.substring(0, listednessList.length() - 1)})"
                        // check listedness
                        result += " and UPPER(gdrq.datasheet_name) not in (${datasheetList.substring(0, datasheetList.length() - 1)})"
                        // check datasheet
                        result += " and gdrq.row_num = ${reassessIndex}" // check sequence number

                    } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                        result = "UPPER(${columnName}) LIKE UPPER('%${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.ADVANCE_CONTAINS) {
                        result = " REGEXP_LIKE(UPPER(${columnName}) , '${e.normalizeValue}') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.START_WITH) {
                        result = "UPPER(${columnName}) LIKE UPPER('${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                        result = "UPPER(${columnName}) LIKE UPPER('%${e.normalizeValue}') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.normalizeValue}') AND ${columnName} IS NOT NULL"
                    }
                }

                return result
            }
        }

        if (e.value == NO_VALUE_SELECTED) {
            result = "1=1" // ignore that bit of query
        } else if (e.operator == QueryOperatorEnum.IS_EMPTY) {
            result = "${columnName} IS NULL"
        } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
            result = "${columnName} IS NOT NULL"
        } else if (e.reportField.isString()) {
            //First, check our custom operator. String comparisons are case insensitive.
            if (e.operator == QueryOperatorEnum.EQUALS) {
                //Second, check if we have multiselect
                if (e?.value?.indexOf(";") == -1) {
                    if (isClobColumn) {
                        result = "dbms_lob.getlength(${columnName}) = dbms_lob.getlength('${e.normalizeValue}') and dbms_lob.instr(UPPER(${columnName}),UPPER('${e.normalizeValue}'),1,1) > 0"
                    } else {
                        result = "UPPER(${columnName}) = UPPER('${e.normalizeValue}')"
                        if (e.reportField.name == 'masterStateId' && e.value.toLowerCase() == "deleted") {
                            result += excludeDeletedCasesForQueryFilter(evaluateCaseDateEnum, asOfCaseVersionDate)
                        }
                    }
                } else {

                    //Multiselect Select2
                    List tokens = e?.normalizeValue?.split(/;/) as List
                    StringBuilder values = new StringBuilder()

                    if (isClobColumn) {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
//                                values += " OR UPPER(${inColumnName})=UPPER('${it}')"
                                values.append(" OR dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) > 0")
                            } else {
                                values.append(" dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) > 0")
                            }
                        }

//                        inColumnName = "dbms_lob.substr(${inColumnName},4000,1)"
                    } else {
                        for(int i=0; i< tokens.size(); i++) {
                            if (i > 0) {
                                values.append(" OR UPPER(${columnName}) = UPPER('${tokens[i]}')")
                            } else {
                                values.append("UPPER(${columnName}) = UPPER('${tokens[i]}')")
                            }
                        }
                    }

                    result += values
                }
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                //Second, check if we have multiselect
                if (e.value.indexOf(";") == -1) {
                    if (isClobColumn) {
                        result = "dbms_lob.instr(UPPER(${columnName}),UPPER('${e.normalizeValue}'),1,1) = 0"
                    } else {
                        result = "UPPER(${columnName}) <> UPPER('${e.normalizeValue}')"
                    }
                } else {

                    //Multiselect Select2
                    String[] tokens = e?.normalizeValue?.split(/;/)
                    StringBuilder values = new StringBuilder()

                    if (isClobColumn) {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
//                                values += " OR UPPER(${inColumnName})=UPPER('${it}')"
                                values.append(" AND dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) = 0")
                            } else {
                                values.append(" dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) = 0")
                            }
                        }

//                        inColumnName = "dbms_lob.substr(${inColumnName},4000,1)"
                    } else {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
                                values.append(" AND UPPER(${columnName}) <> UPPER('${it}')")
                            } else {
                                values.append("UPPER(${columnName}) <> UPPER('${it}')")
                            }
                        }
                    }

                    result += values
                }
            } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                result = "UPPER(${columnName}) LIKE UPPER('%${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.ADVANCE_CONTAINS) {
                result = " REGEXP_LIKE(UPPER(${columnName}), '${e.normalizeValue}') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.START_WITH) {
                result = "UPPER(${columnName}) LIKE UPPER('${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                result = "UPPER(${columnName}) NOT LIKE UPPER('${e.normalizeValue}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                result = "UPPER(${columnName}) LIKE UPPER('%${e.normalizeValue}') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.normalizeValue}') AND ${columnName} IS NOT NULL"
            }
        } else if (e.reportField.isNumber()) {
            // TODO: Numbers can have multiple values?
            List operatorIgnoreList = [QueryOperatorEnum.IS_EMPTY, QueryOperatorEnum.IS_NOT_EMPTY, QueryOperatorEnum.EQUALS, QueryOperatorEnum.NOT_EQUAL]
            result = "${columnName} ${e.operator.value()} ${e.normalizeValue} ${!(e.operator in operatorIgnoreList) ? 'AND ' + columnName + ' IS NOT NULL' : ''}"
        } else if (e.reportField.isDate()) {
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName,locale)
        }
        return result
    }

    String generatePartialDateWhereClause(Expression e, Date nextRunDate, String timezone,Locale locale) {
        String result = ""
        SourceColumnMaster sourceColumn = e.reportField.getSourceColumn(locale)
        String columnName = "$sourceColumn.tableName.tableAlias.$sourceColumn.columnName"
        if (e.value.matches(PARTIAL_DATE_YEAR_ONLY)) { //??-???-yyyy
            String monthAndYear = e.value.substring(6)
            String startDate = "01-JAN${monthAndYear}"
            def startDates
            def endDates

            SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
            Date convertedStartDate = dateFormat.parse(startDate)
            Calendar c = Calendar.getInstance();
            c.setTime(convertedStartDate)
            c.set(Calendar.MONTH, Calendar.DECEMBER)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date convertedEndDate = c.time

            if (e.operator == QueryOperatorEnum.EQUALS) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """  ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                              ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA') """
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """ NOT (${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                                  ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')) """
            } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} < TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} > TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                    e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA') AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                    e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.TOMORROW || e.operator == QueryOperatorEnum.NEXT_MONTH ||
                    e.operator == QueryOperatorEnum.NEXT_WEEK || e.operator == QueryOperatorEnum.NEXT_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA') AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.NEXT_X_DAYS || e.operator == QueryOperatorEnum.NEXT_X_MONTHS ||
                    e.operator == QueryOperatorEnum.NEXT_X_WEEKS || e.operator == QueryOperatorEnum.NEXT_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            }
        } else if (e.value.matches(PARTIAL_DATE_MONTH_AND_YEAR)) { //??-MMM-yyyy
            String monthAndYear = e.value.substring(2)
            String startDate = "01${monthAndYear}"
            def startDates
            def endDates

            SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
            Date convertedStartDate = dateFormat.parse(startDate)
            Calendar c = Calendar.getInstance();
            c.setTime(convertedStartDate)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date convertedEndDate = c.time

            if (e.operator == QueryOperatorEnum.EQUALS) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA') """
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """NOT (${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                                 ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA'))"""
            } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} < TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} > TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                    e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                            ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                    e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                            ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.TOMORROW || e.operator == QueryOperatorEnum.NEXT_MONTH ||
                    e.operator == QueryOperatorEnum.NEXT_WEEK || e.operator == QueryOperatorEnum.NEXT_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                            ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.NEXT_X_DAYS || e.operator == QueryOperatorEnum.NEXT_X_MONTHS ||
                    e.operator == QueryOperatorEnum.NEXT_X_WEEKS || e.operator == QueryOperatorEnum.NEXT_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                            ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            }
        } else if (e.value.matches(PARTIAL_DATE_FULL)) { //dd-MMM-yyyy
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName,locale)
        }
        return result
    }

    String generateDateWhereClause(Expression e, Date nextRunDate, String timezone, String columnName,Locale locale) {
        String result = ""
        def dates
        if (e.operator == QueryOperatorEnum.EQUALS) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)) {
                dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')"""
            } else {
                result = """${columnName} = ${e.value}"""
            }

        } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)) {
                dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
                result = """NOT (${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA'))"""
            } else {
                result = """NOT (${columnName} = ${e.value})"""
            }

        } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
                result = "${columnName} < TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else {
                result = """ ${columnName} < ${e.value}"""
            }

        } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
                result = "${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else {
                result = """ ${columnName} <= ${e.value}"""
            }

        } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
                result = "${columnName} > TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else {
                result = """ ${columnName} > ${e.value}"""
            }

        } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
                result = "${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else {
                result = """ ${columnName} >= ${e.value}"""
            }

        } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
            dates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
            result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${
                columnName
            } IS NOT NULL"""
        } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_X_DAYS) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                def days = 1
                if (e.operator == QueryOperatorEnum.LAST_X_DAYS) {
                    days = Integer.parseInt(e.value)
                }
                dates = RelativeDateConverter.lastXDaysDates(nextRunDate, days, timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${
                    columnName
                } IS NOT NULL"""
            } else {
                String op = messageSource.getMessage(e.operator.i18nKey, null, locale)
                if (e.value) {
                    result = "${columnName} = " + op.replace('X', e.value)
                } else {
                    result = "${columnName} = " + op
                }
            }

        } else if (e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                dates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else {
                String op = messageSource.getMessage(e.operator.i18nKey, null, locale)
                if (e.value) {
                    result = "${columnName} = " + op.replace('X', e.value)
                } else {
                    result = "${columnName} = " + op
                }
            }

        }  else if (e.operator == QueryOperatorEnum.TOMORROW || e.operator == QueryOperatorEnum.NEXT_MONTH ||
                e.operator == QueryOperatorEnum.NEXT_WEEK || e.operator == QueryOperatorEnum.NEXT_YEAR) {
            dates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
            result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${
                columnName
            } IS NOT NULL"""
        } else if (e.operator == QueryOperatorEnum.TOMORROW || e.operator == QueryOperatorEnum.NEXT_X_DAYS) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                def days = 1
                if (e.operator == QueryOperatorEnum.NEXT_X_DAYS) {
                    days = Integer.parseInt(e.value)
                }
                dates = RelativeDateConverter.nextXDaysDates(nextRunDate, days, timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${
                    columnName
                } IS NOT NULL"""
            } else {
                String op = messageSource.getMessage(e.operator.i18nKey, null, locale)
                if (e.value) {
                    result = "${columnName} = " + op.replace('X', e.value)
                } else {
                    result = "${columnName} = " + op
                }
            }

        } else if (e.operator == QueryOperatorEnum.NEXT_X_MONTHS ||
                e.operator == QueryOperatorEnum.NEXT_X_WEEKS || e.operator == QueryOperatorEnum.NEXT_X_YEARS) {
            if(e.value?.matches(Constants.NUMBER_INPUT_PATTERN_REGEX)){
                dates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else {
                String op = messageSource.getMessage(e.operator.i18nKey, null, locale)
                if (e.value) {
                    result = "${columnName} = " + op.replace('X', e.value)
                } else {
                    result = "${columnName} = " + op
                }
            }
        }
        return result
    }

    String convertDateToSQLDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FMT)
        return sdf.format(date)
    }

    // Generate the SQL which represents the Query filters. The SQL will generate a "filteredCaseList temp table" The
    // "filteredCaseList temp table" will become an input into the next step ( generateReportSQL ).
    public String generateQuerySQL(ExecutorDTO executorDTO, SuperQuery query, boolean materialize,
                                   boolean forNotValidCase, int reassessIndex) {

        def executed
        def selectFields = ""

        if (executorDTO.productSelection) {
            selectFields += " cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num, "
        }

        if (executorDTO.productSelection && executorDTO.studySelection) {
            selectFields += " cp.modified_product_id prod_exp_id, "
        }

        if ((executorDTO.usedEventSelection)) {
            // query must pass case id, product sequence number and product event sequence number (PVR-117)
            selectFields += " ce.AE_REC_NUM,"
        }

        // todo: check query set blanks, if no, create blanks
        List<QueryExpressionValue> blanks = []
        executorDTO?.globalQueryValueLists?.each {globalQueryList->
            globalQueryList.parameterValues.each {parmeterValue->
                if (parmeterValue.hasProperty('reportField')) {
                    blanks.add(parmeterValue)
                }
            }
        }

        if (query?.JSONQuery) {
            executed = buildFilterSQLFromJSON(query.JSONQuery, executorDTO.nextRunDate,"UTC", blanks as List<QueryExpressionValue>,
                    reassessIndex, executorDTO.evaluateDateAs,executorDTO.asOfVersionDate, executorDTO.locale)
        }

        LinkedHashMap usedLMTables = executed.usedLMTables
        String extraLMJoins = ""
        int lmTableAlias = -1
        Locale locale = executorDTO.locale

        usedLMTables.each {
            ReportField rf = ReportField.findBySourceColumnIdAndIsDeleted(it.key, false)
            lmTableAlias = (int) it.value
            String enterpriseCheck = ""
            SourceColumnMaster sourceColumn = rf.getSourceColumn(locale)
            if (!CUSTOM_LM_TABLE_ALIASES.contains(sourceColumn.lmTableName.tableAlias) && (sourceColumn.lmTableName.hasEnterpriseId == 1)) {
                enterpriseCheck = """ AND ${sourceColumn.tableName.tableAlias}.tenant_id
                      = ${sourceColumn.lmTableName.tableAlias}_${lmTableAlias}.tenant_id"""
            }
            if (query?.queryType == QueryTypeEnum.QUERY_BUILDER && query?.reassessListedness) { // For re-assess listedness field
                extraLMJoins += """ LEFT JOIN VW_LLIST_LISTEDNESS llist_1
                                    ON  ( gdrq.LISTEDNESS = llist_1.LISTEDNESS_ID
                                    AND gdrq.tenant_id = llist_1.tenant_id) """
            } else {
                extraLMJoins += """
                ${sourceColumn.lmJoinType == "O" ? " LEFT " : ""}
                JOIN ${sourceColumn.lmTableName.tableName} ${sourceColumn.lmTableName.tableAlias}_${lmTableAlias}
                ON  (${sourceColumn.tableName.tableAlias}.${sourceColumn.columnName}
                      = ${sourceColumn.lmTableName.tableAlias}_${lmTableAlias}.${sourceColumn.lmJoinColumn}${
                    enterpriseCheck
                })"""
            }
        }
        String fromClause = buildFromClauseFromReportFields(executed.reportFields, query, executorDTO, forNotValidCase)

        //TODO: get prod_rec_num and event_seq_num when we implement product/event level queries.
        // return """select cm.tenant_id, cm.case_id, max(cm.version_num) version_num, cp.prod_rec_num prod_rec_num, ce.AE_REC_NUM event_seq_num
        // from ${buildFromClauseFromReportFields(executed.reportFields)}
        // where ${executed.result} group by cm.tenant_id, cm.case_id, cp.prod_rec_num, ce.AE_REC_NUM"""

        String selectClause = """select ${materialize ? "/*+ MATERIALIZE */ " : ""}cm.tenant_id, cm.case_id,$selectFields cm.version_num"""
        if (forNotValidCase) {
            selectClause = "select 1"
        }

        return """${selectClause} from ${fromClause} ${extraLMJoins} where ${executed.result} """
    }


    public String processDictionariesWithDLPRev(ExecutorDTO executorDTO) {
        Boolean excludeFollowUp = executorDTO.excludeFollowUp
        String initialDateFilterCol = ReportField.findByName(executorDTO.dateRangeType.name)?.getSourceColumn(executorDTO.locale)?.columnName
        String dateCheckSql = ""
        if (excludeFollowUp) {
            dateCheckSql = """where exists ( select 1 from CDR_DATES dc where dc.tenant_id = t2.tenant_id and
                    dc.VERSION_NUM = t2.VERSION_NUM and dc.case_id = t2.case_id and dc.seq_num =0
                     ${executorDTO.startDate ? "and  dc.${initialDateFilterCol}  >= trunc(TO_DATE( '${executorDTO.startDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))" :""}
                     ${executorDTO.endDate ? "and  dc.${initialDateFilterCol}  <  trunc(TO_DATE( '${executorDTO.endDate.format(DATE_FMT)}', '${DATETIME_FMT_ORA}'))+1)" :""}"""
        }

        String versionSql = " Select t2.tenant_id ,t2.CASE_ID ,t2.VERSION_NUM from $VERSION_BASE_TABLE_NAME t2 ${dateCheckSql} "

        return processDictionaries(executorDTO, versionSql, true)

    }


    // Reassess Listedness for query
    public List<String> setReassessContextForQuery(SuperQuery query, Date startDate, Date endDate) {
        List<String> procedureCall = [];
        if(query) {
            if (query.queryType == QueryTypeEnum.QUERY_BUILDER) {
                procedureCall.add(getSingleQueryProcedure(query, startDate, endDate, 0))
            } else if (query.queryType == QueryTypeEnum.SET_BUILDER) {
                QuerySet querySet = (QuerySet) query
                int reassessIndex = 0
                querySet.queries.each {
                    if (it.queryType == QueryTypeEnum.QUERY_BUILDER) {
                        procedureCall.add(getSingleQueryProcedure(it, startDate, endDate, reassessIndex))
                        if (Query.get(it.id)?.reassessListedness)
                            reassessIndex++
                    }
                }
            }
        }
        return procedureCall
    }

    private String getSingleQueryProcedure(def query, Date startDate, Date endDate, int reassessIndex) {
        String procedureCall = ""
        SuperQuery unwrappedQuery=GrailsHibernateUtil.unwrapIfProxy(query)
        ReassessListednessEnum reassessListednessEnum = unwrappedQuery.reassessListedness
        String reassessDate = ""
        if (reassessListednessEnum == ReassessListednessEnum.BEGINNING_OF_THE_REPORTING && startDate) {
            reassessDate = startDate.format(DATE_FMT)
        } else if (reassessListednessEnum == ReassessListednessEnum.END_OF_THE_REPORTING_PERIOD && endDate) {
            reassessDate = endDate.format(DATE_FMT)
        }
        String datasheet = ""
        JSONObject queryJSON = query? (JSON.parse(query?.JSONQuery)):null
        queryJSON?.all?.containerGroups?.expressions?.flatten()?.each { expression ->
            if (expression.field == "dvListednessReassessQuery") {
                datasheet += expression.RLDS + "," + (expression.RLDS_OPDS == "true" ? 1 : 0) + ","
            }
        }

        if (datasheet.length() > 0) {
            datasheet = datasheet.substring(0, datasheet.length() - 1)
            procedureCall = """{call pkg_reassess_listedness.p_query(${reassessIndex},'${datasheet}','${reassessDate}')}"""
        }

        return  procedureCall
    }



    public String generateSetSQL(ExecutorDTO executorDTO, SuperQuery query) {
        def executed

        def selectFields = ","

        if (query?.JSONQuery) {
            executed = buildSetSQLFromJSON(executorDTO,query)
        }

        String withClause = buildWithSQLFromQueries(executorDTO, executed.tempTables)

        return """WITH ${withClause} ${executed.result}"""
    }





    public String generateCustomQuerySQL(ExecutorDTO executorDTO, SuperQuery query) {

        String result = "select cm.tenant_id, cm.case_id, cm.version_num"

        if ( executorDTO.productSelection) {
            result += ", cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num"
        } else if (executorDTO.studySelection) {
            // We do not need any extra tables here
        }
        if ((executorDTO.usedEventSelection)) {
            result += ", ce.ae_rec_num"
        }


        result += " from C_IDENTIFICATION cm"

        if (executorDTO.productSelection) {
            result += " join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num AND cm.tenant_id = cp.tenant_id)"
        } else if (executorDTO.studySelection) {
            // We do not need any extra tables here
        }
        if ((executorDTO.usedEventSelection)) {
            result += " join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.tenant_id = ce.tenant_id)"
        }

        // @TODO this is not safe to call, customSQLQuery ONLY exists on CustomSQLQuery class
        String sqlQuery = query?.queryType == QueryTypeEnum.CUSTOM_SQL ? query.customSQLQuery:""

        // Parameters
        if (query?.hasBlanks) {
            Map<String, String> parameterMap = [:]
            executorDTO.globalQueryValueLists?.each {
                it.parameterValues?.each {
                    parameterMap.put(it.key, it.value)
                }
            }
            sqlQuery = replaceMapInString(sqlQuery, parameterMap)
        }

        return """$result join $VERSION_TABLE_NAME ver
                    on (cm.case_id = ver.case_id and cm.version_num = ver.version_num) ${sqlQuery}"""
    }

    public String generateEmptyQuerySQL(ExecutorDTO executorDTO) {
        String result = "select cm.tenant_id, cm.case_id, cm.version_num"
        if (executorDTO.productSelection) {
            result += ", cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num"
        } else if (executorDTO.studySelection) {
            // We do not need any extra tables here
        }
        if ((executorDTO.usedEventSelection)) {
            result += ", ce.AE_REC_NUM"
        }

//        Fix for handling both Product and Studay Selection together
        if(executorDTO.productSelection && executorDTO.studySelection){
            result +=" , cp.modified_product_id prod_exp_id "
        }

        result += " from V_C_IDENTIFICATION cm"

        if (executorDTO.productSelection) {
            String productFilter = ""
            //        Fix for handling both Product and Studay Selection together
            if (executorDTO.productSelection && !executorDTO.studySelection) {
                String str = """(cp.modified_product_id IN ( SELECT product_id FROM ("""
                productFilter = " AND " + appendProductFilterInfo(executorDTO, str) + ")))"
            }

            result += """ join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num
                    AND cm.tenant_id = cp.tenant_id ${productFilter})"""
        } else if (executorDTO.studySelection) {
            // We do not need any extra tables here
        }
        if ((executorDTO.usedEventSelection)) {
            result += " join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.tenant_id = ce.tenant_id)"
        }
        return """$result join $VERSION_TABLE_NAME ver
                    on (cm.case_id = ver.case_id and cm.version_num = ver.version_num)"""
    }


    // Temporarily exposed for testing in building SQL from our JSON representation of Expressions & ExpressionGroups
    private String buildFromClauseFromReportFields(def selectedFields, SuperQuery query, ExecutorDTO executorDTO, boolean forNotValidCase) {
        String lmTableJoins = ""
        boolean addedVersioning = false
        if (!forNotValidCase) {
            if ( executorDTO.productSelection) {
                selectedFields.addAll([ReportField.findByNameAndIsDeleted("productProductName",false)])
            } else if (executorDTO.studySelection) {
                // We do not need any extra tables here
            }
            if ((executorDTO.usedEventSelection)) {
                selectedFields.addAll([ReportField.findByNameAndIsDeleted("eventPrefTerm", false)])
            }
        }

        //Make sure case master is always included
        String caseNumberFieldName = executorDTO.sourceProfile?.caseNumberFieldName ?: "masterCaseNum"
        selectedFields.add([ReportField.findByNameAndIsDeleted(caseNumberFieldName, false)])

        def tableName
        def tempTableNames = []
        String caseTableFromClause = ""

        //find table list
        // QueryLevel
        selectedFields.each { ReportField rf ->
            SourceColumnMaster sourceColumn = rf.getSourceColumn(executorDTO.locale)
            if (tableName != sourceColumn.tableName.tableName) {
                tempTableNames.add(sourceColumn.tableName.tableName)
                tableName = sourceColumn.tableName.tableName
            }
        }

        // construct SQL after finding relation between case tables
        def Integer loopCounter = 0
        boolean recursiveFlag = true

        while (recursiveFlag && loopCounter < 5) {
            def relTableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def relCaseTableRelation = relTableJoinMapping.list {
                inList("tableName.tableName", tempTableNames)
                order("mapTableName.tableName", "asc")
            }
            recursiveFlag = false
            relCaseTableRelation.each { CaseColumnJoinMapping rf ->

                if (tableName == rf.mapTableName.tableName) {
                    if (!tempTableNames.contains(rf.mapTableName.tableName)) {
                        tempTableNames.add(rf.mapTableName.tableName)
                        recursiveFlag = true
                    }
                }
                tableName = rf.mapTableName.tableName

            }
            loopCounter++
        }

        loopCounter = 0
        // sort tables in join order
        def tableJoinOrder = SourceTableMaster.createCriteria()
        def tableList = tableJoinOrder.list {
            inList("tableName", tempTableNames)
            order("caseJoinOrder", "asc")
        }

        tableList.each { SourceTableMaster tabRec ->
            if (tabRec.tableName != "GTT_DATASHEET_REASSESS") { // For re-assess listedness, don't join
                def tableJoinMapping = CaseColumnJoinMapping.createCriteria()
                def caseTableRelation = tableJoinMapping.list {
                    inList("mapTableName.tableName", tempTableNames)
                    eq("tableName.tableName", tabRec.tableName)
                    order("mapColumnName", "asc")
                }
                if (loopCounter > 0) {
                    caseTableFromClause += (tabRec.caseJoinType == "O" ? " Left " : "") + " join "
                }

                caseTableFromClause += tabRec.tableName + " " + tabRec.tableAlias
                if (caseTableRelation.size() > 0) {
                    caseTableFromClause += " on ("
                }
                def int iterations = 0

                caseTableRelation.each { CaseColumnJoinMapping rf ->
                    if (iterations > 0) {
                        caseTableFromClause += " AND "
                    }
                    iterations++
                    caseTableFromClause += rf.mapTableName.tableAlias + "." + rf.mapColumnName + " = " + rf.tableName.tableAlias + "." + rf.columnName
                    if (rf.tableName.versionedData == "V" && rf.mapTableName.versionedData == "V") {
                        caseTableFromClause += " AND " + rf.mapTableName.tableAlias + ".version_num = " + rf.tableName.tableAlias + ".version_num"
                    }
                    if (!CUSTOM_LM_TABLE_ALIASES.contains(rf.mapTableName.tableAlias)) {
                        caseTableFromClause += " AND " + rf.mapTableName.tableAlias + ".tenant_id = " + rf.tableName.tableAlias + ".tenant_id"
                    }

//        Fix for handling both Product and Studay Selection together
                    if (rf.tableName.tableAlias == "cp" && executorDTO.productSelection && !executorDTO.studySelection) {
                        String str = """(cp.modified_product_id IN ( SELECT product_id FROM ("""
                        caseTableFromClause += " AND " + appendProductFilterInfo(executorDTO, str) + ")))"
                    }

                }
                if (caseTableRelation.size() > 0) {
                    caseTableFromClause += " ) "
                }
                loopCounter++

                if (!addedVersioning && tabRec.tableName == "V_C_IDENTIFICATION") {
                    caseTableFromClause += """ join $VERSION_TABLE_NAME ver
                                            on (cm.case_id = ver.case_id and
                                                cm.version_num = ver.version_num) """
                    addedVersioning = true
                }
            }
        }

        // Concatenating select + from  clause to construct compilable SQL.
        return caseTableFromClause + lmTableJoins
    }

    public def buildSetSQLFromJSON(ExecutorDTO executorDTO, SuperQuery query) {
        Map tempTables = [:]
        Map dataMap = query? MiscUtil.parseJsonText(query.JSONQuery):[:]
        Map allMap = dataMap.all
        List containerGroupsList = allMap?.containerGroups?:[]
        String result = ""
        for (int i = 0; i < containerGroupsList.size(); i++) {
            if ((allMap.keyword) && (i > 0)) {
                result += " ${allMap.keyword} "
            }
            def executed = buildSetCriteriaFromGroup(containerGroupsList[i] as Map, tempTables, executorDTO)
            result += "(${executed.result})"
        }
        return [result: result, tempTables: tempTables]
    }




    //helper method, don't call this
    private def buildSetCriteriaFromGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Map tempTables, ExecutorDTO executorDTO) {
        String result = ""
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    result += " ${groupMap.keyword} "
                }
                def executed = buildSetCriteriaFromGroup(expressionsList[i] as Map, tempTables,executorDTO)
                result += "(${executed.result})";
            }
        } else {
            if (groupMap.keyword) {
                result += " ${groupMap.keyword} "
            }
            String index = ""
            if (tempTables.containsKey(groupMap.query)) {
                index = tempTables.get(groupMap.query)
            } else {
                index = groupMap.index
                tempTables.put(groupMap.query, index)
            }
            result += convertQueryToWhereClause(index,executorDTO, null)
        }
        return [result: result, tempTables: tempTables]
    }

    public String excludeNonValidCases(SuperQuery nonValidQuery, ExecutorDTO executorDTO) {
        String nonValidCasesSQL
        if(nonValidQuery.queryType == QueryTypeEnum.SET_BUILDER){
            nonValidCasesSQL=generateSetSQL(executorDTO,nonValidQuery)
        }
        else {
            nonValidCasesSQL = generateQuerySQL(executorDTO, nonValidQuery, false, true, 0)
        }
        return """ NOT EXISTS ($nonValidCasesSQL AND cm.case_id = query.case_id AND cm.version_num = query.version_num
                   AND cm.tenant_id = query.tenant_id)"""
    }

    //Exclude Deleted Cases for ExecutorDTO
    public String excludeDeletedCases(SuperQuery deletedQuery, ExecutorDTO executorDTO) {
        String deletedCasesSQL = generateQuerySQL(executorDTO, deletedQuery, false, true, 0)
        return """ NOT EXISTS ($deletedCasesSQL AND cm.case_id = query.case_id AND cm.version_num = query.version_num
                   AND cm.tenant_id = query.tenant_id)"""
    }

    // For QuerySet
    private String buildWithSQLFromQueries(ExecutorDTO executorDTO, LinkedHashMap tempTables) {
        String result = ""

        int reassessIndex = 0
        tempTables.eachWithIndex { elem, index ->
            if (index > 0) {
                result += ", "
            }

            SuperQuery query = SuperQuery.get(elem.key)

            switch (query?.queryType) {
                case QueryTypeEnum.QUERY_BUILDER:
                    result += "${SET_TABLE_NAME}_${elem.value} AS (${generateQuerySQL(executorDTO, query, true, false, reassessIndex)})"
                    if (query.reassessListedness) {
                        reassessIndex++
                    }
                    break;
                case QueryTypeEnum.CUSTOM_SQL:
                    result += "${SET_TABLE_NAME}_${elem.value} AS (${generateCustomQuerySQL(executorDTO, query)})"
                    break;
                default:
                    throw SQLException
                    break;
            }
        }

        return result
    }

    public boolean isVoidedFlagOn(BaseTemplateQuery tempQuery, Locale locale) {
        // check for template set and any of it contains voided -> true
        if (tempQuery.usedTemplate?.templateType == TemplateTypeEnum.TEMPLATE_SET){
            boolean isAnyVoided = tempQuery.usedTemplate.nestedTemplates.any {
                isVoidedInTemplate(it, locale)
            }
            if (isAnyVoided) return true
        } else {
            if (isVoidedInTemplate(tempQuery.usedTemplate, locale)) return true;
        }

        // Check for Query
        if (tempQuery.usedQuery?.queryType == QueryTypeEnum.SET_BUILDER){
            boolean isAnyVoided = tempQuery.usedQuery.queries.any {
                isVoidedInQuery(it)
            }
            if (isAnyVoided) return true
        } else {
            if (isVoidedInQuery(tempQuery.usedQuery)) return true;
        }
        return false
    }

    public boolean isVoidedInTemplate(ReportTemplate template, Locale locale){
        if (template){
            if (template.templateType == TemplateTypeEnum.CASE_LINE || template.templateType == TemplateTypeEnum.DATA_TAB){
                // Check for Template - Field
                if (template.getAllSelectedFieldsInfo()*.reportField?.any {
                    it?.getSourceColumn(locale)?.reportItem == VOIDED_SOURCE_COLUMN
                }) return true

                // Check for Template - Where
                if (template.JSONQuery?.contains(REPORTS_VOIDED_FIELD)){
                    return true
                }
            } else if (template.templateType == TemplateTypeEnum.CUSTOM_SQL){
                if (template?.customSQLTemplateSelectFrom?.contains(VOIDED_JAVA_VARIABLE) || template?.customSQLTemplateWhere?.contains(VOIDED_JAVA_VARIABLE)){
                    return true
                }
            } else if (template.templateType == TemplateTypeEnum.NON_CASE){
                if (template.getNonCaseSql()?.contains(VOIDED_JAVA_VARIABLE)) {
                    return true
                }
            }
        }
        return false
    }

    public boolean isVoidedInQuery(SuperQuery query){
        query= GrailsHibernateUtil.unwrapIfProxy(query)
        if (query){
            if (query.queryType == QueryTypeEnum.QUERY_BUILDER){
                if (query.JSONQuery?.contains(REPORTS_VOIDED_FIELD)) {
                    return true
                }
            } else if (query.queryType == QueryTypeEnum.CUSTOM_SQL){
                if (query.customSQLQuery?.contains(VOIDED_JAVA_VARIABLE)){
                    return true
                }
            }
        }
        return false
    }

    //check deleted cases for query
    public String excludeDeletedCasesForQueryFilter(EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate) {
        String deletedCasesCheckSql = ""
        if (evaluateCaseDateEnum == EvaluateCaseDateEnum.VERSION_ASOF && asOfCaseVersionDate) {
            deletedCasesCheckSql = " and exists ( select 1 from case_deleted_info cdi where cdi.case_id = cm.case_id " +
                    "and TO_DATE('${asOfCaseVersionDate.format(DATETIME_FMT)}','${DATETIME_FMT_ORA}')  >= cdi.deleted_start_date " +
                    "and TO_DATE('${asOfCaseVersionDate.format(DATETIME_FMT)}','${DATETIME_FMT_ORA}')  < cdi.deleted_end_date)  "
        }
        return deletedCasesCheckSql
    }

    private def returnJsonField(Map groupMap, def fieldList) {
        def expressionElements = groupMap?.expressions

        if (expressionElements) {
            for (int i = 0; i < expressionElements?.size(); i++) {
                def expElement = expressionElements[i]

                if (expElement.field) {
                    fieldList.add(expElement.field)
                } else {
                    returnJsonField(expElement, fieldList)
                }
            }
        }
    }

    public String selectedFieldsCustomProcedures(BaseTemplateQuery templateQuery, int execPosition) {
        ReportTemplate reportTemplate = templateQuery?.usedTemplate
        String returnString = ""
        if (execPosition == 1 || execPosition == 2) {
            String proc = ""
            List queryFields = []
            Map dataMap = templateQuery?.usedQuery?.JSONQuery? MiscUtil.parseJsonText(templateQuery?.usedQuery?.JSONQuery):[:]
            Map allMap = dataMap?.all
            List containerGroupsList = allMap?.containerGroups
            for (int i = 0; i < containerGroupsList?.size(); i++) {
                Map groupMap = containerGroupsList[i]
                returnJsonField(groupMap, queryFields)
            }

            if (execPosition == 1) {
                queryFields.each { field ->
                    proc = ReportField?.findByName(field)?.preQueryProcedure
                    if ((proc ? 1 : 0) == 1 && !returnString?.toLowerCase().contains(proc.toLowerCase())) {
                        returnString += proc + ";"
                    }
                }
            }

            if (execPosition == 2) {
                queryFields.each { field ->
                    proc = ReportField?.findByName(field)?.postQueryProcedure
                    if ((proc ? 1 : 0) == 1 && !returnString?.toLowerCase().contains(proc.toLowerCase())) {
                        returnString += proc + ";"
                    }
                }
            }

        }

        if (execPosition == 3) {
            reportTemplate?.getAllSelectedFieldsInfo()?.each { ReportFieldInfo rf ->
                if ((rf?.reportField?.preReportProcedure ? 1 : 0) == 1 && !returnString?.toLowerCase().contains(rf?.reportField?.preReportProcedure.toLowerCase())) {
                    returnString += rf.reportField.preReportProcedure + ";"
                }
            }
        }
        if (returnString) {
            returnString = "Begin " + returnString + " End;"
        }
        return returnString
    }

    public String initializeCaseSeriesGtts(ExecutorDTO executorDTO) {
        String insertStatement = ""
        insertStatement = "begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_params''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_filter_key_values''); end;';"

        String startDate = executorDTO?.startDate?.format(DATE_FMT).toString()
        String endDate = executorDTO?.endDate?.format(DATE_FMT).toString()
        String scheduledDate
        if(executorDTO.reportId){//Only for reports which are scheduled for run once in order to prevent null entry in "ScheduledDate"
            ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration?.findById(executorDTO.reportId)
            if(!executedReportConfiguration) {
                ReportConfiguration reportConfiguration = ReportConfiguration?.findById(executorDTO.reportId)
                scheduledDate = reportConfiguration?.nextRunDate?.format(DATE_FMT).toString()
                if (scheduledDate == 'null' && !reportConfiguration.isEnabled) {
                    ExecutionStatus executionStatus = ExecutionStatus.findByEntityId(reportConfiguration.id, [sort: 'dateCreated', order: 'desc'])
                    scheduledDate = executionStatus?.nextRunDate?.format(DATE_FMT)?.toString()
                }
            }
            else{
                scheduledDate = executedReportConfiguration?.nextRunDate?.format(DATE_FMT).toString()
            }
        }
        else if (executorDTO.caseSeriesId){//Only for caseseries which are scheduled for run once in order to prevent null entry in "ScheduledDate"
            ExecutedCaseSeries executedCaseSeries = ExecutedCaseSeries?.findById(executorDTO.caseSeriesId)
            if(!executedCaseSeries) {
                CaseSeries caseSeries = CaseSeries?.findById(executorDTO.caseSeriesId)
                scheduledDate = caseSeries?.nextRunDate?.format(DATE_FMT).toString()
                if (scheduledDate == 'null' && !caseSeries.isEnabled) {
                    ExecutionStatus executionStatus = ExecutionStatus.findByEntityId(caseSeries.id, [sort: 'dateCreated', order: 'desc'])
                    scheduledDate = executionStatus?.nextRunDate?.format(DATE_FMT)?.toString()
                }
            }
            else{
                scheduledDate = executedCaseSeries?.nextRunDate?.format(DATE_FMT).toString()
            }
        }
        String reportName = executorDTO?.reportName?.replaceAll("(?i)'", "''")
        int includeLockedVersion = executorDTO?.includeLockedVersion ? 1 : 0
        int includeAllStudyDrugsCases = executorDTO?.includeAllStudyDrugsCases ? 1 : 0
        int excludeFu = executorDTO?.excludeFollowUp ? 1 : 0
        SuperQuery globalQuery = GrailsHibernateUtil.unwrapIfProxy(executorDTO.globalQuery)
        int reAssesListednessFlag = (globalQuery && globalQuery?.queryType == QueryTypeEnum.QUERY_BUILDER && globalQuery?.reassessForProduct) ? 1 : 0
        String asOfVersionDate = ""
        if (executorDTO?.asOfVersionDate) {
            asOfVersionDate = executorDTO?.asOfVersionDate?.format(DATE_FMT).toString()
        }
        String dateRangeType = executorDTO?.dateRangeType?.name
        String evaluateCaseDataAs = executorDTO?.evaluateDateAs?.value()
        if (!evaluateCaseDataAs) {
            log.warn("Issue in caseseries execution for value of evaluateCaseDataAs as null for ${executorDTO?.name}. Please check UI. It shouldn't be null")
        }
        int productFilterFlag = (executorDTO?.productSelection || executorDTO?.getValidProductGroupSelection()) ? 1:0
        int studyFilterFlag = executorDTO?.studySelection ? 1 : 0
        int eventFilterFlag = (executorDTO?.usedEventSelection || executorDTO?.getUsedValidEventGroupSelection()) ? 1 : 0
        int includeCleanupVersion = grailsApplication.config?.reports?.includeDataCleanupVersion ? 1 : 0
        // configurable in application
        int supectProductCheck = executorDTO?.suspectProduct ? 1 : 0
        int limitPrimaryPath = executorDTO?.limitPrimaryPath ? 1 : 0
        int excludeNonValidCases = executorDTO?.excludeNonValidCases ? 1 : 0
        int excludeDeletedCases = executorDTO?.excludeDeletedCases ? 1 : 0
        String periodicReportTypeEnum = executorDTO?.periodicReportTypeEnum?.toString() ?: ''
        String primaryDestination = executorDTO.primaryDestination?:''
        int includeOpenCases = executorDTO.includeOpenCasesInDraft? 1:0
        int includePrevMissedCase = executorDTO.includePreviousMissingCases? 1:0
        int bVoidedFlag = globalQuery?.JSONQuery?.contains(REPORTS_VOIDED_FIELD) ? 1 : 0
        int isMultiIngredient = (executorDTO?.isMultiIngredient ? 1:0)
        int includeWHODrugs = (executorDTO?.includeWHODrugs ? 1:0)
        String configCumulativeStartDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY).format(DATE_FMT).toString()

// report Level parameters
        if (executorDTO?.dateRangeType?.name == Constants.EVENT_RECEIPT_DATE_PVR)
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_LEVEL','EVENT');"
        else
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_LEVEL','CASE');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE','${startDate}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE','${endDate}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_SCHEDULED_DATE','${scheduledDate}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_LOCKED_VERSION','${includeLockedVersion}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_STUDY_DRUGS','${includeAllStudyDrugsCases}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VERSION_ASOF_DATE','${asOfVersionDate}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATE_RANGE_TYPE','${dateRangeType}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVALUATE_DATA_ASOF','${evaluateCaseDataAs}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MEDICALLY_CONFIRMED_FLAG','0');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_FILTER_FLAG','${productFilterFlag}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('STUDY_FILTER_FLAG','${studyFilterFlag}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVENT_FILTER_FLAG','${eventFilterFlag}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_FOLLOWUP','${excludeFu}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_CLEANUP_VERSION','${includeCleanupVersion}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUSPECT_PRODUCT_CHECK','${supectProductCheck}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_PRIMARY_PATH','${limitPrimaryPath}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_NONVALID_CASES','${excludeNonValidCases}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_DELETED_CASES','${excludeDeletedCases}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VOIDED_FLAG', '${bVoidedFlag}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_TYPE','${periodicReportTypeEnum}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_FLAG','0');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRIMARY_DESTINATION_NAME','${primaryDestination}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_OPEN_CASES_IN_DRAFT','${includeOpenCases}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MIN_REPORT_START_DATE','${startDate}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MAX_REPORT_END_DATE','${endDate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_NAME','${executorDTO?.name?.replaceAll("(?i)'", "''")}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_PREVIOUSLY_MISSED_CASES','${includePrevMissedCase}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REASSESS_LISTEDNESS_FLAG','${reAssesListednessFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES  ('OBJECT_ID',${executorDTO?.caseSeriesId});"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES  ('SECTION_ID',${executorDTO?.caseSeriesId});"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SOURCE_TYPE','CENTRAL');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_CASE_SERIES','1');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PVR_CASE_SERIES_OWNER', '${Constants.PVR_CASE_SERIES_OWNER}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_MULTI_INGREDIENT', '${isMultiIngredient}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CONFIG_CUMULATIVE_START_DATE', '${configCumulativeStartDate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_WHO_DRUGS', '${includeWHODrugs}');"


        if (executorDTO?.getValidProductGroupSelection()) {
            JSON.parse(executorDTO?.getValidProductGroupSelection()).each {
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0,it.name.lastIndexOf('(') - 1)}'); "
            }
        }

        if (!executorDTO?.getValidProductGroupSelection() && productFilterFlag == 1) // Ids used in product filter
        {
            List<Map> productDetails = MiscUtil?.getProductDictionaryValues(executorDTO?.productSelection)
            List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
            productDetails.eachWithIndex { Map entry, int i ->
                int keyId = productViewsList.get(i).keyId
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,'$k','${v?.replaceAll("(?i)'", "''")}'); "
                }
            }
            if (executorDTO?.productSelection)
                JSON.parse(executorDTO?.productSelection)["100"]?.each {
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (299,'${it.id}','${it.name?.replaceAll("(?i)'", "''")}'); "
                }
        }

        if (studyFilterFlag == 1) // Ids used in study filter
        {
            List<Map> studyDetails = MiscUtil?.getStudyDictionaryValues(executorDTO?.studySelection)
            studyDetails.eachWithIndex { Map entry, int i ->
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (${(i + 5)},'$k','${v?.replaceAll("(?i)'", "''")}'); "
                }
            }
        }

        if (executorDTO.getUsedValidEventGroupSelection()) {
            JSON.parse(executorDTO.getUsedValidEventGroupSelection()).each {
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (600,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
            }
        }

        if (!executorDTO.getUsedValidEventGroupSelection() && eventFilterFlag == 1) // Ids used in event filter
        {
            List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(executorDTO?.usedEventSelection)
            Map soc = eventDetails[0]
            Map hlgt = eventDetails[1]
            Map hlt = eventDetails[2]
            Map pt = eventDetails[3]
            Map llt = eventDetails[4]
            Map synonyms = eventDetails[5]
            Map smqb = eventDetails[6]
            Map smqn = eventDetails[7]
            soc.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (8,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for soc = 8
            hlgt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for hlgt = 9
            hlt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (10,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for hlt = 10
            pt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (11,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for pt = 11
            llt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (12,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for llt = 12
            synonyms.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (13,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for synonym = 13
            smqn.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (18,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for SMQ Narrow = 18
            smqb.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (19,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            }  // KEY_ID for SMQ Broad = 19
        }
        insertStatement += " END;"

        return insertStatement
    }

    private String getSqlInsertableValue(def value) {
        return (value != null ? "'${value}'" : null)
    }

    public String initializeReportGtts(BaseTemplateQuery templateQuery,ReportTemplate usedReportTemplate, boolean hasQuery, Locale locale, int templateSetIndex = 0 , int templateSetId = 0, Long processReportId = 0L, Boolean isDrilldownCll = false, boolean manual = false, boolean viewSql = false) {
        ReportTemplate reportTemplate = usedReportTemplate
        BaseConfiguration config = templateQuery.usedConfiguration
        int isCumulativeTemplate = 0
        Boolean reAssesQueryFlag = (templateQuery.usedQuery && templateQuery.usedQuery?.queryType == QueryTypeEnum.QUERY_BUILDER && templateQuery.usedQuery?.reassessForProduct)
        int reAssesListednessFlag = (templateQuery.usedTemplate.reassessForProduct || reAssesQueryFlag) ? 1 : 0
        int colID = 0
        int rowNumber = 0
        int columnNumber = 0
        String insertStatement = ""
        insertStatement = "begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_tabulation''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_params''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_fields''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_filter_key_values''); end;';"+
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_TABULATION_MEASURES''); end;';"+
                "delete from GTT_QUERY_DETAILS; "+
                "delete from GTT_QUERY_SETS; "

        if(isCumulativeCaseSeriesAvailable(config)){
            insertStatement += "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_VERSIONS_BASE''); end;';"
        }
        int queryExists = hasQuery? 1:0
        String queryLevel = templateQuery?.queryLevel?.name()
        int showDistinct

        if (reportTemplate instanceof CaseLineListingTemplate) { // this parameter only available for line listings
            showDistinct = reportTemplate?.columnShowDistinct? 1:0
        }

        BaseConfiguration baseConfig = templateQuery instanceof TemplateQuery ? templateQuery.report: templateQuery.executedConfiguration

        String inputSeparator = "" // feature not available in UI , comma added by default
        int reassessIndex = 0
        int templateSetFlag = 0
        int templateSetMeasurePop
        List<Date> minMaxDate = config.reportMinMaxDate
        String startDate = templateQuery?.startDate?.format(DATE_FMT)?.toString()
        String endDate
        boolean isCumulativeOrInterval
        int considerOnlyPoi = 0
        int studyMedicationType = 0
        ExecutedReportConfiguration executedReportConfiguration = ExecutedReportConfiguration?.findById(config.id)
        if (!executedReportConfiguration) {
            ReportConfiguration reportConfiguration = ReportConfiguration?.findById(config.id)
            isCumulativeOrInterval = templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUMULATIVE || (reportConfiguration.globalDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE && templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE)
            endDate = isCumulativeOrInterval ? minMaxDate.last()?.format(DATE_FMT)?.toString() : templateQuery?.endDate?.format(DATE_FMT)?.toString()
        } else {
            isCumulativeOrInterval = templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUMULATIVE || (executedReportConfiguration.executedGlobalDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE && templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE)
            endDate = isCumulativeOrInterval ? minMaxDate.last()?.format(DATE_FMT)?.toString() : templateQuery?.endDate?.format(DATE_FMT)?.toString()
            considerOnlyPoi = executedReportConfiguration.considerOnlyPoi ? 1 : 0
            studyMedicationType = executedReportConfiguration.studyMedicationType ? 1 : 0
        }
        String scheduledDate = config?.nextRunDate?.format(DATE_FMT)?.toString()
        if(!scheduledDate && !config.isEnabled) {//Only for reports which are scheduled for run once in order to prevent null entry in "ScheduledDate"
            ExecutionStatus executionStatus = ExecutionStatus.findByEntityId(config.id, [sort: 'dateCreated', order: 'desc'])
            scheduledDate = executionStatus?.nextRunDate?.format(DATE_FMT)?.toString()
        }
        String minStartDate = minMaxDate.first()?.format(DATE_FMT)?.toString()
        String maxEndDate = minMaxDate.last()?.format(DATE_FMT)?.toString()
        String reportName = config?.reportName?.replaceAll("(?i)'", "''")
        int limitToCaseSeriesID = (config instanceof Configuration) ? (config.useCaseSeriesId ?: 0) : ((config instanceof ExecutedConfiguration) ? (config.usedCaseSeriesId ?: 0) : 0)
        if (templateQuery.usedTemplate instanceof ITemplateSet) {
            templateSetFlag = 1
            ((ITemplateSet) templateQuery.usedTemplate).nestedTemplates.each { nested ->
                if (nested instanceof DataTabulationTemplate) {
                    if (((DataTabulationTemplate) nested).columnMeasureList?.find { DataTabulationColumnMeasure colMeasure ->
                        colMeasure.measures.find { DataTabulationMeasure measure ->
                            measure.type in [MeasureTypeEnum.REPORT_COUNT, MeasureTypeEnum.COMPLIANCE_RATE]
                        }
                    }) templateSetMeasurePop = 1
                }
            }
        }
// Dates is cumulative period is selected in report template or any of the date range associated with measures in Tabulation template is a cumulative date range.
        if ((templateQuery?.usedDateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUMULATIVE) ||
                (reportTemplate instanceof DataTabulationTemplate && reportTemplate.hasCumulative())){
            startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY).format(DATE_FMT).toString()
            isCumulativeTemplate = 1
            if(isCumulativeCaseSeriesAvailable(config)) {
                limitToCaseSeriesID = config.cumulativeCaseSeriesId
            }
        }

        String globalStartDate
        String globalEndDate
        int allCumulative = 0
        int allIdentical = 0
        if (config.instanceOf(ExecutedConfiguration) || config.instanceOf(Configuration)) {
            BaseDateRangeInformation globalDateRangeInformation = (config instanceof Configuration ? config.globalDateRangeInformation : config.executedGlobalDateRangeInformation)
            List<BaseTemplateQuery> templateQueries = (config instanceof Configuration ? config.templateQueries : config.executedTemplateQueries)
            if (!globalDateRangeInformation) globalDateRangeInformation = new GlobalDateRangeInformation()  //for backward nullable data handling only
            globalStartDate = globalDateRangeInformation.dateRangeStartAbsolute?.format(DATE_FMT)?.toString()
            globalEndDate = globalDateRangeInformation.dateRangeEndAbsolute?.format(DATE_FMT)?.toString()
            allIdentical = templateQueries.find {
                ((it.usedDateRangeInformationForTemplateQuery.dateRangeEnum != DateRangeEnum.PR_DATE_RANGE)
                        && (!it.usedDateRangeInformationForTemplateQuery.hasSameRange(globalDateRangeInformation)))
            } ? 0 : 1
            if (isDrilldownCll) allIdentical = 0
            if (allIdentical == 1) {
                templateQueries.each { BaseTemplateQuery tq ->
                    if (tq.usedTemplate instanceof DataTabulationTemplate) {
                        ((DataTabulationTemplate) tq.usedTemplate).columnMeasureList?.each { DataTabulationColumnMeasure columnMeasure ->
                            columnMeasure.measures?.each { DataTabulationMeasure measure ->
                                if (measure.dateRangeCount != CountTypeEnum.PERIOD_COUNT) allIdentical = 0
                            }
                        }
                    }
                }
            }
            if (globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                globalStartDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY).format(DATE_FMT).toString()
                globalEndDate =  minMaxDate.last()?.format(DATE_FMT)?.toString()
                allCumulative = (!templateQueries.find {
                    !(it.usedDateRangeInformationForTemplateQuery.dateRangeEnum in [DateRangeEnum.CUMULATIVE, DateRangeEnum.PR_DATE_RANGE])
                }) ? 1 : 0
            }
        }

        int includeLockedVersion = config?.includeLockedVersion? 1:0
        int includeAllStudyDrugsCases = config?.includeAllStudyDrugsCases? 1:0
        int excludeFu =  config?.excludeFollowUp? 1:0
        int includeNonSignificantFollowUp = config?.includeNonSignificantFollowUp ? 1:0
        
        String asOfVersionDate = ""
        if(config?.asOfVersionDate)
        {
            asOfVersionDate = config?.asOfVersionDate?.format(DATE_FMT)?.toString()
        }
        String dateRangeType = config?.dateRangeType?.name
        String sourceType
        if (config.sourceProfile.sourceProfileTypeEnum == SourceProfileTypeEnum.SINGLE) {
            sourceType = config.sourceProfile.isCentral ? "CENTRAL" : "AFF_${config.sourceProfile.sourceId}"
        } else {
            Set<SourceProfile> sourceProfiles = SourceProfile.sourceProfilesForUser(config.owner) - SourceProfile.fetchAllDataSource()
            sourceType = (sourceProfiles.size() == 0) ? "CENTRAL" :  sourceProfiles.collect {
                it.isCentral ? "CENTRAL" : "AFF_${it.sourceId}"
            }.join(",")
        }
        String evaluateCaseDataAs = config?.evaluateDateAs?.value()
        int medicallyConfirmedCasesFlag = config?.includeMedicallyConfirmedCases? 1:0
        int productFilterFlag = (config?.productSelection || config?.validProductGroupSelection) ? 1:0
        int isMultiIngredient = (config?.isMultiIngredient ? 1:0)
        int includeWHODrugs = (config?.includeWHODrugs ? 1:0)
        String configCumulativeStartDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, NEGATIVE_INFINITY).format(DATE_FMT).toString()
        int studyFilterFlag = config?.studySelection? 1:0
        int eventFilterFlag = (config?.usedEventSelection || config?.usedValidEventGroupSelection) ? 1:0
        int includeCleanupVersion = grailsApplication.config?.reports?.includeDataCleanupVersion? 1:0 // configurable in application
        int supectProductCheck = config?.suspectProduct? 1:0
        int limitPrimaryPath = config?.limitPrimaryPath? 1:0
        int bVoidedFlag = isVoidedFlagOn(templateQuery,locale)? 1:0
        int excludeNonValidCases = config?.excludeNonValidCases? 1:0
        int excludeDeletedCases = config?.excludeDeletedCases? 1:0
        String selectedDateRange = templateQuery?.usedDateRangeInformationForTemplateQuery?.dateRangeEnum?.name()
        int periodicReportFlag = (config instanceof PeriodicReportConfiguration || config instanceof ExecutedPeriodicReportConfiguration)? 1:0
        String periodicReportType = (config instanceof PeriodicReportConfiguration || config instanceof ExecutedPeriodicReportConfiguration) ? config.periodicReportType?.toString() : ''
        String primaryDestination = (config instanceof PeriodicReportConfiguration || config instanceof ExecutedPeriodicReportConfiguration) ? config.primaryReportingDestination : ''
        int includeOpenCases = (config instanceof ExecutedPeriodicReportConfiguration && config.includeOpenCasesInDraft && config.hasGeneratedCasesData && (config.status in [ ReportExecutionStatusEnum.GENERATING_DRAFT, ReportExecutionStatusEnum.GENERATING_NEW_SECTION])) ? 1 : ((config instanceof PeriodicReportConfiguration && config.generateCaseSeries && config.includeOpenCasesInDraft) ? 1 : 0)
        int includePrevMissedCase = (config instanceof PeriodicReportConfiguration || config instanceof ExecutedPeriodicReportConfiguration) && config.includePreviousMissingCases ? 1:0
        Integer prCaseSeriesID = 0
        Integer cumCaseSeriesID = 0
        Long reportTemplateId = reportTemplate.id
        if (reportTemplate instanceof ExecutedCaseLineListingTemplate || reportTemplate instanceof ExecutedDataTabulationTemplate || reportTemplate instanceof ExecutedXMLTemplate || reportTemplate instanceof ExecutedNonCaseSQLTemplate || reportTemplate instanceof ExecutedCustomSQLTemplate || reportTemplate instanceof ExecutedTemplateSet) {
            reportTemplateId = reportTemplate.originalTemplateId
        }

        String receiverId = null
        String senderId = null
        String senderEmailAddress = null
        String senderFax = null
        String senderPhone = null
        String senderCountry = null
        String recipientCountry = null

        String senderPostal = null
        String senderState = null
        String senderCity = null
        String senderAddress1 = null
        String senderAddress2 = null
        String senderTitle = null
        String senderFirstName = null
        String senderMiddleName = null
        String senderLastName = null
        String senderDepartment = null
        String recipientTitle = null
        String recipientFirstName = null
        String recipientMiddleName = null
        String recipientLastName = null
        String recipientDepartment = null
        String senderCompanyName = null
        String senderUnitOrganizationName = null
        String senderPrefLanguage = null
        String recipientCompanyName = null
        String recipientUnitOrganizationName = null
        String recipientPrefLanguage = null
        String senderOrganizationName = null
        String senderType = null
        Long recipientOrganizationId = 0L
        String recipientOrganizationName = null
        String recipientType = null
        String recipientAddress1 = null
        String recipientAddress2 = null
        String recipientState = null
        String recipientPostcode = null
        String recipientCity = null
        String recipientPhone = null
        String recipientFax = null
        String recipientEmail = null
        String referenceProfileName = null
        String allowedAttachments = null
        String holderId = null
        String senderHolderId = null
        Integer dueInDays = 0
        Integer isExpedited = 0
        Integer isIcsrProfile = -1
        Integer isIcsrReport = 0
        int icsrPreviewFlag = manual ? 1 : 0
        Integer privacyProtected = 0
        Integer blindProtected = 0
        Integer adjustDueDate = 0
        String dueDateOption = null
        String dueDateAdjustment = null
        String calendars = null
        Integer icsrMsgType = 0
        String icsrMsgTypeName = "";
        Integer includeProductObligation = 0
        Integer includeStudyObligation = 0
        Integer includeNonReportable = 0
        Integer includeOpenCasesForIcsrProfile = 0
        Integer autoScheduling = 0
        Integer autoGenerate = 0
        Integer awareDate = 0
        Integer localCpRequired = 0
        Integer multipleReport = 0
        Integer productLevel = 0
        Integer deviceReportable = 0
        List<Map> authorizationType = []
        Integer flagPMDA = 0
        Long senderTypeId = 0L
        Long recipientTypeId = 0L
        Integer isJapanProfile = 0
        String templateName = templateQuery?.usedTemplate?.name
        String sectionTitle = templateQuery?.title ?: templateQuery?.usedQuery?.name
        Integer isReport = (templateQuery?.usedTemplate?.isCiomsITemplate() || templateQuery?.usedTemplate?.isMedWatchTemplate()) ? 1 : 0
        int isXMLTemplate = (usedReportTemplate?.instanceOf(XMLTemplate)) ? 1 : 0
        if(templateQuery instanceof IcsrTemplateQuery || templateQuery instanceof ExecutedIcsrTemplateQuery){
            dueInDays = templateQuery?.dueInDays
            isExpedited = templateQuery?.isExpedited?1:0
        }
        if (config instanceof ExecutedIcsrReportConfiguration || config instanceof ExecutedIcsrProfileConfiguration) {
            receiverId = config.receiverId
            senderId = config.senderId
            senderEmailAddress = config.email
            senderFax = config.fax
            senderPhone = config.phone
            senderCountry = config.senderCountry
            recipientCountry = config.recipientCountry
            senderPostal = config.postalCode
            senderState = config.state
            senderCity = config.city
            senderAddress1 = config.address1
            senderAddress2 = config.address2
            senderTitle = config.senderTitle?.value()
            senderFirstName = config.senderFirstName
            senderMiddleName = config.senderMiddleName
            senderLastName = config.senderLastName
            senderDepartment = config.senderDept
            senderCompanyName = config.senderCompanyName
            senderUnitOrganizationName = config.senderUnitOrganizationName
            senderPrefLanguage = config.senderPrefLanguage
            recipientTitle = config.recipientTitle?.value()
            recipientFirstName = config.recipientFirstName
            recipientMiddleName = config.recipientMiddleName
            recipientLastName = config.recipientLastName
            recipientDepartment = config.recipientDept
            recipientCompanyName = config.recipientCompanyName
            recipientUnitOrganizationName = config.recipientUnitOrganizationName
            recipientPrefLanguage = config.recipientPrefLanguage
            recipientTypeId = IcsrOrganizationType.findByName(config.recipientType)?.org_name_id
            recipientType = IcsrOrganizationType.findByOrg_name_idAndLangId(recipientTypeId, getPVALanguageId('en'))?.name
            recipientAddress1 = config.recipientAddress1
            recipientAddress2 = config.recipientAddress2
            recipientState = config.recipientState
            recipientPostcode = config.recipientPostcode
            recipientCity = config.recipientCity
            recipientPhone = config.recipientPhone
            recipientFax = config.recipientFax
            recipientEmail = config.recipientEmail
            senderOrganizationName = config.senderOrganizationName
            allowedAttachments = config.allowedAttachments
            senderTypeId = IcsrOrganizationType.findByName(config.senderTypeName)?.org_name_id
            senderType = IcsrOrganizationType.findByOrg_name_idAndLangId(senderTypeId, getPVALanguageId('en'))?.name
            recipientOrganizationId = UnitConfiguration.findByUnitName(config.recipientOrganizationName)?.id
            recipientOrganizationName = config.recipientOrganizationName
            blindProtected = templateQuery?.blindProtected ? 1 : 0
            privacyProtected = templateQuery?.privacyProtected ? 1 : 0
            icsrMsgType = templateQuery?.icsrMsgType
            icsrMsgTypeName = templateQuery?.icsrMsgTypeName
            holderId = config.holderId
            senderHolderId = config.senderHolderId
            if(config instanceof ExecutedIcsrProfileConfiguration){
                isExpedited = templateQuery?.isExpedited?1:0
                isIcsrProfile = 1
                includeProductObligation=config.includeProductObligation?1:0
                includeStudyObligation=config.includeStudyObligation?1:0
                includeNonReportable=config.includeNonReportable?1:0
                includeOpenCasesForIcsrProfile =config.includeOpenCases?1:0
                adjustDueDate=config.adjustDueDate?1:0
                if(adjustDueDate==1) {
                    dueDateOption = config.dueDateOptionsEnum
                    dueDateAdjustment = config.dueDateAdjustmentEnum
                    calendars = config.calendars.collect { it }.join(',').toString()
                }
                autoScheduling=config.autoScheduling?1:0
                autoGenerate=config.autoGenerate?1:0
                awareDate=config.awareDate?1:0
                localCpRequired=config.localCpRequired?1:0
                multipleReport=config.multipleReport?1:0
                productLevel=config.isProductLevel?1:0
                deviceReportable=config.deviceReportable?1:0
                if (multipleReport || productLevel || deviceReportable) {
                    queryLevel = QueryLevelEnum.PRODUCT
                }
                AuthorizationType.withNewSession {
                    config.authorizationTypes.each {
                        String authName = AuthorizationType.findByIdAndLangId(it, 1)?.name
                        authorizationType.add([id: it, name: authName])
                    }
                }
                flagPMDA = config.isPMDAReport() ? 1 : 0
                isJapanProfile = config.isJapanProfile ? 1 : 0
            }
            if(config instanceof  ExecutedIcsrReportConfiguration){
                isIcsrReport = 1
                referenceProfileName = config.referenceProfileName
            }

        }

        if (config instanceof IcsrReportConfiguration || config instanceof IcsrProfileConfiguration) {
            receiverId = config.recipientOrganization?.unitRegisteredId
            senderId = config.senderOrganization?.unitRegisteredId
            senderEmailAddress = config.senderOrganization?.email
            senderFax = config.senderOrganization?.fax
            senderPhone = config.senderOrganization?.phone
            senderCountry = config.senderOrganization?.organizationCountry
            recipientCountry = config.recipientOrganization?.organizationCountry
            senderPostal = config.senderOrganization?.postalCode
            senderState = config.senderOrganization?.state
            senderCity = config.senderOrganization?.city
            senderAddress1 = config.senderOrganization?.address1
            senderAddress2 = config.senderOrganization?.address2
            senderTitle = config.senderOrganization?.title?.value()
            senderFirstName = config.senderOrganization?.firstName
            senderMiddleName = config.senderOrganization?.middleName
            senderLastName = config.senderOrganization?.lastName
            senderDepartment = config.senderOrganization?.department
            senderCompanyName = config.senderOrganization?.organizationName
            senderUnitOrganizationName = config.senderOrganization?.unitOrganizationName
            senderPrefLanguage = config.senderOrganization?.preferredLanguage
            senderHolderId = config.senderOrganization?.holderId
            recipientTitle = config.recipientOrganization?.title?.value()
            recipientFirstName = config.recipientOrganization?.firstName
            recipientMiddleName = config.recipientOrganization?.middleName
            recipientLastName = config.recipientOrganization?.lastName
            recipientDepartment = config.recipientOrganization?.department
            recipientCompanyName = config.recipientOrganization?.organizationName
            recipientUnitOrganizationName = config.recipientOrganization?.unitOrganizationName
            recipientPrefLanguage = config.recipientOrganization?.preferredLanguage
            allowedAttachments = config.recipientOrganization?.allowedAttachments.collect{it}.join(',').toString()
            senderOrganizationName = config.senderOrganization?.unitName
            senderTypeId = IcsrOrganizationType.get(config.senderOrganization?.organizationType?.id)?.org_name_id
            senderType = IcsrOrganizationType.findByOrg_name_idAndLangId(senderTypeId, getPVALanguageId('en'))?.name
            recipientOrganizationId = config.recipientOrganization?.id
            recipientOrganizationName = config.recipientOrganization?.unitName
            recipientTypeId = IcsrOrganizationType.get(config.recipientOrganization?.organizationType?.id)?.org_name_id
            recipientType = IcsrOrganizationType.findByOrg_name_idAndLangId(recipientTypeId, getPVALanguageId('en'))?.name
            recipientAddress1 = config.recipientOrganization?.address1
            recipientAddress2 = config.recipientOrganization?.address2
            recipientState = config.recipientOrganization?.state
            recipientPostcode = config.recipientOrganization?.postalCode
            recipientCity = config.recipientOrganization?.city
            recipientPhone = config.recipientOrganization?.phone
            recipientFax = config.recipientOrganization?.fax
            recipientEmail = config.recipientOrganization?.email
            isExpedited = templateQuery?.isExpedited?1:0
            blindProtected = templateQuery?.blindProtected ? 1 : 0
            privacyProtected = templateQuery?.privacyProtected ? 1 : 0
            icsrMsgType = templateQuery?.icsrMsgType
            holderId = config.recipientOrganization?.holderId

            if(config instanceof IcsrProfileConfiguration){
                isIcsrProfile = 1
                includeProductObligation=config.includeProductObligation?1:0
                includeStudyObligation=config.includeStudyObligation?1:0
                includeNonReportable=config.includeNonReportable?1:0
                includeOpenCasesForIcsrProfile =config.includeOpenCases?1:0
                adjustDueDate=config.adjustDueDate?1:0
                if(adjustDueDate==1) {
                    dueDateOption = config.dueDateOptionsEnum
                    dueDateAdjustment = config.dueDateAdjustmentEnum
                    calendars = config.calendars.collect { it }.join(',').toString()
                }

                autoScheduling=config.autoScheduling?1:0
                autoGenerate=config.autoGenerate?1:0
                awareDate=config.awareDate?1:0
                localCpRequired=config.localCpRequired?1:0
                multipleReport=config.multipleReport?1:0
                productLevel=config.isProductLevel?1:0
                deviceReportable=config.deviceReportable?1:0
                queryLevel = config.deviceReportable ? QueryLevelEnum.PRODUCT : queryLevel
                AuthorizationType.withNewSession {
                    config.authorizationTypes.each {
                        String authName = AuthorizationType.findByIdAndLangId(it, 1)?.name
                        authorizationType.add([id: it, name: authName])
                    }
                }
                flagPMDA = config.isPMDAReport() ? 1 : 0
                isJapanProfile = config.isJapanProfile ? 1 : 0
            }
            if(config instanceof IcsrReportConfiguration){
                isIcsrReport = 1
                referenceProfileName = config.referenceProfile?.reportName
            }
        }

        if (config instanceof ExecutedPeriodicReportConfiguration) {
            prCaseSeriesID = config.caseSeriesId
//            As discussed with Amrit we would need to send cumCaseSeriesID always to procedure as Cumulative Case series always gets generated.
            cumCaseSeriesID = config.cumulativeCaseSeriesId ?: config.caseSeriesId
        }

// template field level parameters
        if (reportTemplate instanceof CaseLineListingTemplate) {
            reportTemplate.getAllSelectedFieldsInfo()?.each { ReportFieldInfo rf ->
                int csvFlag = 0
                int redactedFlag = 0
                int blindedFlag = 0
                int groupColumnFlag = 0
                String sortAscDesc = ""
                colID += 1
                String javaVariable = rf?.reportField?.name
                int sortLevel = rf?.sortLevel
                sortAscDesc = rf?.sort?.value()
                int setId= (templateSetIndex << 16) | rf?.setId
                if (rf.commaSeparatedValue) {
                    csvFlag = 1
                }
                if (rf.blindedValue) {
                    blindedFlag = 1
                }
                String customExpression = rf?.customExpression?.replaceAll("(?i)'", "''")
                customExpression = customExpression?.replaceAll("\\b(?i)null\\b", "\'\$0\'")
                String advSortExpression = rf?.advancedSorting?.replaceAll("(?i)'", "''")
// this parameter only available for line listings
                if (reportTemplate instanceof CaseLineListingTemplate) {
                    groupColumnFlag = reportTemplate.groupingList?.reportFieldInfoList*.reportField.find {
                        it.id == rf.reportField.id
                    } ? 1 : 0
                }
                // incremental id generated for every re-assess column added in the template in the relative order of columns in template
                if (rf?.reportField?.name == "dvListednessReassess") {
                    reassessIndex += 1
                }

                insertStatement += " Insert into GTT_REPORT_INPUT_FIELDS (ID,JAVA_VARIABLE,SORT_LEVEL, SORT_TYPE,CSV_FLAG,BLINDED_FLAG,REDACTED_FLAG," +
                        "CUSTOM_EXPRESSION,GROUP_COLUMN_FLAG,REASSESS_INDEX,ADVANCED_SORT_EXPRESSION,SET_ID,TEMP_SET_ID) VALUES (${colID},'${javaVariable}',${sortLevel}" +
                        ",'${sortAscDesc}',${csvFlag},${blindedFlag},${redactedFlag},'${customExpression}',${groupColumnFlag},${reassessIndex},'${advSortExpression}',${setId},${templateSetId}); "?.replaceAll("'((?i)null)'", "\$1")

            }
        }

        if (reportTemplate instanceof DataTabulationTemplate) {
            int blockNum = 0
            int measureId = 0
            reportTemplate?.selectedFieldsRows?.each { ReportFieldInfo rf ->
                colID += 1
                rowNumber +=1
                String javaVariable = rf?.reportField?.name
                String customExpression = rf?.customExpression?.replaceAll("(?i)'", "''")
                customExpression = customExpression?.replaceAll("(?i)null", "\'\$0\'")
                String advSortExpression = rf?.advancedSorting?.replaceAll("(?i)'", "''")

                if (rf?.reportField?.name == "dvListednessReassess") {
                    reassessIndex += 1
                }

                insertStatement += " Insert into GTT_REPORT_INPUT_FIELDS (ID,JAVA_VARIABLE,CUSTOM_EXPRESSION,REASSESS_INDEX,ADVANCED_SORT_EXPRESSION," +
                        "TABULATION_BLOCK,TABULATION_ROW,TABULATION_COLUMN,TEMP_SET_ID) VALUES" +
                        " (${colID},'${javaVariable}','${customExpression}',${reassessIndex},'${advSortExpression}',${blockNum},${rowNumber},0,${templateSetId}); "?.replaceAll("'((?i)null)'", "\$1")

            }

            reportTemplate?.columnMeasureList?.each { block ->
                int showTotalCum = block?.showTotalCumulativeCases ? 1 : 0
                int showTotalInterval = block?.showTotalIntervalCases ? 1 : 0
                blockNum += 1

                block?.columnList?.reportFieldInfoList?.each { ReportFieldInfo rf ->
                    String sortAscDesc = rf?.sort?.value()
                    String javaVariable = rf?.reportField?.name
                    int sortLevel = rf?.sortLevel
                    colID += 1
                    columnNumber +=1
                    String customExpression = rf?.customExpression?.replaceAll("(?i)'", "''")
                    customExpression = customExpression?.replaceAll("(?i)null", "\'\$0\'")
                    String advSortExpression = rf?.advancedSorting?.replaceAll("(?i)'", "''")

                    // incremental id generated for every re-assess column added in the template in the relative order of columns in template
                    if (rf?.reportField?.name == "dvListednessReassess") {
                        reassessIndex += 1
                    }

                    insertStatement += " Insert into GTT_REPORT_INPUT_FIELDS (ID,JAVA_VARIABLE,SORT_LEVEL, SORT_TYPE,CUSTOM_EXPRESSION,REASSESS_INDEX," +
                            "ADVANCED_SORT_EXPRESSION,TABULATION_BLOCK,TABULATION_ROW,TABULATION_COLUMN,SHOW_TOTAL_INTERVAL,SHOW_TOTAL_CUM,TEMP_SET_ID) VALUES (${colID},'${javaVariable}',${sortLevel}" +
                            ",'${sortAscDesc}','${customExpression}',${reassessIndex},'${advSortExpression}',${blockNum},0,${columnNumber},${showTotalInterval},${showTotalCum},${templateSetId}); "?.replaceAll("'((?i)null)'", "\$1")
                }

                block.measures.each { measure ->
                    String measureName = measure?.type
                    String countType = measure?.dateRangeCount?.type()
                    String cstmStartDate = ""
                    String cstmEndDate = ""

                    switch (measure?.dateRangeCount) {
                        case CountTypeEnum.CUSTOM_PERIOD_COUNT:
                            cstmStartDate = measure?.customPeriodFrom?.format(DATE_FMT)?.toString()
                            cstmEndDate = measure?.customPeriodTo?.format(DATE_FMT)?.toString()
                            break
                        case CountTypeEnum.PERIOD_COUNT:
                            cstmStartDate = templateQuery?.startDate?.format(DATE_FMT)?.toString()
                            cstmEndDate = templateQuery?.endDate?.format(DATE_FMT)?.toString()
                            break
                        case CountTypeEnum.PREVIOUS_PERIOD_COUNT:
                            use(TimeCategory) {
                                (cstmStartDate, cstmEndDate) = templateService.getPrevPeriodStartDateAndEndDate(templateQuery)
                            }
                            break
                        case CountTypeEnum.LAST_X_DAYS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.lastXDays(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.LAST_X_WEEKS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.lastXWeeks(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.LAST_X_MONTHS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.lastXMonths(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.LAST_X_YEARS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.lastXYears(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.NEXT_X_DAYS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.nextXDays(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.NEXT_X_WEEKS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.nextXWeeks(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.NEXT_X_MONTHS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.nextXMonths(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        case CountTypeEnum.NEXT_X_YEARS_PERIOD_COUNT:
                            (cstmStartDate, cstmEndDate) = RelativeDateConverter.nextXYears(templateQuery?.startDate, measure?.relativeDateRangeValue, 'UTC').collect { it.format(DATE_FMT)?.toString() }
                            break
                        default:
                            break
                    }
                    String percentage = measure?.percentageOption
                    int showTotal = measure?.showTotal ? 1 : 0
                    measureId += 1
                    insertStatement += " Insert into GTT_TABULATION_MEASURES (ID,BLOCK_NUM,MEASURE,COUNT_TYPE,CUSTOM_START_DATE,CUSTOM_END_DATE,PERCENTAGE_OPTION,SHOW_TOTAL)" +
                            "VALUES(${measureId},${blockNum},'${measureName}','${countType}','${cstmStartDate}','${cstmEndDate}','${percentage}',${showTotal}); "?.replaceAll("(?i)'null'", "null")

                }
            }
            int drillDownToCaseList = reportTemplate.drillDownToCaseList ? 1 : 0
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_TAB_GENERATE_CL','${drillDownToCaseList}');"
            int rowVal = reportTemplate?.selectedFieldsRows ? 0 : 1
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('NO_ROW_TABULATION','${rowVal}');"
        }
// report Level parameters
        insertStatement +=" Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_QUERY','${queryExists}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_LEVEL','${queryLevel}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SHOW_DISTINCT','${showDistinct}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INPUT_SEPARATOR','${inputSeparator}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE',${getSqlInsertableValue(startDate)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE',${getSqlInsertableValue(endDate)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_SCHEDULED_DATE',${getSqlInsertableValue(scheduledDate)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_LOCKED_VERSION','${includeLockedVersion}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_STUDY_DRUGS','${includeAllStudyDrugsCases}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VERSION_ASOF_DATE','${asOfVersionDate}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATE_RANGE_TYPE',${getSqlInsertableValue(dateRangeType)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SOURCE_TYPE','${sourceType}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVALUATE_DATA_ASOF',${getSqlInsertableValue(evaluateCaseDataAs)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MEDICALLY_CONFIRMED_FLAG','${medicallyConfirmedCasesFlag}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_FILTER_FLAG','${productFilterFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('STUDY_FILTER_FLAG','${studyFilterFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVENT_FILTER_FLAG','${eventFilterFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_FOLLOWUP','${excludeFu}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_CLEANUP_VERSION','${includeCleanupVersion}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUSPECT_PRODUCT_CHECK','${supectProductCheck}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_PRIMARY_PATH','${limitPrimaryPath}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_NONVALID_CASES','${excludeNonValidCases}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_DELETED_CASES','${excludeDeletedCases}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_TO_CASE_SERIES_ID','${limitToCaseSeriesID}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VOIDED_FLAG','${bVoidedFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_FLAG','${periodicReportFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PR_CASE_SERIES_ID','${prCaseSeriesID}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CUM_CASE_SERIES_ID','${cumCaseSeriesID}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SELECTED_DATE_RANGE','${selectedDateRange}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_TYPE','${periodicReportType}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRIMARY_DESTINATION_NAME','${primaryDestination}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_OPEN_CASES_IN_DRAFT','${includeOpenCases}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MIN_REPORT_START_DATE',${getSqlInsertableValue(minStartDate)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MAX_REPORT_END_DATE',${getSqlInsertableValue(maxEndDate)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_NAME','${reportName?.replaceAll("(?i)'", "''")}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ANY_CUMULATIVE_TEMP_FLAG','${baseConfig.isAnyCumulativeTQ()}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_CUMULATIVE_TEMPLATE','${isCumulativeTemplate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_SET_FLAG','${templateSetFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_SET_MEASURE_POP','${templateSetMeasurePop}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_PREVIOUSLY_MISSED_CASES','${includePrevMissedCase}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REASSESS_LISTEDNESS_FLAG','${reAssesListednessFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('OBJECT_ID',${baseConfig?.id});"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXECUTION_ID',${baseConfig?.id});"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SECTION_ID',${templateQuery?.id});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_ID',${reportTemplateId});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_NAME',${getSqlInsertableValue(templateName)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SECTION_TITLE',${getSqlInsertableValue(sectionTitle?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_REPORT','${isReport}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_ID',${templateQuery?.usedQuery?.originalQueryId});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_NON_SIGNIFICANT_FOLLOWUP_CASES',${includeNonSignificantFollowUp});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_CASE_SERIES','0');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PVR_CASE_SERIES_OWNER', '${Constants.PVR_CASE_SERIES_OWNER}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_DUPLICATES', '0');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PVR_CASE_SERIES_OWNER', '${Constants.PVR_CASE_SERIES_OWNER}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_ID',${getSqlInsertableValue(receiverId?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_ID',${getSqlInsertableValue(senderId?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_EMAIL_ADDRESS',${getSqlInsertableValue(senderEmailAddress)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_FAX',${getSqlInsertableValue(senderFax)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_PHONE',${getSqlInsertableValue(senderPhone)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_COUNTRY_CODE',${getSqlInsertableValue(senderCountry?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_COUNTRY_CODE',${getSqlInsertableValue(recipientCountry?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_POSTAL',${getSqlInsertableValue(senderPostal)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_STATE',${getSqlInsertableValue(senderState?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_CITY',${getSqlInsertableValue(senderCity?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_ADDRESS1',${getSqlInsertableValue(senderAddress1?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_ADDRESS2',${getSqlInsertableValue(senderAddress2?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_TITLE',${getSqlInsertableValue(senderTitle)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_FIRST_NAME',${getSqlInsertableValue(senderFirstName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_MIDDLE_NAME',${getSqlInsertableValue(senderMiddleName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_LAST_NAME',${getSqlInsertableValue(senderLastName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_DEPARTMENT',${getSqlInsertableValue(senderDepartment?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_TITLE',${getSqlInsertableValue(recipientTitle)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_FIRST_NAME',${getSqlInsertableValue(recipientFirstName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_MIDDLE_NAME',${getSqlInsertableValue(recipientMiddleName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_LAST_NAME',${getSqlInsertableValue(recipientLastName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_DEPARTMENT',${getSqlInsertableValue(recipientDepartment?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_COMPANY_NAME',${getSqlInsertableValue(senderCompanyName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_UNIT_ORG_NAME',${getSqlInsertableValue(senderUnitOrganizationName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_PREFERENCE_LANGUAGE',${getSqlInsertableValue(senderPrefLanguage?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_COMPANY_NAME',${getSqlInsertableValue(recipientCompanyName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_UNIT_ORG_NAME',${getSqlInsertableValue(recipientUnitOrganizationName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_PREFERENCE_LANGUAGE',${getSqlInsertableValue(recipientPrefLanguage?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_ALLOWED_ATTACHMENTS',${getSqlInsertableValue(allowedAttachments?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_ORG_NAME',${getSqlInsertableValue(senderOrganizationName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_ORG_ID',${recipientOrganizationId});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_TYPE',${getSqlInsertableValue(recipientType?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_ADDRESS1',${getSqlInsertableValue(recipientAddress1?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_ADDRESS2',${getSqlInsertableValue(recipientAddress2?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_STATE',${getSqlInsertableValue(recipientState?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_POSTAL',${getSqlInsertableValue(recipientPostcode?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_CITY',${getSqlInsertableValue(recipientCity?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_PHONE',${getSqlInsertableValue(recipientPhone?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_FAX',${getSqlInsertableValue(recipientFax?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_EMAIL_ADDRESS',${getSqlInsertableValue(recipientEmail?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_ORG_NAME',${getSqlInsertableValue(recipientOrganizationName?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_HOLDER_ID',${getSqlInsertableValue(holderId?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_HOLDER_ID',${getSqlInsertableValue(senderHolderId?.replaceAll("(?i)'", "''"))});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_TYPE',${getSqlInsertableValue(senderType)});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_DUE_IN_DAYS','${dueInDays}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_XML_TEMPLATE','${isXMLTemplate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_CASE_SERIES','0');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_EXPEDITED','${isExpedited}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_NAME', '${reportTemplate?.name?.replaceAll("(?i)'", "''")}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_ICSR_PROFILE', '${isIcsrProfile}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_ICSR_REPORT', '${isIcsrReport}');" +
                "INSERT INTO GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_ICSR_PREVIEW','${icsrPreviewFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_MSG_TYPE_ID', '${icsrMsgType}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_MSG_TYPE_DESC', '${icsrMsgTypeName}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('BLIND_PROTECTED', '${blindProtected}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRIVACY_PROTECTED', '${privacyProtected}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_MULTI_INGREDIENT', '${isMultiIngredient}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CONFIG_CUMULATIVE_START_DATE', '${configCumulativeStartDate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_PMDA', '${flagPMDA}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_WHO_DRUGS', '${includeWHODrugs}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_SENDER_TYPE_ID', '${senderTypeId}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_RECEIVER_TYPE_ID', '${recipientTypeId}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_JPN_PROFILE', '${isJapanProfile}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VIEW_SQL','${viewSql?1:0}');"

        if (reportTemplate.instanceOf(DataTabulationTemplate) && templateQuery.granularity && reportTemplate.isGranularity())
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TABULATION_DATE_FORMAT','${templateQuery.granularity.code()}');"
        if (reportTemplate.instanceOf(DataTabulationTemplate)) {
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_TO_PERIOD_COUNT_GT_0','${reportTemplate.positiveCountOnly ? 1 : 0}');"
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ALL_TIMEFRAMES','${reportTemplate.allTimeframes ? 1 : 0}');"
        }

        if (referenceProfileName) {
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REFERENCE_PROFILE_NAME','${referenceProfileName.replaceAll("(?i)'", "''")}');"
        }

        if (config.instanceOf(ExecutedConfiguration) || config.instanceOf(Configuration)) {
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('GLOBAL_START_DATE','${globalStartDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('GLOBAL_END_DATE','${globalEndDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ALL_CUMULATIVE_TEMP_FLAG','${allCumulative}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ALL_IDENTICAL_DATE_RANGE','${allIdentical}');"
        }
        if (authorizationType.size() > 0) {
            authorizationType.each {
                insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_AUTH_TYPE_ID', '${it.id ?: ""}');"
                insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_AUTH_TYPE_DESC', '${it.name ?: ""}');"
            }
        } else {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_AUTH_TYPE_ID', '');"
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('E2B_AUTH_TYPE_DESC', '');"
        }
        if (config.validProductGroupSelection) {
            JSON.parse(config.validProductGroupSelection).each {
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0,it.name.lastIndexOf('(') - 1)}'); "
            }
        }
        if (!config.validProductGroupSelection && productFilterFlag == 1) {
            if (config.instanceOf(ExecutedConfiguration) || config.instanceOf(Configuration)) {
                insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ALL_CUMULATIVE_TEMP_FLAG','${allCumulative}');" +
                        "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ALL_IDENTICAL_DATE_RANGE','${allIdentical}');"
            }
        }

        if (config.instanceOf(ExecutedConfiguration)) {
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CONSIDER_POI_FOR_UNBLINDED','${considerOnlyPoi}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IGNORE_STUDY_MED_TYPE','${studyMedicationType}');"
        }

        if(productFilterFlag == 1) // Ids used in product filter
        {
            List<Map> productDetails = MiscUtil?.getProductDictionaryValues(config?.productSelection)
            List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
            productDetails.eachWithIndex { Map entry, int i ->
                int keyId = productViewsList.get(i).keyId
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,'$k','${v?.replaceAll("(?i)'", "''")}'); "
                }
            }
            if (config?.productSelection)
                JSON.parse(config?.productSelection)["100"]?.each {
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (299,'${it.id}','${it.name?.replaceAll("(?i)'", "''")}'); "
                }
        }

        if (studyFilterFlag == 1) // Ids used in study filter
        {
            List<Map> studyDetails = MiscUtil?.getStudyDictionaryValues(config?.studySelection)
            studyDetails.eachWithIndex { Map entry, int i ->
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (${(i + 5)},'$k','${v?.replaceAll("(?i)'", "''")}'); "
                }
            }
        }

        if (config.usedValidEventGroupSelection) {
            JSON.parse(config.usedValidEventGroupSelection).each {
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (600,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
            }
        }

        if (!config.usedValidEventGroupSelection && eventFilterFlag == 1) // Ids used in event filter
        {
            List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(config?.usedEventSelection)
            Map soc = eventDetails[0]
            Map hlgt = eventDetails[1]
            Map hlt = eventDetails[2]
            Map pt = eventDetails[3]
            Map llt = eventDetails[4]
            Map synonyms = eventDetails[5]
            Map smqb = eventDetails[6]
            Map smqn = eventDetails[7]
            soc.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (8,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for soc = 8
            hlgt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for hlgt = 9
            hlt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (10,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for hlt = 10
            pt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (11,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for pt = 11
            llt.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (12,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for llt = 12
            synonyms.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (13,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for synonym = 13
            smqn.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (18,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            } // KEY_ID for SMQ Narrow = 18
            smqb.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (19,'$k','${v?.replaceAll("(?i)'", "''")}'); "
            }  // KEY_ID for SMQ Broad = 19

        }

        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_OBLIGATION','${includeProductObligation}');"

        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('STUDY_OBLIGATION','${includeStudyObligation}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_NONREPORTABLE_CASES','${includeNonReportable}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('GENERATE_OPEN_CASES','${includeOpenCasesForIcsrProfile}');"

        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ADJUST_DUE_DATE','${adjustDueDate}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DUE_DATE_OPTION', ${getSqlInsertableValue(dueDateOption)});"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DUE_DATE_ADJUSTMENT',${getSqlInsertableValue(dueDateAdjustment)});"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CALENDARS', ${getSqlInsertableValue(calendars)});"



        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_AUTO_SCHEDULE','${autoScheduling}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_AUTO_GENERATE','${autoGenerate}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_AWARE_DATE','${awareDate}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_LOCAL_CP','${localCpRequired}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_RESEARCH','${multipleReport}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_BASED_SCHEDULING','${productLevel}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('FLAG_DEVICE','${deviceReportable}');"
        insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PROCESSED_REPORT_ID','${processReportId}');"

        insertStatement += " END;"
//TODO for Debug        insertStatement.replace("END;", "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DEBUG_INFO','1'); END;")
        return insertStatement
    }

    String getInsertStatementsToInsert(BaseTemplateQuery templateQuery, Locale locale, boolean nonValidCases , boolean deletedCases) {
        String additionalQueryBuilderBlock = ""
        BaseConfiguration configuration = templateQuery.usedConfiguration
        Set<ParameterValue> poiInputParams = configuration?.poiInputsParameterValues ?: []
        if ((templateQuery.usedTemplate instanceof CaseLineListingTemplate || templateQuery.usedTemplate instanceof DataTabulationTemplate) && templateQuery.usedTemplate.JSONQuery) {
            additionalQueryBuilderBlock = templateQuery.usedTemplate.JSONQuery
        }
        return "\n BEGIN\n" +(getInsertStatementsToInsert(templateQuery.usedQuery, additionalQueryBuilderBlock, templateQuery.usesQueryValueLists, poiInputParams, locale, nonValidCases, templateQuery.usedConfiguration?.owner, deletedCases) + getDowngradeQueryInsertStatements(configuration, poiInputParams, locale)) + "\n END; \n"
    }

    String getDowngradeQueryInsertStatements(BaseConfiguration configuration, Set poiInputParams, Locale locale) {
        String queryString = ""
        SqlGenIDDTO sqlGenIDDTO = new SqlGenIDDTO(value: -9999)
        List<ParameterValue> blanks = []
        List<ParameterValue> customSqlBlanks = []
        // Global Query as DowngradeQuery only in IcsrProfile just like invalid query with negative
        if (configuration instanceof ExecutedIcsrProfileConfiguration && configuration.executedGlobalQuery) {
            log.trace("Finding Downgrade Query to execute")

            configuration.executedGlobalQueryValueLists?.each { globalQueryList ->
                globalQueryList.parameterValues.each { parmeterValue ->
                    if (parmeterValue.hasProperty('reportField')) {
                        blanks.add(parmeterValue)
                    } else {
                        customSqlBlanks.add(parmeterValue)
                    }
                }
            }
            queryString = "\n BEGIN\n" + insertQueriesDataToTempTable(configuration.executedGlobalQuery, sqlGenIDDTO, null, 0, blanks, customSqlBlanks, poiInputParams, locale, 5) + "\n END; \n"
            log.trace("With Downgrade Query to execute:  ${queryString}")

        } else if (configuration instanceof IcsrProfileConfiguration && configuration.globalQuery) {
            log.trace("Finding Downgrade Query to execute")
            configuration.globalQueryValueLists?.each { globalQueryList ->
                globalQueryList.parameterValues.each { parmeterValue ->
                    if (parmeterValue.hasProperty('reportField')) {
                        blanks.add(parmeterValue)
                    } else {
                        customSqlBlanks.add(parmeterValue)
                    }
                }
            }
            queryString = "\n BEGIN\n" + insertQueriesDataToTempTable(configuration.globalQuery, sqlGenIDDTO, null, 0, blanks, customSqlBlanks, poiInputParams, locale, 5) + "\n END; \n"
            log.trace("With Downgrade Query to execute:  ${queryString}")

        }
        return queryString
    }

    String getInsertStatementsToInsert(SuperQuery superQuery, String additionalJSONQuery, List<ValueList> parameters, Set<ParameterValue> poiInputParams, Locale locale, boolean nonValidCases, User owner, Integer queryFlag = null , boolean deletedCases ) {
        List<ParameterValue> blanks = []
        List<ParameterValue> customSqlBlanks = []
        parameters?.each { globalQueryList ->
            globalQueryList.parameterValues.each { parmeterValue ->
                if (parmeterValue.hasProperty('reportField')) {
                    blanks.add(parmeterValue)
                } else {
                    customSqlBlanks.add(parmeterValue)
                }
            }
        }
        String queryString = """ 
                        DECLARE lastInsertedRow ROWID;
                        BEGIN
                            delete from GTT_QUERY_DETAILS;
                            delete from GTT_QUERY_SETS;
                            delete from GTT_REPORT_VAR_INPUT;
                       """
        SqlGenIDDTO sqlGenIDDTO = new SqlGenIDDTO()
        queryString = queryString + insertQueriesDataToTempTable(superQuery, sqlGenIDDTO, null, 0, blanks, customSqlBlanks,poiInputParams, locale, queryFlag)
        if(additionalJSONQuery){
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            queryString = queryString + buildQueryFromJSONQuery(additionalJSONQuery, sqlGenIDDTO, 0, null, false, [], poiInputParams, locale, 2)
        }
        if (nonValidCases) {
            SuperQuery nonValidQuery = SuperQuery.findByNonValidCasesAndIsDeletedAndOriginalQueryId(true,false,0L)
            if (nonValidQuery) {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                queryString = queryString + insertQueriesDataToTempTable(nonValidQuery, sqlGenIDDTO, null, 0, [], [],poiInputParams, locale, 1)
            }
        }
        if (deletedCases) {
            SuperQuery deletedQuery = SuperQuery.findByDeletedCasesAndIsDeletedAndOriginalQueryId(true,false,0L)
            if (deletedQuery) {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                queryString = queryString + insertQueriesDataToTempTable(deletedQuery, sqlGenIDDTO, null, 0, [], [],poiInputParams, locale, 6)
            }
        }
        if (owner) {
            Set<SuperQuery> dataProtectedQueries = UserGroup.fetchAllDataProtectionQueriesByUser(owner)
            if (dataProtectedQueries.size() == 1) {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                queryString = queryString + insertQueriesDataToTempTable(dataProtectedQueries[0], sqlGenIDDTO, null, 0, [], [], poiInputParams, locale, 3)
            } else if (dataProtectedQueries.size() > 1) {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                def dataProtectedQuerySet = [expressions: [], keyword: "INTERSECT"]
                dataProtectedQueries.eachWithIndex { it, index ->
                    dataProtectedQuerySet.expressions << [index: index, query: "${it.id}"]
                }
                queryString = queryString + insertSqlStatementFromQuerySetStatement(dataProtectedQuerySet, sqlGenIDDTO, new SqlGenIDDTO(value: sqlGenIDDTO.value + 1), 0, 0, null, [], [], poiInputParams, locale, 3)
            }
        }
        if (poiInputParams) {
            poiInputParams.each {
                queryString = queryString + "INSERT INTO GTT_REPORT_VAR_INPUT (INPUT_KEY, INPUT_VALUES) values ('${it.key}',${it.value ? "'${it.value}'" : null});\n"
            }
        }
        queryString = queryString + "\n END; \n"
        log.debug(queryString)  //TODO need to remove later
        return queryString
    }

    private String insertQueriesDataToTempTable(SuperQuery superQuery, SqlGenIDDTO sqlGenIDDTO, String joinOperator, int parent, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale, Integer query_flag) {
        String insertQuery = ""
        superQuery = GrailsHibernateUtil.unwrapIfProxy(superQuery)
        if (superQuery?.queryType == QueryTypeEnum.QUERY_BUILDER) {
            insertQuery = insertQuery + buildQueryFromJSONQuery(superQuery.JSONQuery, sqlGenIDDTO, parent, joinOperator, superQuery.hasBlanks, blanks, poiInputParams, locale, query_flag)
        } else if (superQuery?.queryType == QueryTypeEnum.CUSTOM_SQL) {
            String sqlQuery = superQuery.customSQLQuery
            if (superQuery.hasBlanks) {
                sqlQuery = replaceMapInString(sqlQuery, customSqlBlanks.collectEntries { [it.key, it.value] })
            }
            insertQuery += "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (${sqlGenIDDTO.value},${sqlQuery ? "'${sqlQuery.replaceAll("'", "''")}'" : null},${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},${parent ?: 0},${query_flag},${parent ?: 0}); \n"
        } else if (superQuery?.queryType == QueryTypeEnum.SET_BUILDER) {
            Map dataMap = MiscUtil.parseJsonText(superQuery.JSONQuery)
            Map allMap = dataMap.all
            List containerGroupsList = allMap.containerGroups
            insertQuery = insertQuery + insertSqlStatementFromQuerySetStatement(containerGroupsList, sqlGenIDDTO, new SqlGenIDDTO(), 0, 0, joinOperator, blanks, customSqlBlanks,poiInputParams, locale, query_flag)
        }
        insertQuery += ""
        return insertQuery
    }

    String buildQueryFromJSONQuery(String JSONQuery, SqlGenIDDTO sqlGenIDDTO, int parent, String joinOperator, boolean hasBlanks, List blanks, Set<ParameterValue> poiInputParams, Locale locale, Integer query_flag ){
        Map dataMap = MiscUtil.parseJsonText(JSONQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String query = "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values ($sqlGenIDDTO.value,null,${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},${parent}, ${query_flag},${parent}); \n"
        query = query + insertSqlStatementFromQueryBuilderStatement(containerGroupsList, sqlGenIDDTO.value, new SqlGenIDDTO(), 0, 0, null, hasBlanks, blanks,poiInputParams, locale)
        return query
    }

    private String insertSqlStatementFromQueryBuilderStatement(
            def data, Integer setId, SqlGenIDDTO sqlGenIDDTO, int parent, Integer index, String joinOperator, boolean hasBlanks, List<ParameterValue> blanks, Set<ParameterValue> poiInputParams, Locale locale) {
        if (data instanceof Map && data.expressions) {
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            String groupInsert = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID) values ($setId,null,null,null, null,${sqlGenIDDTO.value},${data.keyword ? ("'${data.keyword.toUpperCase()}'") : null},null,${parent}) ;\n"
            return groupInsert + insertSqlStatementFromQueryBuilderStatement(data.expressions, setId, sqlGenIDDTO, sqlGenIDDTO.value, 0, data.keyword, hasBlanks, blanks,poiInputParams, locale)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                    query = query + insertSqlStatementFromQueryBuilderStatement(val, setId, sqlGenIDDTO, parent, i, joinOperator, hasBlanks, blanks,poiInputParams, locale)
                }
                return query
            } else {
                //TODO CUSTOM_INPUT LOGIC
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
                String apostrophe="";
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
                            outSql = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS) values ($setId,$index,'${data.field}','${data.op?.toUpperCase()}','${entry}',${parent},null,${customInput ? "'${customInput}'" : null},${parent},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null}) returning ROWID into lastInsertedRow; \n"
                        else
                            outSql += "UPDATE GTT_QUERY_DETAILS SET FIELD_VALUES=FIELD_VALUES||'${entry}' WHERE ROWID=lastInsertedRow;\n "
                    }
                } else{
                    boolean isRptCompare = data.value?.matches(Constants.RPT_INPUT_PATTERN_REGEX) ?: false
                    if (isRptCompare) {
                        data.value = data.value.replace(Constants.RPT_INPUT_PREFIX, '');
                    }
                    outSql = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS, IS_FIELD_COMPARE) values ($setId,$index,'${data.field}','${data.op?.toUpperCase()}', ${data.value ? "'${data.value}'" : null},${parent},null,${customInput ? "'${customInput}'" : null},${parent},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null}, ${isRptCompare ? 1 : 0}); \n"
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
            def data, SqlGenIDDTO sqlGenIDDTO, SqlGenIDDTO parentSqlGenIDTO, Integer parent, Integer index, String joinOperator, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale, Integer query_flag = null) {
        if (data instanceof Map && data.expressions) {
            parentSqlGenIDTO.value = parentSqlGenIDTO.value + 1
            String groupInsert = "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (${sqlGenIDDTO.value},null,${data.keyword ? "'${data.keyword.toUpperCase()}'" : null},${parentSqlGenIDTO.value},${query_flag},${parent}) ;\n"
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            return groupInsert + insertSqlStatementFromQuerySetStatement(data.expressions, sqlGenIDDTO, parentSqlGenIDTO, parentSqlGenIDTO.value, 0, data.keyword, blanks, customSqlBlanks,poiInputParams, locale, query_flag)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                    sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                    query = query + insertSqlStatementFromQuerySetStatement(val, sqlGenIDDTO, parentSqlGenIDTO, parent, i, joinOperator, blanks, customSqlBlanks, poiInputParams, locale, query_flag)
                }
                return query
            } else {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                return insertQueriesDataToTempTable(SuperQuery.load(data.query), sqlGenIDDTO, joinOperator, parent, blanks, customSqlBlanks, poiInputParams, locale,  query_flag)
            }
        }
    }


    String initializeForICSRPadder(BaseConfiguration baseConfiguration, Locale locale) {
        if ((baseConfiguration instanceof PeriodicReportConfiguration || baseConfiguration instanceof ExecutedPeriodicReportConfiguration) && baseConfiguration.periodicReportType == PeriodicReportTypeEnum.PADER) {
            SuperQuery icsrQuery = SuperQuery.findByIcsrPadderAgencyCasesAndOriginalQueryIdAndIsDeleted(true,0,false)
            if (icsrQuery) {
                return getInsertStatementsToInsert(icsrQuery, null, null, null, locale, baseConfiguration.excludeNonValidCases, baseConfiguration.owner, baseConfiguration.excludeDeletedCases)
            }
        }
        return null
    }

    String initializeForICSRPadder(ExecutorDTO executorDTO) {
        if (executorDTO.reportId && executorDTO.periodicReportTypeEnum == PeriodicReportTypeEnum.PADER) {
            SuperQuery icsrQuery = SuperQuery.findByIcsrPadderAgencyCasesAndOriginalQueryIdAndIsDeleted(true,0,false)
            if (icsrQuery) {
                return getInsertStatementsToInsert(icsrQuery, null, null, null, executorDTO.locale, executorDTO.excludeNonValidCases, executorDTO.owner, executorDTO.excludeDeletedCases)
            }
        }
        return null
    }

    boolean isCaseNumberExistsForTenant(String caseNumber, Integer versionNumber) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            return true
        }
        String query = "select count(1) as count from C_IDENTIFICATION where case_num = :caseNumber and version_num= :versionNumber and tenant_id = :tenantId"
        Sql sql = new Sql(dataSource_pva)
        boolean result = false
        try {
            result = sql.firstRow(query, [caseNumber: caseNumber, versionNumber: versionNumber, tenantId: Tenants.currentId() as Long]).count > 0
        } finally {
            sql?.close()
        }
        return result
    }

    boolean isCaseNumberExistsForTenantInPVCM(String caseNumber, Integer versionNumber) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
            return true
        }
        String query = "select count(1) as count from V_SAFETY_IDENTIFICATION where case_num = :caseNumber and version_num= :versionNumber and tenant_id = :tenantId"
        Sql sql = new Sql(dataSource_pva)
        boolean result = false
        try {
            result = sql.firstRow(query, [caseNumber: caseNumber, versionNumber: versionNumber, tenantId: Tenants.currentId() as Long]).count > 0
        } finally {
            sql?.close()
        }
        return result
    }


    Map getCaseDetails(String caseNumber, Integer versionNumber) {
        Map caseInfoMap = Holders.config.getProperty('caseInfoMap', Map)
        Map caseMultiMap = [:]
        Map dataMap = [:]
        caseInfoMap.each { k, v ->
            dataMap.put(k, [])
        }
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.call("{call p_case_details(?,?,?,?)}", [caseNumber, versionNumber, null, Sql.resultSet(OracleTypes.CURSOR)]) { info ->
                if (info) {
                    while(info.next()){
                        caseInfoMap.each { k, v ->
                            Map map = [:]
                            v.each { key, value ->
                                try {
                                    map[key] = info.getString(key.replaceAll("_"," "))
                                } catch (Throwable th) {
                                    log.error(th.getMessage())
                                }
                            }
                            dataMap["${k}"].add(map)
                        }
                    }
                }
            }
        }finally {
            sql?.close()
        }
        caseInfoMap.each { k, v ->
            caseMultiMap["${k}"] = dataMap["${k}"].unique()
        }
        return caseMultiMap
    }

    def getCaseMetadataDetails(String caseNumber, Integer versionNumber) {
        Map caseDataMultiMap = [:]
        Map caseMetadataInfo = Holders.config.caseMetadataInfo
        if(caseMetadataInfo) {
            Sql sql = new Sql(utilService.getReportConnection())
            Long caseId
            Long tenantId
            try {
                String query = "SELECT case_id, tenant_id FROM v_c_identification WHERE case_num = :caseNumber AND ROWNUM = 1 "
                def row =  sql.firstRow(query, [caseNumber: caseNumber])
                if(row){
                    caseId = row['case_id']
                    tenantId = row['tenant_id']
                    Map caseMultiMap = [:]
                    caseMetadataInfo.each { k, v ->
                        def dataList = fetchMetadata(k, v, caseId, tenantId, caseNumber, versionNumber, sql).unique()
                        dataList = dataList.collect { map ->
                            map.findAll { key, value ->
                                !(value instanceof Map && value.containsKey('hidden') && value.hidden == true)
                            }
                        }
                        caseMultiMap.put("${k}", dataList)
                    }
                    return caseMultiMap
                }else{
                    log.error("No case_id found for case_num: ${caseNumber} ")
                    return caseDataMultiMap
                }

            } catch (Exception e) {
                log.error("Exception caught in fetching case metadata for case num: ${caseNumber} and  vesion: ${versionNumber}, exception: ${e.message}")
            } finally {
                sql?.close()
            }
        }
        return caseDataMultiMap
    }

    def fetchMetadata(String section, Map fieldsMap, Long caseId, Long tenantId, String caseNumber, Integer versionNumber, Sql sql) {
        def dataList = []
        if (fieldsMap && fieldsMap.query && fieldsMap.fields) {
            try {
                sql.rows(fieldsMap.query,[case_id: caseId, version_num: versionNumber, tenant_id: tenantId]).each {
                    Map dataMap = new LinkedHashMap()
                    fieldsMap.fields.each { key, field ->
                        def newField = createField(field)
                        String label = field["label"]
                        try {
                            if (it[label] && it[label] instanceof Clob) {
                                if(it[label]) {
                                    newField['value'] = it[label].characterStream.text
                                }
                            } else {
                                newField['value'] = it[label]
                            }
                            dataMap[key] = newField
                        } catch (Throwable th) {
                            log.error("Error while getting data for section ${section} & field:${key} "+ th.getMessage())
                        }
                    }
                    dataList.add(dataMap)
                }
                if(dataList.empty){
                    fieldsMap.fields.each { k, v ->
                        v['value'] = null
                    }
                    dataList.add(fieldsMap.fields)
                }
            } catch (Exception e) {
                log.error("Exception in fetching case metadata for section: ${section} for case num: ${caseNumber} and  vesion: ${versionNumber}, exception: ${e.message}")
            }
        }
        return dataList
    }

    private def createField(def field){
        def fieldObj = [:]
        field.each {k,v ->
            fieldObj[k] = v
        }
        return fieldObj
    }

    boolean checkIfNewCasesForIcsr() {
        Sql sql = new Sql(dataSource_pva)
        boolean val = false
        try {
            sql.call("{? = call PKG_E2B_PROCESSING.f_check_start_e2b_generation()}", [Sql.NUMERIC]) { result ->
                val = (result > 0 ? true : false)
            }
        } catch (Exception ex) {
            log.error("check If New Cases For Icsr",ex)
        } finally {
            sql?.close()
        }
        return val
    }


    String initializeGTTForSpotfire(String productSelection, String productGroupSelection) {
        String insertStatement = "Begin " +
                "execute immediate('delete from gtt_filter_key_values'); "

        List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
        List<Map> productDetails = PVDictionaryConfig.ProductConfig.columns.collect { [:] }
        if (productSelection) {
            productDetails = MiscUtil?.getProductDictionaryValues(productSelection)
        }
        def productGroups = new JsonSlurper().parseText(productGroupSelection)
        productGroups.each { group ->
            DictionaryGroup productGroup = DictionaryGroup.get(group.id)
            String productGroupData = productGroup.fetchData()
            productDetails = MiscUtil.addProductDictionaryValues(productDetails, productGroupData)
        }
        productDetails.eachWithIndex { Map entry, int i ->
            int keyId = productViewsList.get(i).keyId
            entry.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
            }
        }
        insertStatement += " END;"
        insertStatement
    }

    boolean isCumulativeCaseSeriesAvailable(BaseConfiguration configuration) {
        return configuration && configuration instanceof ExecutedConfiguration && configuration.cumulativeCaseSeriesId && configuration.usedCaseSeriesId
    }

    String getTemplateAdditionalQuery(CaseLineListingTemplate template, Locale locale){
        SqlGenIDDTO sqlGenIDDTO = new SqlGenIDDTO()
        if(template.JSONQuery){
            return buildQueryFromJSONQuery(template.JSONQuery, sqlGenIDDTO, 0, null, false, [], [] as Set, locale, 2)
        }
        return null
    }

    void cleanGttCsvTables(ExecutedReportConfiguration executedConfiguration, Sql sql) {
        if(executedConfiguration) {
            log.info("Deleting GTT CSV TABLES")
            try {
                executedConfiguration.executedTemplateQueries.each {
                    Integer i = sql.call("{?= call PKG_PVR_APP_UTIL.F_DELETE_GTT_CSV_TABLES(?,?) }", [Sql.resultSet(OracleTypes.INTEGER), executedConfiguration.id, it.id])
                    if (i == 1) {
                        log.info("GTT CSV TABLE Deleted for Section : " + it.id)
                    } else {
                        log.warn("SQL Exception found while deleting GTT CSV Table for Section : " + it.id)
                    }
                }
            } catch (Exception e) {
                log.error("Error Occured During Deletion of GTT CSV TABLES - " + e.message)
            }
        }
    }

    void cleanGttCsvTables(ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery executedTemplateQuery, Sql sql) {
        if (executedConfiguration && executedTemplateQuery) {
            log.info("Deleting GTT CSV TABLES")
            try {

                Integer i = sql.call("{?= call PKG_PVR_APP_UTIL.F_DELETE_GTT_CSV_TABLES(?,?) }", [Sql.resultSet(OracleTypes.INTEGER), executedConfiguration.id, executedTemplateQuery.id])
                if (i == 1) {
                    log.info("GTT CSV TABLE Deleted for Section : " + executedTemplateQuery.id)
                } else {
                    log.warn("SQL Exception found while deleting GTT CSV Table for Section : " + executedTemplateQuery.id)
                }
            } catch (Exception e) {
                log.error("Error Occured During Deletion of GTT CSV TABLES - " + e.message)
            }
        }
    }

    @NotTransactional
    String getIncludedAllStudyDrugs(BaseConfiguration configuration) {
        List result = []
        if (configuration.includeAllStudyDrugsCases && (configuration.productSelection || configuration.validProductGroupSelection)) {
            def products = [:]
            if (configuration.productSelection) {
                JSON.parse(configuration.productSelection).each { key, val ->
                    products[key] = val.collect {
                        it.id
                    }.join(",")
                }
            }

            String productGroups = null
            if (configuration.validProductGroupSelection) {
                List ids = []
                MiscUtil.parseJsonText(configuration.validProductGroupSelection).each {
                    if (it.id) {
                        ids.add(it.id)
                    }
                }
                productGroups = ids.join(',')
            }

            Sql sql = new Sql(dataSource_pva)
            try {
                if(configuration.instanceOf(ExecutedReportConfiguration)) {
                    int considerOnlyPoi = configuration.considerOnlyPoi ? 1 : 0
                    int studyMedicationType = configuration.studyMedicationType ? 1 : 0
                    String gttSql = "Begin INSERT INTO GTT_REPORT_INPUT_PARAMS (param_key, param_value) VALUES ('CONSIDER_POI_FOR_UNBLINDED', '${considerOnlyPoi}');"+
                     "INSERT INTO GTT_REPORT_INPUT_PARAMS (param_key, param_value) VALUES ('IGNORE_STUDY_MED_TYPE', '${studyMedicationType}');"
                    gttSql += "END;"
                    sql.execute(gttSql)
                }
                sql.call("{?= call pkg_pvr_app_util.f_include_study_drug(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", [Sql.resultSet(OracleTypes.CURSOR), productGroups, products["1"], products["2"], products["3"], products["4"], products["5"], products["6"], products["7"], products["8"], products["9"], products["10"], products["11"], products["12"], products["13"], products["14"], products["15"], products["16"], products["17"], products["18"], products["19"], products["20"]]) { cursorResults ->
                    MiscUtil.resultSetToList(cursorResults).each { res ->
                        result.push(res.STUDY_NUM)
                    }
                }
            }catch (Exception ex) {
                log.error("get Included All Study Drugs",ex)
            } finally {
                sql?.close()
            }
        }
        return result.join(", ")
    }

    void setCurrentSqlInfoForCaseGeneration(Sql sql, Locale locale) {
        sql.call("{? = call PKG_KILL_SESSION.p_set_client_info()}", [Sql.VARCHAR]) { String sqlInfo ->
            Thread currentThread = Thread.currentThread()
            BigDecimal currentlyExecutingConfiguration = executorThreadInfoService.currentlyGeneratingCases.find {
                it.value.threadObj == currentThread
            }?.key
            if (currentlyExecutingConfiguration) {
                log.debug("Setting SqlInfo for allowing killing of Sql connection for : ${currentlyExecutingConfiguration} ${sqlInfo}")
                executorThreadInfoService.currentlyGeneratingCases.get(currentlyExecutingConfiguration)?.setCurrentSqlInfoId(sqlInfo)
            }
        }
        setSqlSessionContext(sql, locale)
    }

    void setCurrentSqlInfo(Sql sql, Locale locale) {
        sql.call("{? = call PKG_KILL_SESSION.p_set_client_info()}", [Sql.VARCHAR]) { String sqlInfo ->
            Thread currentThread = Thread.currentThread()
            Long currentlyExecutingConfiguration = executorThreadInfoService.currentlyRunning.find {
                it.value.threadObj == currentThread
            }?.key
            if (currentlyExecutingConfiguration) {
                log.debug("Setting SqlInfo for allowing killing of Sql connection for : ${currentlyExecutingConfiguration} ${sqlInfo}")
                executorThreadInfoService.currentlyRunning.get(currentlyExecutingConfiguration)?.setCurrentSqlInfoId(sqlInfo)
            }
        }
        setSqlSessionContext(sql, locale)
    }

    void setSqlSessionContext(Sql sql, Locale locale) {
        sql.execute("begin PKG_APP_LANG_CONTEXT.p_set_context_value(?); end;", [locale.toString()])
    }

    List createQuerySql(BaseTemplateQuery templateQuery, BaseConfiguration configuration, boolean hasQuery, String caseListInsertSql, String querySql, Long caseSeriesId, boolean needToUseAlreadyGeneratedCases, Locale locale) {
        if (templateQuery.usedQuery || configuration.productSelection || configuration.studySelection
                || (configuration.usedEventSelection) || configuration.excludeNonValidCases
                || configuration.suspectProduct || configuration.excludeDeletedCases) {
            querySql = processMultipleQueries(templateQuery, locale)
            caseListInsertSql = generateCaseListInsert(querySql, templateQuery.queryLevel, caseSeriesId?.toString(), needToUseAlreadyGeneratedCases)
        } else {
            hasQuery = false
        }
        return [hasQuery, caseListInsertSql, querySql]
    }

    private String processMultipleQueries(BaseTemplateQuery templateQuery, Locale locale) {
        //TODO: firstQuery will be the only query we execute after Configuration is changed.
        SuperQuery query = templateQuery.usedQuery

        String querySQL = null

        if (query) {
            switch (query?.queryType) {
                case QueryTypeEnum.QUERY_BUILDER:
                    querySQL = "${generateQuerySQL(templateQuery, query, false, false, 0, locale)}"
                    break;
                case QueryTypeEnum.SET_BUILDER:
                    querySQL = "${generateSetSQL(templateQuery, query, locale)}"
                    break;
                case QueryTypeEnum.CUSTOM_SQL:
                    querySQL = "${generateCustomQuerySQL(templateQuery, query)}"
                    break;
            }
        } else {
            querySQL = "${generateEmptyQuerySQL(templateQuery)}"
        }

        // Don't need to check dictionaries
        String result = "SELECT query.TENANT_ID, query.case_id, query.version_num"
        if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT || templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT) {
            result += ", query.prod_rec_num"
        }
        if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT) {
            // query must pass case id, product sequence number and product event sequence number (PVR-117)
            result += ", query.AE_REC_NUM"
        }
        if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
            result += ", query.processed_report_id"
        }
        result += " FROM ($querySQL) query"

        boolean whereAdded = false

        if (templateQuery.usedConfiguration.usedEventSelection) {
            def eventSelectionSQL = getQueryEventDictFilter(ExecutorDTO.create(templateQuery.usedConfiguration))
            String withClause = eventSelectionSQL[0]
            if (withClause) { // no with clause if only select llt for event
                result = "with $withClause $result"
            }

            if (templateQuery.usedConfiguration.productSelection && !templateQuery.usedConfiguration.suspectProduct) {
                result = """$result,
                        (${eventSelectionSQL[1]}) eventSelection
                        WHERE query.case_id = eventSelection.case_id
                         AND query.AE_REC_NUM = eventSelection.AE_REC_NUM"""
            } else if (templateQuery.usedConfiguration.productSelection && templateQuery.usedConfiguration.suspectProduct) {
                result = """$result,
                        (${eventSelectionSQL[1]}) eventSelection,
                        (SELECT cp.case_id, cp.prod_rec_num
                        FROM C_PROD_IDENTIFICATION cp, gtt_versions ver
                        WHERE cp.tenant_id = ver.TENANT_ID
                            and cp.case_id = ver.case_id
                            and cp.version_num = ver.version_num
                            and cp.DRUG_TYPE=1) caseProduct
                        WHERE query.case_id = eventSelection.case_id
                         AND query.case_id = caseProduct.case_id
                         AND query.AE_REC_NUM = eventSelection.AE_REC_NUM
                         AND query.prod_rec_num = caseProduct.prod_rec_num"""
            } else if (templateQuery.usedConfiguration.studySelection) {
                def studySelectionSQL = getQueryStudyDictFilter(ExecutorDTO.create(templateQuery.usedConfiguration))
                if (result.startsWith("with")) {
                    result = ", " + result.substring(4);
                }
                result = """with ${studySelectionSQL[0]} $result, (${studySelectionSQL[1]}) studySelection,
                        (${eventSelectionSQL[1]}) eventSelection WHERE query.case_id = eventSelection.case_id
                        AND query.AE_REC_NUM = eventSelection.AE_REC_NUM AND query.case_id = studySelection.case_id"""
            } else {
                result = """$result, (${eventSelectionSQL[1]}) eventSelection WHERE query.case_id = eventSelection.case_id
                        AND query.AE_REC_NUM = eventSelection.AE_REC_NUM"""
            }
            whereAdded = true

        } else {
            if (templateQuery.usedConfiguration.productSelection) {
                result = """$result
                        """
                whereAdded = false
            } else if (templateQuery.usedConfiguration.studySelection) {
                def studySelectionSQL = getQueryStudyDictFilter(ExecutorDTO.create(templateQuery.usedConfiguration))
                result = """with ${studySelectionSQL[0]} $result, (${studySelectionSQL[1]}) studySelection
                        WHERE query.case_id = studySelection.case_id"""
                whereAdded = true
            }
        }

        if (templateQuery.usedConfiguration.suspectProduct && templateQuery.usedConfiguration.productSelection && (!templateQuery.usedConfiguration.usedEventSelection)) {

            result += suspectProductSql()
            whereAdded = true
            log.debug("Suspect product value checked")
        }

        if (templateQuery.usedConfiguration.excludeNonValidCases) {
            SuperQuery nonValidQuery = SuperQuery.findByNonValidCasesAndIsDeletedAndOriginalQueryId(true,false,0L)
            if (nonValidQuery) {
                if (!whereAdded) {
                    result += " WHERE"
                } else {
                    result += " AND"
                }
                result += excludeNonValidCases(nonValidQuery, templateQuery, locale)
                log.debug("Excluding non-valid cases with 'Non-Valid Cases' query")

            } else {
                log.error("Non-valid query does not exist! None found by name: 'Non-Valid Cases'")
            }
        }
        if (templateQuery.usedConfiguration.excludeDeletedCases) {
            SuperQuery deletedQuery = SuperQuery.findByDeletedCasesAndIsDeletedAndOriginalQueryId(true,false,0L)
            if (deletedQuery) {
                if (!whereAdded) {
                    result += " WHERE"
                } else {
                    result += " AND"
                }
                result += excludeDeletedCases(deletedQuery, templateQuery, locale)
                log.debug("Excluding deleted cases with 'deleted Cases' query")

            } else {
                log.error("Deleted query does not exist! None found by name: 'Deleted Cases'")
            }
        }

        return result
    }

    List getManualCaseList(String term, int offset, Integer max, boolean isFetchTotalCount) {
        List results = []
        List bindVars = []
        Sql sql = new Sql(dataSource_pva)
        log.debug("Fetching cases for Manual caseList latest locked and latest unlocked searching for "+term)
        try {
            StringBuilder queryString = new StringBuilder("select * from VW_ICSR_MANUAL_CASELIST where CASE_NUM like ?")
            bindVars.add('%' + term + '%')

            if (!isFetchTotalCount) {
                queryString.append(" order by CASE_NUM ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")
                bindVars.addAll([offset, max])
            } else {
                queryString.append(" order by CASE_NUM ASC")
            }
            sql.eachRow(queryString.toString(), bindVars) { GroovyResultSet resultSet ->
                Map map = resultSet.toRowResult()
                results << [caseNumber: map["CASE_NUM"], version: map['VERSION_NUM']]
            }
        } finally {
            sql?.close()
        }
        return results
    }


    void localCpProc(Long caseId, Long versionNumber, Long exProfileId, String profileName, Long tenantId, Integer flagLocalCp, String prodHashCode, Long profileId, Long processedReportId, User user = null) {
        if (!user) {
            user = userService.getUser()
        }
        String username = user.fullName ?: user.username
        Sql sql = new Sql(dataSource_pva)
        log.info("Updating local cp flag for caseId : " +caseId+ " ,versionNumber : " + versionNumber+ " ,profileName : " +profileName+ " ,tenantId : "+tenantId + " ,flagLocalCp : "+flagLocalCp + ",username : " +username)
        try {
            //flagLocalCp=1 means user clicked on Local Cp Completed button and flagLocalCp=2 means user clicked on auto Generate button
            sql.execute("begin PKG_E2B_PROCESSING.P_UPDATE_FLAG_LOCAL_CP(?, ?, ?, ?, ?, ?, ?, ?, ?, ?); end;", [caseId, versionNumber, exProfileId, profileName, tenantId, flagLocalCp, username, prodHashCode ?: "-1", profileId, processedReportId])
        } catch (Exception ex) {
            log.error("exception while loading localCp ",ex)
        } finally {
            sql?.close()
        }
    }

    //to check if the selected case and version has multiple Products
    List checkProductList(String caseId, Long versionNumber, Long tenantId) {
        Sql sql = null
        List productList = []
        String query = """SELECT cp.hash_code as prod_hash_code,
                            coalesce(vtn.trade_name, cp.reptd_prod_name) AS product_name
                    FROM tx_identification cm
                    LEFT JOIN tx_prod_identification cp 
                            ON cp.case_id = cm.case_id
                            AND cp.version_num = cm.version_num
                            AND cp.tenant_id = cm.tenant_id
                    LEFT JOIN tx_prod_identification_fu cpif 
                            ON cp.case_id = cpif.case_id
                            AND cp.version_num = cpif.version_num
                            AND cp.tenant_id = cpif.tenant_id
                            AND cp.prod_rec_num = cpif.prod_rec_num
                    LEFT JOIN pvd_src_standard_decode pssd 
                            ON cp.tenant_id = pssd.tenant_id
                            AND cp.drug_role_id = pssd.src_id
                            AND pssd.key_id = 'CP_DRUG_ROLE_TYPE'
                    LEFT JOIN vw_tx_trade_name vtn 
                            ON cp.tenant_id = vtn.tenant_id
                            AND cpif.prod_auth_id = vtn.license_id
                            AND vtn.lang_id = f_get_lang_id('EN')
                    WHERE cm.case_id = ?
                        AND cm.tenant_id = ?
                        AND cm.version_num = ?
                        AND ( pssd.std_id = 1
                        OR pssd.std_id = 4 )
                        ORDER BY cp.rank_id"""
        try {
            sql = new Sql(dataSource_pva)
            sql.rows(query, [caseId, tenantId, versionNumber]).each {
                productList.add(it)
            }
            return productList
        } catch (Exception e) {
            log.error("Failed to load product list for caseId ${caseId}")
            throw e
        } finally {
            sql?.close()
        }
    }

    List checkCaseProductList(String caseId, Long version, Long tenantId) {
        Sql sql = null
        List caseProdList = []
        try {
            sql = new Sql(dataSource_pva)
            sql.call("{?= call PKG_PVR_ICSR_ROUTINE.F_GET_PROD_REC_NUM(?,?,?,?)}",
                    [Sql.resultSet(OracleTypes.CURSOR), caseId, version, tenantId, 1]) { cursorResult ->
                caseProdList = MiscUtil.resultSetToList(cursorResult)
            }
            return caseProdList
        } catch (Exception e) {
            log.error("Failed to load products for caseId ${caseId}")
            throw e
        } finally {
            sql?.close()
        }
    }

    List checkApprovalNumber(Long caseId, Long versionNumber, String prodHashCode, String recieverCountry, Long authId, Boolean flagPMDA, Long tenantId, Boolean isMultiReport) {
        Sql sql = null
        List approvalNumberList = []
        try {
            sql = new Sql(dataSource_pva)
            sql.call("{?= call PKG_E2B_PROCESSING.F_GET_AUTH_PARAMS(?,?,?,?,?,?,?,?,?)}",
                    [Sql.resultSet(OracleTypes.CURSOR), caseId, versionNumber, tenantId, prodHashCode, recieverCountry, authId, (flagPMDA==true) ? 1 : 0, (isMultiReport==true) ? 1 : 0, 1]) { cursorResult ->
                approvalNumberList = MiscUtil.resultSetToList(cursorResult).collect { [id: "${it.AUTH_ID}${Constants.CASE_VERSION_SEPARATOR}${it.RPT_CATEGORY_ID}", text: it.APPROVAL_NUMBER, prodHashCode: it.PROD_HASH_CODE] }
            }
            return approvalNumberList
        } catch (Exception e) {
            log.error("Failed to load Approval number for CaseId ${caseId}")
            throw e
        } finally {
            sql?.close()
        }
    }

    //to fetch CASE ID from V_SAFETY_IDENTIFICATION table against Case Number and Version Number.
    String fetchCaseIdFromSource(String caseNumber, Long version) {
        Sql sql = null
        String caseRecordSql = "SELECT tenant_id, CASE_ID FROM V_SAFETY_IDENTIFICATION WHERE case_num=? AND version_num=?"
        try {
            sql = new Sql(dataSource_pva)
            def caseInfo = sql.firstRow(caseRecordSql, [caseNumber, version])
            if (caseInfo) {
                return caseInfo.CASE_ID
            } else {
                return null
            }
        } catch (Exception e) {
            log.error("Failed to fetch caseId from case number ${caseNumber}")
            throw e
        } finally {
            sql?.close()
        }
    }

    String insertIntoGttForPVCMWorkflow(Long queryId, String caseId) {
        String insertStatement = "begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_tabulation''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_params''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_report_input_fields''); end;';" +
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_filter_key_values''); end;';"+
                "execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_TABULATION_MEASURES''); end;';"+
                "delete from GTT_QUERY_DETAILS; "+
                "delete from GTT_QUERY_SETS; " +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_QUERY','0');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_LEVEL','CASE');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SOURCE_TYPE','CENTRAL');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SELECTED_DATE_RANGE','CUMULATIVE');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_NAME','DQCR_EXECUTION_FOR_INTAKE_CASE:${caseId}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_ID',${queryId});" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_DQCR_EXECUTION', '1');" +
                " END;";
        return insertStatement;
    }

    void insertAckDetail(Long tenantId, Long processedReportId, File ackFile) {
        Sql sql = new Sql(utilService.getReportConnection())
        try {
            String ackText = ackFile.text.replaceAll("(?i)'","''")
            StringBuilder insertSql = new StringBuilder("Begin ")
            insertSql.append("MERGE INTO C_SUBMISSIONS_ACK TGT\n" +
                    "USING (SELECT ${tenantId} AS TENANT_ID, 2 AS FLAG_DB_SOURCE, 0 AS REC_TYPE, ${processedReportId} AS PROCESSED_REPORT_ID, '${ackFile.name}' AS FILENAME, '${ackText}' AS ACK, SYSTIMESTAMP AS REC_CREATION_TS FROM DUAL) SRC\n" +
                    "ON (TGT.PROCESSED_REPORT_ID = SRC.PROCESSED_REPORT_ID\n" +
                    "AND TGT.TENANT_ID = SRC.TENANT_ID\n" +
                    "AND TGT.FLAG_DB_SOURCE = SRC.FLAG_DB_SOURCE\n" +
                    "AND TGT.REC_TYPE = SRC.REC_TYPE \n" +
                    ")\n" +
                    "WHEN MATCHED THEN UPDATE SET \n" +
                    "TGT.FILENAME = SRC.FILENAME,\n" +
                    "TGT.ACK = TGT.ACK,\n" +
                    "TGT.REC_CREATION_TS = REC_CREATION_TS\n" +
                    "WHEN NOT MATCHED THEN \n" +
                    "INSERT (TENANT_ID,FLAG_DB_SOURCE, REC_TYPE, PROCESSED_REPORT_ID, FILENAME, ACK, REC_CREATION_TS)\n" +
                    "VALUES (SRC.TENANT_ID, SRC.FLAG_DB_SOURCE, SRC.REC_TYPE,SRC.PROCESSED_REPORT_ID, SRC.FILENAME, SRC.ACK, SRC.REC_CREATION_TS);")
            insertSql.append('END;')
            sql.execute(insertSql.toString().trim())
        }finally {
            sql?.close()
        }
    }

    void insertIntoGTTforAck(CaseAckSubmissionDTO caseAckSubmissionDTO){

        Sql sql = new Sql(utilService.getReportConnection())
        try {
            String insertStatement = ""
            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TENANT_ID', '${caseAckSubmissionDTO.tenantId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CASE_ID','${caseAckSubmissionDTO.caseId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VERSION_NUM','${caseAckSubmissionDTO.versionNumber}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PROCESSED_REPORT_ID','${caseAckSubmissionDTO.processedReportId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_MESSAGE_NUM','${caseAckSubmissionDTO.icsrMessageNumber}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TRANSMISSION_ACK_CODE', '${caseAckSubmissionDTO.transmissionAckCode}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SAFETY_REPORT_ID', '${caseAckSubmissionDTO.safetyReportId}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LOCAL_REPORT_NUM', '${caseAckSubmissionDTO.localReportNumber}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LOCAL_MESSAGE_NUM', '${caseAckSubmissionDTO.localMessageNumber}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_ACK_CODE', '${caseAckSubmissionDTO.reportAckCode}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ACK_MESSAGE_COMMENT', '${caseAckSubmissionDTO.ackMessageComment}');";

            insertStatement += "END;"
            sql.execute(insertStatement)
            sql.call("{call PKG_E2B_PROCESSING.p_pop_submissions_ack_info}");
        } finally {
            sql?.close()
        }
    }

    void insertAttachmentDetail(Long tenantId, Long processedReportId, File mergedFile, File ackFile, Date ackReceiveDate, String status) {
        def connection = utilService.getReportConnection()
        Sql sql = new Sql(connection)
        try {
            if(ackFile) {
                Clob attachmentAckText = connection.createClob()
                attachmentAckText.setString(1, ackFile.text?.replaceAll("(?i)'","''").toString())
                String updateQuery = "update c_submissions_attach set ack_filename_attach = ?, ack_file_attach = ?, ack_rec_ts_attach = ?, ack_status_attach = ? where tenant_id = ? and flag_db_source = ? and rec_type = ? and processed_report_id = ?"
                sql.execute(updateQuery, [ackFile.name, attachmentAckText, new Date().format(DateUtil.DATEPICKER_FORMAT_AM_PM), status, tenantId, 2, 0, processedReportId])
            } else {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream()
                ObjectOutputStream out = new ObjectOutputStream(bytes)
                out.writeObject(Files.readAllBytes(mergedFile.toPath()));
                out.flush()
                Blob mergedFileBytes = connection.createBlob()
                mergedFileBytes.setBytes(1, bytes.toByteArray())
                String insertQuery = "insert into c_submissions_attach (tenant_id,flag_db_source,rec_type,processed_report_id,filename_attach,file_attach,rec_ts_attach) values (?, ?, ?, ?, ?, ?, ?)"
                sql.execute(insertQuery, [tenantId, 2, 0, processedReportId, mergedFile.name, mergedFileBytes, new Date().format(DateUtil.DATEPICKER_FORMAT_AM_PM)])
            }
        } catch(Exception e) {
            log.error("Getting error while merging the data into c_submissions_attach table ", + e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    Integer getPVALanguageId(String locale) {
        Connection connection = utilService.getReportConnection()
        Sql sql = new Sql(connection)
        try {
            Integer langId = null
            sql.call("{?= call F_GET_LANG_ID(?)}", [Sql.INTEGER, locale.toUpperCase()]) { result ->
                langId = result
            }
            return langId
        } catch (Exception e) {
            log.error("Error while fetch lang id from Mart : "+e.getMessage())
        } finally {
            sql?.close()
        }
    }

    void updatingIcsrStatusAndDate(CaseSubmissionCO caseSubmissionCO, IcsrCaseTracking icsrCaseTrackingInstance){
        CaseStateUpdateDTO dto = new CaseStateUpdateDTO(executedIcsrTemplateQuery: ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId), caseNumber: caseSubmissionCO.caseNumber, versionNumber: caseSubmissionCO.versionNumber)
        dto.with {
            status = caseSubmissionCO.icsrCaseState?.toString()
            dueDate = new Timestamp(caseSubmissionCO.dueDate?.time)
            submissionDate = caseSubmissionCO.submissionDate
            comment = caseSubmissionCO.comment
            commentJ = caseSubmissionCO.commentJ
            justificationId = caseSubmissionCO.justificationId
            reportingDestination = caseSubmissionCO.reportingDestinations
            attachment = caseSubmissionCO?.submissionDocument
            attachmentFilename = caseSubmissionCO?.submissionFilename
            lateFlag = false
            processedReportId = caseSubmissionCO?.processedReportId
            updatedBy = caseSubmissionCO?.updatedBy
            localSubmissionDate = caseSubmissionCO?.localSubmissionDate
            timeZoneId = caseSubmissionCO?.timeZoneId
        }
        Sql sql = new Sql(utilService.getReportConnection())
        try {
            String insertStatement = ""
            insertStatement += "Begin Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_FROM_STATE', '${caseSubmissionCO.currentState}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ICSR_TO_STATE','${caseSubmissionCO.icsrCaseState}');";
            insertStatement += "END;"
            sql.execute(insertStatement)
            sql.query("{call PKG_E2B_PROCESSING.P_UPDATE_E2B_STATUS(:PROFILE_NAME, :QUERY_ID, :CASE_NUMBER, :VERSION_NUMBER, :PROCESSED_REPORT_ID , :STATUS, :SUBMISSION_DATE, :IS_LATE, :REPORTING_DESTINATION, :DUE_DATE, :COMMENT, :COMMENT_J, :JUSTIFICATION_ID, :ERROR, :SUBMISSION_DOCUMENT, :TRANSMISSION_DATE, :TRANSMITTED_DATE, :USER_NAME, :MODIFIED_DATE, :ACK_FILE_NAME, :ACK_DATE, :SUBMISION_FILENAME, :LOCAL_SUBMISSION_DATE, :TIME_ZONE_ID, :DATE_TRANSMISSION_ATTACH, :DATE_TRANSMITTED_ATTACH, :DATE_ACK_RECIEVED_ATTACH, :ATTACH_ACK_FILE_NAME, :PMDA_NUMBER, :AUTH_ID)}", [PROFILE_NAME: caseSubmissionCO.profileName, QUERY_ID: ExecutedIcsrTemplateQuery.read(icsrCaseTrackingInstance.exIcsrTemplateQueryId)?.usedQuery?.originalQueryId, CASE_NUMBER: caseSubmissionCO.caseNumber, VERSION_NUMBER: caseSubmissionCO.versionNumber, PROCESSED_REPORT_ID: dto.processedReportId, STATUS: dto.status, SUBMISSION_DATE: dto.submissionDateTime, IS_LATE: 0, REPORTING_DESTINATION: dto.reportingDestination, DUE_DATE: dto.dueDate, COMMENT: dto.comment, COMMENT_J: dto.commentJ, JUSTIFICATION_ID: dto.justificationId, ERROR: null, SUBMISSION_DOCUMENT: dto.submissionDocument, TRANSMISSION_DATE: null, TRANSMITTED_DATE: null, USER_NAME: dto.updatedBy, MODIFIED_DATE: new Timestamp(new Date().time), ACK_FILE_NAME: null, ACK_DATE: null, SUBMISION_FILENAME: dto.attachmentFilename, LOCAL_SUBMISSION_DATE: dto.localSubmissionDate, TIME_ZONE_ID: dto.timeZoneId, DATE_TRANSMISSION_ATTACH: null, DATE_TRANSMITTED_ATTACH: null, DATE_ACK_RECIEVED_ATTACH: null, ATTACH_ACK_FILE_NAME : null, PMDA_NUMBER : null, AUTH_ID : null]) { rs ->}

        } finally {
            sql?.close()
        }
    }
}
