package com.rxlogix.util

import com.rxlogix.Constants
import grails.converters.JSON
import grails.plugins.orm.auditable.RxAuditLogListener
import grails.util.Holders

/**
 * AuditLog util class
 * Provides util methods for AuditLog plugin
 */
class AuditLogConfigUtil {
    static String scheduleDateJsonView(def value) {
        if (value == null) return "N/A"
        value = value.trim().replaceAll("\\n", "")

        String alternativeValue = value
        try {
            JSON jsonConverter = new JSON()
            def jsonObject = jsonConverter.parse(value)
            if (!jsonObject) return value
            String startDateTime = "Start Date and Time: " + MiscUtil.getReadableStartDateTime(jsonObject.startDateTime)
            String timeZone = "Time Zone: " + MiscUtil.getReadableTimeZone(jsonObject.timeZone)
            String recurrencePattern = "Recurrence Pattern: " + MiscUtil.getReadableRecurrencePattern(jsonObject.recurrencePattern)
            alternativeValue = "$startDateTime \n $timeZone \n $recurrencePattern"
        }
        catch (Exception e) {
           //just return initial value
            return value
        }
        return alternativeValue
    }

    /**
     * Renders map as html text with bold key : value , new line
     *
     * @param map to render
     * @return html string
     */
    static String convertMapToString(Map map) {
        String result = Constants.BLANK_STRING
        map.each {
            result += "\n ${it.getKey()} = ${it.getValue()}"
        }

        result
    }

    private static RxAuditLogListener getAuditListenerBean() {
        //Extracting listener class bean from the list of application event multicaster. TODO need to find better way.
        return Holders.applicationContext.applicationEventMulticaster.applicationListeners.find {
            it instanceof RxAuditLogListener
        }
    }

    static void logChanges(domain, Map newMap, Map oldMap, String eventName, String extraValue = "", String transactionId = ("" + System.currentTimeMillis()), String username = null, String fullname = null) {
        String className = domain.getClass().getSimpleName()
        String domainId = "" + domain.id
        if(domain instanceof ArrayList) {
            domainId = domain[0].id
            className = domain[0].getClass().getSimpleName()
            if(domain[0] instanceof LinkedHashMap)
                className = "actionPlan"
        }
        if(className.charAt(0).isUpperCase()){
            className = className.substring(0, 1).toLowerCase() + className.substring(1)
        }
        auditListenerBean.logChanges(domain, newMap, oldMap, domainId, eventName, className, transactionId, extraValue, username, fullname)
    }
}