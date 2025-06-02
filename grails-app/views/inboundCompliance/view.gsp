<%@ page import="com.rxlogix.config.ExecutedCustomSQLQuery; com.rxlogix.Constants; grails.util.Holders; com.rxlogix.config.Tenant; com.rxlogix.config.Query; com.rxlogix.config.ExecutedTemplateQuery; com.rxlogix.config.TemplateQuery; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.util.RelativeDateConverter; com.rxlogix.config.ReportConfiguration; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeValueEnum" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.pvcentral.inbound.viewCompliance.title"/></title>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:stylesheet src="parameters.css"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <g:set var="queryService" bean="queryService"/>
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
                            <label><g:message code="app.label.reports"/></label>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <div class="row">
                                <div class="col-xs-3">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.sender.name"/></label>

                                            <div class="word-wrapper">${configurationInstance.senderName}</div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.description"/></label>

                                            <g:if test="${configurationInstance.description}">
                                                <div class="word-wrapper">${configurationInstance.description}</div>
                                            </g:if>
                                            <g:else>
                                                <div>
                                                    <g:message code="app.label.none"/>
                                                </div>
                                            </g:else>
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

                                </div>

                                <div class="col-xs-3">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="userGroup.source.profiles.label"/></label>

                                            <div><g:message code="${configurationInstance.sourceProfile.sourceName}"/></div>
                                        </div>
                                    </div>
                                    <g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label><g:message code="app.label.tenant"/></label>

                                                <div>
                                                    ${Tenant.read(configurationInstance.tenantId)?.name}
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.DateRangeType"/></label>

                                            <div><g:message code="${configurationInstance.dateRangeType?.i18nKey}"/></div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.DateRange"/></label>
                                            <g:if test="${isExecuted}">
                                                <g:if test="${configurationInstance.executedGlobalDateRangeInbound?.dateRangeEnum}">
                                                    <div>
                                                        <g:message
                                                                code="${(configurationInstance.executedGlobalDateRangeInbound?.dateRangeEnum?.i18nKey)}"/>
                                                        <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.executedGlobalDateRangeInbound?.dateRangeEnum))}">
                                                            <input type="hidden" id="dateRangeValueRelative"
                                                                   value="${(configurationInstance.executedGlobalDateRangeInbound?.dateRangeEnum)}"/>

                                                            <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.executedGlobalDateRangeInbound?.relativeDateRangeValue)}</div>
                                                        </g:if>
                                                    </div>
                                                    <g:if test="${configurationInstance.executedGlobalDateRangeInbound?.dateRangeEnum == com.rxlogix.enums.DateRangeEnum.CUSTOM}">
                                                        <g:if test="${configurationInstance.executedGlobalDateRangeInbound?.dateRangeStartAbsolute}">
                                                            <div>
                                                                <label><g:message code="app.label.start"/></label>

                                                                <div><g:renderShortFormattedDate date="${configurationInstance.executedGlobalDateRangeInbound?.dateRangeStartAbsolute}"
                                                                /></div>
                                                                <label><g:message code="app.label.end"/></label>

                                                                <div><g:renderShortFormattedDate date="${configurationInstance.executedGlobalDateRangeInbound?.dateRangeEndAbsolute}"
                                                                /></div>
                                                            </div>
                                                        </g:if>
                                                    </g:if>
                                                </g:if>
                                            </g:if>
                                            <g:else>
                                                <g:if test="${configurationInstance?.globalDateRangeInbound?.dateRangeEnum}">
                                                    <div>
                                                        <g:message
                                                                code="${(configurationInstance.globalDateRangeInbound?.dateRangeEnum?.i18nKey)}"/>
                                                        <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(configurationInstance.globalDateRangeInbound?.dateRangeEnum))}">
                                                            <input type="hidden" id="dateRangeValueRelative"
                                                                   value="${(configurationInstance.globalDateRangeInbound?.dateRangeEnum)}"/>

                                                            <div id="relativeDateRangeValueX">where, X = ${(configurationInstance.globalDateRangeInbound?.relativeDateRangeValue)}</div>
                                                        </g:if>
                                                    </div>

                                                    <g:if test="${configurationInstance.globalDateRangeInbound?.dateRangeStartAbsolute}">
                                                        <div>
                                                            <label><g:message code="app.label.start"/></label>

                                                            <div><g:renderShortFormattedDate date="${configurationInstance.globalDateRangeInbound?.dateRangeStartAbsolute}"
                                                            /></div>
                                                            <label><g:message code="app.label.end"/></label>

                                                            <div><g:renderShortFormattedDate date="${configurationInstance.globalDateRangeInbound?.dateRangeEndAbsolute}"
                                                            /></div>
                                                        </div>
                                                    </g:if>
                                                </g:if>
                                            </g:else>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-xs-3">

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
                                            <label><g:message code="app.label.disabled"/></label>

                                            <div><g:formatBoolean boolean="${configurationInstance?.isDisabled}"
                                                                  true="${message(code: "default.button.yes.label")}"
                                                                  false="${message(code: "default.button.no.label")}"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                    <div class="row rxDetailsBorder">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.querySections"/></label>
                        </div>
                    </div>
                    <g:each var="queryCompliance" in="${queriesCompliance}">
                        <div class="row">

                            <div class="col-xs-3">
                                <div class="row">
                                    <label><g:message code="app.label.criteria.name"/></label>

                                    <div class="word-wrapper">${queryCompliance.criteriaName}</div>
                                </div>
                            </div>

                            <div class="col-xs-3">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.queryName"/></label>
                                        <g:if test="${isExecuted}">
                                            <g:if test="${queryCompliance.executedQuery}">
                                                <div>
                                                    <g:link controller="query" action="viewExecutedQuery"
                                                            id="${queryCompliance.executedQuery.id}"
                                                            params="[isExecuted: 'true']">${queryCompliance.executedQuery.name}</g:link>
                                                </div>
                                            </g:if>
                                            <g:else>
                                                <div>
                                                    <g:message code="app.label.none"/>
                                                </div>
                                            </g:else>
                                        </g:if>
                                        <g:else>
                                            <g:if test="${queryCompliance.query}">
                                                <div>
                                                    <g:link controller="query" action="view" id="${queryCompliance.query.id}"
                                                            params="[isExecuted: 'false']">${queryCompliance.query.name}</g:link>
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
                                        <label><g:message code="app.label.parameters"/></label>
                                        <g:if test="${isExecuted}">

                                            <g:if test="${queryCompliance.executedQueryValueLists}">
                                                <div class="italic">
                                                    <g:message code="app.label.query"/>
                                                </div>
                                                <g:each in="${queryCompliance.executedQueryValueLists}">
                                                    <div class="bold">
                                                        <g:if test="${queryCompliance.executedQuery.class != ExecutedCustomSQLQuery}">
                                                            <span class="showQueryStructure showPopover"
                                                                  data-content="${queryService.generateReadableQuery(queryCompliance.executedQueryValueLists, queryCompliance.executedQuery.JSONQuery, queryCompliance, 0)}">${it.query.name}</span>
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
                                            <g:if test="${!queryCompliance.executedQueryValueLists}">
                                                <div><g:message code="app.label.none"/></div>
                                            </g:if>
                                        </g:if>
                                        <g:else>

                                            <g:if test="${queryCompliance.queryValueLists}">
                                                <div class="italic">
                                                    <g:message code="app.label.query"/>
                                                </div>
                                                <g:each in="${queryCompliance.queryValueLists}">
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
                                            </g:if>

                                            <g:if test="${!queryCompliance.queryValueLists}">
                                                <div><g:message code="app.label.none"/></div>
                                            </g:if>
                                        </g:else>
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-3">
                                <div class="row">
                                    <label><g:message code="app.label.allow.timeframe"/></label>

                                    <div class="word-wrapper">${queryCompliance.allowedTimeframe}</div>
                                </div>
                            </div>
                        </div>
                    </g:each>

                    <g:if test="${isExecuted}">
                        <div class="row">
                            <div class="col-xs-12">
                                <div class="pull-right">
                                    <g:if test="${reportConfigurationId}">
                                        <g:link controller="inboundCompliance" action="view"
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
                                    <button class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["inboundCompliance", "edit", {"id":  ${params.id}}]}' id="editBtn">
                                        ${message(code: "default.button.edit.label")}
                                    </button>
                                    <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="configuration"
                                            data-instanceid="${params.id}" data-instancename="${configurationInstance.senderName}"
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