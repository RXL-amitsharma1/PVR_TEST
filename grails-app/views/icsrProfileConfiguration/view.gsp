<%@ page import="com.rxlogix.config.ExecutedCustomSQLQuery; com.rxlogix.Constants; grails.util.Holders; com.rxlogix.enums.DistributionChannelEnum; com.rxlogix.util.RelativeDateConverter; com.rxlogix.config.ReportConfiguration; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeValueEnum" %>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="ICSR Profile Configuration"/>
    <g:set var="titleName" value="${message(code: "app.icsrProfileConf.name.label")}"/>
    <title><g:message code="default.show.title" args="[titleName]"/></title>

    <asset:javascript src="vendorUi/tinymce771/tinymce.min.js"/>
    <asset:javascript src="app/emailTemplateEditor.js"/>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/configuration/templateQueries.js"/>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="parameters.css"/>
</head>

<body>
<g:set var="queryService" bean="queryService"/>
<g:javascript>
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList', params: [showXMLSpecific: true])}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?isQueryTargetReports=true";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
</g:javascript>
<div class="col-md-12">
    <rx:container title="${message(code: "app.label.icsr.profile.conf.icsrProfileConfiguration")}">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${icsrProfileConfInstance}" var="theInstance"/>

        <div class="container-fluid">
        <div class="row">
            <div class="col-md-12">
                <div class="rxmain-container rxmain-container-top">
                            <div class="rxmain-container-row rxmain-container-header">
                                <label class="rxmain-container-header-label">
        <g:message code="app.label.icsr.profile.conf.basic.information"/>
        </label>
            </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.icsrConfigurationName"/></label>

                    <p>${icsrProfileConfInstance?.reportName}</p>
                </div>

                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.recipientOrganization"/></label>

                    <p>
                        <g:link controller="unitConfiguration" action="show"
                                id="${icsrProfileConfInstance?.recipientOrganization?.id}">${icsrProfileConfInstance?.recipientOrganization?.unitName}</g:link>
                    </p>
                </div>

                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.recipientType"/></label>

                    <p>
                        ${ViewHelper.getOrganizationTypeByPreference(icsrProfileConfInstance?.recipientOrganization?.organizationType)}
                    </p>
                </div>

                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.recipientCountry"/></label>

                    <p>
                        <g:if test="${icsrProfileConfInstance?.recipientOrganization?.organizationCountry}">
                            ${ViewHelper.getOrganizationCountryNameByPreference(icsrProfileConfInstance?.recipientOrganization?.organizationCountry)}
                        </g:if>
                    </p>
                </div>

                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.senderOrganization"/></label>

                    <p>
                        <g:link controller="unitConfiguration" action="show"
                                id="${icsrProfileConfInstance?.senderOrganization?.id}">${icsrProfileConfInstance?.senderOrganization?.unitName}</g:link>
                    </p>
                </div>

                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.senderType"/></label>

                    <p>
                        ${ViewHelper.getOrganizationTypeByPreference(icsrProfileConfInstance?.senderOrganization?.organizationType)}
                    </p>
                </div>

            </div>

            <div class="row">
