package com.rxlogix

import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import grails.converters.JSON
import com.rxlogix.config.AutoReasonOfDelay
import com.rxlogix.config.Late
import com.rxlogix.config.ResponsibleParty
import com.rxlogix.config.RootCause
import com.rxlogix.config.RootCauseClassification
import com.rxlogix.config.RootCauseSubCategory
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import javax.persistence.EntityExistsException

@Transactional
class ReasonOfDelayService {

    def dataSource_pva
    def userService

    void auditLogCreate(def theInstance) {

        Map update = theInstance.properties.collectEntries { k, v -> [(k), v] }
        AuditLogConfigUtil.logChanges(theInstance,update, null,"INSERT")
    }

    void auditLogDelete(def theInstance, justification) {
        theInstance.deleteJustification=justification
        AuditLogConfigUtil.logChanges(theInstance,null, theInstance.properties.collectEntries { k, v -> [(k), v] },"DELETE")
    }

    void auditLogSave(def theInstance) {
        Map beforeUpdate = theInstance.properties.collectEntries { k, v -> [(k), v] }
        theInstance.refresh()
        Map afterUpdate = theInstance.properties.collectEntries { k, v -> [(k), v] }
        List changesMade = []
        AuditLogConfigUtil.logChanges(theInstance,afterUpdate, beforeUpdate,"UPDATE")
    }


