<%@ page import="com.rxlogix.config.ExecutedCustomSQLQuery; com.rxlogix.Constants; grails.util.Holders; com.rxlogix.config.Tenant; com.rxlogix.config.Query; com.rxlogix.config.ExecutedTemplateQuery; com.rxlogix.config.TemplateQuery; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.util.RelativeDateConverter; com.rxlogix.config.ReportConfiguration; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeValueEnum; com.rxlogix.config.Configuration" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ViewReport.title"/></title>

    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/viewScheduler.js"/>
    <asset:stylesheet src="parameters.css"/>
    <asset:javascript src="app/reportTask.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <script>
        var aggregateReportViewTaskMode = true;
        var pageType = 'show';
    </script>
</head>

<body>
<div class="content">
    <div class="container">
        <g:set var="queryService" bean="queryService"/>
        <div>
<g:if test="${isExecuted}">
    <g:set var="title" value="${message(code: "app.label.viewExecutedConfiguration")}"/>
</g:if>
<g:else>
    <g:set var="title" value="${message(code: "app.label.viewConfiguration")}"/>
</g:else>
<rx:container title="${title}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="app.label.report"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.reportName"/></label>

                                <div class="word-wrapper">${configurationInstance.reportName.encodeAsHTML()}</div>
                            </div>
                        </div>
                        <g:if test="${!configurationInstance.pvqType}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.description"/></label>

                                <g:if test="${configurationInstance.description}">
                                    <div class="word-wrapper">${configurationInstance.description.encodeAsHTML()}</div>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:message code="app.label.none"/>
                                    </div>
                                </g:else>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.SuspectProduct"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.suspectProduct}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>

                        <g:if test="${configurationInstance?.productSelection || configurationInstance.validProductGroupSelection || configurationInstance?.studySelection}">
                            <div class="row">
                                <div class="col-xs-12">
                                    <g:if test="${configurationInstance.productSelection || configurationInstance.validProductGroupSelection}">
                                        <label><g:message code="app.productDictionary.label"/></label>

                                        <div id="showProductSelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
                                    </g:if>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <g:if test="${configurationInstance.studySelection}">
                                        <label><g:message code="app.studyDictionary.label"/></label>

                                        <div id="showStudySelection"></div>
                                        ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.STUDY)}
                                    </g:if>
                                </div>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.productSelection"/></label>
                                    <div></div>
                                    <g:message code="app.label.none"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.studySelection"/></label>
                                    <div></div>
                                    <g:message code="app.label.none"/>
                                </div>
                            </div>
                        </g:else>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label>
                                        <g:message code="app.label.productDictionary.include.who.drugs"/>
                                    </label>

                                    <div><g:formatBoolean boolean="${configurationInstance?.includeWHODrugs}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label>
                                        <g:if test="${Holders.config.safety.source == Constants.PVCM}">
                                            <g:message code="app.label.productDictionary.multi.substance"/>
                                        </g:if>
                                        <g:else>
                                            <g:message code="app.label.productDictionary.multi.ingredient"/>
                                        </g:else>
                                    </label>
                                    <div><g:formatBoolean boolean="${configurationInstance?.isMultiIngredient}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/></div>
                                </div>
                            </div>
                            <g:if test="${configurationInstance instanceof com.rxlogix.config.Configuration}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.qbeForm"/></label>

                                        <div><g:formatBoolean boolean="${configurationInstance?.qbeForm}"
                                                              true="${message(code: "default.button.yes.label")}"
                                                              false="${message(code: "default.button.no.label")}"/></div>
                                    </div>
                                </div>
                            </g:if>
                        </g:if>
                        <g:else>
                            <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="qualityModule.ad.hoc.alert.button"/></label>
                                <div class="word-wrapper">${configurationInstance.pvqType}</div>
                            </div>
                        </div>
                        </g:else>
                    </div>
                    <g:if test="${!configurationInstance.pvqType}">
                    <div class="col-xs-3">

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.qualityChecked"/></label>
                                <div>
                                    <g:formatBoolean boolean="${configurationInstance.qualityChecked}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                                </div>
                            </div>
                        </div>
                        <g:if test="${configurationInstance instanceof ReportConfiguration}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.template"/></label>
                                <div>
                                    <g:formatBoolean boolean="${configurationInstance.isTemplate}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                                </div>
                            </div>
                        </div>
                        </g:if>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.tags"/></label>
                                <g:if test="${configurationInstance.tags}">
                                    <g:each in="${configurationInstance.tags}">
                                        <div>${it.name}</div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.eventSelection.limit.primary.path"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.limitPrimaryPath}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.eventDictId.label"/></label>
                                <g:if test="${configurationInstance.eventSelection || configurationInstance.usedValidEventGroupSelection}">
                                    <div id="showEventSelection"></div>
                                    <g:hiddenField name="editable" id="editable" value="false"/>
                                    ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.EVENT)}
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>
                            </div>
                        </div>
                        %{--Global Query Link Section--}%
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.globalQueryName"/></label>
                                <g:if test="${isExecuted}">
                                    <g:if test="${configurationInstance.executedGlobalQuery}">
                                        <div>
                                            <g:link controller="query" action="viewExecutedQuery"
                                                    id="${configurationInstance.executedGlobalQuery.id}"
                                                    params="[isExecuted: 'true']">${configurationInstance.executedGlobalQuery.name}</g:link>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:if test="${configurationInstance.globalQuery}">
                                        <div>
                                            <g:link controller="query" action="view"
                                                    id="${configurationInstance.globalQuery.id}"
                                                    params="[isExecuted: 'false']">${configurationInstance.globalQuery.name}</g:link>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </g:else>
                            </div>
                        </div>
                    </div>
                    </g:if>
                    <div class="col-xs-3">
                    <g:if test="${!configurationInstance.pvqType}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="userGroup.source.profiles.label"/></label>

                                <div><g:message code="${configurationInstance.sourceProfile.sourceName.encodeAsHTML()}"/></div>
                            </div>
                        </div>
                        <g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.tenant"/></label>

                                    <div>
                                        ${Tenant.read(configurationInstance.tenantId)?.name.encodeAsHTML()}
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </g:if>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.DateRangeType"/></label>

                                <div><g:message code="${configurationInstance.dateRangeType?.i18nKey}"/></div>
                            </div>
                        </div>
                        <g:if test="${!configurationInstance.pvqType}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.EvaluateCaseDateOn"/></label>

                                <div id="evaluateCaseDate">
                                    <g:message
                                            code="${(EvaluateCaseDateEnum.(configurationInstance.evaluateDateAs)?.i18nKey)}"/>
                                    <g:if test="${configurationInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF}">
                                        <div>
                                            <g:renderShortFormattedDate date="${configurationInstance.asOfVersionDate}"/>
                                        </div>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.useCaseSeries"/></label>
                               <div>
                                   <g:if test="${isExecuted}">
                                       <g:if test="${configurationInstance.usedCaseSeries}">
                                           <g:link controller="caseList" action="index"
                                                   params="[viewOnly: true, cid: configurationInstance.usedCaseSeries.id]">${configurationInstance.usedCaseSeries.seriesName}</g:link>
                                       </g:if>
                                       <g:else>
                                           <div>
                                               <g:message code="app.label.none"/>
                                           </div>
                                       </g:else>
                                   </g:if>
                                   <g:else>
                                       <g:if test="${configurationInstance.useCaseSeries}">
                                           <g:link controller="caseList" action="index"
                                                   params="[viewOnly: true, cid: configurationInstance.useCaseSeries.id]">${configurationInstance.useCaseSeries.seriesName}</g:link>
                                       </g:if>
                                       <g:else>
                                           <div>
                                               <g:message code="app.label.none"/>
                                           </div>
                                       </g:else>
                                   </g:else>
                               </div>
                            </div>
                        </div>
                        </g:if>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.DateRange"/></label>
                                <g:if test="${isExecuted}">
                                    <g:if test="${configurationInstance.executedGlobalDateRangeInformation?.dateRangeEnum}">
                                        <div>
                                            <g:message
                                                    code="${(configurationInstance.executedGlobalDateRangeInformation?.dateRangeEnum?.i18nKey)}"/>
                                            <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.executedGlobalDateRangeInformation?.dateRangeEnum))}">
                                                <input type="hidden" id="dateRangeValueRelative"
                                                       value="${(configurationInstance.executedGlobalDateRangeInformation?.dateRangeEnum)}"/>

                                                <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.executedGlobalDateRangeInformation?.relativeDateRangeValue)}</div>
                                            </g:if>
                                        </div>
                                        <g:if test="${configurationInstance.executedGlobalDateRangeInformation?.dateRangeEnum == com.rxlogix.enums.DateRangeEnum.CUSTOM}">
                                            <g:if test="${configurationInstance.executedGlobalDateRangeInformation?.dateRangeStartAbsolute}">
                                                <div>
                                                    <label><g:message code="app.label.start"/></label>

                                                    <div><g:renderShortFormattedDate date="${configurationInstance.executedGlobalDateRangeInformation?.dateRangeStartAbsolute}"
                                                    /></div>
                                                    <label><g:message code="app.label.end"/></label>

                                                    <div><g:renderShortFormattedDate date="${configurationInstance.executedGlobalDateRangeInformation?.dateRangeEndAbsolute}"
                                                    /></div>
                                                </div>
                                            </g:if>
                                        </g:if>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:if test="${configurationInstance?.globalDateRangeInformation?.dateRangeEnum}">
                                        <div>
                                            <g:message
                                                    code="${(configurationInstance.globalDateRangeInformation?.dateRangeEnum?.i18nKey)}"/>
                                            <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.globalDateRangeInformation?.dateRangeEnum))}">
                                                <input type="hidden" id="dateRangeValueRelative"
                                                       value="${(configurationInstance.globalDateRangeInformation?.dateRangeEnum)}"/>

                                                <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.globalDateRangeInformation?.relativeDateRangeValue)}</div>
                                            </g:if>
                                        </div>

                                        <g:if test="${configurationInstance.globalDateRangeInformation?.dateRangeStartAbsolute}">
                                            <div>
                                                <label><g:message code="app.label.start"/></label>

                                                <div><g:renderShortFormattedDate date="${configurationInstance.globalDateRangeInformation?.dateRangeStartAbsolute}"
                                                /></div>
                                                <label><g:message code="app.label.end"/></label>

                                                <div><g:renderShortFormattedDate date="${configurationInstance.globalDateRangeInformation?.dateRangeEndAbsolute}"
                                                /></div>
                                            </div>
                                        </g:if>
                                    </g:if>
                                </g:else>
                            </div>
                        </div>
                        <g:if test="${!configurationInstance.pvqType}">
                        %{--Global Query Values Section--}%
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.parameters"/></label>
                                <div>
                                    -- <g:message code="app.label.query"/> --
                                </div>
                                <g:if test="${isExecuted}">
                                    <g:if test="${configurationInstance.executedGlobalQueryValueLists}">
                                        <g:each in="${configurationInstance.executedGlobalQueryValueLists}">
                                            <div class="bold left-indent">
                                                <g:if test="${configurationInstance.executedGlobalQuery.class != ExecutedCustomSQLQuery}">
                                                    <span class="showQueryStructure showPopover"
                                                          data-content="${queryService.generateReadableQuery(configurationInstance.executedGlobalQueryValueLists, configurationInstance.executedGlobalQuery.JSONQuery, configurationInstance, 0)}">${it.query.name}</span>
                                                </g:if>
                                                <g:else>
                                                    <span>${it.query.name}</span>
                                                </g:else>
                                            </div>

                                            <div class="left-indent">
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
                                </g:if>
                                <g:else>
                                    <g:if test="${configurationInstance.globalQueryValueLists}">
                                        <g:each in="${configurationInstance.globalQueryValueLists}">
                                            <div class="bold left-indent">
                                                ${it.query.name}
                                            </div>

                                            <div class="left-indent">
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
                                </g:else>
                            </div>
                        </div>
                            <br/>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.poi.parameters" /></label>
                                <g:if test="${configurationInstance.poiInputsParameterValues}">
                                    <g:each in="${configurationInstance.poiInputsParameterValues}" var="parameterValue">
                                        <div>
                                            <g:if test="${parameterValue.value}">
                                                ${parameterValue.key} = ${parameterValue.value}
                                            </g:if>
                                            <g:else>
                                                ${parameterValue.key}
                                            </g:else>
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:message code="app.label.none"/>
                                    </div>
                                </g:else>
                            </div>
                        </div>
                        </g:if>
                    </div>
                    <g:if test="${!configurationInstance.pvqType}">
                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.exclude.follow.up"/></label>

                                <div>
                                    <g:formatBoolean boolean="${configurationInstance?.excludeFollowUp}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.include.locked.versions.only"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.includeLockedVersion}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.include.all.study.drugs.cases"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.includeAllStudyDrugsCases}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.exclude.non.valid.cases"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.excludeNonValidCases}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.exclude.deleted.cases"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.excludeDeletedCases}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>


                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.include.medically.confirm.cases" /></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.includeMedicallyConfirmedCases}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="reportCriteria.include.non.significant.followup.cases"/></label>

                                <div><g:formatBoolean boolean="${configurationInstance?.includeNonSignificantFollowUp}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/></div>
                            </div>
                        </div>
                    </div>
                    </g:if>
                </div>
            </div>
        </div>

        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.criteria.sections"/></label>
            </div>
        </div>
        <g:each var="templateQuery" in="${templateQueries}">
            <div class="row">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.templateName"/></label>

                            <div>
                                <g:if test="${isExecuted}">
                                    <div>
                                        <g:link controller="template" action="viewExecutedTemplate"
                                                id="${templateQuery.executedTemplate.id}">${templateQuery.executedTemplate.name}</g:link>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:link controller="template" action="view"
                                                id="${templateQuery.template.id}">${templateQuery.template.name}</g:link>
                                    </div>
                                </g:else>
                            </div>

                            <g:if test="${templateQuery.header}">
                                <div class="row">
                                    <div class="col-xs-6 sec-title">
                                        <label><g:message code="app.label.sectionHeader"/></label>
                                    </div>
                                    <div class="col-xs-6 sec-value">
                                        ${templateQuery.header}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${templateQuery.title}">
                                <div class="row">
                                    <div class="col-xs-6 sec-title">
                                        <label><g:message code="app.label.sectionTitle"/></label>
                                    </div>
                                    <div class="col-xs-6 sec-value">
                                        ${templateQuery.title}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${templateQuery.footer}">
                                <div class="row">
                                    <div class="col-xs-6 sec-title">
                                        <label><g:message code="app.label.sectionFooter"/></label>
                                    </div>
                                    <div class="col-xs-6 sec-value">
                                        ${templateQuery.footer}
                                    </div>
                                </div>
                            </g:if>
                            <div class="row">
                                <div class="col-xs-6 sec-title">
                                    <label><g:message code="templateQuery.headerProductSelection.label"/></label>
                                </div>
                                <div class="col-xs-6 sec-value">
                                    <g:formatBoolean boolean="${templateQuery?.headerProductSelection}"
                                                      true="${message(code: "default.button.yes.label")}"
                                                      false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-6 sec-title">
                                    <label><g:message code="templateQuery.headerDateRange.label"/></label>
                                </div>
                                <div class="col-xs-6 sec-value">
                                    <g:formatBoolean boolean="${templateQuery?.headerDateRange}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-6 sec-title">
                                    <label><g:message code="templateQuery.displayMedDraVersionNumber.label"/></label>
                                </div>
                                <div class="col-xs-6 sec-value">
                                    <g:formatBoolean boolean="${templateQuery?.displayMedDraVersionNumber}"
                                                     true="${message(code: "default.button.yes.label")}"
                                                     false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <g:if test="${templateQuery.granularity}">
                        <div class="row">
                            <div class="col-xs-6">
                                <g:message code="app.label.granularity" />
                                <g:message code="${templateQuery.granularity.i18nKey}" />
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${templateQuery.privacyProtected}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="templateQuery.privacyProtected.label"/></label>
                                <div>
                                    <g:formatBoolean boolean="${templateQuery.privacyProtected}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${templateQuery.blindProtected}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="templateQuery.blindProtected.label"/></label>
                                <div>
                                    <g:formatBoolean boolean="${templateQuery.blindProtected}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.queryName"/></label>
                            <g:if test="${isExecuted}">
                                <g:if test="${templateQuery.executedQuery}">
                                    <div>
                                        <g:link controller="query" action="viewExecutedQuery"
                                                id="${templateQuery.executedQuery.id}"
                                                params="[isExecuted: 'true']">${templateQuery.executedQuery.name}</g:link>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:message code="app.label.none"/>
                                    </div>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:if test="${templateQuery.query}">
                                    <div>
                                        <g:link controller="query" action="view" id="${templateQuery.query.id}"
                                                params="[isExecuted: 'false']">${templateQuery.query.name}</g:link>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div>
                                        <g:message code="app.label.none"/>
                                    </div>
                                </g:else>
                            </g:else>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.DateRange"/></label>

                            <g:if test="${isExecuted}">
                                <g:if test="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum}">
                                    <div>
                                        <g:message
                                                code="${(templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum?.i18nKey)}"/>
                                        <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum))}">
                                            <input type="hidden" id="dateRangeValueRelative"
                                                   value="${(templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum)}"/>

                                            <div id="relativeDateRangeValueX">where, X = ${(templateQuery?.executedDateRangeInformationForTemplateQuery?.relativeDateRangeValue)}</div>
                                        </g:if>
                                    </div>
                                    <g:if test="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUSTOM}">
                                        <div class="row">
                                            <div class="col-xs-12">
                                                <div>
                                                    <label><g:message code="app.label.start"/></label>
                                                </div>

                                                <div>
                                                    <g:renderShortFormattedDate date="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"/>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="row">
                                            <div class="col-xs-12">
                                                <div>
                                                    <label><g:message code="app.label.end"/></label>
                                                </div>

                                                <div>
                                                    <g:renderShortFormattedDate date="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"/>
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                </g:if>
                            </g:if>

                            <g:else>
                                <g:if test="${templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum}">
                                    <div>
                                        <g:message
                                                code="${(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum?.i18nKey)}"/>
                                        <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum))}">
                                            <input type="hidden" id="dateRangeValueRelative"
                                                   value="${(templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum)}"/>

                                            <div id="relativeDateRangeValueX">where, X = ${(templateQuery?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue)}</div>
                                        </g:if>
                                    </div>

                                    <g:if test="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUSTOM}">

                                        <div class="row">
                                            <div class="col-xs-12">
                                                <div>
                                                    <label><g:message code="app.label.start"/></label>
                                                </div>

                                                <div><g:renderShortFormattedDate date="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"/>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="row">
                                            <div class="col-xs-12">
                                                <div>
                                                    <label><g:message code="app.label.end"/></label>
                                                </div>

                                                <div>
                                                    <g:renderShortFormattedDate date="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"/>
                                                </div>
                                            </div>
                                        </div>

                                    </g:if>

                                </g:if>
                            </g:else>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.queryLevel"/></label>

                                    <div id="queryLevel">
                                        <div><g:message code="${templateQuery.queryLevel.i18nKey}"/></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.parameters"/></label>
                            <g:if test="${isExecuted}">
                                <g:if test="${templateQuery.executedTemplateValueLists || templateQuery.showReassessDateDiv()}">
                                    <div class="italic">
                                        <g:message code="app.label.template"/>
                                    </div>
                                    <g:each in="${templateQuery.executedTemplateValueLists}">
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
                                    <g:if test="${templateQuery.showReassessDateDiv()}">
                                        <div class="left-indent">
                                            <g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/> :
                                            ${renderShortFormattedDate(date: templateQuery.templtReassessDate)}
                                        </div>
                                    </g:if>
                                </g:if>

                                <g:if test="${templateQuery.executedQueryValueLists || templateQuery.showQueryReassessDateDiv()}">
                                    <div class="italic">
                                        <g:message code="app.label.query"/>
                                    </div>
                                    <g:each in="${templateQuery.executedQueryValueLists}">
                                        <div class="bold">
                                            <g:if test="${templateQuery.executedQuery.class != ExecutedCustomSQLQuery}">
                                                <span class="showQueryStructure showPopover"
                                                      data-content="${queryService.generateReadableQueryFromExTemplateQuery(templateQuery, 0)}">${it.query.name}</span>
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
                                    <g:if test="${templateQuery.showQueryReassessDateDiv()}">
                                        <div class="left-indent">
                                            <g:message code="app.label.reassessListedness"/> <g:message
                                                    code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/> :
                                            ${renderShortFormattedDate(date: templateQuery.reassessListednessDate)}
                                        </div>
                                    </g:if>
                                </g:if>
                                <g:if test="${!templateQuery.executedTemplateValueLists && !templateQuery.executedQueryValueLists && !templateQuery.showReassessDateDiv() && !templateQuery.showQueryReassessDateDiv()}">
                                    <div><g:message code="app.label.none"/></div>
                                </g:if>
                            </g:if>
                            <g:else>
                                <g:if test="${templateQuery.templateValueLists || templateQuery.showReassessDateDiv()}">
                                    <div class="italic">
                                        <g:message code="app.label.template"/>
                                    </div>
                                    <g:each in="${templateQuery.templateValueLists}">
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
                                    <g:if test="${templateQuery.showReassessDateDiv()}">
                                        <div class="left-indent">
                                            <g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/> :
                                            ${renderShortFormattedDate(date: templateQuery.templtReassessDate)}
                                        </div>
                                    </g:if>
                                </g:if>

                                <g:if test="${templateQuery.queryValueLists || templateQuery.showQueryReassessDateDiv()}">
                                    <div class="italic">
                                        <g:message code="app.label.query"/>
                                    </div>
                                    <g:each in="${templateQuery.queryValueLists}">
                                        <div class="bold">
                                            ${it.query.name}
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
                                    <g:if test="${templateQuery.showQueryReassessDateDiv()}">
                                        <div class="left-indent">
                                            <g:message code="app.label.reassessListedness"/> <g:message
                                                    code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/> :
                                            ${renderShortFormattedDate(date: templateQuery.reassessListednessDate)}
                                        </div>
                                    </g:if>
                                </g:if>

                                <g:if test="${!templateQuery.templateValueLists && !templateQuery.queryValueLists && !templateQuery.showReassessDateDiv() && !templateQuery.showQueryReassessDateDiv()}">
                                    <div><g:message code="app.label.none"/></div>
                                </g:if>
                            </g:else>
                        </div>
                    </div>
                </div>
            </div>
            <g:if test="${configurationInstance.pvqType}">

                <div class="row">
                <div class="col-xs-2">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="rod.fieldType.Issue_Type"/></label>
                            <div class="word-wrapper">${rcaMap["LATE_MAP"][templateQuery.issueType]?:""}</div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.lateMapping.root.cause"/></label>
                            <div class="word-wrapper"> ${rcaMap["ROOT_CAUSE_MAP"][templateQuery.rootCause]?:""}</div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.rootCause.responsible.party"/></label>
                            <div class="word-wrapper">${rcaMap["RESPONSIBLE_PARTY_MAP"][templateQuery.responsibleParty]?:""}</div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="actionItem.assigned.to.label"/></label>
                            <div class="word-wrapper">${templateQuery.assignedToUser?.fullNameAndUserName?:(templateQuery.assignedToGroup?.name?:"")}</div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="actionItem.assigned.to.label"/></label>
                            <div class="word-wrapper">${templateQuery.priority?:""}</div>
                        </div>
                    </div>
                </div>
                </div>
                <div class="row">
                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.pvc.investigation"/></label>
                                <div class="word-wrapper">${templateQuery.investigation?:templateQuery.investigationSql?:""}</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.pvc.summary"/></label>
                                <div class="word-wrapper">${templateQuery.summary?:templateQuery.summarySql?:""}</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.pvc.actions"/></label>
                                <div class="word-wrapper">${templateQuery.actions?:templateQuery.actions?:""}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
            <hr>
        </g:each>


        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.delivery.option.scheduler"/></label>
            </div>
        </div>

        <div class="row" id="schedulerDiv">
            <div class="col-xs-3">
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.sharedWith"/></label>
                        <g:if test="${deliveryOption.sharedWith || deliveryOption.sharedWithGroup}">
                            <g:if test="${deliveryOption.sharedWith}">
                                <g:each in="${deliveryOption.sharedWith}">
                                    <div>${it.reportRequestorValue}</div>
                                </g:each>
                            </g:if>
                            <g:if test="${deliveryOption.sharedWithGroup}">
                                <g:each in="${deliveryOption.sharedWithGroup}">
                                    <div>${it.name}</div>
                                </g:each>
                            </g:if>
                        </g:if>
                        <g:else>
                            <div><g:message code="app.label.none"/></div>
                        </g:else>
                    </div>
                </div>
                <g:render template="/configuration/includes/showEmailDetails"
                          model="[instance: configurationInstance, deliveryOption: deliveryOption]"/>
            </div>

            <div class="col-xs-6 fuelux" id="disabledScheduler">
                <g:hiddenField name="isEnabled" id="isEnabled" value="${configurationInstance?.isEnabled}"/>
                <g:if test="${configurationInstance?.scheduleDateJSON}">
                    <g:render template="schedulerTemplate" model="[mode:'show', adhoc:(configurationInstance instanceof com.rxlogix.config.Configuration)]"/>
                    <g:hiddenField name="scheduleDateJSON" value="${configurationInstance?.scheduleDateJSON ?: null}"/>
                </g:if>
                <g:else>
                    <label>
                        <g:message code="app.reportNotScheduled.message"/>
                    </label>
                </g:else>
                <g:if test="${(configurationInstance instanceof com.rxlogix.config.Configuration) || isExecuted}" >
                    <label class="m-t-5"><g:message code="app.label.removeOldVersion"/></label>
                    <div>
                        <g:formatBoolean boolean="${configurationInstance?.removeOldVersion}"
                                          true="${message(code: "default.button.yes.label")}"
                                          false="${message(code: "default.button.no.label")}"/>
                    </div>
                </g:if>
            </div>

            <div class="col-xs-3">
                <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate || isExecuted}">

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.scheduledBy"/></label>

                            <div>${configurationInstance.owner.fullName}</div>
                        </div>
                    </div>

                    <g:if test="${!isExecuted}">

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="next.run.date"/></label>
                                <g:if test="${configurationInstance.isEnabled && configurationInstance.nextRunDate}">
                                    <div><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                   model="[date: configurationInstance.nextRunDate]"/></div>
                                </g:if>
                                <g:else>
                                    <div>None</div>
                                </g:else>
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${isExecuted}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="scheduled.run.date"/></label>

                                <div><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                               model="[date: configurationInstance.nextRunDate]"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="last.run.date"/></label>

                                <div>
                                    <g:render template="/includes/widgets/dateDisplayWithTimezone"
                                              model="[date: configurationInstance.lastRunDate]"/>
                                </div>

                            </div>
                        </div>
                    </g:if>
                </g:if>
                <g:if test="${(configurationInstance.dmsConfiguration && !configurationInstance.dmsConfiguration.isDeleted)}">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.dms.upload.label"/></label>

                            <div>
                                <g:message
                                        code="app.label.dms.folderPath"/>: <g:message
                                        code="app.label.dms.rootFolder"/>${configurationInstance.dmsConfiguration.folder?('/'+configurationInstance.dmsConfiguration.folder):''}<br/>
                                <g:message
                                        code="app.label.dms.name"/>: ${configurationInstance.dmsConfiguration.name ?: configurationInstance.reportName}<br/>
                                <g:message
                                        code="app.label.dms.format"/>: <g:message
                                        code="${configurationInstance.dmsConfiguration.format.i18nKey}"/>

                            </div>
                        </div>
                    </div>

                </g:if>
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.removeOldVersion"/></label>

                        <div><g:formatBoolean boolean="${configurationInstance?.removeOldVersion}"
                                              true="${message(code: "default.button.yes.label")}"
                                              false="${message(code: "default.button.no.label")}"/>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <g:if test="${!configurationInstance.pvqType}">
        <g:if test="${!isExecuted}">
            <div class="row rxDetailsBorder" style="margin-top: 30px">
                <div class="col-xs-12">
                    <label><g:message code="app.label.deliveryOptions.task.header"/></label>
                </div>
            </div>
            <div class="row">
                <g:render template="/taskTemplate/includes/reportTaskTable" model="[mode:'show']"/>
                <g:hiddenField id="tasks" name="tasks" value="${configurationInstance?.getReportTasksAsJson()}"/>
            </div>
        </g:if>
        </g:if>
        <g:if test="${isExecuted}">
            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:if test="${reportConfigurationId}">
                            <g:link controller="configuration" action="view"
                                    id="${reportConfigurationId}"><g:message
                                    code="configuration.see.current.report"/></g:link>
                        </g:if>
                    </div>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <button class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["configuration", "edit", {"id":  ${params.id}}]}' id="editBtn">
                            ${message(code: "default.button.edit.label")}
                        </button>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["configuration", "copy", {"id":  ${params.id}}]}' id="copyBtn">
                            ${message(code: "default.button.copy.label")}
                        </button>
                        <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="configuration"
                                data-instanceid="${params.id}" data-instancename="${configurationInstance.reportName}"
                                class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
                    </div>
                </div> 
            </div>

        </g:else>

    </div>
    <g:if test="${viewSql}">
        <div>
            <g:each in="${viewSql}">
                <h3><g:message code="app.templatequery.id"/>: ${it.templateQueryId}</h3>
                <pre>
                    ${it.versionSql}<br>
                    ${it.querySql}<br>
                    ${it.gttSql}<br>
                    ${it.reportSql}<br>
                    ${it.headerSql}<br>
                </pre>
            </g:each>
        </div>
    </g:if>
    <g:if test="${configurationJson}">
        <div>
            <g:textArea name="configurationExport" value="${configurationJson}"
                        style="width: 100%; height: 150px; margin-top:20px"/>
        </div>
    </g:if>

    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${configurationInstance}" var="theInstance"/>
</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
        </div>
    </div>
</div>
</body>
</html>