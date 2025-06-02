package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.enums.*
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.json.JsonOutput
import com.rxlogix.mapping.*
import com.rxlogix.pvdictionary.event.*
import com.rxlogix.repo.RepoFileResource
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import net.sf.dynamicreports.report.constant.PageOrientation
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.rxlogix.localization.Localization
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartFile

import javax.sql.DataSource
import java.sql.SQLException
import java.text.ParseException
import java.text.SimpleDateFormat

@Transactional
class ImportService {

    def userService
    GrailsApplication grailsApplication
    def templateService
    def CRUDService
    DataSource dataSource_pva
    DataSource dataSource_faers
    def executedConfigurationService
    def queryService

    @NotTransactional
    List<Tuple2<ReportTemplate, Boolean>> importTemplates(JSONElement listOfTemplates) {
        if (!listOfTemplates) {
            return []
        }
        User currentUser = userService.getUser()
        List<Tuple2<ReportTemplate, Boolean>> templates = []
        for (JSONElement it in listOfTemplates) {
            Tuple2<ReportTemplate, Boolean> templateTuple
            ReportTemplate reportTemplate
            Boolean isReferenced
            try {
                templateTuple = getReportTemplateFromJSON(it, currentUser)
                reportTemplate = templateTuple.getFirst()
            } catch (Exception e){
                log.error("Error during Import Service -> importTemplates : ${e?.message} for template - ${it?.name}")
                if (templateTuple?.getSecond()) {
                    throw new Exception("Custom Report Fields already exists for template - ${it?.name}")
                } else  throw e
            }
            if (reportTemplate) {
                try {
                    CRUDService.save(reportTemplate)
                    if (!(reportTemplate instanceof ITemplateSet)) {
                        executedConfigurationService.createReportTemplate(reportTemplate)
                    }
                } catch (ValidationException ve) {
                    log.warn("Failed to import ${reportTemplate.name} due to validation error")
                }
                templates.add(templateTuple)
            }
        }
        return templates
    }

    Tuple2<ReportTemplate, Boolean> getReportTemplateFromJSON(JSONElement jsonElement, User user) {
        LinkedHashMap bindingMap = getBindingMapForLoading(jsonElement, user)

        //BindingMap entries common to all template types
        List<Tag> parsedTags = null
        if (jsonElement.tags) {
            parsedTags = jsonElement.tags.collect { current -> Tag.findByName(current.name) }
            bindingMap.putAt("tags", parsedTags)
        }

        //todo:  verify that this is only relevant for CLL and DT; if so, move to their private creation methods below
        ReportFieldInfoList parsedColumns = null
        Boolean isReferenced = false
        if (jsonElement?.columnList) {
            (parsedColumns, isReferenced) = getParsedColumns(jsonElement, parsedColumns)
            bindingMap.putAt("columnList", parsedColumns)
        }

        ReportTemplate reportTemplate = createReportTemplate(jsonElement, bindingMap)
        // Handle the Category, if applicable; without this, you'll have a TransientObjectException.
        if (reportTemplate?.name == ReportTemplate.MEDWATCH_TEMPLATE_NAME && jsonElement?.medWatch) {
            reportTemplate.medWatch = true
            MiscUtil.linkFixedTemplate(Constants.MEDWATCH_JRXML_FILENAME,"/jrxml", reportTemplate)
        }
        if (reportTemplate?.name == ReportTemplate.CIOMS_I_TEMPLATE_NAME && jsonElement?.ciomsI) {
            reportTemplate.ciomsI = true
            MiscUtil.linkFixedTemplate(Constants.CIOMS_I_JRXML_FILENAME, "/jrxml", reportTemplate)
        }
        if (jsonElement.userTemplates) {
            List<User> userList = jsonElement.userTemplates.collect { current -> User.findByUsername(current) }?.findAll { it }
            userList.each {
                reportTemplate.addToUserTemplates(new UserTemplate(user: it))
            }
        }
        if (jsonElement.userGroupTemplates) {
            List<UserGroup> userList = jsonElement.userGroupTemplates.collect { current -> UserGroup.findByName(current) }?.findAll { it }
            userList.each {
                reportTemplate.addToUserGroupTemplates(new UserGroupTemplate(userGroup: it))
            }
        }

        if (reportTemplate?.category) {
            def existingCategory = Category.findByName(reportTemplate.category.name)
            if (existingCategory) {
                reportTemplate.category = existingCategory
            } else {
                reportTemplate.category = new Category(name: reportTemplate.category.name, defaultName: reportTemplate.category.defaultName)
            }
        }

        if (jsonElement.shareWithUsers) {
            jsonElement.shareWithUsers.each { userTemplate ->
                User sharedUser = User.findByUsernameAndEnabled(userTemplate.username, true)
                if (sharedUser) {
                    reportTemplate.addToUserTemplates(new UserTemplate(user: sharedUser))
                }
            }
        }
        if (jsonElement.shareWithGroups) {
            jsonElement.shareWithGroups.each { userGroupTemplate ->
                UserGroup sharedUserGroup = UserGroup.findByNameAndIsDeleted(userGroupTemplate.name, false)
                if (sharedUserGroup) {
                    reportTemplate.addToUserGroupTemplates(new UserGroupTemplate(userGroup: sharedUserGroup))
                }
            }
        }
        if (jsonElement.fixedTemplate && !(reportTemplate.ciomsI || reportTemplate.medWatch)) {
            if (!reportTemplate.fixedTemplate) {
                FileResource mainReport = new FileResource(
                        name: "${reportTemplate.name}.jrxml",
                        label: reportTemplate.name,
                        fileType: FileResource.TYPE_JRXML)
                reportTemplate.fixedTemplate = new RepoFileResource()
                reportTemplate.fixedTemplate.copyFromClient(mainReport)
            }
            reportTemplate.fixedTemplate.name = jsonElement.fixedTemplate.name
            reportTemplate.fixedTemplate.data = getFixedTemplateData(jsonElement.fixedTemplate.data)
        }
        return [reportTemplate, isReferenced]
    }

    private byte[] getFixedTemplateData(data) {
        if (data != null) {
            if (data instanceof ArrayList)
                return data.collect { it as byte }
            else if (data instanceof byte[])
                return data
            else if (data instanceof String)
                return Base64.decoder.decode(data)
        }
        return null
    }


    ReportTemplate getTemplateSetFromJson(JSONElement jsonElement, User user) {
        LinkedHashMap bindingMap = getBindingMapForLoading(jsonElement, user)
        List<Tag> parsedTags = jsonElement.tags.collect { current -> Tag.findByName(current.name) }
        List<ReportTemplate> templatesInUse = []
        jsonElement.nestedTemplates?.each { current ->
            ReportTemplate template = ReportTemplate.findByNameAndOwnerAndOriginalTemplateIdAndIsDeleted(current.name, user, 0L, false)
            if (template) {
                templatesInUse.add(template)
            } else{
                List<ReportTemplate> qcTemplates = ReportTemplate.findAllByNameAndQualityCheckedAndOriginalTemplateIdAndIsDeleted(current.name, true, 0L, false)
                if (qcTemplates && !qcTemplates.isEmpty()) {
                    ReportTemplate latestTemplate = qcTemplates.max { it.lastUpdated }
                    templatesInUse.add(latestTemplate)
                } else {
                    log.error("nestedTemplate with the name ${current.name} doesn't exist")
                    throw new RuntimeException("nestedTemplate with name ${current.name} not found")
                }
            }
        }
        bindingMap.putAt("tags", parsedTags)
        bindingMap.putAt("nestedTemplates", templatesInUse)
        bindingMap.putAt("excludeEmptySections", jsonElement.excludeEmptySections ? true : false)
        bindingMap.putAt("linkSectionsByGrouping", jsonElement.linkSectionsByGrouping ? true : false)
        bindingMap.putAt("sectionBreakByEachTemplate", jsonElement.sectionBreakByEachTemplate ? true : false)
        // Handle the Category, if applicable; without this, you'll have a TransientObjectException.
        ReportTemplate reportTemplate = new TemplateSet(bindingMap)
        if (reportTemplate?.category) {
            def existingCategory = Category.findByName(reportTemplate.category.name)
            if (existingCategory) {
                reportTemplate.category = existingCategory
            } else {
                reportTemplate.category = new Category(name: reportTemplate.category.name, defaultName: reportTemplate.category.defaultName)
            }
        }
        if (jsonElement.userTemplates) {
            List<User> userList = jsonElement.userTemplates.collect { current -> User.findByUsername(current) }?.findAll { it }
            userList.each {
                reportTemplate.addToUserTemplates(new UserTemplate(user: it))
            }
        }
        if (jsonElement.userGroupTemplates) {
            List<UserGroup> userList = jsonElement.userGroupTemplates.collect { current -> UserGroup.findByName(current) }?.findAll { it }
            userList.each {
                reportTemplate.addToUserGroupTemplates(new UserGroupTemplate(userGroup: it))
            }
        }

        if (jsonElement.fixedTemplate) {
            if (!reportTemplate.fixedTemplate) {
                FileResource mainReport = new FileResource(
                        name: "${reportTemplate.name}.jrxml",
                        label: reportTemplate.name,
                        fileType: FileResource.TYPE_JRXML)
                reportTemplate.fixedTemplate = new RepoFileResource()
                reportTemplate.fixedTemplate.copyFromClient(mainReport)
            }
            reportTemplate.fixedTemplate.name = jsonElement.fixedTemplate.name
            reportTemplate.fixedTemplate.data = getFixedTemplateData(jsonElement.fixedTemplate.data)
        }
        return reportTemplate
    }

