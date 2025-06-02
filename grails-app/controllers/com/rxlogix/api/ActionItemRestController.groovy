package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.LibraryFilter
import com.rxlogix.util.FilterUtil
import com.rxlogix.config.ActionItem
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.InboundDrilldownMetadata
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.QualitySampling
import com.rxlogix.config.QualitySubmission
import com.rxlogix.enums.ActionItemFilterEnum
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import grails.gorm.multitenancy.Tenants
import com.rxlogix.config.ActionItemCategory

@Secured('permitAll')
class ActionItemRestController extends RestfulController implements SanitizePaginationAttributes {

    def userService

    ActionItemRestController() {
        super(ActionItem)
    }

    def index(String filterType) {
        Long executedReportId = params.long("executedReportId")
        Long sectionId = params.long("sectionId")
        Long publisherId = params.long("publisherId")
        Boolean pvq = params.boolean("pvq")
        filterType = filterType ?: ActionItemFilterEnum.MY_OPEN.value
        User currentUser = userService.getUser()
        sanitize(params)
        LibraryFilter filter = new LibraryFilter(params, currentUser, ActionItem)
        List<Long> ids = ActionItem.fetchActionItemsBySearchString(filter, filterType, currentUser, executedReportId, sectionId,publisherId, pvq, params.sort, params.order).list([max: params.max, offset: params.offset])*.first()
        List<ActionItem> actionItemList = ActionItem.getAll(ids)

        render([aaData         : actionItemList*.toActionItemMap(),
                recordsTotal   : ActionItem.fetchActionItemsBySearchString(new LibraryFilter(currentUser), filterType, currentUser, executedReportId, sectionId,publisherId, pvq).count(),
                recordsFiltered: ActionItem.fetchActionItemsBySearchString(filter, filterType, currentUser, executedReportId, sectionId,publisherId, pvq).count()] as JSON)
}

    def listForPublisher() {
        User currentUser = params.boolean("user") ? userService.getUser() : null
        sanitize(params)
        Long executedReportId = params.long("id")
        switch (params.sort) {
            case "assignedTo":
                params.sort = null
                break
            case "actionItemId":
                params.sort = "id"
        }
        List<Long> sectionsIds = ExecutedPeriodicReportConfiguration.get(executedReportId)?.publisherConfigurationSections?.collect { it.id }
        List<Long> publisherIds = ExecutedPeriodicReportConfiguration.get(executedReportId)?.publisherReports?.collect { it.id }
        List<ActionItem> actionItemList = ActionItem.fetchActionItemsForPublisherExecutedReport(params.searchString, executedReportId, sectionsIds, publisherIds, currentUser).list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        def aaData = actionItemList.collect {
            Map map = it.toActionItemMap()
            def relatedFor = ViewHelper.getMessage("app.label.PublisherTemplate.report")
            if (it.publisherSection)
                relatedFor = it.publisherSection.name
            map.put("relatedFor", relatedFor)
            map
        }
        render([aaData         : aaData, recordsTotal: ActionItem.fetchActionItemsForPublisherExecutedReport(null, executedReportId, sectionsIds, publisherIds, currentUser).count(),
                recordsFiltered: ActionItem.fetchActionItemsForPublisherExecutedReport(params.searchString, executedReportId, sectionsIds, publisherIds, currentUser).count()] as JSON)
    }

    def indexPvq() {
        User currentUser = userService.getUser()
        List actionItemIds = null
        Long tenantId = Tenants.currentId()
        switch(params.dataType){
            case PvqTypeEnum.CASE_QUALITY.toString():
                actionItemIds = QualityCaseData.getActionItemIds(params, tenantId)
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                actionItemIds = QualitySubmission.getActionItemIds(params, tenantId)
                break
            case null:
                actionItemIds = ActionItem.findAllByIsDeleted(false, [offset: params.start, sort: params.sort, order: params.direction]).findAll {it.actionCategory.forPvq && it.getAssignedToUserList().contains(currentUser)}.take(Integer.parseInt(params.length)).collect { it.id }
                break
            default:
                actionItemIds = QualitySampling.getActionItemIds(params, tenantId, params.dataType)
        }
        List<ActionItem> actionItemList = []
        actionItemIds.collate(1000).each { list ->
            actionItemList += ActionItem.findAllByIdInList(list as List, [sort: params.sort, order: params.direction])
        }
        render([aaData: actionItemList*.toActionItemMap(), recordsTotal: actionItemIds.size(), recordsFiltered: actionItemList.size()] as JSON)
    }

    def indexByParentEntityKey() {
        Date from = DateUtil.getDateWithDayStartTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.from))
        Date to = DateUtil.getDateWithDayEndTime(Date.parse(DateUtil.DATEPICKER_FORMAT, params.to))
        List<ActionItem> actionItemList = ActionItem.findAllByParentEntityKeyAndDateRangeFromLessThanEqualsAndDateRangeToGreaterThanEqualsAndIsDeleted(params.parentEntityKey, to, from, false, [max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        int total = ActionItem.countByParentEntityKeyAndIsDeletedAndDateCreatedGreaterThanAndDateCreatedLessThanEquals(params.parentEntityKey, false, from, to)
        render([aaData: actionItemList*.toActionItemMap(), recordsTotal: total, recordsFiltered: actionItemList.size()] as JSON)
    }

    def indexDrilldown() {
        List<Long> actionItemIds
        if (params.senderId) {
            actionItemIds = InboundDrilldownMetadata.getMetadataRecord(params).get().actionItems.collect{it.id}
        }
        else {
            actionItemIds = DrilldownCLLMetadata.getMetadataRecord(params).get().actionItems.collect{it.id}
        }
        List<ActionItem> actionItemList = ActionItem.findAllByIdInListAndIsDeleted(actionItemIds, false, [max: params.length, offset: params.start, sort:params.sort, order: params.direction])
        render([aaData: actionItemList*.toActionItemMap(), recordsTotal: actionItemIds.size(), recordsFiltered: actionItemList.size()] as JSON)
    }
}
