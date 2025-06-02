package com.rxlogix.config

import com.rxlogix.QueryRCAValueList
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import com.rxlogix.util.DbUtil
@DirtyCheck
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['autoReasonOfDelay'])
class QueryRCA {
    static auditable =  true
    SuperQuery query
    List<QueryRCAValueList> queryValueLists
    DateRangeInformationQueryRCA dateRangeInformationForQueryRCA = new DateRangeInformationQueryRCA()
    Long lateId
    Long rootCauseId
    String rcCustomExpression
    Long rootCauseClassId
    String rcClassCustomExp
    Long rootCauseSubCategoryId
    String rcSubCatCustomExp
    Long responsiblePartyId
    String rpCustomExpression
    User assignedToUser
    UserGroup assignedToUserGroup
    boolean sameAsRespParty = false
    String summary
    String actions
    String investigation
    String summarySql
    String actionsSql
    String investigationSql

    boolean dynamicFormEntryDeleted

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

//    static propertiesToUseWhileCopying = ['query', 'lateId', 'rootCauseId', 'responsiblePartyId', 'assignedToUser', 'assignedToUserGroup']

    static transients = ['dynamicFormEntryDeleted']

    static belongsTo = [autoReasonOfDelay: AutoReasonOfDelay]
    static hasMany = [queryValueLists: QueryValueList, assignedToUser: User, assignedToUserGroup: UserGroup]
    static hasOne = [dateRangeInformationForQueryRCA: DateRangeInformationQueryRCA]

    static mapping = {
        table name: "QUERY_RCA"
        tablePerHierarchy false

        query column: "SUPER_QUERY_ID", cascade: "none"
        queryValueLists joinTable: [name: "QRS_RCA_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "QUERY_RCA_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        lateId column: "LATE_ID"
        rootCauseId column: 'ROOT_CAUSE_ID'
        rcCustomExpression column: 'RC_CUSTOM_EXPRESSION', sqlType: DbUtil.longStringType
        rootCauseClassId column: 'RC_CLASSIFICATION_ID'
        rcClassCustomExp column: 'RC_CLASS_CUSTOM_EXP', sqlType: DbUtil.longStringType
        rootCauseSubCategoryId column: 'RC_SUB_CATEGORY_ID'
        rcSubCatCustomExp column: 'RC_SUB_CAT_CUSTOM_EXP', sqlType: DbUtil.longStringType
        responsiblePartyId column: 'RESPONSIBLE_PARTY_ID'
        rpCustomExpression column: 'RP_CUSTOM_EXPRESSION', sqlType: DbUtil.longStringType
        assignedToUser column: "ASSIGNED_TO_USER"
        assignedToUserGroup column: "ASSIGNED_TO_USER_GROUP"
        autoReasonOfDelay column: "AUTO_REASON_OF_DELAY_ID",  cascade: "none"
        sameAsRespParty column: "SAME_AS_RESP_PARTY"
        summary column: "SUMMARY"
        investigation column: "INVESTIGATION"
        actions column: "ACTIONS"
        summarySql column: "SUMMARY_SQL"
        actionsSql column: "ACTIONS_SQL"
        investigationSql column: "INVESTIGATION_SQL"
    }

    static constraints = {
        query(nullable: false)
        dateRangeInformationForQueryRCA(nullable: false)
        queryValueLists(cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists?.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.QueryRCA.parameterValues.valueless"
            }
            return hasValues
        })
        lateId(nullable: true)
        rootCauseId(nullable: true)
        rcCustomExpression(nullable: true, validator: { val ->
            if(val && val.length() > 32000){
                return "com.rxlogix.config.QueryRCA.rcCustomExpression.maxSize";
            }
        })
        rootCauseClassId(nullable: true)
        rcClassCustomExp(nullable: true, validator: { val ->
            if(val && val.length() > 32000){
                return "com.rxlogix.config.QueryRCA.rcClassCustomExp.maxSize";
            }
        })
        rootCauseSubCategoryId(nullable: true)
        rcSubCatCustomExp(nullable: true, validator: { val ->
            if(val && val.length() > 32000){
                return "com.rxlogix.config.QueryRCA.rcSubCatCustomExp.maxSize";;
            }
        })
        responsiblePartyId(nullable: true)
        rpCustomExpression(nullable: true, validator: { val ->
            if(val && val.length() > 32000){
                return "com.rxlogix.config.QueryRCA.rpCustomExpression.maxSize";
            }
        })
        assignedToUser(nullable: true)
        assignedToUserGroup(nullable: true)
        dynamicFormEntryDeleted(bindable: true)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        summary(nullable: true, maxSize: 32000)
        investigation(nullable: true, maxSize: 32000)
        actions(nullable: true, maxSize: 32000)
        summarySql(nullable: true, maxSize: 32000)
        actionsSql(nullable: true, maxSize: 32000)
        investigationSql(nullable: true, maxSize: 32000)

    }

    AutoReasonOfDelay getUsedConfiguration() {
        return GrailsHibernateUtil.unwrapIfProxy(autoReasonOfDelay)
    }

    SuperQuery getUsedQuery() {
//        Added to JavaAssist Proxy Object cast exception http://stackoverflow.com/questions/5622481/removing-proxy-part-of-grails-domain-object
        return GrailsHibernateUtil.unwrapIfProxy(query)
    }

    List<QueryValueList> getUsesQueryValueLists() {
        return queryValueLists
    }

    String assignedToName() {
        return (assignedToUser ? (assignedToUser?.fullName ?: assignedToUser?.username) : assignedToUserGroup?.name)
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && (oldValues?.keySet()?.contains("queryValueLists") || oldValues?.keySet()?.contains("dateRangeInformationForQueryRCA"))) {
            withNewSession {
                QueryRCA tq = QueryRCA.read(id);
                if (oldValues?.keySet()?.contains("queryValueLists")) oldValues.put("queryValueLists", tq.queryValueLists?.toString())
                if (oldValues?.keySet()?.contains("dateRangeInformationForQueryRCA")) oldValues.put("dateRangeInformationForQueryRCA", tq.dateRangeInformationForQueryRCA)
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }
}