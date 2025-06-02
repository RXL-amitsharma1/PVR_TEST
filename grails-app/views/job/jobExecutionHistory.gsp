<%@ page import="com.rxlogix.Constants;" %>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.jobExecutionHistory.title"/></title>
    <style>
    table.dataTable thead tr > th{
        text-align: center;
    }
    table.dataTable tbody {
        text-align: center;
    }
    </style>
    <asset:javascript src="jquery.shorten.js"/>
    <asset:javascript src="dataTablesActionButtons.js"/>
    <asset:javascript src="/app/jobExecutionHistory.js"/>
    <g:javascript>
        listUrl = "${createLink(controller: 'jobExecutionHistoryRest', action: 'list')}?jobTitle=${Constants.AUTO_REASON_OF_DELAY}";
    </g:javascript>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.jobExecutionHistory.title")}">
                <div class="pv-caselist">
                    <table id="rxTableJobExecutionHistory" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th style="width: 20%"><g:message code="app.jobExecutionHistory.label.jobTitle"/></th>
                            <th style="width: 10%"><g:message code="app.jobExecutionHistory.label.jobStartRunDate"/></th>
                            <th style="width: 10%"><g:message code="app.jobExecutionHistory.label.jobEndRunDate"/></th>
                            <th style="width: 10%"><g:message code="app.jobExecutionHistory.label.jobRunStatus"/></th>
                            <th style="width: 40%"><g:message code="app.jobExecutionHistory.label.jobRunRemarks"/></th>
                            <th style="width: 10%"><g:message code="app.label.dateCreated"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
