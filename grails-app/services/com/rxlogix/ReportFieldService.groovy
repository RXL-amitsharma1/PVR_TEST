package com.rxlogix

import com.hazelcast.core.HazelcastInstance
import com.rxlogix.config.CustomReportField
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.SourceProfile
import com.rxlogix.enums.ReportFieldSelectionTypeEnum
import com.rxlogix.helper.LocaleHelper
import com.rxlogix.mapping.ClDatasheetReassess
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.FieldProfileFields
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.grails.core.exceptions.GrailsRuntimeException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable

import javax.sql.DataSource
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap

class ReportFieldService {
    GrailsApplication grailsApplication
    def customMessageService
    private Map<String, Map<?, ?>> values = [:]
    def userService
    String valuesFileName = "selectable_list_file.dat"
    DataSource dataSource_pva
    DataSource dataSource_faers
    def persistenceInterceptor
    private ConcurrentHashMap<String,?> reportFieldCache =[:]
    def seedDataService

    Map<String, Map<?, ?>> getValues() {
        return values
    }

    void setValues(Map<String, Map<?, ?>> values) {
        this.values = values
    }

    @Cacheable('reportFields')
    public def getAllReportFields() {
        return ReportField.findAllByIsDeleted(false)
    }

    public List<ReportField> getReportFieldsForSourceProfile(Boolean useSourceProfile = false, Integer sourceId = 0){
        List<ReportField> reportFields = []
        if (useSourceProfile) {
            if (sourceId) {
                reportFields = ReportField.findAllByIsDeletedAndSourceId(false, sourceId)
            } else {
                reportFields = ReportField.findAllByIsDeletedAndSourceIdInList(false, SourceProfile.sourceProfilesForUser(userService.currentUser).sourceId)
            }
        } else {
            reportFields = getAllReportFields()
        }
        return reportFields
    }

    private void putReportFieldCache(String key,List<Object> value) {
        reportFieldCache.put(key,value);
    }

    private List<Object> getReportFieldCache(String key) {
        return reportFieldCache.get(key)
    }
    public Map getSelectableValuesForFields(String lang) {
        if (!lang) {
            return [:]
        }
        if (!values) {
            values = [:]
        }
        if (values.get(lang) == null) {
            values.put(lang, readValues(lang))
            if (values.get(lang) == null) {
                values.put(lang, retrieveValuesFromDatabase(lang))
            }
        }
        return values.get(lang) as Map
    }

    public List<Object> getNonCacheSelectableValuesForFields(String field, String lang, String searchTerm, int max, int offset, boolean isFaersTarget) {
        ReportField reportField = ReportField.findByNameAndIsDeleted(field,false)
        if (reportField?.nonCacheSelectable) {
            NonCacheSelectableList selectableListInstance = (NonCacheSelectableList) reportField.listDomainClass.newInstance()
            selectableListInstance.dataSource = isFaersTarget ? dataSource_faers : dataSource_pva
            selectableListInstance.sqlString = reportField.getLmSql(lang)
            return selectableListInstance.getPaginatedSelectableList(searchTerm, max, offset, Tenants.currentId() as Integer)
        }
        return []
    }

    @CacheEvict(value = "selectableValues", allEntries = true)
    public void clearCacheSelectableValues() {
    }

    public def getExtraValuesForFields(String lang) {
        //This code is redundant but is being used in UI so i am calling another method.
        HashMap values = [:]
        ReportField reptField = ReportField.findByNameAndIsDeleted("dvListednessDatasheetId", false)
        if (reptField)
            values.put("dvListednessReassessQuery", grailsApplication.mainContext.reportFieldService.getListValues(reptField, reptField.name, lang))
//      return datasheet list
        return values
    }

