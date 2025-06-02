package com.rxlogix.config

import com.rxlogix.LibraryFilter
import com.rxlogix.OrderByUtil
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.UserGroup
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class ReportSubmission {
    static auditable =  true

    ReportSubmissionStatusEnum reportSubmissionStatus = ReportSubmissionStatusEnum.SUBMITTED
    String comment

    Date submissionDate

    //Standard fields
    Date dateCreated
    Date lastUpdated
    Date dueDate
    String createdBy
    String modifiedBy
    String reportingDestination
    String late

    boolean isPrimary
    Long tenantId
    static belongsTo = [executedReportConfiguration: ExecutedReportConfiguration]
    static hasMany = [attachments: SubmissionAttachment, lateReasons: ReportSubmissionLateReason]

    static constraints = {
        submissionDate(nullable: true, validator: { val, obj ->
            if (obj.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMITTED && !val) {
                return "submission.date.nullable"
            }
        })
        reportingDestination(nullable: false, blank: false)
        reportingDestination unique: "executedReportConfiguration"
        comment nullable: false, maxSize: 4000
        dueDate nullable: true
        attachments nullable: true
        late(nullable: true, validator: { val, obj ->
            if (val && (!obj.lateReasons || obj.lateReasons.size() == 0)) {
                return "submission.late.reason"
            }
        })
        lateReasons nullable: true
    }

    static mapping = {
        table name: "RPT_SUBMISSION"
        reportSubmissionStatus column: "RPT_SUBMISSION_STATUS"
        comment column: 'COMMENT_DATA', sqlType: "VARCHAR(4000)"
        submissionDate column: 'SUBMISSION_DATE'
        executedReportConfiguration column: 'EX_RCONFIG_ID'
        reportingDestination column: "REPORTING_DESTINATION"
        dueDate column: "DUE_DATE"
        tenantId column: "TENANT_ID"
    }

    static namedQueries = {

        fetchReportSubmissionBySearchString { LibraryFilter filter, ReportSubmissionStatusEnum status, List<Long> executedIdList, Boolean icsr, String sortBy = null, String sortDirection = "asc" ->

            projections {
                distinct('id')
                property("report.reportName", "reportName")
                property("report.periodicReportType", "reportType")
                property("globalDateRange.dateRangeStartAbsolute", "pvrDateRangeStart")
                property("submissionDate", "submissionDate")
                property("reportingDestination", "reportingDestination")
                property("reportSubmissionStatus", "reportSubmissionStatus")
                property("dueDate", "dueDate")
                property("isPrimary", "isPrimaryDestination")
                property("late")
                property("dateCreated")
            }
            createAlias("executedReportConfiguration", "report", CriteriaSpecification.LEFT_JOIN)
            createAlias("report.executedGlobalDateRangeInformation", "globalDateRange", CriteriaSpecification.LEFT_JOIN)
            createAlias('report.executedDeliveryOption', 'exd', CriteriaSpecification.LEFT_JOIN)
            createAlias('report.owner', 'owner', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWith', 'sw', CriteriaSpecification.LEFT_JOIN)
            createAlias('exd.sharedWithGroup', 'swg', CriteriaSpecification.LEFT_JOIN)

            if (sortBy) {
                if (sortBy == 'reportingDestination') {
                    order(OrderByUtil.trimOrderIgnoreCase(sortBy, sortDirection));
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }

            if (filter.forPublisher && !filter.allReportsForPublisher) {
                eq('report.isPublisherReport', true)
            }
            if (filter.search) {
                or {

                    or {
                        iLikeWithEscape('report.reportName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                        iLikeWithEscape('report.productSelection', "%\"name\":\"${EscapedILikeExpression.escapeString(filter.search)}%")
                        PeriodicReportTypeEnum.searchBy(filter.search)?.each {
                            eq('report.periodicReportType', it)
                        }
                    }

                    ReportSubmissionStatusEnum.searchBy(filter.search)?.each {
                        eq('reportSubmissionStatus', it)
                    }
                    iLikeWithEscape('reportingDestination', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                }
            }
            if (executedIdList) {
                or {
                    executedIdList.collate(999).each { part ->
                        'in'('report.id', part)
                    }
                }
            }
            if (status) {
                eq("reportSubmissionStatus", status)
            }

            if (icsr) {
                eq('report.clazz', ExecutedIcsrReportConfiguration.name)
            } else {
                ne('report.clazz', ExecutedIcsrReportConfiguration.name)
            }

            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }

            if (!filter.user?.isAdmin()) {
                or {
                    'eq'('owner.id', filter.user.id)
                    'in'('sw.id', [filter.user.id])
                    if (UserGroup.countAllUserGroupByUser(filter.user)) {
                        'in'('swg.id', UserGroup.fetchAllUserGroupByUser(filter.user).id)
                    }
                }
            }
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_DEV")) {
                searchByTenant(Tenants.currentId() as Long)
            }
        }
        searchByTenant { Long id ->
            eq('tenantId', id)
        }
    }

    String getInstanceIdentifierForAuditLog() {
        return "Report Submission for ${executedReportConfiguration?.reportName}"
    }

    boolean isSubmissionRequired() {
        return reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMITTED
    }

    public String toString() {
        return "Report Submission for ${this.executedReportConfiguration?.reportName}"
    }

}
