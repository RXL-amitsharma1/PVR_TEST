<%@ page import="grails.util.Holders; com.rxlogix.Constants;" %>
<div class="modal fade" id="adHocAlertModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <g:form name="adHocAlert" data-evt-sbt='{"method": "saveAdHocAlert", "params": []}' action="saveAdHocAlert">
            <g:hiddenField  name="masterRptTypeId" type="hidden"/>
            <g:hiddenField name="masterCountryIdSelect" type="hidden"/>
            <g:hiddenField name="masterPrimProdName" type="hidden"/>
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel"><g:message
                            code="qualityModule.popup.ad.hoc.alert.header"/></h4>
                </div>

                <div class="modal-body" style="max-height: 500px;">
                    <div class="errorMessageDiv" style="color: red;text-align: left;"><g:message
                            code="qualityModule.adhocAlert.errorMessage"/></div>
                    <div class="alert alert-danger hide">
                        <button type="button" class="close">
                            <span aria-hidden="true" data-evt-clk='{"method" : "addClassHide", "params" : [".alert-danger"]}' >&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <span class="errorMessageSpan"></span>
                    </div>
                    <div class="row form-group">
                        <div class="col-md-6" style="padding-left: 20px">
                            <div class="form-group">
                                <label><g:message code="qualityAlert.caseNumber"/><span
                                        class="required-indicator">*</span>
                                </label>

                                <div><g:textField name="masterCaseNum" maxlength="255" required="required"
                                                                             /></div>
                            </div>

                            <div id="spinner" disabled><asset:image src="spinner.gif" height="30px" width="30px"/></div>

                            <div class="form-group">
                                <label>
                                    <g:message code="qualityAlert.caseReceiptDate"/>
                                </label>

                                <div>
                                    <g:textField name="masterCaseReceiptDate" disabled="disabled"/>
                                </div>
                            </div>
                            <input name="masterVersionNum" id="masterVersionNum" type="hidden">
                            <div class="form-group">
                                <label><g:message code="app.label.errorType"/><span
                                        class="required-indicator">*</span>
                                </label>

                                <div><g:textField name="errorType" required="required" value="Manual" readonly="true"/></div>
                            </div>
                        </div>

                        <div class="col-md-6" style="padding-left: 20px">


                            <input class="hidden" name="masterCountryId" id="masterCountryId"/>

                            <g:if test="${!grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)}">
                                <div class="form-group">
                                    <label><g:message
                                            code="qualityAlert.masterSiteId"/></label>

                                    <div><g:textField name="masterSiteId" disabled="disabled"
                                                      required="required"/></div>
                                </div>
                            </g:if>

                            <div class="form-group">
                                <label><g:message code="qualityAlert.priority"/></label>

                                <div><g:select id="priorityManualObservation" name="priority" from="[]" class="form-control"/></div>
                            </div>

                        </div>
                    </div>
                    <div class="row form-group">
                        <div class="form-group quality-observation-details" >
                            <label ><g:message code="qualityAlert.additionalDetails"/></label>

                            <div><g:textArea name="comment" maxlength="8000" class="multiline-text" rows="6" cols="60" disabled="disabled"/></div>
                        </div>
                    </div>
                    <div class="bs-callout bs-callout-info">
                        <h5><g:message code="app.label.note" /> : <g:message code="app.pvq.qualityObservationDetails.validation.note" /></h5>
                    </div>
                </div>
                <input type="hidden" name="dataType" id="adAlertType">
                <div class="modal-footer">
                    <div class="buttons creationButtons col-md-12" id="saveAlertDiv">
                        <g:submitButton class="btn primaryButton btn-primary" style="float: left;" disabled="disabled" name="submit"
                                        value="${message(code: 'default.button.save.label')}"/>
                        <button type="button" class="btn pv-btn-grey" style="float: left;" data-dismiss="modal">
                            <g:message code="default.button.cancel.label"/>
                        </button>
                    </div>
                    <div class="buttons creationButtons col-md-12" id="closeAlertDiv">
                        <button type="button" class="btn btn-default" style="float: left;" data-dismiss="modal">
                            <g:message code="default.button.close.label"/>
                        </button>
                    </div>
                </div>
            </div>
            </div>
        </g:form>
</div>