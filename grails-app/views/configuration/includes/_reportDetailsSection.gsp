<%@ page import="com.rxlogix.config.PeriodicReportConfiguration; grails.util.Holders; com.rxlogix.Constants; com.rxlogix.config.UnitConfiguration; com.rxlogix.config.Configuration; com.rxlogix.enums.PeriodicReportTypeEnum" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.reportDetails"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div class="row">
                %{--Report Name--}%
                <div class="col-xs-4">
                    <div class="${hasErrors(bean: configurationInstance, field: 'reportName', 'has-error')}">
                        <label><g:message code="app.label.reportName"/><span class="required-indicator">*</span></label>
                        <input type="text" name="reportName" placeholder="${g.message(code: 'input.name.placeholder')}"
                               class="form-control"
                               maxlength="${Configuration.constrainedProperties.reportName.maxSize}"
                               value="${configurationInstance?.reportName}"/>
                    </div>
                    <g:if test="${isForPeriodicReport}">
                        <div class="pvpOnly" style="display: none;">

                            <label><g:message code="app.label.versionNamePattern" default="Version Name Pattern"/>
                                <span class="fa fa-question-circle"  style="cursor: pointer;" data-toggle="modal" data-target="#generatedReportPatternHelp"></span></label>
                            <input type="text" name="generatedReportName" placeholder="${g.message(code: 'app.label.versionNamePattern')}"
                                   class="form-control"
                                   maxlength="${PeriodicReportConfiguration.constrainedProperties.generatedReportName.maxSize}"
                                   value="${configurationInstance?.generatedReportName?:Holders.config.aggregateReport.generatedReportNamePattern}"/>


                        </div>

                        <div class="${hasErrors(bean: configurationInstance, field: 'periodicReportType', 'has-error')}  ${params.fromTemplate ? "hidden" : ""}">
                            <label><g:message code="app.label.periodicReportType"/><span
                                    class="required-indicator">*</span>
                            </label>
                            <g:select name="periodicReportType" from="${PeriodicReportTypeEnum.asList}" optionKey="key"
                                      value="${configurationInstance.periodicReportType?.key}" class="form-control"/>
                        </div>

                    </g:if>

                    %{--Tags--}%
                    <div class=" ${(params.fromTemplate||configurationInstance.pvqType) ? "hidden" : ""}">
                        <g:render template="/includes/widgets/tagsSelect2" model="['domainInstance': configurationInstance]"/>
                    </div>
                </div>
                <g:if test="${configurationInstance.pvqType}">
                    <div class="col-xs-4">
                        <label><g:message code="qualityModule.ad.hoc.alert.button"/><span class="required-indicator">*</span></label>
                        <g:set var="qualityService" bean="qualityService"/>
                        <select id="pvqType" name="pvqType" multiple class="form-control" required>
                            <g:each in="${qualityService.listTypes()}" var="type">
                                <option ${configurationInstance?.pvqType?.split(";").contains(type.value) ? "selected" : ""} value="${type.value}">${type.label}</option>
                            </g:each>
                        </select>
                    </div>
                </g:if>
                %{--Description--}%
                <div class="col-xs-4 ${(configurationInstance.pvqType)? "hidden" : ""}">
                    <div >

                        <label for="description"><g:message code="app.label.reportDescription"/></label>
                        <g:render template="/includes/widgets/descriptionControl" model="[value:configurationInstance?.description, maxlength: Configuration.constrainedProperties.description.maxSize]"/>
                    </div>
                    <g:if test="${isForPeriodicReport}">
                        <input type="hidden" name="isPublisherReport" value="true" class="nonPvpRemove">
                        <div class="p-t-10">
                                           <div class="checkbox checkbox-primary  ${params.fromTemplate ? "hidden" : ""}">
                           <g:checkBox name="generateCaseSeries"
                                       value="${configurationInstance?.generateCaseSeries}"
                                       checked="${configurationInstance?.generateCaseSeries}"/>
                          <label for="generateCaseSeries">
                             <g:message code="reportCriteria.generate.case.series"/>
                          </label>
                       </div>
                        <div class="checkbox checkbox-primary generateDraft ${params.fromTemplate ? "hidden" : ""}" style="display: none;">
                           <g:checkBox name="generateDraft"
                                       value="${configurationInstance?.generateDraft}"
                                       checked="${configurationInstance?.generateDraft}"/>
                          <label for="generateDraft">
                             <g:message code="reportCriteria.generate.draft"/>
                          </label>
                       </div>
                        <div class="generateDraft" style="display: none">
                            <g:render template="/caseSeries/includes/spotfire" model="[seriesInstance: configurationInstance, isForPeriodicReport:isForPeriodicReport]"/>
                        </div>
                       <div class="clearfix"></div>
                        </div>
                    </g:if>
                </div>
                %{--Public--}%
                <g:if test="${isForPeriodicReport}">
                <div class="col-xs-4 pvpOnly" style="display: none;">
                    <label ><g:message code="app.publisher.publisherContributors" default="Publisher Contributors"/></label>
                    <div class="destinations">
                        <g:hiddenField name="primaryPublisherContributor" value="${configurationInstance?.primaryPublisherContributor?.id}"/>
                        <g:select name="publisherContributors" from="${[]}"
                                       value="${configurationInstance?.allPublisherContributors?.collect{it.id}?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                       class="form-control" multiple="multiple"/>
                    </div>
                </div>
                </g:if>
                <div class="col-xs-4" style="margin-top: 29px">
                    <div class="row" >
                        <div class="col-md-12">
                            <g:render template="/includes/widgets/tenantDropDownTemplate"
                                      model="[configurationInstance: configurationInstance]"/>
                        </div>
                    </div>

                    <div class="row ${(params.fromTemplate ||configurationInstance.pvqType)  ? "hidden" : ""}">
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
                            <div class="checkbox checkbox-primary isTemplate" style="display: none">
                                <g:checkBox name="isTemplate" class="isTemplate" value="${configurationInstance?.isTemplate}"
                                            checked="${configurationInstance?.isTemplate}"/>
                                <label for="isTemplate">
                                    <g:message code="app.label.template"/>
                                </label>
                            </div>
                            <g:if test="${configurationInstance instanceof com.rxlogix.config.Configuration}">
                            <sec:ifAnyGranted roles="ROLE_BQA_EDITOR">
                            <div class="checkbox checkbox-primary" >
                                <g:checkBox name="qbeForm" value="${configurationInstance?.qbeForm}"
                                            checked="${configurationInstance?.qbeForm}"/>
                                <label for="qbeForm">
                                    <g:message code="app.label.qbeForm"/>
                                </label>
                            </div>
                            </sec:ifAnyGranted>
                            </g:if>
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
                    <button type="button" class="btn pv-btn-grey cancel" data-evt-clk='{"method": "modalHide", "params": ["#generatedReportPatternHelp"]}'><g:message code="default.button.ok.label" /></button>
                </div>
            </div>
        </div>
    </div>
</div>