    private List<DataTabulationColumnMeasure> getSavedColumnMeasureList(JSONElement it) {
        List<DataTabulationColumnMeasure> savedColumnMeasureList = []
        if (it?.columnMeasureList) {
            it.columnMeasureList.each { columnMeasure ->
                DataTabulationColumnMeasure dtColumnMeasure = new DataTabulationColumnMeasure(showTotalCumulativeCases: columnMeasure?.showTotalCumulativeCases,showTotalIntervalCases: columnMeasure?.showTotalIntervalCases)
                columnMeasure.measures.each { measure ->
                    CaseLineListingTemplate drillDownTemplate = measure.drillDownTemplate?.name?CaseLineListingTemplate.findByNameAndOriginalTemplateIdAndIsDeleted(measure.drillDownTemplate.name,0l,false):null
                    if(measure.drillDownTemplate?.name && !drillDownTemplate){
                        log.error("drillDownTemplate with the name ${measure.drillDownTemplate.name} doesn't exist")
                        throw new Exception("drillDownTemplate with name ${measure.drillDownTemplate.name} not found")
                    }
                    dtColumnMeasure.addToMeasures(new DataTabulationMeasure(name: measure?.name, dateRangeCount: CountTypeEnum.valueOf(measure.dateRangeCount.name),
                            customPeriodFrom: measure?.customPeriodFrom, customPeriodTo: measure?.customPeriodTo,
                            percentageOption: PercentageOptionEnum.valueOf(measure?.percentageOption?.name),
                            showSubtotalRowAfterGroups: measure.showSubtotalRowAfterGroups,
                            valuesChartType:measure?.valuesChartType,percentageChartType:measure?.percentageChartType,drillDownTemplate:drillDownTemplate,
                            showTotalRowOnly: measure?.showTotalRowOnly, showTotalAsColumn: measure?.showTotalAsColumn, colorConditions: measure?.colorConditions,
                            type: MeasureTypeEnum.valueOf(measure?.type?.name), showTotal: measure?.showTotal ?: false, relativeDateRangeValue: measure?.relativeDateRangeValue ?: 1,
                            showTopX: measure?.showTopX ?: false, topXCount: measure?.topXCount))
                }

                ReportFieldInfoList dtColumns = new ReportFieldInfoList()
                columnMeasure.columnList.each {
                    if (it.toString() != "null") {
                        it.remove('reportField')
                        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                        reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
                        if (!reportFieldInfo.reportField) {
                            log.error("ReportField not found for name : ${it.reportFieldName}")
                        }
                        if (it.sortEnumValue) {
                            reportFieldInfo.sort = SortEnum.valueOfName(it.sortEnumValue)
                        }
                        dtColumns.addToReportFieldInfoList(reportFieldInfo)
                    }
                }
                dtColumns.save(failOnError: true)
                dtColumnMeasure.columnList = dtColumns

                savedColumnMeasureList.add(dtColumnMeasure)
            }
        }
         return savedColumnMeasureList
    }

    private List<DataTabulationMeasure> getSavedMeasures(JSONElement it) {
        List<DataTabulationMeasure> savedMeasures = []
        if (it?.measures) {
            it?.measures?.each { measure ->
                savedMeasures.add(new DataTabulationMeasure(name: measure?.name, dateRangeCount: CountTypeEnum.valueOf(measure.dateRangeCount.name),
                        customPeriodFrom: measure?.customPeriodFrom, customPeriodTo: measure?.customPeriodTo,
                        percentageOption: PercentageOptionEnum.valueOf(measure?.percentageOption?.name),
                        showSubtotalRowAfterGroups: measure.showSubtotalRowAfterGroups,
                        showTotalRowOnly: measure?.showTotalRowOnly, showTotalAsColumn: measure?.showTotalAsColumn,
                        type: MeasureTypeEnum.valueOf(measure?.type?.name), showTotal: measure?.showTotal, relativeDateRangeValue: measure?.relativeDateRangeValue?:1))
            }

        }

        return savedMeasures
    }

    private Set<CustomSQLValue> getParsedCustomSQLValues(JSONElement it) {
        Set<CustomSQLValue> parsedCustomSQLValues = []
        if (it?.customSQLValues) {
            it?.customSQLValues?.each { customSQLValue ->
                parsedCustomSQLValues.add(new CustomSQLValue(key: customSQLValue.key, value: customSQLValue.value).save())
            }
        }

        return parsedCustomSQLValues
    }

    private ReportFieldInfoList getParsedRows(JSONElement it, String property) {
        ReportFieldInfoList parsedRows = null
        if (it?.getAt(property)) {
            parsedRows = new ReportFieldInfoList()
            it.getAt(property).each {
                it.remove('reportField')
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
                if (!reportFieldInfo.reportField) {
                    log.error("ReportField not found for name : ${it.reportFieldName}")
                }
                parsedRows.addToReportFieldInfoList(reportFieldInfo)
            }
            parsedRows.save(failOnError: true)
        }
        return parsedRows
    }

