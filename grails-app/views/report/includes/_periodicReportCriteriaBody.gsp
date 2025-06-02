<%@ page import="com.rxlogix.config.ExecutedCustomSQLQuery; com.rxlogix.Constants; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.config.ExecutedTemplateQuery; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.util.ViewHelper; com.google.gson.annotations.Until; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.enums.ReportFormatEnum; grails.util.Holders" %>

%{--Report Criteria--}%
    <asset:stylesheet src="parameters.css"/>
    <div class="row">
        <g:set var="reportExecutorService" bean="reportExecutorService"/>
        <g:set var="queryService" bean="queryService"/>

        <div class="col-md-12" style="padding-bottom: 10px">
            <label class="rxmain-container-header-label"><g:message code="app.label.reportCriteria"/></label>
        </div>

        <div class="col-md-12">
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.reportName"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:link controller="periodicReport" action="viewExecutedConfig"
                            params="[id: executedConfigurationInstance.id]">
                        ${executedConfigurationInstance.reportName}
                    </g:link>
                </div>
            </div>
            <g:if test="${executedConfigurationInstance.isPublisherReport}">
                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="app.label.versionName"/></label></div>

                    <div class="col-md-${column2Width}">
                            ${executedConfigurationInstance.generatedReportName}
                    </div>
                </div>
            </g:if>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.reportVersion"/></label></div>

                <div class="col-md-${column2Width}">
                    ${executedConfigurationInstance.numOfExecutions}
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.reportDescription"/></label></div>

                <div class="col-md-${column2Width}">
                    ${executedConfigurationInstance.description}
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.periodicReportType"/></label>
                </div>

                <div class="col-md-${column2Width}">
                    <g:message message="${executedConfigurationInstance.periodicReportType}"/>
                </div>
            </div>

            <div class="row">

                <g:if test="${executedConfigurationInstance?.productSelection  || executedConfigurationInstance?.validProductGroupSelection}">
                    <div class="col-md-${column1Width}"><label><g:message code="app.label.productSelection"/></label></div>

                    <div class="col-md-${column2Width}">
                        ${ViewHelper.getDictionaryValues(executedConfigurationInstance, DictionaryTypeEnum.PRODUCT)}
                    </div>
                </g:if>

            </div>

            <div class="row">
                <div class="col-md-${column1Width}">
                    <label>
                        <g:message code="app.label.productDictionary.include.who.drugs"/>
                    </label>
                </div>
                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance?.includeWHODrugs}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}">
                    <label>
                        <g:if test="${Holders.config.safety.source == Constants.PVCM}">
                            <g:message code="app.label.productDictionary.multi.substance"/>
                        </g:if>
                        <g:else>
                            <g:message code="app.label.productDictionary.multi.ingredient"/>
                        </g:else>
                    </label>
                </div>
                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance?.isMultiIngredient}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.studySelection"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:if test="${executedConfigurationInstance?.studySelection}">
                        ${ViewHelper.getDictionaryValues(executedConfigurationInstance, DictionaryTypeEnum.STUDY)}
                    </g:if>
                    <g:else>
                        <g:message code="app.label.none"/>
                    </g:else>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.globalQueryName"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:if test="${executedConfigurationInstance?.executedGlobalQuery}">
                        <g:link controller="query" action="viewExecutedQuery"
                                id="${executedConfigurationInstance?.executedGlobalQuery?.id}">
                            ${executedConfigurationInstance?.executedGlobalQuery?.name}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:message code="app.label.no.query"/>
                    </g:else>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.globalQuery.parameter"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:if test="${executedConfigurationInstance.executedGlobalQueryValueLists}">
                        <g:each in="${executedConfigurationInstance.executedGlobalQueryValueLists}">
                            <div class="bold">
                                <g:if test="${executedConfigurationInstance.executedGlobalQuery.class != ExecutedCustomSQLQuery}">
                                    <span class="showQueryStructure showPopover"
                                          data-content="${queryService.generateReadableQuery(executedConfigurationInstance.executedGlobalQueryValueLists, executedConfigurationInstance.executedGlobalQuery.JSONQuery, executedConfigurationInstance, 0)}">${it.query.name}</span>
                                </g:if>
                                <g:else>
                                    <span>${it.query.name}</span>
                                </g:else>
                            </div>

                            <div>
                                <g:each in="${it.parameterValues}">
                                    <div class="left-indent">
                                        <g:if test="${it.hasProperty('reportField')}">
                                            <g:if test="${it.value}">
                                                <g:message code="app.reportField.${it.reportField.name}"/>
                                                <g:message code="${it.operator.getI18nKey()}"/>
                                                ${it.value?.tokenize(';')?.join(', ')}
                                            </g:if>
                                        </g:if>
                                        <g:else>
                                            <g:if test="${it.value}">
                                                ${it.key} = ${it.value}
                                            </g:if>
                                        </g:else>
                                    </div>
                                </g:each>
                            </div>
                        </g:each>
                    </g:if>
                    <g:else>
                        <div><g:message code="app.label.none"/></div>
                    </g:else>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message
                        code="app.label.globalDateRangInformation"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:if test="${executedConfigurationInstance?.executedGlobalDateRangeInformation}">
                        <g:renderDateRangeInformation
                                executedDateRangeInformation="${executedConfigurationInstance.executedGlobalDateRangeInformation}"/>
                    </g:if>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.source.profiles.label"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:message code="${executedConfigurationInstance.sourceProfile.sourceName}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.EvaluateCaseDateOn"/></label>
                </div>

                <div class="col-md-${column2Width}">
                    <g:message code="${executedConfigurationInstance.evaluateDateAs.getI18nKey()}"/>
                </div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.DateRangeType"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:message code="${executedConfigurationInstance.dateRangeType.getI18nKey()}"/>
                </div>
            </div>


            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message
                        code="reportCriteria.exclude.non.valid.cases"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance.excludeNonValidCases}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>


            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message
                        code="reportCriteria.exclude.deleted.cases"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance.excludeDeletedCases}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>


            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="reportCriteria.exclude.follow.up"/></label>
                </div>

                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance.excludeFollowUp}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.SuspectProduct"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance.suspectProduct}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message
                        code="reportCriteria.include.previous.missing.cases"/></label>
                </div>

                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance.includePreviousMissingCases}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message
                        code="app.label.includeOpenCasesInDraft"/></label>
                </div>

                <div class="col-md-${column2Width}">
                    <g:formatBoolean boolean="${executedConfigurationInstance.includeOpenCasesInDraft}"
                                     true="${message(code: "default.button.yes.label")}"
                                     false="${message(code: "default.button.no.label")}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message
                        code="app.label.includeAllStudyDrugsCases"/></label>
                </div>

                <div class="col-md-${column2Width}">
                    <g:if test="${includeAllStudyDrugsCases?.length()>0}">
                        ${includeAllStudyDrugsCases}
                    </g:if>
                    <g:else>
                        <g:formatBoolean boolean="${executedConfigurationInstance.includeAllStudyDrugsCases}"
                                         true="${message(code: "default.button.yes.label")}"
                                         false="${message(code: "default.button.no.label")}"/>
                    </g:else>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.reportOwner"/></label></div>

                <div class="col-md-${column2Width}">
                    ${executedConfigurationInstance.owner.fullNameAndUserName}
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.runDateAndTime"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:render template="/includes/widgets/dateDisplayWithTimezone"
                              model="[date: isFinal ? executedConfigurationInstance.finalLastRunDate : executedConfigurationInstance.lastRunDate, showTimeZone: true]"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.last.successful.etl.start.time"/></label></div>

                <div class="col-md-${column2Width}">
                    ${etlRunTime}
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.comment"/></label></div>

                <div class="col-md-${column2Width}">
                    <a href="#" class="commentModalTrigger" data-owner-id="${executedConfigurationInstance?.id}" data-comment-type="${CommentTypeEnum.EXECUTED_CONFIGURATION}"
                       data-toggle="modal" data-target="#commentModal">
                        <g:renderAnnotateIcon comments="${comments}" class="word-icon" style="font-size: 20px" title="${message(code: "app.label.comment")}"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div class="margin20Top"></div>

