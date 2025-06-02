<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.config.TemplateQuery; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeEnum" %>
<%@ page import="grails.util.Holders; com.rxlogix.config.Tenant; com.rxlogix.enums.IcsrReportSpecEnum; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate;com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.util.DateUtil; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum" %>

<!-- Modal header -->
<style>
.sectionsHeader {
    background-image: none;
    background-color: #D2D2D2;
}
</style>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <span class="modalHeader"><g:message code="app.label.add.section" /></span>
</div>
<!-- Modal body -->
<div class="modal-body periodicReport">
    <g:form>
        <g:hiddenField name="executeRptFromCount" value="true"/>
        <g:hiddenField name="executedDateRangeInformationForTemplateQuery.dateRangeEnum" value="${parentExecutedTemplate.executedDateRangeInformationForTemplateQuery.dateRangeEnum}"/>
        <g:hiddenField name="executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute" value="${parentExecutedTemplate.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute}"/>
        <g:hiddenField name="executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute" value="${parentExecutedTemplate.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute}"/>
        <g:hiddenField name="executedDateRangeInformationForTemplateQuery.relativeDateRangeValue" value="${parentExecutedTemplate.executedDateRangeInformationForTemplateQuery.relativeDateRangeValue}"/>
        <g:hiddenField name="executedConfiguration.relativeDateRangeValue" value="${executedConfiguration.executedGlobalDateRangeInformation.relativeDateRangeValue}"/>
        <g:hiddenField name="rowId" value="${params.rowId}"/>
        <g:hiddenField name="columnName" value="${params.columnName}"/>
        <g:hiddenField name="count" value="${params.count}"/>
        <g:hiddenField name="reportResultId" value="${params.reportResultId}"/>
        <g:hiddenField name="isInDraftMode" value="${(params.boolean('isInDraftMode') || executedConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT) ? true : false}"/>
        <g:hiddenField name="submitUrl" value="${submitUrl}"/>

        <div class="templateQuery-div">
            <div class="row">
                <div class="col-md-12">
                    <label><g:message code="app.label.chooseAReportTemplate"/><span
                            class="required-indicator">*</span></label>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div>
                        <g:select name="template.id"
                                  from="${[]}"
                                  class="form-control selectTemplate"/>
                        <g:hiddenField name="executedConfiguration.id" value="${executedConfiguration.id}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-9">
                    <a class="add-cursor showHeaderFooterArea"><g:message
                            code="add.header.title.and.footer"/></a>
                </div>
            </div>

            <div class="row headerFooterArea" hidden="hidden">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-4">
                            <g:textField name="header"
                                         maxlength="${TemplateQuery.constrainedProperties.header.maxSize}"
                                         placeholder="Header"
                                         class="form-control"/>
                        </div>

                        <div class="col-md-4">
                            <g:textField name="title"
                                         maxlength="${Configuration.constrainedProperties.reportName.maxSize}"
                                         placeholder="Title"
                                         class="form-control"/>
                        </div>

                        <div class="col-md-4">
                            <g:footerSelect name="footer" class="form-control footerSelect"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="headerProductSelection"/>
                                <label for="headerProductSelection">
                                    <g:message code="templateQuery.headerProductSelection.label" />
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="headerDateRange"/>
                                <label for="headerDateRange">
                                    <g:message code="templateQuery.headerDateRange.label" />
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="displayMedDraVersionNumber"/>
                                <label for="displayMedDraVersionNumber">
                                    <g:message code="templateQuery.displayMedDraVersionNumber.label" />
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="ciomsProtectedArea" hidden="hidden">
                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="privacyProtected"/>
                            <label for="privacyProtected">
                                <g:message code="templateQuery.privacyProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="blindProtected"/>
                            <label for="blindProtected">
                                <g:message code="templateQuery.blindProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-12">
            <table class="table table-striped table-bordered">
                <thead class="sectionsHeader">
                <tr class="template-criteria-th-bg">
                    <th width="55%"><g:message code="app.label.sectionTitle"/></th>
                    <th width="40%"><g:message code="app.label.template"/></th>
                    <th width="5%"></th>
                </tr>
                </thead>
                <tbody>
                <g:if test="${executedConfiguration.executedTemplateQueries}">
                    <g:each var="executedTemplateQuery"
                            in="${executedConfiguration.executedTemplateQueries}">
                        <tr>
                            <td>
                                <g:if test="${!executedTemplateQuery.reportResult || executedTemplateQuery.reportResult.executionStatus == ReportExecutionStatusEnum.ERROR}">
                                    <div style="text-align: center"><i class="fa fa-exclamation-triangle" title="${g.message(code: 'app.label.section.not.generated')}"></i></div>
                                </g:if>
                                <g:elseif test="${executedTemplateQuery.reportResult.executionStatus != ReportExecutionStatusEnum.COMPLETED}">
                                    <div style="text-align: center"><i class="fa fa-clock-o fa-lg es-scheduled fa-lg es-scheduled" title="${g.message(code: 'app.label.scheduled')}"></i></div>
                                </g:elseif>
                                <g:else>
                                    <g:set var="reportShowController" value="report"/>
                                    <g:if test="${executedConfiguration instanceof com.rxlogix.config.ExecutedIcsrReportConfiguration}">
                                        <g:set var="reportShowController" value="icsrReport"/>
                                    </g:if>
                                    <g:elseif
                                            test="${executedConfiguration instanceof com.rxlogix.config.ExecutedIcsrProfileConfiguration}">
                                        <g:set var="reportShowController" value="executedIcsrProfile"/>
                                    </g:elseif>
                                    <g:link controller="${reportShowController}" action="show"
                                            params="[id: executedTemplateQuery.reportResult.id]">
                                        <g:renderDynamicReportName
                                                executedConfiguration="${executedConfiguration}"
                                                executedTemplateQuery="${executedTemplateQuery}"
                                                hideSubmittable="${true}"
                                                rowId="${params.rowId}"
                                                columnName="${params.columnName}"/>
                                    </g:link>
                                </g:else>
                            </td>
                            <td>
                                <g:link controller="template" action="viewExecutedTemplate"
                                        params="[id: executedTemplateQuery?.executedTemplate?.id]">
                                    <g:renderTemplateName executedTemplateQuery="${executedTemplateQuery}" />
                                </g:link>
                            </td>
                            <td class="remove">
                                <g:if test="${executedTemplateQuery.manuallyAdded && (!executedTemplateQuery.reportResult || (executedTemplateQuery.reportResult.executionStatus &&
                                        executedTemplateQuery.reportResult.executionStatus in [ReportExecutionStatusEnum.COMPLETED, ReportExecutionStatusEnum.ERROR, ReportExecutionStatusEnum.WARN]))}">
                                    <asset:image src="icons/trash-icon.png" class="removeSectionIconBtn" data-id="${executedTemplateQuery.id}" data-instancename="${g.renderDynamicReportName(executedTemplateQuery: executedTemplateQuery,executedConfiguration: executedConfiguration, rowId: rowId, columnName : columnName)}"/>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:if test="${!executedConfiguration.executedTemplateQueries}">
                    <tr>
                        <td colspan="3" style="text-align: center"><g:message code="qualityModule.noDataFound.page"/></td>
                    </tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </g:form>
    <div>

    </div>
</div>

<!-- Modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-primary submit-section"><g:message code="default.button.run.label"/></button>
    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
            code="default.button.cancel.label"/></button>
</div>
<asset:javascript src="app/addTemplateSection.js"/>