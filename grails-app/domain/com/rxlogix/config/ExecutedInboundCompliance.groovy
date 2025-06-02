package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.hibernate.criterion.CriteriaSpecification

class ExecutedInboundCompliance extends BaseInboundCompliance {

    ExecutedGlobalDateRangeInbound executedGlobalDateRangeInbound

    List<ExecutedQueryCompliance> executedQueriesCompliance = []

    boolean isTemplate = false
    boolean isICInitialize = false
    ReportExecutionStatusEnum status = ReportExecutionStatusEnum.ERROR
    InboundCompliance inboundCompliance
    Long startTime = 0L
    Long endTime = 0L
    Long runDuration
    String message
    String errorDetails

    static hasMany = [executedQueriesCompliance: ExecutedQueryCompliance]
    static mappedBy = [executedQueriesCompliance: 'executedInboundCompliance']

    static mapping = {
        table name: "EX_INBOUND_COMPLIANCE"
        executedGlobalDateRangeInbound column: "EX_GLOBAL_DATA_RANGE_ID"
        executedQueriesCompliance joinTable: [name: "EX_QUERY_COMPLIANCE", column: "ID", key: "EX_QUERY_COMPLIANCE_ID"], indexColumn: [name: "EX_QUERY_COMPLIANCE_IDX"], cascade: 'all-delete-orphan'
        tags joinTable: [name: "EX_INBOUND_COMPLIANCE_TAGS", column: "TAG_ID", key: "EX_INBOUND_COMPLIANCE_ID"], indexColumn: [name: "TAG_IDX"]
        poiInputsParameterValues joinTable: [name: "EX_INBOUND_POI_PARAMS", column: "PARAM_ID", key: "EX_INBOUND_COMPLIANCE_ID"]
        status column: 'EX_STATUS'
        isTemplate column: "IS_TEMPLATE"
        isICInitialize column: "IS_IC_INITIALIZE"
        inboundCompliance column: "INBOUND_COMPLIANCE_ID"
        startTime column: "START_TIME"
        endTime column: "END_TIME"
        runDuration formula: '(END_TIME - START_TIME)'
        message column: "MESSAGE", sqlType: DbUtil.longStringType
        errorDetails column: "ERROR_DETAILS", sqlType: DbUtil.longStringType
    }

    ExecutedInboundCompliance(InboundCompliance inboundCompliance) {
        setProperties(inboundCompliance.properties)
    }

    static constraints = {
        senderName(nullable: false, blank: false, maxSize: 555)
        isICInitialize nullable: false
        startTime (nullable: true)
        endTime(nullable: true)
        message(nullable: true)
        errorDetails(maxSize: 64 * 1024, nullable: true)
    }

    static namedQueries = {

        countAllBySearchStringAndStatusInList { LibraryFilter filter ->
            projections {
                countDistinct("id")
            }
            fetchAllBySearchStringAndStatusInListQuery(filter)
        }

        fetchAllBySearchStringAndStatusInList { LibraryFilter filter ->
            projections {
                distinct('id')
                property("dateCreated")
                property("lastUpdated")
                property("senderName")
                property("description")
                'owner' {
                    property("fullName", "fullName")
                }
            }
            fetchAllBySearchStringAndStatusInListQuery(filter)
        }
        fetchAllBySearchStringAndStatusInListQuery { LibraryFilter filter ->

            'in'("status", ReportExecutionStatusEnum.getCompletedStatusesList())
            eq('isDeleted', false)
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            if (filter.search) {
                or {
                    iLikeWithEscape('senderName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            if (filter.favoriteSort) {
                and {
                    order('lastUpdated', 'desc')
                }
            }
        }

        searchByTenant { Long id ->
            eq('tenantId', id)
        }

        fetchAllInboundBySearchString{LibraryFilter filter ->
            projections {
                distinct('id')
                'inboundCompliance' {
                    property("version", "version")
                    property("lastRunDate", "lastRunDate")
                }
                property("senderName")
                property("lastUpdated", "lastUpdated")
                'owner' {
                    property("fullName", "fullName")
                }
                property("runDuration")
            }
            eq('isDeleted', false)
            if (filter.search) {
                or {
                    iLikeWithEscape('senderName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
            if (filter.favoriteSort) {
                and {
                    order('lastUpdated', 'desc')
                }
            }
        }

        fetchAllInboundBySearchStringAndInProgressStatus{ LibraryFilter filter ->
            fetchAllInboundBySearchString(filter)
            eq('status', ReportExecutionStatusEnum.GENERATING)
        }

        fetchAllInboundBySearchStringAndCompletedStatus{ LibraryFilter filter ->
            fetchAllInboundBySearchString(filter)
            eq('status', ReportExecutionStatusEnum.COMPLETED)
        }

        fetchAllInboundBySearchStringAndErrorStatus{ LibraryFilter filter ->
            fetchAllInboundBySearchString(filter)
            eq('status', ReportExecutionStatusEnum.ERROR)
        }

    }

    boolean isEditableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isDev() || ((tenantId == Tenants.currentId() as Long) && (owner.id == currentUser?.id || currentUser.isAdmin() || owner.id in currentUser.getUserTeamIds())))
    }

    @Override
    public String toString() {
        super.toString()
    }

}
