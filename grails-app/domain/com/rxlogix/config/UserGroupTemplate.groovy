package com.rxlogix.config

import com.rxlogix.user.UserGroup
import org.apache.commons.lang.builder.HashCodeBuilder

class UserGroupTemplate implements Serializable {

    private static final long serialVersionUID = 1

    UserGroup userGroup

    boolean equals(other) {
        if (!(other instanceof UserGroupTemplate)) {
            return false
        }

        other.userGroup?.id == userGroup?.id &&
                other.reportTemplate?.id == reportTemplate?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (userGroup) builder.append(userGroup.id)
        if (reportTemplate) builder.append(reportTemplate.id)
        builder.toHashCode()
    }

    static UserGroupTemplate get(long userGroupId, long reportTemplateId) {
        UserGroupTemplate.where {
            userGroup == UserGroup.load(userGroupId) &&
                    reportTemplate == ReportTemplate.load(reportTemplateId)
        }.get()
    }

    static boolean exists(long userGroupId, long reportTemplateId) {
        UserGroupTemplate.where {
            userGroup == UserGroup.load(userGroupId) &&
                    reportTemplate == ReportTemplate.load(reportTemplateId)
        }.count() > 0
    }

    static UserGroupTemplate create(UserGroup userGroup, ReportTemplate reportTemplate, boolean flush = false) {
        def instance = new UserGroupTemplate(userGroup: userGroup, reportTemplate: reportTemplate)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(UserGroup u, ReportTemplate r, boolean flush = false) {
        if (u == null || r == null) return false

        int rowCount = UserGroupTemplate.where {
            userGroup == UserGroup.load(u.id) &&
                    reportTemplate == ReportTemplate.load(r.id)
        }.deleteAll()

        if (flush) {
            UserGroupTemplate.withSession { it.flush() }
        }

        rowCount > 0
    }



    static void removeAll(UserGroup u, boolean flush = false) {
        if (u == null) return

        UserGroupTemplate.where {
            userGroup == UserGroup.load(u.id)
        }.deleteAll()

        if (flush) {
            UserGroupTemplate.withSession { it.flush() }
        }
    }

    static void removeAll(ReportTemplate r, boolean flush = false) {
        if (r == null) return

        UserGroupTemplate.where {
            reportTemplate == ReportTemplate.load(r.id)
        }.deleteAll()

        if (flush) {
            UserGroupTemplate.withSession { it.flush() }
        }
    }

    static belongsTo = [reportTemplate: ReportTemplate]

    static constraints = {
        reportTemplate validator: { ReportTemplate r, UserGroupTemplate ugt ->
            if (ugt.userGroup == null) return
            boolean existing = false
            UserGroupTemplate.withNewSession {
                existing = UserGroupTemplate.exists(ugt.userGroup.id, r.id)
            }
            if (existing) {
                return ["userGroupTemplate.exists", "${ugt.userGroup.name}"]
            }
        }
    }

    static mapping = {
        id composite: ['reportTemplate', 'userGroup']
        version false
        table name: "RPT_TEMPLATE_USER_GROUP"
        userGroup cascade:'none'
        reportTemplate cascade:'none'
    }

    public String toString() {
        return userGroup.name
    }
}
