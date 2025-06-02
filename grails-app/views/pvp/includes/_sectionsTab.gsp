<%@ page import="com.rxlogix.enums.ReportActionEnum; com.rxlogix.config.publisher.GanttItem; com.rxlogix.config.publisher.PublisherReport; com.rxlogix.config.publisher.PublisherExecutedTemplate;com.rxlogix.util.DateUtil; com.rxlogix.enums.ActionItemGroupState; com.rxlogix.enums.StatusEnum; com.rxlogix.config.ActionItem; com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>

<div role="tabpanel" class="tab-pane " id="sectionsTab">

    <g:if test="${canGenerateAll}">
        <button class="btn btn-primary generateAll" style="margin-right: 10px"><g:message code="app.label.PublisherTemplate.generateAll" default="Generate Draft for all sections"/></button>
    </g:if>
    <g:if test="${canPublish && isContributor}">
        <button class="btn btn-primary publish"><g:message code="app.label.PublisherTemplate.createFull" default="Generate Full Document"/></button>
    </g:if>
    <br><br>
    <table class="table dataTable" id="publisherSectionsTable">
        <thead>
        <tr>
            <th width="3%" class="text-center">
                <g:if test="${isContributor||isOwner}">
                <a href="${createLink(action: 'addSection')}?reportId=${params.id}"><span class="table-add md md-plus theme-color md-lg"></span>
                </g:if>
            </a></th>
            <th width="15%" class="text-center"><g:message code="app.label.PublisherTemplate.sectionName"/></th>
            <th width="4%" class="text-center"><g:message code="app.label.reportSubmission.destinations"/></th>
            <th width="12%" class="text-center"><g:message code="app.label.reportRequest.assigned.to"/></th>
            <th width="12%" class="text-center"><g:message code="app.label.reportRequestType.modifiedBy"/></th>
            <th width="12%" class="text-center"><g:message code="app.periodicReport.executed.daysLeft.label"/></th>
            <th width="3%" class="text-center"><g:message code="app.label.state"/></th>
            <th width="15%" class="text-center"><g:message code="app.label.PublisherTemplate.output"/></th>
            <th width="15%" class="text-center"><g:message code="app.label.action.app.name"/></th>
            <th width="4%" class="text-center"><g:message code="report.submission.comment"/></th>
            <th width="12%" class="text-center"><g:message code="app.label.action"/></th>

        </tr>
        </thead>
        <g:if test="${isFinal}">
            <g:each var="row" in="${table}">
                <g:set var="isCompleted" value="${row.publisherAction == PublisherExecutedTemplate.Status.FINAL.name()}"/>
                <tr data-id="${row.id}">
                <td class="col-min-70">
                    <g:if test="${row.publisherAction == PublisherExecutedTemplate.Status.FINAL.name()}">
                        <button type="button" class="btn btn-xs btn-primary">
                            <span class="fa fa-check" style=""></span>
                        </button>
                    </g:if>
                    <g:else>
                        <g:if test="${isContributor||isOwner}">
                        <a href="javascript:void(0)" class="removeSectionBtn iconButton" data-id="${row.id}" data-instancename="${row.name}"><span class='table-remove md md-close pv-cross'></span>
                        </a>
                        </g:if>
                    </g:else>
                <g:if test="${isContributor||isOwner}">
                    <span class='table-add md md-arrow-up pv-cross publisherExecutedSectionUp'></span>
                    <span class='table-add md md-arrow-down pv-cross publisherExecutedSectionDown'></span>
                </g:if>
                </td>
                </td>
                <td>

                    <span class="nameLabel content">${row.name}</span> <span class="fa fa-edit nameUpdate" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span>
                    <input class="sectionId" type="hidden" value="${row.id}">
                </td>

                <td>
                    <span class="destinationLabel">${row.destination}</span>  <g:if test="${isContributor}"><span class="fa fa-edit destinationUpdate" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span></g:if>
                    <input class="sectionId" type="hidden" value="${row.id}">
                </td>
                <td>
                    <input class="sectionId" type="hidden" value="${row.id}">
                    <span class="queryTemplateUserGroupLabel">${row.assignedTo}</span>  <g:if test="${isContributor}"><span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span></g:if>
                    <input type="hidden" name="assigned" value="">
                    <br>

                    <g:message code="app.label.PublisherTemplate.author"/>: <span class="authorLabel">${row.author}</span>  <g:if test="${isContributor}"><span class="fa fa-edit author" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span></g:if>
                    <input type="hidden" name="author" value="">
                    <br>
                    <g:message code="app.label.PublisherTemplate.reviewer"/>: <span class="reviewerLabel">${row.reviewer}</span>  <g:if test="${isContributor}"><span class="fa fa-edit reviewer" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span></g:if>
                    <input type="hidden" name="reviewer" value="">
                    <br>
                    <g:message code="app.label.PublisherTemplate.approver"/>: <span class="approverLabel">${row.approver}</span>  <g:if test="${isContributor}"><span class="fa fa-edit approver" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span></g:if>
                    <input type="hidden" name="approver" value="">
                </td>
                <td>
                    <g:renderLongFormattedDate date="${row.lastUpdated}" timeZone="${g.getCurrentUserTimezone()}"/> (${row.modifiedBy})
                </td>
                <td align="center">
                    <span class="dueLabel"><span class="roundLabel ${row.dueDateClass}">${row.dueDate}</span>
                    </span>
                    <g:if test="${isContributor}"> <span class="fa fa-edit dueUpdate" style="cursor: pointer" title="${message(code: "default.button.edit.label")}"></span></g:if>
                    <input class="sectionId" type="hidden" value="${row.id}">
                </td>
                <td align="center">
                    <g:if test="${isContributor}">
                        <button class="btn btn-default btn-round btn-xs" style="min-width: 100px" data-publisherSection-id="${row.id}" data-initial-state="${row.workflowSate}" data-evt-clk='{"method": "openStateHistoryModal", "params": ["${this}", "${createLink(controller: "workflowJustificationRest", action: "publisherSection")}", "${createLink(controller: "workflowJustificationRest", action: "savePublisherSection")}", "_page"]}'>${row.workflowSate}</button>
                    </g:if>
                    <g:else>
                        <button class="btn btn-default btn-round btn-xs" style="min-width: 100px" >${row.workflowSate}</button>
                    </g:else>
                </td>
                <td align="center">
                     <g:if test="${row.publisherAction == PublisherExecutedTemplate.Status.EMPTY.name()}">
                                <g:if test="${row.draftExecutionStatus}">
                                    <a href='javascript:void(0)'  data-id='${row.draftFileId}' class='btn btn-xs btn-round btn-danger showPublisherLog '><g:message code="app.label.PublisherTemplate.generationErrors" default="Generation Errors"/></a>
                                    <a class='btn  btn-xs btn-success graftPublisherTemplate ' title="Restore" href='javascript:void(0)' data-id="${row.id}"><span class="fa fa-undo"></span> <g:message code="app.label.PublisherTemplate.restore" default="Restore"/>
                                </g:if>
                                <g:else>
                                    <a href="javascript:void(0)" class="btn btn-xs btn-round btn-danger"><g:message code="app.label.PublisherTemplate.notGenerated"/></a>
                                </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${row.publisherAction == PublisherExecutedTemplate.Status.DRAFT.name()}">

                               <g:if test="${grailsApplication.config.officeOnline.enabled}">
                                    <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "default.button.edit.label")}" href="${createLink(controller: 'wopi', action: 'edit')}?type=publisherExecutedTemplate&id=${row.draftFileId}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}"><span class="fa fa-edit" style="cursor: pointer"></span></a>
                                    <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.reportFormat.PDF")}" href="${createLink(controller: 'wopi', action: 'pdf')}?type=publisherExecutedTemplate&id=${row.draftFileId}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}"><span class="fa fa-file-pdf-o" style="cursor: pointer"></span></a>
                                </g:if>
                                <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherExecutedTemplate', absolute: true)}?id=${row.draftFileId}&type=${row.type}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}" data-name="${row.name}.docx" type="button" class="btn btn-xs btn-round btn-success finalPublisherTemplate downloadUrl" title="${message(code: "app.label.download")}"><span class="fa fa-download"></span></a>
                                <a href="javascript:void(0)" class="btn btn-xs btn-success graftPublisherTemplate" data-id="${row.id}" title="${message(code: "report.submission.history")}"> <span class="fa fa-history"></span></a>
                                <g:if test="${row.draftExecutionStatus == PublisherExecutedTemplate.ExecutionStatus.WARNINGS}">
                                <a href='javascript:void(0)'  data-id='${row.draftFileId}' class='showPublisherLog '><span style='padding-left: 3px;padding-right: 3px;' class='fa fa-warning ${row.executionStatusCss}' title="${ViewHelper.getMessage(row.executionStatus?.i18nKey) ?: "-"}"></span></a>
                                </g:if>

         <div><g:message code="app.label.PublisherTemplate.pending"/><br>
                                <g:message code="app.label.PublisherTemplate.pending.parameters"/>:${row.pendingVariable}<br>
                                <g:message code="app.label.PublisherTemplate.manual"/>:${row.pendingManual}<br>
                                <g:message code="app.label.PublisherTemplate.pending.comments"/>:${row.pendingComment}
                            </g:if>
                            <g:else>
                                <g:if test="${grailsApplication.config.officeOnline.enabled}">
                                    <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.label.view")}" href="${createLink(controller: 'wopi', action: 'view')}?type=publisherExecutedTemplate&id=${row.finalFileId}"><span class="fa fa-eye" style="cursor: pointer"></span>
                                    </a>
                                    <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.reportFormat.PDF")}" href="${createLink(controller: 'wopi', action: 'pdf')}?type=publisherExecutedTemplate&id=${row.finalFileId}"><span class="fa fa-file-pdf-o" style="cursor: pointer"></span>
                                    </a>
                                    <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherExecutedTemplate', absolute: true)}?id=${row.finalFileId}&type=${row.type}&reportId=${params.id}" data-name="${row.name}.docx" type="button" class="btn btn-xs  btn-success finalPublisherTemplate downloadUrl" title="${message(code: "app.label.download")}"><span class="fa fa-download"></span>
                                    </a>
                                    <a href="javascript:void(0)" class="btn btn-xs btn-success graftPublisherTemplate" data-id="${row.id}" title="${message(code: "report.submission.history")}"> <span class="fa fa-history"></span>
                                    </a>
                                </g:if>
                                <g:else>
                                    <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherExecutedTemplate', absolute: true)}?id=${row.finalFileId}&type=${row.type}&reportId=${params.id}" data-name="${row.name}.docx" type="button" class="btn btn-xs btn-round btn-success finalPublisherTemplate downloadUrl"><span class="fa fa-download"></span> <g:message code="app.label.PublisherTemplate.final"/>
                                    </a>
                                    <a href="javascript:void(0)" class="btn btn-xs btn-success graftPublisherTemplate" data-id="${row.id}"> <span class="fa fa-history"></span>
                                    </a>
                                </g:else>
                            </g:else>
                        </g:else>

                </td>
                <td align="center">
                    <g:if test="${row.aiClass}">
                        <button type="button" class="btn ${row.aiClass} btn-xs btn-round actionItemModalIcon" data-section-id="${row.id}" style="width:70px;">${row.aiLabel}</button>
                    </g:if>
                    <span class="fa fa-plus-circle createPublisherSectionActionItem" data-section-id="${row.id}" style="cursor: pointer"></span>
                </td>
                <td align="center">
                    <a href="#" class="commentModalTrigger" data-owner-id="${row.type == "queryTemplateId" ? row.reportResult.id : row.id}"
                       data-comment-type="${row.type == "queryTemplateId" ? CommentTypeEnum.REPORT_RESULT : CommentTypeEnum.PUBLISHER_SECTION}"
                       data-toggle="modal" data-target="#commentModal">
                        <g:renderAnnotateIcon comments="${row.comments}" title="${message(code: "section.annotaion")}"/>
                    </a>

                </td>
                <td style="padding-left: 4px;padding-right: 0px;min-width: 110px">

                        <div class="btn-group dropdown" align="center" style="min-width: 161px">

                            <g:if test="${row.publisherAction == PublisherExecutedTemplate.Status.EMPTY.name()}">
                                <g:if test="${row.noTemplate}">
                                    <a href="javascript:void(0)" class="btn btn-success btn-xs uploadDocument " data-id="${row.id}" data-type="${row.type}">Upload</a>
                                </g:if>
                                <g:else>
                                    <a data-href="${createLink(action: 'generate')}?id=${row.id}&type=${row.type}&reportId=${params.id}" data-id="${row.id}" data-type="${row.type}" class="btn btn-success btn-xs generateBtn btn-left-round generatePublisherTemplate"><g:message code="app.label.PublisherTemplate.generate"/></a>
                                    <button type="button" class="btn btn-default btn-xs btn-right-round dropdown-toggle" data-toggle="dropdown">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">

                                    <li role="presentation"><a href="javascript:void(0)" class="editTemplateBtn" data-id="${row.id}"><g:message code="app.label.PublisherTemplate.editSectionConfig"/></a></li>

                                </g:else>
                            </g:if>
                            <g:else>
                                <g:if test="${row.publisherAction == PublisherExecutedTemplate.Status.DRAFT.name()}">
                                    <g:if test="${(row.pendingManual == 0) && (row.pendingVariable == 0) && isContributor}">
                                            <a data-href="${createLink(controller: "pvp", action: "setAsFinal")}?sectionid=${row.id}&reportId=${params.id}" data-id="${row.id}" class="btn btn-left-round btn-xs btn-success setAsFinal" data-actionitems="${row.aiLabel}"><g:message code="app.label.PublisherTemplate.setFinal"/></a>
                                    </g:if>
                                    <g:else>
                                        <a href="javascript:void(0)" data-id="${row.id}" class="pendingBtn btn btn-left-round btn-xs btn-warning"><g:message code="app.label.PublisherTemplate.generate.continue" /></a>
                                    </g:else>
                                    <button type="button" class="btn btn-default btn-right-round btn-xs dropdown-toggle" data-toggle="dropdown">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                                    <g:if test="${((row.pendingManual == 0) && (row.pendingVariable == 0))}">
                                        <li role="presentation"><a href="javascript:void(0)" data-id="${row.id}" class="pendingBtn"><g:message code="app.label.PublisherTemplate.generate.continue" default="Continue Generation"/></a></li>
                                    </g:if>
                                    <g:if test="${!row.noTemplate}">
                                        <li role="presentation"><a href="javascript:void(0)" data-href="${createLink(action: 'generate')}?id=${row.id}&reportId=${params.id}" class="generateBtn"><g:message code="app.label.PublisherTemplate.generate.fromTemplate" default="Generate From Template"/></a>
                                        </li>
                                    </g:if>
                                    <li role="presentation"><a href="javascript:void(0)" class="uploadDocument" data-id="${row.id}" data-type="${row.type}"><g:message code="app.label.PublisherTemplate.upload.modal.button"/></a>
                                    </li>
                                    <g:if test="${!row.noTemplate}">
                                    <li role="presentation"><a href="javascript:void(0)" class="editTemplateBtn" data-id="${row.id}"><g:message code="app.label.PublisherTemplate.editSectionConfig"/></a>
                                    </li>
                                    </g:if>
                                    <g:if test="${row.draftFileId}">
                                        <li role="presentation"><a href="${createLink(controller: 'pvp', action: 'pushTheLastChanges')}?sectionId=${row.id}&reportId=${params.id}" data-evt-clk='{"method": "showLoader", "params": []}'><g:message code="app.publisher.push"/></a>
                                        </li>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:if test="${isContributor}">
                                    <a class="btn btn-default btn-xs btn-round" href="${createLink(action: 'removeFinalStatus')}?id=${row.finalFileId}&reportId=${params.id}"><g:message code="app.label.PublisherTemplate.removeFinalStatus"/></a>
                                    </g:if>
                                </g:else>
                            </g:else>

                        </ul>

                        </div>
                    <div class="publisherParameterValueDiv" style="display: none">
                        <input type="hidden" name="id" value="${row.id}">
                        <input type="hidden" name="reportId" value="${params.id}">
                        <g:render template="/configuration/includes/publisherParameterValue" model="[publisherParameterName: 'publisherTemplateSectionParameterValue', publisherTemplate: row?.publisherTemplate, parameterValues: row?.parameterValues]"/>

                    </div>
                    <div class="publisherFileParameterValueDiv" style="display: none">
                        <input type="hidden" name="id" value="${row.id}">
                        <input type="hidden" name="reportId" value="${params.id}">
                        <g:render template="/configuration/includes/publisherFileSection" model="[publisherParameterName: 'publisherTemplateSectionParameterValue', parameters: row?.parameterValues, filename: row?.filename]"/>

                    </div>
                </td>
                </tr>
            </g:each>
        </g:if>
    </tbody>
    </table>

</div>


<g:render template="/pvp/includes/actionCompletionModal"/>