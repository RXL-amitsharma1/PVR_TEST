package com.rxlogix

import com.rxlogix.config.ActionItem
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.Query
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.UserGroupQuery
import com.rxlogix.config.UserGroupTemplate
import com.rxlogix.config.UserTemplate
import com.rxlogix.customException.OwnershipException
import com.rxlogix.enums.ActionItemFilterEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.TransferTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.validation.ValidationException

class OwnershipService {
    def CRUDService
    def customMessageService

    def updateOwners(Map items) throws RuntimeException {

        User current = items.newUser
        UserGroup currentGroup = items.newUserGroup
        User previous = items.previousOwner

        //------query---------
        processQueries(items?.queries, items.transferType, previous, current, currentGroup)

        // templates
        processTemplates(items?.templates, items.transferType, previous, current, currentGroup)

        //------configuration---------
        processReportConfig(items?.configurations, Configuration.class, items.transferType, previous, current, currentGroup)

        //------aggregate configuration---------
        processReportConfig(items?.periodicConfigurations, PeriodicReportConfiguration.class, items.transferType, previous, current, currentGroup)

        //------ExecutedConfiguration---------
        processExecutedReportConfig(items?.executedConfigurations, ExecutedConfiguration.class, items.transferType, previous, current, currentGroup)

        //------ExecutedPeriodicReportConfiguration ---------
        processExecutedReportConfig(items?.executedPeriodicConfigurations, ExecutedPeriodicReportConfiguration.class, items.transferType, previous, current, currentGroup)

        //------case series---------
        processCaseSeries(items?.caseSeries, items.transferType, previous, current, currentGroup)

        //------Executed case series ---------
        processExecutedCaseSeries(items?.executedCaseSeries, items.transferType, previous, current, currentGroup)

        //-------ActionItems----------------------------------
        processActionItems(items?.actionItems, items.transferType, current, currentGroup )

        //-------ReportRequests----------------------------------
        processReportRequests(items?.reportRequests, items?.requesterRequests, items.transferType, previous, current, currentGroup )

    }


