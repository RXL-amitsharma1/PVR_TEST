package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.ReportRequestField
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class ReportRequestRestController extends RestfulController implements SanitizePaginationAttributes {

    def userService

    ReportRequestRestController() {
        super(ReportRequest)
    }

    def reportRequestDropdownList() {
        forSelectBox(params)
        User currentUser = userService.currentUser
        def result = ReportRequest.fetchByTerm(currentUser, params.term).list([max: params.max, offset: params.offset, sort: "reportName"])
        int total = ReportRequest.fetchByTerm(currentUser, params.term).count()
        render([items: result.collect { [id: it.id, text: it.id + " " + it.reportName] }, total_count: total] as JSON)
    }

    def index() {
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, userService.getUser(), ReportRequest)
        List<Long> ids = ReportRequest.fetchByFilter(filter, params.sort, params.order).list([max: params.max, offset: params.offset])*.first()
        List<ReportRequest> reportRequestList = ReportRequest.getAll(ids)
        render([aaData         : reportRequestList*.toReportRequestDto(),
                recordsTotal   : ReportRequest.countByFilter(new LibraryFilter(userService.getUser())).get(),
                recordsFiltered: ReportRequest.countByFilter(filter).get()] as JSON)
    }

    def widgetSearch() {
        User currentUser = userService.getUser()
        sanitize(params)
        formSort()
        def widgetFilter = params.wFilter ? JSON.parse(params.wFilter) : null
        List<Long> ids = ReportRequest.fetchByWidgetFilter(widgetFilter, currentUser).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])*.first()
        List<ReportRequest> reportRequestList = ReportRequest.getAll(ids)
        render([aaData         : reportRequestList*.toReportRequestDto(),
                recordsTotal   : ReportRequest.countByWidgetFilter(null, currentUser).get(),
                recordsFiltered: ReportRequest.countByWidgetFilter(widgetFilter, currentUser).get()] as JSON)
    }

    private formSort() {
        switch (params.sort) {
            case "assignedTo":
                params.sort = null
                break
            case "reportRequestId":
                params.sort = "id"
                break;
            case "requestName":
                params.sort = "reportName";
                break
            case "status":
                params.sort = "workflowState.name"
                break
            case "reportRequestType":
                params.sort = "reportRequestType.name"
                break
        }
    }

    def plan() {
        sanitize(params)
        params.aggregateOnly = false
        LibraryFilter filter = new LibraryFilter(params, userService.getUser(), ReportRequest)
        List reportRequestList = ReportRequest.fetchPlanByFilter(filter, params.sort, params.order).list()
        List customFields = []
        ReportRequestField.findAllByIsDeletedAndShowInPlan(false, true)?.sort { it.id }?.each {
            customFields << it.name
            if (it.fieldType == ReportRequestField.Type.CASCADE) customFields << "secondary" + it.name
        }
        render([aaData: formTree(reportRequestList.collect { ReportRequest.toReportRequestDtoFromFilter(it, customFields) })] as JSON)
    }

    List formTree(list) {
        List rootlist = []
        List childlist = []
        list.each {
            if (it.parentReportRequest)
                childlist << it
            else
                rootlist << it
        }
        childlist.each { child ->
            if (!rootlist.find { it.reportRequestId == child.parentReportRequest }) {
                ReportRequest root = ReportRequest.get(child.parentReportRequest)
                if (root)
                    rootlist << root.toReportRequestDto()
            }
        }
        rootlist.each { root ->
            def children = childlist.findAll { it.parentReportRequest == root.reportRequestId }
            if (children) {
                root.put("children", children)
            }
        }
    }
}