    private ReportFieldInfoList getParsedServiceCols(JSONElement it) {
        ReportFieldInfoList parsedServiceCols = new ReportFieldInfoList()
        it.serviceColumnList.each {
            it.remove('reportField')
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            parsedServiceCols.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedServiceCols.save(failOnError: true)
        return parsedServiceCols
    }

    private ReportFieldInfoList getParsedRowCols(JSONElement it, ReportFieldInfoList parsedRowCols) {
        parsedRowCols = new ReportFieldInfoList()
        it.rowColumnList.each {
            it.remove('reportField')
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            parsedRowCols.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedRowCols.save(failOnError: true)
        return parsedRowCols
    }

    private ReportFieldInfoList getParsedGrouping(JSONElement it, ReportFieldInfoList parsedGrouping) {
        parsedGrouping = new ReportFieldInfoList()
        it.groupingList.each {
            it.remove('reportField')
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName,false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            if (it.sortEnumValue) {
                reportFieldInfo.sort = SortEnum.valueOfName(it.sortEnumValue)
            }
            parsedGrouping.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedGrouping.save(failOnError: true)
        return parsedGrouping
    }

    private Tuple2<ReportFieldInfoList, Boolean> getParsedColumns(JSONElement it, ReportFieldInfoList parsedColumns) {
        Boolean isReferenced = false
        parsedColumns = new ReportFieldInfoList()
        it.columnList.each {
            it.remove('reportField')
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName,false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            if(reportFieldInfo.drillDownTemplate) {
                CaseLineListingTemplate drillDownTemplate = CaseLineListingTemplate.findByNameAndOriginalTemplateIdAndIsDeleted(reportFieldInfo.drillDownTemplate.name,0l,false)
                if(!drillDownTemplate){
                    log.error("drillDownTemplate with the name ${reportFieldInfo.drillDownTemplate.name} doesn't exist")
                    throw new Exception("drillDownTemplate with name ${reportFieldInfo.drillDownTemplate.name} not found")
                }
                reportFieldInfo.drillDownTemplate = drillDownTemplate
            }
            CustomReportField customReportField = reportFieldInfo.customField
            if(customReportField) {
                customReportField.reportField = reportFieldInfo.reportField
                customReportField.fieldGroup = ReportFieldGroup.findByName(it.customFieldGroupName)
                CustomReportField customReportField1 = CustomReportField.findByCustomNameIlikeAndIsDeleted(customReportField.customName,false)
                if (customReportField1 || Localization.countByTextIlike(customReportField.customName)){
                    if (!customReportField.checkEquals(customReportField1)){
                        log.error("Custom Report Field -> ${customReportField.customName} already exists")
                        throw new Exception("Custom Report Field with name ${customReportField.customName} already exists.")
                    } else {
                        log.warn("Custom Report Field -> ${customReportField.customName} already exists and is referenced in the template being imported.")
                        isReferenced = true
                        reportFieldInfo.customField = customReportField1
                    }
                }
                else {
                    customReportField.save()
                }
            }
            if (it.sortEnumValue) {
                reportFieldInfo.sort = SortEnum.valueOfName(it.sortEnumValue)
            }
            parsedColumns.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedColumns.save(failOnError: true)
        return [parsedColumns, isReferenced]
    }

    @NotTransactional
    List<SuperQuery> importQueries(JSONElement listOfQueries) {
        if (!listOfQueries) {
            return []
        }
        User currentUser = userService.getUser()
        List<SuperQuery> queries = []
        for (JSONElement it in listOfQueries) {
            SuperQuery tempQuery = getSuperQueryFromJson(it, currentUser)
            if (tempQuery) {
                try {
                    CRUDService.save(tempQuery)
                    if(!(tempQuery instanceof QuerySet)) {
                        queryService.createExecutedQuery(tempQuery)
                    }
                } catch(ValidationException ve){
                    log.warn("Failed to import ${tempQuery.name}")
                }
                // only when you flush the session that the child domain instances have their IDs set.
                queries.add(tempQuery)
            }
        }

        return queries
    }

    SuperQuery getSuperQueryFromJson(JSONElement jsonElement, User user) {
        def parsedTags = jsonElement.tags.collect { current -> Tag.findByName(current.name) }
        SuperQuery tempQuery = null
        if (jsonElement.queryType.name == QueryTypeEnum.QUERY_BUILDER.name()) {
            tempQuery = createQuery(jsonElement, parsedTags, user)
        } else if (jsonElement.queryType.name == QueryTypeEnum.SET_BUILDER.name()) {
            //Do not attempt the load QuerySets.  This is not supported at this time. Need to work more on getQuerySetFromJson
        } else if (jsonElement.queryType.name == QueryTypeEnum.CUSTOM_SQL.name()) {
            tempQuery = createCustomSQLQuery(jsonElement, parsedTags, user)
        }

        if (tempQuery.queryType == QueryTypeEnum.QUERY_BUILDER) {
            tempQuery.queryExpressionValues.each {
                if (!it.reportField.querySelectable) {
                    throw new Exception("Report Field ${it.reportField.getDisplayName(userService.currentUser.preference.locale)} - ${it.reportField.name} is not Query Selectable")
                }
            }
        }

        if (jsonElement.shareWithUsers) {
            jsonElement.shareWithUsers.each { userQuery ->
                User sharedUser = User.findByUsernameAndEnabled(userQuery.username, true)
                if (sharedUser) {
                    tempQuery.addToUserQueries(new UserQuery(user: sharedUser))
                }
            }
        }
        if (jsonElement.shareWithGroups) {
            jsonElement.shareWithGroups.each { userGroupQuery ->
                UserGroup sharedUserGroup = UserGroup.findByNameAndIsDeleted(userGroupQuery.name, false)
                if (sharedUserGroup) {
                    tempQuery.addToUserGroupQueries(new UserGroupQuery(userGroup: sharedUserGroup))
                }
            }
        }
        return tempQuery
    }

    private Query createQuery(JSONElement it, List<Tag> parsedTags, User currentUser) {
        Query tempQuery = new Query(
                queryType: QueryTypeEnum.QUERY_BUILDER,
                name: it.name,
                icsrPadderAgencyCases: it.icsrPadderAgencyCases ?: false,
                description: it.description,
                modifiedBy: currentUser.username,
                createdBy: currentUser.username,
                tags: parsedTags,
                JSONQuery: it.JSONQuery,
                hasBlanks: it.hasBlanks,
                owner: currentUser,
                queryTarget: it.queryTarget?.name ? QueryTarget.valueOf(it.queryTarget?.name) : QueryTarget.REPORTS,
                qualityChecked: it.qualityChecked ?: false,
                reassessListedness: it?.reassessListedness ? ReassessListednessEnum.valueOf(it?.reassessListedness?.name) : null,
                reassessListednessDate: it?.reassessListedness && it.reassessListedness.name == ReassessListednessEnum.CUSTOM_START_DATE.name() && it?.reassessListednessDate ? tryParse(it.reassessListednessDate) : null,
                reassessForProduct: it?.reassessForProduct ?: false, nonValidCases: it.nonValidCases ?: false,deletedCases:it.deletedCases ?: false)

        it.queryExpressionValues?.each {
            QueryExpressionValue qev = new QueryExpressionValue(key: it.key, value: "",
                    reportField: ReportField.findByNameAndIsDeleted(it.reportField.name, false),
                    operator: QueryOperatorEnum.valueOf(it.operator.name), specialKeyValue: it.specialKeyValue)
            if (!qev.reportField) {
                log.error("ReportField not found for name : ${it.reportField.name} used in query : ${tempQuery.name}")
            }

            tempQuery.addToQueryExpressionValues(qev)
        }
        return tempQuery
    }

    private CustomSQLQuery createCustomSQLQuery(JSONElement it, List<Tag> parsedTags, User currentUser) {
        CustomSQLQuery tempQuery = new CustomSQLQuery(queryType: QueryTypeEnum.valueOf(it.queryType.name), owner: currentUser,
                name: it.name, description: it.description, modifiedBy: currentUser.username, icsrPadderAgencyCases: it.icsrPadderAgencyCases ?: false,
                createdBy: currentUser.username, tags: parsedTags, JSONQuery: it.JSONQuery, hasBlanks: it.hasBlanks,
                customSQLQuery: it.customSQLQuery, qualityChecked: it.qualityChecked ?: false)

        it.customSQLValues?.each {
            tempQuery.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
        }

        return tempQuery
    }

    QuerySet getQuerySetFromJson(JSONElement jsonElement, User user) {
        def parsedTags = jsonElement.tags.collect { current -> Tag.findByName(current.name) }
        // QuerySets are dependent on existing queries.
        List<SuperQuery> queriesInUse = []
        String JSONQuery = jsonElement.JSONQuery

        jsonElement.queries.each { currentQuery ->
            SuperQuery query = SuperQuery.findByNameAndOriginalQueryIdAndIsDeletedAndOwner(currentQuery.name, 0L, false, user)
            if (query) {
                queriesInUse.add(query)
            } else {
                throw new Exception("Unable to find a query with the name: ${currentQuery.name} and owner: ${user.username}")
            }

            def oldId = currentQuery.id
            def newId = query?.id
            JSONQuery = JSONQuery.replaceAll(/"query": "$oldId"/, '"query": "' + newId?.toString() + '"')
        }

        QuerySet querySet = new QuerySet(queryType: QueryTypeEnum.SET_BUILDER, name: jsonElement.name,
                description: jsonElement.description, modifiedBy: user.username, createdBy: user.username,
                tags: parsedTags, JSONQuery: JSONQuery, hasBlanks: jsonElement.hasBlanks, owner: user,
                queries: queriesInUse, qualityChecked: jsonElement.qualityChecked, queryTarget: jsonElement.queryTarget?.name ? QueryTarget.valueOf(jsonElement.queryTarget?.name) : QueryTarget.REPORTS)

        if (jsonElement.shareWithUsers) {
            jsonElement.shareWithUsers.each { userQuery ->
                User sharedUser = User.findByUsernameAndEnabled(userQuery.username, true)
                if (sharedUser) {
                    querySet.addToUserQueries(new UserQuery(user: sharedUser))
                }
            }
        }
        if (jsonElement.shareWithGroups) {
            jsonElement.shareWithGroups.each { userGroupQuery ->
                UserGroup sharedUserGroup = UserGroup.findByNameAndIsDeleted(userGroupQuery.name, false)
                if (sharedUserGroup) {
                    querySet.addToUserGroupQueries(new UserGroupQuery(userGroup: sharedUserGroup))
                }
            }
        }
        querySet
    }

    Set<String> getDuplicates(List<String> list) {
        Set<String> lump = new HashSet<String>()
        Set<String> dupl = new HashSet<String>()
        list?.each {
            if (lump.contains(it))
                dupl.add(it)
            else
                lump.add(it)
        }
        return dupl
    }

    @ReadOnly('pva')
    Map<String, List> getValidInvalidValues(List values, String fieldName, String lang, boolean isFaersTarget) {
        log.debug("Validating for field name: ${fieldName}")
        Long tenantId = Tenants.currentId() as Long
        List validValues = []
        Set invalidValues = values
        if (values && fieldName) {
            if (fieldName in [CaseLineListingTemplate.CLL_TEMPLATE_REPORT_FIELD_NAME, CaseLineListingTemplate.CLL_TEMPLATE_J_REPORT_FIELD_NAME]) {
                values.collate(500).each { list ->
                    validValues += CaseInfo.findAllValidValue(list).list()
                }
                invalidValues = values - validValues
            }

            if(fieldName in CaseLineListingTemplate.CLL_TEMPLATE_LAM_REPORT_FIELD_NAME){
                values.collate(500).each { list ->
                    validValues += CaseInfoLam.findAllValidValue(list).list()
                }
                invalidValues = values - validValues
            }

            ReportField reportField = ReportField.findByName(fieldName)
            //We are validating only for those for which Domain class do exist.
            if (reportField?.isImportValidatable()) {
                if (reportField.listDomainClass?.name) {
                    if (reportField.isAutocomplete) {
                        Sql sql = new Sql(isFaersTarget ? dataSource_faers : dataSource_pva)
                        try {
                            String columnName = ''
                            String sqlString = ''
                            def result
                            String validateValues = ''
                            values.collate(500)?.each { inputValue ->
                                validateValues = "'" + inputValue.collect {
                                    it.replaceAll("(?i)'", "''").toUpperCase()
                                }?.join("','") + "'"
                                sqlString = reportField.getLmSql(lang)?.replace("like ?", "in (${validateValues})")?.replace("LIKE :SEARCH_TERM", "in (${validateValues})") //TO handle Non cache and Auto queries. Also added tenant parameter.
                                result = sql.rows(sqlString, [TENANT_ID: tenantId]) { meta ->
                                    columnName = meta.getColumnName(1)
                                }
                                validValues += result.collect { it.getProperty(columnName) }
                            }
                            List valuesToCompareWith = validValues*.replaceAll("(?i)'", "''")*.toUpperCase()
                            validValues = values.findAll {
                                it.replaceAll("(?i)'", "''").toUpperCase() in valuesToCompareWith
                            }
                            invalidValues = values - validValues
                        } catch (SQLException e) {
                            log.error("Could not Validate autocomplete SQL for ReportField: ${reportField.name}, ${e.message}")
                        } finally {
                            sql?.close()
                        }
                    } else {
                        List<Object> referenceValues = grailsApplication.mainContext.reportFieldService.getListValues(reportField, reportField.listDomainClass.name, lang)*.toUpperCase()
                        validValues = values.findAll { it.toUpperCase() in referenceValues }
                        invalidValues = values - validValues
                    }
                } else if (reportField.dictionaryType && reportField.dictionaryLevel) {
                    values = values.collect {it.replace("'","''")}
                    switch (reportField.dictionaryType) {
                        case DictionaryTypeEnum.EVENT:
                            validValues = validateValuesFromEventDic(values, reportField.dictionaryLevel)*.toUpperCase()
                            break
                        case DictionaryTypeEnum.STUDY:
                            validValues = validateValuesFromStudyDic(values, reportField.dictionaryLevel)*.toUpperCase()
                            break
                        case DictionaryTypeEnum.PRODUCT:
                            validValues = validateValuesFromProductDic(values, reportField.dictionaryLevel)*.toUpperCase()
                            break

                    }
                    values = values.collect {it.replace("''","'")}
                    validValues = values.findAll {it.toUpperCase() in validValues}
                    invalidValues = values - validValues
                }
            }
        }
        return [validValues: validValues.unique(), invalidValues: invalidValues as List]
    }

    private List validateValuesFromEventDic(List values, int level) {
        List validValues = []
        switch (level) {
            case 1:
                validValues = fetchValidValuesByName(values, validValues, LmEventDicView1, "SOC_NAME")
                break
            case 2:
                validValues = fetchValidValuesByName(values, validValues, LmEventDicView2, "HLGT_NAME")
                break
            case 3:
                validValues = fetchValidValuesByName(values, validValues, LmEventDicView3, "HLT_NAME")
                break
            case 4:
                validValues = fetchValidValuesByName(values, validValues, LmEventDicView4, "PT_NAME")
                break
            case 5:
                validValues = fetchValidValuesByName(values, validValues, LmEventDicView5, "LLT_NAME")
                break
            case 6:
                validValues = fetchValidValuesByName(values, validValues, LmEventDicView6, "SYN")
                break

        }
        return validValues
    }

    private List validateValuesFromProductDic(List values, int level) {
        List validValues = []
        switch (level) {
            case 1:
                validValues = fetchValidValuesByIngredient(values, validValues, LmIngredient, "INGREDIENT")
                break
            case 2:
                validValues = fetchValidValuesByName(values, validValues, LmProductFamily, "FAMILY_NAME")
                break
            case 3:
                validValues = fetchValidValuesByName(values, validValues, LmProduct, "PRODUCT_NAME")
                break
            case 4:
                validValues = fetchValidValuesByTradeName(values, validValues, LmLicense, "TRADE_NAME_APPROVAL_NUMBER")
                break
        }
        return validValues
    }

    private List validateValuesFromStudyDic(List values, int level) {
        List validValues = []
        switch (level) {
            case 1:
                validValues = fetchValidValuesByDescription(values, validValues, LmProtocols, "PROTOCOL_DESCRIPTION")
                break
            case 2:
                validValues = fetchValidValuesByStudyNum(values, validValues, LmStudies, "STUDY_NUM")
                break
            case 3:
                validValues = fetchValidValuesByName(values, validValues, LmDrugWithStudy, "PRODUCT_NAME")
                break
        }
        return validValues
    }

    private ReportTemplate createReportTemplate(JSONElement jsonElement, Map bindingMap) {
        if (jsonElement.templateType.name == TemplateTypeEnum.CASE_LINE.name()) {
            return createCaseLineListingTemplate(jsonElement, bindingMap)
        } else if (jsonElement.templateType.name == TemplateTypeEnum.DATA_TAB.name()) {
            return createDataTabulationTemplate(jsonElement, bindingMap)
        } else if (jsonElement.templateType.name == TemplateTypeEnum.NON_CASE.name()) {
            return createNonCaseTemplate(jsonElement, bindingMap)
        } else if (jsonElement.templateType.name == TemplateTypeEnum.CUSTOM_SQL.name()) {
            return createCustomSQLTemplate(jsonElement, bindingMap)
        } else if (jsonElement.templateType.name == TemplateTypeEnum.ICSR_XML.name()) {
            return createXMLTemplate(jsonElement, bindingMap)
        } else if (jsonElement.templateType.name == TemplateTypeEnum.TEMPLATE_SET.name()) {
            //Do not attempt the load TemplateSets. This is not supported at this time. Need to fix getTemplateSetFromJson
        }
        throw new UnsupportedOperationException("Import of template type ${jsonElement.templateType.name} is not supported")
    }

    private createCaseLineListingTemplate(JSONElement it, Map bindingMap) {
        if (it?.groupingList) {
            ReportFieldInfoList parsedGrouping = null
            parsedGrouping = getParsedGrouping(it, parsedGrouping)
            bindingMap.putAt("groupingList", parsedGrouping)
        }

        if (it?.rowColumnList) {
            ReportFieldInfoList parsedRowCols = null
            parsedRowCols = getParsedRowCols(it, parsedRowCols)
            bindingMap.putAt("rowColumnList", parsedRowCols)
        }

        if (it?.serviceColumnList) {
            ReportFieldInfoList parsedServiceCols = getParsedServiceCols(it)
            bindingMap.putAt("serviceColumnList", parsedServiceCols)
        }

        bindingMap.putAt("JSONQuery", it.JSONQuery)
        bindingMap.putAt("pageBreakByGroup", it.pageBreakByGroup)
        bindingMap.putAt("columnShowTotal", it.columnShowTotal)
        bindingMap.putAt("hideTotalRowCount", it.hideTotalRowCount)
        bindingMap.putAt("columnShowSubTotal", it.columnShowSubTotal ?: false)
        bindingMap.putAt("columnShowDistinct", it.columnShowDistinct)

        ReportTemplate reportTemplate = new CaseLineListingTemplate(bindingMap)
        templateService.fillCLLTemplateServiceFields(reportTemplate)
        return reportTemplate
    }


    private createDataTabulationTemplate(JSONElement it, Map bindingMap) {
        ReportFieldInfoList groupingList =  getParsedRows(it, "groupingList")
        ReportFieldInfoList rowList =  getParsedRows(it, "rowList")
        List<DataTabulationMeasure> savedMeasures = getSavedMeasures(it)
        List<DataTabulationColumnMeasure> savedColumnMeasureList = getSavedColumnMeasureList(it)

        bindingMap.putAt("groupingList", groupingList)
        bindingMap.putAt("rowList", rowList)
        bindingMap.putAt("showChartSheet", it.showChartSheet ?: false)
        bindingMap.putAt("supressHeaders", it.supressHeaders ?: false)
        bindingMap.putAt("supressRepeatingExcel", it.supressRepeatingExcel ?: false)
        bindingMap.putAt("pageBreakByGroup", it.pageBreakByGroup ?: false)
        bindingMap.putAt("drillDownToCaseList", it.drillDownToCaseList ?: false)
        bindingMap.putAt("transposeOutput", it.transposeOutput ?: false)
        bindingMap.putAt("positiveCountOnly", it.positiveCountOnly ?: false)
        bindingMap.putAt("allTimeframes", it.allTimeframes ?: false)
        bindingMap.putAt("chartCustomOptions", it?.chartCustomOptions)
        bindingMap.putAt("maxChartPoints", it?.maxChartPoints)
        bindingMap.putAt("chartExportAsImage", it?.chartExportAsImage?: false)
        bindingMap.putAt("worldMap", it?.worldMap?: false)
        bindingMap.putAt("worldMapConfig", it?.worldMapConfig?: false)
        bindingMap.putAt("JSONQuery", it.JSONQuery)

        ReportTemplate reportTemplate = new DataTabulationTemplate(bindingMap)
        savedMeasures.each {
            reportTemplate.addToMeasures(it) //TODO need to remove.
        }
        savedColumnMeasureList.each {
            reportTemplate.addToColumnMeasureList(it)
        }
        return reportTemplate
    }

    private createCustomSQLTemplate(JSONElement it, Map bindingMap) {
        Set<CustomSQLValue> parsedCustomSQLValues = getParsedCustomSQLValues(it)
        bindingMap.putAt("customSQLTemplateSelectFrom", it.customSQLTemplateSelectFrom)
        bindingMap.putAt("customSQLTemplateWhere", it.customSQLTemplateWhere)
        bindingMap.putAt("columnNamesList", it.columnNamesList)
        bindingMap.putAt("customSQLValues", parsedCustomSQLValues)
        bindingMap.putAt("hasBlanks", it?.hasBlanks)
        return new CustomSQLTemplate(bindingMap)
    }

    private createNonCaseTemplate(JSONElement it, Map bindingMap) {
        Set<CustomSQLValue> parsedCustomSQLValues = getParsedCustomSQLValues(it)
        bindingMap.putAt("nonCaseSql", it?.nonCaseSql)
        bindingMap.putAt("columnNamesList", it?.columnNamesList)
        bindingMap.putAt("customSQLValues", parsedCustomSQLValues)
        bindingMap.putAt("hasBlanks", it?.hasBlanks)
        bindingMap.putAt("usePvrDB", it?.usePvrDB)
        bindingMap.putAt("chartCustomOptions", it?.chartCustomOptions)
        bindingMap.putAt("maxChartPoints", it?.maxChartPoints)
        bindingMap.putAt("chartExportAsImage", it?.chartExportAsImage?: false)
        bindingMap.putAt("showChartSheet", it?.showChartSheet ?: false)
        bindingMap.putAt("specialChartSettings", it?.specialChartSettings)
        return new NonCaseSQLTemplate(bindingMap)
    }

    private createXMLTemplate(JSONElement jsonElement, Map bindingMap) {
        ReportTemplate reportTemplate = new XMLTemplate(bindingMap)
        if (jsonElement.rootNode) {
            Map<Long, Long> replacementMap = [:]
            if (jsonElement.cllTemplates) {
                jsonElement.nestedTemplates = jsonElement.cllTemplates
            }
            reportTemplate.nestedTemplates = getNestedTemplates(jsonElement, replacementMap)
            XMLTemplateNode rootNode = createXMLTemplateNode(jsonElement.rootNode, replacementMap)
            rootNode.save(failOnError: true)
            reportTemplate.rootNode = rootNode
        }
        return reportTemplate
    }

    private Map getBindingMapForLoading(def it, User user) {
        Map bindingMap = [
                category          : it.category,
                name              : it?.name,
                description       : it?.description,
                qualityChecked    : it.qualityChecked ?: false,
                createdBy         : user.username,
                modifiedBy        : user.username,
                owner             : user,
                templateType      : TemplateTypeEnum.valueOf(it.templateType.name),
                reassessListedness: it?.reassessListedness ? ReassessListednessEnum.valueOf(it?.reassessListedness?.name) : null,
                templtReassessDate: it?.reassessListedness && it.reassessListedness.name == ReassessListednessEnum.CUSTOM_START_DATE.name() && it?.templtReassessDate ? tryParse(it.templtReassessDate) : null,
                reassessForProduct: it?.reassessForProduct ?: false,
                templateFooter    : it?.templateFooter,
                useFixedTemplate  : it.useFixedTemplate ?: false,
                interactiveOutput : it.interactiveOutput ?: false
        ]
        return bindingMap
    }

    public static List readFromExcel(MultipartFile file) {
        List list = []
        Workbook workbook = null

        if (file.originalFilename.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }

        Sheet sheet = workbook.getSheetAt(0);  //get the first worksheet from excel
        Row row;
        Cell cell;

        // starts reading the values from 2nd row
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            if ((row = sheet.getRow(i)) != null) {
                cell = (Cell) row?.getCell(0);  //get the first column from excel
                cell?.setCellType(CellType.STRING);
                if (cell?.getStringCellValue()?.trim()?.length()) {
                    list << cell?.getStringCellValue()?.trim()
                }
            }
        }
        return list.sort() as List
    }

// Import Report Request
    @NotTransactional
    List importConfigurations(JSONElement listOfConfigurations) {
        if (!listOfConfigurations) {
            return []
        }
        User currentUser = userService.getUser()
        List configurations = []
        for (JSONElement it in listOfConfigurations) {
            try {
                ReportConfiguration reportConfiguration = getReportConfigurationFromJson(it, currentUser)
                if (!((!reportConfiguration.emailConfiguration || reportConfiguration.emailConfiguration.save(flush: true)) &&
                        (!reportConfiguration.dmsConfiguration || reportConfiguration.dmsConfiguration.save(flush: true)) &&
                        CRUDService.save(reportConfiguration))) {
                    log.error("Couldn't Upload JSON for ${reportConfiguration.reportName}")
                    log.error(reportConfiguration.emailConfiguration?.errors?.allErrors)
                    log.error(reportConfiguration.dmsConfiguration?.errors?.allErrors)
                    log.error(reportConfiguration.errors.allErrors)
                    EmailConfiguration emailConfiguration = reportConfiguration.emailConfiguration
                    reportConfiguration.emailConfiguration = null
                    emailConfiguration?.id && emailConfiguration.delete()
                }
                configurations.add(reportConfiguration)
            } catch (Exception ex) {
                log.error("Exception while importing report configuration ${it.reportName}", ex)
                configurations.add([reportName: it.reportName])
            }
        }
        return configurations
    }


    ReportConfiguration getReportConfigurationFromJson(JSONElement jsonElement, User user) {
        if (!jsonElement.classNameData) {
            return null
        }
        ReportConfiguration reportConfiguration = Class.forName(jsonElement.classNameData).newInstance(reportName: jsonElement.reportName, createdBy: user.username, modifiedBy: user.username, owner: user, description: jsonElement.description, asOfVersionDateDelta: jsonElement.asOfVersionDateDelta, generateCaseSeries: jsonElement.generateCaseSeries, scheduleDateJSON: jsonElement.scheduleDateJSON, isEnabled: jsonElement.isEnabled, productGroupSelection: jsonElement.productGroupSelection, productSelection: jsonElement.productSelection, studySelection: jsonElement.studySelection, configSelectedTimeZone: jsonElement.configSelectedTimeZone, excludeFollowUp: jsonElement.excludeFollowUp, includeLockedVersion: jsonElement.includeLockedVersion, adjustPerScheduleFrequency: jsonElement.adjustPerScheduleFrequency, excludeNonValidCases: jsonElement.excludeNonValidCases, includeAllStudyDrugsCases: jsonElement.includeAllStudyDrugsCases, suspectProduct: jsonElement.suspectProduct, limitPrimaryPath: jsonElement.limitPrimaryPath, includeMedicallyConfirmedCases: jsonElement.includeMedicallyConfirmedCases, qualityChecked: jsonElement.qualityChecked, blankValuesJSON: jsonElement.blankValuesJSON)

        reportConfiguration.generateDraft = jsonElement.isNull('generateDraft') ? false : jsonElement.generateDraft
        reportConfiguration.includeNonSignificantFollowUp = jsonElement.isNull('includeNonSignificantFollowUp') ? false : jsonElement.includeNonSignificantFollowUp

        reportConfiguration.nextRunDate = jsonElement.nextRunDate ? tryParse(jsonElement.nextRunDate) : null
        reportConfiguration.lastRunDate = jsonElement.lastRunDate ? tryParse(jsonElement.lastRunDate) : null
        reportConfiguration.dateRangeType = jsonElement.dateRangeType ? DateRangeType.findByNameAndIsDeleted(jsonElement.dateRangeType.name, false) : null
        reportConfiguration.asOfVersionDate = jsonElement.asOfVersionDate ? tryParse(jsonElement.asOfVersionDate) : null
        if (!jsonElement.evaluateDateAs){
            log.info(" \" Evaluate Case Data as of\" for ${jsonElement.reportName} is : ${jsonElement.evaluateDateAs}")
        }
        reportConfiguration.evaluateDateAs = jsonElement.evaluateDateAs ? EvaluateCaseDateEnum.values().find{it.name() == jsonElement.evaluateDateAs?.name} : EvaluateCaseDateEnum.LATEST_VERSION
        reportConfiguration.isTemplate = jsonElement.isTemplate
        reportConfiguration.qbeForm = jsonElement.isNull('qbeForm') ? false : jsonElement.qbeForm
        reportConfiguration.pvqType = jsonElement.pvqType
        reportConfiguration.tenantId = jsonElement.tenantId ?: (Tenants.currentId() as Long)
        reportConfiguration.isMultiIngredient = jsonElement.isMultiIngredient ?: false
        reportConfiguration.includeWHODrugs = jsonElement.includeWHODrugs ?: false
        if (reportConfiguration instanceof PeriodicReportConfiguration) {
            reportConfiguration.periodicReportType = PeriodicReportTypeEnum.getAsList().find {
                it.name() == jsonElement.periodicReportType.name
            }
            reportConfiguration.includePreviousMissingCases = jsonElement.includePreviousMissingCases
            reportConfiguration.includeOpenCasesInDraft = jsonElement.includeOpenCasesInDraft
            reportConfiguration.includeLockedVersion = jsonElement.includeLockedVersion
            reportConfiguration.primaryReportingDestination = jsonElement.isNull('primaryReportingDestination') ? null : jsonElement.primaryReportingDestination
            reportConfiguration.reportingDestinations = jsonElement.reportingDestinations ?: []
            reportConfiguration.dueInDays = jsonElement.isNull('dueInDays') ? null : jsonElement.dueInDays
        } else if(reportConfiguration instanceof Configuration) {
            reportConfiguration.eventSelection = jsonElement.isNull('eventSelection') ? null : jsonElement.eventSelection
            reportConfiguration.eventGroupSelection = jsonElement.isNull('eventGroupSelection') ? null : jsonElement.eventGroupSelection
        } else if (reportConfiguration instanceof IcsrReportConfiguration) {
            reportConfiguration.periodicReportType = PeriodicReportTypeEnum.getAsList().find {
                it.name() == jsonElement.periodicReportType?.name
            }
            reportConfiguration.includePreviousMissingCases = jsonElement.includePreviousMissingCases
            reportConfiguration.includeOpenCasesInDraft = jsonElement.includeOpenCasesInDraft
            reportConfiguration.includeLockedVersion = jsonElement.includeLockedVersion
            reportConfiguration.primaryReportingDestination = jsonElement.isNull('primaryReportingDestination') ? null : jsonElement.primaryReportingDestination
            reportConfiguration.recipientOrganization = jsonElement.isNull('recipientOrganization') ? null : UnitConfiguration.findByUnitNameAndUnitType(jsonElement.recipientOrganization.unitName, UnitTypeEnum.valueOf(jsonElement.recipientOrganization.unitType.name))
            reportConfiguration.senderOrganization = jsonElement.isNull('senderOrganization') ? null : UnitConfiguration.findByUnitNameAndUnitType(jsonElement.senderOrganization.unitName, UnitTypeEnum.valueOf(jsonElement.senderOrganization.unitType.name))
            reportConfiguration.referenceProfile = jsonElement.referenceProfile ? IcsrProfileConfiguration.findByReportNameAndIsDeleted(jsonElement.referenceProfile.reportName, false) : null
        } else if (reportConfiguration instanceof IcsrProfileConfiguration) {
            reportConfiguration.recipientOrganization = jsonElement.isNull('recipientOrganization') ? null : UnitConfiguration.findByUnitNameAndUnitType(jsonElement.recipientOrganization.unitName, UnitTypeEnum.valueOf(jsonElement.recipientOrganization.unitType.name))
            reportConfiguration.senderOrganization = jsonElement.isNull('senderOrganization') ? null : UnitConfiguration.findByUnitNameAndUnitType(jsonElement.senderOrganization.unitName, UnitTypeEnum.valueOf(jsonElement.senderOrganization.unitType.name))
            reportConfiguration.comparatorReporting = jsonElement.comparatorReporting
            reportConfiguration.autoTransmit = jsonElement.autoTransmit
            reportConfiguration.autoScheduling = jsonElement.autoScheduling
            reportConfiguration.autoGenerate = jsonElement.autoGenerate
            reportConfiguration.autoSubmit = jsonElement.autoSubmit
            reportConfiguration.submissionDateFrom = jsonElement.submissionDateFrom ? IcsrProfileSubmissionDateOptionEnum.valueOf(jsonElement.submissionDateFrom.name) : null
            reportConfiguration.localCpRequired = jsonElement.localCpRequired
            reportConfiguration.manualScheduling = jsonElement.manualScheduling
            reportConfiguration.deviceReportable = jsonElement.deviceReportable
            reportConfiguration.multipleReport = jsonElement.multipleReport
            reportConfiguration.includeOpenCases = jsonElement.includeOpenCases
            reportConfiguration.includeNonReportable = jsonElement.includeNonReportable
            reportConfiguration.includeProductObligation = jsonElement.includeProductObligation
            reportConfiguration.includeStudyObligation = jsonElement.includeStudyObligation
            reportConfiguration.adjustDueDate = jsonElement.adjustDueDate
            reportConfiguration.calendars = jsonElement.holidayCalendars ? createHolidayCalanders(jsonElement.holidayCalendars) : null
            reportConfiguration.dueDateOptionsEnum = jsonElement.dueDateOptionsEnum ? IcsrProfileDueDateOptionsEnum.valueOf(jsonElement.dueDateOptionsEnum.name) : null
            reportConfiguration.dueDateAdjustmentEnum = jsonElement.dueDateAdjustmentEnum ? IcsrProfileDueDateAdjustmentEnum.valueOf(jsonElement.dueDateAdjustmentEnum.name) : null
            reportConfiguration.autoScheduleFUPReport = jsonElement.autoScheduleFUPReport
            reportConfiguration.awareDate = jsonElement.awareDate
            reportConfiguration.authorizationTypes = jsonElement.authorizationTypeId
            reportConfiguration.needPaperReport = jsonElement.needPaperReport
            reportConfiguration.assignedValidation = jsonElement.assignedValidation
            reportConfiguration.e2bDistributionChannel = jsonElement.e2bDistributionChannel ? createDistributionChannel(jsonElement.e2bDistributionChannel, user) : null
            reportConfiguration.fieldProfile = jsonElement.fieldProfile ? FieldProfile.findByNameAndIsDeleted(jsonElement.fieldProfile.name, false) : null
            reportConfiguration.isJapanProfile = jsonElement.isJapanProfile
            reportConfiguration.isProductLevel = jsonElement.isProductLevel
            reportConfiguration.isEnabled = false
        }

        if (jsonElement.globalDateRangeInformation) {
            reportConfiguration.globalDateRangeInformation = new GlobalDateRangeInformation()
            reportConfiguration.globalDateRangeInformation.reportConfiguration = reportConfiguration
            reportConfiguration.globalDateRangeInformation.relativeDateRangeValue = jsonElement.globalDateRangeInformation.relativeDateRangeValue
            reportConfiguration.globalDateRangeInformation.dateRangeEnum = jsonElement.globalDateRangeInformation?.dateRangeEnum?.name
            if (reportConfiguration.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM) {
                reportConfiguration.globalDateRangeInformation.dateRangeStartAbsolute = DateUtil.parseDate(jsonElement.globalDateRangeInformation.dateRangeStartAbsolute, DateUtil.ISO_DATE_TIME_FORMAT)
                reportConfiguration.globalDateRangeInformation.dateRangeEndAbsolute = DateUtil.parseDate(jsonElement.globalDateRangeInformation.dateRangeEndAbsolute, DateUtil.ISO_DATE_TIME_FORMAT)
            }
        } else {
            reportConfiguration.globalDateRangeInformation = new GlobalDateRangeInformation()
            reportConfiguration.globalDateRangeInformation.reportConfiguration = reportConfiguration
        }

        if(reportConfiguration.validProductGroupSelection){
            List productGroupSelection = MiscUtil.parseJsonText(reportConfiguration.productGroupSelection)
            productGroupSelection.each {
                if(it.name && it.id){
                    String name = it.name
                    int lastIndex = name.lastIndexOf(' (')
                    String grpName = (lastIndex != -1) ? name.substring(0, lastIndex) : name
                    DictionaryGroup dgObj = DictionaryGroup.findByGroupName(grpName)
                    if(!dgObj){
                        throw new Exception("DictionaryGroup doesn't exist for groupName: ${grpName}")
                    }

                    it.id = dgObj.id
                    it.name = dgObj.groupName + " (" + dgObj.id + ")"
                    it.isMultiIngredient = dgObj.isMultiIngredient
                    it.includeWHODrugs = dgObj.includeWHODrugs
                }
            }
            reportConfiguration.productGroupSelection = JsonOutput.toJson(productGroupSelection)
        }

        jsonElement.templateQueries?.each { tempQuery ->

            Map templateQueryCommonParam = [index: tempQuery.index, draftOnly: tempQuery.draftOnly, createdBy: user.username, modifiedBy: user.username, header: tempQuery.header, title: tempQuery.title, footer: tempQuery.footer, headerProductSelection: tempQuery.headerProductSelection, headerDateRange: tempQuery.headerDateRange, displayMedDraVersionNumber: tempQuery.displayMedDraVersionNumber ?: false, blindProtected: tempQuery.blindProtected, privacyProtected: tempQuery.privacyProtected,
                                            issueType: tempQuery.issueType, rootCause: tempQuery.rootCause, responsibleParty: tempQuery.responsibleParty, assignedToUser: tempQuery.assignedToUser, assignedToGroup: tempQuery.assignedToGroup, priority: tempQuery.priority,
                                            investigation: tempQuery.investigation, summary: tempQuery.summary, actions: tempQuery.actions, investigationSql: tempQuery.investigationSql, actionsSql: tempQuery.actionsSql, summarySql: tempQuery.summarySql,
                                            queryLevel: QueryLevelEnum.values().find {
                it.name() == tempQuery.queryLevel?.name
            },granularity:GranularityEnum.values().find{
                it.name() == tempQuery.granularity?.name
            }, templtReassessDate: tempQuery?.templtReassessDate ? tryParse(tempQuery.templtReassessDate) : null,
            reassessListednessDate: tempQuery?.reassessListednessDate ? tryParse(tempQuery.reassessListednessDate) : null]

            TemplateQuery templateQuery
            if (reportConfiguration instanceof IcsrReportConfiguration || reportConfiguration instanceof IcsrProfileConfiguration) {
                templateQuery = new IcsrTemplateQuery(templateQueryCommonParam)
                templateQuery.with {
                    authorizationType = AuthorizationTypeEnum.values().find {
                        it.name() == tempQuery.authorizationType?.name
                    }
                    productType = tempQuery.productType
                    dueInDays = tempQuery.dueInDays
                    icsrMsgType = tempQuery.icsrMsgType
                    distributionChannelName = DistributionChannelEnum.values().find {
                        it.name() == tempQuery.distributionChannelName?.name
                    }
                    orderNo = tempQuery.orderNo
                    isExpedited = tempQuery.isExpedited
                }
                templateQuery.emailConfiguration = null
                if (tempQuery.emailConfiguration) {
                    templateQuery.emailConfiguration = new EmailConfiguration(to: tempQuery.emailConfiguration.to, subject: tempQuery.emailConfiguration.subject, body: tempQuery.emailConfiguration.body, cc: tempQuery.emailConfiguration.cc, noEmailOnNoData: tempQuery.emailConfiguration.noEmailOnNoData, isDeleted: tempQuery.emailConfiguration.isDeleted, showPageNumbering: tempQuery.emailConfiguration.showPageNumbering, excludeCriteriaSheet: tempQuery.emailConfiguration.excludeCriteriaSheet, excludeAppendix: tempQuery.emailConfiguration.excludeAppendix, excludeComments: tempQuery.emailConfiguration.excludeComments, excludeLegend: tempQuery.emailConfiguration.excludeLegend, showCompanyLogo: tempQuery.emailConfiguration.showCompanyLogo)
                    templateQuery.emailConfiguration.pageOrientation = PageOrientation.values().find {
                        it.name() == tempQuery.emailConfiguration.pageOrientation?.name
                    }
                    templateQuery.emailConfiguration.paperSize = PageSizeEnum.values().find {
                        it.name() == tempQuery.emailConfiguration.paperSize?.name
                    }
                    templateQuery.emailConfiguration.sensitivityLabel = SensitivityLabelEnum.values().find {
                        it.name() == tempQuery.emailConfiguration.sensitivityLabel?.name
                    }
                }
            } else {
                templateQuery = new TemplateQuery(templateQueryCommonParam)
            }
            templateQuery.dateRangeInformationForTemplateQuery = new DateRangeInformation(
                    relativeDateRangeValue: tempQuery.dateRangeInformationForTemplateQuery.relativeDateRangeValue,
                    dateRangeEnum: DateRangeEnum.values().find {
                        it.name() == tempQuery.dateRangeInformationForTemplateQuery.dateRangeEnum?.name
                    })

            if (templateQuery.dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUSTOM) {
                templateQuery.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.parseDate(tempQuery.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute, DateUtil.ISO_DATE_TIME_FORMAT)
                templateQuery.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.parseDate(tempQuery.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute, DateUtil.ISO_DATE_TIME_FORMAT)
            }
            templateQuery.dateRangeInformationForTemplateQuery.templateQuery = templateQuery
            templateQuery.template = ReportTemplate.fetchOriginalByName(tempQuery.template.name)
            templateQuery.query = tempQuery.query ? SuperQuery.fetchOriginalByName(tempQuery.query.name) : null
            tempQuery.queryValueLists.each { qvl ->
                QueryValueList queryValueList = new QueryValueList(query: SuperQuery.fetchOriginalByName(qvl.query.name))
                qvl.parameterValues.each {
                    if (it.reportField?.name) {
                        queryValueList.addToParameterValues(new QueryExpressionValue(key: it."key",
                                reportField: ReportField.findByNameAndIsDeleted(it.reportField?.name, false), operator: QueryOperatorEnum.valueOf(it.operator.name), value: it."value", specialKeyValue: it.specialKeyValue))
                    } else {
                        queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                                value: it.value))
                    }
                }
                templateQuery.addToQueryValueLists(queryValueList)
            }

            tempQuery.templateValueLists.each { tvl ->
                TemplateValueList templateValueList = new TemplateValueList(template: ReportTemplate.fetchOriginalByName(tvl.template.name))
                tvl.parameterValues.each {
                    templateValueList.addToParameterValues(new CustomSQLTemplateValue(field: it.field, key: it.key,
                            value: it.value))
                }
                templateQuery.addToTemplateValueLists(templateValueList)
            }
            reportConfiguration.addToTemplateQueries(templateQuery)
        }
        reportConfiguration.globalQuery = jsonElement.globalQuery ? SuperQuery.fetchOriginalByName(jsonElement.globalQuery.name) : null
        jsonElement.globalQueryValueLists?.each { qvl ->
            QueryValueList queryValueList = new QueryValueList(query: SuperQuery.fetchOriginalByName(qvl.query.name))
            qvl.parameterValues.each {
                if (it.reportField?.name) {
                    queryValueList.addToParameterValues(new QueryExpressionValue(key: it."key",
                            reportField: ReportField.findByNameAndIsDeleted(it.reportField?.name, false), operator: QueryOperatorEnum.valueOf(it.operator.name), value: it."value", specialKeyValue: it.specialKeyValue))
                } else {
                    queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                            value: it.value))
                }
            }
            reportConfiguration.addToGlobalQueryValueLists(queryValueList)
        }
        DeliveryOption deliveryOption = null
        if (jsonElement.deliveryOption) {
            deliveryOption = new DeliveryOption(attachmentFormats: jsonElement.deliveryOption.attachmentFormats.collect {
                ReportFormatEnum.valueOf(it.name)
            })
            jsonElement.deliveryOption.emailToUsers.each {
                if(User.findByEmail(it) || Email.findByEmail(it)){
                    deliveryOption.addToEmailToUsers(it)
                }
            }
            jsonElement.deliveryOption.sharedWithGroup.each {
                UserGroup shareWithGroup = UserGroup.findByNameAndIsDeleted(it.name, false)
                if (shareWithGroup) {
                    deliveryOption.addToSharedWithGroup(shareWithGroup)
                }
            }
            jsonElement.deliveryOption.sharedWith.each {
                User shareWith = User.findByUsername(it.username)
                if (shareWith) {
                    deliveryOption.addToSharedWith(shareWith)
                }
            }
            if (!deliveryOption.sharedWith.contains(user)) {
                deliveryOption.addToSharedWith(user)
            }
            jsonElement.deliveryOption.executableByGroup.each {
                UserGroup group = UserGroup.findByNameAndIsDeleted(it.name, false)
                if (group) {
                    deliveryOption.addToExecutableByGroup(group)
                }
            }
            jsonElement.deliveryOption.executableBy.each {
                User u = User.findByUsername(it.username)
                if (u) {
                    deliveryOption.addToExecutableBy(u)
                }
            }
            if (!deliveryOption.executableBy.contains(user)) {
                deliveryOption.addToExecutableBy(user)
            }
        }
        reportConfiguration.emailConfiguration = null
        if (jsonElement.emailConfiguration) {
            reportConfiguration.emailConfiguration = new EmailConfiguration(to: jsonElement.emailConfiguration.to,subject: jsonElement.emailConfiguration.subject, body: jsonElement.emailConfiguration.body, cc: jsonElement.emailConfiguration.cc, noEmailOnNoData: jsonElement.emailConfiguration.noEmailOnNoData, isDeleted: jsonElement.emailConfiguration.isDeleted, showPageNumbering: jsonElement.emailConfiguration.showPageNumbering, excludeCriteriaSheet: jsonElement.emailConfiguration.excludeCriteriaSheet, excludeAppendix: jsonElement.emailConfiguration.excludeAppendix, excludeComments: jsonElement.emailConfiguration.excludeComments, excludeLegend: jsonElement.emailConfiguration.excludeLegend, showCompanyLogo: jsonElement.emailConfiguration.showCompanyLogo)
            reportConfiguration.emailConfiguration.pageOrientation = PageOrientation.values().find {
                it.name() == jsonElement.emailConfiguration.pageOrientation?.name
            }
            reportConfiguration.emailConfiguration.paperSize = PageSizeEnum.values().find {
                it.name() == jsonElement.emailConfiguration.paperSize?.name
            }
            reportConfiguration.emailConfiguration.sensitivityLabel = SensitivityLabelEnum.values().find {
                it.name() == jsonElement.emailConfiguration.sensitivityLabel?.name
            }
        }
        def dmsConfiguration=jsonElement.dmsConfiguration
        if (dmsConfiguration) {
            reportConfiguration.dmsConfiguration = new DmsConfiguration(
                    folder: dmsConfiguration.folder,
                    name: dmsConfiguration.name,
                    description: dmsConfiguration.description,
                    tag: dmsConfiguration.tag,
                    isDeleted: dmsConfiguration.isDeleted,
                    noDocumentOnNoData: dmsConfiguration.noDocumentOnNoData,
                    showPageNumbering: dmsConfiguration.showPageNumbering,
                    excludeCriteriaSheet: dmsConfiguration.excludeCriteriaSheet,
                    excludeAppendix: dmsConfiguration.excludeAppendix,
                    excludeComments: dmsConfiguration.excludeComments,
                    excludeLegend: dmsConfiguration.excludeLegend,
                    showCompanyLogo: dmsConfiguration.showCompanyLogo)
            reportConfiguration.dmsConfiguration.format = ReportFormatEnum.values().find {
                it.name() == dmsConfiguration.format?.name
            }
            reportConfiguration.dmsConfiguration.pageOrientation = PageOrientation.values().find {
                it.name() == dmsConfiguration.pageOrientation?.name
            }
            reportConfiguration.dmsConfiguration.paperSize = PageSizeEnum.values().find {
                it.name() == dmsConfiguration.paperSize?.name
            }
            reportConfiguration.dmsConfiguration.sensitivityLabel = SensitivityLabelEnum.values().find {
                it.name() == dmsConfiguration.sensitivityLabel?.name
            }
        }
        deliveryOption.report = reportConfiguration

