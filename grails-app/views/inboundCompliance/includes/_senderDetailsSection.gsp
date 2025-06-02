<%@ page import="com.rxlogix.config.PeriodicReportConfiguration; com.rxlogix.util.ViewHelper; grails.util.Holders; com.rxlogix.Constants; com.rxlogix.config.UnitConfiguration; com.rxlogix.config.Configuration; com.rxlogix.enums.PeriodicReportTypeEnum" %>
<g:set var="inboundComplianceService" bean="inboundComplianceService"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.sender.details"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div class="row">
                %{--Report Name--}%
                <div class="col-xs-4">
                %{--                from="${[['key': 'Sender Name 1', 'display': message(code: "app.label.yes")], ['key': 'No', 'display': message(code: "app.label.no")]]}"--}%
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.sender.name"/><span class="required-indicator">*</span></label>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12">
                            <div>
                                <g:select name="senderName" id='senderName' from="${inboundComplianceService.getDataBasedOnLmsql()}"
                                          class="form-control select2-box senderName" optionKey="name" optionValue="display"
                                          noSelection="['': message(code: 'select.one')]" value="${configurationInstance?.senderName}"/>
                            </div>
                        </div>
                    </div>

                %{--Tags--}%
                    <div class=" ${params.fromTemplate ? "hidden" : ""}">
                        <g:render template="/includes/widgets/tagsSelect2" model="['domainInstance': configurationInstance]"/>
                    </div>
                </div>
                %{--Description--}%
                <div class="col-xs-4">
                    <div >
                        <label for="description"><g:message code="app.label.reportDescription"/></label>
                        <g:render template="/includes/widgets/descriptionControl" model="[value:configurationInstance?.description, maxlength: Configuration.constrainedProperties.description.maxSize]"/>

                    </div>
                </div>
            %{--Public--}%
                <div class="col-xs-4" style="margin-top: 20px">
                    <div class="row" >
                        <div class="col-md-12">
                            <g:render template="/includes/widgets/tenantDropDownTemplate"
                                      model="[configurationInstance: configurationInstance]"/>
                        </div>
                    </div>

                    <div class="row ${params.fromTemplate ? "hidden" : ""}">
                        <div class="col-md-12">
                            <sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="qualityChecked" id="qualityChecked"
                                                value="${configurationInstance?.qualityChecked}"
                                                checked=""/>
                                    <label for="qualityChecked">
                                        <g:message code="app.label.qualityChecked"/>
                                    </label>
                                </div>
                            </sec:ifAnyGranted>
                            <div id="isTemplate" class="checkbox checkbox-primary" style="display: none">
                                <g:checkBox name="isTemplate" value="${configurationInstance?.isTemplate}"
                                            checked="${configurationInstance?.isTemplate}"/>
                                <label for="isTemplate">
                                    <g:message code="app.label.template"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="generatedReportPatternHelp" tabindex="-1" role="dialog" aria-labelledby="Generated Report Pattern Help">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#generatedReportPatternHelp"]}' aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <span><b><g:message code="app.label.help" /></b></span>
                </div>
                <div class="modal-body container-fluid">
                    <b><g:message code="app.label.generatedReportPatternHelp.text1" /></b>
                    <ul>
                        <li>[version] - <g:message code="app.label.reportVersion"/></li>
                        <li>[description] - <g:message code="app.label.reportDescription"/></li>
                        <li>[periodicReportType] - <g:message code="app.label.periodicReportType"/></li>
                        <li>[productSelection] - <g:message code="app.label.productSelection"/></li>
                        <li>[studySelection] - <g:message code="app.label.studySelection"/></li>
                        <li>[executedGlobalQuery] - <g:message code="app.label.globalQueryName"/></li>
                        <li>[globalDateRange] - <g:message code="app.label.globalDateRangInformation"/></li>
                        <li>[dateRangeType] - <g:message code="app.label.DateRangeType"/></li>
                        <li>[excludeNonValidCases] - <g:message code="reportCriteria.exclude.non.valid.cases"/></li>
                        <li>[excludeDeletedCases] - <g:message code="reportCriteria.exclude.deleted.cases"/></li>
                        <li>[excludeFollowUp] - <g:message code="reportCriteria.exclude.follow.up"/></li>
                        <li>[suspectProduct] - <g:message code="app.label.SuspectProduct"/></li>
                        <li>[includePreviousMissingCases] - <g:message code="reportCriteria.include.previous.missing.cases"/></li>
                        <li>[includeOpenCasesInDraft] - <g:message code="app.label.includeOpenCasesInDraft"/></li>
                        <li>[includeAllStudyDrugsCases] - <g:message code="app.label.includeAllStudyDrugsCases"/></li>
                        <li>[includeNonSignificantFollowUp] - <g:message code="reportCriteria.include.non.significant.followup.cases"/></li>
                        <li>[owner] - <g:message code="app.label.reportOwner"/></li>
                        <li>[lastRunDate] - <g:message code="app.label.runDateAndTime"/></li>
                        <li>[status] - <g:message code="app.label.executionStatus"/></li>
                        <li>[sections] - <g:message code="app.label.reportSections"/></li>

                    </ul>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-grey cancel" data-evt-clk='{"method": "modalHide", "params": ["generatedReportPatternHelp"]}'><g:message code="default.button.ok.label" /></button>
                </div>
            </div>
        </div>
    </div>
</div>