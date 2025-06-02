package com.rxlogix.dictionary

import com.rxlogix.Constants
import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql

@Transactional
class DictionaryGroupDaoService {

    def CRUDService
    def sessionFactory
    def signalIntegrationService
    GrailsApplication grailsApplication

    void save(DictionaryGroup dictionaryGroup, DictionaryGroupCmd dgc, Integer tenantId,boolean flag,boolean ifUpdate) {
        syncProperties(dgc, dictionaryGroup, tenantId,ifUpdate)
        boolean notifySignal = false
        try {
            notifySignal = dictionaryGroup?.id ? true : false
            CRUDService.saveOrUpdate(dictionaryGroup, [flush: true])
        }
        catch (Exception e){
            log.error(e.getMessage())
            return
        }

        // Notify PVSignal to update their tables with these new changes
        if (notifySignal && grailsApplication.config.pvsignal.url){
            signalIntegrationService.notifySignalForUpdate(dictionaryGroup)
        }

        sessionFactory.currentSession.flush()
        if(flag){
            syncJSONDataBulk(dictionaryGroup, dgc,flag)
        }
        else{
            syncJSONData(dictionaryGroup, dgc)
        }

    }

    void delete(DictionaryGroup dictionaryGroup, String userName) {
        dictionaryGroup.isDeleted = true
        dictionaryGroup.modifiedBy = userName
        CRUDService.softDelete(dictionaryGroup, dictionaryGroup.groupName, "Deleted Dictionary Group", [flush: true])
        sessionFactory.currentSession.flush()
        List<String> availableDataSources = Holders.getGrailsApplication().getConfig().supported.datasource
        List<String> dataSources = dictionaryGroup.isEventGroup() ? (availableDataSources) : (dictionaryGroup.dataSources?.toList())
        dataSources?.each { String dsn ->
            DictionaryGroupData."$dsn".withNewSession { session ->
                DictionaryGroupData dGD = DictionaryGroupData."$dsn".get(dictionaryGroup.id)
                if (dGD) {
                    dGD.isDeleted = true
                    dGD."$dsn".save(flush: true, failOnError: true, insert: false)
                }
            }
        }
    }


    private void syncProperties(DictionaryGroupCmd dgc, DictionaryGroup dictionaryGroup, Integer tenantIdValue,boolean ifUpdate) {
        dictionaryGroup.groupName = dgc.groupName
        dictionaryGroup.isMultiIngredient = dgc.isMultiIngredient
        dictionaryGroup.includeWHODrugs = dgc.includeWHODrugs
        if (dictionaryGroup.includeWHODrugs)
            dictionaryGroup.isMultiIngredient = true
        dictionaryGroup.with {
            groupName = dgc.groupName
            description = dgc.description
            modifiedBy = dgc.owner
            if (!id) {
                owner = User.findByUsernameIlike(dgc.owner)
                createdBy = dgc.owner
                tenantId = tenantIdValue
                type = dgc.type
            }
        }
        if(!ifUpdate){
            dictionaryGroup.dataSources?.clear()
        }
        dictionaryGroup.sharedWithUser?.clear()
        dictionaryGroup.sharedWithGroup?.clear()
        if (dgc.sharedWith) {
            dgc.sharedWith.split(";").each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    dictionaryGroup.addToSharedWithGroup(UserGroup.load(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, ''))))
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    dictionaryGroup.addToSharedWithUser(User.load(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, ''))))
                }
            }
        }
        if(!ifUpdate) {
            dgc.dataSources.each {
                dictionaryGroup.addToDataSources(it)
            }
        }
        else{
            dgc.dataSourceNames.each {
                if(!dictionaryGroup.dataSources.contains(it)){
                    dictionaryGroup.addToDataSources(it)
                }
            }
        }
    }

    private void syncJSONData(DictionaryGroup dg, DictionaryGroupCmd dgc) {
        List<String> availableDataSources = Holders.getGrailsApplication().getConfig().getRequiredProperty('supported.datasource', List<String>)
        //In case of eventGroup send data to all dataSources
        List<String> dataSources = dg.isEventGroup() ? (availableDataSources) : dgc.dataSources
        availableDataSources.each { String dsn ->
            Holders.applicationContext.getBean('dataSource_' + dsn).getConnection().withCloseable { connection ->
                DictionaryGroupData."$dsn".withTransaction { status ->
                    DictionaryGroupData dGD = DictionaryGroupData."$dsn".get(dg.id)
                    if (!(dsn in dataSources)) {
                        if (dGD) {
                            dGD.isDeleted = true
                            dGD."$dsn".save(flush: true, failOnError: true, insert: false)
                        }
                        return
                    }
                    boolean insert = false
                    if (!dGD) {
                        dGD = new DictionaryGroupData()
                        dGD.id = dg.id
                        insert = true
                    }
                    dGD.tenantId = dg.tenantId
                    dGD.groupName = dg.groupName
                    dGD.type = dg.type
                    dGD.isDeleted = false
                    dGD.data = dgc.getDataFor(dg.isEventGroup() ? 'pva' : dsn)
                    dGD."$dsn".save(flush: true, failOnError: true, insert: insert)
                    def sql = new Sql(connection)
                    sql.call('{call PKG_DICT_GROUP.P_DICT_GRP_JSON_MAP(?,?,?)}', [dg.tenantId, dg.id, dg.type])
                }
            }
        }
    }
    private void syncJSONDataBulk(DictionaryGroup dg, DictionaryGroupCmd dgc,boolean flag) {
        List<String> availableDataSources = Holders.getGrailsApplication().getConfig().getRequiredProperty('supported.datasource', List<String>)
        //In case of eventGroup send data to all dataSources
        Set<String> dataSources = dg.isEventGroup() ? (availableDataSources) : dgc.dataSourceNames
        availableDataSources.each { String dsn ->
            DictionaryGroupData."$dsn".withNewSession { session ->
                DictionaryGroupData dGD = DictionaryGroupData."$dsn".get(dg.id)
                if(!(dsn in dataSources) && dGD){
                    if(dGD.isDeleted){
                        return;
                    }
                    dGD.groupName = dg.groupName
                    dGD."$dsn".save(flush: true, failOnError: true, insert: false)
                    def sql = new Sql(session.connection())
                    sql.call('{call PKG_DICT_GROUP.P_DICT_GRP_JSON_MAP(?,?,?)}', [dg.tenantId, dg.id, dg.type])
                    return
                }
                if (!(dsn in dataSources)) {
                    return               // In this case we don't want to delete existing produ
                }
                boolean insert = false
                if (!dGD) {
                    dGD = new DictionaryGroupData()
                    dGD.id = dg.id
                    insert = true
                }
                dGD.tenantId = dg.tenantId
                dGD.groupName = dg.groupName
                dGD.type = dg.type
                dGD.isDeleted = false
                if(dsn in dataSources){
                    dGD.data = dgc.getDataFor(dg.isEventGroup() ? 'pva' : dsn)
                }
                dGD."$dsn".save(flush: true, failOnError: true, insert: insert)
                def sql = new Sql(session.connection())
                sql.call('{call PKG_DICT_GROUP.P_DICT_GRP_JSON_MAP(?,?,?)}', [dg.tenantId, dg.id, dg.type])
            }

        }
    }
}
