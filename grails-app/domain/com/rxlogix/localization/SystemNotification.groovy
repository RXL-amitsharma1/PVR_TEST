package com.rxlogix.localization


import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DbUtil

class SystemNotification {
    String title
    String description
    String details
    boolean published = false

    static hasMany = [userGroups: UserGroup]

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    boolean isDeleted = false

    transient def auditLogService

    static mapping = {
        table name: "SYSTEM_NOTES"
        title column: "TITLE"
        description column: "DESCRIPTION", sqlType: DbUtil.longStringType
        details column: "DETAILS", sqlType: DbUtil.longStringType
        isDeleted column: "IS_DELETED"
        published column: "PUBLISHED"
        userGroups joinTable: [name: "SYSTEM_NOTES_USER_GROUPS", column: "USER_GROUP_ID", key: "SYS_NOTE_ID"], indexColumn: [name:"USER_GROUP_IDX"]
    }

    static constraints = {

        title maxSize: 4000, nullable: true
        description nullable: true
        details nullable: true
        userGroups nullable: true
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
                iLikeWithEscape('title', "%${EscapedILikeExpression.escapeString(search)}%")
            }
            eq("isDeleted", false)
        }
    }

    static List<SystemNotification> fetchNew(User user) {
        List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(user) ?: []
        List<SystemNotification> list = findAll("from SystemNotification n where n.published=true and n.id not in (select un.systemNotification.id from SystemNotificationNotifier un where un.user=:user)", [user: user], [sort: 'id', order: "desc"])
        list.findAll {
            if (!it.userGroups) return true
            return it.userGroups.find { it in userGroups }
        }
    }

}
