<%@ page import="com.rxlogix.Constants; grails.util.Holders; com.rxlogix.RxCodec; com.rxlogix.config.ReportRequestField; com.rxlogix.config.ReportRequest; grails.converters.JSON; com.rxlogix.enums.StatusEnum; com.rxlogix.util.DateUtil; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper" contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <g:javascript>
        var findTaskUrl = "${createLink(controller: 'reportRequest', action: 'findTasks')}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}"
        var reportRequestDropdownURL = "${createLink(controller: "reportRequestRest", action: "reportRequestDropdownList")}"
    </g:javascript>

    <meta name="layout" content="main">
    <asset:javascript src="app/reportRequest.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:javascript src="app/reportRequestActionItems.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <g:set var="entityName" value="Request Report"/>
    <title><g:message code="app.task.reportRequest.show.title"/></title>
</head>

<body>
<g:set var="userTimeZone" value="${getCurrentUserTimezone()}"/>
<div class="col-md-12">
    <rx:container title="${message(code: 'app.label.viewReportRequest')}">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestInstance}" var="theInstance"/>
        <div class="alert alert-success alert-dismissible reportRequestAlert m-t-4" role="alert" hidden="hidden">
            <button type="button" class="close">
                <span aria-hidden="true">&times;</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <div id="successMessage"></div>
        </div>

        <div class="container-fluid">
            <div class="row rxDetailsBorder">
                <div class="col-xs-12">
                    <label><g:message code="app.label.report.request"/></label>
                </div>
            </div>

    <div class="row">
        <div class="col-xs-12">
            <div class="row">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.report.request.name"/></label>

                            <div class="word-wrapper"><g:applyCodec encodeAs="HTML">${reportRequestInstance.reportName}</g:applyCodec></div>
                        </div>
                    </div>
                    <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
                        <div class="row">
                            <div class="col-xs-12">
                                <label>
                                    <g:message code="app.label.reportRequest.masterPlanningRequest"/>
                                </label>
                                <div><g:formatBoolean boolean="${reportRequestInstance?.masterPlanningRequest}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>

                            </div></div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message default="Type & Category of Report" code="app.label.reportRequest.psrTypeFile"/></label>
                                ${reportRequestInstance?.psrTypeFile}

                            </div></div>
                    </sec:ifAnyGranted>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.description"/></label>
                            <div class="word-wrapper">
                                <g:textArea class="form-control reportRequest transparent" disabled="disabled" name="description" value="${reportRequestInstance?.description}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.SuspectProduct"/></label>

                            <div><g:formatBoolean boolean="${reportRequestInstance?.suspectProduct}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/></div>
                        </div>
                    </div>

                            <g:if test="${reportRequestInstance?.productSelection || reportRequestInstance.validProductGroupSelection || reportRequestInstance?.studySelection}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <g:if test="${reportRequestInstance.productSelection || reportRequestInstance.validProductGroupSelection}">
                                            <label><g:message code="app.productDictionary.label"/></label>

                                            <div id="showProductSelection"></div>
                                            ${com.rxlogix.util.ViewHelper.getDictionaryValues(reportRequestInstance, com.rxlogix.enums.DictionaryTypeEnum.PRODUCT)}
                                        </g:if>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>
                                            <g:message code="app.label.productDictionary.include.who.drugs"/>
                                        </label>

                                        <div><g:formatBoolean boolean="${reportRequestInstance?.includeWHODrugs}"
                                                              true="${message(code: "default.button.yes.label")}"
                                                              false="${message(code: "default.button.no.label")}"/></div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>
                                            <g:if test="${Holders.config.safety.source == Constants.PVCM}">
                                                <g:message code="app.label.productDictionary.multi.substance"/>
                                            </g:if>
                                            <g:else>
                                                <g:message code="app.label.productDictionary.multi.ingredient"/>
                                            </g:else>
                                        </label>
                                        <div>
                                            <g:formatBoolean boolean="${reportRequestInstance?.isMultiIngredient}"
                                                             true="${message(code: "default.button.yes.label")}"
                                                             false="${message(code: "default.button.no.label")}"/>
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-xs-12">
                                        <g:if test="${reportRequestInstance.studySelection}">
                                            <label><g:message code="app.studyDictionary.label"/></label>

                                    <div id="showStudySelection"></div>
                                    ${ViewHelper.getDictionaryValues(reportRequestInstance, DictionaryTypeEnum.STUDY)}
                                </g:if>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.productSelection"/></label>
                                <div></div>
                                <g:message code="app.label.none"/>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.studySelection"/></label>
                                <div></div>
                                <g:message code="app.label.none"/>
                            </div>
                        </div>
                    </g:else>
                    <g:set var="requestorNames" value="${reportRequestInstance.allRequestorNames}"/>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.report.request.request.by"/></label>
                            <div>${requestorNames ? requestorNames.join(', ')?.encodeAsHTML() : ''}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.reportRequest.assigned.to"/></label>
                            <div>${reportRequestInstance?.assignedToName()}</div>
                        </div>
                    </div>
                    <g:if test="${requestorNotesVisible}">
                        <div class="row">
                            <div class="col-xs-6">
                                <label><g:message default="Requestor Notes" code="app.label.reportRequest.requestorNotes"/></label>
                                <div>${reportRequestInstance?.requestorNotes}</div>
                            </div>
                        </div>
                    </g:if>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.reportRequest.request.type"/></label>

                            <div>${reportRequestInstance?.reportRequestType?.name}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.report.request.priority"/></label>
                            <div>${reportRequestInstance?.priority?.name}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.report.request.status"/></label>

                            <div>${reportRequestInstance?.workflowState?.name}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.eventSelection.limit.primary.path"/></label>
                            <div>
                                <g:formatBoolean boolean="${reportRequestInstance?.limitPrimaryPath}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.reportRequest.completion.date" /></label>
                                    <div><g:renderShortFormattedDate date="${reportRequestInstance?.completionDate}"/></div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.eventDictId.label"/></label>
                                    <g:if test="${reportRequestInstance.eventSelection || reportRequestInstance.validEventGroupSelection}">
                                        <div id="showEventSelection"></div>
                                        <g:hiddenField name="editable" id="editable" value="false"/>
                                        ${ViewHelper.getDictionaryValues(reportRequestInstance, DictionaryTypeEnum.EVENT)}
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>
                        </div>

                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.DateRangeType"/></label>
                                <g:if test="${reportRequestInstance.dateRangeType}">
                                    <div><g:message code="${reportRequestInstance.dateRangeType.i18nKey}"/></div>
                                </g:if>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="evaluate.on.label"/></label>

                                <div id="evaluateCaseDate">
                                    <g:message
                                            code="${(com.rxlogix.enums.EvaluateCaseDateEnum.(reportRequestInstance.evaluateDateAs).i18nKey)}"/>
                                    <g:if test="${reportRequestInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF}">
                                        <div>${renderShortFormattedDate(date: reportRequestInstance?.asOfVersionDate)}</div>

                                </g:if>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.report.request.dueDate.label"/></label>
                            <div><g:renderShortFormattedDate date="${reportRequestInstance?.dueDate}"/></div>
                        </div>
                    </div>
                    <g:if test="${reportRequestInstance?.reportRequestType?.aggregate}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message default="INN" code="app.label.reportRequest.inn"/></label>
                                <div>${reportRequestInstance?.inn}</div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message default="Type & Category of Report" code="app.label.reportRequest.psrTypeFile"/></label>
                                <div>${reportRequestInstance?.psrTypeFile}</div>
                            </div>
                        </div>
                        <div class="row"><div class="col-xs-12">
                            <label><g:message default="Drug Code" code="app.label.reportRequest.drugCode"/></label>
                            <div>${reportRequestInstance?.drugCode}</div>
                        </div></div>
                        <div class="row"><div class="col-xs-12">
                            <label><g:message default="IBD" code="app.label.reportRequest.ibd"/></label>
                            <div><g:renderShortFormattedDate date="${reportRequestInstance?.ibd}"/></div>
                        </div></div> <div class="row"><div class="col-xs-12">
                        <label><g:message code="app.label.primaryReportingDestination"/></label>

                        <div>${reportRequestInstance?.primaryReportingDestination}</div>
                    </div></div> <div class="row"><div class="col-xs-12">
                        <label><g:message code="app.label.reportingDestinations"/></label>
                        <g:each in="${reportRequestInstance.reportingDestinations}" var="reportingDestination">
                            <div>${reportingDestination}</div>
                        </g:each>
                    </div></div>
                    </g:if>
                    <g:else>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="select.report.start.date" /></label>
                                <div><g:renderShortFormattedDate date="${reportRequestInstance?.startDate}"/></div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="select.report.end.date" /></label>
                                <div><g:renderShortFormattedDate date="${reportRequestInstance?.endDate}"/></div>
                            </div>
                        </div>
                    </g:else>

                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="reportCriteria.exclude.follow.up"/></label>

                            <div>
                                <g:formatBoolean boolean="${reportRequestInstance?.excludeFollowUp}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="reportCriteria.include.locked.versions.only"/></label>

                            <div><g:formatBoolean boolean="${reportRequestInstance?.includeLockedVersion}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="reportCriteria.include.all.study.drugs.cases"/></label>

                            <div><g:formatBoolean boolean="${reportRequestInstance?.includeAllStudyDrugsCases}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="reportCriteria.exclude.non.valid.cases"/></label>

                            <div><g:formatBoolean boolean="${reportRequestInstance?.excludeNonValidCases}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="reportCriteria.exclude.deleted.cases"/></label>

                            <div><g:formatBoolean boolean="${reportRequestInstance?.excludeDeletedCases}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="reportCriteria.include.medically.confirm.cases"/></label>
                            <div>
                                <g:formatBoolean boolean="${reportRequestInstance?.includeMedicallyConfirmedCases}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>
                    </div>

                </div>

            </div>
        </div>
        <g:render template="includes/customFieldView" model="[section: ReportRequestField.Section.BASE]"/>
        <g:render template="includes/customFieldView" model="[section: ReportRequestField.Section.SELECTION]"/>
    </div>

    <g:if test="${reportRequestInstance?.reportRequestType?.aggregate}">
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message default="Schedule Information" code="app.label.reportRequest.scheduleInformation"/></label>
            </div>
        </div>

        <div id="schedulerTable">

            <div class="row" style="margin: 5px;">

                <div class="col-xs-2">

                    <label><g:message default="Reporting Period Start" code="app.label.reportRequest.reportingPeriodStart"/></label>
                    <div><g:renderShortFormattedDate date="${reportRequestInstance?.reportingPeriodStart}"/></div>
                </div>
                <div class="col-xs-2">

                    <label><g:message code="app.label.frequency"/></label>
                    <div>
                        <g:message code="${reportRequestInstance?.frequency?.getI18nKey()}"/>${reportRequestInstance?.frequencyX > 0 ? "X: " + reportRequestInstance?.frequencyX : ""}

                    </div>
                </div>
                <div class="col-xs-2">

                    <label><g:message default="Reporting Period End" code="app.label.reportRequest.reportingPeriodEnd"/></label>
                    <div><g:renderShortFormattedDate date="${reportRequestInstance?.reportingPeriodEnd}"/></div>
                </div>

                <div class="col-xs-2">

                    <label><g:message default="End after X occurrences" code="app.label.reportRequest.occurrences"/></label>
                    <div>${reportRequestInstance?.occurrences}</div>
                </div>
                <div class="col-xs-2">
                    <label><g:message default="Due-in to HA (in days)" code="app.label.reportRequest.dueDateToHa"/></label>
                    <div>${reportRequestInstance?.dueInToHa}</div>
                </div>
            </div>
        </div>
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


                <input type="hidden" name="parentReportRequest" id="parentReportRequest" value=" ${reportRequestInstance?.parentReportRequest}">
            </g:else>
        </div>

        <div id="parentList" style="margin: 5px;"></div>
        <g:render template="includes/customFieldView" model="[section: ReportRequestField.Section.SCHEDULE]"/>
        <div class="row rxDetailsBorder">
            <div class="col-xs-12"><label>
                <g:message code="app.label.reportRequest.additionalInformation"/>
            </label>

            </div>
        </div>
        <div>
            <div class="row">

                <div class="col-md-3">

                    <label><g:message code="app.label.reportRequest.currentPeriodDueDateToHa"/></label>
                    <div>${renderShortFormattedDate(date: reportRequestInstance?.curPrdDueDate)}</div>
                </div>
                <div class="col-md-3">
                    <label><g:message code="app.label.reportRequest.periodCoveredByReport"/></label>
                    <div>
                        ${reportRequestInstance?.periodCoveredByReport}
                    </div>
                </div>
                <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
                    <g:if test="${reportRequestInstance?.masterPlanningRequest}">
                        <div class="row masterPlanningRequest">
                            <div class="col-md-3">
                                <label><g:message code="app.label.reportRequest.dueDateForDistribution"/></label>
                                <div>
                                    <g:renderShortFormattedDate date="${reportRequestInstance?.dueDateForDistribution}"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                </sec:ifAnyGranted>
            </div>
            <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_PLANNING_TEAM">
                <g:if test="${reportRequestInstance?.masterPlanningRequest}">
                    <div class="row masterPlanningRequest">
                        <div class="col-md-3">
                            <label>Previous Period Start Date</label>
                            <div>
                                <g:renderShortFormattedDate date="${reportRequestInstance?.previousPeriodStart}"/>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <label>Previous Period End Date</label>
                            <div>
                                <g:renderShortFormattedDate date="${reportRequestInstance?.previousPeriodEnd}"/>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <label>Previous Period Type Category
                            </label>
                            <div>
                                ${reportRequestInstance?.previousPsrTypeFile}
                            </div>
                        </div>
                    </div>
                </g:if>
            </sec:ifAnyGranted>
            <g:render template="includes/customFieldView" model="[section: ReportRequestField.Section.ADDITIONAL]"/>
        </div>

    </g:if>


    <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message code="app.report.submission.comment.label"/></label>
            <span class="add-comment reportRequest create-action-item" style="float:right;cursor:pointer;color: #428bca"><g:message code="app.label.comment.add"/></span>
        </div>
    </div>
    <g:form action="updateComments">
        <g:hiddenField name="id" value="${reportRequestInstance.id}"/>
        <g:hiddenField name="version" value="${reportRequestInstance.version}"/>
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
        <g:hiddenField id="commentSize" name="commentSize" value="${reportRequestInstance?.comments?.size()}"/>
        <g:submitButton class="btn btn-primary" name="Update Comments" style="float: right">Update Comments</g:submitButton>
    </g:form>
        <g:render template="includes/customFieldView" model="[section: ReportRequestField.Section.COMMENTS]"/>
    <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message code="app.label.reportRequest.app.name"/></label>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-12">
            <div class="reportActionItemDiv associationsDiv">
                <g:each var="actionItemObj" in="${actionItems}" status="i">
                    <div style="width: 100%;">
                        <div class="panel panel-default">
                            <div>
                                <strong class="assignedTo"><g:message code="actionItem.assigned.to.label"/>: ${actionItemObj?.assignedToName().encodeAsHTML()}</strong>
                                <span class="test-muted"><g:message code="actionItem.with.due.date.on.label"/> :
                                <g:renderShortFormattedDate date="${actionItemObj?.dueDate}"/>
                                </span>
                                <div style="float: right" class="statusIcons">
                                    <g:if test="${actionItemObj?.status == StatusEnum.OPEN}">
                                        <span class="statusString" style="color:blue; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>

                                    <g:if test="${actionItemObj?.status == StatusEnum.IN_PROGRESS}">
                                        <span class="statusString" style="color:orange; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>

                                    <g:if test="${actionItemObj?.status == StatusEnum.NEED_CLARIFICATION}">
                                        <span class="statusString" style="color:orange; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>

                                    <g:if test="${actionItemObj?.status == StatusEnum.CLOSED}">
                                        <span class="statusString" style="color:green; font-style: italic;">${ViewHelper.getMessage(actionItemObj?.status?.getI18nKey())}</span><span>&nbsp;</span>
                                    </g:if>
                                </div>
                            </div>

                            <div style="min-height: 70px;height: auto;width: 80%">
                                <span style="word-wrap: break-word">
                                    <g:applyCodec encodeAs="HTML">${actionItemObj.description?.replaceAll("  ", "&nbsp; ")?.replaceAll("\n", "<br>")}
                                    </g:applyCodec>
                                </span>
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

    <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message code="app.label.attachments"/></label>
        </div>
    </div>
    <g:each var="attachment" in="${reportRequestInstance?.attachments}" status="i">
        <div>
            <strong>${attachment?.createdBy}</strong><span class="test-muted"><g:message code="reportRequest.attachment.date.label" args="[formatDate(date: attachment?.dateCreated, timeZone: userTimeZone, format: DateUtil.DATEPICKER_FORMAT_AM_PM)]"/></span>
        </div>
        <div style="margin-top: 5px;">
            <a href="${createLink(controller: 'reportRequest', action: 'downloadAttachment')}?id=${attachment.id}" class="btn btn-xs btn-primary"><span class="fa fa-download" title="${message(code: 'app.label.download')}"></span>
            </a> ${com.rxlogix.RxCodec.decode(attachment.name)}
        </div>
    </g:each>
    <g:if test="${!reportRequestInstance?.attachments}">
        <span class="noCommentSpan noAssociations"><g:message code="app.reportRequest.attachment.empty.message"/></span>
    </g:if>

    <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message code="app.label.reportRequest.linked"/></label>
        </div>
    </div>
    <g:each var="link" in="${linkedReports}" status="i">
        <div style="margin-top: 5px;">
            <b>${link.linkType.name}</b>
            <g:if test="${link.to.id != reportRequestInstance.id}">
                <span class="glyphicon glyphicon-arrow-right"></span>
                <a href="${createLink(controller: 'reportRequest', action: 'show')}?id=${link.to.id}">${link.to.id} ${link.to.reportName}</a>
            </g:if>
            <g:else>
                <span class="glyphicon glyphicon-arrow-left"></span>
                <a href="${createLink(controller: 'reportRequest', action: 'show')}?id=${link.from.id}">${link.from.id} ${link.from.reportName}</a>
            </g:else>
            <span>
                <g:applyCodec encodeAs="HTML">${link.description?.replaceAll("  ", "&nbsp; ")?.replaceAll("\n", "<br>")}
                </g:applyCodec>
            </span>
        </div>
    </g:each>
    <g:if test="${!linkedReports}">
        <span class="noCommentSpan noAssociations"><g:message code="app.reportRequest.link.empty.message"/></span>
    </g:if>

    <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message default="Linked Configurations" code="app.label.reportRequest.linkedConfigurations"/></label>
        </div>
    </div>
    <g:set var="linkedCfg" value="${reportRequestInstance?.linkedConfigurations ? JSON.parse(reportRequestInstance?.linkedConfigurations) : []}"/>
    <g:each var="link" in="${linkedCfg}" status="i">
        <div style="margin-top: 5px;">
            <a href="${createLink(controller: 'configurationRest', action: 'redirectViewConfiguration')}?id=${link.id}">${link.name}</a>
        </div>
    </g:each>
    <g:if test="${!linkedCfg}">
        <span class="noCommentSpan noAssociations"><g:message default="There are no linked Configurations" code="app.reportRequest.linkedConfigurations.empty.message"/></span>
    </g:if>

    <div class="row rxDetailsBorder">
        <div class="col-xs-12">
            <label><g:message default="Linked Generated Reports" code="app.label.reportRequest.linkedGeneratedReports"/></label>
        </div>
    </div>
    <g:set var="linkedRep" value="${reportRequestInstance?.linkedGeneratedReports ? JSON.parse(reportRequestInstance?.linkedGeneratedReports) : []}"/>
    <g:each var="link" in="${linkedRep}" status="i">
        <div style="margin-top: 5px;">
            <a href="${createLink(controller: 'report', action: 'showFirstSection')}?id=${link.id}">${link.name}</a>
        </div>
    </g:each>
    <g:if test="${!linkedRep}">
        <span class="noCommentSpan noAssociations"><g:message default="There are no linked Generated Reports" code="app.reportRequest.linkedGeneratedReports.empty.message"/></span>
    </g:if>


    <br><br>
        <div class="row">
    <div class="col-md-12 report_request_footer">
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${reportRequestInstance}" var="theInstance"/>
        </div>
        <div class="col-md-12">
        <div class="buttonBar" style="text-align: right">
            <div class="pull-right">
            <g:if test="${reportRequestInstance.isDeleted}">
                <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["reportRequest", "index"]}'
                        id="cancelButton">${message(code: "default.button.cancel.label")}</button>
            </g:if>
            <g:else>
                <g:if test="${editable}">
                    <g:link action="edit" id="${reportRequestInstance?.id ?: params.id}" class="btn btn-primary updateButton"><g:message code='default.button.edit.label'/></g:link>
                </g:if>
                <button type="button" class="btn btn-default" data-evt-clk='{"method": "goToUrl", "params": ["reportRequest", "index"]}'
                        id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                <g:link action="copy" id="${reportRequestInstance?.id ?: params.id}" class="btn btn-default">${message(code: "default.button.copy.label")}</g:link>
                <g:link action="copyNext" id="${reportRequestInstance?.id ?: params.id}" class="btn btn-default">Create for next period</g:link>
                <g:if test="${currentUserAssigned && editable}">
                    <g:if test="${reportRequestInstance.reportRequestType.aggregate}">
                        <g:link action="createReport" params="${[id: reportRequestInstance.id, configurationType: com.rxlogix.enums.ConfigurationTypeEnum.PERIODIC_REPORT.name()]}" class="btn btn-primary"><g:message code="app.label.createAggregateReport"/></g:link>
                    </g:if>
                    <g:else>
                        <g:link action="createReport" params="${[id: reportRequestInstance.id, configurationType: com.rxlogix.enums.ConfigurationTypeEnum.ADHOC_REPORT.name()]}" class="btn btn-primary"><g:message code="app.label.createAdhocReport"/></g:link>
                    </g:else>
                </g:if>
            </g:else>
        </div>
    </div>
<g:render template="includes/reportRequestComment" model="[]" />
<g:render template="/actionItem/includes/actionItemModal" model="[]" />
</rx:container>
</body>
</html>