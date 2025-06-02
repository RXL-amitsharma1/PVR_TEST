package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class CustomSQLQuery extends SuperQuery {
    static auditable = [ignore:["factoryDefault"]]

    String customSQLQuery

    // todo: change to list
    static hasMany = [customSQLValues: CustomSQLValue]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false

        table name: "SQL_QUERY"
        customSQLValues joinTable: [name: "SQL_QRS_SQL_VALUES", column:"SQL_VALUE_ID", key:"SQL_QUERY_ID"], cascade: 'all-delete-orphan'

        customSQLQuery  column: "QUERY", sqlType: DbUtil.longStringType
    }

    static constraints = {
        customSQLQuery(blank: false, validator: { val, obj -> //business validation within preValidateQuery method of QueryCOntroller
            if (val && val.toLowerCase() ==~ Constants.SQL_DML_PATTERN_REGEX) {
                return "com.rxlogix.config.query.customSQLQuery.invalid"
            }
        })
    }

    static namedQueries = {

        getCustomQueriesByUser { User user, Long oldSelectedId, String search ->
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

    @Override
    transient List<String> getFieldsToValidate(){
        return this.getClass().getSuperclass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] } + this.getClass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] }
    }

    static String getSqlQueryToValidate(CustomSQLQuery query){
        String toValidate
        if (Holders.config.getProperty('source.profile.lam.irt.enabled', Boolean)) {
            toValidate = Constants.MULTIPLE_DATASOURCE_QUERY + "${query.customSQLQuery} and 1=2"
        } else {
            toValidate = Constants.SINGLE_DATASOURCE_QUERY + "${query.customSQLQuery} and 1=2"
        }
        return toValidate
    }


    Integer getParameterSize() {
        return customSQLValues?.size() ?: 0
    }

    @Override
    public String toString() {
        super.toString()
    }
}