        //PVR-19187 Default handling in case delivery share to options only have groups specified that
        //do not exist in the system in which the JSON is being imported.
        //See BaseDeliveryOption validators.
        if(!(deliveryOption.emailToUsers || deliveryOption.sharedWith || deliveryOption.sharedWithGroup)){
            deliveryOption.addToSharedWith(user)
        }


        reportConfiguration.deliveryOption = deliveryOption
        jsonElement.poiInputsParameterValues.each {
            reportConfiguration.addToPoiInputsParameterValues(new ParameterValue(key: it.key, value: it.value))
        }

        jsonElement.tags.each { current ->
            Tag tag = Tag.findByName(current.name)
            if(!tag){
                tag = new Tag()
                tag.name = current.name
            }
            reportConfiguration.addToTags(tag)
        }

        if(jsonElement.sourceProfile){
            reportConfiguration.sourceProfile = SourceProfile.findBySourceId(jsonElement.sourceProfile.sourceId as Long)
        }

        if(reportConfiguration instanceof Configuration) {
            reportConfiguration.useCaseSeries = jsonElement.useCaseSeries ? ExecutedCaseSeries.findBySeriesNameAndIsDeleted(jsonElement.useCaseSeries.seriesName,false) : null
        }
        return reportConfiguration
    }

    Date tryParse(String dateString) {
        List<String> formatStrings = grailsApplication.config.grails.databinding.dateFormats
        for (String formatString : formatStrings) {
            try {
                return new SimpleDateFormat(formatString).parse(dateString)
            }
            catch (ParseException e) {
            }
        }
        return null
    }

    void importUnitConfigsJson(String text) {
        def jsons = JSON.parse(text)
        jsons?.each { jsonElement ->
            UnitConfiguration unitConfiguration = new UnitConfiguration(unitRegisteredId: jsonElement.unitRegisteredId, unitAttachmentRegId: jsonElement.unitAttachmentRegId, unitName: jsonElement.unitName, organizationCountry: jsonElement.organizationCountry, address1: jsonElement.address1, address2: jsonElement.address2, city: jsonElement.city, state: jsonElement.state, postalCode: jsonElement.postalCode, phone: jsonElement.phone, email: jsonElement.email, unitRetired: jsonElement.unitRetired, firstName: jsonElement.firstName, middleName: jsonElement.middleName, lastName: jsonElement.lastName, department: jsonElement.department, fax: jsonElement.fax, xsltName: jsonElement.xsltName, holderId: jsonElement.holderId, organizationName: jsonElement.organizationName, postalCodeExt : jsonElement.postalCodeExt, xmlVersion : jsonElement.xmlVersion, xmlEncoding : jsonElement.xmlEncoding, xmlDoctype : jsonElement.xmlDoctype, preferredTimeZone : jsonElement.preferredTimeZone, preferredLanguage : jsonElement.preferredLanguage ?: "eng", unitOrganizationName : jsonElement.unitOrganizationName)
            unitConfiguration.unitType = UnitTypeEnum.values().find { it.name() == jsonElement.unitType?.name }
            unitConfiguration.organizationType = IcsrOrganizationType.findByOrg_name_id(jsonElement.organizationType?.org_name_id)
            unitConfiguration.title = TitleEnum.values().find { it.name() == jsonElement.title?.name }
            unitConfiguration.registeredWith = UnitConfiguration.findByUnitRegisteredId(jsonElement.registeredWith?.unitRegisteredId)
            unitConfiguration.allowedAttachments = jsonElement.allowedAttachments.collect{it as Long}
            User user = userService.currentUser
            unitConfiguration.createdBy = user.username
            unitConfiguration.modifiedBy = user.username
            if (!unitConfiguration.validate()) {
                throw new ValidationException("UnitConfiguration has validation issues", unitConfiguration.errors)
            }
            CRUDService.save(unitConfiguration, [failOnError: true])
        }
    }

    void importFieldProfilesJson(String text) {
        def jsons = JSON.parse(text)
        jsons?.each { jsonElement ->
            if (FieldProfile.countByNameAndIsDeleted(jsonElement.name, false)) {
                log.debug("Field Profile : ${jsonElement.name} already exists")
                return
            }
            FieldProfile fieldProfile = new FieldProfile(name: jsonElement.name, description: jsonElement.description)
            CRUDService.save(fieldProfile, [failOnError: true])
            jsonElement.blindedFields?.each {
                userService.addToFieldsWithFlag(fieldProfile, ReportField.findByNameAndIsDeleted(it, false), true, false, false)
            }
            jsonElement.protectedFields?.each {
                userService.addToFieldsWithFlag(fieldProfile, ReportField.findByNameAndIsDeleted(it, false), false, true, false)
            }
            jsonElement.hiddenFields?.each {
                userService.addToFieldsWithFlag(fieldProfile, ReportField.findByNameAndIsDeleted(it, false), false, false, true)
            }
        }
    }

    private XMLTemplateNode createXMLTemplateNode(JSONObject xmlTag, Map<Long, Long> replacementMap) {
        XMLTemplateNode node = new XMLTemplateNode(getXMLTemplateNodeBindingMap(xmlTag, replacementMap))
        Collection<XMLTemplateNode> children = createXMLTemplateNodes(xmlTag.children, replacementMap)
        int orderingNumber = 0
        children?.each { child ->
            child.setOrderingNumber(orderingNumber++)
            node.addToChildren(child)
        }
        return node
    }

    private Collection<XMLTemplateNode> createXMLTemplateNodes(JSONArray xmlTags, Map<Long, Long> replacementMap) {
        xmlTags.collect {
            createXMLTemplateNode(it, replacementMap)
        }
    }

    private Set<CaseLineListingTemplate> getNestedTemplates(JSONElement jsonElement, Map<Long, Long> replacementMap) {
        Set<CaseLineListingTemplate> templates = new HashSet<>()
        for (JSONElement nestedTemplate : jsonElement.nestedTemplates) {
            ReportTemplate template = ReportTemplate.fetchOriginalByName(nestedTemplate.name)
            if(Holders.config.json.xml.template.importValidate &&nestedTemplate.name && !template){
                throw new RuntimeException("Report template not found for ${nestedTemplate.name}")
            }
            replacementMap[nestedTemplate.id] = template?.id
            templates.add(template)
        }
        return templates
    }

    private getXMLTemplateNodeBindingMap(def node, Map<Long, Long> replacementMap) {
        ReportTemplate template = node.data?.templateId ? ReportTemplate.load(replacementMap[node.data?.templateId]) : null
        List allSelectedFieldsInfo = null
        if (template?.templateType == TemplateTypeEnum.CASE_LINE) {
            allSelectedFieldsInfo = CaseLineListingTemplate.load(replacementMap[node.data?.templateId])?.allSelectedFieldsInfoForXML
        }
        E2BLocaleName e2BLocaleName = null
        if (node.data?.e2bLocale && node.data?.e2bLocaleElementName) {
            e2BLocaleName = new E2BLocaleName(e2bLocale: node.data?.e2bLocale, e2bLocaleElementName: node.data?.e2bLocaleElementName)
        }
        def bindingMap = [
                tagName                 : node.title,
                elementType             : node.data?.elementType,
                type                    : node.data?.type,
                orderingNumber          : node.data?.orderingNumber,
                template                : template,
                filterFieldInfo         : allSelectedFieldsInfo?.find {
                    it.argusName == node.data?.filterFieldInfo?.argusName && it.reportField?.name == node.data?.filterFieldInfo?.reportField?.name && it.renameValue == node.data?.filterFieldInfo?.renameValue
                }, //TODO need to check how to identify reportInfo uniquely while importing.
                reportFieldInfo         : allSelectedFieldsInfo?.find {
                    it.argusName == node.data?.reportFieldInfo?.argusName && it.reportField?.name == node.data?.reportFieldInfo?.reportField?.name && it.renameValue == node.data?.reportFieldInfo?.renameValue
                }, //TODO need to check how to identify reportInfo uniquely while importing.
                customSQLFieldInfo      : node.data.customSQLFieldInfo?.id,
                customSQLFilterFiledInfo: node.data.customSQLFilterFiledInfo?.id,
                value                   : node.data?.value,
                dateFormat              : node.data?.dateFormat,
                tagColor                : node.data?.tagColor,
                e2bElement              : node.data?.e2bElement,
                e2bElementName          : node.data?.e2bElementName,
                e2bElementNameLocale    : e2BLocaleName,
                sourceFieldLabel        : node.data?.sourceFieldLabel,
                sourceFieldLabelVal     : node.data?.sourceFieldLabelVal
        ]
        if(Holders.config.getProperty('json.xml.template.importValidate', Boolean) && node.data?.reportFieldInfo && !bindingMap.reportFieldInfo){
            throw new RuntimeException("For ${node.title} reportField is not found ${node.data?.reportFieldInfo?.reportField?.name}")
        }
        if(Holders.config.getProperty('json.xml.template.importValidate', Boolean) && node.data?.filterFieldInfo && !bindingMap.filterFieldInfo){
            throw new RuntimeException("For ${node.title} reportField is not found ${node.data?.filterFieldInfo?.reportField?.name}")
        }
        if(Holders.config.getProperty('json.xml.template.importValidate', Boolean) && node.data?.templateId && !bindingMap.template){
            throw new RuntimeException("For ${node.title} template is not found ${node.data?.templateId}")
        }
        bindingMap
    }

    private List  fetchValidValuesByName(List values, List validValues = [], def view, String name) {
        values.collate(999).each { list ->
            validValues += view.'pva'.createCriteria().list {
                sqlRestriction("upper(${name}) in ('${list*.toUpperCase()?.join("','")}')")
            }.name
        }
        return validValues
    }

    private List  fetchValidValuesByTradeName(List values, List validValues = [], def view, String name) {
        values.collate(999).each { list ->
            validValues += view.'pva'.createCriteria().list {
                sqlRestriction("upper(${name}) in ('${list*.toUpperCase()?.join("','")}')")
            }.tradeName
        }
        return validValues
    }

    private List  fetchValidValuesByDescription(List values, List validValues = [], def view, String name) {
        values.collate(999).each { list ->
            validValues += view.'pva'.createCriteria().list {
                sqlRestriction("upper(${name}) in ('${list*.toUpperCase()?.join("','")}')")
            }.description
        }
        return validValues
    }

    private List  fetchValidValuesByStudyNum(List values, List validValues = [], def view, String name) {
        values.collate(999).each { list ->
            validValues += view.'pva'.createCriteria().list {
                sqlRestriction("upper(${name}) in ('${list*.toUpperCase()?.join("','")}')")
            }.studyNum
        }
        return validValues
    }

    private List  fetchValidValuesByIngredient(List values, List validValues = [], def view, String name) {
        values.collate(999).each { list ->
            validValues += view.'pva'.createCriteria().list {
                sqlRestriction("upper(${name}) in ('${list*.toUpperCase()?.join("','")}')")
            }.ingredient
        }
        return validValues
    }

    private List<Long> createHolidayCalanders(def jsonElement) {
        List<Long> ids = []
        SafetyCalendar.withNewSession {
            jsonElement.each {
                ids.add(SafetyCalendar.findByName(it.name)?.id)
            }
        }
        return ids
    }

    private DistributionChannel createDistributionChannel(def jsonElement, User user) {
        DistributionChannel distributionChannel = new DistributionChannel()
        distributionChannel.with {
            outgoingFolder = jsonElement.outgoingFolder
            reportFormat = E2BReportFormatEnum.values().find { it.name() == jsonElement.reportFormat?.name }
            incomingFolder = jsonElement.incomingFolder
            markSubmittedAfter = jsonElement.markSubmittedAfter
            deliveryReceipt = jsonElement.deliveryReceipt
            maxReportPerMsg = jsonElement.maxReportPerMsg
        }
        return (DistributionChannel) CRUDService.save(distributionChannel)
    }

    //--- dashboard---

    def getDashboardAsJSON(Dashboard dashboard) {
        HashMap dashboardMap = new HashMap(dashboard.properties)
        if (dashboardMap.parentId) dashboardMap.parent = Dashboard.get(dashboardMap.parentId as Long)?.label
        dashboardMap.widgets = dashboard.widgets.collect {
            new HashMap(widgetType: it.widgetType,
                    reportConfiguration: it.reportConfiguration?.reportName,
                    x: it.x, y: it.y, width: it.width, height: it.height,
                    autoPosition: it.autoPosition, settings: it.settings, sectionNumber: it.sectionNumber)
        }
        return dashboardMap as JSON
    }

    Dashboard getDashboardFromJSON(JSONElement jsonElement, User user) {
        Dashboard dashboard = new Dashboard([
                owner        : user,
                label        : jsonElement.label,
                dashboardType: jsonElement.dashboardType.name as DashboardEnum,
                parentId     : jsonElement.parent ? Dashboard.findByLabelAndDashboardTypeInListAndIsDeleted(jsonElement.parent, [DashboardEnum.PVC_PUBLIC, DashboardEnum.PVC_USER], false)?.id : null,
                dateCreated  : new Date(),
                lastUpdated  : new Date(),
                createdBy    : user.username,
                modifiedBy   : user.username,
                icon         : jsonElement.icon])

        jsonElement.widgets?.each {
            ReportConfiguration reportConfiguration
            if (it.reportConfiguration) {
                reportConfiguration = ReportConfiguration.findByReportNameAndIsDeleted(it.reportConfiguration, false)
            }
            if (!it.reportConfiguration || reportConfiguration) {
                ReportWidget reportWidget = new ReportWidget([
                        widgetType         : it.widgetType.name as WidgetTypeEnum,
                        reportConfiguration: reportConfiguration,
                        x                  : it.x, y: it.y, width: it.width, height: it.height,
                        autoPosition       : it.autoPosition, settings: it.settings, sectionNumber: it.sectionNumber])
                dashboard.addToWidgets(reportWidget)
            }
        }
        dashboard
    }

    List<Dashboard> importDashboards(JSONElement listOfDashboards) {
        if (!listOfDashboards) {
            return []
        }
        User currentUser = userService.getUser()
        List<Dashboard> dashboards = []
        for (JSONElement it in listOfDashboards) {
            Dashboard dashboard = getDashboardFromJSON(it, currentUser)
            if (dashboard) {
                try {
                    CRUDService.save(dashboard)
                } catch (ValidationException ve) {
                    log.warn("Failed to import ${dashboard.label} due to validation errors")
                }
                dashboards.add(dashboard)
            }
        }
        return dashboards
    }
}