    private processTemplates(templatesList, TransferTypeEnum transferType, User previous, User current, UserGroup currentGroup) {
        if (templatesList) {
            templatesList.collate(999).each { templatesIds -> //oracle has limit of 1000 items in 'in' condition
                List<ReportTemplate> templates = ReportTemplate.findAllByIdInList(templatesIds)
                templates.each { template ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        template.owner = current
                    } else {
                        if(transferType == TransferTypeEnum.SHAREWITH) {
                            UserTemplate previousUserTemplate = template.userTemplates.find { it.user.id == previous.id }
                            if (previousUserTemplate) {
                                UserTemplate.remove(previous, template)
                                template.removeFromUserTemplates(previousUserTemplate)
                            }
                        }
                        template?.refresh()
                        if (current && !template.userTemplates.find { it.user.id == current.id })
                            template.addToUserTemplates(new UserTemplate(user: current))
                        if (currentGroup && !template.userGroupTemplates.find { it.userGroup.id == currentGroup.id })
                            template.addToUserGroupTemplates(new UserGroupTemplate(userGroup: currentGroup))
                    }
                    try {
                        CRUDService.update(template)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.query"), template?.name, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }
    private processQueries(queriesList, TransferTypeEnum transferType, User previous, User current, UserGroup currentGroup) {
        if (queriesList) {
            queriesList.collate(999).each { queriesIds -> //oracle has limit of 1000 items in 'in' condition
                List<SuperQuery> queries = SuperQuery.findAllByIdInList(queriesIds)
                queries.each { query ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        query.owner = current
                    } else {
                        if(transferType == TransferTypeEnum.SHAREWITH) {
                            UserQuery previousUserQuery = query.userQueries.find { it.user.id == previous.id }
                            if (previousUserQuery) {
                                UserQuery.remove(previous, query)
                                query.removeFromUserQueries(previousUserQuery)
                            }
                        }
                            query?.refresh()
                        if (current && !query.userQueries.find { it.user.id == current.id })
                            query.addToUserQueries(new UserQuery(user: current))
                        if (currentGroup && !query.userGroupQueries.find { it.userGroup.id == currentGroup.id })
                            query.addToUserGroupQueries(new UserGroupQuery(userGroup: currentGroup))
                    }
                    try {
                        CRUDService.update(query)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.query"), query?.name, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }

    }
    private processReportRequests(reportRequestsList, requesterRequestsList, TransferTypeEnum transferType, User previous, User current, UserGroup currentGroup) {
        if (reportRequestsList) {
            reportRequestsList.collate(999).each { requestsIds -> //oracle has limit of 1000 items in 'in' condition
                List<ReportRequest> requests = ReportRequest.findAllByIdInList(requestsIds)
                requests.each { request ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        request.owner = current
                    } else {
                        if (current) {
                            request.assignedTo = current
                        } else {
                            request.assignedTo = null
                            request.assignedGroupTo = currentGroup
                        }
                    }
                    try {
                        CRUDService.update(request)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.report.request"), request?.description, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
        if (requesterRequestsList && transferType == TransferTypeEnum.SHAREWITH)  {
            requesterRequestsList.collate(999).each { requestsIds -> //oracle has limit of 1000 items in 'in' condition
                List<ReportRequest> requests = ReportRequest.findAllByIdInList(requestsIds)
                requests.each { request ->
                    request.removeFromRequesters(previous)
                    if (current && !request.requesters.find { it == current })
                        request.addToRequesters(current)
                    if (currentGroup && !request.requesterGroups.find { it == currentGroup })
                        request.addToRequesterGroups(currentGroup)
                    try {
                        CRUDService.update(request)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.report.request"), request?.description, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }

    private processActionItems(actionItemsList, TransferTypeEnum transferType, User current, UserGroup currentGroup) {
        if (actionItemsList) {
            actionItemsList.collate(999).each { actionsIds -> //oracle has limit of 1000 items in 'in' condition
                List<ActionItem> actionItems = ActionItem.findAllByIdInList(actionsIds)
                actionItems.each { item ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        item.createdBy = current.username //createdBy plays owner role for ActionItem
                    } else {
                        if (current) {
                            item.assignedTo = current
                        } else {
                            item.assignedTo = null
                            item.assignedGroupTo = currentGroup
                        }
                    }
                    try {
                        CRUDService.update(item)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.action.app.name"), item?.description, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }


    private processReportConfig(configurationList, configurationType, TransferTypeEnum transferType, User previousUser, User currentUser, UserGroup currentGroup) {
        if (configurationList) {
            configurationList.collate(999).each { configurationsIds -> //oracle has limit of 1000 items in 'in' condition
                List<ReportConfiguration> configurations = (configurationType == Configuration.class ? Configuration.findAllByIdInList(configurationsIds) : PeriodicReportConfiguration.findAllByIdInList(configurationsIds))
                configurations.each { report ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        report.owner = currentUser
                        report.templateQueries.each { templateQuery ->
                            if (templateQuery.query && !templateQuery.query.isVisible(currentUser)) {
                                templateQuery.query.addToUserQueries(new UserQuery(user: currentUser))
                            }
                            if (templateQuery.template && !templateQuery.template.isVisible(currentUser)) {
                                templateQuery.template.addToUserTemplates(new UserTemplate(user: currentUser))
                            }
                        }
                    } else {
                        if(transferType == TransferTypeEnum.SHAREWITH) {
                            if (report.deliveryOption.sharedWith.find { it == previousUser }) {
                                report.deliveryOption.removeFromSharedWith(previousUser)
                            }
                        }
                        if (currentUser && !report.deliveryOption.sharedWith.find { it == currentUser })
                            report.deliveryOption.addToSharedWith(currentUser)
                        if (currentGroup && !report.deliveryOption.sharedWithGroup.find { it == currentGroup })
                            report.deliveryOption.addToSharedWithGroup(currentGroup)

                    }
                    try {
                        CRUDService.update(report)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.configuration"), report?.reportName, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }

    private processExecutedReportConfig(configurationList, configurationType, TransferTypeEnum transferType, User previousUser, User currentUser, UserGroup currentGroup) {
        if (configurationList) {
            configurationList.collate(999).each { configurationsIds -> //oracle has limit of 1000 items in 'in' condition
                List<ExecutedReportConfiguration> configurations = (configurationType == ExecutedConfiguration.class ? ExecutedConfiguration.findAllByIdInList(configurationsIds) : ExecutedPeriodicReportConfiguration.findAllByIdInList(configurationsIds))
                configurations.each { report ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        report.owner = currentUser
                    } else {
                        if(transferType == TransferTypeEnum.SHAREWITH) {
                            if (report.executedDeliveryOption.sharedWith.find { it == previousUser }) {
                                report.executedDeliveryOption.removeFromSharedWith(previousUser)
                            }
                        }
                        if (currentUser && !report.executedDeliveryOption.sharedWith.find { it == currentUser })
                            report.executedDeliveryOption.addToSharedWith(currentUser)
                        if (currentGroup && !report.executedDeliveryOption.sharedWithGroup.find { it == currentGroup })
                            report.executedDeliveryOption.addToSharedWithGroup(currentGroup)
                    }
                    try {
                        CRUDService.update(report)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.configuration"), report?.reportName, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }


    private processCaseSeries(caseSeriesList, TransferTypeEnum transferType, User previousUser, User currentUser, UserGroup currentGroup) {
        if (caseSeriesList) {
            caseSeriesList.collate(999).each { caseSeriesIds -> //oracle has limit of 1000 items in 'in' condition
                List<CaseSeries> caseSeries = CaseSeries.findAllByIdInList(caseSeriesIds)
                caseSeries.each { item ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        item.owner = currentUser
                        if (item.globalQuery && !item.globalQuery.isVisible(currentUser)) {
                            item.globalQuery.addToUserQueries(new UserQuery(user: currentUser))
                        }
                    } else {
                        if(transferType == TransferTypeEnum.SHAREWITH) {
                            if (item.deliveryOption.sharedWith.find { it == previousUser }) {
                                item.deliveryOption.removeFromSharedWith(previousUser)
                            }
                        }
                        if (currentUser && !item.deliveryOption.sharedWith.find { it == currentUser })
                            item.deliveryOption.addToSharedWith(currentUser)
                        if (currentGroup && !item.deliveryOption.sharedWithGroup.find { it == currentGroup })
                            item.deliveryOption.addToSharedWithGroup(currentGroup)
                    }
                    try {
                        CRUDService.update(item)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("app.label.case.series"), item?.seriesName, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }

    private processExecutedCaseSeries(caseSeriesList, TransferTypeEnum transferType, User previousUser, User currentUser, UserGroup currentGroup) {

        if (caseSeriesList) {
            caseSeriesList.collate(999).each { caseSeriesIds -> //oracle has limit of 1000 items in 'in' condition
                List<ExecutedCaseSeries> caseSeries = ExecutedCaseSeries.findAllByIdInList(caseSeriesIds)
                caseSeries.each { item ->
                    if (transferType == TransferTypeEnum.OWNERSHIP) {
                        item.owner = currentUser
                    } else {
                        if(transferType == TransferTypeEnum.SHAREWITH) {
                            if (item.executedDeliveryOption.sharedWith.find { it == previousUser }) {
                                item.executedDeliveryOption.removeFromSharedWith(previousUser)
                            }
                        }
                        if (currentUser && !item.executedDeliveryOption.sharedWith.find { it == currentUser })
                            item.executedDeliveryOption.addToSharedWith(currentUser)
                        if (currentGroup && !item.executedDeliveryOption.sharedWithGroup.find { it == currentGroup })
                            item.executedDeliveryOption.addToSharedWithGroup(currentGroup)
                    }
                    try {
                        CRUDService.update(item)
                    } catch (ValidationException e) {
                        OwnershipException e1 = new OwnershipException(customMessageService.getMessage("ownership.executedCaseSeries.label"), item?.seriesName, e.message, e.errors)
                        throw e1
                    }
                }
            }
        }
    }
}
