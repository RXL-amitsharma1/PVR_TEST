package com.rxlogix.localization


import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class ReleaseNotesItem {
    static auditable =  true
    @AuditEntityIdentifier
    String title
    String description
    Boolean hasDescription
    String summary
    String shortDescription
    Boolean invisible = false
    Integer sortNumber

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    Boolean isDeleted = false

    static belongsTo = [releaseNotes: ReleaseNotes]

    static mapping = {
        table name: "RELEASE_NOTES_ITEM"
        releaseNotes column: "RELEASE_NOTE_ID"
        description column: "DESCRIPTION", sqlType: DbUtil.longStringType, lazy: true
        title column: "TITLE"
        hasDescription column: "HAS_DESCRIPTION"
        summary column: "SUMMARY"
        shortDescription column: "SHORT_DESCRIPTION"
        invisible column: "HIDDEN"
        sortNumber column: "SORT_NUMBER"
    }
    static constraints = {

        title maxSize: 4000, nullable: true
        description nullable: true
        summary nullable: true
        shortDescription nullable: true
        invisible nullable: true
        sortNumber nullable: true
    }

    static namedQueries = {
        fetchByString { String search ->
            if (search) {
                createAlias('releaseNotes', 'rn', CriteriaSpecification.LEFT_JOIN)
                or {
                    iLikeWithEscape('releaseNumber', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('rn.title', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq("isDeleted", false)
        }
    }
}
