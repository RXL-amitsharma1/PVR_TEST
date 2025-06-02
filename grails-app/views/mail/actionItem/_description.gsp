<%@ page import="com.rxlogix.enums.AppTypeEnum; com.rxlogix.util.DateUtil;com.rxlogix.util.ViewHelper" %>
<g:each var="actionItem" in="${actionItems}">
    <g:set var="actionItemDueDate"
           value="${actionItem?.dueDate instanceof Date ? actionItem?.dueDate : DateUtil.StringToDate(actionItem?.dueDate, DateUtil.DATEPICKER_UTC_FORMAT)}"/>
    <g:set var="actionItemCompletionDate"
           value="${actionItem?.completionDate instanceof Date ? actionItem?.completionDate : DateUtil.StringToDate(actionItem?.completionDate, DateUtil.DATEPICKER_UTC_FORMAT)}"/>

    <div class="row" style="width:100%">
        <div style="width:50%">
            <span><b><g:message code="app.label.action.item.action.category"/> :</b></span>
            <span>
                ${ViewHelper.getMessage(actionItem?.actionCategory?.getI18nKey())}
            </span>
        </div>

        <div style="width:50%">
            <span><b><g:message code="app.label.action.item.priority"/> :</b></span>
            <span>
                ${ViewHelper.getMessage("app.priority." + actionItem?.priority)}
            </span>
        </div>
    </div>

    <div class="row" style="width:100%">
        <div style="width:50%">
            <span><b><g:message code="app.label.action.item.due.date"/> :</b></span>
            <span>
                <g:renderShortFormattedDate date="${actionItemDueDate}"/>
            </span>
        </div>

        <div style="width:50%">
            <span><b><g:message code="app.label.action.item.completion.date"/> :</b></span>
            <span>
                <g:renderShortFormattedDate date="${actionItemCompletionDate}"/>
            </span>
        </div>
    </div>

    <div class="row" style="width:100%">
        <div style="width:100%">
            <span><b><g:message code="app.label.action.item.description"/> :</b></span>
            <span>
                ${actionItem?.description}
            </span>
        </div>
    </div>

    <div class="row" style="width:100%">
        <div style="width:100%">
            <span><b><g:message code="app.label.action.item.status"/> :</b></span>
            <span>
                ${ViewHelper.getMessage(actionItem?.status?.getI18nKey())}
            </span>
        </div>
    </div>
    <g:if test="${actionItem instanceof com.rxlogix.config.ActionItem && actionItem.appType == AppTypeEnum.REPORT_REQUEST}">
    <rx:showReportRequest actionItemId="${actionItem.id}" />
    </g:if>
    <g:if test="${actionItem instanceof com.rxlogix.config.ActionItem }">

        <g:if test="${actionItem.appType in [AppTypeEnum.ADHOC_REPORT, AppTypeEnum.PERIODIC_REPORT]}">
            <g:set var="executedReportConfiguration" value="${com.rxlogix.config.ExecutedReportConfiguration.getByActionItem(actionItem.id).get()}"/>
        <g:if test="${executedReportConfiguration}">
            <div class="row" style="width:100%">
                <div style="width:100%">
                    <span><b><g:message code="app.actionItem.associatedReport.label"/> :</b></span>
                    <span>
                        <a href="${grailsApplication.config.grails.appBaseURL}/report/showFirstSection/${executedReportConfiguration.id}" >${executedReportConfiguration.reportName}</a>
                    </span>
                </div>
            </div>
        </g:if>
        <g:if test="${actionItem.configuration}">
            <div class="row" style="width:100%">
                <div style="width:100%">
                    <span><b><g:message code="app.actionItem.associatedConfiguration.label"/> :</b></span>
                    <span>
                        <a href="${grailsApplication.config.grails.appBaseURL}/configuration/view/${actionItem.configuration.id}" >${actionItem.configuration.reportName}</a>
                    </span>
                </div>
            </div>
        </g:if>
        </g:if>
        <g:if test="${actionItem.appType == AppTypeEnum.DRILLDOWN_RECORD}">
            <g:set var="reasonOfDelayData" value="${actionItem.getReasonOfDelayData()}"/>
            <g:if test="${reasonOfDelayData?.drilldownRecordCaseNum}">
                <div class="row" style="width:100%">
                    <div style="width:100%">
                        <span><b><g:message code="app.actionItem.associatedDrilldown.label"/> :</b></span>
                        <span>
                            <a href="${grailsApplication.config.grails.appBaseURL}/advancedReportViewer/viewDelayReason/${reasonOfDelayData.drilldownReportId}?cllRecordId=${reasonOfDelayData.drilldownRecordId}" >${reasonOfDelayData.drilldownReportName} - ${reasonOfDelayData.drilldownRecordCaseNum}</a>
                        </span>
                    </div>
                </div>
            </g:if>
        </g:if>
        <g:if test="${actionItem.appType == AppTypeEnum.QUALITY_MODULE}">
            <g:set var="qualityService" bean="qualityService"/>
            <g:set var="caseNoDetailMap" value="${qualityService.getCaseNoByActionItemId(actionItem.id)}"/>
            <g:if test="${caseNoDetailMap?.id}">
                <div class="row" style="width:100%">
                    <div style="width:100%">
                        <span><b><g:message code="app.label.action.item.associated.caseNumber"/> :</b></span>
                        <span>
                            <a href='${grailsApplication.config.grails.appBaseURL}/quality/caseForm/?type=${caseNoDetailMap.dataType}&caseNumber=${caseNoDetailMap.masterCaseNum}&id=${caseNoDetailMap.id}&versionNumber=${caseNoDetailMap.masterVersionNum}'>${caseNoDetailMap.masterCaseNum}</a>
                        </span>
                    </div>
                </div>
            </g:if>
        </g:if>
        <g:if test="${actionItem.appType == AppTypeEnum.QUALITY_MODULE_CAPA}">
            <g:set var="qualityService" bean="qualityService"/>
            <g:set var="capa" value="${qualityService.getCapa(actionItem.id)}"/>
            <g:if test="${capa}">
                <div class="row" style="width:100%">
                    <div style="width:100%">
                        <span><b><g:message code="app.label.action.item.associated.caseNumber"/> :</b></span>
                        <span>
                            <a href='${grailsApplication.config.grails.appBaseURL}/issue/view/${capa.associatedIssueId}'>${capa.associatedIssueNumber}</a>
                        </span>
                    </div>
                </div>
            </g:if>
        </g:if>
    </g:if>
    <div class="row" style="width:100%">
        <div style="width:100%">
            <span><b><g:message code="app.actionItem.associatedActionItem.label"/> :</b></span>
            <span>
                <a href="${url}${actionItem.id}"><g:message code="app.label.actionItem.link"/></a>
            </span>
        </div>
    </div>
    <br>
    <hr>
    <br>
</g:each>