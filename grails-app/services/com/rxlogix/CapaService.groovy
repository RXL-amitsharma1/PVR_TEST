package com.rxlogix

import grails.gorm.transactions.Transactional
import groovy.sql.Sql

@Transactional
class CapaService {

    def dataSource_pva
    def reportExecutorService

    def createCAPA(String label, String appType, Long capaType){
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            if(capaType==0L) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, label, null, appType, 0, null, null])
            } else {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, label, appType, 0, null, null])
            }
        } catch(Exception e){
            log.error("Error occurred in creating CAPA", e)
        } finally{
            sql?.close()
        }
    }

    def deleteCAPA(Long id, Long capaType,String ownerApp){
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            if(capaType==0L) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, null, id, null, null, null,ownerApp])
            } else {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, null, null, id, null, null,ownerApp])
            }
        } catch(Exception e){
            log.error("Error occurred in deleting late mapping", e)
        } finally{
            sql?.close()
        }
    }

    def editCAPA(Long id, String label, String appType, Long capaType){
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            if(capaType==0) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, (id + ";" + label), null, appType, 1, null, null])
            } else {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, (id + ";" + label), appType, 1, null, null])
            }
        } catch(Exception e){
            log.error("Error occurred in editing late mapping", e)
        } finally{
            sql?.close()
        }
    }
}
