<%@ page import="com.rxlogix.enums.ReportActionEnum; com.rxlogix.config.publisher.GanttItem; com.rxlogix.config.publisher.PublisherReport; com.rxlogix.config.publisher.PublisherExecutedTemplate;com.rxlogix.util.DateUtil; com.rxlogix.enums.ActionItemGroupState; com.rxlogix.enums.StatusEnum; com.rxlogix.config.ActionItem; com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>

<div role="tabpanel" class="tab-pane" id="overviewTab">
    <div style="margin-left: 50px;">

        <ul class="timeline">
            <li class="timeline-inverted">
                <g:if test="${isFinal}">
                    <div class="timeline-badge success"><i class="glyphicon glyphicon-thumbs-up"></i>
                    </div>
                </g:if>
                <g:else>
                    <div class="timeline-badge warning"><i class="glyphicon glyphicon-refresh"></i>
                    </div>
                </g:else>

                <div class="timeline-panel">
                    <div class="timeline-heading">
                        <h4 class="timeline-title"><g:message code="app.label.gantt.stage.reportStage"/></h4>
                    </div>
                    <div class="timeline-body">

                        <div class="row timelineRow">
                            <div class="col-md-3">
                                <div class="panel panel-default">
                                    <div class="panel-heading"><g:message code="app.label.publisher.overview.main"/></div>
                                    <div class="panel-body">
                                        <b><g:message code="app.label.publisher.overview.currentWorkflowState"/>:</b> ${executedConfigurationInstance.workflowState?.name}<br>
                                        <b><g:message code="app.label.publisher.overview.finalReportWasGenerated"/>:</b>  ${isFinal ? "YES" : "NO"}
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="panel panel-default">
                                    <div class="panel-heading"><g:message code="app.label.publisher.overview.actionItems"/></div>
                                    <div class="panel-body">
                                        <g:set var="userService" bean="userService"/>
                                        <g:set var="userGroups" value="${UserGroup.fetchAllUserGroupByUser(userService.currentUser)}"/>

                                        <g:set var="openAi" value="${executedConfigurationInstance.actionItems}"/>
                                        <b><g:message code="app.label.publisher.overview.totalClosed"/>:</b> ${openAi?.size() ?: 0}(${openAi?.findAll { it.status == StatusEnum.CLOSED }?.size() ?: 0})<br>
                                        <g:set var="openAi" value="${executedConfigurationInstance.actionItems?.findAll { userService.currentUser in it.assignedToUserList }}"/>
                                        <b><g:message code="app.label.publisher.overview.assignedToMeClosed"/>:</b> ${openAi?.size() ?: 0} (${openAi?.findAll { it.status == StatusEnum.CLOSED }?.size() ?: 0})<br>

                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="panel panel-default">
                                    <div class="panel-heading"><g:message code="app.label.publisher.overview.reportOutput"/></div>
                                    <div class="panel-body">
                                        <g:each var="executedTemplateQuery" in="${executedConfigurationInstance.executedTemplateQueries}">

                                            <g:set var="reportResult" value="${executedTemplateQuery.finalReportResult}"/>
                                            <div class="row">
                                                <div class="col-md-8">
                                                    <g:if test="${!reportResult || reportResult.executionStatus == ReportExecutionStatusEnum.ERROR}">
                                                        <div style="text-align: center"><i class="fa fa-exclamation-triangle" title="${g.message(code: 'app.label.section.not.generated')}"></i>
                                                        </div>
                                                    </g:if>
                                                    <g:elseif test="${reportResult.executionStatus != ReportExecutionStatusEnum.COMPLETED}">
                                                        <div style="text-align: center"><i class="fa fa-clock-o fa-lg es-scheduled fa-lg es-scheduled" title="${g.message(code: 'app.label.scheduled')}"></i>
                                                        </div>
                                                    </g:elseif>
                                                    <g:else>
                                                        <g:if test="${executedTemplateQuery.isVisible()}">
                                                            <g:link controller="report" action="show"
                                                                    params="[id: reportResult.id, isInDraftMode: false]">
                                                                <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}"
                                                                                           executedTemplateQuery="${executedTemplateQuery}"
                                                                                           hideSubmittable="${true}"/>
                                                            </g:link>
                                                        </g:if>
                                                        <g:else>
                                                            <span class="fa fa-ban" title="${g.message(code: 'app.label.PublisherTemplate.error.section.message')}"></span>
                                                            <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}"
                                                                                       executedTemplateQuery="${executedTemplateQuery}"
                                                                                       hideSubmittable="${true}"/>
                                                        </g:else>
                                                        <br>
                                                        <b><g:message code="app.label.template"/>:</b> <g:link controller="template" action="viewExecutedTemplate"
                                                                                                               params="[id: executedTemplateQuery?.executedTemplate?.id]">${executedTemplateQuery?.executedTemplate?.name}
                                                    </g:link>
                                                        <b><g:message code="app.label.query"/>:</b>
                                                        <g:if test="${executedTemplateQuery?.executedQuery}">
                                                            <g:link controller="query" action="viewExecutedQuery"
                                                                    params="[id: executedTemplateQuery?.executedQuery?.id]">
                                                                ${executedTemplateQuery?.executedQuery?.name}
                                                            </g:link>
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="app.label.no.query"/>
                                                        </g:else>
                                                    </g:else>

                                                </div>
                                                <div class="col-md-4">
                                                    <g:if test="${!reportResult || reportResult.executionStatus == ReportExecutionStatusEnum.ERROR}">
                                                        <div style="text-align: center"><i class="fa fa-exclamation-triangle" title="${g.message(code: 'app.label.section.not.generated')}"></i>
                                                        </div>
                                                    </g:if>
                                                    <g:elseif test="${reportResult.executionStatus != ReportExecutionStatusEnum.COMPLETED}">
                                                        <div style="text-align: center"><i class="fa fa-clock-o fa-lg es-scheduled fa-lg es-scheduled" title="${g.message(code: 'app.label.scheduled')}"></i>
                                                        </div>
                                                    </g:elseif>
                                                    <g:else>
                                                        <g:if test="${executedTemplateQuery.isVisible()}">

                                                            <a href="javascript:void(0)"
                                                               data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.PDF.name()], absolute: true)}"
                                                               data-name="${renderDynamicReportName(executedConfiguration: executedConfigurationInstance, executedTemplateQuery: executedTemplateQuery, hideSubmittable: true).replaceAll(":", "_")}.pdf"
                                                               class="downloadUrl btn btn-xs btn-success btn-round" style=" min-width: 90px; ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.PDF, reportType) ? 'pointer-events: none' : ''}">
                                                                <span class="fa fa-file-pdf-o"></span> PDF
                                                            </a>


                                                            <a href="javascript:void(0)"
                                                               data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.XLSX.name()], absolute: true)}"
                                                               data-name="${renderDynamicReportName(executedConfiguration: executedConfigurationInstance, executedTemplateQuery: executedTemplateQuery, hideSubmittable: true).replaceAll(":", "_")}.XLSX"
                                                               class="downloadUrl btn btn-xs btn-success btn-round" style=" min-width: 90px;${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.XLSX, reportType) ? 'pointer-events: none' : ''}">
                                                                <span class="fa fa-file-excel-o"></span> Excel
                                                            </a>

                                                            <a href="javascript:void(0)"
                                                               data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.DOCX.name()], absolute: true)}"
                                                               data-name="${renderDynamicReportName(executedConfiguration: executedConfigurationInstance, executedTemplateQuery: executedTemplateQuery, hideSubmittable: true).replaceAll(":", "_")}.DOCX"
                                                               class="downloadUrl btn btn-xs btn-success btn-round" style=" min-width: 90px;${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.DOCX, reportType) ? 'pointer-events: none' : ''}">
                                                                <span class="fa fa-file-word-o"></span> Word
                                                            </a>


                                                            <a href="javascript:void(0)"
                                                               data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.PPTX.name()], absolute: true)}"
                                                               data-name="${renderDynamicReportName(executedConfiguration: executedConfigurationInstance, executedTemplateQuery: executedTemplateQuery, hideSubmittable: true).replaceAll(":", "_")}.PPTX"
                                                               class="downloadUrl btn btn-xs btn-success btn-round" style=" min-width: 90px; ${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.PPTX, reportType) ? 'pointer-events: none' : ''}">
                                                                <span class="fa fa-file-powerpoint-o"></span> Power Point
                                                            </a>

                                                        </g:if>
                                                        <g:else>
                                                            <span class="fa fa-ban" title="${g.message(code: 'app.label.PublisherTemplate.error.section.message')}"></span>
                                                        </g:else>
                                                    </g:else>
                                                </div>
                                            </div>

                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </li>
            <g:if test="${!isFinal}">
                <li class="timeline-inverted">
                    <div class="timeline-badge"><i class="glyphicon glyphicon-hourglass"></i></div>
                    <div class="timeline-panel">
                        <div class="timeline-heading">
                            <h4 class="timeline-title"><g:message code="app.label.gantt.stage.pubSectStage"/></h4>
                        </div>
                        <div class="timeline-body">
                            <g:message code="app.label.publisher.overview.finalEmpty"/>
                            <p><small class="text-muted"><i class="glyphicon glyphicon-info-sign"></i>
                                <g:message code="app.label.publisher.overview.comment1"/>
                            </small></p>
                        </div>
                    </div>
                </li>
            </g:if>
            <g:else>
                <g:each in="${executedConfigurationInstance.publisherConfigurationSections?.sort { it.sortNumber }}" var="section">
                    <li class="timeline-inverted">
                        <g:if test="${section.getState() == PublisherExecutedTemplate.Status.FINAL}">
                            <div class="timeline-badge success"><i class="glyphicon glyphicon-thumbs-up"></i>
                            </div>
                        </g:if>
                        <g:elseif test="${section.getState() == PublisherExecutedTemplate.Status.DRAFT}">
                            <div class="timeline-badge warning"><i class="glyphicon glyphicon-refresh"></i>
                            </div>
                        </g:elseif>
                        <g:else>
                            <div class="timeline-badge "><i class="glyphicon glyphicon-hourglass"></i>
                            </div>
                        </g:else>
                        <div class="timeline-panel">
                            <div class="timeline-heading">
                                <h4 class="timeline-title"><g:message code="app.label.gantt.stage.pubSectStage"/>: ${section.name}</h4>
                            </div>
                            <div class="timeline-body">
                                <div class="row timelineRow">
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading"><g:message code="app.label.publisher.overview.main"/></div>
                                            <div class="panel-body">
                                                <b><g:message code="app.label.publisher.overview.currentWorkflowState"/>:</b> ${section.workflowState?.name}<br>
                                                <b><g:message code="app.label.publisher.overview.finalDocument"/>:</b>  ${section.lastPublisherExecutedTemplates?.status == PublisherExecutedTemplate.Status.FINAL ? "YES" : "NO"}<br>
                                                <b><g:message code="app.label.publisher.overview.destination"/>:</b> ${section.destination}<br>
                                                <b><g:message code="app.label.publisher.overview.overview"/>:</b> ${section.dueDate?.format(DateUtil.DATEPICKER_FORMAT_AM_PM) ?: ""}<br>

                                                <b><g:message code="app.label.publisher.overview.generatedDocument"/>:</b>

                                                <g:if test="${section.lastPublisherExecutedTemplates?.id}">
                                                <g:if test="${section.isVisible() && section.lastPublisherExecutedTemplates?.id}">
                                                    <g:if test="${grailsApplication.config.officeOnline.enabled}">
                                                        <div class="btn-group" role="group">
                                                            <g:set var="isCompleted" value="${section.lastPublisherExecutedTemplates.status == PublisherExecutedTemplate.Status.FINAL}"/>
                                                            <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.reportFormat.PDF")}" href="${createLink(controller: 'wopi', action: 'pdf')}?type=publisherExecutedTemplate&id=${section.lastPublisherExecutedTemplates?.id}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}"><span class="fa fa-file-pdf-o" style="cursor: pointer"></span>
                                                            </a>
                                                            <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.label.view")}" href="${createLink(controller: 'wopi', action: 'view')}?type=publisherExecutedTemplate&id=${section.lastPublisherExecutedTemplates?.id}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}"><span class="fa fa-eye" style="cursor: pointer"></span>
                                                            </a>
                                                            <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherExecutedTemplate', absolute: true)}?id=${section.lastPublisherExecutedTemplates?.id}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}" data-name="${section.name}.docx" type="button" class="btn btn-xs  btn-success finalPublisherTemplate downloadUrl"><span class="fa fa-download"></span>
                                                            </a>
                                                        </div>
                                                    </g:if>
                                                    <g:else>
                                                        <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherExecutedTemplate', absolute: true)}?id=${section.lastPublisherExecutedTemplates?.id}&reportId=${params.id}&fromOneDrive=${isCompleted?"false":"true"}" data-name="${section.name}.docx" type="button" class="btn btn-xs  btn-success finalPublisherTemplate downloadUrl"><span class="fa fa-download"></span>
                                                        </a>
                                                    </g:else>

                                                </g:if>
                                                <g:else>
                                                    <g:message code="app.label.publisher.overview.permission"/>
                                                </g:else>
                                                </g:if>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading"><g:message code="app.label.publisher.overview.assignedTo"/></div>
                                            <div class="panel-body">
                                                <b><g:message code="app.label.publisher.overview.assignedTo"/>:</b> ${section.assignedToGroup ? (ViewHelper.getMessage("app.excelExport.groups") + section.assignedToGroup?.name) : defaultSharedWith}<br>
                                                <b><g:message code="app.label.PublisherTemplate.author"/>:</b> ${section.author?.fullName}<br>
                                                <b><g:message code="app.label.PublisherTemplate.reviewer"/>:</b> ${section.reviewer?.fullName}<br>
                                                <b><g:message code="app.label.PublisherTemplate.approver"/>:</b> ${section.approver?.fullName}<br>

                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading"><g:message code="app.label.publisher.overview.pending"/></div>
                                            <div class="panel-body">
                                                <b><g:message code="app.label.PublisherTemplate.pending.parameters"/>:</b>${section.pendingVariable}<br>
                                                <b><g:message code="app.label.PublisherTemplate.manual"/>:</b>${section.pendingManual}<br>
                                                <b><g:message code="app.label.PublisherTemplate.pending.comments"/>:</b>${section.pendingComment}
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading"><g:message code="app.label.publisher.overview.actionItems"/></div>
                                            <div class="panel-body">
                                                <g:set var="userService" bean="userService"/>

                                                <g:set var="openAi" value="${section.actionItems}"/>
                                                <b><g:message code="app.label.publisher.overview.totalClosed"/>:</b> ${openAi?.size() ?: 0}(${openAi?.findAll { it.status == StatusEnum.CLOSED }?.size() ?: 0})<br>
                                                <g:set var="openAi" value="${section.actionItems?.findAll { userService.currentUser in it.assignedToUserList }}"/>
                                                <b><g:message code="app.label.publisher.overview.assignedToMeClosed"/>:</b> ${openAi?.size() ?: 0} (${openAi?.findAll { it.status == StatusEnum.CLOSED }?.size() ?: 0})<br>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </li>
                </g:each>
            </g:else>
            <g:if test="${!executedConfigurationInstance.publisherReports}">
                <li class="timeline-inverted">
                    <div class="timeline-badge"><i class="glyphicon glyphicon-hourglass"></i></div>
                    <div class="timeline-panel">
                        <div class="timeline-heading">
                            <h4 class="timeline-title"><g:message code="app.label.gantt.stage.publishingStage"/></h4>
                        </div>
                        <div class="timeline-body">
                            <g:message code="app.label.publisher.overview.noFull"/>
                            <p><small class="text-muted"><i class="glyphicon glyphicon-info-sign"></i>
                                <g:message code="app.label.publisher.overview.comment2"/>
                            </small></p>
                        </div>
                    </div>
                </li>
            </g:if>
            <g:else>
                <g:each in="${executedConfigurationInstance.publisherReports.findAll{it.isDeleted==false}}" var="report">
                    <li class="timeline-inverted">
                        <g:if test="${report.published}">
                            <div class="timeline-badge success"><i class="glyphicon glyphicon-thumbs-up"></i>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="timeline-badge warning"><i class="glyphicon glyphicon-refresh"></i>
                            </div>
                        </g:else>
                        <div class="timeline-panel">
                            <div class="timeline-heading">
                                <h4 class="timeline-title"><g:message code="app.label.gantt.stage.publishingStage"/>: ${report.name}</h4>
                            </div>
                            <div class="timeline-body">
                                <div class="row timelineRow">
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading"><g:message code="app.label.publisher.overview.main"/></div>
                                            <div class="panel-body">
                                                <b><g:message code="app.label.publisher.overview.currentWorkflowState"/>:</b> ${report.workflowState?.name}<br>
                                                <b><g:message code="app.label.publisher.overview.documentCompleted"/>:</b>  ${report.published ? "YES" : "NO"}<br>
                                                <b><g:message code="app.label.publisher.overview.destination"/>:</b> ${report.destination}<br>
                                                <b><g:message code="app.label.publisher.overview.dueDate"/>:</b> ${report.dueDate?.format(DateUtil.DATEPICKER_FORMAT_AM_PM) ?: ""}<br>

                                                <b><g:message code="app.label.publisher.overview.generatedDocument"/>:</b>
                                                <g:if test="${report.isVisible()}">
                                                    <div class="btn-group" role="group">
                                                        <g:if test="${grailsApplication.config.officeOnline.enabled}">

                                                            <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.reportFormat.PDF")}" href="${createLink(controller: 'wopi', action: 'pdf')}?type=publisherReport&id=${report.id}&reportId=${params.id}&fromOneDrive=${report.published?"false":"true"}"><span class="fa fa-file-pdf-o" style="cursor: pointer"></span>
                                                            </a>
                                                            <a target="_blank" class='btn  btn-xs btn-success wopiLink' title="${message(code: "app.label.view")}" href="${createLink(controller: 'wopi', action: 'view')}?type=publisherReport&id=${report.id}&reportId=${params.id}&fromOneDrive=${report.published?"false":"true"}"><span class="fa fa-eye" style="cursor: pointer"></span>
                                                            </a>

                                                        </g:if>

                                                        <a href="javascript:void(0)" data-url="${createLink(action: 'downloadPublisherReport', absolute: true)}?id=${report.id}&type=publisherReport&reportId=${params.id}&fromOneDrive=${report.published?"false":"true"}" data-name="${report.name}.docx" type="button" class="btn btn-xs btn-success  downloadUrl"><span class="fa fa-download"></span>
                                                        </a>

                                                    </div>
                                                </g:if>
                                                <g:else>
                                                    <g:message code="app.label.publisher.overview.permission2"/>
                                                </g:else>

                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading"><g:message code="app.label.publisher.overview.assignedTo"/></div>
                                            <div class="panel-body">
                                                <b><g:message code="app.label.publisher.overview.assignedTo"/>:</b> ${report.assignedToGroup ? (ViewHelper.getMessage("app.excelExport.groups") + report.assignedToGroup?.name) : defaultSharedWith}<br>
                                                <b><g:message code="app.label.PublisherTemplate.author"/>:</b> ${report.author?.fullName}<br>
                                                <b><g:message code="app.label.PublisherTemplate.reviewer"/>:</b> ${report.reviewer?.fullName}<br>
                                                <b><g:message code="app.label.PublisherTemplate.approver"/>:</b> ${report.approver?.fullName}<br>

                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="panel panel-default">
                                            <div class="panel-heading">Action Items</div>
                                            <div class="panel-body">
                                                <g:set var="userService" bean="userService"/>

                                                <g:set var="openAi" value="${report.actionItems}"/>
                                                <b><g:message code="app.label.publisher.overview.totalClosed"/>:</b> ${openAi?.size() ?: 0}(${openAi?.findAll { it.status == StatusEnum.CLOSED }?.size() ?: 0})<br>
                                                <g:set var="openAi" value="${report.actionItems?.findAll { userService.currentUser in it.assignedToUserList }}"/>
                                                <b><g:message code="app.label.publisher.overview.assignedToMeClosed"/>:</b> ${openAi?.size() ?: 0} (${openAi?.findAll { it.status == StatusEnum.CLOSED }?.size() ?: 0})<br>

                                            </div>
                                        </div>
                                    </div>

                                </div>

                            </div>
                        </div>

                    </li>
                </g:each>
            </g:else>
            <li class="timeline-inverted">
                <g:if test="${!executedConfigurationInstance.reportSubmissions}">
                    <div class="timeline-badge"><i class="glyphicon glyphicon-hourglass"></i></div>
                </g:if>
                <g:else>
                    <div class="timeline-badge success"><i class="glyphicon glyphicon-thumbs-up"></i>
                    </div>
                </g:else>
                <div class="timeline-panel">
                    <div class="timeline-heading">
                        <h4 class="timeline-title"><g:message code="app.label.gantt.stage.submissionStage"/></h4>
                    </div>
                    <div class="timeline-body">
                        <g:if test="${!executedConfigurationInstance.reportSubmissions}">
                            No submissions yet
                        </g:if>
                        <g:else>
                            <table class="table">
                                <tr><th><g:message code="report.submission.submissionDate"/></th>
                                    <th><g:message code="report.submission.status"/></th>
                                    <th><g:message code="app.label.reportSubmission.destinations"/></th>
                                    <th><g:message code="app.label.view.case.late"/></th>
                                    <th><g:message code="app.label.document"/></th>
                                </tr>

                                <g:each var="row" in="${executedConfigurationInstance.reportSubmissions}">
                                    <tr>
                                        <td><g:renderShortFormattedDate date="${row.submissionDate}"/></td>
                                        <td>${message(code: row.reportSubmissionStatus.i18nKey)}</td>
                                        <td>${row.reportingDestination}</td>
                                        <td>${row.late}</td>
                                        <td>
                                            <g:each var="att" in="${row.attachments}">
                                                <a href="javascript:void(0)" data-url="${createLink(controller: "reportSubmission", action: "downloadAttachment")}?id=${att.id}" data-name="${att.name}.docx" type="button" class="btn btn-xs btn-success  downloadUrl"><span class="fa fa-download"></span>${att.name}
                                                </a><br>
                                            </g:each>
                                        </td>

                                    </tr>
                                </g:each>
                            </table>
                        </g:else>
                    </div>
                </div>
            </li>
        </ul>
    </div>

</div>