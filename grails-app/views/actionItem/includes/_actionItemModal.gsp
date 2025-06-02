<style>
    #actionItemListModal.modal .modal-dialog .modal-content .modal-body {
        padding: 5px 15px 15px 15px !important;
    }
    #actionItemListModal .dt-layout-row.dt-layout-table {
        margin-right: 2px;
    }
    #actionItemListModal .dt-layout-row:first-child {
        margin-top: 0;
        padding-right: 0;
    }
</style>
<div class="modal fade" id="actionItemModal"  data-backdrop="static" tabindex="-1" role="dialog" >

    <div class="modal-dialog modal-lg">

        <div class="modal-content">
            <div class="modal-header dropdown">
                <div class="col-xs-1 pull-right">
                    <button type="button" class="close close-action-item-model" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <a href="#" class="ic-sm pv-ic export-icon">
                    <i class="md-lg md-export excelExport" id="excelSingleAI" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.ActionItem.export.label')}" style="cursor:pointer; margin-left: 25px"></i>
                    </a>
                </div>
                <h4 class="modal-title"><g:message code="app.label.action.app.name" /></h4>

            </div>
            <div class="modal-body action-item-modal-body">
                <div class="alert alert-danger" style="display:none">
                    <a href="#" class="close alert-close" aria-label="close">&times;</a>
                    <span id="otherErrorMessage" style="display:none"><g:message code="app.error.fill.all.required"/></span>
                    <span id="fieldErrorMessage"><g:message code="app.error.fill.all.required"/></span>
                </div>
               <g:form name="actionItemForm" id="actionItemForm">
                   <g:render template="/actionItem/includes/form" model="[categories:categories]"/>

                   <!-- Executed report id field is kept -->
                   <g:hiddenField name="executedReportId" id="executedReportId" />
                   <g:hiddenField name="sectionId" id="sectionId" />
                   <g:hiddenField name="publisherId" id="publisherId" />
                   <g:hiddenField name="assignedToName" id="assignedToName" />
                   <g:hiddenField name="caseNumber" id="caseNumber" />
                   <g:hiddenField name="errorType" id="errorType" />
                   <g:hiddenField name="qualityId" id="qualityId" />
                   <g:hiddenField name="parentEntityKey" id="parentEntityKey" />
                   <g:hiddenField name="dateRangeFrom" id="dateRangeFrom" />
                   <g:hiddenField name="dateRangeTo" id="dateRangeTo" />
                   <g:hiddenField name="dataType" id="dataTypeAi" />
                   <g:hiddenField name="caseVersion" id="caseVersion" />
                   <g:hiddenField name="capaId" id="capaId" />
                   <g:hiddenField name="tenantId" id="tenantId"/>
                   <g:hiddenField name="masterCaseId" id="masterCaseId"/>
                   <g:hiddenField name="processedReportId" id="processedReportId"/>
                   <g:hiddenField name="configurationId" id="configurationId"/>
                   <g:hiddenField name="senderId" id="senderId"/>
                   <g:hiddenField name="masterVersionNum" id="masterVersionNum"/>
                   <g:hiddenField name="isInbound" id="isInbound"/>
               </g:form>

            </div>

            <div class="modal-footer">
                <div  class="buttons creationButtons">
                    <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
                    <input id="creationScreenButton" type="button" class="btn btn-primary save-action-item creationButton" value="${message(code: 'app.save.button.label')}" >
                    <input id="editScreenButton" type="button" class="hide btn btn-primary update-action-item consultationButtons" value="${message(code: 'app.update.button.label')}">
                    <input id="viewScreenButton" type="button" class="hide btn btn-primary edit-action-item consultationButtons" value="${message(code: 'app.edit.button.label')}">
                    <input id="deleteActionItem" type="button" class="hide btn btn-primary action-item-delete consultationButtons" value="${message(code: 'app.delete.button.label')}">
                    </sec:ifAnyGranted>
                        <button type="button" class="btn pv-btn-grey close-action-item-model"><g:message code="app.button.close"/> </button>

                </div>

                <!-- Index kept for the purpose if integrating this with other apps and knowing the current index.  -->
                <g:hiddenField name="index" id="index" />

            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="actionItemListModal"  data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">

        <div class="modal-content">
            <div class="modal-header dropdown">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.action.app.name" /></h4>

            </div>
            <div class="modal-body action-item-modal-body pv-caselist">
                <table id="actionItemList" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                    <thead>
                        <tr>
                            <th width="20%"><g:message code="app.label.action.item.assigned.to"/></th>
                            <th width="20%"><g:message code="app.label.action.item.description"/></th>
                            <th width="20%"><g:message code="app.label.action.item.due.date"/></th>
                            <th width="20%"><g:message code="app.label.action.item.priority"/></th>
                            <th width="20%"><g:message code="app.label.action.item.status"/></th>
                        </tr>
                    </thead>
                </table>
            </div>
            <div class="modal-footer">
                <div  class="buttons creationButtons">
                    <button type="button" class="btn pv-btn-grey close-action-item-model"><g:message code="app.button.close"/> </button>
                </div>
            </div>
        </div>
    </div>
</div>
