package com.rxlogix.config.publisher

import com.rxlogix.hibernate.EscapedILikeExpression
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.springframework.web.multipart.MultipartFile
@CollectionSnapshotAudit
class PublisherTemplate {

    static transient List<String> allowedFileTypes = ["docx","doc"]
    static auditable =  [ignore:['template']]
    @AuditEntityIdentifier
    String name
    String description
    String fileName
    boolean isDeleted = false
    byte[] template
    boolean qualityChecked = false

    static hasMany = [parameters: PublisherTemplateParameter]

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table('publisher_tpl')
        parameters cascade: "all-delete-orphan"
        qualityChecked column: "QUALITY_CHECKED"
    }

    static constraints = {
        name(maxSize: 255, validator: { val, obj ->
            if (!obj.id || obj.isDirty("name")) {
                long count = PublisherTemplate.createCriteria().count {
                    ilike('name', "${val}")
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count > 0) {
                    return "unique.per.template"
                }
            }
        })
        description(nullable:true, maxSize: 4000)
        template(nullable: true)
        fileName(maxSize: 255, validator: { val, obj ->
            if(val && !val.toString().trim().isEmpty()) {
                String fileName = val.toLowerCase()
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1)
                if (!allowedFileTypes.contains(ext)) {
                    return "invalid"
                }
            }
        })
    }

    Map toMap() {
        [
                id         : id,
                name       : name,
                description: description,
                qualityChecked:qualityChecked,
                isDeleted  : isDeleted,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
                createdBy  : createdBy,
                modifiedBy : modifiedBy,
        ]
    }

    static namedQueries = {
        publisherTemplateBySearchString { String search ->
            eq('isDeleted', false)
            if (search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('fileName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('createdBy', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('modifiedBy', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
        }

        qualityCheckedWithSearch {String search ->
            eq('isDeleted', false)
            projections {
                distinct('id')
                property("name")
                property("description")
                property("qualityChecked")
            }
            def _search = search
            boolean qc = false
            if (search.toLowerCase().startsWith("qced")) {
                qc = true
                _search = search.substring(4).trim()
            }
            and {
                if (qc) eq('qualityChecked', true)
                if (_search) {
                    or {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(_search)}%")
                        iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(_search)}%")
                    }
                }
            }
            and {
                order('qualityChecked', 'desc')
                order('name', 'asc')
            }
        }
    }
    String toString() {
        return name
    }
}