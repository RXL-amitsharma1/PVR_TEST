<%@ page import="com.rxlogix.config.WorkflowState" %>

<!-- Modal for Workflow State Justification -->
<div class="modal fade workflowStatusJustification"  data-backdrop="static" style="margin-left: 5px" id="workflowStatusJustification"
     tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "closeJustificationModal", "params": []}' aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="app.label.workflow.status"/></h4>
            </div>

            <div class="modal-body">
                <div class="workflow-state-update">

                    <g:renderClosableInlineAlert id="workflow-status-ai-warning-alert" type="warning" icon="warning" />
                    <g:renderClosableInlineAlert id="workflow-status-error-alert" type="danger" />

                    <g:form autocomplete="off"  name="justificationForm" id="justificationForm" >
                        <div>
                            <label>
                                <g:message code="app.periodicReport.executed.workflowState.label"/>
                                <span class="required-indicator">*</span>
                            </label>
                        </div>

                        <div id="showSel">
                            <div class="row">
                                <div class="col-lg-6"> <select style="height: 30px;width:250px;" class="form-control" name="toState.id" id="workflowSelect"></select></div>
                                <div class="col-lg-6">  <div id="forAllDiv" style="display: none">
                                    <div>
                                        <div class="checkbox checkbox-primary checkbox-inline">
                                            <g:checkBox name="setWorkflowStateForAll" />
                                            <label for="setWorkflowStateForAll" class="add-margin-bottom">
                                                <g:message code="app.label.PublisherTemplate.setWorkflowStateForAllSection" default="Set Workflow state for all sections with the same state"/>
                                            </label>
                                        </div>
                                    </div>
                                </div></div>
                            </div>

                        </div>

                        <div class="workflow"></div>
                        <div id="needApprovalDiv" style="display: none">
                            <label>
                                <g:message code="app.label.workflow.rule.needApproval"/>
                            </label>
                            <div>
                                <label>
                                    <g:message code="user.username.label"/>
                                    <span class="required-indicator">*</span>
                                </label>
                            </div>
                            <div>
                                <g:set var="userService" bean="userService"/>
                                <input autocomplete="autocomplete_off_xfr4!k1"  name="${new Date().getTime()}" disabled="true" id="login-input" value="${userService.currentUser.fullName}" class="form-control login-input">
                            </div>
                            <div>
                                <label>
                                    <g:message code="user.password.label"/>
                                    <span class="required-indicator">*</span>
                                </label>
                            </div>
                            <div id="password-input-div">
                                <input autocomplete="autocomplete_off_xfr4!k"  name="${new Date().getTime()}" id="password-input" class="form-control password-input" type="text">
                            </div>
                        </div>
                        <div class="description-wrapper">
                            <label>
                                <g:message code="app.label.justification"/>
                                <g:if test="${isPeriodicReport}">
                                    <span class="required-indicator">*</span>
                                </g:if>
                            </label>
                            <g:textArea rows="5" cols="3" id="description" name="description" maxlength="255" style="height: 110px;" value="" class="form-control withCharCounter"/>

                        </div>

                        <div class="workflow"></div>

                        <input type="hidden" name="dataLength" id="dataLength">
                        <input type="hidden" name="fromState.id" id="fromState">
                        <input type="hidden" name="executedReportConfiguration.id" id="reportId">
                        <input type="hidden" name="reportRequest.id" id="wfReportRequestId">
                        <input type="hidden" name="workflowRule.id" id="workflowRuleId">
                        <input type="hidden" name="dataType" id="dataType">
                        <input type="hidden" name="qualityCaseData.id" id="qualityCaseDataId">
                        <input type="hidden" name="qualitySubmission.id" id="qualitySubmissionId">
                        <input type="hidden" name="qualitySampling.id" id="qualitySamplingId">
                        <input type="hidden" name="drilldownCLLMetadata.caseId" id="caseId">
                        <input type="hidden" name="drilldownCLLMetadata.processedReportId" id="processReportId">
                        <input type="hidden" name="drilldownCLLMetadata.tenantId" id="ddCllMetaDataTenantId">
                        <input type="hidden" name="inboundMetadata.caseId" id="inCaseId">
                        <input type="hidden" name="inboundMetadata.tenantId" id="inMetaDataTenantId">
                        <input type="hidden" name="inboundMetadata.caseVersion" id="inVersionNum">
                        <input type="hidden" name="inboundMetadata.senderId" id="inSenderId">
                        <input type="hidden" name="cllRowId" id="cllRowId">
                        <input type="hidden" name="publisherSection.id" id="publisherSectionId">
                        <input type="hidden" name="publisherReport.id" id="publisherDocumentId">
                        <input type="hidden" name="publisherDocumentType" id="publisherDocumentType">
                    </g:form>
                </div>

                <div class="workflow-state-history">
                    <div>
                        <label><g:message code="app.label.history"/></label>
                        <table id="workflowJustificationTable" border="1" bordercolor="#D3D3D3" class="table table-striped">
                            <th class="workflowTableHead1"><g:message code="app.label.from.state"/></th>
                            <th class="workflowTableHead1"><g:message code="app.label.to.state"/></th>
                            <th class="workflowTableHead1"><g:message code="app.label.date"/></th>
                            <th class="workflowhead2"><g:message code="app.label.routed.by"/></th>
                            <th id="assignmentHistroyRow"><g:message code="app.label.action.item.assigned.to"/></th>
                            <th class="workflowhead2"><g:message code="app.label.justification"/></th>
                        </table>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="saveButton" class="btn btn-success confirm-workflow-justification" data-evt-clk='{"method": "confirmJustification", "params": ["${tableId}"]}'>
                    <g:message code="default.button.confirm.label"/>
                </button>
                <button type="button" class="btn btn-default cancel" data-evt-clk='{"method": "closeJustificationModal", "params": []}'>
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>