    public def getExtraValuesForFields(String lang, Long tenantId) {
        //This code is redundant but is being used in UI so i am calling another method.
        HashMap values = [:]
        ReportField reptField = ReportField.findByNameAndIsDeleted("dvListednessDatasheetId", false)
        if (reptField) {
            if (reptField.listDomainClass == ClDatasheetReassess) {
                values.put("dvListednessReassessQuery", new ClDatasheetReassess().getSelectableList(lang, tenantId))
            } else {
                values.put("dvListednessReassessQuery", grailsApplication.mainContext.reportFieldService.getListValues(reptField, reptField.name, lang))
            }
        }
//      return datasheet list
        return values
    }

    Map retrieveValuesFromDatabase(String lang , String reportFieldName=null) {
        log.info("Retrieving selectableValues from DB.........")
        // look for only Report fields which have selectableList is correct way to do but empty value is also being written in the file so find All
        Map map = [:]
        def reportFieldList = []
        if(!reportFieldName) {
            reportFieldList = ReportField.findAllByIsDeleted(false, [sort: 'name', order: 'asc'])
        }
        else{
            reportFieldList = [ReportField.findByNameAndIsDeleted(reportFieldName , false)]
        }
        if(reportFieldList && reportFieldList.get(0) != null) {
            map = reportFieldList.collectEntries { field ->
                [field.name, grailsApplication.mainContext.reportFieldService.getListValues(field, field.name, lang) ?: ""] // In order to get @Cachable you need to call via service proxy https://jira.grails.org/browse/GPCACHE-18
            }
        }
        else{
            log.error("No Field Variable {} present in our Application",reportFieldName)
        }
        log.info("Completed selectableValues from DB.........")
        return map
    }


    // Autocomplete
    List retrieveValuesFromDatabaseSingle(ReportField field, String search, String lang) {
        List result = []
        if (field?.hasSelectableList()) {
            if (field.isAutocomplete) {
                Sql sql = new Sql(dataSource_pva)
                try {
                    String columnName = ""
                    List<GroovyRowResult> rows = sql.rows(field.getLmSql(lang), ['%' + search.toUpperCase() + '%']) { ResultSetMetaData meta ->
                        columnName = meta.getColumnName(1)
                    }
                    rows.each {
                        result.add(it.getProperty(columnName))
                    }
                } catch (SQLException e) {
                    log.error("Could not complete autocomplete SQL for ReportField: ${field.name}, search: ${search}, ${e.message}")
                } finally {
                    sql?.close()
                }
            } else {
                log.error("Field '$field.name' should be marked as autocomplete in the ARGUS database.")
            }
        }
        return result
    }

    @Cacheable(value = 'selectableValues', key = '#name.toString().concat(#lang.toString())')
    def getListValues(ReportField reportField, String name, String lang) {
        def selectableList = []
        if (reportField.hasSelectableList() && name != '') {
            // may be redundant if we only look for Reportfields which have a selectable list
            long start = System.currentTimeMillis()
            SelectableList selectableListInstance = (SelectableList) reportField.listDomainClass.newInstance()
            String cachekey = selectableListInstance.class.name
            if (selectableListInstance.hasProperty('dataSource')) {
                selectableListInstance.dataSource = dataSource_pva
                selectableListInstance.sqlString = reportField.getLmSql(lang)
                cachekey = "${cachekey}-${reportField.lmSQL}"
            }

            String reportFieldName = customMessageService.getMessage("app.reportField." + reportField.name)
            String locale = reportFieldName.endsWith('(J)') ? Locale.JAPANESE : Locale.ENGLISH
            cachekey = "${cachekey}-${locale}"
            //First check in reportFieldCacheMap if the value already existing -PVR-12280
            selectableList = getReportFieldCache(cachekey)
            //If values not existing with the key (classname, and (lmquery + lang in case of datasource property available) -PVR-12280
            if (!selectableList) {
                selectableList = selectableListInstance.getSelectableList(locale)
                //Store fetch value in reportFieldCache map - PVR-12280
                putReportFieldCache(cachekey, new ArrayList(selectableList))
            }
            long end = System.currentTimeMillis()
            log.debug("Found ${reportField.name} lang ${lang} with ${selectableListInstance.class.name} with entries in ${end - start}ms")
        }
        return selectableList
    }