    //PVR-34838 User want to hide the RCA mapping  hide flag  NUMBER  --0:Nothing,1:hide,2:unhide
    @ReadOnly(connection = 'pva')
    def createLateMapping(String label, List<Long> rootCauseIds, String appType, Long lateType, List<Long> rootCauseClassIds,boolean hide) {
        checkRcaEntityAlreadyCreated(Late.class, label, appType)
        Sql sql
        String rootCauseString = null
        int hidden = 0
        hidden = hide?2:1
        if (rootCauseIds.size() > 0) {
            rootCauseString = getRootCauseAddString(rootCauseIds)
        }
        try {
            sql = new Sql(dataSource_pva)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [label, lateType, rootCauseString, null, null, null, appType, 0, getRootCauseClassAddString(rootCauseClassIds), null])
            Late late = Late.findByTextDescAndLateTypeAndOwnerApp(label, lateType, appType)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [late.id, null, null, null, null, null, null, appType,hidden])
            auditLogCreate(late)
        } catch (Exception e) {
            log.error("Error occurred in creating late mapping", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def createRootCauseMapping(String label, List<Long> responsiblePartyIds, String appType, List<Long> rootCauseSubIds,boolean hide) {
        checkRcaEntityAlreadyCreated(RootCause.class, label, appType)
        Sql sql
        String responsiblePartyString = null
        int hidden = 0
        hidden = hide?2:1
        if (responsiblePartyIds.size() > 0) {
            responsiblePartyString = getReponsiblePartyAddString(responsiblePartyIds)
        }
        try {
            sql = new Sql(dataSource_pva)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, label, responsiblePartyString ?: null, null, null, appType, 0, null, getRootCauseSubAddString(rootCauseSubIds)])
            RootCause rootCause = RootCause.findByTextDescAndOwnerApp(label, appType)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null,rootCause.id, null, null, null, null, null, appType,hidden])
            auditLogCreate(rootCause)
        } catch (Exception e) {
            log.error("Error occurred in creating root cause mapping", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def createResponsibleParty(String label, String appType,boolean hide) {
        checkRcaEntityAlreadyCreated(ResponsibleParty.class, label, appType)
        Sql sql
        int hidden = 0
        hidden = hide?2:1
        try {
            sql = new Sql(dataSource_pva)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, label, null, null, appType, 0, null, null])
            ResponsibleParty responsibleParty = ResponsibleParty.findByTextDescAndOwnerApp(label, appType)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, null,responsibleParty.id, null, null, null, null, appType,hidden])
            auditLogCreate(responsibleParty)
        } catch (Exception e) {
            log.error("Error occurred in creating responsible party", e)
        } finally {
            sql?.close()
        }

    }

    @ReadOnly(connection = 'pva')
    def createRootCauseSub(String label, String appType,boolean hide) {
        checkRcaEntityAlreadyCreated(RootCauseSubCategory.class, label, appType)
        Sql sql
        int hidden = 0
        hidden = hide?2:1
        try {
            sql = new Sql(dataSource_pva)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, appType, 0, null, label])
            RootCauseSubCategory rootCauseSubCategory = RootCauseSubCategory.findByTextDescAndOwnerApp(label, appType)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null,rootCauseSubCategory.id, appType,hidden])
            auditLogCreate(rootCauseSubCategory)
        } catch (Exception e) {
            log.error("Error occurred in creating root cause subcategory", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def createRootCauseClass(String label, String appType,boolean hide) {
        checkRcaEntityAlreadyCreated(RootCauseClassification.class, label, appType)
        Sql sql
        int hidden = 0
        hidden = hide?2:1
        try {
            sql = new Sql(dataSource_pva)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, appType, 0, label, null])
            RootCauseClassification rootCauseClassification = RootCauseClassification.findByTextDescAndOwnerApp(label, appType)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null,rootCauseClassification.id, null, appType,hidden])
            auditLogCreate(rootCauseClassification)
        } catch (Exception e) {
            log.error("Error occurred in creating root cause subcategory", e)
        } finally {
            sql?.close()
        }
    }


    @ReadOnly(connection = 'pva')
    def hideWarning(Long id, String active) {
        def warning = 0
        if(id) {
            if (active == 'showLate') {
                Late late = Late.get(id)
                List<AutoReasonOfDelay> autoReasonOfDelayList = AutoReasonOfDelay.findAll()
                if (autoReasonOfDelayList.size() > 0) {
                    def test = autoReasonOfDelayList.get(0)
                    def lateIds = test.queriesRCA.lateId
                    def result = lateIds.collect() { it ->
                        if (it == late.id) {
                            true
                        }
                    }
                    return result.contains(true)
                }
                return false
            } else if (active == 'showRootCause') {
                RootCause rootCause = RootCause.get(id)
                List<AutoReasonOfDelay> autoReasonOfDelayList = AutoReasonOfDelay.findAll()
                if (autoReasonOfDelayList.size() > 0) {
                    def test = autoReasonOfDelayList.get(0)
                    def lateIds = test.queriesRCA.rootCauseId
                    def result = lateIds.collect() { it ->
                        if (it == rootCause.id) {
                            true
                        }
                    }
                    return result.contains(true)
                }
                return false

            } else if (active == 'showRootCauseSub') {
                RootCauseSubCategory rootCauseSubCategory = RootCauseSubCategory.get(id)
                List<AutoReasonOfDelay> autoReasonOfDelayList = AutoReasonOfDelay.findAll()
                if (autoReasonOfDelayList.size() > 0) {
                    def test = autoReasonOfDelayList.get(0)
                    def lateIds = test.queriesRCA.rootCauseSubCategoryId
                    def result = lateIds.collect() { it ->
                        if (it == rootCauseSubCategory.id) {
                            true
                        }
                    }
                    return result.contains(true)
                }
                return false
            } else if (active == 'showRootCauseClass') {
                RootCauseClassification rootCauseClassification = RootCauseClassification.get(id)
                List<AutoReasonOfDelay> autoReasonOfDelayList = AutoReasonOfDelay.findAll()
                if (autoReasonOfDelayList.size() > 0) {
                    def test = autoReasonOfDelayList.get(0)
                    def lateIds = test.queriesRCA.rootCauseClassId
                    def result = lateIds.collect() { it ->
                        if (it == rootCauseClassification.id) {
                            true
                        }
                    }
                    return result.contains(true)
                }
                return false
            } else {
                ResponsibleParty responsibleParty = ResponsibleParty.get(id)
                List<AutoReasonOfDelay> autoReasonOfDelayList = AutoReasonOfDelay.findAll()
                if (autoReasonOfDelayList.size() > 0) {
                    def test = autoReasonOfDelayList.get(0)
                    def lateIds = test.queriesRCA.responsiblePartyId
                    def result = lateIds.collect() { it ->
                        if (it == responsibleParty.id) {
                            true
                        }
                    }
                    return result.contains(true)
                }
                return false
            }
        }
    }

    @ReadOnly(connection = 'pva')
    def deleteLateMapping(Long id, String ownerApp, String justification) {
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            auditLogDelete(Late.get(id), justification)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [id, null, null, null, null, null, null, ownerApp])
        } catch (Exception e) {
            log.error("Error occurred in deleting late mapping", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def deleteRootCauseMapping(Long id, String ownerApp, String justification) {
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            auditLogDelete(RootCause.get(id), justification)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, id, null, null, null, null, null, ownerApp])
        } catch (Exception e) {
            log.error("Error occurred in deleting root cause", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def deleteResponsiblePartyMapping(Long id, String ownerApp, String justification) {
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            auditLogDelete(ResponsibleParty.get(id), justification)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, id, null, null, null, null, ownerApp])
        } catch (Exception e) {
            log.error("Error occurred in deleting responsible party", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def deleteRootCauseSubMapping(Long id, String ownerApp, String justification) {
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            auditLogDelete(RootCauseSubCategory.get(id), justification)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, id, ownerApp])
        } catch (Exception e) {
            log.error("Error occurred in deleting root cause subcategory", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def deleteRootCauseClassMapping(Long id, String ownerApp, String justification) {
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            auditLogDelete(RootCauseClassification.get(id), justification)
            sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, null, null, null, id, null, ownerApp])
        } catch (Exception e) {
            log.error("Error occurred in deleting root cause subcategory", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def editLateMapping(Long id, String label, List<Long> rootCauseIds, String appType, Long lateType, List<Long> rootCauseClassIds,boolean hide) {
        checkRcaEntityAlreadyCreated(Late.class, label, appType, id)
        Sql sql
        Late late = Late.get(id)
        int hidden = 0
        hidden = hide?2:1
        String rootCauseAddString = null
        String rootCauseRemoveString = null
        List<Long> rootCauseRemoved = []
        late.rootCauseIds?.each {
            if (!rootCauseIds.contains(it)) {
                rootCauseRemoved.add(it)
            }
        }
        if (rootCauseRemoved.size() > 0) {
            rootCauseRemoveString = getRootCauseRemoveString(late.id, rootCauseRemoved)
        }
        List<Long> rootCauseAdded = []
        rootCauseIds.each {
            if (!late.rootCauseIds.contains(it)) {
                rootCauseAdded.add(it)
            }
        }
        if (rootCauseAdded.size() > 0) {
            rootCauseAddString = getRootCauseAddString(rootCauseAdded)
        }
        String rootCauseClassAddString = getRootCauseClassAddString(rootCauseClassIds - late.rootCauseClassIds)
        String rootCauseClassRemoveString = (late.rootCauseClassIds - rootCauseClassIds).collect { late.id + ";" + it } join(",")
        try {
            sql = new Sql(dataSource_pva)
            Long objLateType = late.lateType
            if (label != late?.textDesc || objLateType != lateType) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [(id + ";" + label), lateType, null, null, null, null, appType, 1, null, null])
            }
            if (rootCauseRemoveString != null) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, rootCauseRemoveString, null, null, null, null, null, appType])
            }
            if (rootCauseAddString != null) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [label, lateType, rootCauseAddString, null, null, null, appType, 0, null, null])
            }
            if (rootCauseClassRemoveString) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, null, null, null, rootCauseClassRemoveString, null, appType])
            }
            if (rootCauseClassAddString) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [label, lateType, null, null, null, null, appType, 0, rootCauseClassAddString, null])
            }
            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [id, null, null, null, null, null, null, appType,hidden])

            auditLogSave(late)
        } catch (Exception e) {
            log.error("Error occurred in editing late mapping", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def editRootCauseMapping(Long id, String label, List<Long> respPartyIds, String appType, List<Long> rootCauseSubIds,boolean hide) {
        checkRcaEntityAlreadyCreated(RootCause.class, label, appType, id)
        Sql sql
        RootCause rootCause = RootCause.get(id)
        int hidden = 0
        hidden = hide?2:1
        String responsiblePartyAddString = null
        String responsiblePartyRemoveString = null
        List<Long> respPartyRemoved = []
        rootCause.responsiblePartyIds?.each {
            if (!respPartyIds.contains(it)) {
                respPartyRemoved.add(it)
            }
        }
        if (respPartyRemoved.size() > 0) {
            responsiblePartyRemoveString = getResponsiblePartyRemoveString(rootCause.id, respPartyRemoved)
        }
        List<Long> respPartyAdded = []
        respPartyIds.each {
            if (!rootCause.responsiblePartyIds.contains(it)) {
                respPartyAdded.add(it)
            }
        }
        if (respPartyAdded.size() > 0) {
            responsiblePartyAddString = getReponsiblePartyAddString(respPartyAdded)
        }
        String rootCauseSubAddString = getRootCauseSubAddString(rootCauseSubIds - rootCause.rootCauseSubCategoryIds)
        String rootCauseSubRemoveString = (rootCause.rootCauseSubCategoryIds - rootCauseSubIds).collect { rootCause.id + ";" + it } join(",")
        try {
            sql = new Sql(dataSource_pva)
            if (label != rootCause?.textDesc) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, (id + ";" + label), null, null, null, appType, 1, null, null])
            }
            if (responsiblePartyRemoveString != null) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, responsiblePartyRemoveString, null, null, null, null, appType])
            }
            if (responsiblePartyAddString != null) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, label, responsiblePartyAddString, null, null, appType, 0, null, null])
            }
            if (rootCauseSubRemoveString) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_DELETE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, rootCauseSubRemoveString, appType])
            }
            if (rootCauseSubAddString != null) {
                sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, label, null, null, null, appType, 0, null, rootCauseSubAddString])
            }

            sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, id, null, null, null, null, null, appType,hidden])
            auditLogSave(rootCause)
        } catch (Exception e) {
            log.error("Error occurred in editing late mapping", e)
        } finally {
            sql?.close()
        }
    }

    @ReadOnly(connection = 'pva')
    def editResponsiblePartyMapping(Long id, String label, String appType,boolean hide) {
        checkRcaEntityAlreadyCreated(ResponsibleParty.class, label, appType, id)
        Sql sql
        ResponsibleParty responsibleParty = ResponsibleParty.get(id)
        int hidden = 0
        hidden = hide?2:1
            try {
                sql = new Sql(dataSource_pva)
                if (label != responsibleParty?.textDesc) {
                    sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, (id + ";" + label), null, null, appType, 1, null, null])
                }
                sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, null, id, null, null, null, null, appType,hidden])
                auditLogSave(responsibleParty)
            } catch (Exception e) {
                log.error("Error occurred in creating late mapping", e)
            } finally {
                sql?.close()
            }
    }

    @ReadOnly(connection = 'pva')
    def editRootCauseSubMapping(Long id, String label, String appType,boolean hide) {
        checkRcaEntityAlreadyCreated(RootCauseSubCategory.class, label, appType, id)
        Sql sql
        RootCauseSubCategory rootCauseSubCategory = RootCauseSubCategory.get(id)
        int hidden = 0
        hidden = hide?2:1
            try {
                sql = new Sql(dataSource_pva)
                if (label != rootCauseSubCategory?.textDesc) {
                    sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, appType, 1, null, (id + ";" + label)])
                }
                sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, id, appType,hidden])
                auditLogSave(rootCauseSubCategory)
            } catch (Exception e) {
                log.error("Error occurred in creating root cause subcategory mapping", e)
            } finally {
                sql?.close()
            }
    }

    @ReadOnly(connection = 'pva')
    def editRootCauseClassMapping(Long id, String label, String appType,boolean hide) {
        checkRcaEntityAlreadyCreated(RootCauseClassification.class, label, appType, id)
        Sql sql
        RootCauseClassification rootCauseClassification = RootCauseClassification.get(id)
        int hidden = 0
        hidden = hide?2:1
            try {
                sql = new Sql(dataSource_pva)
                if (label != rootCauseClassification?.textDesc) {
                    sql.call('{call PKG_PVR_PVC_HANDLER.P_ADD_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, null, appType, 1, (id + ";" + label), null])
                }
                sql.call('{call PKG_PVR_PVC_HANDLER.P_HIDE_ROD_CONFIG_VALUES(?,?,?,?,?,?,?,?,?)}', [null, null, null, null, null, id, null, appType,hidden])
                auditLogSave(rootCauseClassification)
            } catch (Exception e) {
                log.error("Error occurred in creating root cause subcategory mapping", e)
            } finally {
                sql?.close()
            }
    }

    @ReadOnly(connection = 'pva')
    String getRootCauseAddString(List<Long> rootCauseIds) {
        StringBuilder rootCauseBuilder = new StringBuilder()
        List<RootCause> rootCauseList = RootCause.findAllByIdInList(rootCauseIds)
        for (int i = 0; i < rootCauseList.size(); i++) {
            if (i == rootCauseList.size() - 1) {
                rootCauseBuilder.append(rootCauseList.get(i).textDesc)
            } else {
                rootCauseBuilder.append(rootCauseList.get(i).textDesc + "#")
            }
        }
        return rootCauseBuilder.toString()
    }

    @ReadOnly(connection = 'pva')
    String getRootCauseClassAddString(Collection<Long> rootCauseClassIds) {
        if (!rootCauseClassIds) return null
        return RootCauseClassification.findAllByIdInList(rootCauseClassIds)?.collect { it.textDesc }?.join("#")
    }

    @ReadOnly(connection = 'pva')
    String getRootCauseSubAddString(Collection<Long> rootCauseSubIds) {
        if (!rootCauseSubIds) return null
        return RootCauseSubCategory.findAllByIdInList(rootCauseSubIds)?.collect { it.textDesc }?.join("#")
    }

    @ReadOnly(connection = 'pva')
    String getReponsiblePartyAddString(List<Long> responsiblePartyIds) {
        List<ResponsibleParty> responsiblePartyList = ResponsibleParty.findAllByIdInList(responsiblePartyIds)
        StringBuilder respPartyBuilder = new StringBuilder()
        for (int i = 0; i < responsiblePartyList.size(); i++) {
            if (i == responsiblePartyList.size() - 1) {
                respPartyBuilder.append(responsiblePartyList.get(i).textDesc)
            } else {
                respPartyBuilder.append(responsiblePartyList.get(i).textDesc + "#")
            }
        }
        return respPartyBuilder.toString()
    }

    @ReadOnly(connection = 'pva')
    String getRootCauseRemoveString(Long lateId, List<Long> rootCauseIds) {
        StringBuilder rootCauseRemoveString = new StringBuilder()
        for (int i = 0; i < rootCauseIds.size(); i++) {
            if (i == rootCauseIds.size() - 1) {
                rootCauseRemoveString.append(lateId + ";" + rootCauseIds.get(i))
            } else {
                rootCauseRemoveString.append(lateId + ";" + rootCauseIds.get(i) + ",")
            }
        }
        return rootCauseRemoveString.toString()
    }

    @ReadOnly(connection = 'pva')
    String getResponsiblePartyRemoveString(Long rootCauseId, List<Long> responsiblePartyIds) {
        StringBuilder respPartyRemoveString = new StringBuilder()
        for (int i = 0; i < responsiblePartyIds.size(); i++) {
            if (i == responsiblePartyIds.size() - 1) {
                respPartyRemoveString.append(rootCauseId + ";" + responsiblePartyIds.get(i))
            } else {
                respPartyRemoveString.append(rootCauseId + ";" + responsiblePartyIds.get(i) + ",")
            }
        }
        return respPartyRemoveString.toString()
    }

    private void checkRcaEntityAlreadyCreated(Class rcaEntityClass, String label, String appType, Long id = null) {
        boolean isRcaEntityAlreadyCreated = false
        switch (rcaEntityClass) {
            case Late.class:
                Late late = Late.findByTextDescAndOwnerApp(label, appType)

                isRcaEntityAlreadyCreated = late != null && (!id || late.id != id)
                break
            case RootCause.class:
                RootCause rootCause = RootCause.findByTextDescAndOwnerApp(label, appType)

                isRcaEntityAlreadyCreated = rootCause != null && (!id || rootCause.id != id)
                break
            case RootCauseClassification.class:
                RootCauseClassification rootCauseClassification = RootCauseClassification.findByTextDescAndOwnerApp(label, appType)

                isRcaEntityAlreadyCreated = rootCauseClassification != null && (!id || rootCauseClassification.id != id)
                break
            case RootCauseSubCategory.class:
                RootCauseSubCategory rootCauseSubCategory = RootCauseSubCategory.findByTextDescAndOwnerApp(label, appType)

                isRcaEntityAlreadyCreated = rootCauseSubCategory != null && (!id || rootCauseSubCategory.id != id)
                break
            case ResponsibleParty.class:
                ResponsibleParty responsibleParty = ResponsibleParty.findByTextDescAndOwnerApp(label, appType)

                isRcaEntityAlreadyCreated = responsibleParty != null && (!id || responsibleParty.id != id)
                break
        }
        if (isRcaEntityAlreadyCreated) {
            throw new EntityExistsException()
        }
    }

    public String getPVCRecipientsByEmailPreference(User user, String mode){
        String recipient = null
        User currentUser = userService.getCurrentUser()
        if(user!=currentUser) {
            if (mode == Constants.ASSIGNED_TO) {
                if (user?.preference?.pvcEmail?.assignedToMe)
                    recipient = user.email
            }
            if (mode == Constants.ASSIGNED_TO_GROUP) {
                if (user?.preference?.pvcEmail?.assignedToMyGroup)
                    recipient = user.email
            }
            if (mode == Constants.WORKFLOW_CHANGES) {
                if (user?.preference?.pvcEmail?.workflowStateChange)
                    recipient = user.email
            }
        }
        return recipient
    }


}
