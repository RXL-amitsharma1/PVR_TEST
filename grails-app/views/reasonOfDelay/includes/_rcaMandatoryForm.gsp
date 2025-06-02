<%@ page import="com.rxlogix.WorkflowStateController; com.rxlogix.config.WorkflowState; com.rxlogix.enums.ReasonOfDelayAppEnum; com.rxlogix.enums.ReasonOfDelayFieldEnum; com.rxlogix.config.RCAMandatory; com.rxlogix.Constants" %>
<g:set var="workflowStates" value="${WorkflowState.findAllByIsDeleted(false)}"/>
<script>
    var sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
    var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
    $(function () {
        bindShareWith($('.editableBy'), sharedWithListUrl, sharedWithValuesUrl, "100%");
    });
</script>
<style>
.editableBy {
    width: 70% !important;
}
</style>

<div class="row">
    <div class="col-md-12">
        <div class="container">
            <table class="table table-striped">
                <tbody class="tbody-border">
                <g:each in="${ReasonOfDelayFieldEnum}" var="field">
                    <g:if test="${(ownerApp==ReasonOfDelayAppEnum.PVQ && (field==ReasonOfDelayFieldEnum.Root_Cause_Class || field==ReasonOfDelayFieldEnum.Root_Cause_Sub_Cat))}"></g:if>
                    <g:else>
                    <g:set var="rcaMandatoryInstance" value="${RCAMandatory.findByOwnerAppAndField(ownerApp, field)}"/>
                    <g:set var="editableBy" value="${ ((rcaMandatoryInstance?.editableByGroups?.collect{Constants.USER_GROUP_TOKEN + it.id}?:[]) + (rcaMandatoryInstance?.editableByUsers?.collect{Constants.USER_TOKEN + it.id}?:[]))?.join(";")}"/>
                    <tr data-field='${field}' data-owner-app="${ownerApp}">
                        <td style="width: 35%;">
                            <label>
                                <g:if test="${(ownerApp==ReasonOfDelayAppEnum.PVC || ownerApp==ReasonOfDelayAppEnum.PVC_Inbound) && field==ReasonOfDelayFieldEnum.Issue_Type}">
                                    <g:message code="app.label.view.case.late"/>
                                </g:if>
                                <g:else>
                                    <g:message code="${field.getI18nKey()}"/>
                                </g:else>
                            </label>

                            <div>
                                <span class="pull-left m-r-10">
                                    <g:message code="label.rcaMapping.mandatoryIn"/>
                                    <i class="fa fa-info-circle"
                                       title="${message(code: "title.rcaMandatory.workflow")}"></i>
                                </span>

                                <g:select name="mandatoryWorkflow" multiple="true" id="mandatoryWorkflow_${field}_${ownerApp}"
                                          from="${workflowStates}"
                                          optionKey="id"
                                          optionValue="name"
                                          value="${rcaMandatoryInstance.mandatoryInStates}"
                                          class="form-control pull-left select2-box workflowState mandatoryWorkflow"
                                          style="display: inline-block; width: 60%;"/>
                            </div>
                        </td>

                        <td style="width: 35%;">
                            <div class="m-t-25">
                                <span class="pull-left m-r-10">
                                    <g:message code="label.rcaMapping.editableIn"/>
                                    <i class="fa fa-info-circle"
                                       title="${message(code: "title.rcaEditable.workflow")}"></i>
                                </span>

                                <g:select name="editableWorkflow" multiple="true" id="editableWorkflow_${field}_${ownerApp}"
                                          from="${workflowStates}"
                                          optionKey="id"
                                          optionValue="name"
                                          value="${rcaMandatoryInstance.editableInStates}"
                                          class="form-control pull-left select2-box workflowState editableWorkflow"
                                          style="display: inline-block; width: 60%;"/>

                            </div>
                        </td>

                        <td style="width: 30%;">
                            <div class="m-t-25 forceLineWrap">
                                <div style="width: 30%;">
                                <span class="pull-left m-r-10">
                                    <g:message code="label.rcaMapping.editableBy"/>
                                    <i class="fa fa-info-circle"
                                       title="${message(code: "title.rcaEditableBy.User")}"></i>
                                </span>
                                </div>

                                <select class="form-control editableBy pull-left" name="editableBy" data-value="${editableBy}"
                                        style="display: inline-block;"></select>
                            </div>
                        </td>
                    </tr>
                </g:else>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</div>