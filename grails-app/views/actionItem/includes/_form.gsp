<%@ page import="com.rxlogix.config.ActionItemCategory; com.rxlogix.user.UserGroup; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PriorityEnum; com.rxlogix.enums.StatusEnum; com.rxlogix.user.User;com.rxlogix.config.ActionItem" %>
<div>

        <div class="row">

            <div class="col-xs-4">
                <label><g:message default="Action Category" code="app.label.action.item.action.category"/><span class="required-indicator">*</span></label>
                <g:select id="actionCategory" name="actionCategory"
                          from="${categories?:(ViewHelper.isPvqModule(request) ? ViewHelper.actionItemCategoryEnumPvq() : ViewHelper.actionItemCategoryEnumPvr())}"
                          optionKey="name" optionValue="display"
                          noSelection="['': message(code: 'select.category')]"
                          class="form-control select2-box needsActionItemRole"/>
            </div>

            <div class="col-xs-4">
                <label><g:message default="Priority" code="app.label.action.item.priority"/><span class="required-indicator">*</span></label>
                <g:select id="priority" name="priority"
                          optionKey="name" optionValue="display"
                          from="${ViewHelper.priorityEnum}"
                          class="form-control select2-box needsActionItemRole"/>
            </div>

            <div class="col-xs-4">
                <script>
                    sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                    sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                    $(function () {
                        bindShareWith($('#actionItemModal').find('.sharedWithControl'), sharedWithListUrl, sharedWithValuesUrl, "100%", true, $('#actionItemModal'))
                    });
                    var caseDataLinkAiUrl = "${createLink(controller: 'quality', action: 'caseForm')}";
                    var viewIssueNumberLinkUrl = "${createLink(controller: 'issue', action: 'view')}";
                    var viewPVCIssueNumberLinkUrl = "${createLink(controller: 'pvcIssue', action: 'view')}";
                </script>
                <label><g:message default="Assigned To" code="app.label.action.item.assigned.to"/><span class="required-indicator">*</span></label>
                <select class="sharedWithControl form-control needsActionItemRole" id="assignedTo" name="assignedTo" value=""></select>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-4">
                <label><g:message default="Due Date" code="app.label.action.item.due.date"/><span class="required-indicator">*</span></label>
                <div class="fuelux">
                    <div>
                        <div class="datepicker toolbarInline pastDateNotAllowed" id="dueDateDiv">
                            <div class="input-group">
                                <g:textField id="dueDate" placeholder="${message(code:"placeholder.dueDate.label" )}" class="form-control fuelux needsActionItemRole" name="dueDate" />
                                <g:render id="dueDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xs-4">
                <label><g:message default="Completion Date" code="app.label.action.item.completion.date"/><span class="required-indicator hidden" id="completionDateRequired">*</span></label>
                <div class="fuelux">
                    <div>
                        <div class="datepicker toolbarInline pastDateNotAllowed" id="completionDateDiv">
                            <div class="input-group">
                                <g:textField id="completionDate" placeholder="${message(code: "placeholder.completionDate.label")}" class="form-control fuelux needsActionItemRole" name="completionDate"/>
                                <g:render id="completionDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xs-4">
                <label><g:message default="Status" code="app.label.action.item.status"/></label>
                <g:select name="status" optionKey="name" optionValue="display"
                          from="${ViewHelper.statuses}" class="form-control select2-box needsActionItemRole"/>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-6">
                <label><g:message default="Description" code="app.label.action.item.description"/><span class="required-indicator">*</span></label>
                <g:textArea id="description" name="description" value="" rows="15" cols="40" maxlength="4000" class="form-control needsActionItemRole" />
            </div>
            <div class="col-xs-6">
                <label><g:message default="Description" code="app.label.PublisherTemplate.pending.comments"/></label>
                <g:textArea id="comment" name="comment" value="" rows="15" cols="40" maxlength="4000" class="form-control needsActionItemRole" />
            </div>

        </div>

        <div class="row">
            <div class="col-xs-6">
                <span id='remainingChar' class="pull-left" style="margin-bottom: 10px;"> </span>
            </div>
            <div class="col-xs-6">
                <span id='remainingChar2' class="pull-left" style="margin-bottom: 10px;"> </span>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-4" id="linkDiv" style="display: none">
                <label><g:message code="app.actionItem.associatedReport.label"/></label><br>
                <a href="" data-base-url="${createLink(controller: "report", action: "showFirstSection")}" id="link"></a>
            </div>
            <div class="col-xs-4" id="linkConfigDiv" style="display: none">
                <label><g:message code="app.actionItem.associatedConfiguration.label"/></label><br>
                <a href="" id="configLink"></a>
            </div>
            <div class="col-xs-4" id="linkRequestDiv" style="display: none">
                <label><g:message code="app.actionItem.associatedRequest.label"/></label><br>
                <a href="" data-base-url="${createLink(controller: "reportRequest", action: "show")}" id="requestLink"></a>
                <input type="hidden" id="rptRequestId" name="rptRequestId" />
            </div>
            <div class="col-xs-4" id="linkDrilldownDiv" style="display: none">
                <label><g:message code="app.actionItem.associatedDrilldown.label"/></label><br>
                <a href="" data-base-url="${createLink(controller: "advancedReportViewer", action: "viewDelayReason")}" id="drilldowLink"></a>
            </div>
            <div class="col-xs-4">
               <label><g:message default="Created By" code="app.label.action.item.created.by"/></label>
               <div id="createdBy">${sec.loggedInUserInfo(field: "fullName")}</div>
               <g:hiddenField name="actionItemId" id="actionItemId" />
               <g:hiddenField name="appType" id="appType" />
           </div>
           <div class="col-xs-4">
               <label><g:message default="Date Created" code="app.label.action.item.date.created"/></label>
               <div id="dateCreated" class="dateCreated"></div>
               <g:hiddenField name="dateCreated" value=""/>
           </div>
        </div>
        <div class="row" id="associatedCaseNoDiv">
             <div class="col-xs-12">
                 <label><g:message default="Associated Case Number" code="app.label.action.item.associated.caseNumber"/></label>
                 <div id="associatedCaseNumber" class="associatedCaseNumber"></div>
             </div>
        </div>
        <div class="row" id="associatedIssueNoDiv">
            <div class="col-xs-12">
                <label><g:message default="Associated Issue Number" code="app.label.action.item.associated.issueNumber"/></label>
                <div id="associatedIssueNumber" class="associatedIssueNumber"></div>
            </div>
        </div>
    <g:hiddenField name="aiVersion" value=""/>
</div>