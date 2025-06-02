package com.rxlogix.odata

import com.rxlogix.OdataService
import com.rxlogix.config.OdataSettings
import grails.converters.JSON
import grails.util.Holders
import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType
import org.apache.olingo.commons.api.edm.provider.CsdlProperty
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef
import org.springframework.jdbc.datasource.SimpleDriverDataSource

import javax.sql.DataSource

class OdataSettingsCache {

    public static final String NAMESPACE = "OData.Demo";
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    private static Map<String, Map> entityMap = [:]
    private static Map<String, Map> entityFields = [:]
    private static Map<String, Map<String, FullQualifiedName>> FQN = [:]
    private static Map<String, Date> DS_CACHE_DATES = [:]

    static OdataService getOdataService() {
        return Holders.applicationContext.getBean("odataService")
    }

    static private Map<String,DataSource> datasources = [:]

    static DataSource getDS(String dsName) {
        if (!datasources[dsName]) {
            synchronized (datasources) {
                if (datasources[dsName]) return datasources[dsName]
                OdataSettings odataSettings = OdataSettings.findByDsNameAndIsDeleted(dsName, false)
                SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource()
                simpleDriverDataSource.driverClass = Class.forName("oracle.jdbc.OracleDriver")
                simpleDriverDataSource.username = odataSettings.dsLogin
                simpleDriverDataSource.password = odataSettings.getPasswordDecoded()
                simpleDriverDataSource.url = "jdbc:oracle:thin:@" + odataSettings.dsUrl
                datasources[dsName] = simpleDriverDataSource
            }
        }

        return datasources[dsName]
    }


    static void clearCacheAndUpdateDateForDs(String dsName, Date date) {
        datasources.remove(dsName)
        entityMap.remove(dsName)
        entityFields.remove(dsName)
        FQN.remove(dsName)
        if (date) {
            DS_CACHE_DATES.put(dsName, date)
        } else {
            DS_CACHE_DATES.remove(dsName)
        }
    }

    static Date getDsCacheDate(String dsName){
        return DS_CACHE_DATES.get(dsName)
    }

    /* -- entityMap for dbSource structure example ---
   entityMap=[
           CaseSeries: [    tableName  : "C_IDENTIFICATION",
                            description: "some description",
                            limitQuery : "CASE_ID>100015",
                            create : true,
                            update: true,
                            delete : true,
                            fields     : ["ID"             : "CASE_ID",
                                          "versionNum"    : "VERSION_NUM",
                                          "caseNumber"     : "CASE_NUM",
                                          "dateCreate"     : "DATE_CREATE_MOST_RECENT"
                            ]],
          ....
   ]
   */

    static Map getEntitiesMap(String dsName) {
        if (!entityMap[dsName]) {
            OdataSettings.withNewSession {
                entityMap[dsName] = JSON.parse(OdataSettings.findByDsNameAndIsDeleted(dsName,false).settings)
            }
        }
        return entityMap[dsName]
    }

    static Map getEntityMap(String dsName, String entityName) {
        return getEntitiesMap(dsName)[entityName]
    }

    static def reloadEntityMap() {
        FQN = [:]
        entityFields = [:]
        entityMap = [:]
        DS_CACHE_DATES = [:]
        datasources = [:]
    }

    static FullQualifiedName getFullQualifiedName(String dsName, String entityName) {
        if (!FQN[dsName]) {
            FQN[dsName] = [:]
        }
        if (!FQN[dsName][entityName]) {
            FQN[dsName][entityName] = new FullQualifiedName(NAMESPACE, entityName);
        }
        FQN[dsName][entityName]
    }

    static Map getEntityFields(String dsName, String entityName) {
        if (!entityFields[dsName]) entityFields[dsName] = [:]
        def entry = entityFields[dsName][entityName]
        if (!entry) {

            List fields = getOdataService().getFieldsType(dsName, getEntityMap(dsName, entityName).tableName, getEntityMap(dsName, entityName).fields)
            //create EntityType properties
            List<CsdlProperty> properties = []
            boolean hasId = false
            fields.each {
                if (it.label == "ID") hasId = true
                properties << new CsdlProperty().setName(it.label).setType(OdataUtils.getEdmPrimitiveTypeKindName(it.dataType)).setPrecision(42);;
            }
            // configure EntityType
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(entityName);
            entityType.setProperties(properties);
            if (hasId) {
                CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                propertyRef.setName("ID");
                entityType.setKey(Collections.singletonList(propertyRef));
            } else {

            }
            entityFields[dsName] << [(entityName): [fields: fields, type: entityType]]
        }
        return entityFields[dsName][entityName]
    }


}
