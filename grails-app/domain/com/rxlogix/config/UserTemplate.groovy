package com.rxlogix.config

import com.rxlogix.user.User
import org.apache.commons.lang.builder.HashCodeBuilder

class UserTemplate implements Serializable {

    private static final long serialVersionUID = 1

    User user

    boolean equals(other) {
        if (!(other instanceof UserTemplate)) {
            return false
        }

        other.user?.id == user?.id &&
                other.reportTemplate?.id == reportTemplate?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (user) builder.append(user.id)
        if (reportTemplate) builder.append(reportTemplate.id)
        builder.toHashCode()
    }

    static UserTemplate get(long userId, long reportTemplateId) {
        UserTemplate.where {
            user == User.load(userId) &&
                    reportTemplate == ReportTemplate.load(reportTemplateId)
        }.get()
    }

    static boolean exists(long userId, long reportTemplateId) {
        UserTemplate.where {
            user == User.load(userId) &&
                    reportTemplate == ReportTemplate.load(reportTemplateId)
        }.count() > 0
    }

    static UserTemplate create(User user, ReportTemplate reportTemplate, boolean flush = false) {
        def instance = new UserTemplate(user: user, reportTemplate: reportTemplate)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(User u, ReportTemplate r, boolean flush = false) {
        if (u == null || r == null) return false

        int rowCount = UserTemplate.where {
            user == User.load(u.id) &&
                    reportTemplate == ReportTemplate.load(r.id)
        }.deleteAll()

        if (flush) {
            UserTemplate.withSession { it.flush() }
        }

        rowCount > 0
    }

    static void removeAll(User u, boolean flush = false) {
        if (u == null) return

        UserTemplate.where {
            user == User.load(u.id)
        }.deleteAll()

        if (flush) {
            UserTemplate.withSession { it.flush() }
        }
    }

    static void removeAll(ReportTemplate r, boolean flush = false) {
        if (r == null) return

        UserTemplate.where {
            reportTemplate == ReportTemplate.load(r.id)
        }.deleteAll()

        if (flush) {
            UserTemplate.withSession { it.flush() }
        }
    }

    static belongsTo = [reportTemplate: ReportTemplate]

    static constraints = {
        reportTemplate validator: { ReportTemplate r, UserTemplate ut ->
            if (ut.user == null) return
            boolean existing = false
            UserTemplate.withNewSession {
                existing = UserTemplate.exists(ut.user.id, r.id)
            }
            if (existing) {
                return ["userTemplate.exists","${ut.user.fullNameAndUserName}"]
            }
        }
    }

    static mapping = {
        id composite: ['reportTemplate', 'user']
        version false
        table name: "RPT_TEMPLATE_USER"
        user cascade: 'none'
        reportTemplate cascade: 'none'
    }

    public String toString() {
        return user.getFullNameAndUserName()
    }
}
