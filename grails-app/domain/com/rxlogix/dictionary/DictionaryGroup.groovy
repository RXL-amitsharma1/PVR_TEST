package com.rxlogix.dictionary

import com.rxlogix.Constants
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.grails.web.json.JSONObject
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
@CollectionSnapshotAudit
class DictionaryGroup {
    static auditable =  [ignore: ["tenantId"]]
    @AuditEntityIdentifier
    String groupName
    Integer type
    String description
    User owner
    Boolean isDeleted = false
    Boolean isMultiIngredient = false
    Boolean includeWHODrugs = false

    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    Integer tenantId

    static hasMany = [dataSources: String, sharedWithUser: User, sharedWithGroup: UserGroup]

    static fetchMode = [dataSources: 'eager', owner: 'eager']

    static mapping = {
        table "DICTIONARY_GROUP"
        groupName column: "GROUP_NAME"
        type column: "GROUP_TYPE"
        description column: "DESCRIPTION"
        owner column: "PVUSER_ID"
        isDeleted column: "IS_DELETED"
        tenantId column: 'TENANT_ID'
        sharedWithUser joinTable: [name: "DICT_GRP_SHARED_WITHS", column: "SHARED_WITH_ID", key: "DICTIONARY_GROUP_ID"]
        sharedWithGroup joinTable: [name: "DICT_GRP_SHARED_WITH_GRPS", column: "SHARED_WITH_GROUP_ID", key: "DICTIONARY_GROUP_ID"]
        dataSources joinTable: [name: "DICT_GRP_DATA_SRC", column: "DATA_SRC_NAME", key: "DICTIONARY_GROUP_ID"]
        isMultiIngredient column: "IS_MULTI_INGREDIENT"
        includeWHODrugs column: "INCLUDE_WHO_DRUGS"
    }

    static constraints = {
        groupName(nullable: false, blank: false, maxSize: 255, validator: { val, obj ->
            //Name and type is unique to user
            if (!obj.id || obj.isDirty("groupName") || obj.isDirty("owner")) {
                long count = DictionaryGroup.createCriteria().count {
                    ilike('groupName', "${val}")
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    eq('type', obj.type)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "unique.per.user"
                }
            }
        })

        description nullable: true, maxSize: 8000

    }

    static namedQueries = {
        sharedWithUser { User user ->
            if (!user?.isAnyAdmin()) {
                createAlias('sharedWithUser', 'sw', CriteriaSpecification.LEFT_JOIN)
                createAlias('sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)
                or {
                    eq('owner.id', user.id)
                    'in'('sw.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(user).id)
                    }
                }
            }
        }

        getAllRecordsBySearch { Integer dicType, String term, String dataSource, User user, Integer tenantId,Boolean exactSearch, Boolean isMultiIngredient ->
            eq('isDeleted', false)
            eq('tenantId', tenantId)
            eq('type', dicType)
            sharedWithUser(user)
            if(exactSearch){
                eq('groupName',term.trim(),[ignoreCase:true])
            }
            else{
                iLikeWithEscape('groupName', "%${EscapedILikeExpression.escapeString(term)}%")
            }
             if (dataSource) {
                createAlias("dataSources", "dataSources", JoinType.LEFT_OUTER_JOIN)
                'in'("dataSources.elements", dataSource.split(',').toList())
            }
            if (dicType == 1){
                eq('isMultiIngredient',isMultiIngredient)
            }

        }

        getAllIdsBySearch { Integer dicType, String term, String dataSource, User user, Integer tenantId,Boolean exactSearch, Boolean isMultiIngredient ->
            projections {
                distinct('id')
            }
            getAllRecordsBySearch(dicType, term, dataSource, user, tenantId,exactSearch, isMultiIngredient)
        }

        countRecordsBySearch { Integer dicType, String term, String dataSource, User user, Integer tenantId, Boolean exactSearch, Boolean isMultiIngredient->
            projections {
                countDistinct("id")
            }
            getAllRecordsBySearch(dicType, term, dataSource, user, tenantId,exactSearch, isMultiIngredient)
        }
    }

    transient String getOwnerFullName() {
        return owner.fullName
    }

    String fetchData() {
        if (!dataSources) {
            return ''
        }
        JSONObject data = new JSONObject()
        dataSources.sort().each { dsn ->
            if (Holders.config.get("dataSources.$dsn")) {
                DictionaryGroupData."$dsn".withNewSession {
                    DictionaryGroupData obj = DictionaryGroupData."$dsn".get(id)
                    if(obj){
                        data.put(dsn, JSON.parse(obj.data?:''))
                    }

                }
            } else {
                log.error("Issue in DBs configuartion for $dsn in $id")
            }
        }
        return data.toString()
    }

    String fetchSharedWith() {
        (sharedWithGroup.collect { "$Constants.USER_TOKEN$it" } + sharedWithGroup.collect {
            "$Constants.USER_GROUP_TOKEN$it"
        }).join(';')
    }

    List<Map> fetchSharedWithUserData() {
        return sharedWithUser.collect {
            [id: "${Constants.USER_TOKEN}${it.id}", text: it.fullName]
        }
    }

    List<Map> fetchSharedWithGroupData() {
        return sharedWithGroup.collect {
            [id: "${Constants.USER_GROUP_TOKEN}${it.id}", text: it.name]
        }
    }

    transient boolean isEventGroup() {
        return (type == 2)
    }

    private getSharedWithUserChanges(theInstance, Map params) {
        String oldSharedWithUser = (params?.oldSharedWithUser) ? params.oldSharedWithUser?.toString() : null
        String newSharedWithUser = theInstance?.sharedWithUser ? theInstance?.sharedWithUser?.toString() : null

        if (params && oldSharedWithUser != newSharedWithUser) {
            def value = [entityName   : ViewHelper.getMessage("auditLog.domainObject.DictionaryGroup"),
                         entityId     : theInstance?.id ?: '',
                         fieldName    : 'sharedWithUser',
                         originalValue: oldSharedWithUser ?: '(None)',
                         newValue     : newSharedWithUser ?: '(None)']

            return value
        }
        return null
    }

    private getSharedWithGroupChanges(theInstance, Map params) {
        String oldSharedWithGroup = (params?.oldSharedWithGroup) ? params.oldSharedWithGroup?.toString() : null
        String newSharedWithGroup = theInstance?.sharedWithGroup ? theInstance?.sharedWithGroup?.toString() : null

        if (params && oldSharedWithGroup != newSharedWithGroup) {
            def value = [entityName   : ViewHelper.getMessage("auditLog.domainObject.DictionaryGroup"),
                         entityId     : theInstance?.id ?: '',
                         fieldName    : 'sharedWithGroup',
                         originalValue: oldSharedWithGroup ?: '(None)',
                         newValue     : newSharedWithGroup ?: '(None)']

            return value
        }
        return null
    }

    public String toString() {
        return "$groupTypeName Group - $groupName"
    }

    public getGroupTypeName() {
        if (type == 1) {
            return 'Product'
        }
        if (type == 2) {
            return 'Event'
        }
        return type
    }

}
