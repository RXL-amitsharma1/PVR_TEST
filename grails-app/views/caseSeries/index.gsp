<%@ page import="com.rxlogix.config.CaseSeries" %>
<!doctype html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.caseSeries.library.title"/></title>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>

    <g:javascript>
        var caseSeriesErrorUrl = "${createLink(controller: 'caseSeries', action: 'show')}";
        var listCaseSeries = "${createLink(controller: 'caseSeriesRest', action: 'index')}";
        var viewCasesURL = "${createLink(controller: 'caseList', action: 'index')}";
        var runNowUrl = "${createLink(controller: 'caseSeries', action: 'runNow')}";
        var showURL = "${createLink(controller: 'caseSeries', action: 'show')}";
        var editURL = "${createLink(controller: 'caseSeries', action: 'edit')}";
        var deleteURL = "${createLink(controller: 'caseSeries', action: 'delete')}";
        var copyUrl = "${createLink(controller: 'caseSeries', action: 'copy')}";
        var toFavorite = "${createLink(controller: 'caseSeries', action: 'favorite')}";
        var ownerListUrl = "${createLink(controller: 'userRest', action: 'ownerFilterList', params: [clazz:CaseSeries.name])}";
        var ownerValuesUrl = "${createLink(controller: 'userRest', action: 'userValue')}";
        var caseSeriesScheduledUrl = "${createLink(controller: 'caseSeriesRest', action: 'scheduledCaseSeriesList')}";
        var ShowScheduledConfigUrl = "${createLink(controller: 'executionStatus', action: 'viewScheduledConfig')}";
        var unscheduleUrl = "${createLink(controller: 'caseSeriesRest', action: 'unschedule')}";
    </g:javascript>
    <asset:javascript src="app/caseSeriesList.js"/>
    <asset:javascript src="app/caseSeriesScheduledList.js"/>
<style>
#newTabContent .active a {
    color: #fff!important;
    padding: 0 10px;
    background: #4c5667!important;
    border: 0 solid #ccc!important;
    margin-top: 5px!important;
    border-radius: 18px!important;
    line-height: 25px!important;
    font-weight: 300!important;
    margin-right: 5px !important;
    margin-left: 5px !important;
}

#newTabContent li > a {
    font-size: 12px!important;
    padding: 0 10px;
    color: #414658;
    line-height: 23px!important;
    font-weight: 600!important;
    border: 1px solid #b9b5b5!important;
    background: 0 0!important;
    margin-top: 5px!important;
    border-radius: 18px!important;
    margin-right: 5px !important;
    margin-left: 5px !important;
}

.nav.nav-tabs + .tab-content {
    padding: 0px;
}
.tab-content  {
    color:black;
}
.dt-layout-row:first-child > .col-xs-7:first-child{
    width: 45%;
}
#caseSeriesList_wrapper .searchToolbar .pull-right.col-xs-8 {
    max-width: 500px !important;
}
</style>
</head>

<body>
<div class="content ">
    <div class="container ">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${seriesInstance}" var="theInstance"/>
            <rx:container title="${message(code: "caseSeries.configuration.library.label")}" options="true" filterButton="true">

            <ul class="nav nav-tabs" id="newTabContent" role="tablist" style="margin-left: 270px; border: 0;margin-top: -36px;">
                <li role="presentation" class="active" style="margin-top: -8px;"><a href="#configuredCaseSeriesList" aria-controls="configuredCaseSeriesList" role="tab"
                                                          data-toggle="tab">
                    <g:message code="app.case.series.library.label"/></a></li>
                <li role="presentation" class="" style="margin-top: -8px;"><a href="#scheduledCaseSeriesList" aria-controls="scheduledCaseSeriesList" role="tab"
                                                    data-toggle="tab">
                    <g:message code="app.scheduled.case.series.label"/></a></li>
            </ul>
            <div class="topControls">
                <g:render template="/includes/widgets/sharedWithFilter" model="[clazz: CaseSeries.name]"/>
            </div>
            <div class="tab-content">
                <div role="tabpanel" class="tab-pane active" id="configuredCaseSeriesList">
                    <div class="pv-caselist">
                        <table id="caseSeriesList" class="table table-striped pv-list-table dataTable no-footer"
                               style="width: 100%;">
                            <thead>
                            <tr>
                                <th style="font-size: 16px"><span class="glyphicon glyphicon-star"></span></th>
                                <th style="min-width: 200px"><g:message code="caseSeries.name.label"/></th>
                                <th style="min-width: 200px"><g:message code="caseSeries.description.label"/></th>
                                <th style="min-width: 150px"><g:message code="app.label.tag"/></th>
                                <th style="width: 70px"><g:message code="app.label.runTimes"/></th>
                                <th style="width: 70px"><g:message code="app.label.qc" default="QCed"/></th>
                                <th style="width: 150px;"><g:message code="app.label.dateCreated"/></th>
                                <th style="width: 150px;"><g:message code="app.label.dateModified"/></th>
                                <th style="width: 100px;"><g:message code="app.label.owner"/></th>
                                <th style="width: 80px;"><g:message code="app.label.action"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane" id="scheduledCaseSeriesList">
                    <div class="pv-caselist">
                        <table id="rxTableCaseSerisExecutionStatus" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                            <thead>
                            <tr>
                                <th><g:message code="caseSeries.name.label"/></th>
                                <th><g:message code="app.label.version"/></th>
                                <th><g:message code="app.label.executionStatus"/></th>
                                <th><g:message code="app.label.owner"/></th>
                                <th><g:message code="app.label.runDate"/></th>
                                <th><g:message code="app.label.frequency"/></th>
                                <th><g:message code="app.label.sharedWith"/></th>
                                <th><g:message code="app.label.deliveryMedia"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>

</rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
    </div>
</div>
</body>
<html>