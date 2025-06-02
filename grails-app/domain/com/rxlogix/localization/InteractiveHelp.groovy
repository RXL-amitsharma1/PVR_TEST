package com.rxlogix.localization


import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil

class InteractiveHelp {
    String title
    String page
    String description
    boolean published = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    boolean isDeleted = false

    transient def auditLogService

    static mapping = {
        table name: "INTERACTIVE_HELP"
        title column: "TITLE"
        page column: "PAGE"
        description column: "DESCRIPTION", sqlType: DbUtil.longStringType
        isDeleted column: "IS_DELETED"
        published column: "PUBLISHED"
    }

    static constraints = {

        title maxSize: 4000
        page maxSize: 4000
        description nullable: true
    }

    public String toString() {
        return title
    }

    List detectChangesForAuditLog(theInstance, Map params, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = auditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        changesMade.flatten()
    }

    String getInstanceIdentifierForAuditLog() {
        return "${title}"
    }

    static namedQueries = {
        fetchByString { String search ->
            if (search) {
                or {
                    iLikeWithEscape('title', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('page', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq("isDeleted", false)
        }
        fetchByPage { String search ->
            if (search) {
                or {
                    iLikeWithEscape('page', "%${EscapedILikeExpression.escapeString(search) + ";"}%")
                    iLikeWithEscape('page', "%${EscapedILikeExpression.escapeString(search)}")
                }
            }
            eq("isDeleted", false)
            eq("published", true)
        }
    }
}
