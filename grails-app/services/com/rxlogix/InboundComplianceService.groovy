package com.rxlogix

import com.rxlogix.config.InboundCompliance
import com.rxlogix.config.InboundInitialConfiguration
import com.rxlogix.config.ReportField
import com.rxlogix.config.SuperQuery
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.NotTransactional
import groovy.json.JsonSlurper
import groovy.sql.Sql

import java.sql.SQLException

@Transactional
class InboundComplianceService {

    def dataSource_pva
    def userService
    def customMessageService

    Map fetchConfigurationMapFromSession(params, session) {
        Map inboundComplianceParams = null
        Map queryComplianceIndexMap = null
        Map editingInboundComplianceMap = session.editingInboundCompliance
        if (editingInboundComplianceMap?.inboundComplianceParams && params.continueEditing) {
            queryComplianceIndexMap = [index: editingInboundComplianceMap.queryComplianceIndex, type: "query"]
            inboundComplianceParams = new JsonSlurper().parseText(editingInboundComplianceMap.inboundComplianceParams) as Map
        }
        [inboundComplianceParams: inboundComplianceParams, queryComplianceIndex: queryComplianceIndexMap]
    }

    @NotTransactional
    void initConfigurationQueriesFromSession(session, InboundCompliance inboundCompliance) {
        Integer queryComplianceIndex = session.editingInboundCompliance.queryComplianceIndex as Integer
        if (session.editingInboundCompliance.queryId) {
            if (queryComplianceIndex != null &&
                    queryComplianceIndex < inboundCompliance.queriesCompliance.size() &&
                    inboundCompliance.queriesCompliance[queryComplianceIndex])
                inboundCompliance.queriesCompliance[queryComplianceIndex].query = SuperQuery.get(session.editingInboundCompliance.queryId as Long)
            else {
                for (int i = 0; i < inboundCompliance.queriesCompliance.size(); i++) {
                    if (inboundCompliance.queriesCompliance[i].query == null) {
                        inboundCompliance.queriesCompliance[i].query = SuperQuery.get(session.editingInboundCompliance.queryId as Long)
                        session.editingInboundCompliance.queryComplianceIndex = i
                        break
                    }
                }
            }
        }
    }
    void removeRemovedQueriesCompliance(InboundCompliance configurationInstance) {
        def _toBeRemoved = configurationInstance.queriesCompliance?.findAll {
            (it?.dynamicFormEntryDeleted || (it == null) || (it?.query == null && (it?.criteriaName == null || it?.criteriaName.trim() == '')))
        }
        if (_toBeRemoved) {
            configurationInstance.queriesCompliance?.removeAll(_toBeRemoved)
        }
        configurationInstance.queriesCompliance?.eachWithIndex() { query, i ->
            if (query) {
                query.index = i
            }
        }
    }

    def getDataBasedOnLmsql() {
        String fieldName = InboundInitialConfiguration?.first()?.reportField?.name
        log.debug("field name --> DataBasedOnLmSql(): ${fieldName}")
        if(fieldName) {
            ReportField reportField = ReportField.findByNameAndIsDeleted(fieldName,false)
            def results
            if (reportField.listDomainClass?.name) {
                String currentLang = userService.user?.preference?.locale?.language
                if (reportField.isAutocomplete) {
                    Sql sql = new Sql(dataSource_pva)
                    try {
                        String sqlString = reportField.getLmSql(currentLang)?.replace("like ?", "LIKE '%%'")?.replace("LIKE :SEARCH_TERM", "LIKE '%%'") //TO handle Non cache and Auto queries. Also added tenant parameter.
                        String columnName
                        Long tenantId = Tenants.currentId() as Long
                        def result = sql.rows(sqlString, [TENANT_ID: tenantId]) { meta ->
                            columnName = meta.getColumnName(1)
                        }
                        return result.collect {[name: it.getProperty(columnName), display: it.getProperty(columnName)]}
                    } catch (SQLException sqlEx) {
                        log.error("Could not Validate autocomplete SQL for ReportField: ${reportField.name}, ${sqlEx.message}")
                    } finally {
                        sql?.close()
                    }
                } else {
                    if (reportField.hasSelectableList() && reportField.name != '') {
                        long start = System.currentTimeMillis()
                        SelectableList selectableListInstance = (SelectableList) reportField.listDomainClass.newInstance()
                        if (selectableListInstance.hasProperty('dataSource')) {
                            selectableListInstance.dataSource = dataSource_pva
                            selectableListInstance.sqlString = reportField.getLmSql(currentLang)
                        }
                        String reportFieldName = customMessageService.getMessage("app.reportField." + reportField.name)
                        String locale = reportFieldName.endsWith('(J)') ? Locale.JAPANESE : Locale.ENGLISH
                        results = selectableListInstance.getSelectableList(locale)
                        long end = System.currentTimeMillis()
                        log.debug("Found ${reportField.name} lang ${currentLang} with ${selectableListInstance.class.name} with entries in ${end - start}ms")
                        return results.collect { [name: it, display: it ] }
                    }
                }
            }
        }
        return null
    }

}
