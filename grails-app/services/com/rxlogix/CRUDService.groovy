package com.rxlogix

import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import grails.gorm.transactions.Transactional
import grails.plugins.orm.auditable.AuditLogListenerThreadLocal
import grails.validation.ValidationException
import org.springframework.dao.DataIntegrityViolationException


@Transactional
class CRUDService {

    def userService

    def save(theInstance, Map saveParams = null) {
        userService.setOwnershipAndModifier(theInstance)
        enableAuditLog(theInstance, {
            if (!theInstance.save(saveParams ?: [:])) {
                log.warn(theInstance.errors?.toString())
                throw new ValidationException("Validation Exception", theInstance.errors)
            }
        })
        return theInstance
    }

    def update(theInstance, Map saveParams = null) {
        userService.setOwnershipAndModifier(theInstance)
        enableAuditLog(theInstance, {
            if (!theInstance.save(saveParams ?: [:])) {
                log.warn(theInstance.errors?.toString())
                throw new ValidationException("Validation Exception", theInstance.errors)
            }
        })
        return theInstance
    }

    def updateWithMandatoryAuditlog(theInstance, Map saveParams = null) {
        theInstance.markDirty()
        theInstance = userService.setOwnershipAndModifier(theInstance)
        enableMandatoryAuditLog(theInstance, {
            enableAuditLog(theInstance, {
                if (!theInstance.save(saveParams ?: [:])) {
                    log.warn(theInstance.errors?.toString())
                    throw new ValidationException("Validation Exception", theInstance.errors)
                }
            })
        })
        return theInstance
    }

    def saveOrUpdate(theInstance, Map saveParams = null) {
        if (theInstance.id) {
            return update(theInstance, saveParams)
        } else {
            return save(theInstance, saveParams)
        }
    }


    void delete(theInstance) {
        try {
            //Must flush session to force constraint violations, if any, and then respond with appropriate validation message
            theInstance.delete(flush: true)
        } catch (DataIntegrityViolationException e) {
            log.warn(theInstance.errors?.toString())
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }

    void deleteWithAuditLog(theInstance, String name) {
        try {
            theInstance.delete(flush: true)
        } catch (DataIntegrityViolationException e) {
            log.warn(theInstance.errors?.toString())
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }

    def softDelete(theInstance, name, String justification = null, Map saveParams = null) {
        userService.setOwnershipAndModifier(theInstance)
        theInstance.isDeleted = true
        if (justification) {
            if (!theInstance.hasProperty("deleteJustification")) {
                theInstance.metaClass.deleteJustification = justification
            } else {
                theInstance.deleteJustification = justification
            }
        }
        enableAuditLog(theInstance, {
            if (!theInstance.save(saveParams ?: [:])) {
                log.warn(theInstance.errors?.toString())
                throw new ValidationException("Validation Exception", theInstance.errors)
            }
        })

        return theInstance
    }

    def softDeleteForUser(User user, theInstance, name, String justification = null) {
        theInstance.deleteForUser(user)
        AuditLogConfigUtil.logChanges(theInstance, [deleted: "Deleted for user ${user.fullName} (Justification: ${justification})"], [deleted: "-"], Constants.AUDIT_LOG_UPDATE)
        return theInstance
    }

    def saveWithoutAuditLog(theInstance) {
        return updateWithoutAuditLog(theInstance)
    }

    def updateWithoutAuditLog(theInstance) {
        userService.setOwnershipAndModifier(theInstance)
        disableAuditLog {
            if (!theInstance.save()) {
                log.warn(theInstance.errors?.toString())
                throw new ValidationException("Validation Exception", theInstance.errors)
            }
        }
        return theInstance
    }

    def instantSaveWithoutAuditLog(theInstance) {
        userService.setOwnershipAndModifier(theInstance)
        disableAuditLog {
            theInstance.save(failOnError: true, flush: true)
        }
        return theInstance
    }

    def instantUpdateWithoutAuditLog(theInstance) {
        instantSaveWithoutAuditLog(theInstance)
    }

    void disableAuditLog(Closure closure) {
        boolean oldStatus = AuditLogListenerThreadLocal.getAuditLogDisabled()
        try {
            AuditLogListenerThreadLocal.setAuditLogDisabled(true)
            closure.call()
        } finally {
            AuditLogListenerThreadLocal.setAuditLogDisabled(oldStatus)
        }
    }

    //TODO in future we can remove the same as by default all auditing is enabled
    private void enableAuditLog(theInstance, Closure closure) {
        boolean oldStatus = false
        try {
            if (theInstance.hasProperty("skipAudit")) {
                oldStatus = theInstance.skipAudit
                theInstance.skipAudit = false
            }
            closure.call()
        } finally {
            if (theInstance.hasProperty("skipAudit")) {
                theInstance.skipAudit = oldStatus
            }
        }
    }

    private void enableMandatoryAuditLog(theInstance, Closure closure) {
        boolean oldStatus = false
        try {
            if (theInstance.hasProperty("mandatoryAudit")) {
                oldStatus = theInstance.mandatoryAudit
                theInstance.mandatoryAudit = true
            }
            closure.call()
        } finally {
            if (theInstance.hasProperty("mandatoryAudit")) {
                theInstance.mandatoryAudit = oldStatus
            }
        }
    }

}