    synchronized void serializeValues(values, lang) {
        FileOutputStream fos = null
        ObjectOutputStream oos = null
        try {
            fos = new FileOutputStream(new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + lang))
            oos = new ObjectOutputStream(new BufferedOutputStream(fos))
            oos.writeObject(values)
        } catch (all) {
            log.error("Errors when serialize selectable values to file", all)
        } finally {
            oos.close()
            fos.close()
        }
    }

    Map serializeValuesForSingleField(singleFieldVariable , lang, reportField){
            File f = new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + lang)
            Map cacheMap = [:]
            if(f.exists()){
                FileInputStream fis = new FileInputStream(f)
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis))
                cacheMap = ois.readObject()
                cacheMap.put(reportField , singleFieldVariable.get(reportField))
                serializeValues(cacheMap , lang)
            }
        return cacheMap
    }

    Map readValues(String lang) {
        File f = new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + lang)
        if (f.exists()) {
            FileInputStream fis = null
            ObjectInputStream ois = null
            try {
                fis = new FileInputStream(f)
                ois = new ObjectInputStream(new BufferedInputStream(fis))
                def o = ois.readObject()
                return o as Map
            } catch (all) {
                log.error("Error reading the selectable values from file", all)
            } finally {
                ois.close()
                fis.close()
            }
        }
        null
    }

    void loadValuesToCacheFile() {
        List locales = LocaleHelper.buildLocaleSelectList()
        reportFieldCache.clear();
        List result = GParsPool.withPool(2) {
            locales.collectParallel { obj ->
                File f = new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + obj.lang_code)
                if (!f.exists()) {
                    try {
                        persistenceInterceptor.init()
                        def values = retrieveValuesFromDatabase(obj.lang_code)
                        if (values != null && values.keySet().size() > 0) {
                            serializeValues(values, obj.lang_code)
                            clearCacheSelectableValues()
                        }
                        getExtraValuesForFields(obj.lang_code)
                        persistenceInterceptor.flush()
                    } finally {
                        persistenceInterceptor.destroy()
                    }
                }
                return obj.lang_code
            }
        }
        if (!locales*.lang_code.containsAll(result)) {
            throw new GrailsRuntimeException("Error Occurred while loading cache: ${result}")
        }
        reportFieldCache.clear();
    }

    void reLoadValuesToCacheFile() {
        LocaleHelper.buildLocaleSelectList().each {
            File f = new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + it.lang_code)
            if (f.exists()) {
                f.delete()
            }
        }
        loadValuesToCacheFile()
    }

    List<Map<String, List>> getReportFieldsForQuery(Integer sourceId = 0) {
        /*
            At the moment, only the usage from Query is cached.
            Usage from Template will call getReportFields() directly.
         */
        getReportingFields(userService.getUser(),ReportFieldSelectionTypeEnum.QUERY, sourceId)
    }

    @CacheEvict(value = "reportFieldGroups", allEntries = true)
    public void clearCacheReportFields() {
    }

    @CacheEvict(value = ["reportFieldGroups", "reportFields", "selectableValues"], allEntries = true)
    public void clearAllCaches() {
        values = null
        log.debug("All caches have been cleared")
    }

    List<Map<String, List>> getReportFields(ReportFieldSelectionTypeEnum reportFieldSelectionTypeEnum, Map<ReportFieldGroup, List<ReportField>> hiddenFieldGroupedList, useSourceProfile = false, Integer sourceId = 0) {

        List sourceIds = []
        if (useSourceProfile) {
            Set<SourceProfile> sourceProfiles = SourceProfile.sourceProfilesForUser(userService.currentUser)
            sourceIds = (sourceId ? [sourceId] : sourceProfiles*.sourceId)
        }
        List<ReportFieldGroup> fieldGroups = getReportFieldGroups()
        List fieldSelection = []

        def selector
        if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.QUERY) {
            selector = "fetchAllByFieldGroupHavingQuerySelectable"
        } else if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.CLL) {
            selector = "fetchAllByFieldGroupHavingTemplateCLLSelectable"
        } else if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.DT_ROW) {
            selector = "fetchAllByFieldGroupHavingTemplateDTRowSelectable"
        } else if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.DT_COLUMN) {
            selector = "fetchAllByFieldGroupAndTemplateDTColumnSelectable"
        }

