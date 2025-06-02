<%@ page import="com.rxlogix.config.ReportTemplate; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.unitConfiguration.title"/></title>
    <asset:javascript src="app/configuration/unitConfiguration.js"/>
    <g:javascript>
        var UNITCONFIGURATION = {
             getAllUnitRegWithBasedOnParam: "${createLink(controller: 'unitConfiguration', action: 'getAllUnitRegWithBasedOnParam')}"

         }
        var getAllowedAttachments = "${createLink(controller: 'unitConfigurationRest', action: 'getAllowedAttachments')}";
        var checkXsltIsHl7Url = "${createLink(controller: 'unitConfiguration', action: 'checkXsltIsHl7')}"

    </g:javascript>
    <style>
    .form-horizontal .form-group {
        margin-left: -5px;
        margin-right: -5px;
    }
    .form-group .col-md-2{
        width: 20%;
    }
    </style>

</head>
<body>
  <div class="container">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${unitConfigurationInstance}" var="theInstance"/>

            <g:form name="unitConfigurationForm" method="post" action="save" autocomplete="off">

                <g:render template="includes/form" model="['mode':'create', unitConfigurationInstance: unitConfigurationInstance, icsrOrganizationTypeList: icsrOrganizationTypeList]"/>

                <div class="button" style="text-align: right">
                    <g:actionSubmit class="btn btn-primary unitSave" action="save" disabled="false"
                                    value="${message(code: 'default.button.save.label')}"/>
                    <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["unitConfiguration", "index"]}'
                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
            </g:form>
        </div>
  </div>
</body>
