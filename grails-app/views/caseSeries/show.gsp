<%@ page import="com.rxlogix.Constants; grails.util.Holders; com.rxlogix.config.Tenant; grails.converters.JSON; com.rxlogix.util.ViewHelper;com.rxlogix.enums.DictionaryTypeEnum;com.rxlogix.enums.DateRangeEnum;com.rxlogix.enums.EvaluateCaseDateEnum;com.rxlogix.util.DateUtil;" %>
<html>
<head>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/viewScheduler.js"/>
    <asset:stylesheet src="parameters.css"/>
    <g:set var="entityName" value="Case Series"/>
    <title><g:message code="app.caseSeries.view.title"/></title>
    <meta name="layout" content="main"/>

    <g:javascript>
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var queryEditUrl = "${createLink(controller: 'query', action: 'edit')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var getSmqDropdownListUrl = "${createLink(controller: 'eventDictionary', action: 'getSmqDropdownList')}";
    </g:javascript>

    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>

</head>

<body>
<div class="col-md-12">
    <rx:container title="${message(code: "app.label.view.case.series")}">

        <g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>

        <div class="container-fluid">
            <div class="row rxDetailsBorder">
                <div class="col-xs-12">
                    <label><g:message code="app.label.selectionCriteria"/></label>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="row">
                        <div class="col-xs-3">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="caseSeries.name.label"/></label>

                                    <div class="word-wrapper">${seriesInstance.seriesName}</div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.description"/></label>

                                    <div class="word-wrapper">${seriesInstance.description}</div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.qualityChecked"/></label>
                                    <div>
                                        <g:formatBoolean boolean="${seriesInstance.qualityChecked}"
                                                         true="${message(code: "default.button.yes.label")}"
                                                         false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.tag"/></label>
                                    <g:if test="${seriesInstance.tags?.name}">
                                        <g:each in="${seriesInstance.tags.name}">
                                            <div>${it}</div>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.SuspectProduct"/></label>

                                    <div><g:formatBoolean boolean="${seriesInstance?.suspectProduct}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.productDictionary.label"/></label>
                                    <g:if test="${seriesInstance.productSelection || seriesInstance.validProductGroupSelection}">

                                        <div id="showProductSelection"></div>
                                        ${ViewHelper.getDictionaryValues(seriesInstance, DictionaryTypeEnum.PRODUCT)}
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label>
                                        <g:message code="app.label.productDictionary.include.who.drugs"/>
                                    </label>

                                    <div><g:formatBoolean boolean="${seriesInstance?.includeWHODrugs}"
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
                                        <g:formatBoolean boolean="${seriesInstance?.isMultiIngredient}"
                                                         true="${message(code: "default.button.yes.label")}"
                                                         false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.studyDictionary.label"/></label>
                                    <g:if test="${seriesInstance.studySelection}">

                                        <div id="showStudySelection"></div>
                                        ${ViewHelper.getDictionaryValues(seriesInstance, DictionaryTypeEnum.STUDY)}
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.eventDictId.label"/></label>
                                    <g:if test="${seriesInstance.eventSelection || seriesInstance.validEventGroupSelection}">

                                        <div id="showEventSelection"></div>
                                        ${ViewHelper.getDictionaryValues(seriesInstance, DictionaryTypeEnum.EVENT)}
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.spotfire.caseSeries.generate.spotfire"/></label>
                                    <g:if test="${seriesInstance.generateSpotfire}">

                                        <div id="showGenerateSpotfire"></div>
                                        ${JSON.parse(seriesInstance.generateSpotfire).fullFileName}
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>

                        </div>

                        <div class="col-xs-3">

                            <g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.tenant"/></label>

                                        <div>
                                            ${Tenant.read(seriesInstance.tenantId)?.name}
                                        </div>
                                    </div>
                                </div>
                            </g:if>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.DateRangeType"/></label>
                                    <g:if test="${seriesInstance.dateRangeType}">
                                        <div><g:message code="${seriesInstance.dateRangeType.i18nKey}"/></div>
                                    </g:if>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="evaluate.on.label"/></label>

                                    <div id="evaluateCaseDate">
                                        <g:message
                                                code="${(EvaluateCaseDateEnum.(seriesInstance.evaluateDateAs).i18nKey)}"/>
                                        <g:if test="${seriesInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF}">
                                            <div>
                                                <g:renderShortFormattedDate date="${seriesInstance.asOfVersionDate}"/>
                                            </div>
                                        </g:if>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.DateRange"/></label>
                                    <g:if test="${seriesInstance?.caseSeriesDateRangeInformation?.dateRangeEnum}">
                                        <div>
                                            <g:message
                                                    code="${(seriesInstance?.caseSeriesDateRangeInformation?.dateRangeEnum?.i18nKey)}"/>
                                            <input type="hidden" id="dateRangeValueRelative"
                                                   value="${(seriesInstance?.caseSeriesDateRangeInformation?.dateRangeEnum)}"/>
                                            <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(seriesInstance?.caseSeriesDateRangeInformation?.dateRangeEnum))}">
                                                <div id="relativeDateRangeValueX">where, X = ${(seriesInstance?.caseSeriesDateRangeInformation?.relativeDateRangeValue)}</div>
                                            </g:if>
                                        </div>

                                        <g:if test="${seriesInstance?.caseSeriesDateRangeInformation?.dateRangeStartAbsolute}">
                                            <div>
                                                <label><g:message code="app.label.start"/></label>

                                                <div><g:renderShortFormattedDate date="${seriesInstance?.caseSeriesDateRangeInformation?.dateRangeStartAbsolute}"
                                                /></div>
                                                <label><g:message code="app.label.end"/></label>

                                                <div><g:renderShortFormattedDate date="${seriesInstance?.caseSeriesDateRangeInformation?.dateRangeEndAbsolute}"
                                                /></div>
                                            </div>
                                        </g:if>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.queryName"/></label>

                                    <div>
                                        <g:if test="${seriesInstance?.globalQuery}">
                                            <g:link controller="query" action="view"
                                                    id="${seriesInstance?.globalQueryId}"
                                                    params="[viewOnly: true]">${seriesInstance?.globalQuery?.name}</g:link>
                                        </g:if>
                                        <g:else>
                                            <g:message code="app.label.none"/>
                                        </g:else>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-xs-3">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.scheduledBy"/></label>

                                    <div>${seriesInstance.owner.fullName}</div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="reportCriteria.exclude.follow.up"/></label>

                                    <div>
                                        <g:formatBoolean boolean="${seriesInstance?.excludeFollowUp}"
                                                         true="${message(code: "default.button.yes.label")}"
                                                         false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="reportCriteria.include.locked.versions.only"/></label>

                                    <div><g:formatBoolean boolean="${seriesInstance?.includeLockedVersion}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="reportCriteria.include.all.study.drugs.cases"/></label>

                                    <div><g:formatBoolean boolean="${seriesInstance?.includeAllStudyDrugsCases}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="reportCriteria.exclude.non.valid.cases"/></label>

                                    <div><g:formatBoolean boolean="${seriesInstance?.excludeNonValidCases}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="reportCriteria.exclude.deleted.cases"/></label>

                                    <div><g:formatBoolean boolean="${seriesInstance?.excludeDeletedCases}"
                                                          true="${message(code: "default.button.yes.label")}"
                                                          false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>

                            <g:if test="${seriesInstance.globalQuery}">
                                <g:if test="${seriesInstance.globalQueryValueLists}">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.parameters"/></label>

                                            <div>-- <g:message code="app.label.query"/> --</div>
                                            <g:each in="${seriesInstance.globalQueryValueLists}">
                                                <div class="left-indent">
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
                                                </div>
                                            </g:each>
                                        </div>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none"/></div>
                                </g:else>
                            </g:if>

                        </div>

                    </div>
                </div>
            </div>

            <div class="row rxDetailsBorder">
                <div class="col-xs-12">
                    <label><g:message code="app.label.caseDeliveryOptions"/></label>
                </div>
            </div>

            <div class="row" id="schedulerDiv">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.sharedWith"/></label>
                            <g:if test="${seriesInstance?.deliveryOption?.sharedWith || seriesInstance?.deliveryOption?.sharedWithGroup}">
                                <g:if test="${seriesInstance?.deliveryOption?.sharedWith}">
                                    <g:each in="${seriesInstance?.deliveryOption?.sharedWith}">
                                        <div>${it.reportRequestorValue}</div>
                                    </g:each>
                                </g:if>
                                <g:if test="${seriesInstance?.deliveryOption?.sharedWithGroup}">
                                    <g:each in="${seriesInstance?.deliveryOption?.sharedWithGroup}">
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
                              model="[instance: seriesInstance, deliveryOption: seriesInstance?.deliveryOption]"/>
                </div>

                <div class="col-xs-6 fuelux" id="disabledScheduler">
                    <g:hiddenField name="isEnabled" id="isEnabled" value="${seriesInstance?.isEnabled}"/>
                    <g:if test="${seriesInstance?.scheduleDateJSON}">
                        <g:render template="/configuration/schedulerTemplate" model="[mode:'show']"/>
                        <g:hiddenField name="scheduleDateJSON" value="${seriesInstance?.scheduleDateJSON ?: null}"/>
                    </g:if>
                    <g:else>
                        <label>
                            <g:message code="app.reportNotScheduled.message"/>
                        </label>
                    </g:else>
                </div>
                <div class="col-xs-3">
                    <g:if test="${seriesInstance?.isEnabled && seriesInstance?.nextRunDate || isExecuted}">

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.scheduledBy"/></label>

                                <div>${seriesInstance.owner.fullName}</div>
                            </div>
                        </div>

                        <g:if test="${!isExecuted}">

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="next.run.date"/></label>
                                    <g:if test="${seriesInstance.isEnabled && seriesInstance.nextRunDate}">
                                        <div><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                       model="[date: seriesInstance.nextRunDate]"/></div>
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
                                                   model="[date: seriesInstance.nextRunDate]"/></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="last.run.date"/></label>

                                    <div>
                                        <g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                  model="[date: seriesInstance.lastRunDate]"/>
                                    </div>

                                </div>
                            </div>
                        </g:if>
                    </g:if>
                </div>

            </div>

            <div class="row">
                <div class="col-xs-12">
                    <g:if test="${error}">
                        <label><g:message code="error.date.caseSeries.label" /></label>
                        <div>${seriesInstance?.errorDateCreated}</div>
                        <div>&nbsp;</div>
                        <label><g:message code="app.label.stackTrace.caseSeries" /></label>
                        <g:textArea name="stackTrace" value="${seriesInstance?.stackTrace}" class="error" readonly="true"/>
                    </g:if>
                    <div class="pull-right">
                        <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["caseSeries", "edit", {"id": ${seriesInstance.id}}]}' id="editBtn">
                            ${message(code: "default.button.edit.label")}
                        </button>
                        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["caseSeries", "copy", {"id":  ${params.id}}]}' id="copyBtn">
                            ${message(code: "default.button.copy.label")}
                        </button>
                        <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="caseSeries"
                                data-instanceid="${params.id}" data-instancename="${seriesInstance.seriesName}"
                                class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
                    </div>
                </div>
            </div>

        </div>
        <g:if test="${viewGlobalSql}">
            <div>
                <h3><g:message code="app.globalQuery.label"/>:</h3>
                <pre>
                    ${viewGlobalSql.globalVersionSql}<br>
                    ${viewGlobalSql.globalQuerySql}<br>
                    ${viewGlobalSql.caseSeriesSql}<br>
                </pre>
            </div>
        </g:if>

        <g:if test="${error}">
            <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${seriesInstance}" var="theInstance"/>
        </g:if>
    </rx:container>
</div>

<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>
</html>