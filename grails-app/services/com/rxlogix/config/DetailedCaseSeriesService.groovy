package com.rxlogix.config

import grails.config.Config
import grails.converters.JSON
import grails.core.support.GrailsConfigurationAware
import grails.gorm.transactions.ReadOnly

@ReadOnly
class DetailedCaseSeriesService implements GrailsConfigurationAware {

    Map<String, Map> primaryFields
    Map<String, Map> secondaryFields
    String pvdExtractIdQuery
    String caseSeriesQuery
    List<String> downloadFields

    @Override
    void setConfiguration(Config co) {
        String primaryFieldsConfig = co.getRequiredProperty('caseSeries.list.primaryFields', String)
        String secondaryFieldsConfig = co.getRequiredProperty('caseSeries.list.secondaryFields', String)
        pvdExtractIdQuery = co.getRequiredProperty('caseSeries.list.pvd.query', String)
        caseSeriesQuery = co.getRequiredProperty('caseSeries.list.query', String)
        primaryFields =  new LinkedHashMap((JSON.parse(primaryFieldsConfig) as Map))
        secondaryFields =  new LinkedHashMap((JSON.parse(secondaryFieldsConfig) as Map))
        downloadFields = new ArrayList<String>(JSON.parse(co.getRequiredProperty('caseSeries.list.downloadFields', String)) as List)
    }

    String getQueryToExecute(String searchQuery, String sort, String direction) {
        return caseSeriesQuery.replace(':ORDER_BY', sort).replace(':SORT_DIRECTION', direction).replace(':SEARCH_QUERY', searchQuery)
    }

    Map getPrimaryVisibleFields() {
        return new LinkedHashMap(primaryFields.findAll {
            it.value.visible == true
        }.sort {
            if(!it.value.order) {
                it.value.order = 999
            }
            it.value.order
        })
    }

    boolean isDateType(String field) {
        if (primaryFields.get(field) && primaryFields.get(field).type == 'Date') {
            return true
        }
        if (secondaryFields.get(field) && secondaryFields.get(field).type == 'Date') {
            return true
        }
        return false
    }

}
