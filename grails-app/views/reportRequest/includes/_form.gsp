<%@ page import="com.rxlogix.RxCodec; grails.util.Holders; grails.converters.JSON; com.rxlogix.config.ReportRequestField; com.rxlogix.config.UserDictionary; com.rxlogix.enums.ReportRequestFrequencyEnum; com.rxlogix.config.ReportRequest; com.rxlogix.Constants; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PriorityEnum; com.rxlogix.enums.StatusEnum;com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.WorkflowState" %>

<g:if test="${'mode' != 'create'}">
    <g:hiddenField name="id" value="${reportRequestInstance?.id}"/>
</g:if>
<g:set var="userTimeZone" value="${getCurrentUserTimezone()}"/>
<g:set var="userService" bean="userService"/>
<g:set var="userLocale" value="${userService?.getCurrentUser()?.preference?.locale}"/>

<div class="rxmain-container rxmain-container-top">

    <div class="rxmain-container-inner">

            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.basic.information"/>
                </label>
            </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div class="row">

                %{-- Report Request Name --}%
                <div class="col-xs-3">
                    <div class="${hasErrors(bean: reportRequestInstance, field: 'reportName', 'has-error')}">
                        <label><g:message code="app.label.report.request.name"/><span class="required-indicator">*</span>
                        </label>
                        <input type="text" name="reportName" placeholder="${g.message(code: 'input.name.placeholder')}"
                               class="form-control reportRequest"
                               value="${reportRequestInstance?.reportName}"
                               maxlength="${ReportRequest.constrainedProperties.reportName.maxSize}"/>
                    </div>
                </div>

                %{-- Report Configuration Type --}%
                <div class="col-xs-3">
                    <div class="${hasErrors(bean: reportRequestInstance, field: 'reportType', 'has-error')}">
                        <label><g:message code="app.label.reportRequest.request.type"/><span class="required-indicator">*</span>
                        </label>
                        <select name="reportRequestType.id" id="reportRequestType.id" class="form-control reportRequest select2-box">
                            <g:each in="${reportRequestTypes}" var="type">
                                <option value="${type.id}" ${reportRequestInstance?.reportRequestType?.id == type.id ? 'selected="selected"' : ''} data-aggregate="${type.aggregate}">${type.name}</option>
                            </g:each>
                        </select>
                    </div>
                </div>

                %{-- Report Priority --}%
                <div class="col-xs-3">
                    <div class="${hasErrors(bean: reportRequestInstance, field: 'priority', 'has-error')}">
                        <label><g:message code="app.label.action.item.priority"/></label>
                        <g:select name="priority.id" optionKey="id" optionValue="name" data-placeholder=""
                                  from="${reportRequestPriority}" class="form-control reportRequest select2-box"
                                  value="${reportRequestInstance?.priority?.id}"
                                  data-value="${reportRequestInstance?.priority?.id}"
                        />
                    </div>
                </div>

                %{-- Report Status --}%
                <div class="col-xs-3">
                    <div>
                        <label><g:message code="app.label.action.item.status"/></label>
                        <input class="form-control" disabled value="${(reportRequestInstance?.id ? reportRequestInstance.workflowState?.name : WorkflowState.getDefaultWorkState().name)}">

                    </div>
                </div>

            </div>

            %{-- Row with due date, requesters and assigned to. --}%
            <div class="row">

                <div class="col-xs-3 aggregateReportInformation">
                    <div>
                        <label><g:message code="app.label.versionNamePattern" default="Version Name Pattern"/>
                            <span class="fa fa-question-circle"  style="cursor: pointer;" data-toggle="modal" data-target="#generatedReportPatternHelp"></span></label>
                        <input type="text" name="generatedReportName" placeholder="${g.message(code: 'app.label.versionNamePattern')}"
                               class="form-control"
                               maxlength="${ReportRequest.constrainedProperties.generatedReportName.maxSize}"
                               value="${reportRequestInstance?.generatedReportName?: Holders.config.aggregateReport.generatedReportNamePattern}"/>
                    </div>
                    <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
                        <div class="checkbox checkbox-primary " style="margin-top: 5px !important;">
                            <g:checkBox name="masterPlanningRequest" value="${reportRequestInstance?.masterPlanningRequest}" checked="${reportRequestInstance?.masterPlanningRequest}"/>
                            <label for="masterPlanningRequest">
                                <g:message code="app.label.reportRequest.masterPlanningRequest"/>
                            </label>
                        </div>
                    </sec:ifAnyGranted>
                    <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
                        <input type="checkbox" id="masterPlanningRequest" style="display: none">
                    </sec:ifAnyGranted>
                </div>
                <div class="col-xs-3 adhocReportInformation">
                    <label><g:message default="Due Date" code="app.label.action.item.due.date"/><span class="required-indicator">*</span>
                    </label>
                    <div class="fuelux ${hasErrors(bean: reportRequestInstance, field: 'dueDate', 'has-error')}">
                        <div>
                            <div class="datepicker pastDateNotAllowed toolbarInline" id="dueDateDiv">
                                <div class="input-group">
                                    <g:textField id="dueDate" placeholder="${message(code: "placeholder.dueDate.label")}" class="form-control fuelux date reportRequest" name="dueDate"
                                                 value=""/>
                                    <g:render id="dueDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 aggregateReportInformation">
                    <label><g:message default="Type & Category of Report" code="app.label.reportRequest.psrTypeFile"/></label>
                    <g:select id="psrTypeFile" name="psrTypeFile" multiple="true"
                              noSelection="['': '']"
                              from="${UserDictionary.findAllByType(UserDictionary.UserDictionaryType.PSR_TYPE_FILE)}"
                              value="${reportRequestInstance?.psrTypeFile?.tokenize(",") as List}"
                              optionKey="name"
                              optionValue="name"
                              data-type="${UserDictionary.UserDictionaryType.PSR_TYPE_FILE.name()}"
                              class="form-control"/>

                </div>
                <div class="col-xs-3 adhocReportInformation">
                    <label><g:message default="Completion Date" code="app.label.action.item.completion.date"/></label>
                    <div class="fuelux">
                        <div>
                            <div class="datepicker pastDateNotAllowed toolbarInline" id="completionDateDiv">
                                <div class="input-group">
                                    <g:textField id="completionDate" placeholder="${message(code: "placeholder.completionDate.label")}" class="form-control fuelux date reportRequest" name="completionDate"
                                                 value=""/>
                                    <g:render id="completionDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <g:set var="reportRequestors" value="${reportRequestInstance?.requesters}"/>
                    <g:set var="reportRequestorGroups" value="${reportRequestInstance?.requesterGroups}"/>
                    <g:set var="users" value="${User.findAllByEnabled(true).sort { it.username }}"/>
                    <g:set var="userGroups" value="${UserGroup.findAllByIsDeleted(false).sort { it.name }}"/>
                    <label><g:message code="app.label.report.request.request.by"/></label>
                    <g:if test="${!reportRequestors && !reportRequestorGroups}">
                        <g:set var="requestorsValue" value="${Constants.USER_TOKEN + userService?.getCurrentUser()?.id}"/>
                    </g:if>
                    <g:else>
                        <g:set var="requestorsValue" value="${([] + reportRequestorGroups?.collect { Constants.USER_GROUP_TOKEN + it.id } + reportRequestors?.collect { Constants.USER_TOKEN + it.id })?.join(";")}"/>
                    </g:else>
                    <select class="form-control" id="requesters" name="requesters" data-value="${requestorsValue}" value="${requestorsValue}"></select>
                </div>

                <div class="col-xs-3">
                    <label><g:message default="Assigned To" code="app.label.action.item.assigned.to"/><span class="required-indicator">*</span>
                    </label>
                    <script>
                        sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                        sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                        $(function () {
                            bindShareWith($('.assignedToReportRequest'), sharedWithListUrl, sharedWithValuesUrl, "100%", true);
                            bindShareWith($('#requesters'), sharedWithListUrl, sharedWithValuesUrl, "100%");
                        });
                    </script>
                    <g:set var="assignedToValue" value="${reportRequestInstance?.assignedGroupTo ? (Constants.USER_GROUP_TOKEN + reportRequestInstance.assignedGroupTo.id) : (reportRequestInstance?.assignedTo ? (Constants.USER_TOKEN + reportRequestInstance.assignedTo.id) : "")}"/>
                    <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_ASSIGN">
                        <select class="form-control assignedTo assignedToReportRequest" name="assignedTo" data-value="${assignedToValue}" value="${assignedToValue}"></select>
                    </sec:ifAnyGranted>
                    <sec:ifNotGranted roles="ROLE_REPORT_REQUEST_ASSIGN">
                        <select class="form-control assignedTo assignedToReportRequest" readonly="" name="assignedTo" data-value="${assignedToValue}" value="${assignedToValue}"></select>
                    </sec:ifNotGranted>
                </div>

            </div>

            %{--Row with description, start date and end date --}%
            <div class="row">

                <div class="col-xs-3">
                    <div class="${hasErrors(bean: reportRequestInstance, field: 'description', 'has-error')}">
                        <label><g:message code="app.label.reportDescription"/></label>
                        <g:textArea name="description" value="${reportRequestInstance?.description}"
                                    maxlength="4000"
                                    class="form-control reportRequest" style="height: 110px;"/>
                    </div>
                </div>
                <div class="col-xs-3 aggregateReportInformation">
                    <g:if test="${reportRequestInstance && requestorNotesVisible}">
                        <label><g:message default="Requestor Notes" code="app.label.reportRequest.requestorNotes"/></label>
                        <g:textArea name="requestorNotes" value="${reportRequestInstance?.requestorNotes}"
                                    maxlength="4000"
                                    class="form-control reportRequest" style="height: 110px;"/>
                    </g:if>
                </div>

                <div class="col-xs-3 aggregateReportInformation">
                    <label><g:message default="INN" code="app.label.reportRequest.inn"/></label>
                    <g:select id="inn" name="inn"
                              noSelection="['': '']"
                              from="${UserDictionary.findAllByType(UserDictionary.UserDictionaryType.INN)}"
                              value="${reportRequestInstance?.inn}"
                              optionKey="name"
                              optionValue="name"
                              data-type="${UserDictionary.UserDictionaryType.INN.name()}"
                              class="form-control select2-box"/>
                    <label><g:message default="Drug Code" code="app.label.reportRequest.drugCode"/></label>
                    <g:select id="drugCode" name="drugCode"
                              noSelection="['': '']"
                              from="${UserDictionary.findAllByType(UserDictionary.UserDictionaryType.DRUG)}"
                              value="${reportRequestInstance?.drugCode}"
                              optionKey="name"
                              optionValue="name"
                              data-type="${UserDictionary.UserDictionaryType.DRUG.name()}"
                              class="form-control select2-box"/>
                </div>
                <div class="col-xs-3 aggregateReportInformation">

                    <label><g:message default="IBD" code="app.label.reportRequest.ibd"/></label>
                    <div class="fuelux">
                        <div>
                            <div class="datepicker reportSelectionDate toolbarInline" id="ibdDiv">
                                <div class="input-group">
                                    <g:textField id="ibd" placeholder="${message(code: "select.date")}" class="form-control fuelux date reportRequest" name="ibd"
                                                 value="${renderShortFormattedDate(date: reportRequestInstance?.ibd)}"/>
                                    <g:render id="ibdin" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <label class="add-margin-bottom"><g:message code="app.label.reportingDestinations"/><span class="required-indicator">*</span></label>
                    <div class="destinations">
                        <g:hiddenField name="primaryReportingDestination" value="${reportRequestInstance?.primaryReportingDestination}"/>
                        <g:select name="reportingDestinations"
                                       from="${[]}"
                                       value="${reportRequestInstance.allReportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                       data-value="${reportRequestInstance.allReportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                       class="form-control" multiple="multiple"/>
                    </div>

                </div>

            </div>
            <g:render template="includes/customFields" model="[section: ReportRequestField.Section.BASE]"/>
        </div>

    </div>

    <g:render template="includes/reportConfigurationSection" model="[reportRequestInstance: reportRequestInstance]"/>
    <div class="rxmain-container-inner rxmain-container-top aggregateReportInformation">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message default="Schedule Information" code="app.label.reportRequest.scheduleInformation"/>
            </label>
        </div>
        <div class="rxmain-container-content rxmain-container-show" >
        <div id="schedulerTable">

            <div class="row" style="margin: 5px;">

                <div class="col-xs-2">

                    <label><g:message default="Reporting Period Start" code="app.label.reportRequest.reportingPeriodStart"/></label>
                    <div class="fuelux">
                        <div>
                            <div class="datepicker reportSelectionDate toolbarInline " id="reportingPeriodStartDiv">
                                <div class="input-group">
                                    <g:textField id="reportingPeriodStart" placeholder="${message(code: "select.date")}" class="form-control fuelux date reportRequest" name="reportingPeriodStart"
                                                 value="${renderShortFormattedDate(date: reportRequestInstance?.reportingPeriodStart)}"/>
                                    <g:render id="reportingPeriodStartin" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
                <div class="col-xs-4">

                    <label><g:message code="app.label.frequency"/></label>
                    <div class="row" style="padding: 0">
                        <div class="col-xs-8">
                            <g:select name="frequency"
                                      from="${ReportRequestFrequencyEnum.forSelect()}"
                                      optionValue="display" optionKey="name"
                                      value="${reportRequestInstance?.frequency}"
                                      class="form-control"/>
                        </div>
                        <div class="col-xs-1" style="margin-top: 7px;"><b>X:</b></div>
                        <div class="col-xs-3" style="padding: 0;">
                            <input class="form-control" width="30px" type="number" name="frequencyX" id="frequencyX" value="${reportRequestInstance?.frequencyX}">
                        </div>
                    </div>
                </div>
                <div class="col-xs-2">

                    <label><g:message default="Reporting Period End" code="app.label.reportRequest.reportingPeriodEnd"/></label>
                    <div class="fuelux">
                        <div>
                            <div class="datepicker reportSelectionDate toolbarInline " id="reportingPeriodEndDiv">
                                <div class="input-group">
                                    <g:textField id="reportingPeriodEnd" placeholder="${message(code: "select.date")}" class="form-control fuelux date reportRequest" name="reportingPeriodEnd"
                                                 value="${renderShortFormattedDate(date: reportRequestInstance?.reportingPeriodEnd)}"/>
                                    <g:render id="reportingPeriodEndin" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

                <div class="col-xs-4">
                    <div class="row">

                        <div class="col-xs-6">

                            <label><g:message default="End after X occurrences" code="app.label.reportRequest.occurrences"/></label>
                            <div>
                                <input name="occurrences" id="occurrences" class="form-control" type="number" min="1" value="${reportRequestInstance?.occurrences}">
                            </div>
                        </div>
                        <div class="col-xs-6">

                            <label><g:message default="Due In Post DLP (in days)" code="app.label.reportRequest.dueDateToHa"/></label>
                            <div>
                                <input name="dueInToHa" id="dueInToHa" class="form-control" type="number" min="1" value="${reportRequestInstance?.dueInToHa}">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
            <div class="row" style="margin: 5px; margin-top: 20px;">

                <g:set var="children" value="${reportRequestInstance?.id ? ReportRequest.findAllByParentReportRequest(reportRequestInstance?.id) : []}"/>
                <g:if test="${children?.size() > 0}">
                    <div class="col-xs-2">
                        <label><g:message default="Related (child) Report Request:" code="app.label.reportRequest.childReportRequest"/></label>
                    </div>
                    <div class="col-xs-10">
                        <g:each in="${children}" var="child">
                            <div id="parentLink">
                                <a href="${createLink(controller: 'reportRequest', action: 'show')}?id=${child.id}">(ID:${child.id}) ${child.reportName}</a>
                            </div>
                        </g:each>
                    </div>
                </g:if>
                <g:else>
                    <div class="col-xs-2">
                        <label><g:message default="Parent Report Request:" code="app.label.reportRequest.parentReportRequest"/></label>
                    </div>
                    <div class="col-xs-2">

                        <g:if test="${reportRequestInstance?.parentReportRequest}">
                            <g:set var="parent" value="${ReportRequest.get(reportRequestInstance?.parentReportRequest)}"/>
                            <div id="parentLink">
                                <a href="${createLink(controller: 'reportRequest', action: 'show')}?id=${parent.id}">(ID:${parent.id}) ${parent.reportName}</a>
                            </div>
                        </g:if>
                        <g:else>
                            <div id="parentLink">
                                <g:message default="No Parent Report Request" code="app.label.reportRequest.noParentReportRequest"/>
                            </div>
                        </g:else>

                    </div>

                    <div class="col-xs-1">
                        <input type="button" value="Remove Parent" class="btn btn-primary btn-xs remooveParent"/>
                    </div>
                    <div class="col-xs-1">

                    </div>

                    <input type="hidden" name="parentReportRequest" id="parentReportRequest" value=" ${reportRequestInstance?.parentReportRequest}">
                </g:else>
            </div>
        </sec:ifAnyGranted>
        <div id="parentList" style="margin: 5px;"></div>
        <g:render template="includes/customFields" model="[section: ReportRequestField.Section.SCHEDULE]"/>
        </div>
    </div>
    <div class="rxmain-container-inner rxmain-container-top aggregateReportInformation">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.reportRequest.additionalInformation"/>
            </label>

        </div>
        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-md-3">

                    <label><g:message code="app.label.reportRequest.currentPeriodDueDateToHa"/></label>
                    <input readonly="readonly" id="curPrdDueDate" name="curPrdDueDate" value="${renderShortFormattedDate(date: reportRequestInstance?.curPrdDueDate)}" class="form-control"/>
                </div>
                <div class="col-md-3">
                    <label><g:message code="app.label.reportRequest.periodCoveredByReport"/></label>
                    <div>
                        <input id="periodCoveredByReport1" disabled class="form-control" value="${reportRequestInstance?.periodCoveredByReport}">
                        <input type="hidden" name="periodCoveredByReport" id="periodCoveredByReport2" value="${reportRequestInstance?.periodCoveredByReport}">
                    </div>
                </div>
                <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
                    <div class="col-md-3 masterPlanningRequest">
                        <label><g:message code="app.label.reportRequest.dueDateForDistribution"/></label>
                        <div class="fuelux">
                            <div>
                                <div class="datepicker reportSelectionDate toolbarInline " id="dueDateForDistributionDiv">
                                    <div class="input-group">
                                        <g:textField id="dueDateForDistribution" placeholder="${message(code: "select.date")}" class="form-control fuelux date reportRequest" name="dueDateForDistribution"
                                                     value="${renderShortFormattedDate(date: reportRequestInstance?.dueDateForDistribution)}"/>
                                        <g:render id="dueDateForDistributionin" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </sec:ifAnyGranted>
                <div class="col-xs-3">
                    <label ><g:message code="app.publisher.publisherContributors" default="Publisher Contributors"/></label>
                    <div class="destinations">
                        <g:hiddenField name="primaryPublisherContributor" value="${reportRequestInstance?.primaryPublisherContributor?.id}"/>
                        <g:select name="publisherContributors" from="${[]}"
                                       value="${reportRequestInstance.allPublisherContributors?.collect{it.id}?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                       data-value="${reportRequestInstance.allPublisherContributors?.collect{it.id}?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                       class="form-control" multiple="multiple"/>
                    </div>
                </div>
            </div>
%{--            <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">--}%
%{--                <div class="row masterPlanningRequest">--}%
%{--                    <div class="col-md-3">--}%
%{--                        <label><g:message code="app.label.reportRequest.previousPeriodStart"/></label>--}%
%{--                        <div>--}%
%{--                            <g:textField class="form-control " name="previousPeriodStart" disabled="disabled" value="${renderShortFormattedDate(date: reportRequestInstance?.previousPeriodStart)}"/>--}%
%{--                        </div>--}%
%{--                    </div>--}%
%{--                    <div class="col-md-3">--}%
%{--                        <label><g:message code="app.label.reportRequest.previousPeriodEnd"/></label>--}%
%{--                        <div>--}%
%{--                            <g:textField class="form-control " name="previousPeriodEnd" disabled="disabled" value="${renderShortFormattedDate(date: reportRequestInstance?.previousPeriodEnd)}"/>--}%
%{--                        </div>--}%
%{--                    </div>--}%
%{--                    <div class="col-md-3">--}%
%{--                        <label><g:message code="app.label.reportRequest.previousPsrTypeFile"/></label>--}%
%{--                        <div>--}%
%{--                            <input id="previousPsrTypeFile" name="previousPsrTypeFile" disabled class="form-control" value="${reportRequestInstance?.previousPsrTypeFile}">--}%
%{--                        </div>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--            </sec:ifAnyGranted>--}%

            <g:render template="includes/customFields" model="[section: ReportRequestField.Section.ADDITIONAL]"/>
        </div>
    </div>
    <div class="rxmain-container-inner rxmain-container-top">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="report.submission.comment"/>
            </label>
            <span class="add-comment reportRequest create-action-item" style="float:right;cursor:pointer;color: #428bca"><g:message code="app.label.comment.add"/></span>
        </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div class="reportCommentDiv associationsDiv">
                <g:each var="commentObj" in="${comments}" status="i">

                    <div class="panelDiv" data-id="${i}" style="display:${commentObj.isDeleted ? 'none' : 'block'}">
                        <div class="panel panel-default">
                            <div>
                                <strong>${commentObj?.createdBy}</strong> <span class="test-muted"><g:message code="reportRequest.comment.date.label" args="[renderLongFormattedDate(date: commentObj?.dateCreated, timeZone: userTimeZone)]"/></span>

                                <g:showIfLoggedInUserSame userName="${commentObj?.createdBy}">
                                    <div style="float: right; cursor: pointer" class="deleteComment">
                                        <span class="glyphicon glyphicon-trash reportRequest deleteIcon reportRequest" data-name="COMMENT"></span>
                                    </div>
                                    <div style="float: right; cursor: pointer" class="editComment">
                                        <span class="glyphicon glyphicon-edit editComment reportRequest" data-name="COMMENT"></span>
                                    </div>
                                </g:showIfLoggedInUserSame>

                            </div>
                            <div style="">
                                <g:hiddenField class="commentId" name='comments[${i}].id' value="${commentObj?.id}"/>
                                <g:hiddenField class="commentDeleted" name='comments[${i}].deleted' value='${commentObj?.isDeleted}'/>
                                <g:hiddenField class="commentNew" name='comments[${i}].newObj' value='${commentObj?.newObj}'/>
                                <g:hiddenField class="commentDateCreated" name="comments[${i}].dateCreated" value="${formatDate(date: commentObj?.dateCreated, timeZone: userTimeZone, format: DateUtil.DATEPICKER_FORMAT_AM_PM)}"/>
                                <g:hiddenField class="reportComment" id="comments[${i}].reportComment" name='comments[${i}].reportComment' value='${commentObj.reportComment}'/>
                                <span id="comments[${i}].display">
                                    <g:applyCodec encodeAs="none">
                                        ${commentObj.reportComment?.replaceAll("  ", "&nbsp; ")?.replaceAll("\n", "<br>")}
                                    </g:applyCodec>
                                </span>
                            </div>
                        </div>
                    </div>
                </g:each>
                <g:if test="${!comments}">
                    <span class="noCommentSpan noAssociations"><g:message code="comment.list.empty.message"/></span>
                </g:if>
            </div>
            <g:render template="includes/customFields" model="[section: ReportRequestField.Section.COMMENTS]"/>
        </div>
    </div>

        <div class="rxmain-container-inner rxmain-container-top">

            <div class="rxmain-container-row rxmain-container-header subTasks">
                <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.action.app.name"/>
                </label>
            <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
                <div style="float:right" id="createActionItemObj"><span style="color:#428bca" id="createActionItem" class="actionItemId create-action-item" data-toggle="actionItemModal" data-target="#actionItemModal">
                    <span><g:message code="app.label.action.item.create"/></span>
                </span></div>
            </sec:ifAnyGranted>

        </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div>
                <table style="width:100%">
                    <tr>
                        <td style="width:10%">
                            <label style="color:#4d4d4d;font-weight: bold">
                                <g:message code="app.label.add.task.templates"/>
                            </label>
                        </td>
                        <td style="width:20%">
                            <g:select style="width:100%" id="taskTemplate" name="taskTemplate" from="${taskTemplates}"
                                      optionKey="id" optionValue="name" noSelection="['': message(code: 'placeholder.taskTemplate.label')]" class="taskTemplate select2-box form-control"/>
                        </td>
                        <td style="width:1%"><span>&nbsp;</span></td>
                        <td>
                            <button type="button" class="btn btn-default addTaskFromTemplate"><g:message code="app.add.button.label"/></button>
                        </td>
                        <td><span id="noDueDate" class="hide" style="color:red;"></span></td>
                    </tr>
                </table>
            </div>
            <br/>
            <br/>
            <div class="reportActionItemDiv associationsDiv action-item-panel">
                <g:each var="actionItemObj" in="${actionItems}" status="i">
                    <div class="panelDiv" data-id="${i}">
                        <div class="panel panel-default">

                            <div>
                                <strong class="assignedTo"><g:message code="actionItem.assigned.to.label"/> : ${actionItemObj?.assignedToName()}</strong>
                                <span class="text-muted"><g:message code="actionItem.with.due.date.on.label"/> : ${renderShortFormattedDate(date: actionItemObj?.dueDate)}</span>

                                <div style="float: right; cursor: pointer">
                                    <span class="glyphicon glyphicon-trash reportRequest deleteIcon reportRequest" data-name="ACTIONITEM"></span>
                                </div>
                                <div style="float: right; cursor: pointer" data-id="${actionItemObj?.id}">
                                    <span class="glyphicon glyphicon-edit reportRequest editActionItem" data-name="ACTIONITEM"></span>
                                </div>

                                <div style="float: right" class="statusIcons">
                                    <g:if test="${actionItemObj?.status == StatusEnum.OPEN}">
                                        <span class="statusString"
                                              style="color:blue; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>

                                    <g:if test="${actionItemObj?.status in [StatusEnum.IN_PROGRESS, StatusEnum.NEED_CLARIFICATION]}">
                                        <span class="statusString"
                                              style="color:orange; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>

                                    <g:if test="${actionItemObj?.status == StatusEnum.CLOSED}">
                                        <span class="statusString"
                                              style="color:green; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>
                                </div>

                            </div>

                             <div style="min-height:70px;height: auto;width: 80%" >
                                 <input type="hidden" class="actionItemId reportRequest" id='actionItems[${i}].id' name='actionItems[${i}].id' value="${actionItemObj?.id}"/>
                                 <input type="hidden" class="actionItemActionCategory reportRequest" id='actionItems[${i}].actionCategory' name='actionItems[${i}].actionCategory' value="REPORT_REQUEST" />
                                 <input type="hidden" class="actionItemPriority reportRequest" id='actionItems[${i}].priority' name='actionItems[${i}].priority' value="${actionItemObj?.priority}" />
                                 <input type="hidden" class="actionItemStatus reportRequest" id='actionItems[${i}].status' name='actionItems[${i}].status' value="${actionItemObj?.status}" />
                                 <g:if test="${actionItemObj?.assignedTo}">
                                     <input type="hidden" class="actionItemAssignedTo reportRequest" id='actionItems[${i}].assignedTo' name='actionItems[${i}].assignedTo' value="User_${actionItemObj?.assignedTo?.id}" />
                                 </g:if>
                                 <g:else>
                                     <input type="hidden" class="actionItemAssignedTo reportRequest" id='actionItems[${i}].assignedTo'  name='actionItems[${i}].assignedTo' value="UserGroup_${actionItemObj?.assignedGroupTo?.id}" />
                                 </g:else>
                                 <input type="hidden" class="actionItemCompletionDate reportRequest" id='actionItems[${i}].completionDate' name='actionItems[${i}].completionDate' value="${actionItemObj?.completionDate}" />
                                 <input type="hidden" class="actionItemDueDate reportRequest" id='actionItems[${i}].dueDate' name='actionItems[${i}].dueDate' value="${DateUtil.toDateString(actionItemObj?.dueDate, DateUtil.DATEPICKER_FORMAT)}" />
                                 <input type="hidden" class="actionItemDeleted reportRequest" id='actionItems[${i}].deleted' name='actionItems[${i}].deleted' value="false"/>
                                 <input type="hidden" class="actionItemDateCreated reportRequest" id="actionItems[${i}].dateCreatedObj" id="actionItems[${i}].dateCreatedObj" value="${formatDate(date: actionItemObj?.dateCreated, format: DateUtil.DATEPICKER_FORMAT_AM_PM)}"/>
                                 <g:if test="${!actionItemObj?.id}">
                                     <input type="hidden" class="actionItemNew reportRequest" id='actionItems[${i}].newObj' name='actionItems[${i}].newObj' value="true"/>
                                 </g:if>
                                 <g:else>
                                     <input type="hidden" class="actionItemNew reportRequest" id='actionItems[${i}].newObj' name='actionItems[${i}].newObj' value="false"/>
                                 </g:else>
                                 <input type="hidden" class="actionItemDescription reportRequest" id='actionItems[${i}].description' name='actionItems[${i}].description' value="${actionItemObj.description}" />
                                 <span id="actionItems[${i}].display" style="word-wrap: break-word">
                                 <g:applyCodec encodeAs="NONE">
                                     ${actionItemObj.description?.replaceAll("  ","&nbsp; ")?.replaceAll("\n","<br>")?.encodeAsHTML()}
                                 </g:applyCodec></span>
                             </div>
                         </div>
                     </div>
                </g:each>
                <g:if test="${!actionItems}">
                    <span class="noActionItem noAssociations"><g:message code="app.label.no.actions"/></span>
                </g:if>
            </div>

        </div>
    </div>

        <div class="rxmain-container-inner rxmain-container-top">

            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.attachments" />
                </label>
            </div>

            <div class="rxmain-container-content rxmain-container-show">
                <g:each var="attachment" in="${reportRequestInstance?.attachments}" status="i">
                    <div style="margin-top: 5px;">
                        <div>
                            <strong>${attachment?.createdBy}</strong><span class="test-muted"> <g:message code="reportRequest.attachment.date.label" args="[formatDate(date: attachment?.dateCreated, timeZone: userTimeZone, format: DateUtil.DATEPICKER_FORMAT_AM_PM)]"/></span>
                        </div>
                        <a href="javascript:void(0)" class="btn btn-xs btn-primary deleteAttachment" data-id="${attachment.id}"><span class="fa fa-remove" title="${message(code: 'app.delete.button.label')}"></span> </a>
                        <a href="${createLink(controller: 'reportRequest', action: 'downloadAttachment')}?id=${attachment.id}" class="btn btn-xs btn-primary"><span class="fa fa-download" title="${message(code: 'app.label.download')}"></span></a> ${com.rxlogix.RxCodec.decode(attachment.name)}
                    </div>

                </g:each>
                <g:if test="${showAttachmentWarning}">
                    <div class="alert alert-warning alert-dismissible forceLineWrap" role="alert">
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <i class="fa fa-warning"></i>
                        <g:message code="app.reportRequest.attachment.warning"/>
                    </div>
                </g:if>
                <div class="input-group" style="width: 400px;margin-top: 30px;">
                    <input type="text" class="form-control" id="file_name" readonly>
                    <label class="input-group-btn">
                        <span class="btn btn-primary">
                            <g:message code="app.label.attach"/>
                            <input type="file" id="file_input" name="file" accept="image/*, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/json, application/pdf, application/msword, application/vnd.ms-powerpoint, text/plain" multiple  style="display: none;">
                        </span>
                    </label>
                </div>
                <div id="file_Status"></div>
                <input type="hidden" name="attachmentsToDelete" id="attachmentsToDelete" value="">
            </div>
        </div>

    %{--lnked reports--}%
    <div class="rxmain-container-inner rxmain-container-top">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.reportRequest.linked" />
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div id="RRlinkList">
                <g:each var="link" in="${linkedReports}" status="i">
                    <div style="margin-top: 5px;">
                        <a href="javascript:void(0)" class="btn btn-xs btn-primary deleteLink" data-id="${link.id}"><span class="fa fa-remove" title="${message(code: 'app.delete.button.label')}"></span>
                        </a>
                        <b>${link.linkType.name.encodeAsHTML()}</b>
                        <g:if test="${link.to.id != reportRequestInstance.id}">
                            <span class="glyphicon glyphicon-arrow-right"></span>
                            <a href="${createLink(controller: 'reportRequest', action: 'show')}?id=${link.to.id}">${link.to.id} ${link.to.reportName.encodeAsHTML()}</a>
                        </g:if>
                        <g:else>
                            <span class="glyphicon glyphicon-arrow-left"></span>
                            <a href="${createLink(controller: 'reportRequest', action: 'show')}?id=${link.from.id}">${link.from.id} ${link.from.reportName.encodeAsHTML()}</a>
                        </g:else>
                        <g:applyCodec encodeAs="none">
                            ${link.description?.replaceAll("  ", "&nbsp; ")?.replaceAll("\n", "<br>")?.encodeAsHTML()}
                        </g:applyCodec>

                    </div>

                </g:each>
            </div>
            <g:if test="${showLinkWarning}">
                <div class="alert alert-warning alert-dismissible forceLineWrap" role="alert">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <i class="fa fa-warning"></i>
                    <g:message code="app.reportRequest.link.warning"/>
                </div>
            </g:if>
            <br>
            <input type="button" class="btn btn-primary" data-toggle="modal" data-target="#reportRequestLinkModal" value="${message(code: 'default.button.create.label')}"/>

            <input type="hidden" name="linksToAdd" id="linksToAdd" value="${linksToAdd ?: ''}">
            <input type="hidden" name="linksToDelete" id="linksToDelete" value="${linksToDelete ?: ''}">
        </div>
    </div>

    <div class="rxmain-container-inner rxmain-container-top">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message default="Linked Configurations" code="app.label.reportRequest.linkedConfigurations"/>
            </label>
        </div>
        <div class="rxmain-container-content rxmain-container-show">
        <div class="row">
            <div class="col-md-6" style="margin-left: 20px;">
                <select id="configurationDropdown" class="form-control"></select>
                <g:hiddenField name="linkedConfigurations" value="${reportRequestInstance?.linkedConfigurations}"/>
            </div>
            <div class="col-md-1">
                <button type="button" class="btn btn-default linkConfiguration"><g:message default="Link" code="app.label.reportRequest.linkConfiguration"/></button>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6 " id="linkedConfigurationsDiv">
            </div>
        </div>
        </div>
    </div>

    <div class="rxmain-container-inner rxmain-container-top">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message default="Linked Generated Reports" code="app.label.reportRequest.linkedGeneratedReports"/>
            </label>
        </div>
        <div class="rxmain-container-content rxmain-container-show">
        <div class="row">
            <div class="col-md-6" style="margin-left: 20px;">
                <select id="generatedReportsDropdown" class="form-control"></select>
                <g:hiddenField name="linkedGeneratedReports" value="${reportRequestInstance?.linkedGeneratedReports}"/>
            </div>
            <div class="col-md-1">
                <button type="button" class="btn btn-default linkedGeneratedReport"><g:message default="Link" code="app.label.reportRequest.linkConfiguration"/></button>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6 " id="linkedGeneratedReportsDiv">
            </div>
        </div>
        </div>
    </div>
</div>




<div id="loggedInUser" class="hide">${sec.loggedInUserInfo(field: "fullName")}</div>
<g:hiddenField name="mode" value="${mode}" id="mode"/>
<g:hiddenField id="commentSize" name="commentSize" value="${reportRequestInstance?.comments?.size()}"/>
<g:hiddenField id="actionItems" name="actionItems" value="${actionItems?.size()}"/>
<g:hiddenField id="currentDate" name="currentDate"
               value="${formatDate(date: new Date(), timeZone: userTimeZone, format: DateUtil.DATEPICKER_FORMAT_AM_PM)}"/>
<g:hiddenField id="dueDateHidden" name="dueDateHidden"
               value="${renderShortFormattedDate(date: reportRequestInstance?.dueDate)}"/>
<g:hiddenField id="startDateHidden" name="startDateHidden"
               value="${renderShortFormattedDate(date: reportRequestInstance?.startDate)}"/>
<g:hiddenField id="endDateHidden" name="endDateHidden"
               value="${renderShortFormattedDate(date: reportRequestInstance?.endDate)}"/>
<g:hiddenField id="completionDateHidden" name="completionDateHidden"
               value="${renderShortFormattedDate(date: reportRequestInstance?.completionDate)}"/>
<g:hiddenField id="asOfVersionDateHidden" name="asOfVersionDateHidden"
               value="${renderShortFormattedDate(date: reportRequestInstance?.asOfVersionDate)}"/>
<g:render template="includes/linkModal" model="[reportRequestId: reportRequestInstance?.id, linkType: linkType]"/>

<div class="modal fade" id="errorModal" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">Error!</h4>
            </div>

            <div class="modal-body">

                <div class="description" style="font-weight:bold;"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                        code="default.button.ok.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->