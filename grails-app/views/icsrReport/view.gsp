<%@ page import="com.rxlogix.util.RelativeDateConverter; com.rxlogix.config.ExecutedCustomSQLQuery; com.rxlogix.Constants; grails.util.Holders; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.config.ReportConfiguration; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeValueEnum" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ViewReport.title"/></title>

    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/viewScheduler.js"/>
    <asset:javascript src="app/reportTask.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:stylesheet src="parameters.css"/>
    <script>
        var aggregateReportViewTaskMode=true;
        var pageType = 'show';
    </script>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
<g:set var="userService" bean="userService"/>
            <g:set var="queryService" bean="queryService"/>
<g:set var="user" value="${userService?.getUser()}"/>

<g:set var="timeZone" value="${user?.preference?.timeZone}"/>

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

                        <div class="row">
                            <div class="col-xs-12">
                                <g:if test="${configurationInstance.productSelection || configurationInstance.validProductGroupSelection}">
                                    <label><g:message code="app.productDictionary.label"/></label>

                                    <div id="showProductSelection"></div>
                                    ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.PRODUCT)}
                                </g:if>
                            </div>
                        </div>

                        <g:if test="${configurationInstance.productSelection || configurationInstance.validProductGroupSelection}">
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

                                    <div>
                                        <g:formatBoolean boolean="${configurationInstance?.isMultiIngredient}"
                                                         true="${message(code: "default.button.yes.label")}"
                                                         false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>
                        </g:if>

                        <div class="row">
                            <div class="col-xs-12">
                                <g:if test="${configurationInstance.studySelection}">
                                    <label><g:message code="app.studyDictionary.label"/></label>

                                    <div id="showStudySelection"></div>
                                    ${ViewHelper.getDictionaryValues(configurationInstance, DictionaryTypeEnum.STUDY)}
                                </g:if>
                            </div>
                        </div>
                    </div>

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

                        <!--<div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.icsrReport.referenceProfile.label"/></label>
                                <g:if test="${isExecuted}">
                                    <g:if test="${configurationInstance.referenceProfileName}">
                                        <div>
                                            ${configurationInstance.referenceProfileName}
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:if test="${configurationInstance.referenceProfile}">
                                        <div>
                                            <g:link controller="icsrProfileConfiguration" action="view"
                                                    id="${configurationInstance.referenceProfile.id}">${configurationInstance.referenceProfile.reportName.encodeAsHTML()}</g:link>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </g:else>
                            </div>
                        </div>-->

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

                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="userGroup.source.profiles.label"/></label>

                                <div><g:message code="${configurationInstance.sourceProfile.sourceName.encodeAsHTML()}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.DateRangeType"/></label>
                                <div><g:message code="${configurationInstance.dateRangeType?.i18nKey}"/></div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.EvaluateCaseDateOn"/></label>

                                <div id="evaluateCaseDate">
                                    <g:message
                                            code="${(EvaluateCaseDateEnum.(configurationInstance.evaluateDateAs).i18nKey)}"/>
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

                                            <div>
                                                <g:each in="${it.parameterValues}">
                                                    <div>
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
                                            <div class="bold">
                                                ${it.query.name}
                                            </div>

                                            <div>
                                                <g:each in="${it.parameterValues}">
                                                    <div>
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

                    </div>

                    <div class="col-xs-3">

                        <div class="row"><label><g:message code="reportCriteria.include.previous.missing.cases"/></label>

                            <div>
                                <g:formatBoolean boolean="${configurationInstance?.includePreviousMissingCases}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>

                        <div class="row"><label><g:message code="reportCriteria.include.open.cases.in.draft"/></label>

                            <div>
                                <g:formatBoolean boolean="${configurationInstance?.includeOpenCasesInDraft}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>

                        <div class="row"><label><g:message code="reportCriteria.exclude.follow.up"/></label>

                            <div>
                                <g:formatBoolean boolean="${configurationInstance?.excludeFollowUp}"
                                                 true="${message(code: "default.button.yes.label")}"
                                                 false="${message(code: "default.button.no.label")}"/>
                            </div>
                        </div>

                        <div class="row">
                            <label><g:message code="reportCriteria.include.all.study.drugs.cases"/></label>

                            <div><g:formatBoolean boolean="${configurationInstance?.includeAllStudyDrugsCases}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/></div>
                        </div>

                        <div class="row">
                            <label><g:message code="reportCriteria.exclude.non.valid.cases"/></label>

                            <div><g:formatBoolean boolean="${configurationInstance?.excludeNonValidCases}"
                                                  true="${message(code: "default.button.yes.label")}"
                                                  false="${message(code: "default.button.no.label")}"/></div>
                        </div>
                    </div>

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
                        </div>
                    </div>
                    <g:if test="${templateQuery.draftOnly}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.draftOnly"/></label>
                                <div>
                                    <g:formatBoolean boolean="${templateQuery.draftOnly}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}"/>
                                </div>
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
                                    <g:if test="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEnum == com.rxlogix.enums.DateRangeEnum.CUSTOM}">
                                        <g:if test="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}">
                                            <div>
                                                <label><g:message code="app.label.start"/></label>

                                                <div><g:renderShortFormattedDate date="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"
                                                                   type="datetime" timeZone="${timeZone}"/></div>
                                                <label><g:message code="app.label.end"/></label>

                                                <div><g:renderShortFormattedDate date="${templateQuery?.executedDateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"
                                                                   type="datetime" timeZone="${timeZone}"/></div>
                                            </div>
                                        </g:if>
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

                                    <g:if test="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}">
                                        <div>
                                            <label><g:message code="app.label.start"/></label>

                                            <div><g:renderShortFormattedDate date="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"
                                                               /></div>
                                            <label><g:message code="app.label.end"/></label>

                                            <div><g:renderShortFormattedDate date="${templateQuery.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"
                                                               /></div>
                                        </div>
                                    </g:if>
                                </g:if>
                            </g:else>

                            <label><g:message code="app.label.queryLevel"/></label>

                            <div id="queryLevel">
                                <div><g:message code="${templateQuery.queryLevel.i18nKey}"/></div>
                            </div>

                            <label><g:message code="app.label.icsr.profile.conf.messageType"/></label>

                            <div id="queryLevel">
                                <g:if test="${templateQuery?.icsrMsgType}">
                                    <div>
                                        <g:renderIcsrMsgTypeName icsrMsgType="${templateQuery?.icsrMsgType}"></g:renderIcsrMsgTypeName>
                                    </div>
                                </g:if>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.parameters"/></label>
                            <g:if test="${isExecuted}">
                                <div>
                                    -- <g:message code="app.label.template"/> --
                                </div>
                                <g:if test="${templateQuery.executedTemplateValueLists}">
                                    <g:each in="${templateQuery.executedTemplateValueLists}">
                                        <div class="bold">
                                            ${it.template.name}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div>
                                                    <g:if test="${it.value}">
                                                        ${it.key} = ${it.value}
                                                    </g:if>
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>

                                <div>
                                    -- <g:message code="app.label.query"/> --
                                </div>
                                <g:if test="${templateQuery.executedQueryValueLists}">
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
                                                <div>
                                                    <g:if test="${it.hasProperty('reportField')}">
                                                        <g:if test="${it.value}">
                                                            <g:message code="app.reportField.${it.reportField.name}"/>
                                                            <g:message code="${it.operator.getI18nKey()}"/>
                                                            ${it.value?.tokenize(';')?.join(', ')}
                                                        </g:if>
                                                    </g:if>
                                                    <g:else>
                                                        <g:if test="${it.value}">
                                                            <g:if test="${it.value}">
                                                                ${it.key} = ${it.value}
                                                            </g:if>
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
                                <div>
                                    -- <g:message code="app.label.template"/> --
                                </div>
                                <g:if test="${templateQuery.templateValueLists}">
                                    <g:each in="${templateQuery.templateValueLists}">
                                        <div class="bold">
                                            ${it.template.name}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div>
                                                    <g:if test="${it.value}">
                                                        ${it.key} = ${it.value}
                                                    </g:if>
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>

                                <div>
                                    -- <g:message code="app.label.query"/> --
                                </div>
                                <g:if test="${templateQuery.queryValueLists}">
                                    <g:each in="${templateQuery.queryValueLists}">
                                        <div class="bold">
                                            ${it.query.name}
                                        </div>

                                        <div>
                                            <g:each in="${it.parameterValues}">
                                                <div>
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
                </div>
            </div>
        </g:each>


        <div class="row rxDetailsBorder">
            <div class="col-xs-12">
                <label><g:message code="report.delivery.option.scheduler"/></label>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                        <label><g:message code="app.label.icsr.profile.conf.senderOrganization"/></label>

                        <div>
                            <g:if test="${isExecuted}">
                                <g:if test="${configurationInstance.senderOrganizationName}">
                                    ${configurationInstance.senderOrganizationName}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.none"/>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:if test="${configurationInstance.senderOrganization}">
                                    ${configurationInstance.senderOrganization?.unitName}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.none"/>
                                </g:else>
                            </g:else>
                        </div>
                        </div>
                    </div>
            </div>
            <div class="col-xs-3">
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.icsr.profile.conf.recipientOrganization"/></label>
                    <div>
                            <g:if test="${isExecuted}">
                                <g:if test="${configurationInstance.recipientOrganizationName}">
                                    ${configurationInstance.recipientOrganizationName}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.none"/>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:if test="${configurationInstance.recipientOrganization}">
                                    ${configurationInstance.recipientOrganization?.unitName}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.none"/>
                                </g:else>
                            </g:else>
                        </div>
                    </div>
                </div>
        </div>
        </div>


        <div class="row">
            <div class="col-xs-3">
                <g:if test="${deliveryOption?.sharedWith || deliveryOption?.sharedWithGroup}">
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
                </g:if>
                <g:render template="/configuration/includes/showEmailDetails"
                          model="[instance: configurationInstance, deliveryOption: deliveryOption]"/>
            </div>

            <div class="col-xs-6 fuelux" id="schedulerDiv">
                <g:hiddenField name="isEnabled" id="isEnabled" value="${configurationInstance?.isEnabled}"/>
                <g:if test="${configurationInstance?.scheduleDateJSON}">
                    <g:render template="/configuration/schedulerTemplate" model="[mode:'show']"/>
                    <g:hiddenField name="schedulerTime"
                                   value="${RelativeDateConverter.getCurrentTimeWRTTimeZone(user)}"/>
                    <g:hiddenField name="scheduleDateJSON"
                                   value="${configurationInstance?.scheduleDateJSON ?: null}"/>
                    <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                           value="${configurationInstance?.configSelectedTimeZone ?: timeZone}"/>
                    <input type="hidden" id="timezoneFromServer" name="timezone"
                           value="${DateUtil.getTimezone(user)}"/>
                </g:if>
                <g:else>
                    <label>
                        <g:message code="app.reportNotScheduled.message"/>
                    </label>
                </g:else>
            </div>
            <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate || isExecuted}">
                <div class="col-xs-3">
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
                </div>
            </g:if>
        </div>
        <g:if test="${isExecuted}">
            <div class="row">
                <div class="col-xs-12">
                    %{--<div class="pull-right">--}%
                    %{--<g:link controller="configuration" action="view"--}%
                    %{--id="${(com.rxlogix.config.ExecutedConfiguration)configurationInstance.executedTemplateQueries[0].reportResult[0]?.templateQuery.configuration.id}">See current configuration</g:link>--}%
                    %{--</div>--}%
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <button class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["icsrReport", "edit", {"id": ${params.id}}]}' id="editBtn">
                            ${message(code: "default.button.edit.label")}
                        </button>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["icsrReport", "copy", {"id": ${params.id}}]}' id="copyBtn">
                            ${message(code: "default.button.copy.label")}
                        </button>
                        <button url="#" data-toggle="modal" data-target="#deleteModal"
                                data-instancetype="periodicReportConfiguration"
                                data-instanceid="${params.id}" data-instancename="${configurationInstance.reportName}"
                                class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
                    </div>
                </div>
            </div>

        </g:else>
    </div>
    <g:if test="${viewGlobalSql}">
        <div>
            <h3><g:message code="app.globalQuery.label"/>:</h3>
            <pre>
                ${viewGlobalSql.globalVersionSql}<br>
                ${viewGlobalSql.globalQuerySql}<br>
            </pre>
        </div>
    </g:if>
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