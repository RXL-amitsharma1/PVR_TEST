<%@ page import="com.rxlogix.util.DateUtil; grails.util.Holders" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.comparison.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/comparisonList.js"/>

    <g:javascript>
        var queueListURL = "${createLink(controller: 'comparison', action: 'listQueue')}";
        var resultListURL = "${createLink(controller: 'comparison', action: 'listResults')}";
        var viewReportURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var viewConfigURL = "${createLink(controller: 'configuration', action: 'view')}";
        var viewComporisonURL = "${createLink(controller: 'comparison', action: 'comparison')}";
        var executedCaseSeriesListUrl = "${createLink(controller: 'executedCaseSeriesRest', action: 'getExecutedCaseSeriesList')}";
        var executedCaseSeriesItemUrl = "${createLink(controller: 'executedCaseSeriesRest', action: 'getExecutedCaseSeriesItem')}";
    </g:javascript>
    <style>
    .dt-layout-row:first-child {margin-top: 5px; padding-right:0px;}
    </style>
</head>

<body>
<g:set var="userService" bean="userService"/>
<g:set var="currentDate" value="${DateUtil.StringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT, userService.currentUser?.preference?.timeZone)}"/>
<g:set var="currentTime" value="${DateUtil.StringFromDate(new Date(), "hh:mm a", userService.currentUser?.preference?.timeZone)}"/>
<div class="content">
    <div class="container ">
        <div>
            <rx:container title="${message(code: 'app.comparison.reportsComparison')}">
                <div class="body">
                    <g:render template="/includes/layout/flashErrorsDivs" var="theInstance"/>
                    <ul class="nav nav-tabs" role="tablist">
                        <li role="presentation" class="${tab == "result" ? "active" : ""}"><a href="#result" aria-controls="users" role="tab" data-toggle="tab"><g:message code="app.comparison.results"/></a>
                        </li>
                        <li role="presentation" class="${tab == "queue" ? "active" : ""}"><a href="#queue" aria-controls="roles" role="tab" data-toggle="tab"><g:message code="app.comparison.queue"/></a>
                        </li>
                    </ul>

                    <!-- Tab panes -->
                    <div class="tab-content">

                        <div role="tabpanel" class="tab-pane ${tab == "result" ? "active" : ""}" id="result">

                            <g:form action="bulkCompare">
                                <div class="row" style="margin:5px; padding:5px; border: 1px solid #cccccc; border-radius: 10px ">
                                    <input value="queue" name="tab" type="hidden">
                                    <div class="col-md-4">
                                        <label><g:message code="app.comparison.reportsId"/></label>
                                        <input class="form-control" name="ids" value="">
                                    </div>

                                    <div class="col-md-3">
                                        <label><g:message code="next.run.date"/></label>
                                        <div class="row">
                                            <div class="col-md-8">
                                                <div class="fuelux">
                                                    <div class="datepicker" id="nextRunDateDatePicker">
                                                        <div class="input-group">
                                                            <g:textField name="nextRunDate"
                                                                         class="form-control"
                                                                         value="${currentDate }"/>
                                                            <g:render template="/includes/widgets/datePickerTemplate"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <input name="time" class="form-control" placeholder="00:00 am" value="${currentTime}">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-2">
                                        <label><g:message code="app.comparison.prefix"/></label>
                                        <input class="form-control" name="prefix" value="${Holders.config.report.comparison.prefix}">
                                    </div>
                                    <div class="col-md-2" style="margin-top: 20px">

                                        <button type="submit" class="btn btn-primary showLoader">
                                            <g:message code="app.comparison.rerun"/>
                                        </button>
                                    </div>
                                </div>
                            </g:form>
                            <g:form action="bulkCompare">
                                <div class="row" style="margin:5px; padding:5px; border: 1px solid #cccccc; border-radius: 10px ">
                                    <input value="queue" name="tab" type="hidden">
                                    <div class="col-md-2">
                                        <label><g:message code="app.from.date"/> </label>
                                        <div class="fuelux">
                                            <div class="datepicker" id="fromDate">
                                                <div class="input-group">
                                                    <g:textField name="fromDate"
                                                                 class="form-control"
                                                                 value="${currentDate}"/>
                                                    <g:render template="/includes/widgets/datePickerTemplate"/>

                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-1">
                                        <input name="fromTime" class="form-control" placeholder="00:00 am" value="${currentTime}" style="margin-top: 25px">
                                    </div>
                                    <div class="col-md-2">
                                        <label><g:message code="toDate.label"/></label>
                                        <div class="fuelux">
                                            <div class="datepicker" id="toDate">
                                                <div class="input-group">
                                                    <g:textField name="toDate"
                                                                 class="form-control"
                                                                 value="${currentDate}"/>
                                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-1">
                                        <input name="toTime" class="form-control" placeholder="00:00 am" value="${currentTime}" style="margin-top: 25px">
                                    </div>
                                    <div class="col-md-3">
                                        <label><g:message code="next.run.date"/> </label>
                                        <div class="row">
                                            <div class="col-md-8">
                                                <div class="fuelux">
                                                    <div class="datepicker" id="nextRunDateDatePicker2">
                                                        <div class="input-group">
                                                            <g:textField name="nextRunDate"
                                                                         class="form-control"
                                                                         value="${currentDate}"/>
                                                            <g:render template="/includes/widgets/datePickerTemplate"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <input name="time" class="form-control" placeholder="00:00 am" value="${currentTime}">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-1">
                                        <label><g:message code="app.comparison.prefix"/></label>
                                        <input class="form-control" name="prefix" value="${Holders.config.report.comparison.prefix}">
                                    </div>
                                    <div class="col-md-2" style="margin-top: 20px">
                                        <button type="submit" class="btn btn-primary showLoader">
                                            <g:message code="app.comparison.rerun"/>
                                        </button>
                                    </div>
                                </div>
                            </g:form>
                            <g:form action="compare">
                                <div class="row" style="margin:5px; padding:5px; border: 1px solid #cccccc; border-radius: 10px ">
                                    <input value="queue" name="tab" type="hidden">
                                    <div class="col-md-2">
                                        <label><g:message code="app.comparison.reportResultId"/></label>
                                        <input class="form-control" name="id1">
                                    </div>
                                    <div class="col-md-2">
                                        <label><g:message code="app.comparison.reportResultId"/></label>
                                        <input class="form-control" name="id2">
                                    </div>
                                    <div class="col-md-3" style="margin-top: 20px">
                                        <button type="submit" class="btn btn-primary showLoader">
                                            <g:message code="app.comparison.compare"/>
                                        </button>
                                    </div>
                                </div>
                            </g:form>
                            <div class="list">
                                <div class="pv-caselist">
                                    <table id="resultList" class="table table-striped pv-list-table dataTable no-footer">
                                        <thead>
                                        <tr>
                                            <th><g:message code="app.comparison.source"/></th>
                                            <th><g:message code="app.comparison.clone"/></th>
                                            <th><g:message code="app.comparison.state"/></th>
                                            <th><g:message code="app.comparison.compared"/></th>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>

                        </div>
                        <div role="tabpanel" class="tab-pane ${tab == "queue" ? "active" : ""}" id="queue">
                            <div class="row">

                                <div id="action-list-conainter" class="list">

                                    <div class="pv-caselist">
                                        <table id="queueList" class="table table-striped pv-list-table dataTable no-footer">
                                            <thead>
                                            <tr>
                                                <th width="15%"><g:message code="app.comparison.source"/></th>
                                                <th width="15%"><g:message code="app.comparison.cloneConfig"/></th>
                                                <th width="15%"><g:message code="app.comparison.state"/></th>
                                                <th width="15%"><g:message code="app.comparison.created"/></th>
                                                <th><g:message code="app.comparison.message"/></th>
                                            </tr>
                                            </thead>
                                        </table>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                </div>
            </rx:container>

        </div>
    </div>
</div>
</body>
</html>
