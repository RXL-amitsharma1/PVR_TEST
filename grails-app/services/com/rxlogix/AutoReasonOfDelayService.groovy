package com.rxlogix

import com.rxlogix.config.AutoReasonOfDelay
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.SuperQuery
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import grails.gorm.transactions.NotTransactional

@Transactional
class AutoReasonOfDelayService {

    Map fetchConfigurationMapFromSession(params, session) {
        Map autoReasonOfDelayParams = null
        Map queryRCAIndexMap = null
        Map editingAutoReasonOfDelayMap = session.editingAutoReasonOfDelay
        if (editingAutoReasonOfDelayMap?.autoReasonOfDelayParams && params.continueEditing) {
            queryRCAIndexMap = [index: editingAutoReasonOfDelayMap.queryRCAIndex, type: "query"]
            autoReasonOfDelayParams = new JsonSlurper().parseText(editingAutoReasonOfDelayMap.autoReasonOfDelayParams) as Map
        }
        [autoReasonOfDelayParams: autoReasonOfDelayParams, queryRCAIndex: queryRCAIndexMap]
    }

    @NotTransactional
    void initConfigurationQueriesFromSession(session, AutoReasonOfDelay autoReasonOfDelayInstance) {
        Integer queryRCAIndex = session.editingAutoReasonOfDelay.queryRCAIndex as Integer
        if (session.editingAutoReasonOfDelay.queryId) {
            if (queryRCAIndex != null &&
                    queryRCAIndex < autoReasonOfDelayInstance.queriesRCA.size() &&
                    autoReasonOfDelayInstance.queriesRCA[queryRCAIndex])
                autoReasonOfDelayInstance.queriesRCA[queryRCAIndex].query = SuperQuery.get(session.editingAutoReasonOfDelay.queryId as Long)
            else {
                for (int i = 0; i < autoReasonOfDelayInstance.queriesRCA.size(); i++) {
                    if (autoReasonOfDelayInstance.queriesRCA[i].query == null) {
                        autoReasonOfDelayInstance.queriesRCA[i].query = SuperQuery.get(session.editingAutoReasonOfDelay.queryId as Long)
                        session.editingAutoReasonOfDelay.queryRCAIndex = i
                        break
                    }
                }
            }
        }
    }
}
