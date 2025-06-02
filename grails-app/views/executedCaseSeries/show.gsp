<%@ page import="com.rxlogix.config.ExecutedCustomSQLQuery; com.rxlogix.Constants; grails.util.Holders; com.rxlogix.config.Tenant; com.rxlogix.util.ViewHelper;com.rxlogix.enums.DictionaryTypeEnum;com.rxlogix.enums.DateRangeEnum;com.rxlogix.enums.EvaluateCaseDateEnum;com.rxlogix.util.DateUtil;" %>
<head>
    <g:set var="entityName" value="Case Series"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
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
    <asset:stylesheet src="parameters.css"/>
</head>

<body>
<div class="col-md-12">
    <g:set var="queryService" bean="queryService"/>
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

                                    <div>${seriesInstance.seriesName}</div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.description"/></label>

                                    <div>${seriesInstance.description}</div>
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
                                    <g:if test="${seriesInstance.productSelection}">

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
                                    <div>
                                        <g:formatBoolean boolean="${seriesInstance?.includeWHODrugs}"
                                                         true="${message(code: "default.button.yes.label")}"
                                                         false="${message(code: "default.button.no.label")}"/>
                                    </div>
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
                                    <g:if test="${seriesInstance.evaluateDateAs}">
                                        <div id="evaluateCaseDate">
                                            <g:message
                                                    code="${(EvaluateCaseDateEnum.(seriesInstance.evaluateDateAs).i18nKey)}"/>
                                            <g:if test="${seriesInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF}">
                                                <div>
                                                    <g:renderShortFormattedDate date="${seriesInstance.asOfVersionDate}"/>
                                                </div>
                                            </g:if>
                                        </div>
                                    </g:if>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.DateRange"/></label>
                                    <g:if test="${seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeEnum}">
                                        <div>
                                            <g:message
                                                    code="${(seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeEnum?.i18nKey)}"/>
                                            <input type="hidden" id="dateRangeValueRelative"
                                                   value="${(seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeEnum)}"/>
                                            <g:if test="${(DateRangeEnum.getRelativeDateOperatorsWithX().contains(seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeEnum))}">
                                                <div id="relativeDateRangeValueX">where, X = ${(seriesInstance?.executedCaseSeriesDateRangeInformation?.relativeDateRangeValue)}</div>
                                            </g:if>
                                        </div>

                                        <g:if test="${seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeStartAbsolute}">
                                            <div>
                                                <label><g:message code="app.label.start"/></label>

                                                <div><g:renderShortFormattedDate
                                                        date="${seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeStartAbsolute}"/></div>
                                                <label><g:message code="app.label.end"/></label>

                                                <div><g:renderShortFormattedDate
                                                        date="${seriesInstance?.executedCaseSeriesDateRangeInformation?.dateRangeEndAbsolute}"/></div>
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
                                        <g:if test="${seriesInstance?.executedGlobalQuery}">
                                            <g:link controller="query" action="viewExecutedQuery"
                                                    id="${seriesInstance?.executedGlobalQueryId}"
                                                    params="[viewOnly: true]">${seriesInstance?.executedGlobalQuery?.name}</g:link>
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

                            <g:if test="${seriesInstance.executedGlobalQuery}">
                                <g:if test="${seriesInstance.executedGlobalQueryValueLists}">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label><g:message code="app.label.parameters"/></label>

                                            <div>-- <g:message code="app.label.query"/> --</div>
                                            <g:each in="${seriesInstance.executedGlobalQueryValueLists}">
                                                <div class="left-indent">
                                                    <div class="bold">
                                                        <g:if test="${seriesInstance.executedGlobalQuery.class != ExecutedCustomSQLQuery}">
                                                            <span class="showQueryStructure showPopover"
                                                                  data-content="${queryService.generateReadableQuery(seriesInstance.executedGlobalQueryValueLists, seriesInstance.executedGlobalQuery.JSONQuery, seriesInstance, 0)}">${it.query.name}</span>
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
                            <g:if test="${seriesInstance?.executedDeliveryOption?.sharedWith || seriesInstance?.executedDeliveryOption?.sharedWithGroup}">
                                <g:if test="${seriesInstance?.executedDeliveryOption?.sharedWith}">
                                    <g:each in="${seriesInstance?.executedDeliveryOption?.sharedWith}">
                                        <div>${it.reportRequestorValue}</div>
                                    </g:each>
                                </g:if>
                                <g:if test="${seriesInstance?.executedDeliveryOption?.sharedWithGroup}">
                                    <g:each in="${seriesInstance?.executedDeliveryOption?.sharedWithGroup}">
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
                              model="[instance: seriesInstance, deliveryOption: seriesInstance?.executedDeliveryOption]"/>
                </div>

                <div class="col-xs-6 fuelux" id="disabledScheduler">

                </div>
            </div>

            <div class="row">
                <div class="col-xs-4">
                    <label><g:message code="app.label.generated.spotfire.file"/></label>
                    <g:if test="${seriesInstance?.associatedSpotfireFile}">
                        <div>${seriesInstance?.associatedSpotfireFile}</div>
                    </g:if>
                    <g:else>
                        <div><g:message code="app.label.none"/></div>
                    </g:else>
                </div>
            </div>


            <div class="row">
                <div class="col-xs-12">
                    <g:if test="${error}">
                        <label><g:message code="error.date.caseSeries.label"/></label>

                        <div>${seriesInstance?.errorDateCreated}</div>

                        <div>&nbsp;</div>
                        <label><g:message code="app.label.stackTrace.caseSeries"/></label>
                        <g:textArea name="stackTrace" value="${seriesInstance?.stackTrace}" class="error" readonly="true"/>
                    </g:if>
                    <div class="pull-right">
                        <g:link controller="caseList" action="index" params="[cid: seriesInstance.id]"
                                class="btn btn-primary">${message(code: 'app.view.casesSeries')}</g:link>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <g:if test="${caseSeriesInstanceId}">
                            <g:link controller="caseSeries" action="show"
                                    id="${caseSeriesInstanceId}"><g:message
                                    code="configuration.see.current.caseseries"/></g:link>
                        </g:if>
                    </div>
                </div>
            </div>

        </div>
        <g:if test="${viewSql}">
            <div class="row">
                <div class="col-md-12">
                    <h3><g:message code="app.caseSeries.id"/>: ${viewSql.id}</h3>
                    <table class="table table-striped table-bordered viewSQLTable">
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
                        <g:each in="${viewSql.data}" var="viewSqlDTO">
                            <tr>
                                <td>${viewSqlDTO.rowId}<br></td>
                                <td>${viewSqlDTO.scriptName}<br></td>
                                <td>${viewSqlDTO.executingSql}<br></td>
                                <td>${viewSqlDTO.executionTime}<br></td>
                                <td>${viewSqlDTO.rowsUpsert}<br></td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </g:if>
        <g:if test="${error}">
            <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${seriesInstance}" var="theInstance"/>
        </g:if>
    </rx:container>
</div>


</body>
</html>