//        def showJapaneseReportFields = ApplicationSettings.first().showJapaneseReportFields

        fieldGroups.each { ReportFieldGroup reportFieldGroup ->
            try {
                List<ReportField> children = ReportField."${selector}"(reportFieldGroup)?.list() - hiddenFieldGroupedList?.get(reportFieldGroup)
                if (useSourceProfile) {
                    children = children.findAll { it.sourceId in (sourceIds) }
                }
//            if (!showJapaneseReportFields) {
//                def japaneseReportFields = children.findAll {it.sourceColumn.columnName.endsWith("_J")}
//                children.removeAll(japaneseReportFields)
//            }
                fieldSelection.add(text: [reportFieldGroup?.name], children: children)
            } catch (e) {
                log.error("Error while extracting fields for ${selector}, ${reportFieldSelectionTypeEnum} and ${reportFieldGroup}", e)
            }
        }

        return fieldSelection
    }

    List<ReportFieldGroup> getReportFieldGroups() {
        return ReportFieldGroup.createCriteria().list {
            if (!grailsApplication.config.pv.app.pvcentral.enabled || !SpringSecurityUtils.ifAnyGranted("ROLE_PVC_EDIT, ROLE_PVC_VIEW, ROLE_PVC_INBOUND_EDIT, ROLE_PVC_INBOUND_VIEW")) {
                ne("name", "PVCentralInformation")
            }
            if (!grailsApplication.config.pv.app.pvquality.enabled || !SpringSecurityUtils.ifAnyGranted("ROLE_PVQ_VIEW, ROLE_PVQ_VIEW")) {
                ne("name", "PVQualityInformation")
            }
            eq("isDeleted", false)
            order("priority")
        } as List<ReportFieldGroup>
    }

    List getAllReportFieldsWithGroups(){
        List<ReportFieldGroup> fieldGroups = ReportFieldGroup.findAllByIsDeleted(false, [sort: "priority"])
        List fieldSelection = []
        fieldGroups.each { ReportFieldGroup reportFieldGroup ->
            def children = ReportField.findAllByFieldGroup(reportFieldGroup)
            fieldSelection.add(text: reportFieldGroup?.name, children: children)
        }

        return fieldSelection
    }
    List getAllReportFieldsWithGroupsForTemplates(){
        List<ReportFieldGroup> fieldGroups = ReportFieldGroup.findAllByIsDeleted(false, [sort: "priority"])
        List fieldSelection = []
        fieldGroups.each { ReportFieldGroup reportFieldGroup ->
            def children = ReportField.fetchAllByFieldGroupAndTemplateCLLSelectableOrTemplateDTRowSelectableOrTemplateDTColumnSelectable(reportFieldGroup).list()
            fieldSelection.add(text: reportFieldGroup?.name, children: children)
        }

        return fieldSelection
    }

    List getReportFields(ReportFieldGroup reportFieldGroup) {
        List fieldSelection = []
        if (reportFieldGroup) {
//            def showJapaneseReportFields = ApplicationSettings.first().showJapaneseReportFields
            def selector = "fetchAllByFieldGroupAndTemplateCLLSelectableOrTemplateDTRowSelectableOrTemplateDTColumnSelectable"
            def children = ReportField."${selector}"(reportFieldGroup).list()
//            if (!showJapaneseReportFields) {
//                def japaneseReportFields = children.findAll {it.sourceColumn.columnName.endsWith("_J")}
//                children.removeAll(japaneseReportFields)
//            }
            fieldSelection = children
        }
        fieldSelection
    }

    def reportFieldNameQueryCriteria(String searchString){
        return ReportField.fetchAllReportFieldBySearchString(searchString)
    }

    def fetchReportFields(List<ReportField> reportFieldList) {
        List<Map> reportFields = reportFieldList.collect{
            [id:it.id,name:it.name,fieldGroup:it.fieldGroup.name]
        }
        reportFields
    }

    Integer reportFieldQueryCount(String searchString) {
        return ReportField.fetchAllReportFieldBySearchString(searchString).count()
    }

    List fetchReportField(Map params) {
        List<ReportField> reportFieldList = reportFieldNameQueryCriteria(params.searchString).list([max: params.max, offset: params.offset, sort: params.sort, order:params.order ])
        List<Map> reportFields = fetchReportFields(reportFieldList)
        [reportFields,reportFieldQueryCount(params.searchString),reportFieldQueryCount(null)]
    }

    Long getCachedFileLastModified(Locale locale) {
        File f = new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + locale?.toString())
        if (f.exists()) {
            return f.lastModified()
        }
        return new Date().time
    }

    List<Map<String, List>> getReportingFields(User user, ReportFieldSelectionTypeEnum reportFieldSelectionTypeEnum, Integer sourceId = 0) {
        Set<FieldProfile> fieldProfiles = UserGroup.fetchAllFieldProfileByUser(user)
        Set<ReportField> hiddenFieldList = []
        if (fieldProfiles && !fieldProfiles.any { !it }) {
            hiddenFieldList = FieldProfileFields.findAllByFieldProfileInListAndIsHidden(fieldProfiles as List, true).collect { it.reportField }
            if (hiddenFieldList) {
                switch (reportFieldSelectionTypeEnum) {
                    case ReportFieldSelectionTypeEnum.CLL:
                        hiddenFieldList = hiddenFieldList.findAll { it.templateCLLSelectable }
                        break;
                    case ReportFieldSelectionTypeEnum.DT_COLUMN:
                        hiddenFieldList = hiddenFieldList.findAll { it.templateDTColumnSelectable }
                        break;
                    case ReportFieldSelectionTypeEnum.DT_ROW:
                        hiddenFieldList = hiddenFieldList.findAll { it.templateDTRowSelectable }
                        break;
                    case ReportFieldSelectionTypeEnum.QUERY:
                        hiddenFieldList = hiddenFieldList.findAll { it.querySelectable }
                        break;
                }
            }
        }
        List<Map<String, List>> fieldSelection = getReportFields(reportFieldSelectionTypeEnum, hiddenFieldList.groupBy { it.fieldGroup }, true, sourceId)
        if (reportFieldSelectionTypeEnum != ReportFieldSelectionTypeEnum.QUERY) {
            CustomReportField.searchByType(reportFieldSelectionTypeEnum).each { customField ->
                Map<String, List> group = fieldSelection.find { it.text[0] == customField.fieldGroup?.name }
                if (group) {
                    group.children.add(customField)
                } else {
                    fieldSelection.add(text: [customField.fieldGroup?.name], children: [customField])
                }
            }
        }
        Set<ReportField> blindedFieldsForUser = User.getBlindedFieldsForUser(user)
        fieldSelection.each { group ->
            group.children.each { field ->
                if (blindedFieldsForUser.find { it.id == field.id }) field.isBlinded = true
            }
        }
        Set<ReportField> protectedFieldsForUser = User.getProtectedFieldsForUser(user)
        return fieldSelection.each { group ->
            group.children.each { field ->
                if (protectedFieldsForUser.find { it.id == field.id }) field.isProtected = true
            }
        }
    }

    def getDataFromVWRPTFIELD(String uniqueId , Long tenantId, String lang){
        if(uniqueId.contains(Constants.USER_DEFINED_FIELD)){
            seedDataService.seedSourceTableMasterMetaTable(uniqueId, tenantId, lang)
            seedDataService.seedSourceColumnMasterMetaTable(uniqueId, tenantId, lang)
        }
        seedDataService.seedReportFieldsMetaTable(uniqueId, tenantId, lang)
        seedDataService.seedReportFieldLocalizations(uniqueId, tenantId, lang)
    }


    Map loadSingleValuesToCacheFile(String reportFieldName) {
        Map allLangMap = [:]
        if (reportFieldName) {
            List locales = LocaleHelper.buildLocaleSelectList()
            reportFieldCache.clear()
            List result = GParsPool.withPool(2) {
                locales.collectParallel { obj ->
                    File f = new File(System.getProperty("java.io.tmpdir"), valuesFileName + "_" + obj.lang_code)
                    if (f.exists()) {
                        try {
                            persistenceInterceptor.init()
                            def singleFieldMap = retrieveValuesFromDatabase(obj.lang_code, reportFieldName)
                            if (singleFieldMap != null && singleFieldMap.keySet().size() > 0) {
                                allLangMap.put(obj.lang_code , serializeValuesForSingleField(singleFieldMap, obj.lang_code, reportFieldName))
                            }
                            persistenceInterceptor.flush()
                        } finally {
                            persistenceInterceptor.destroy()
                        }
                    }
                    return obj.lang_code
                }
            }
            if (!locales*.lang_code.containsAll(result)) {
                throw new GrailsRuntimeException("Error Occurred while loading cache: ${result}")
            }
            reportFieldCache.clear()
        }
        else{
            log.error("No Field present in Application with report Field Name as -> ${reportFieldName}")
        }
        return allLangMap
    }

    String getFieldVariableForUniqueId(String uniqueFieldString , String langId , Long tenantId){
        Sql sql = new Sql(dataSource_pva)
        String uniqueString = null
        try {
            List<GroovyRowResult> reportFieldVariable = sql.rows("SELECT NAME FROM RPT_FIELD WHERE UNIQUE_FIELD_ID = '${uniqueFieldString}' AND LOC = '${langId}' AND TENANT_ID = ${tenantId}")
            if (reportFieldVariable) {
                uniqueString = reportFieldVariable[0].get("NAME")
            } else {
                log.error("No Field present in Views for the unique Field Id -> ${uniqueFieldString}")
            }
        }
        catch(Exception exc){
            log.error("Sql Exception Caught fetchin java variable->" , exc)
            throw exc
        }
        finally {
            sql?.close()
        }
        return uniqueString
    }

    void addFileDatatoCache(Map allLangMap){
        try {
            List locales = LocaleHelper.buildLocaleSelectList()
            locales.collect { obj ->
                if (!values) {
                    values = [:]
                }
                values.put(obj.lang_code, allLangMap.get(obj.lang_code))
            }
        }
        catch(Exception ex){
            log.error("Not Able to Update the Cache due to " , ex)
            throw ex
        }
    }

    boolean isLinkedField(String name){
        String sqlLinkedQuery = "select count(*) as count from vw_csfm_pvr_field_details vwc join rpt_field rf on (vwc.APPLICATION_FIELD_VARIABLE = rf.name) where vwc.APPLICATION_FIELD_VARIABLE = '${name}'"
        Sql sql = new Sql(dataSource_pva)
        try {
            GroovyRowResult linkedResult = sql.firstRow(sqlLinkedQuery)
            if (linkedResult.COUNT != 0) {
                return true
            }
            return false
        }
        catch (SQLException sqlexc){
            log.error("Not Able to fetch the count due to {}" , sqlexc)
        }
    }

}
