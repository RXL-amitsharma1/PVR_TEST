<%@ page import="com.rxlogix.enums.ReportActionEnum; com.rxlogix.config.publisher.GanttItem; com.rxlogix.config.publisher.PublisherReport; com.rxlogix.config.publisher.PublisherExecutedTemplate;com.rxlogix.util.DateUtil; com.rxlogix.enums.ActionItemGroupState; com.rxlogix.enums.StatusEnum; com.rxlogix.config.ActionItem; com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>

<div role="tabpanel" class="tab-pane " id="publisherTab">
    <table class="table dataTable" id="publisherReportsTable">
        <thead>
        <tr>
            <th width="3%" style="text-align: center"><g:if test="${isContributor}">
                <a href="#" class="addPublisherReportDocument" data-toggle="modal" data-target="#publishReportModal"><span class="table-add md md-plus md-lg theme-color"></span>
            </a></g:if></th>
            <th width="10%" style="text-align: center"><g:message code="app.label.name"/></th>
            <th width="12%" style="text-align: center"><g:message code="app.label.reportSubmission.destinations"/></th>
            <th width="12%" style="text-align: center"><g:message code="app.label.reportRequest.assigned.to"/></th>
            <th width="10%" style="text-align: center"><g:message code="app.label.modifiedBy"/></th>
            <th width="10%" style="text-align: center"><g:message code="app.label.dateModified"/></th>
            <th width="14%" style="text-align: center"><g:message code="app.periodicReport.executed.daysLeft.label"/></th>
            <th width="7%" style="text-align: center"><g:message code="app.label.state"/></th>
            <th width="7%" style="text-align: center"><g:message  default="QC State" code="app.label.qcstate"/></th>
            <th width="10%" style="text-align: center"><g:message code="app.label.PublisherTemplate.output"/></th>
            <th width="4%" style="text-align: center"><g:message code="app.label.action.app.name"/></th>
            <th width="3%" style="text-align: center"><g:message code="report.submission.comment"/></th>
            <th width="8%" style="text-align: center"><g:message code="app.label.action"/></th>

        </tr>
        </thead>

        <g:each var="row" in="${publisherReports?.sort { it.id }}">

            <tr data-id="${row.id}">
                <td style="text-align: center;">
                    <g:if test="${row.published}">
                        <button type="button" class="btn btn-xs btn-primary">
                            <span class="fa fa-check" style=""></span>
                        </button>
                    </g:if>
                    <g:else>
                        <g:if test="${isContributor}">
                        <a href="javascript:void(0)" class="removePublisherReportBtn iconButton" data-id="${row.id}" data-instancename="${row.name}"><span class='table-remove md md-close pv-cross'></span>
                        </a>
                        </g:if>
                    </g:else>
                </td>
                <td>

                    <span class="nameLabel content">${row.name}</span> <span class="fa fa-edit nameUpdate" style="cursor: pointer"></span>
                    <input class="publisherReportId" type="hidden" value="${row.id}">
                </td>

                <td>
                    <span class="destinationLabel">${row.destination}</span> <g:if test="${isContributor}"><span class="fa fa-edit destinationUpdate" style="cursor: pointer"></span></g:if>
                    <input class="publisherReportId" type="hidden" value="${row.id}">
                </td>
                <td>
                    <input class="publisherReportId" type="hidden" value="${row.id}">
                    <span class="queryTemplateUserGroupLabel">${row.assignedTo}</span> <g:if test="${isContributor}"><span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span></g:if>
                    <input type="hidden" name="assigned" value="">
                    <br>

            <g:message code="app.label.PublisherTemplate.author"/>: <span class="authorLabel">${row.author}</span> <g:if test="${isContributor}"><span class="fa fa-edit author" style="cursor: pointer"></span></g:if>
                    <input type="hidden" name="author" value="">
                    <br>
                    <g:message code="app.label.PublisherTemplate.reviewer"/>: <span class="reviewerLabel">${row.reviewer}</span> <g:if test="${isContributor}"><span class="fa fa-edit reviewer" style="cursor: pointer"></span></g:if>
                    <input type="hidden" name="reviewer" value="">
                    <br>
                    <g:message code="app.label.PublisherTemplate.approver"/>: <span class="approverLabel">${row.approver}</span> <g:if test="${isContributor}"><span class="fa fa-edit approver" style="cursor: pointer"></span></g:if>
                    <input type="hidden" name="approver" value="">
                </td>
                <td>${row.modifiedBy}</td>
                <td>
                    <g:renderLongFormattedDate date="${row.lastUpdated}" timeZone="${g.getCurrentUserTimezone()}"/>
                </td>
                <td align="center">
                    <span class="dueLabel"><span class="roundLabel ${row.dueDateClass}">${row.dueDate}</span>
                    </span>
                    <g:if test="${isContributor}"> <span class="fa fa-edit dueUpdate" style="cursor: pointer"></span></g:if>
                    <input class="publisherReportId" type="hidden" value="${row.id}">
                </td>
                <td align="center">
                    <g:if test="${isContributor}">
                        <button class="btn btn-default btn-round btn-xs" style="min-width: 100px" data-publisherDocument-id="${row.id}" data-publisherDocument-type="PUBLISHER_FULL" data-initial-state="${row.workflowSate}" data-evt-clk='{"method": "openStateHistoryModal", "params": ["${this}", "${createLink(controller: "workflowJustificationRest", action: "publisherDocument")}", "${createLink(controller: "workflowJustificationRest", action: "savePublisherDocument")}", "_page"]}'>${row.workflowSate}</button>
                    </g:if><g:else>
                    <button class="btn btn-default btn-round btn-xs" style="min-width: 100px">${row.workflowSate}</button>
                </g:else>
                </td>
                <td align="center">
                    <g:if test="${isContributor}">
                        <button class="btn btn-default btn-round btn-xs" style="min-width: 100px" data-publisherDocument-id="${row.id}" data-publisherDocument-type="PUBLISHER_FULL_QC" data-initial-state="${row.qcWorkflowState}" data-evt-clk='{"method": "openStateHistoryModal", "params": ["${this}", "${createLink(controller: "workflowJustificationRest", action: "publisherDocument")}", "${createLink(controller: "workflowJustificationRest", action: "savePublisherDocument")}", "_page"]}'>${row.qcWorkflowState}</button>
                    </g:if><g:else>
                    <button class="btn btn-default btn-round btn-xs" style="min-width: 100px">${row.qcWorkflowState}</button>
                </g:else>
                </td>
                <td align="center">

                        <g:if test="${grailsApplication.config.officeOnline.enabled}">
                            <g:if test="${row.published}">
                                <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.label.view")}" href="${createLink(controller: 'wopi', action: 'view')}?type=publisherReport&id=${row.id}&reportId=${params.id}&fromOneDrive=${row.published?"false":"true"}"><span class="fa fa-eye" style="cursor: pointer"></span>
                                </a>
                            </g:if>
                            <g:else>
                                <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "default.button.edit.label")}" href="${createLink(controller: 'wopi', action: 'edit')}?type=publisherReport&id=${row.id}&reportId=${params.id}&fromOneDrive=${row.published?"false":"true"}"><span class="fa fa-edit" style="cursor: pointer"></span>
                                </a>
                            </g:else>
                            <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.reportFormat.PDF")}" href="${createLink(controller: 'wopi', action: 'pdf')}?type=publisherReport&id=${row.id}&fromOneDrive=${row.published?"false":"true"}"><span class="fa fa-file-pdf-o" style="cursor: pointer"></span>
                            </a>
                        </g:if>

                        <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherReport', absolute: true)}?id=${row.id}&fromOneDrive=${row.published?"false":"true"}&type=publisherReport&reportId=${params.id}" data-name="${row.name}.docx" type="button" class="btn btn-xs btn-success  downloadUrl" title="${message(code: "app.label.download")}"><span class="fa fa-download"></span>
                        </a>

                </td>
                <td align="center">
                    <g:if test="${row.aiClass}">
                        <button type="button" class="btn ${row.aiClass} btn-xs btn-round actionItemPublisherModalIcon" data-publisher-id="${row.id}" style="width:65px;">${row.aiLabel}</button>
                    </g:if>
                    <span class="fa fa-plus-circle createPublisherDocumentActionItem" data-publisher-id="${row.id}" style="cursor: pointer"></span>
                </td>
                <td align="center">
                    <a href="#" class="commentModalTrigger" data-owner-id="${row.id}"
                       data-comment-type="${CommentTypeEnum.PUBLISHER_FULL}"
                       data-toggle="modal" data-target="#commentModal">
                        <g:renderAnnotateIcon comments="${row.comments}" title="${message(code: "section.annotaion")}"/>
                    </a>

                </td>
                <td style="padding-left: 4px;padding-right: 0px;min-width: 110px">
                        <g:if test="${row.published}">
                            <g:if test="${isContributor}">
                            <a class="btn btn-default btn-xs btn-round" href="${createLink(action: 'distribute')}?id=${row.id}&reportId=${params.id}"><g:message code="app.label.PublisherTemplate.removeFinalStatus"/></a>
                            </g:if>
                        </g:if>
                        <g:else>
                            <div class="btn-group dropdown" align="center">
                                <a href="javascript:void(0)" class="btn btn-success btn-xs btn-left-round updatePublisherReportDocument " data-id="${row.id}"><g:message code="app.label.PublisherTemplate.upload.modal.button"/></a>
                                <button type="button" class="btn btn-default btn-xs btn-right-round dropdown-toggle" data-toggle="dropdown">
                                    <span class="caret"></span>
                                    <span class="sr-only">Toggle Dropdown</span>
                                </button>
                                <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                                    <g:if test="${isContributor}">
                                        <li role="presentation"><a href="${createLink(controller: "pvp", action: "distribute")}?id=${row.id}&reportId=${params.id}" data-id="${row.id}"><g:message code="app.label.PublisherTemplate.setFinal"/></a>
                                        </li>
                                    </g:if>
                                    <li role="presentation"><a href="${createLink(controller: 'pvp', action: 'pushTheLastChanges')}?publisherId=${row.id}&reportId=${params.id}" data-evt-clk='{"method": "showLoader", "params": []}'><g:message code="app.publisher.push"/></a>
                                    </li>
                                </ul>
                            </div>
                        </g:else>
                </td>
            </tr>
        </g:each>
    </table>
    <g:if test="${!publisherReports}">
        <h5 style="text-align: center;"><g:message code="app.label.Publisher.publisherReports.empty"/></h5>
    </g:if>

</div>