%{--                <div class="col-md-2">
                    <label><g:message code="userGroup.source.profiles.label"/></label>

                    <g:if test="${icsrProfileConfInstance?.sourceProfile}">
                        <div><g:message code="${icsrProfileConfInstance?.sourceProfile?.sourceName}"/></div>
                    </g:if>
                    <g:else>
                        <div><g:message code="app.label.none" /></div>
                    </g:else>
                </div> --}%

                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="autoScheduling" disabled=""
                                    name="autoScheduling"
                                    value="${icsrProfileConfInstance?.autoScheduling}"
                                    checked="${icsrProfileConfInstance?.autoScheduling}"/>
                        <label for="autoScheduling">
                            <g:message code="app.label.icsr.profile.conf.autoScheduling"/>
                        </label>
                    </div>
                </div>

                <div class="col-md-2">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="autoGenerate" disabled=""
                                    name="autoGenerate"
                                    value="${icsrProfileConfInstance?.autoGenerate}"
                                    checked="${icsrProfileConfInstance?.autoGenerate}"/>
                        <label for="autoGenerate">
                            <g:message code="app.label.icsr.profile.conf.autoGenerate"/>
                        </label>
                    </div>
                </div>

                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="autoTransmit" disabled=""
                                    name="autoTransmit"
                                    value="${icsrProfileConfInstance?.autoTransmit}"
                                    checked="${icsrProfileConfInstance?.autoTransmit}"/>
                        <label for="autoTransmit">
                            <g:message code="app.label.icsr.profile.conf.autoTransmit"/>
                        </label>
                    </div>
                </div>

                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="autoSubmit" disabled=""
                                    name="autoSubmit"
                                    value="${icsrProfileConfInstance?.autoSubmit}"
                                    checked="${icsrProfileConfInstance?.autoSubmit}"/>
                        <label for="autoSubmit">
                            <g:message code="app.label.icsr.profile.conf.autoSubmit"/>
                        </label>
                    </div>
                </div>

                <div class="col-md-2">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="localCpRequired" disabled=""
                                    name="localCpRequired"
                                    value="${icsrProfileConfInstance?.localCpRequired}"
                                    checked="${icsrProfileConfInstance?.localCpRequired}"/>
                        <label for="localCpRequired">
                            <g:message code="app.label.icsr.profile.conf.localCpRequired"/>
                        </label>
                    </div>
                </div>

                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="manualScheduling" disabled=""
                                    name="manualScheduling"
                                    value="${icsrProfileConfInstance?.manualScheduling}"
                                    checked="${icsrProfileConfInstance?.manualScheduling}"/>
                        <label for="manualScheduling">
                            <g:message code="app.label.icsr.profile.conf.manualScheduling"/>
                        </label>
                    </div>
                </div>

                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="autoScheduleFUPReport" disabled=""
                                    name="autoScheduleFUPReport"
                                    value="${icsrProfileConfInstance?.autoScheduleFUPReport}"
                                    checked="${icsrProfileConfInstance?.autoScheduleFUPReport}"/>
                        <label for="autoScheduleFUPReport">
                            <g:message code="app.label.icsr.profile.conf.autoScheduleFUPReport"/>
                        </label>
                    </div>
                </div>

                %{--<div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="deviceReportable" disabled=""
                                    name="deviceReportable"
                                    value="${icsrProfileConfInstance?.deviceReportable}"
                                    checked="${icsrProfileConfInstance?.deviceReportable}"/>
                        <label for="deviceReportable">
                            <g:message code="app.label.icsr.profile.conf.deviceReportable"/>
                        </label>
                    </div>
                </div>--}%

                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="adjustDueDate" disabled=""
                                    name="adjustDueDate"
                                    value="${icsrProfileConfInstance?.adjustDueDate}"
                                    checked="${icsrProfileConfInstance?.adjustDueDate}"/>
                        <label for="adjustDueDate">
                            <g:message code="app.label.icsr.profile.adjustDueDate"/>
                        </label>
                    </div>
                </div>
                <div class="col-md-2">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="isJapanProfile" disabled=""
                                    name="isJapanProfile"
                                    value="${icsrProfileConfInstance?.isJapanProfile}"
                                    checked="${icsrProfileConfInstance?.isJapanProfile}"/>
                        <label for="isJapanProfile">
                            <g:message code="app.label.icsr.profile.conf.japan.profile"/>
                        </label>
                    </div>
                </div>
                <div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="awareDate" disabled=""
                                    name="awareDate"
                                    value="${icsrProfileConfInstance?.awareDate}"
                                    checked="${icsrProfileConfInstance?.awareDate}"/>
                        <label for="awareDate">
                            <g:message code="app.label.icsr.profile.conf.japanAwareDate"/>
                        </label>
                    </div>
                </div>
                %{--<div class="col-md-2">

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="multipleReport" disabled=""
                                    name="multipleReport"
                                    value="${icsrProfileConfInstance?.multipleReport}"
                                    checked="${icsrProfileConfInstance?.multipleReport}"/>
                        <label for="multipleReport">
                            <g:message code="app.label.icsr.profile.conf.multipleReporting"/>
                        </label>
                    </div>
                </div>--}%
                <div class="col-md-2">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="includeOpenCases" disabled=""
                                    name="includeOpenCases"
                                    value="${icsrProfileConfInstance?.includeOpenCases}"
                                    checked="${icsrProfileConfInstance?.includeOpenCases}"/>
                        <label for="includeOpenCases">
                            <g:message code="app.label.icsr.profile.conf.includeOpenCases"/>
                        </label>
                    </div>
                </div>
                <div class="col-md-2">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="isDisabled" disabled=""
                                    name="isDisabled"
                                    value="${icsrProfileConfInstance?.isDisabled}"
                                    checked="${icsrProfileConfInstance?.isDisabled ?: false}"/>
                        <label for="isDisabled">
                            <g:message code="app.label.icsr.profile.conf.disabled"/>
                        </label>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-2">
                    <label><g:message code="app.label.icsr.profile.conf.iscr.product.evaluation"/></label>
                    <g:if test="${icsrProfileConfInstance?.ruleEvaluation}">
                        <div>
                            <g:renderRuleEvaluation ruleEvaluation="${icsrProfileConfInstance?.ruleEvaluation}"></g:renderRuleEvaluation>
                        </div>
                    </g:if>
                    <g:else>
                        <div><g:message code="app.label.none"/></div>
                    </g:else>
                </div>
                <div class="hidden">
                    <g:if test="${icsrProfileConfInstance?.fieldProfile?.name}">
                        <div class="col-md-2">
                            <label><g:message code="userGroup.field.profile.label"/></label>

                            <div><g:message code="${icsrProfileConfInstance?.fieldProfile?.name}"/></div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="col-md-2">
                            <label><g:message code="userGroup.field.profile.label"/></label>

                            <div><g:message code="app.label.none"/></div>
                        </div>
                    </g:else>
                </div>
                <g:if test="${icsrProfileConfInstance?.autoSubmit}">
                    <div class="col-md-2">
                        <label><g:message code="app.label.icsr.profile.conf.iscr.submission.date"/></label>
                        <g:if test="${icsrProfileConfInstance?.submissionDateFrom}">
                            <div><g:message
                                    code="${(icsrProfileConfInstance?.submissionDateFrom)?.getI18nKey()}"/></div>
                        </g:if>
                        <g:else>
                            <div><g:message code="app.label.none"/></div>
                        </g:else>
                    </div>
                </g:if>
                <g:if test="${icsrProfileConfInstance?.adjustDueDate}">
                    <div class="col-md-2">
                        <label><g:message code="app.label.icsr.profile.calendar"/></label>
                        <g:if test="${icsrProfileConfInstance?.calendars}">
                            <div>
                                <g:renderCalendarName calendarIds="${icsrProfileConfInstance?.calendars}"></g:renderCalendarName>
                            </div>
                        </g:if>
                        <g:else>
                            <div><g:message code="app.label.none"/></div>
                        </g:else>
                    </div>
                </g:if>
                <g:if test="${icsrProfileConfInstance?.adjustDueDate}">
                    <div class="col-md-2">
                        <label><g:message code="app.label.due.Date.Options"/></label>
                        <g:if test="${icsrProfileConfInstance?.dueDateOptionsEnum}">
                            <div><g:message
                                    code="${(icsrProfileConfInstance?.dueDateOptionsEnum)?.getI18nKey()}"/></div>
                        </g:if>
                        <g:else>
                            <div><g:message code="app.label.none"/></div>
                        </g:else>
                    </div>
                </g:if>
                <g:if test="${icsrProfileConfInstance?.adjustDueDate}">
                    <div class="col-md-2">
                        <label><g:message code="app.label.due.Date.Adjustment"/></label>
                        <g:if test="${icsrProfileConfInstance?.dueDateAdjustmentEnum}">
                            <div><g:message
                                    code="${(icsrProfileConfInstance?.dueDateAdjustmentEnum)?.getI18nKey()}"/></div>
                        </g:if>
                        <g:else>
                            <div><g:message code="app.label.none"/></div>
                        </g:else>
                    </div>
                </g:if>
            </div>
        </div>

        <div class="row">


        </div>
        </div>
        <div class="rxmain-container rxmain-container-top">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    <g:message code="app.label.globalCriteria"/>
                </label>
            </div>

            <div class="row">
                <g:if test="${icsrProfileConfInstance.productSelection || icsrProfileConfInstance.productGroupSelection}">
                    <div class="col-md-2">
                        <label><g:message code="app.productDictionary.label"/></label>

                        <div id="showProductSelection"></div>
                        ${ViewHelper.getDictionaryValues(icsrProfileConfInstance, DictionaryTypeEnum.PRODUCT)}
                    </div>
                </g:if>
                <g:else>
                    <div class="col-md-2">
                        <label><g:message code="app.label.productSelection"/></label>

                        <div></div>
                        <g:message code="app.label.none"/>
                    </div>
                </g:else>
                    <div class="col-md-2">
                        <g:if test="${icsrProfileConfInstance.productSelection || icsrProfileConfInstance.validProductGroupSelection}">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label>
                                        <g:message code="app.label.productDictionary.include.who.drugs"/>
                                    </label>

                                    <div><g:formatBoolean boolean="${icsrProfileConfInstance?.includeWHODrugs}"
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
                                        <g:formatBoolean boolean="${icsrProfileConfInstance?.isMultiIngredient}"
                                                         true="${message(code: "default.button.yes.label")}"
                                                         false="${message(code: "default.button.no.label")}"/>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </div>
                <g:if test="${icsrProfileConfInstance.studySelection}">
                    <div class="col-md-2">
                        <label><g:message code="app.studyDictionary.label"/></label>

                        <div id="showStudySelection"></div>
                        ${ViewHelper.getDictionaryValues(icsrProfileConfInstance, DictionaryTypeEnum.STUDY)}
                    </div>
                </g:if>
                <g:else>
                    <div class="col-md-2">
                        <label><g:message code="app.label.studySelection"/></label>

                        <div></div>
                        <g:message code="app.label.none"/>
                    </div>
                </g:else>
                        <div class="col-md-2">
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="checkbox checkbox-primary">
                                        <g:checkBox id="includeProductObligation" disabled=""
                                                    name="includeProductObligation"
                                                    value="${icsrProfileConfInstance?.includeProductObligation}"
                                                    checked="${icsrProfileConfInstance?.includeProductObligation}"/>
                                        <label for="includeProductObligation">
                                            <g:message code="reportCriteria.include.product.obligation.cases"/>
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="checkbox checkbox-primary">
                                        <g:checkBox id="excludeNonValidCases" disabled=""
                                                    name="excludeNonValidCases"
                                                    value="${icsrProfileConfInstance?.excludeNonValidCases}"
                                                    checked="${icsrProfileConfInstance?.excludeNonValidCases}"/>
                                        <label for="excludeNonValidCases">
                                            <g:message code="reportCriteria.exclude.non.valid.cases"/>
                                        </label>
                                    </div>
                                </div>
                            </div>

                            %{--removed include comparator checkbox for PVR-22257--}%

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="checkbox checkbox-primary">
                                        <g:checkBox id="includeStudyObligation" disabled=""
                                                    name="includeStudyObligation"
                                                    value="${icsrProfileConfInstance?.includeStudyObligation}"
                                                    checked="${icsrProfileConfInstance?.includeStudyObligation}"/>
                                        <label for="includeStudyObligation">
                                            <g:message code="reportCriteria.include.study.obligation.cases"/>
                                        </label>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="checkbox checkbox-primary">
                                        <g:checkBox id="includeNonReportable" disabled=""
                                                    name="includeNonReportable"
                                                    value="${icsrProfileConfInstance?.includeNonReportable}"
                                                    checked="${icsrProfileConfInstance?.includeNonReportable}"/>
                                        <label for="includeNonReportable">
                                            <g:message code="reportCriteria.include.non.reportable.cases"/>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-4">
                            <div class="row">
                                <div class="col-md-12">
                                    <label><g:message code="app.label.icsr.profile.conf.authorizationType" /></label>
                                    <g:if test="${icsrProfileConfInstance.authorizationTypes}">
                                        <div>
                                            <g:renderIcsrAuthTypeName icsrAuthType="${icsrProfileConfInstance?.authorizationTypes}"></g:renderIcsrAuthTypeName>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div><g:message code="app.label.none"/></div>
                                    </g:else>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12">
                                    <div>
                                        <label><g:message code="app.label.downgradeQuery"/></label>
                                    </div>
                                    <g:if test="${isExecuted}">
                                        <g:if test="${icsrProfileConfInstance.executedGlobalQuery}">
                                            <div>
                                                <g:link controller="query" action="viewExecutedQuery"
                                                        id="${icsrProfileConfInstance.executedGlobalQuery.id}"
                                                        params="[isExecuted: 'true']">${icsrProfileConfInstance.executedGlobalQuery.name}</g:link>
                                            </div>
                                        </g:if>
                                        <g:else>
                                            <div><g:message code="app.label.none"/></div>
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        <g:if test="${icsrProfileConfInstance.globalQuery}">
                                            <div>
                                                <g:link controller="query" action="view"
                                                        id="${icsrProfileConfInstance.globalQuery.id}"
                                                        params="[isExecuted: 'false']">${icsrProfileConfInstance.globalQuery.name}</g:link>
                                            </div>
                                        </g:if>
                                        <g:else>
                                            <div><g:message code="app.label.none"/></div>
                                        </g:else>
                                    </g:else>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="app.label.parameters"/></label>

                                    <div>
                                        -- <g:message code="app.label.query"/> --
                                    </div>
                                    <g:if test="${isExecuted}">
                                        <g:if test="${icsrProfileConfInstance.executedGlobalQueryValueLists}">
                                            <g:each in="${icsrProfileConfInstance.executedGlobalQueryValueLists}">
                                                <div class="bold left-indent">
                                                    <g:if test="${icsrProfileConfInstance.executedGlobalQuery.class != ExecutedCustomSQLQuery}">
                                                        <span class="showQueryStructure showPopover"
                                                              data-content="${queryService.generateReadableQuery(icsrProfileConfInstance.executedGlobalQueryValueLists, icsrProfileConfInstance.executedGlobalQuery.JSONQuery, icsrProfileConfInstance, 0)}">${it.query.name}</span>
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
                                        <g:if test="${icsrProfileConfInstance.globalQueryValueLists}">
                                            <g:each in="${icsrProfileConfInstance.globalQueryValueLists}">
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
                                    <label><g:message code="app.label.poi.parameters"/></label>
                                    <g:if test="${icsrProfileConfInstance.poiInputsParameterValues}">
                                        <g:each in="${icsrProfileConfInstance.poiInputsParameterValues}"
                                                var="parameterValue">
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
                    </div>
                </div>

            </div>
        </div>

        <div class="rxmain-container rxmain-container-top">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    <g:message code="app.label.icsr.profile.conf.scheduling.criteria"/>
                </label>
            </div>

            <div class="rxmain-container-content rxmain-container-show">
                <g:each var="templateQuery" in="${templateQueries}" status="i">
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.to"
                                   value="${templateQuery?.emailConfiguration?.to}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.cc"
                                   value="${templateQuery?.emailConfiguration?.cc}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.body"
                                   value="${templateQuery?.emailConfiguration?.body}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.subject"
                                   value="${templateQuery?.emailConfiguration?.subject}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.deliveryReceipt"
                                   value="${templateQuery?.emailConfiguration?.deliveryReceipt}"/>
                    <div class="row">

                        <div class="col-md-2">
                            <div class="row">
                                <div class="col-md-12">
                                    <label><g:message code="app.label.icsr.profile.conf.reportForm"/></label>

                                    <div class="word-wrapper">
                                        <g:link controller="template" action="view"
                                                id="${templateQuery.template.id}">${templateQuery.template.name}</g:link>
                                    </div>
                                </div>

                        </div>
                        <g:if test="${templateQuery.title}">
                            <div class="sec-title">
                                <label><g:message code="app.label.description"/></label>

                                <div class="sec-value">
                                    ${templateQuery.title}
                                </div>
                            </div>
                        </g:if>
                        <g:if test="${templateQuery.privacyProtected}">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="templateQuery.privacyProtected.label"/></label>

                                        <div>
                                            <g:formatBoolean boolean="${templateQuery.privacyProtected}"
                                                             true="${message(code: "default.button.yes.label")}"
                                                             false="${message(code: "default.button.no.label")}"/>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${templateQuery.blindProtected}">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label><g:message code="templateQuery.blindProtected.label"/></label>

                                        <div>
                                            <g:formatBoolean boolean="${templateQuery.blindProtected}"
                                                             true="${message(code: "default.button.yes.label")}"
                                                             false="${message(code: "default.button.no.label")}"/>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </div>


                        <div class="col-md-2">
                            <label><g:message code="app.label.icsr.profile.conf.schedulingCriteria"/></label>

                            <div class="word-wrapper">
                                <g:if test="${templateQuery.query}">
                                    <g:link controller="query" action="view" id="${templateQuery.query.id}"
                                            params="[isExecuted: 'false']">${templateQuery.query.name}</g:link>
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.none"/>
                                </g:else>
                            </div>
                        </div>

                        <div class="col-md-2">
                            <label><g:message code="app.label.parameters"/></label>
                            <g:if test="${templateQuery.templateValueLists}">
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
                            </g:if>

                            <g:if test="${templateQuery.queryValueLists}">
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
                            </g:if>

                            <g:if test="${!templateQuery.templateValueLists && !templateQuery.queryValueLists}">
                                <div><g:message code="app.label.none"/></div>
                            </g:if>
                        </div>

                        <div class="col-md-1">
                            <label><g:message code="app.label.icsr.profile.conf.dueInDays"/></label>

                            <p>${templateQuery?.dueInDays}</p>
                        </div>

                        <div class="col-md-1">
                            <label>
                                <g:message code="app.label.icsr.profile.conf.expedited"/>
                            </label>

                            <p><g:formatBoolean boolean="${templateQuery.isExpedited}"
                                                true="${message(code: "default.button.yes.label")}"
                                                false="${message(code: "default.button.no.label")}"/></p>
                        </div>

                        <div class="col-md-2">
                            <label><g:message code="app.label.icsr.profile.conf.messageType"/></label>

                            <g:if test="${templateQuery?.icsrMsgType}">
                                <div>
                                    <g:renderIcsrMsgTypeName icsrMsgType="${templateQuery?.icsrMsgType}"></g:renderIcsrMsgTypeName>
                                </div>
                            </g:if>
                        </div>

                        <div class="col-md-2">
                            <label><g:message code="app.label.icsr.profile.conf.distributionChannel"/></label>

                            <p><g:if test="${templateQuery?.distributionChannelName}"><g:message
                                    code="${templateQuery.distributionChannelName.getI18nKey()}"/></g:if>
                                <span class="showEmailConfiguration" data-idx="${this.id}" data-evt-clk='{"method": "viewEmailConfiguration", "params": []}'
                                      id="showEmailConfiguration-${i}" <g:if
                                              test="${templateQuery?.distributionChannelName != DistributionChannelEnum.EMAIL}">style="cursor: pointer; display:none;"</g:if>><asset:image
                                        src="icons/email-secure.png"
                                        title="${message(code: 'default.button.addEmailConfigurationEdited.label')}"/></span>
                            </p>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>

        <div class="rxmain-container rxmain-container-top">

            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    <g:message code="app.label.icsr.profile.conf.additionalDetails"/>
                </label>
            </div>

            <div class="rxmain-container-content rxmain-container-show">
                <div class="row">
                    <div class="col-md-2">
                        <label><g:message code="app.label.sharedWith"/></label>
                        <g:if test="${icsrProfileConfInstance?.deliveryOption?.sharedWith || icsrProfileConfInstance?.deliveryOption?.sharedWithGroup}">
                            <g:if test="${icsrProfileConfInstance?.deliveryOption?.sharedWith}">
                                <g:each in="${icsrProfileConfInstance?.deliveryOption?.sharedWith}">
                                    <div>${it.reportRequestorValue}</div>
                                </g:each>
                            </g:if>
                            <g:if test="${icsrProfileConfInstance?.deliveryOption?.sharedWithGroup}">
                                <g:each in="${icsrProfileConfInstance?.deliveryOption?.sharedWithGroup}">
                                    <div>${it.name}</div>
                                </g:each>
                            </g:if>
                        </g:if>
                        <g:else>
                            <div><g:message code="app.label.none"/></div>
                        </g:else>
                    </div>

                    <div class="col-md-2">
                        <g:render template="includes/showEmailDetails"
                                  model="[instance: icsrProfileConfInstance, deliveryOption: icsrProfileConfInstance?.deliveryOption]"/>
                    </div>

                    <div class="col-md-2">
                        <label><g:message code="app.label.description"/></label>

                        <g:if test="${icsrProfileConfInstance.description}">
                            <div class="word-wrapper">${icsrProfileConfInstance.description}</div>
                        </g:if>
                        <g:else>
                            <div>
                                <g:message code="app.label.none"/>
                            </div>
                        </g:else>
                    </div>

                    <div class="col-md-2">
                        <label><g:message code="app.label.qualityChecked"/></label>
                        <div>
                            <g:formatBoolean boolean="${icsrProfileConfInstance.qualityChecked}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <g:if test="${icsrProfileConfInstance.hasAnyE2BDistributionChannel() || icsrProfileConfInstance.hasEmailAsE2BDistributionChannel()}">
            <div class="rxmain-container rxmain-container-top">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="app.label.icsr.profile.conf.e2b.distribution.settings"/>
                    </label>
                </div>

                <div class="rxmain-container-content rxmain-container-show">
                    <div class="row">
                        <div class="col-md-2">
                            <label><g:message code="app.label.icsr.profile.conf.reportFormat"/></label>

                            <p><g:if test="${icsrProfileConfInstance?.e2bDistributionChannel?.reportFormat}"><g:message
                                    code="${icsrProfileConfInstance.e2bDistributionChannel?.reportFormat?.getI18nKey()}"/></g:if></p>
                        </div>

                        <g:if test="${icsrProfileConfInstance.hasAnyE2BDistributionChannel()}">
                            <div class="col-md-4">
                                <label><g:message code="app.label.icsr.profile.conf.outgoingFolder"/></label>

                                <p class="word-wrapper">${icsrProfileConfInstance?.e2bDistributionChannel?.outgoingFolder?.substring(icsrProfileConfInstance?.e2bDistributionChannel?.outgoingFolder?.indexOf('/outgoing'))}</p>
                            </div>

                            <div class="col-md-4">
                                <label><g:message code="app.label.icsr.profile.conf.incomingFolder"/></label>

                                <p class="word-wrapper">${icsrProfileConfInstance?.e2bDistributionChannel?.incomingFolder?.substring(icsrProfileConfInstance?.e2bDistributionChannel?.incomingFolder?.indexOf('/incoming'))}</p>
                            </div>
                        </g:if>

                        %{-- <div class="col-md-2">
                             <div class="checkbox checkbox-primary">
                                 <g:checkBox id="needPaperReport" disabled=""
                                             name="needPaperReport"
                                             value="${icsrProfileConfInstance?.needPaperReport}"
                                             checked="${icsrProfileConfInstance?.needPaperReport}"/>
                                 <label for="needPaperReport">
                                     <g:message code="app.label.icsr.profile.conf.need.paper.report"/>
                                 </label>
                             </div>
                         </div>--}%

                    </div>
                </div>
            </div>
        </g:if>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <button class="btn btn-primary"
                            data-evt-clk='{"method": "goToUrl", "params": ["icsrProfileConfiguration", "edit", {"id": ${params.id}}]}' id="editBtn">
                        ${message(code: "default.button.edit.label")}
                    </button>
                    <button type="button" class="btn pv-btn-grey"
                            data-evt-clk='{"method": "goToUrl", "params": ["icsrProfileConfiguration", "copy", {"id": ${params.id}}]}' id="copyBtn">
                        ${message(code: "default.button.copy.label")}
                    </button>
                    <button url="#" data-toggle="modal" data-target="#deleteModal"
                            data-instancetype="icsrProfileConfiguration"
                            data-instanceid="${params.id}" data-instancename="${icsrProfileConfInstance?.reportName}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
                </div>
            </div> 
        </div>
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

        <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${icsrProfileConfInstance}" var="theInstance"/>
        <g:render template="/configuration/includes/emailConfigurationDistributionChannel"
                  model="[emailConfiguration: icsrProfileConfInstance?.emailConfiguration, editable: false]"/>
    </rx:container>
</div>

<g:form controller="${controller}" method="delete">
<g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>
</html>
