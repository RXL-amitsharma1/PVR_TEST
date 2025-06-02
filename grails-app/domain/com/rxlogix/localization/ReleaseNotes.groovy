package com.rxlogix.localization


import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class ReleaseNotes {
    static auditable =  true
    @AuditEntityIdentifier
    String releaseNumber
    String title
    String description

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    boolean isDeleted = false

    static hasMany = [notes: ReleaseNotesItem]
    static mapping = {
        table name: "RELEASE_NOTES"
        releaseNumber column: "RELEASE_NUMBER"
        title column: "TITLE"
        description column: "DESCRIPTION", sqlType: DbUtil.longStringType
        isDeleted column: "IS_DELETED"
    }

    static constraints = {

        title maxSize: 4000, nullable: true
        description nullable: true
        notes nullable: true
    }

    public String toString() {
        return releaseNumber
    }

    static namedQueries = {
        fetchByString { String search ->
            if (search) {
                or {
                    iLikeWithEscape('releaseNumber', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('title', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq("isDeleted", false)
        }
    }

    static ReleaseNotes getLast() {
        List<ReleaseNotes> list = getAllSorted()
        return list?.size() > 0 ? list[0] : null
    }

    static List<ReleaseNotes> getAllSorted() {
        List<ReleaseNotes> list = findAllByIsDeleted(false)
        list = list?.sort { ReleaseNotes a, ReleaseNotes b ->
            List<String> arrA = a.releaseNumber.trim().split("\\.")
            List<String> arrB = b.releaseNumber.trim().split("\\.")
            return compareList(arrA, arrB)
        }
        list ?: []
    }


    private static int compareList(List<String> arrA, List<String> arrB) {
        for (int i = 0; i < Math.max(arrA.size(), arrB.size()); i++) {
            if ((arrA.size() > i) && (arrB.size() > i) && (arrA[i] != arrB[i])) return Integer.parseInt(arrB[i]).compareTo(Integer.parseInt(arrA[i]))
            if ((arrA.size() > i) && (arrB.size() <= i)) return -1
            if ((arrB.size() > i) && (arrA.size() <= i)) return 1
        }
        return 0
    }
}
