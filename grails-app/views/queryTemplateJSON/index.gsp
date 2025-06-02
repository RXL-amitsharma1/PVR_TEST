<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.download.json.title"/></title>
    <asset:javascript src="app/queryTemplate/queryTemplateList.js"/>
    <g:javascript>
        var downloadQueryJSONUrl = "${createLink(controller: 'queryTemplateJSON', action: 'downloadQueryJSON', absolute: true)}";
        var downloadTemplateJSONUrl = "${createLink(controller: 'queryTemplateJSON', action: 'downloadTemplateJSON', absolute: true)}";
        var downloadConfigurationJSONUrl = "${createLink(controller: 'queryTemplateJSON', action: 'downloadConfigurationJSON', absolute: true)}";
        var downloadDashboardJSONUrl = "${createLink(controller: 'queryTemplateJSON', action: 'downloadDashboardJSON', absolute: true)}";
        var viewQueryUrl = "${createLink(controller: 'query', action: 'view', absolute: true)}";
        var viewTemplateUrl = "${createLink(controller: 'template', action: 'view', absolute: true)}";
        var viewConfigurationUrl = "${createLink(controller: 'queryTemplateJSON', action: 'viewConfiguration', absolute: true)}";
        var viewDashboardUrl = "${createLink(controller: 'dashboardDictionary', action: 'show', absolute: true)}";
        var renderFileUrl = "${createLink(controller: 'queryTemplateJSON', action: 'renderFile')}"
    </g:javascript>


%{--    <asset:stylesheet src="user-group.css"/>--}%
    <style>
    .dt-layout-row:first-child {
        margin-top: 5px; padding-right:0px;
    }
    .dt-layout-row:last-child {
        margin-top: 5px;
    }
    </style>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
<rx:container title="${message(code: "app.label.download.json")}">
    <div class="body">
        <div id="action-list-conainter" class="list pv-caselist">
            <g:render template="/includes/layout/flashErrorsDivs"/>

            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="active"><a href="#queryTab" aria-controls="queryTab" role="tab"
                                                          data-toggle="tab">
                    <g:message code="app.QueryLibrary.label"/></a></li>
                <li role="presentation" class=""><a href="#templateTab" aria-controls="templateTab" role="tab"
                                                    data-toggle="tab">
                    <g:message code="app.TemplateLibrary.label"/></a></li>
                <li role="presentation" class=""><a href="#configurationTab" aria-controls="configurationTab" role="tab"
                                                    data-toggle="tab">
                    <g:message code="app.case.series.library.label"/></a></li>
                <li role="presentation" class=""><a href="#dashboardTab" aria-controls="dashboardTab" role="tab"
                                                    data-toggle="tab">
                    <g:message code="app.DashboardLibrary.label"/></a></li>
            </ul>


            <div class="tab-content">
                <div role="tabpanel" class="tab-pane active" id="queryTab">
                    <div class="navScaffold">
                        <button type="button" class="btn btn-primary"
                                id="downloadQueryJSONbtn">
                            <span class="glyphicon glyphicon-download icon-white"></span>
                            <g:message code="default.button.download.label"/>
                        </button>
                    </div>

                    <div>
                        <table id="queryListTable" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th style="vertical-align: middle;text-align: left; min-width: 30px">
                                    <div class="">
                                        <g:checkBox class="selectAll" name="selectAll" value="queryListTable" checked="false"/>
                                    </div>
                                </th>
                                <th><g:message default="Type" code="app.label.type"/></th>
                                <th><g:message default="Name" code="app.label.queryName"/></th>
                                <th><g:message default="Quality Checked" code="app.label.qualityChecked"/></th>
                                <th><g:message default="Owner" code="app.label.owner"/></th>
                                <th><g:message default="Date Created" code="app.label.dateCreated"/></th>
                                <th><g:message default="Date Modified" code="app.label.dateModified"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane" id="templateTab">
                    <div id="priorityContent">
                        <div class="navScaffold">
                            <button type="button" class="btn btn-primary"
                                    id="downloadTemplateJSONbtn">
                                <span class="glyphicon glyphicon-download icon-white"></span>
                                <g:message code="default.button.download.label"/>
                            </button>
                        </div>

                        <table id="templateListTable" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th style="vertical-align: middle;text-align: left; min-width: 30px">
                                    <div class="">
                                        <g:checkBox class="selectAll" name="selectAll" value="templateListTable" checked="false"/>
                                    </div>
                                </th>
                                <th><g:message default="Type" code="app.label.type"/></th>
                                <th><g:message default="Name" code="app.label.templateName"/></th>
                                <th><g:message default="Quality Checked" code="app.label.qualityChecked"/></th>
                                <th><g:message default="Owner" code="app.label.owner"/></th>
                                <th><g:message default="Date Created" code="app.label.dateCreated"/></th>
                                <th><g:message default="Date Modified" code="app.label.dateModified"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane" id="configurationTab">
                    <div class="navScaffold">
                        <button type="button" class="btn btn-primary"
                                id="downloadConfigurationJSONbtn">
                            <span class="glyphicon glyphicon-download icon-white"></span>
                            <g:message code="default.button.download.label"/>
                        </button>
                    </div>

                    <div>
                        <table id="configurationListTable" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th style="vertical-align: middle;text-align: left; min-width: 30px">
                                    <div class="">
                                        <g:checkBox class="selectAll" name="selectAll" value="configurationListTable" checked="false"/>
                                    </div>
                                </th>
                                <th><g:message default="Type" code="app.label.type"/></th>
                                <th><g:message default="Name" code="app.label.name"/></th>
                                <th><g:message default="Quality Checked" code="app.label.qualityChecked"/></th>
                                <th><g:message default="Owner" code="app.label.owner"/></th>
                                <th><g:message default="Date Created" code="app.label.dateCreated"/></th>
                                <th><g:message default="Date Modified" code="app.label.dateModified"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>


                <div role="tabpanel" class="tab-pane " id="dashboardTab">
                <div class="navScaffold">
                    <button type="button" class="btn btn-primary"
                            id="downloadDashboardJSONbtn">
                        <span class="glyphicon glyphicon-download icon-white"></span>
                        <g:message code="default.button.download.label"/>
                    </button>
                </div>

                <div>
                    <table id="dashboardListTable" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                        <thead>
                        <tr>
                            <th></th>
                            <th style="vertical-align: middle;text-align: left; min-width: 30px">
                                <div class="">
                                    <g:checkBox class="selectAll" name="selectAll" value="dashboardListTable" checked="false"/>
                                </div>
                            </th>
                            <th><g:message default="Type" code="app.label.type"/></th>
                            <th><g:message default="Name" code="app.label.name"/></th>
                            <th><g:message default="Quality Checked" code="app.label.qualityChecked"/></th>
                            <th><g:message default="Owner" code="app.label.owner"/></th>
                            <th><g:message default="Date Created" code="app.label.dateCreated"/></th>
                            <th><g:message default="Date Modified" code="app.label.dateModified"/></th>
                        </tr>
                        </thead>
                    </table>
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
