<%@ page import="com.rxlogix.enums.UnitTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.enums.TimeZoneEnum;" contentType="text/html;charset=UTF-8" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>

<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.unitConfiguration.title"/></title>
    <script>
        $(function() {
            $(".taskTemplateField").prop("disabled", "disabled");
            $("#taskTable .glyphicon ").hide();
        })

    </script>
    <style>
    .row .col-md-2{
        width: 20%;
    }
    </style>
    <g:javascript>
        var getAllowedAttachments = "${createLink(controller: 'unitConfigurationRest', action: 'getAllowedAttachments')}";
    </g:javascript>

    <asset:javascript src="app/configuration/unitConfiguration.js"/>
</head>

<body>
<rx:container title="${message(code: message(code:"app.unitConfiguration.label.myInbox"))}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${unitConfigurationInstance}" var="theInstance"/>
    <div class="container-fluid">
        <div class="rxmain-container rxmain-container-top">
            <div class="rxmain-container-inner basicInformation" style="margin-bottom: 5px">
                <div class="rxmain-container-row rxmain-container-header">
                    <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                    <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                        <g:message code="app.label.icsr.profile.conf.basic.information"/>
                    </label>
                </div>
                <div class="rxmain-container-content rxmain-container-show">
                    <div class="row">
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.unitName"/></label>
                            <p>${unitConfigurationInstance?.unitName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.unitType"/></label>
                            <p><g:if test="${unitConfigurationInstance?.unitType}"><g:message code="${unitConfigurationInstance.unitType.getI18nKey()}"/></g:if></p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.organizationName"/></label>
                            <p>${unitConfigurationInstance?.unitOrganizationName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.organizationType"/></label>
                            <p>${ViewHelper.getOrganizationTypeByPreference(unitConfigurationInstance?.organizationType)}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.organizationCountry"/></label>
                            <p>${ViewHelper.getOrganizationCountryNameByPreference(unitConfigurationInstance?.organizationCountry)}
                            %{--                                    <g:if test="${unitConfigurationInstance?.organizationCountry}">--}%
                            %{--                                        <g:country code="${unitConfigurationInstance?.organizationCountry}"/>--}%
                            %{--                                    </g:if>--}%
                            </p>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.address1"/></label>
                            <p>${unitConfigurationInstance?.address1}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.address2"/></label>
                            <p>${unitConfigurationInstance?.address2}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.preferredLanguage"/></label>
                            <p>${preferredLanguage}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.city"/></label>
                            <p>${unitConfigurationInstance?.city}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.state"/></label>
                            <p>${unitConfigurationInstance?.state}</p>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.postalCode"/></label>
                            <p>${unitConfigurationInstance?.postalCode}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.postalCodeExt"/></label>
                            <p>${unitConfigurationInstance?.postalCodeExt}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.phone"/></label>
                            <p>${unitConfigurationInstance?.phone}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.email"/></label>
                            <p>${unitConfigurationInstance?.email}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.fax"/></label>
                            <p>${unitConfigurationInstance?.fax}</p>
                        </div>
                        <div class="col-md-2" style="display:${(unitConfigurationInstance?.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH])?'block':'none'};">
                            <label><g:message code="app.unitConfiguration.label.attachment"/></label>
                            <p><g:renderAllowedAttachments attachmentIds="${unitConfigurationInstance?.allowedAttachments}"/></p>
                        </div>
                        <div class="col-md-2 form-group reportSubDateDiv" id="disabledScheduler"
                             style="display:${(unitConfigurationInstance?.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH]) ? 'block' : 'none'};">
                            <label><g:message code="app.unitConfiguration.label.timeZone"/></label>

                            <p>${ViewHelper.getMessage(timeZone?.getI18nKey(), timeZone?.getGmtOffset())}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.unitRetired"/></label>
                            <p>
                                <g:formatBoolean boolean="${unitConfigurationInstance?.unitRetired}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                            </p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="rxmain-container-inner e2bDistributionSetting" style="margin-bottom: 5px">
                <div class="rxmain-container-row rxmain-container-header">
                    <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                    <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                        <g:message code="app.label.icsr.profile.conf.e2b.distribution.settings"/>
                    </label>
                </div>
                <div class="rxmain-container-content rxmain-container-show">
                    <div class="row">
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.unitRegisteredID"/></label>
                            <p>${unitConfigurationInstance?.unitRegisteredId}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.companyName"/></label>
                            <p>${unitConfigurationInstance?.organizationName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.xsltName"/></label>
                            <p>${unitConfigurationInstance?.xsltName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.holderId"/></label>
                            <p>${unitConfigurationInstance?.holderId}</p>
                        </div>
                        <div class="col-md-2" style="display:${(unitConfigurationInstance?.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH]) ? 'block' : 'none'};">
                            <label><g:message code="app.unitConfiguration.label.unitAttachmentRegId"/></label>
                            <p>${unitConfigurationInstance?.unitAttachmentRegId}</p>
                        </div>
                    </div>
                    <g:if test="${Hl7 == 'false'}">
                        <div class="row">
                            <div class="col-md-2">
                                <label><g:message code="app.unitConfiguration.label.xmlVersion"/></label>
                                <p>${unitConfigurationInstance?.xmlVersion}</p>
                            </div>
                            <div class="col-md-2">
                                <label><g:message code="app.unitConfiguration.label.xmlEncoding"/></label>
                                <p>${unitConfigurationInstance?.xmlEncoding}</p>
                            </div>
                            <div class="col-md-2">
                                <label><g:message code="app.unitConfiguration.label.xmlDoctype"/></label>
                                <p>${unitConfigurationInstance?.xmlDoctype}</p>
                            </div>
                        </div>
                     </g:if>
                </div>
            </div>
            <div class="rxmain-container-inner contactInformation" style="margin-bottom: 5px">
                <div class="rxmain-container-row rxmain-container-header">
                    <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                    <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                        <g:message code="app.label.icsr.profile.conf.contact.information"/>
                    </label>
                </div>
                <div class="rxmain-container-content rxmain-container-show">
                    <div class="row">
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.title"/></label>
                            %{--<p>${unitConfigurationInstance?.title}</p>--}%
                            <p><g:if test="${unitConfigurationInstance?.title}"><g:message code="${unitConfigurationInstance.title.getI18nKey()}"/></g:if></p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.firstName"/></label>
                            <p>${unitConfigurationInstance?.firstName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.middleName"/></label>
                            <p>${unitConfigurationInstance?.middleName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.lastName"/></label>
                            <p>${unitConfigurationInstance?.lastName}</p>
                        </div>
                        <div class="col-md-2">
                            <label><g:message code="app.unitConfiguration.label.department"/></label>
                            <p>${unitConfigurationInstance?.department}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <g:if test="${isExecuted != true}">
            <div class="row">
                <div class="col-xs-12">
                    <div class="pull-right">
                        <button class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["unitConfiguration", "edit", {"id": ${params.id}}]}' id="editBtn">
                                ${message(code: "default.button.edit.label")}
                        </button>
                    </div>
                </div>
            </div>
        </g:if>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${unitConfigurationInstance}" var="theInstance"/>

</rx:container>

</body>
</html>
