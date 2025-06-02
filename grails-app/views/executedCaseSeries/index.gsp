<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders; com.rxlogix.config.ExecutedCaseSeries" %>
<!doctype html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.caseSeries.executed.title"/></title>

    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>

    <g:javascript>
        var caseSeriesErrorUrl = "${createLink(controller: 'executedCaseSeries', action: 'show')}";
        var listCaseSeries = "${createLink(controller: 'executedCaseSeriesRest', action: 'index')}";
        var viewCasesURL = "${createLink(controller: 'caseList', action: 'index')}";
        var showURL = "${createLink(controller: 'executedCaseSeries', action: 'show')}";
        var deleteURL = "${createLink(controller: 'executedCaseSeries', action: 'delete')}";
        var toArchive = "${createLink(controller: 'executedCaseSeries', action: 'archive')}";
        var toFavorite = "${createLink(controller: 'executedCaseSeries', action: 'favorite')}";
        var addEmailConfiguration="${createLink(controller: "executedCaseSeries", action: "addEmailConfiguration")}";
        var getSharedWith = "${createLink(controller: 'executedCaseSeriesRest', action: 'getSharedWithUsers')}";
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:ExecutedCaseSeries.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var checkDeleteForAllAllowedURL = "${createLink(controller: 'executedCaseSeries', action: 'checkDeleteForAllAllowed')}";
        var spotfireFileViewUrl = "${createLink(controller: 'dataAnalysis', action:'view')}";
        var libraryRoot = "${Holders.config.spotfire.libraryRoot}";
        var dataAnalysisRoleGranted = "${SpringSecurityUtils.ifAnyGranted('ROLE_DATA_ANALYSIS')}"
    </g:javascript>
    <asset:javascript src="app/executedCaseSeriesList.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <script>
        $(function () {
            exCaseSeries.caseList.init_ex_case_list_table(listCaseSeries);
        });
    </script>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>

            <rx:container title="${message(code: "caseSeries.label")}" options="true" filterButton="true">
                <div class="topControls">
                    <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: ExecutedCaseSeries.name]"/>
                    <g:render template="/includes/widgets/archiveFilter"/>
                </div>
            <div class="pv-caselist">
                <table id="exCaseSeriesList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%;">
                    <thead>
                    <tr>
                        <th style="font-size: 16px"><span class="glyphicon glyphicon-star"></span></th>
                        <th style="min-width: 200px"><g:message code="caseSeries.name.label"/></th>
                        <th style="min-width: 200px"><g:message code="caseSeries.description.label"/></th>
                        <th style="min-width: 150px"><g:message code="app.label.tag"/></th>
                        <th style="width: 70px"><g:message code="app.label.version"/></th>
                        <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
                        <th style="width: 150px;"><g:message code="app.label.dateCreated"/></th>
                        <th style="width: 150px;"><g:message code="app.label.dateModified"/></th>
                        <th style="width: 100px;"><g:message code="app.label.owner"/></th>
                        <th style="width: 80px;"><g:message code="app.label.action"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
</rx:container>
<g:form controller="executedCaseSeries" data-evt-sbt='{"method": "submitForm", "params": []}'>
    <g:hiddenField name="executedConfigId"/>
    <g:render template="/report/includes/sharedWithModal"/>
    <g:render template="/report/includes/emailToModal"/>
</g:form>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
<g:render template="/includes/widgets/confirmation"/>
        </div>
    </div>
</div>
</body>
<html>
