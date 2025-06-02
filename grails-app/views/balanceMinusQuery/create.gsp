<%@ page import="com.rxlogix.config.SourceProfile;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.balanceMinusQueryStatus.title"/></title>
    <g:javascript>
        var USERGROUP = {
             ajaxProfileSearchUrl: "${createLink(controller: 'userGroup', action: 'ajaxProfileSearch')}"
        },
        querySearchUrl = "${createLink(controller: 'queryRest', action: 'getQueryList')}?notBlank=true",
        queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}",
        dashboardURL = "${createLink(controller: 'dashboard', action: 'index')}",
        validateValue="${createLink(controller: 'query', action: 'validateValue')}",
        importExcel="${createLink(controller: 'quality', action: 'importExcel')}",
        distTablesUrl = "${createLink(controller: 'balanceMinusQuery', action: 'fetchDistTables')}"
    </g:javascript>
    <asset:javascript src="vendorUi/jquery/jquery.ui.widget.js"/>
    <asset:javascript src="vendorUi/jquery/jquery-picklist.js"/>
    <asset:stylesheet href="vendorUi/jquery-picklist.css"/>
    <asset:stylesheet href="user-group.css"/>
    <asset:javascript src="app/balanceAndMinus/balanceScheduler.js"/>
    <asset:javascript src="app/balanceAndMinus/copyPasteValues.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="balance-minus-query.css"/>

</head>

<body>

    <div class="col-md-12">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${bmQueryInstance}" var="theInstance"/>

        <g:set var="creatable" value="true"/>

        <g:form method="post" action="save" class="form-horizontal" autocomplete="off">

            <g:render template="/balanceMinusQuery/include/form" model="[bmQueryInstance: bmQueryInstance, sourceProfiles: sourceProfiles, centralSource: centralSource]"/>


            <div class="buttonBar" style="text-align: right">
                <button type="submit" class="btn btn-primary" id="saveButton">${message(code: "default.button.save.label")}</button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["balanceMinusQuery", "index"]}' id="cancelButton">${message(code: "default.button.cancel.label")}</button>
            </div>


        </g:form>
        <div>
            <g:render template='/balanceMinusQuery/include/bmQuerySection' model="['bmQuerySection': null, 'i': '_clone', 'hidden': true]"></g:render>
        </div>
    </div>
</body>
</html>