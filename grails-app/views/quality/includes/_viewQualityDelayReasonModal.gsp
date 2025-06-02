<%@ page import="com.rxlogix.config.QualityCaseData; com.rxlogix.enums.ReasonOfDelayFieldEnum;"%>
<style>
    .reasonOfDelayModalBody .select2-dropdown {
        position: fixed;
    }
    .reasonOfDelayModalBody .textAreaCharCounterWrapper {
        margin-bottom: 4px;
    }
    .reasonOfDelayModalBody .textAreaCharCounterWrapper .textAreaCharCounter {
        margin-top: -4px;
    }
    #reasonOfDelayModalId.modal .select2-container--default .select2-results > .select2-results__options {
        padding-bottom: 20px !important;
    }
    #reasonOfDelayModalId .modal-content {
        min-width: 840px;
    }
    #reasonOfDelayBody textarea[name=investigation],
    #reasonOfDelayBody textarea[name=summary],
    #reasonOfDelayBody textarea[name=actions] {
        width: 100%;
    }
</style>
<div class="modal fade resizableModal" id="reasonOfDelayModalId"  tabindex="-1" role="dialog" >
                <div class="modal-dialog modal-lg"  style="width:1300px;" role="document">
        <div class="modal-content">
                        <div class="modal-header"  style="cursor: move">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="reasonOfDelayModalLabel"><g:message code="quality.capa.rootCause.label"/></h4>
            </div>

                        <div class="modal-body" style="width: 100%; height: calc(100% - 85px);">
                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                     <span class="errorMessageSpan"></span>
                </div>
                <form id="reasonOfDelayModalForm">
                    <div class="row reasonOfDelayModalFormHeader">
                        <div class="col-md-2 ">
                            <label><g:message code="quality.capa.issueType.label"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Issue_Type.toString()} " style="display:inline !important;">*</span></label>
                            <div class="lateSelectDiv"></div>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.label.assignedToGroup"/></label>
                            <select class=' col-md-12 form-control' name='assignedToUserGroup'></select>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.label.assignedToUser"/></label>
                            <select class=' col-md-12 form-control' name='assignedToUser'></select>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.pcv.workflowState"/></label>
                            <input type="hidden" class="workflowCurrentId" name="workflowCurrentId"/>
                            <select class="form-control workflow" name="workflowRule"><option></option></select>
                        </div>
                        <div class="col-md-6 justificationDiv" style="display: none">
                            <label><g:message code="app.label.justification"/></label>
                            <input class="form-control justification" name="justification" maxlength="${QualityCaseData.constrainedProperties.justification.maxSize}">
                        </div>
                        <div class="col-md-8 noworkflow" style="display: none">
                            <label><g:message code="app.pcv.workflowState"/></label>
                            <div><g:message code="app.pcv.differentRows"/></div>
                        </div>
                    </div>
                    <div class="row reasonOfDelayModalBody">
                        <div id="mainCaseValues" style="display: none">
                            <input type="hidden" name="versionNumber" >
                            <input type="hidden" name="caseId" >
                            <input type="hidden" name="enterpriseId" >
                            <input type="hidden" name="reportId" >
                        </div><div id="otherCaseValues" style="display: none"></div>
                        <table id="reasonOfDelayListTable" class="table">
                            <thead>
                            <tr>
                                <th style="width: 4%;"><span class="table-add glyphicon glyphicon-plus"></span></th>
                                <th style="width: 4%;"><label><g:message code="app.pvc.pri.sec"/></label></th>
                                <th style="width: 12%;"><label><g:message code="app.pvc.rootcause"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Root_Cause.toString()} ">*</span><br><g:message code="app.pvc.ResponsibleParty"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Resp_Party.toString()} ">*</span></label></th>
                                <th style="width: 12%;"><label><g:message code="app.pvc.CorrectiveAction"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Corrective_Action.toString()} ">*</span><br><g:message code="app.pvc.PreventiveAction"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Preventive_Action.toString()} ">*</span></label></th>
                                <th style="width: 20%;"><label><g:message code="app.pvc.CorrectiveDate"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Corrective_Date.toString()} ">*</span><br><g:message code="app.pvc.PreventiveDate"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Preventive_Date.toString()} ">*</span></label></th>
                                <th style="width: 16%;"><label><g:message code="app.pvc.investigation"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Investigation.toString()} ">*</span></label></th>
                                <th style="width: 16%;"><label><g:message code="app.pvc.summary"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Summary.toString()} ">*</span></label></th>
                                <th style="width: 16%;"><label><g:message code="app.pvc.actions"/><span class="required-indicator ${ReasonOfDelayFieldEnum.Actions.toString()} ">*</span></label></th>
                            </tr>
                            </thead>
                            <tbody id="reasonOfDelayBody"></tbody>
                        </table>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button type="button" class="btn btn-primary saveReasonsOfDelay"><g:message code="default.button.save.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>