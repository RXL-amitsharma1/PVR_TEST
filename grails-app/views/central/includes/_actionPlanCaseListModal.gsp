<div class="modal fade custom-xl" id="caseListModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel"><g:message code="app.label.report.case.list"/></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger hide modalError">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                </div>

                <div class="alert alert-success hide modalSuccess">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <g:message code="app.template.update.success" args="[message(code: 'app.pvc.summary')]"/>
                </div>
                <div class="row">
                    <div class="col-sm-3">

                        <div class="radio radio-primary radio-inline">
                            <input type="radio" name="summaryPeriod" checked id="summaryPeriod1" class="summaryPeriodRadio" value="current" autocomplete="off">
                            <label for="summaryPeriod1">
                                <g:message code="app.actionPlan.sumummaryFroPeriod"/>
                            </label>
                        </div>
                        <div class="radio radio-primary radio-inline">
                            <input type="radio" name="summaryPeriod" id="summaryPeriod2" class="summaryPeriodRadio" value="all" autocomplete="off">
                            <label for="summaryPeriod2">
                                <g:message code="app.label.all"/>
                            </label>
                        </div>
                        <div class="row">

                            <select class="form-control select2-box" id="actionPlanSummaries" >
                                <option value="">10-Mar-2021 - 10-Apr-2021</option>
                                <option value="">10-Mar-2021 - 10-Apr-2021</option>
                            </select>

                        </div>
                        <div style="margin-top: 25px" class="row fuelux">
                            <button class="btn btn-primary createNewSummary"><g:message code="app.actionPlan.createNewSP"/></button>

                        </div>
                    </div>
                    <div class="col-sm-9">
                        <b><g:message code="app.actionPlan.sumummaryFor"/> <span id="summaryDate">10-Apr-2021 - 10-Mar-2021</span></b> <span class="required-indicator">*</span> <button class="btn btn-xs btn-primary saveSummaryButton"><g:message code="app.actionPlan.saveChanges"/></button><button class="btn btn-xs btn-default deleteSummary"><g:message code="default.button.delete.label"/></button>
                        <input id="currentSummary" type="hidden" />
                        <textarea name="summaryText" id="summaryText" class="form-control" maxlength="4000"  style="width: 100%; height: 75px; resize: auto;"></textarea>
                    </div>

                </div>
                <div class="pv-caselist">
                    <table id="caseListTable" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.caseNumber.label"/></th>
                            <th><g:message code="app.label.quality.caseVersion"/></th>

                            <th><g:message code="quality.capa.rootCause.label"/><br><g:message code="app.pvc.RootCauseClass"/></th>
                            <th><g:message code="app.pvc.RootCauseSubCategory"/><br><g:message code="quality.capa.responsibleParty.label"/></th>
                            <th><g:message code="app.pvc.CorrectiveAction"/><br><g:message code="app.pvc.PreventiveAction"/></th>
                            <th><g:message code="app.pvc.CorrectiveDate"/><br><g:message code="app.pvc.PreventiveDate"/></th>
                            <th><g:message code="app.pvc.investigation"/></th>
                            <th><g:message code="app.pvc.summary"/></th>
                            <th><g:message code="app.pvc.actions"/></th>
                            <th><g:message code="app.pvc.import.Primary"/></th>
                            <th><g:message code="app.label.workflow.appName"/></th>
                            <th><g:message code="app.label.workflow.rule.assignedTo"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->