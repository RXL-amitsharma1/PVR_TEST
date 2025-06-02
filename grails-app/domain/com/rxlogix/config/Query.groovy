package com.rxlogix.config

import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class Query extends SuperQuery implements Comparable {
    static auditable = true

    ReassessListednessEnum reassessListedness
    Date reassessListednessDate
    boolean reassessForProduct
    List<QueryExpressionValue> queryExpressionValues

    static hasMany = [queryExpressionValues: QueryExpressionValue]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false

        table name: "QUERY"
        queryExpressionValues joinTable:[name: "QUERIES_QRS_EXP_VALUES", column: "QUERY_EXP_VALUE_ID", key:"QUERY_ID"], cascade: 'all-delete-orphan'
        reassessListednessDate column: "REASSESS_LISTEDNESS_DATE"
    }

    static constraints = {
        reassessListedness(nullable: true)
        reassessListednessDate(nullable: true)
    }

    static namedQueries = {

        getQueriesByUser { User user, Long oldSelectedId, String search ->
            projections {
                distinct('id')
                property("name")
            }
            createAlias('userQueries', 'userQuery', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupQueries', 'userGroupQuery', CriteriaSpecification.LEFT_JOIN)
            eq("originalQueryId", 0L)
            or {
                if (oldSelectedId) {
                    and {
                        eq("id", oldSelectedId)
                        if (search) {
                            or {
                                iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                                iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                                'owner' {
                                    iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                                }
                            }
                        }
                    }
                }

                and {
                    eq("isDeleted", false)
                    if (!user.isAdmin()) {
                        or {
                            eq('owner.id', user.id)
                            'in'('userQuery.user', user)
                            if (UserGroup.countAllUserGroupByUser(user)) {
                                'in'('userGroupQuery.userGroup', UserGroup.fetchAllUserGroupByUser(user))
                            }
                        }
                    }
                    if (search) {
                        or {
                            iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                            iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                            'owner' {
                                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                            }
                        }
                    }
                }
            }
        }

    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            newValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog(queryExpressionValues, JSONQuery, 0))
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("JSONQuery")) {
            newValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog(queryExpressionValues, JSONQuery, 0))
            oldValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog(this.getPersistentValue("queryExpressionValues"), this.getPersistentValue("JSONQuery"), 0))
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    Integer getParameterSize() {
        return queryExpressionValues?.size()
    }

    @Override
    public String toString() {
        super.toString()
    }

}