%{--Report Sections Criteria--}%
    <div class="row">

        <div class="col-md-12" style="padding-bottom: 10px">
            <div style="float: left; width: 90%">
                <label class="rxmain-container-header-label"><g:message code="report.sections.criteria"/></label>
                <g:if test="${executedConfigurationInstance.status==ReportExecutionStatusEnum.GENERATED_DRAFT}">
                    <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_VIEW">
                        <span class="addSectionLink">
                            <a data-url="${createLink(controller: 'periodicReport', action: "addSection", id: executedConfigurationInstance.id)}"
                               href="#" data-toggle="modal" data-target="#addSectionModal"><asset:image
                                    src="icons/add_section_icon.png"/></a>
                        </span>
                    </sec:ifAnyGranted>
                </g:if>
            </div>

        </div>
    %{--View Entire Report link--}%
        <g:if test="${(executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus().size() > 1) && !executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus().find{sec-> !sec.isVisible()} && !(executedConfigurationInstance.status in [ReportExecutionStatusEnum.GENERATED_CASES,ReportExecutionStatusEnum.inProgressStatusesList].flatten())}">
            <div class="col-md-12">

                <div class="row">

                    <div class="col-md-2" style="padding-bottom: 20px;">
                        <g:link action="viewMultiTemplateReport" params="[id: executedConfigurationInstance.id, isInDraftMode: params.isInDraftMode]"><g:message
                                code="view.entire.report"/></g:link>
                    </div>

                    <div class="col-md-6" style="padding-bottom: 20px;">

                            <a href="javascript:void(0)"
                               data-url="${createLink(controller: 'report', action: 'viewMultiTemplateReport', params: [id: params.id, outputFormat: ReportFormatEnum.PDF.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                               data-name="${executedConfigurationInstance.reportName}.pdf"
                               class="downloadUrl"  style="margin-right: 20px; ${ViewHelper.isNotExportable(executedConfigurationInstance, null, ReportFormatEnum.PDF, ReportTypeEnum.MULTI_REPORT)? 'pointer-events: none' : ''}">
                                <asset:image src="pdf-icon.png" class="pdf-icon" height="24" width="24"/> <g:message code="save.as.pdf" /></a>

                            <a href="javascript:void(0)"
                               data-url="${createLink(controller: 'report', action: 'viewMultiTemplateReport', params: [id: params.id, outputFormat: ReportFormatEnum.XLSX.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                               data-name="${executedConfigurationInstance.reportName}.xsls"
                               class="downloadUrl"  style="margin-right: 20px; ${ViewHelper.isNotExportable(executedConfigurationInstance, null, ReportFormatEnum.XLSX, ReportTypeEnum.MULTI_REPORT)? 'pointer-events: none' : ''}">
                                <asset:image src="excel.gif" class="excel-icon" height="24" width="24"/> <g:message code="save.as.excel" /></a>

                            <a href="javascript:void(0)"
                               data-url="${createLink(controller: 'report', action: 'viewMultiTemplateReport', params: [id: params.id, outputFormat: ReportFormatEnum.DOCX.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                               data-name="${executedConfigurationInstance.reportName}.docx"
                               class="downloadUrl"  style="margin-right: 20px; ${ViewHelper.isNotExportable(executedConfigurationInstance, null, ReportFormatEnum.DOCX, ReportTypeEnum.MULTI_REPORT)? 'pointer-events: none' : ''}">
                                <asset:image src="word-icon.png" class="word-icon" height="24" width="24"/> <g:message code="save.as.word" /></a>


                            <a href="javascript:void(0)"
                               data-url="${createLink(controller: 'report', action: 'viewMultiTemplateReport', params: [id: params.id, outputFormat: ReportFormatEnum.PPTX.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                               data-name="${executedConfigurationInstance.reportName}.pptx"
                               class="downloadUrl"  style="margin-right: 20px; ${ViewHelper.isNotExportable(executedConfigurationInstance, null, ReportFormatEnum.PPTX, ReportTypeEnum.MULTI_REPORT)? 'pointer-events: none' : ''}">
                                <asset:image src="powerpoint-icon.png" class="powerpoint-icon" height="24" width="24"/> <g:message code="save.as.powerpoint" /></a>
                        </div>
                    </div>
            </div>



        </g:if>

        <div class="col-md-12">
            <table class="table table-striped table-bordered">
                <thead class="sectionsHeader">
                <tr class="criteria-th-bg">
                    <th width="10%"><g:message code="app.label.sectionTitle"/></th>
                    <th width="13%"><g:message code="app.label.template"/></th>
                    <th width="12%"><g:message code="app.label.query"/></th>
                    <th width="15%"><g:message code="app.label.parameters"/></th>
                    <th width="10%"><g:message code="app.label.DateRange"/></th>
                    <th width="10%"><g:message code="app.label.EvaluateCaseDateOn"/></th>
                    <th width="7%"><g:message code="app.label.queryLevel"/></th>
                    <th width="4%"><g:message code="app.label.caseCount"/></th>
                    <th width="4%"><g:message code="app.label.draftOnly"/></th>
                    <th width="11%"><g:message code="app.label.saveAs"/></th>

                </tr>
                </thead>
                <tbody style="overflow-wrap: anywhere">

                <g:each var="executedTemplateQuery" in="${executedTemplateQueriesForProcessing}">
                    <g:set var="reportResult"
                           value="${executedTemplateQuery.getReportResult(Boolean.valueOf(params.isInDraftMode ?: false))}"/>
                    <tr>
                        <td>
                            <g:if test="${!reportResult || reportResult.executionStatus == ReportExecutionStatusEnum.ERROR}">
                                <div style="text-align: center"><i class="fa fa-exclamation-triangle" title="${g.message(code: 'app.label.section.not.generated')}"></i></div>
                            </g:if>
                            <g:elseif test="${reportResult.executionStatus != ReportExecutionStatusEnum.COMPLETED}">
                                <div style="text-align: center"><i class="fa fa-clock-o fa-lg es-scheduled fa-lg es-scheduled" title="${g.message(code: 'app.label.scheduled')}"></i></div>
                            </g:elseif>
                            <g:else>
                                <g:if test="${executedTemplateQuery.isVisible()}">
                                <g:link controller="report" action="show" target="${ViewHelper.isPvPModule(request)?'_blank':''}"
                                        params="[id: reportResult.id, isInDraftMode: params.isInDraftMode]">
                                    <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}"
                                                               executedTemplateQuery="${executedTemplateQuery}"
                                                               hideSubmittable="${true}" />
                                </g:link>
                                </g:if>
                                    <g:else>
                                        <span class="fa fa-ban" title="${g.message(code: 'app.label.PublisherTemplate.error.section.message')}"></span>
                                        <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}"
                                                                   executedTemplateQuery="${executedTemplateQuery}"
                                                                   hideSubmittable="${true}" />
                                    </g:else>
                            </g:else>

                        </td>
                        <td>
                            <g:link controller="template" action="viewExecutedTemplate" target="${ViewHelper.isPvPModule(request)?'_blank':''}"
                                    params="[id: executedTemplateQuery?.executedTemplate?.id]">
                                <g:renderTemplateName executedTemplateQuery="${executedTemplateQuery}" />
                            </g:link>
                        </td>
                        <td>
                            <g:if test="${executedTemplateQuery?.executedQuery}">
                                <g:link controller="query" action="viewExecutedQuery"
                                        params="[id: executedTemplateQuery?.executedQuery?.id]">
                                    ${executedTemplateQuery?.executedQuery?.name}
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:message code="app.label.no.query"/>
                            </g:else>

                        </td>
                        <td>
                            <g:if test="${executedTemplateQuery.executedTemplateValueLists || executedTemplateQuery.showReassessDateDiv()}">
                                <div class="italic">
                                    <g:message code="app.label.template"/>
                                </div>
                                <g:each in="${executedTemplateQuery.executedTemplateValueLists}">
                                    <div class="bold">
                                        ${it.template.name}
                                    </div>

                                    <div>
                                        <g:each in="${it.parameterValues}">
                                            <div class="left-indent">
                                                <g:if test="${it.value}">
                                                    ${it.key} = ${it.value}
                                                </g:if>
                                            </div>
                                        </g:each>
                                    </div>
                                </g:each>
                                <g:if test="${executedTemplateQuery.showReassessDateDiv()}">
                                    <div class="left-indent">
                                        <g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/> :
                                        ${renderShortFormattedDate(date: executedTemplateQuery.templtReassessDate)}
                                    </div>
                                </g:if>
                            </g:if>

                            <g:if test="${executedTemplateQuery.executedQueryValueLists || executedTemplateQuery?.showQueryReassessDateDiv()}">
                                <div class="italic">
                                    <g:message code="app.label.query"/>
                                </div>
                                <g:each in="${executedTemplateQuery.executedQueryValueLists}">
                                    <div class="bold">
                                        <g:if test="${executedTemplateQuery.executedQuery.class != ExecutedCustomSQLQuery}">
                                            <span class="showQueryStructure showPopover"
                                                  data-content="${queryService.generateReadableQueryFromExTemplateQuery(executedTemplateQuery, 0)}">${it.query.name}</span>
                                        </g:if>
                                        <g:else>
                                            <span>${it.query.name}</span>
                                        </g:else>
                                    </div>

                                    <div>
                                        <g:each in="${it.parameterValues}">
                                            <div class="left-indent">
                                                <g:if test="${it.hasProperty('reportField')}">
                                                    <g:if test="${it.value}">
                                                        <g:message code="app.reportField.${it.reportField.name}"/>
                                                        <g:message code="${it.operator.getI18nKey()}"/>
                                                        ${it.value?.tokenize(';')?.join(', ')}
                                                    </g:if>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${it.value}">
                                                        ${it.key} = ${it.value}
                                                    </g:if>
                                                </g:else>
                                            </div>
                                        </g:each>
                                    </div>
                                </g:each>
                                <g:if test="${executedTemplateQuery?.showQueryReassessDateDiv()}">
                                    <div class="left-indent">
                                        <g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/> :
                                        ${renderShortFormattedDate(date: executedTemplateQuery.reassessListednessDate)}
                                    </div>
                                </g:if>
                            </g:if>
                            <g:if test="${executedTemplateQuery.onDemandSectionParams}">
                                <div class="italic">
                                    <g:message code="app.label.drilldownTo"/><br>
                                    <span>${reportExecutorService.getDrilldownOnDemandSection(executedTemplateQuery.onDemandSectionParams)}</span>
                                </div>
                            </g:if>

                            <g:if test="${!executedTemplateQuery.executedTemplateValueLists && !executedTemplateQuery.executedQueryValueLists && !executedTemplateQuery.onDemandSectionParams && !executedTemplateQuery?.showReassessDateDiv() && !executedTemplateQuery?.showQueryReassessDateDiv()}">
                                <div><g:message code="app.label.none"/></div>
                            </g:if>
                        </td>
                        <td>
                            <g:dateRangeValueForCriteria
                                    executedTemplateQuery="${executedTemplateQuery}"/>
                        </td>
                        <td>
                            <g:renderShortFormattedDate
                                    date="${executedConfigurationInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD ? executedTemplateQuery.getGlobalEndDate() :
                                            executedTemplateQuery.executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate}"
                                    timeZone="${TimeZone.getTimeZone("UTC")}"/>
                        </td>
                        <td>
                            <g:message code="${executedTemplateQuery.queryLevel.i18nKey}"/>
                        </td>
                        <td>
                            <g:if test="${(reportResult?.executionStatus != ReportExecutionStatusEnum.ERROR) && reportResult?.caseCount}">
                                <div>${reportResult.caseCount}</div>
                            </g:if>
                            <g:else>
                                <g:message code="app.label.notAvailable"/>
                            </g:else>
                        </td>
                        <td>
                            <g:formatBoolean boolean="${executedTemplateQuery.draftOnly}" true="Yes" false="No"/>
                        </td>

                        <td style="width:30%">
                            <g:if test="${!reportResult || reportResult.executionStatus == ReportExecutionStatusEnum.ERROR}">
                                <div style="text-align: center"><i class="fa fa-exclamation-triangle" title="${g.message(code: 'app.label.section.not.generated')}"></i></div>
                            </g:if>
                            <g:elseif test="${reportResult.executionStatus != ReportExecutionStatusEnum.COMPLETED}">
                                <div style="text-align: center"><i class="fa fa-clock-o fa-lg es-scheduled fa-lg es-scheduled" title="${g.message(code: 'app.label.scheduled')}"></i></div>
                            </g:elseif>
                            <g:else>
                                <g:if test="${executedTemplateQuery.isVisible()}">

                                    <a href="javascript:void(0)"
                                       data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.PDF.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                                       data-name="${renderDynamicReportName(executedConfiguration:executedConfigurationInstance, executedTemplateQuery:executedTemplateQuery, hideSubmittable:true).replaceAll(":","_")}.pdf"
                                       class="downloadUrl"       style="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.PDF, reportType) ? 'pointer-events: none' : ''}">
                                        <asset:image src="pdf-icon.png" class="pdf-icon" height="24" width="24"/></a>


                                    <a href="javascript:void(0)"
                                       data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.XLSX.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                                       data-name="${renderDynamicReportName(executedConfiguration:executedConfigurationInstance, executedTemplateQuery:executedTemplateQuery, hideSubmittable:true).replaceAll(":","_")}.XLSX"
                                       class="downloadUrl"       style="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.XLSX, reportType) ? 'pointer-events: none' : ''}">
                                        <asset:image src="excel.gif" class="excel-icon" height="24" width="24"/></a>

                                    <a href="javascript:void(0)"
                                       data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.DOCX.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                                       data-name="${renderDynamicReportName(executedConfiguration:executedConfigurationInstance, executedTemplateQuery:executedTemplateQuery, hideSubmittable:true).replaceAll(":","_")}.DOCX"
                                       class="downloadUrl"       style="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.DOCX, reportType) ? 'pointer-events: none' : ''}">
                                        <asset:image src="word-icon.png" class="word-icon" height="24" width="24"/></a>


                                    <a href="javascript:void(0)"
                                       data-url="${createLink(controller: 'report', action: 'show', params: [id: reportResult.id, outputFormat: ReportFormatEnum.PPTX.name(), isInDraftMode: params.isInDraftMode], absolute: true)}"
                                       data-name="${renderDynamicReportName(executedConfiguration:executedConfigurationInstance, executedTemplateQuery:executedTemplateQuery, hideSubmittable:true).replaceAll(":","_")}.PPTX"
                                       class="downloadUrl"       style="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateQuery.executedTemplate, ReportFormatEnum.PPTX, reportType) ? 'pointer-events: none' : ''}">
                                        <asset:image src="powerpoint-icon.png" class="powerpoint-icon" height="24" width="24"/></a>
                                <g:if test="${(executedTemplateQuery?.executedTemplate instanceof CaseLineListingTemplate) && executedTemplateQuery.hasCaseNumberAndReportData()}">
                                    <a href="#" data-toggle="modal" data-target="#saveCaseSeries"
                                       data-title="${executedTemplateQuery.caseSeriesName}"
                                       title="${g.message(code: 'app.label.save.caseSeries')}"
                                       data-id="${executedTemplateQuery.id}">
                                        <asset:image src="/icons/caseSeries.png" height="24" width="24"/>
                                    </a>
                                </g:if>
                                </g:if>
                                <g:else>
                                    <span class="fa fa-ban" title="${g.message(code: 'app.label.PublisherTemplate.error.section.message')}"></span>
                                </g:else>
                            </g:else>
                            <g:set var="canDelete" value="${executedTemplateQuery.manuallyAdded && ( !reportResult || (reportResult.executionStatus &&
                                    reportResult.executionStatus in [ReportExecutionStatusEnum.COMPLETED, ReportExecutionStatusEnum.ERROR, ReportExecutionStatusEnum.WARN]))}"/>

                            <g:if test="${executedTemplateQueriesForProcessing.size() > 1 && !executedConfigurationInstance.hasReachedToFinal() && canDelete}">
                                <i class="glyphicon glyphicon-trash removeIconBtn fa-lg" style="padding-top: 0px;top: 5px;" data-id="${executedTemplateQuery.id}"
                                   data-instancename="${g.renderDynamicReportName(executedTemplateQuery: executedTemplateQuery,executedConfiguration: executedConfigurationInstance, hideSubmittable:true)}"/>
                            </g:if>

                        </td>

                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

    </div>

    <g:if test="${viewSql}">
        <div class="row">
            <div class="col-md-12">
                <g:each in="${viewSql}">
                    <h3><g:message code="app.templatequery.id"/>: ${it.id}</h3>
                    <table class="table table-striped table-bordered">
                        <thead class="sectionsHeader">
                        <tr>
                            <th width="5%"><g:message code="app.label.viewSql.id"/></th>
                            <th width="10%"><g:message code="app.label.viewSql.scriptName"/></th>
                            <th width="70%"><g:message code="app.label.viewSql.executingSql"/></th>
                            <th width="10%"><g:message code="app.label.viewSql.executionTime"/></th>
                            <th width="5%"><g:message code="app.label.viewSql.rowsUpdated"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${it.data}" var="viewSqlDTO">
                            <tr>
                                <td>${viewSqlDTO.rowId}<br></td>
                                <td>${viewSqlDTO.scriptName}<br></td>
                                <td style="word-break: break-all;">${viewSqlDTO.executingSql}<br></td>
                                <td style="text-align: center">${viewSqlDTO.executionTime}<br></td>
                                <td style="text-align: center">${viewSqlDTO.rowsUpsert}<br></td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </g:each>
            </div>
        </div>
    </g:if>



