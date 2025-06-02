<%@ page import="com.rxlogix.enums.ReportActionEnum; com.rxlogix.config.publisher.GanttItem; com.rxlogix.config.publisher.PublisherReport; com.rxlogix.config.publisher.PublisherExecutedTemplate;com.rxlogix.util.DateUtil; com.rxlogix.enums.ActionItemGroupState; com.rxlogix.enums.StatusEnum; com.rxlogix.config.ActionItem; com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.ReportSubmissionStatusEnum; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>

<div role="tabpanel" class="tab-pane " id="aiTab">
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" class=" active"><a href="#myActionItems" class="btn-pointed" aria-controls="myActionItems" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.myAi" default="Assigned to Me Or My  Group"/></a>
        </li>
        <li role="presentation"><a href="#allActionItems" class="btn-pointed" aria-controls="allActionItems" role="tab" data-toggle="tab"><g:message code="app.label.PublisherTemplate.allAi" default="All Action Items"/></a>
        </li>
    </ul>
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active pv-caselist" id="myActionItems">
            <table id="actionItemListPublisher" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                <thead>
                <tr>
                    <th><g:message code="app.label.action.item.assigned.to"/></th>
                    <th><g:message code="app.label.PublisherTemplate.relatedFor"/></th>
                    <th><g:message code="app.label.action.item.description"/></th>
                    <th><g:message code="app.label.action.item.due.date"/></th>
                    <th><g:message code="app.label.action.item.priority"/></th>
                    <th><g:message code="app.label.action.item.status"/></th>
                </tr>
                </thead>
            </table>
        </div><div role="tabpanel" class="tab-pane pv-caselist" id="allActionItems">
        <table id="allActionItemListPublisher" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.action.item.assigned.to"/></th>
                <th><g:message code="app.label.PublisherTemplate.relatedFor"/></th>
                <th><g:message code="app.label.action.item.description"/></th>
                <th><g:message code="app.label.action.item.due.date"/></th>
                <th><g:message code="app.label.action.item.priority"/></th>
                <th><g:message code="app.label.action.item.status"/></th>
            </tr>
            </thead>
        </table>
    </div>
    </div>
</div>